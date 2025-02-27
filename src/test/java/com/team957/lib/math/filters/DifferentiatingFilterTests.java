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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Tests for the DifferentiatingFilter. */
public class DifferentiatingFilterTests {
    private static final double epsilon = 0.0001;

    @Test
    public void differentiatingFilterReturnsZeroForDtOfZero() {
        assertEquals(0, new DifferentiatingFilter().calculate(1, 0), epsilon);
    }

    @Test
    public void differentiatingFilterDerivOfFirstInputIsCorrect() {
        assertEquals(5, new DifferentiatingFilter().calculate(5, 1), epsilon);

        assertEquals(200, new DifferentiatingFilter().calculate(4, 0.02), epsilon);
    }

    @Test
    public void differentiatingFilterDerivIsCorrectForFixedDt() {
        DifferentiatingFilter filter = new DifferentiatingFilter();

        assertEquals(300, filter.calculate(6, 0.02), epsilon);
        assertEquals(-200, filter.calculate(2, 0.02), epsilon);
        assertEquals(0, filter.calculate(2, 0.02), epsilon);
        assertEquals(400, filter.calculate(10, 0.02), epsilon);
    }

    @Test
    public void differentiatingFilterDerivIsCorrectForChangingDt() {
        DifferentiatingFilter filter = new DifferentiatingFilter();

        assertEquals(1, filter.calculate(2, 2), epsilon);
        assertEquals(1, filter.calculate(3, 1), epsilon);
        assertEquals(-4, filter.calculate(1, 0.5), epsilon);
    }
}
