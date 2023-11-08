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
package com.team957.lib.commands;

import com.team957.lib.telemetry.HighLevelLogger;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Simple command to log a message to the default DataLog (given by {@code
 * HighLevelLogger.getLog()}).
 *
 * <p>This command initializes and ends in the same scheduler loop.
 */
public class LogMessageCommand extends Command {
    private final Supplier<String> messageLambda;

    /**
     * Construts a LogMessageCommand. Use this when the value of the message might change from
     * construction to command scheduling.
     *
     * @param messageLambda Lambda of the message to log.
     */
    public LogMessageCommand(Supplier<String> messageLambda) {
        this.messageLambda = messageLambda;
    }

    /**
     * Constructs a LogMessageCommand. Use this when the value of the message won't change from
     * construction to command scheduling.
     *
     * @param message The message to log.
     */
    public LogMessageCommand(String message) {
        this(() -> message);
    }

    public boolean runsWhenDisabled() {
        return true;
    }

    public void initialize() {
        HighLevelLogger.getInstance().logMessage(messageLambda.get());
    }

    public boolean isFinished() {
        return true;
    }

    public Set<Subsystem> getRequirements() {
        return Set.of();
    }
}
