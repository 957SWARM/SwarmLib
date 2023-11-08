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

public class RateLimiterTests {
    private static final double epsilon = 0.0001;

    @Test
    public void rateLimiterFunctional() {
        RateLimiter limiter = new RateLimiter(5);

        assertEquals(2, limiter.calculate(2, 1), epsilon);
        assertEquals(7, limiter.calculate(10, 1), epsilon);
        assertEquals(2, limiter.calculate(-5, 1), epsilon);
        assertEquals(0, limiter.calculate(0, 1), epsilon);
    }
}
