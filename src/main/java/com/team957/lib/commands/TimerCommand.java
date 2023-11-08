/**
Copyright 2023 FRC Teams 957 and 997

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
package com.team957.lib.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.Set;

/** Command which starts a timer when it is scheduled. */
public class TimerCommand extends Command {
    private final Timer timer = new Timer();

    /** Constructs a TimerCommand. */
    public TimerCommand() {
        timer.stop();
        timer.reset();
    }

    public void initialize() {
        timer.start();
    }

    /**
     * Returns the time in seconds since the command was scheduled.
     *
     * @return The time since scheduling.
     */
    public double getTimeElapsed() {
        return timer.get();
    }

    public Set<Subsystem> getRequirements() {
        return Set.of();
    }
}
