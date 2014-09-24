package org.opentrafficsim.core.value.vdouble;

import org.opentrafficsim.core.value.MathFunctions;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface DoubleMathFunctions extends MathFunctions
{
    /**
     * Multiply the value(s) with a constant.
     * @param constant the multiplier
     */
    void multiply(double constant);

    /**
     * Divide the value(s) by a constant.
     * @param constant the divisor
     */
    void divide(double constant);

}
