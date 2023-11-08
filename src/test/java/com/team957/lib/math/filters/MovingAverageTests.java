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

import com.team957.lib.math.filters.MovingAverageFilter.MEAN_IMPLEMENTATION;
import org.junit.Test;

public class MovingAverageTests {
    private static final double epsilon = 0.0001;

    @Test
    public void movingAverageFilterWorksInfiniteWindow() {
        MovingAverageFilter filter = new MovingAverageFilter(0, MEAN_IMPLEMENTATION.ARITHMETIC);

        assertEquals(1, filter.calculate(1), epsilon);
        assertEquals(1, filter.calculate(1), epsilon);
        assertEquals(2, filter.calculate(4), epsilon);
        assertEquals(0, filter.calculate(-6), epsilon);
    }

    @Test
    public void movingAverageFilterWorksFiniteWindow() {
        MovingAverageFilter filter = new MovingAverageFilter(3, MEAN_IMPLEMENTATION.ARITHMETIC);

        assertEquals(3, filter.calculate(3), epsilon);
        assertEquals(1.5, filter.calculate(0), epsilon);
        assertEquals(0, filter.calculate(-3), epsilon);

        assertEquals(-3, filter.calculate(-6), epsilon);
        assertEquals(-3, filter.calculate(0), epsilon);
        assertEquals(-1, filter.calculate(3), epsilon);
    }
}
