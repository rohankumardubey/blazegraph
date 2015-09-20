/*

Copyright (C) SYSTAP, LLC 2006-2015.  All rights reserved.

Contact:
     SYSTAP, LLC
     2501 Calvert ST NW #106
     Washington, DC 20008
     licenses@systap.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/*
 * Created on Sep 9, 2015
 */

package com.bigdata.service.geospatial;

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.bigdata.btree.ITuple;
import com.bigdata.btree.KeyOutOfRangeException;
import com.bigdata.btree.filter.Advancer;
import com.bigdata.btree.keys.IKeyBuilder;
import com.bigdata.btree.keys.KeyBuilder;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.internal.IVUtility;
import com.bigdata.rdf.internal.impl.extensions.GeoSpatialLiteralExtension;
import com.bigdata.rdf.internal.impl.literal.LiteralExtensionIV;
import com.bigdata.rdf.model.BigdataLiteral;
import com.bigdata.rdf.model.BigdataValue;
import com.bigdata.rdf.spo.SPO;
import com.bigdata.util.BytesUtil;

/**
 * Advances the cursor to the next zOrderKey that is greater or equal than the
 * first point in the next region. Note that this next key is not necessarily a
 * hit (but, depending on the data) this might be a miss again.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 */
public class ZOrderIndexBigMinAdvancer extends Advancer<SPO> {

   private static final long serialVersionUID = -6438977707376228799L;

   private static final transient Logger log = Logger
         .getLogger(ZOrderIndexBigMinAdvancer.class);
   
   // Search min (upper left) byte array as z-order string (as unsigned)
   private final byte[] searchMinZOrder;

   // the long values representing search min's values in the dimensions
   private final long[] seachMinLong;
   
   // Search max (lower right) byte array as z-order string (as unsigned)
   private final byte[] searchMaxZOrder;
   
   // the long values representing search max's values in the dimensions
   private final long[] seachMaxLong;

   // the position within the index in which we find the zOrderComponent
   private final int zOrderComponentPos;
   
   // the GeoSpatialLiteralExtension object
   private final GeoSpatialLiteralExtension<BigdataValue> litExt;
   
   // counters object for statistics
   final GeoSpatialCounters geoSpatialCounters;
   
   // pre-allocated, reusable byte[] for bigmin calculation
   final int zOrderArrayLength;
   final byte[] min;
   final byte[] max;
   byte[] bigmin;
   
   
   private transient IKeyBuilder keyBuilder;

   public ZOrderIndexBigMinAdvancer(
      final byte[] searchMinZOrder, /* the minimum search key (top left) */
      final byte[] searchMaxZOrder, /* the maximum search key (bottom right) */
      final GeoSpatialLiteralExtension<BigdataValue> litExt,
      final int zOrderComponentPos /* position of the zOrder in the index */,
      final GeoSpatialCounters geoSpatialCounters) {

      this.litExt = litExt;
      this.searchMinZOrder = litExt.unpadLeadingZero(searchMinZOrder);
      this.seachMinLong = litExt.fromZOrderByteArray(this.searchMinZOrder);
      
      this.searchMaxZOrder = litExt.unpadLeadingZero(searchMaxZOrder);
      this.seachMaxLong = litExt.fromZOrderByteArray(this.searchMaxZOrder);
      
      this.zOrderComponentPos = zOrderComponentPos;
      this.geoSpatialCounters = geoSpatialCounters;
      
      zOrderArrayLength = this.searchMinZOrder.length;
      min = new byte[zOrderArrayLength];
      max = new byte[zOrderArrayLength];
      bigmin = new byte[zOrderArrayLength];
      
   }
   
   @SuppressWarnings("rawtypes")
   @Override
   protected void advance(final ITuple<SPO> tuple) {

      if (keyBuilder == null) {
         keyBuilder = KeyBuilder.newInstance();
      }
      
      // iterate unless tuple in range is found or we reached the end
      ITuple<SPO> curTuple = tuple; 
      while(curTuple!=null) {
         
         if (log.isDebugEnabled()) {
            log.debug("Advancor visiting tuple:    " + curTuple);
         }
         
         final long rangeCheckCalStart = System.nanoTime();
         
         final byte[] key = curTuple.getKey();
   
         keyBuilder.reset();
   
         // decode components up to (and including) the z-order string
         final IV[] ivs = IVUtility.decode(key,zOrderComponentPos+1);
         
         // encode everything up to (and excluding) the z-order component "as is"
         for (int i=0; i<ivs.length-1; i++) {
            IVUtility.encode(keyBuilder, ivs[i]);
         }
   
         // this is the z-order literal
         @SuppressWarnings("unchecked")
         final LiteralExtensionIV<BigdataLiteral> zOrderIv = 
            (LiteralExtensionIV<BigdataLiteral>)ivs[ivs.length-1];
         
         // current record (aka dividing record) as unsigned
         final byte[] dividingRecord = 
            litExt.toZOrderByteArray(zOrderIv.getDelegate());

         
         long[] divRecordComponents = litExt.fromZOrderByteArray(dividingRecord);
         
         boolean inRange = true;
         for (int i=0; i<divRecordComponents.length && inRange; i++) {
            inRange &= seachMinLong[i]<=divRecordComponents[i];
            inRange &= seachMaxLong[i]>=divRecordComponents[i];
         }

         
         final long rangeCheckCalEnd = System.nanoTime();
         geoSpatialCounters.addRangeCheckCalculationTime(rangeCheckCalEnd-rangeCheckCalStart);
   
         if (!inRange) {


            // this is a miss
            geoSpatialCounters.registerZOrderIndexMiss();
            
            long bigMinCalStart = System.nanoTime();
            
            if (log.isDebugEnabled()) {
               log.debug("-> tuple " + curTuple + " not in range");
            }

            // calculate bigmin over the z-order component
            final byte[] bigMin = calculateBigMin(dividingRecord);
            
            // pad a zero
            final LiteralExtensionIV bigMinIv = litExt.createIVFromZOrderByteArray(bigMin);
            IVUtility.encode(keyBuilder, bigMinIv);
   
            final long bigMinCalEnd = System.nanoTime();
            geoSpatialCounters.addBigMinCalculationTime(bigMinCalEnd-bigMinCalStart);
            
            // advance to the specified key ...
            try {
               if (log.isDebugEnabled()) {
                  log.debug("-> advancing to bigmin: " + bigMinIv);
               }

               ITuple<SPO> next = src.seek(keyBuilder.getKey());
                     
               // ... or the next higher one
               if (next==null) {
                  next = src.next(); 
               }
      
               // continue iterate
               curTuple = next;

            }  catch (NoSuchElementException e) {
     
               throw new KeyOutOfRangeException("Advancer out of search range");
               
            }
            
         } else {
            
            geoSpatialCounters.registerZOrderIndexHit();
            return;
         }
      }

   }
   
   /** 
    * Returns the BIGMIN, i.e. the next relevant value in the search range.
    * The value is returned as unsigned, which needs to be converted into
    * two's complement prior to appending as a key 
    * (see {@link GeoSpatialLiteralExtension} for details).
    * 
    * This method implements the BIGMIN decision table as provided in
    * http://www.vision-tools.com/h-tropf/multidimensionalrangequery.pdf,
    * see page 76.
    * 
    * @param iv the IV of the dividing record
    * @return
    */
   private byte[] calculateBigMin(final byte[] dividingRecord) {
            
      if (dividingRecord.length!=searchMinZOrder.length ||
          dividingRecord.length!=searchMaxZOrder.length) {
         
         // this should never happen, assuming correct configuration
         throw new RuntimeException("Key dimenisions differs");
      } 
      
      final int numBytes = dividingRecord.length;
      final int numDimensions = litExt.getNumDimensions();

      System.arraycopy(searchMinZOrder, 0, min, 0, zOrderArrayLength);
      System.arraycopy(searchMaxZOrder, 0, max, 0, zOrderArrayLength);
      java.util.Arrays.fill(bigmin,(byte)0); // reset bigmin
      
      boolean finished = false;
      for (int i = 0; i < numBytes * Byte.SIZE && !finished; i++) { 

         final boolean divRecordBitSet = BytesUtil.getBit(dividingRecord, i);
         final boolean minBitSet = BytesUtil.getBit(min, i);
         final boolean maxBitSet = BytesUtil.getBit(max, i);

         if (!divRecordBitSet) {
            
            if (!minBitSet) {
               
               if (!maxBitSet) {
                  
                  // case 0 - 0 - 0: continue (nothing to do)
                  
               } else {
                  
                  // case 0 - 0 - 1
                  System.arraycopy(min, 0, bigmin, 0, zOrderArrayLength);
                  load(true /* setFirst */, i, bigmin, numDimensions);
                  load(false, i, max, numDimensions);
                  
               }
               
            } else {
               
               if (!maxBitSet) {
                  
                  // case 0 - 1 - 0
                  throw new RuntimeException("MIN must be <= MAX.");
                  
               } else {
                  
                  // case 0 - 1 - 1
                  System.arraycopy(min, 0, bigmin, 0, zOrderArrayLength);
                  finished = true; 
                  
               }               
            }
         } else {
            
            if (!minBitSet) {
               
               if (!maxBitSet) {
                  
                  // case 1 - 0 - 0
                  finished = true;
                  
               } else {
                  
                  // case 1 - 0 - 1
                  load(true, i, min, numDimensions);
                  
               }
               
            } else {
               
               if (!maxBitSet) {
                  
                  // case 1 - 1 - 0
                  throw new RuntimeException("MIN must be <= MAX.");                  
                  
               } else {
                  
                  // case 1 - 1 - 1: continue (nothing to do)
                  
               }               
            }
            
         }
      }
      
      return bigmin;
   }
   
   /**
    * Implements the load function from p.75 in
    * http://www.vision-tools.com/h-tropf/multidimensionalrangequery.pdf:
    * 
    * If firstBitSet, then load 10000... is executed as defined in the paper
    * (setting the bit identified through position to 1 and all following bits
    * in the respective dimension to 0); otherwise load 01111... is executed
    * (which sets the bit identified through position to 0 and all following
    * bits of the dimension to 1). The method has no return value, but instead
    * as a side effect the passed array arr will be modified.
    */
   private void load(
      final boolean setFirst, final int position,
      final byte[] arr, final int numDimensions) {
      
      // set the trailing bit
      if (setFirst)
         arr[(int)(position / 8)] |= 1<<7-(position%8);
      else
         arr[(int)(position / 8)] &= ~(1<<7-(position%8));

      // set the remaining bits (inverted)
      for (int i=position+numDimensions; i<arr.length * Byte.SIZE; i+=numDimensions) {
         
         final int posInByte = i%8;

         // for performance reasons, we aggregate all changes to the current byte
         int posInByteInv = 7 - posInByte;
         int mask = 1 << posInByteInv;
         for (posInByteInv-=numDimensions; posInByteInv>=0; posInByteInv-=numDimensions) {
            mask |= 1 << posInByteInv;
            i+=numDimensions; // corresponds to one time skip of outer loop
         }
         
         if (setFirst)
            arr[(int) (i / 8)] &= ~mask;
         else
            arr[(int) (i / 8)] |= mask;
         
      }

   }
   
}
