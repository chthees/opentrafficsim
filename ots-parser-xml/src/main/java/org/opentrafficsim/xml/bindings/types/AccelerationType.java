package org.opentrafficsim.xml.bindings.types;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Acceleration;

/**
 * Expression type with Acceleration value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AccelerationType extends ExpressionType<Acceleration>
{

    /** Function to convert output from expression to the right type. */
    private static final Function<Object, Acceleration> TO_TYPE = (o) -> Acceleration.instantiateSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value Acceleration; value, may be {@code null}.
     */
    public AccelerationType(final Acceleration value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public AccelerationType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
