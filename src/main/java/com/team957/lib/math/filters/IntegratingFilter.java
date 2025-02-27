/**
Copyright 2022-2023 FRC Teams 957 and 997

This program is free software: 
you can redistribute it and/or modify it under the terms of the 
GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. 
If not, see <https://www.gnu.org/licenses/>.
*/
package com.team957.lib.math.filters;

import com.team957.lib.util.SizedStack;

/**
 * Filter which returns an time-weighted sum (integral) of a series of values.
 *
 * <p>Approximated with finite timesteps using a trapezoidal Riemann sum.
 */
public class IntegratingFilter extends Filter {
    private final SizedStack<Double> stack;

    private double currentOutput = 0;

    private double previousInputValue = 0;

    private final boolean infinite;

    /**
     * Constructs an IntegratingFilter.
     *
     * @param window Number of values to look back in calculating the integral. If zero or negative,
     *     will instead be an infinite window.
     */
    public IntegratingFilter(int window) {
        stack = new SizedStack<>(window);

        infinite = (window < 1);
    }

    @Override
    /**
     * Adds the value to the filter and calculates the integral, using a provided dt.
     *
     * @param value Value to add to the window of the filter.
     * @param dt Timestep in seconds.
     * @return Value of the (approximated) integral.
     */
    public double calculate(double value, double dt) {
        if (infinite) {
            currentOutput = currentOutput + (dt * 0.5 * (value + previousInputValue));

            previousInputValue = value;

            return currentOutput;
        }

        stack.push(dt * 0.5 * (value + previousInputValue));

        double sum = 0;

        for (double entry : stack) sum += entry;

        currentOutput = sum;

        previousInputValue = value;

        return sum;
    }

    @Override
    /** {@inheritDoc} */
    public void reset() {
        stack.clear();
    }

    @Override
    /** {@inheritDoc} */
    public double getCurrentOutput() {
        return currentOutput;
    }
}
