/**
Copyright 2022-2023 FRC Team 997

This program is free software: 
you can redistribute it and/or modify it under the terms of the 
GNU General Public License as published by the Free Software Foundation, 
either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SpartanLib2. 
If not, see <https://www.gnu.org/licenses/>.
*/
package org.chsrobotics.lib.hardware.LEDStrip;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LEDAnimationTests {
    @Test
    public void LEDAnimationGradientCascadeWorks() {
        LEDAnimation gCascade = LEDAnimation.gradientCascade(5, RGBColor.BLACK, RGBColor.WHITE);

        RGBColor lightGrey = new RGBColor(63, 63, 63);
        RGBColor midGrey = new RGBColor(127, 127, 127);
        RGBColor darkerGrey = new RGBColor(191, 191, 191);

        assertEquals(
                new LEDAnimationFrame(
                        RGBColor.BLACK, lightGrey, midGrey, darkerGrey, RGBColor.WHITE),
                gCascade.getFrame(0));

        assertEquals(
                new LEDAnimationFrame(
                        RGBColor.WHITE, RGBColor.BLACK, lightGrey, midGrey, darkerGrey),
                gCascade.getFrame(1));
    }

    @Test
    public void LEDAnimationCascadeWorks() {
        LEDAnimation cascade =
                LEDAnimation.cascading(
                        new LEDAnimationFrame(RGBColor.WHITE, RGBColor.BLUE, RGBColor.BLACK));

        assertEquals(
                new LEDAnimationFrame(RGBColor.BLACK, RGBColor.WHITE, RGBColor.BLUE),
                cascade.getFrame(1));
    }

    @Test
    public void LEDAnimationFlashingWorks() {
        LEDAnimationFrame a = new LEDAnimationFrame(RGBColor.BLUE, RGBColor.WHITE);
        LEDAnimationFrame b = new LEDAnimationFrame(RGBColor.RED, RGBColor.GREEN);

        LEDAnimation flashing = LEDAnimation.flashing(3, 2, a, b);

        assertEquals(a, flashing.getFrame(0));
        assertEquals(a, flashing.getFrame(1));
        assertEquals(a, flashing.getFrame(2));
        assertEquals(b, flashing.getFrame(3));
        assertEquals(b, flashing.getFrame(4));
    }
}
