package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.Relative;
import org.opentrafficsim.core.value.SparseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.ValueUtil;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctions;
import org.opentrafficsim.core.value.vfloat.FloatMathFunctionsImpl;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix1D;
import cern.jet.math.tfloat.FloatFunctions;

/**
 * MutableFloatVector.
 * <p>
 * This file was generated by the OpenTrafficSim value classes generator, 26 jun, 2015
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version26 jun, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit; the unit of this MutableFloatVector
 */
public abstract class MutableFloatVector<U extends Unit<U>> extends FloatVector<U> implements
        WriteFloatVectorFunctions<U>, FloatMathFunctions<MutableFloatVector<U>>
{
    /**  */
    private static final long serialVersionUID = 20150626L;

    /**
     * Construct a new MutableFloatVector.
     * @param unit U; the unit of the new MutableFloatVector
     */
    protected MutableFloatVector(final U unit)
    {
        super(unit);
        // System.out.println("Created MutableFloatVector");
    }

    /** If set, any modification of the data must be preceded by replacing the data with a local copy. */
    private boolean copyOnWrite = false;

    /**
     * Retrieve the value of the copyOnWrite flag.
     * @return boolean
     */
    private boolean isCopyOnWrite()
    {
        return this.copyOnWrite;
    }

    /**
     * Change the copyOnWrite flag.
     * @param copyOnWrite boolean; the new value for the copyOnWrite flag
     */
    final void setCopyOnWrite(final boolean copyOnWrite)
    {
        this.copyOnWrite = copyOnWrite;
    }

    /** {@inheritDoc} */
    @Override
    public final void normalize() throws ValueException
    {
        float sum = zSum();
        if (0 == sum)
        {
            throw new ValueException("zSum is 0; cannot normalize");
        }
        checkCopyOnWrite();
        for (int i = 0; i < size(); i++)
        {
            safeSet(i, safeGet(i) / sum);
        }
    }

    /**
     * @param <U> Unit
     */
    public abstract static class Abs<U extends Unit<U>> extends MutableFloatVector<U> implements Absolute
    {
        /**  */
        private static final long serialVersionUID = 20150626L;

        /**
         * Construct a new Absolute MutableFloatVector.
         * @param unit U; the unit of the new Absolute MutableFloatVector
         */
        protected Abs(final U unit)
        {
            super(unit);
            // System.out.println("Created Abs");
        }

        /**
         * @param <U> Unit
         */
        public static class Dense<U extends Unit<U>> extends Abs<U> implements DenseData
        {
            /**  */
            private static final long serialVersionUID = 20150626L;

            /**
             * Construct a new Absolute Dense MutableFloatVector.
             * @param values float[]; the initial values of the entries in the new Absolute Dense MutableFloatVector
             * @param unit U; the unit of the new Absolute Dense MutableFloatVector
             * @throws ValueException when values is null
             */
            public Dense(final float[] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Absolute Dense MutableFloatVector.
             * @param values FloatScalar.Abs&lt;U&gt;[]; the initial values of the entries in the new Absolute Dense
             *            MutableFloatVector
             * @throws ValueException when values has zero entries
             */
            public Dense(final FloatScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values FloatMatrix1D; the initial values of the entries in the new Absolute Dense
             *            MutableFloatVector
             * @param unit U; the unit of the new Absolute Dense MutableFloatVector
             */
            protected Dense(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final FloatVector.Abs.Dense<U> immutable()
            {
                setCopyOnWrite(true);
                return new FloatVector.Abs.Dense<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Abs.Dense<U> mutable()
            {
                setCopyOnWrite(true);
                final MutableFloatVector.Abs.Dense<U> result =
                        new MutableFloatVector.Abs.Dense<U>(getVectorSI(), getUnit());
                result.setCopyOnWrite(true);
                return result;
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix1D createMatrix1D(final int size)
            {
                return new DenseFloatMatrix1D(size);
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Abs.Dense<U> copy()
            {
                return mutable();
            }

        }

        /**
         * @param <U> Unit
         */
        public static class Sparse<U extends Unit<U>> extends Abs<U> implements SparseData
        {
            /**  */
            private static final long serialVersionUID = 20150626L;

            /**
             * Construct a new Absolute Sparse MutableFloatVector.
             * @param values float[]; the initial values of the entries in the new Absolute Sparse MutableFloatVector
             * @param unit U; the unit of the new Absolute Sparse MutableFloatVector
             * @throws ValueException when values is null
             */
            public Sparse(final float[] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Absolute Sparse MutableFloatVector.
             * @param values FloatScalar.Abs&lt;U&gt;[]; the initial values of the entries in the new Absolute Sparse
             *            MutableFloatVector
             * @throws ValueException when values has zero entries
             */
            public Sparse(final FloatScalar.Abs<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values FloatMatrix1D; the initial values of the entries in the new Absolute Sparse
             *            MutableFloatVector
             * @param unit U; the unit of the new Absolute Sparse MutableFloatVector
             */
            protected Sparse(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final FloatVector.Abs.Sparse<U> immutable()
            {
                setCopyOnWrite(true);
                return new FloatVector.Abs.Sparse<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Abs.Sparse<U> mutable()
            {
                setCopyOnWrite(true);
                final MutableFloatVector.Abs.Sparse<U> result =
                        new MutableFloatVector.Abs.Sparse<U>(getVectorSI(), getUnit());
                result.setCopyOnWrite(true);
                return result;
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix1D createMatrix1D(final int size)
            {
                return new SparseFloatMatrix1D(size);
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Abs.Sparse<U> copy()
            {
                return mutable();
            }

        }

        /** {@inheritDoc} */
        @Override
        public final FloatScalar.Abs<U> get(final int index) throws ValueException
        {
            return new FloatScalar.Abs<U>(getInUnit(index, getUnit()), getUnit());
        }

        /**
         * Increment the value by the supplied value and return the result.
         * @param increment FloatVector.Rel&lt;U&gt;; amount by which the value is incremented
         * @return MutableFloatVector.Abs&lt;U&gt;
         * @throws ValueException when the size of increment is not identical to the size of this
         */
        public final MutableFloatVector.Abs<U> incrementBy(final FloatVector.Rel<U> increment) throws ValueException
        {
            return (MutableFloatVector.Abs<U>) incrementByImpl(increment);
        }

        /**
         * Decrement the value by the supplied value and return the result.
         * @param decrement FloatVector.Rel&lt;U&gt;; amount by which the value is decremented
         * @return MutableFloatVector.Abs&lt;U&gt;
         * @throws ValueException when the size of increment is not identical to the size of this
         */
        public final MutableFloatVector.Abs<U> decrementBy(final FloatVector.Rel<U> decrement) throws ValueException
        {
            return (MutableFloatVector.Abs<U>) decrementByImpl(decrement);
        }

        /**********************************************************************************/
        /********************************** MATH METHODS **********************************/
        /**********************************************************************************/

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> abs()
        {
            assign(FloatFunctions.abs);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> acos()
        {
            assign(FloatFunctions.acos);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> asin()
        {
            assign(FloatFunctions.asin);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> atan()
        {
            assign(FloatFunctions.atan);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> cbrt()
        {
            assign(FloatMathFunctionsImpl.cbrt);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> ceil()
        {
            assign(FloatFunctions.ceil);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> cos()
        {
            assign(FloatFunctions.cos);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> cosh()
        {
            assign(FloatMathFunctionsImpl.cosh);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> exp()
        {
            assign(FloatFunctions.exp);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> expm1()
        {
            assign(FloatMathFunctionsImpl.expm1);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> floor()
        {
            assign(FloatFunctions.floor);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> log()
        {
            assign(FloatFunctions.log);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> log10()
        {
            assign(FloatMathFunctionsImpl.log10);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> log1p()
        {
            assign(FloatMathFunctionsImpl.log1p);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> pow(final double x)
        {
            assign(FloatFunctions.pow((float) x));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> rint()
        {
            assign(FloatFunctions.rint);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> round()
        {
            assign(FloatMathFunctionsImpl.round);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> signum()
        {
            assign(FloatMathFunctionsImpl.signum);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> sin()
        {
            assign(FloatFunctions.sin);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> sinh()
        {
            assign(FloatMathFunctionsImpl.sinh);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> sqrt()
        {
            assign(FloatFunctions.sqrt);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> tan()
        {
            assign(FloatFunctions.tan);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> tanh()
        {
            assign(FloatMathFunctionsImpl.tanh);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> toDegrees()
        {
            assign(FloatMathFunctionsImpl.toDegrees);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> toRadians()
        {
            assign(FloatMathFunctionsImpl.toRadians);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> inv()
        {
            assign(FloatFunctions.inv);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> multiplyBy(final float constant)
        {
            assign(FloatFunctions.mult(constant));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Abs<U> divideBy(final float constant)
        {
            assign(FloatFunctions.div(constant));
            return this;
        }

    }

    /**
     * @param <U> Unit
     */
    public abstract static class Rel<U extends Unit<U>> extends MutableFloatVector<U> implements Relative
    {
        /**  */
        private static final long serialVersionUID = 20150626L;

        /**
         * Construct a new Relative MutableFloatVector.
         * @param unit U; the unit of the new Relative MutableFloatVector
         */
        protected Rel(final U unit)
        {
            super(unit);
            // System.out.println("Created Rel");
        }

        /**
         * @param <U> Unit
         */
        public static class Dense<U extends Unit<U>> extends Rel<U> implements DenseData
        {
            /**  */
            private static final long serialVersionUID = 20150626L;

            /**
             * Construct a new Relative Dense MutableFloatVector.
             * @param values float[]; the initial values of the entries in the new Relative Dense MutableFloatVector
             * @param unit U; the unit of the new Relative Dense MutableFloatVector
             * @throws ValueException when values is null
             */
            public Dense(final float[] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * Construct a new Relative Dense MutableFloatVector.
             * @param values FloatScalar.Rel&lt;U&gt;[]; the initial values of the entries in the new Relative Dense
             *            MutableFloatVector
             * @throws ValueException when values has zero entries
             */
            public Dense(final FloatScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Dense");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values FloatMatrix1D; the initial values of the entries in the new Relative Dense
             *            MutableFloatVector
             * @param unit U; the unit of the new Relative Dense MutableFloatVector
             */
            protected Dense(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Dense");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final FloatVector.Rel.Dense<U> immutable()
            {
                setCopyOnWrite(true);
                return new FloatVector.Rel.Dense<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Rel.Dense<U> mutable()
            {
                setCopyOnWrite(true);
                final MutableFloatVector.Rel.Dense<U> result =
                        new MutableFloatVector.Rel.Dense<U>(getVectorSI(), getUnit());
                result.setCopyOnWrite(true);
                return result;
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix1D createMatrix1D(final int size)
            {
                return new DenseFloatMatrix1D(size);
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Rel.Dense<U> copy()
            {
                return mutable();
            }

        }

        /**
         * @param <U> Unit
         */
        public static class Sparse<U extends Unit<U>> extends Rel<U> implements SparseData
        {
            /**  */
            private static final long serialVersionUID = 20150626L;

            /**
             * Construct a new Relative Sparse MutableFloatVector.
             * @param values float[]; the initial values of the entries in the new Relative Sparse MutableFloatVector
             * @param unit U; the unit of the new Relative Sparse MutableFloatVector
             * @throws ValueException when values is null
             */
            public Sparse(final float[] values, final U unit) throws ValueException
            {
                super(unit);
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * Construct a new Relative Sparse MutableFloatVector.
             * @param values FloatScalar.Rel&lt;U&gt;[]; the initial values of the entries in the new Relative Sparse
             *            MutableFloatVector
             * @throws ValueException when values has zero entries
             */
            public Sparse(final FloatScalar.Rel<U>[] values) throws ValueException
            {
                super(checkNonEmpty(values)[0].getUnit());
                // System.out.println("Created Sparse");
                initialize(values);
            }

            /**
             * For package internal use only.
             * @param values FloatMatrix1D; the initial values of the entries in the new Relative Sparse
             *            MutableFloatVector
             * @param unit U; the unit of the new Relative Sparse MutableFloatVector
             */
            protected Sparse(final FloatMatrix1D values, final U unit)
            {
                super(unit);
                // System.out.println("Created Sparse");
                setCopyOnWrite(true);
                initialize(values); // shallow copy
            }

            /** {@inheritDoc} */
            @Override
            public final FloatVector.Rel.Sparse<U> immutable()
            {
                setCopyOnWrite(true);
                return new FloatVector.Rel.Sparse<U>(getVectorSI(), getUnit());
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Rel.Sparse<U> mutable()
            {
                setCopyOnWrite(true);
                final MutableFloatVector.Rel.Sparse<U> result =
                        new MutableFloatVector.Rel.Sparse<U>(getVectorSI(), getUnit());
                result.setCopyOnWrite(true);
                return result;
            }

            /** {@inheritDoc} */
            @Override
            protected final FloatMatrix1D createMatrix1D(final int size)
            {
                return new SparseFloatMatrix1D(size);
            }

            /** {@inheritDoc} */
            @Override
            public final MutableFloatVector.Rel.Sparse<U> copy()
            {
                return mutable();
            }

        }

        /** {@inheritDoc} */
        @Override
        public final FloatScalar.Rel<U> get(final int index) throws ValueException
        {
            return new FloatScalar.Rel<U>(getInUnit(index, getUnit()), getUnit());
        }

        /**
         * Increment the value by the supplied value and return the result.
         * @param increment FloatVector.Rel&lt;U&gt;; amount by which the value is incremented
         * @return MutableFloatVector.Rel&lt;U&gt;
         * @throws ValueException when the size of increment is not identical to the size of this
         */
        public final MutableFloatVector.Rel<U> incrementBy(final FloatVector.Rel<U> increment) throws ValueException
        {
            return (MutableFloatVector.Rel<U>) incrementByImpl(increment);
        }

        /**
         * Decrement the value by the supplied value and return the result.
         * @param decrement FloatVector.Rel&lt;U&gt;; amount by which the value is decremented
         * @return MutableFloatVector.Rel&lt;U&gt;
         * @throws ValueException when the size of increment is not identical to the size of this
         */
        public final MutableFloatVector.Rel<U> decrementBy(final FloatVector.Rel<U> decrement) throws ValueException
        {
            return (MutableFloatVector.Rel<U>) decrementByImpl(decrement);
        }

        /**********************************************************************************/
        /********************************** MATH METHODS **********************************/
        /**********************************************************************************/

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> abs()
        {
            assign(FloatFunctions.abs);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> acos()
        {
            assign(FloatFunctions.acos);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> asin()
        {
            assign(FloatFunctions.asin);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> atan()
        {
            assign(FloatFunctions.atan);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> cbrt()
        {
            assign(FloatMathFunctionsImpl.cbrt);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> ceil()
        {
            assign(FloatFunctions.ceil);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> cos()
        {
            assign(FloatFunctions.cos);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> cosh()
        {
            assign(FloatMathFunctionsImpl.cosh);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> exp()
        {
            assign(FloatFunctions.exp);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> expm1()
        {
            assign(FloatMathFunctionsImpl.expm1);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> floor()
        {
            assign(FloatFunctions.floor);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> log()
        {
            assign(FloatFunctions.log);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> log10()
        {
            assign(FloatMathFunctionsImpl.log10);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> log1p()
        {
            assign(FloatMathFunctionsImpl.log1p);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> pow(final double x)
        {
            assign(FloatFunctions.pow((float) x));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> rint()
        {
            assign(FloatFunctions.rint);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> round()
        {
            assign(FloatMathFunctionsImpl.round);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> signum()
        {
            assign(FloatMathFunctionsImpl.signum);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> sin()
        {
            assign(FloatFunctions.sin);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> sinh()
        {
            assign(FloatMathFunctionsImpl.sinh);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> sqrt()
        {
            assign(FloatFunctions.sqrt);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> tan()
        {
            assign(FloatFunctions.tan);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> tanh()
        {
            assign(FloatMathFunctionsImpl.tanh);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> toDegrees()
        {
            assign(FloatMathFunctionsImpl.toDegrees);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> toRadians()
        {
            assign(FloatMathFunctionsImpl.toRadians);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> inv()
        {
            assign(FloatFunctions.inv);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> multiplyBy(final float constant)
        {
            assign(FloatFunctions.mult(constant));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public final MutableFloatVector.Rel<U> divideBy(final float constant)
        {
            assign(FloatFunctions.div(constant));
            return this;
        }

    }

    /**
     * Make (immutable) FloatVector equivalent for any type of MutableFloatVector.
     * @return FloatVector&lt;U&gt;; immutable version of this FloatVector
     */
    public abstract FloatVector<U> immutable();

    /**
     * Check the copyOnWrite flag and, if it is set, make a deep copy of the data and clear the flag.
     */
    protected final void checkCopyOnWrite()
    {
        if (isCopyOnWrite())
        {
            // System.out.println("copyOnWrite is set: Copying data");
            deepCopyData();
            setCopyOnWrite(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void setSI(final int index, final float valueSI) throws ValueException
    {
        checkIndex(index);
        checkCopyOnWrite();
        safeSet(index, valueSI);
    }

    /** {@inheritDoc} */
    @Override
    public final void set(final int index, final FloatScalar<U> value) throws ValueException
    {
        setSI(index, value.getSI());
    }

    /** {@inheritDoc} */
    @Override
    public final void setInUnit(final int index, final float value, final U valueUnit) throws ValueException
    {
        setSI(index, (float) ValueUtil.expressAsSIUnit(value, valueUnit));
    }

    /**
     * Execute a function on a cell by cell basis.
     * @param f cern.colt.function.tfloat.FloatFunction; the function to apply
     */
    public final void assign(final cern.colt.function.tfloat.FloatFunction f)
    {
        checkCopyOnWrite();
        getVectorSI().assign(f);
    }

    /**********************************************************************************/
    /******************************* NON-STATIC METHODS *******************************/
    /**********************************************************************************/

    /**
     * Increment the values in this MutableFloatVector by the corresponding values in a FloatVector.
     * @param increment FloatVector&lt;U&gt;; the values by which to increment the corresponding values in this
     *            MutableFloatVector
     * @return MutableFloatVector&lt;U&gt;; this modified MutableFloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    private MutableFloatVector<U> incrementValueByValue(final FloatVector<U> increment) throws ValueException
    {
        checkSizeAndCopyOnWrite(increment);
        for (int index = size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) + increment.safeGet(index));
        }
        return this;
    }

    /**
     * Decrement the values in this MutableFloatVector by the corresponding values in a FloatVector.
     * @param decrement FloatVector&lt;U&gt;; the values by which to decrement the corresponding values in this
     *            MutableFloatVector
     * @return MutableFloatVector&lt;U&gt;; this modified MutableFloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    private MutableFloatVector<U> decrementValueByValue(final FloatVector<U> decrement) throws ValueException
    {
        checkSizeAndCopyOnWrite(decrement);
        for (int index = size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) - decrement.safeGet(index));
        }
        return this;
    }

    /**
     * Increment the values in this MutableFloatVector by the corresponding values in a Relative FloatVector. <br>
     * Only Relative values are allowed; adding an Absolute value to an Absolute value is not allowed. Adding an
     * Absolute value to an existing Relative value would require the result to become Absolute, which is a type change
     * that is impossible. For that operation use a static method.
     * @param rel FloatVector.Rel&lt;U&gt;; the Relative FloatVector
     * @return MutableFloatVector&lt;U&gt;; this modified MutableFloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    protected final MutableFloatVector<U> incrementByImpl(final FloatVector.Rel<U> rel) throws ValueException
    {
        return incrementValueByValue(rel);
    }

    /**
     * Decrement the corresponding values of this Relative FloatVector from the values of this MutableFloatVector. <br>
     * Only Relative values are allowed; subtracting an Absolute value from a Relative value is not allowed. Subtracting
     * an Absolute value from an existing Absolute value would require the result to become Relative, which is a type
     * change that is impossible. For that operation use a static method.
     * @param rel FloatVector.Rel&lt;U&gt;; the Relative FloatVector
     * @return MutableFloatVector&lt;U&gt;; this modified MutableFloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    protected final MutableFloatVector<U> decrementByImpl(final FloatVector.Rel<U> rel) throws ValueException
    {
        return decrementValueByValue(rel);
    }

    // FIXME It makes no sense to subtract an Absolute from a Relative
    /**
     * Decrement the values in this Relative MutableFloatVector by the corresponding values in an Absolute FloatVector.
     * @param abs FloatVector.Abs&lt;U&gt;; the Absolute FloatVector
     * @return MutableFloatVector.Rel&lt;U&gt;; this modified Relative MutableFloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    protected final MutableFloatVector.Rel<U> decrementBy(final FloatVector.Abs<U> abs) throws ValueException
    {
        return (MutableFloatVector.Rel<U>) decrementValueByValue(abs);
    }

    /**
     * Scale the values in this MutableFloatVector by the corresponding values in a FloatVector.
     * @param factor FloatVector&lt;?&gt;; contains the values by which to scale the corresponding values in this
     *            MutableFloatVector
     * @throws ValueException when the vectors do not have the same size
     */
    protected final void scaleValueByValue(final FloatVector<?> factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) * factor.safeGet(index));
        }
    }

    /**
     * Scale the values in this MutableFloatVector by the corresponding values in a float array.
     * @param factor float[]; contains the values by which to scale the corresponding values in this MutableFloatVector
     * @return MutableFloatVector&lt;U&gt;; this modified MutableFloatVector
     * @throws ValueException when the vector and the array do not have the same size
     */
    protected final MutableFloatVector<U> scaleValueByValue(final float[] factor) throws ValueException
    {
        checkSizeAndCopyOnWrite(factor);
        for (int index = size(); --index >= 0;)
        {
            safeSet(index, safeGet(index) * factor[index]);
        }
        return this;
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other FloatVector&lt;?&gt;; partner for the size check
     * @throws ValueException when the vectors do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final FloatVector<?> other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

    /**
     * Check sizes and copy the data if the copyOnWrite flag is set.
     * @param other float[]; partner for the size check
     * @throws ValueException when the vectors do not have the same size
     */
    private void checkSizeAndCopyOnWrite(final float[] other) throws ValueException
    {
        checkSize(other);
        checkCopyOnWrite();
    }

}
