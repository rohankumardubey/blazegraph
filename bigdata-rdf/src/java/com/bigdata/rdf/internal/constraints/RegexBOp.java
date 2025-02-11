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
package com.bigdata.rdf.internal.constraints;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

import com.bigdata.bop.BOp;
import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.IConstant;
import com.bigdata.bop.IValueExpression;
import com.bigdata.bop.NV;
import com.bigdata.rdf.error.SparqlTypeErrorException;
import com.bigdata.rdf.internal.IV;

/**
 * SPARQL REGEX operator.
 */
public class RegexBOp extends XSDBooleanIVValueExpression 
        implements INeedsMaterialization {

    /**
     * 
     */
    private static final long serialVersionUID = 1357420268214930143L;
    
    private static final transient Logger log = Logger.getLogger(RegexBOp.class);

    public interface Annotations extends XSDBooleanIVValueExpression.Annotations {
        
        /**
         * The cached regex pattern.
         */
        public String PATTERN = RegexBOp.class.getName()
                + ".pattern";
        
    }

    private static Map<String,Object> anns(
            final IValueExpression<? extends IV> pattern,
            final IValueExpression<? extends IV> flags) {
        
        try {
            
            if (pattern instanceof IConstant && 
                    (flags == null || flags instanceof IConstant)) {
                
                final IV parg = ((IConstant<IV>) pattern).get();
                
                final IV farg = flags != null ?
                        ((IConstant<IV>) flags).get() : null;
                        
                if (parg.hasValue() && (farg == null || farg.hasValue())) {
                
                    final Value pargVal = parg.getValue();
                    
                    final Value fargVal = farg != null ? farg.getValue() : null;
                    
                    return NV.asMap(
                            new NV(Annotations.PATTERN, 
                                    getPattern(pargVal, fargVal)));
                    
                }
                
            }
            
        } catch (Exception ex) {
        
            if (log.isInfoEnabled()) {
                log.info("could not create pattern for: " + pattern + ", " + flags);
            }
            
        }
            
        return BOp.NOANNS;
        
    }
    
    /**
     * Construct a regex bop without flags.
     */
    @SuppressWarnings("rawtypes")
    public RegexBOp(
            final IValueExpression<? extends IV> var, 
            final IValueExpression<? extends IV> pattern) {
        
        this(new BOp[] { var, pattern }, anns(pattern, null));

    }
    
    /**
     * Construct a regex bop with flags.
     */
    @SuppressWarnings("rawtypes")
    public RegexBOp(
            final IValueExpression<? extends IV> var, 
            final IValueExpression<? extends IV> pattern,
            final IValueExpression<? extends IV> flags) {
        
        this(new BOp[] { var, pattern, flags }, anns(pattern, flags)); 
        
    }
    
    /**
     * Required shallow copy constructor.
     */
    public RegexBOp(final BOp[] args, final Map<String, Object> anns) {

        super(args, anns);
        
        if (args.length < 2 || args[0] == null || args[1] == null)
            throw new IllegalArgumentException();

    }

    /**
     * Constructor required for {@link com.bigdata.bop.BOpUtility#deepCopy(FilterNode)}.
     */
    public RegexBOp(final RegexBOp op) {
        super(op);
    }

    @Override
    public Requirement getRequirement() {

        return INeedsMaterialization.Requirement.SOMETIMES;

    }

    @Override
    public boolean accept(final IBindingSet bs) {

        final Value var = asValue(getAndCheckBound(0, bs));

        @SuppressWarnings("rawtypes")
        final IV pattern = getAndCheckBound(1, bs);

        @SuppressWarnings("rawtypes")
        final IV flags = arity() > 2 ? get(2).get(bs) : null;

        if (log.isDebugEnabled()) {
            log.debug("regex var: " + var);
            log.debug("regex pattern: " + pattern);
            log.debug("regex flags: " + flags);
        }

        return accept(var, pattern.getValue(), flags != null ? flags.getValue()
                : null);

    }
    
    /**
     * Lifted directly from Sesame's EvaluationStrategyImpl.
     * 
     * FIXME The Pattern should be cached if the pattern argument and flags are
     * constants.
     * 
     * @see <a href="http://sourceforge.net/apps/trac/bigdata/ticket/516">
     *      REGEXBOp should cache the Pattern when it is a constant </a>
     */
    private boolean accept(final Value arg, final Value parg, final Value farg) {

        if (log.isDebugEnabled()) {
            log.debug("regex var: " + arg);
            log.debug("regex pattern: " + parg);
            log.debug("regex flags: " + farg);
        }

        if (QueryEvaluationUtil.isSimpleLiteral(arg)) {

            final String text = ((Literal) arg).getLabel();

            try {

                // first check for cached pattern
                Pattern pattern = (Pattern) getProperty(Annotations.PATTERN);

                if (pattern == null) {

                    // resolve the pattern. NB: NOT cached.
                    pattern = getPattern(parg, farg);
                    
                }

                if (Thread.interrupted()) {

                    /*
                     * Eagerly notice if the operator is interrupted.
                     * 
                     * Note: Regex can be a high latency operation for a large
                     * RDF Literal. Therefore we want to check for an interrupt
                     * before each regex test. The Pattern code itself will not
                     * notice an interrupt....
                     */
                    throw new RuntimeException(new InterruptedException());
                
                }

                final boolean result = pattern.matcher(text).find();

                return result;

            } catch (IllegalArgumentException ex) {

                throw new SparqlTypeErrorException();

            }

        } else {

            throw new SparqlTypeErrorException();

        }

    }
    
    private static Pattern getPattern(final Value parg, final Value farg)
            throws IllegalArgumentException {
        
        if (log.isDebugEnabled()) {
            log.debug("regex pattern: " + parg);
            log.debug("regex flags: " + farg);
        }
        
        if (QueryEvaluationUtil.isSimpleLiteral(parg)
                && (farg == null || QueryEvaluationUtil.isSimpleLiteral(farg))) {

            final String ptn = ((Literal) parg).getLabel();
            String flags = "";
            if (farg != null) {
                flags = ((Literal)farg).getLabel();
            }
            int f = 0;
            for (char c : flags.toCharArray()) {
                switch (c) {
                    case 's':
                        f |= Pattern.DOTALL;
                        break;
                    case 'm':
                        f |= Pattern.MULTILINE;
                        break;
                    case 'i': {
                    /*
                     * The SPARQL REGEX operator is based on the XQuery REGEX
                     * operator. That operator should be Unicode clean by
                     * default. Therefore, when case-folding is specified, we
                     * also need to include the UNICODE_CASE option.
                     * 
                     * @see <a
                     * href="https://sourceforge.net/apps/trac/bigdata/ticket/655"
                     * > SPARQL REGEX operator does not perform case-folding
                     * correctly for Unicode data </a>
                     */
                        f |= Pattern.CASE_INSENSITIVE;
                        f |= Pattern.UNICODE_CASE;
                        break;
                    }
                    case 'x':
                        f |= Pattern.COMMENTS;
                        break;
                    case 'd':
                        f |= Pattern.UNIX_LINES;
                        break;
                    case 'u': // Implicit with 'i' flag.
//                      f |= Pattern.UNICODE_CASE;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
            final Pattern pattern = Pattern.compile(ptn, f);
            return pattern;
        }
        
        throw new IllegalArgumentException();
        
    }
    
}
