/**
Copyright 2022 FRC Team 997

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
package org.chsrobotics.lib.input;

import edu.wpi.first.wpilibj2.command.button.Trigger;
import java.util.function.Supplier;

/** Represents a hardware button on an input device which has two states. */
public class JoystickButton extends Trigger {
    private final Supplier<Boolean> pressedLambda;
    private final String name;
    private final boolean isReal;

    /**
     * Constructs a JoystickButton.
     *
     * @param pressedLambda Supplier of the boolean state of the button.
     * @param name String identifier of the index of this button in the driver station view.
     * @param isReal If this JoystickButton is a representation of actual hardware.
     */
    protected JoystickButton(Supplier<Boolean> pressedLambda, String name, boolean isReal) {
        this.pressedLambda = pressedLambda;
        this.name = name;
        this.isReal = isReal;
    }

    /**
     * Returns a boolean of whether the button is pressed (in a {@code true} state).
     *
     * @return Whether the button is pressed.
     */
    @Override
    public boolean get() {
        return pressedLambda.get();
    }

    /**
     * Whether this JoystickButton is a representation of physical hardware.
     *
     * @return If this exists on an actual controller.
     */
    public boolean isReal() {
        return isReal;
    }

    @Override
    public String toString() {
        return ("JoystickButton: " + name);
    }
}
