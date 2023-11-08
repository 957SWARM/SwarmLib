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

/**
 * Filter which performs a nonop on a signal (returns it unchanged).
 *
 * <p>This can be useful in composing a controller out of filters, and when multiplied by a gain is
 * identical to a PID's P term.
 */
public class NullFilter extends Filter {
    private double currentValue = 0;

    @Override
    /** {@inheritDoc} */
    public double calculate(double value) {
        currentValue = value;
        return value;
    }

    @Override
    /** {@inheritDoc} */
    public double calculate(double value, double dt) {
        return calculate(value);
    }

    @Override
    /** {@inheritDoc} */
    public void reset() {
        currentValue = 0;
    }

    @Override
    /** {@inheritDoc} */
    public double getCurrentOutput() {
        return currentValue;
    }
}
