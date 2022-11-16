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
package org.chsrobotics.lib.telemetry;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.BooleanLogEntry;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.util.datalog.StringLogEntry;

/**
 * Utility to streamline publishing data to NetworkTables and saving it to an on-robot log file.
 *
 * <p>Parameterized to the type of data to publish/log.
 *
 * <p>If the data isn't numerical (Double, Int, Long, etc) or Boolean/String, this will instead
 * publish and log the result of its {@code .toString()} method.
 *
 * @param <T> The data type of the log entry.
 */
public class Logger<T> {
    private final DataLog log;
    private boolean publishToNT;
    private boolean recordInLog;

    private final String logEntryIdentifier;

    private BooleanLogEntry boolLogEntry;
    private DoubleLogEntry doubleLogEntry;
    private StringLogEntry stringLogEntry;

    private final NetworkTableEntry ntEntry;

    private boolean logEntryExists = false;

    private T previousValue = null;

    /**
     * Constructs a Logger using a provided DataLog, with the option of whether to publish to NT and
     * the log.
     *
     * @param log The DataLog to log values inside of, most likely from {@code
     *     HighLevelLogger.getLog()} or whatever log is being used program-wide.
     * @param key A string identifier to associate with and describe the data on the dashboard. In
     *     the log, the string identifier will be "{@code [subdirName]_[key]}".
     * @param subdirName The string name of the existing or new NetworkTables sub-table to write to.
     * @param publishToNT Whether this should push logged values to NetworkTables.
     * @param recordInLog Whether this should store logged values in an on-robot log file.
     */
    public Logger(
            DataLog log, String key, String subdirName, boolean publishToNT, boolean recordInLog) {
        this.log = log;
        this.publishToNT = publishToNT;
        this.recordInLog = recordInLog;

        logEntryIdentifier = subdirName + "_" + key;

        ntEntry = NetworkTableInstance.getDefault().getTable(subdirName).getEntry(key);
    }

    /**
     * Constructs a Logger using {@code HighLevelLogger.getLog()}, publishing to NT and logging.
     *
     * @param key A string identifier to associate with and describe the data on the dashboard. In
     *     the log, the string identifier will be "{@code [subdirName]_[key]}".
     * @param subdirName The string name of the existing or new NetworkTables sub-table to write to.
     */
    public Logger(String key, String subdirName) {
        this(HighLevelLogger.getLog(), key, subdirName, true, true);
    }

    /**
     * Returns whether this is publishing values to NetworkTables.
     *
     * @return True if new values sent to {@code update()} will be pushed to the dashboard.
     */
    public boolean isPublishingToNT() {
        return publishToNT;
    }

    /** New values sent to {@code update()} will be pushed to NetworkTables. */
    public void startPublishingToDashboard() {
        publishToNT = true;
    }

    /** New values sent to {@code update()} will not be pushed to NetworkTables. */
    public void stopPublishingToDashboard() {
        publishToNT = false;
    }

    /**
     * Returns whether this is recording values to the on-robot log.
     *
     * @return True if new values sent to {@code update()} will be recorded to the log.
     */
    public boolean isRecordingToLog() {
        return recordInLog;
    }

    /** New values sent to {@code update()} will be recorded to the log. */
    public void startRecordingToLog() {
        recordInLog = true;
    }

    /** New values sent to {@code update()} will not be recorded to the log. */
    public void stopRecordingToLog() {
        recordInLog = false;
    }

    /**
     * Feeds a new value to the Logger, which may be sent to NetworkTables, an off-robot log, both,
     * or neither, depending on how this class is configured.
     *
     * @param value An instance of T (whatever this class was parameterized with).
     */
    public void update(T value) {
        if (value instanceof Double
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Short
                || value instanceof Byte
                || value instanceof Float) {
            if (!logEntryExists) {
                doubleLogEntry = new DoubleLogEntry(log, logEntryIdentifier);
                logEntryExists = true;
            }
            if (recordInLog && !value.equals(previousValue)) doubleLogEntry.append((double) value);
            if (publishToNT && !value.equals(previousValue)) {
                ntEntry.forceSetDouble((double) value);
            }
        } else if (value instanceof Boolean) {
            if (!logEntryExists) {
                boolLogEntry = new BooleanLogEntry(log, logEntryIdentifier);
                logEntryExists = true;
            }
            if (recordInLog && !value.equals(previousValue)) boolLogEntry.append((boolean) value);
            if (publishToNT && !value.equals(previousValue)) {
                ntEntry.forceSetBoolean((boolean) value);
            }
        } else if (value instanceof String) {
            if (!logEntryExists) {
                stringLogEntry = new StringLogEntry(log, logEntryIdentifier);
                logEntryExists = true;
            }
            if (recordInLog && !value.equals(previousValue)) stringLogEntry.append((String) value);
            if (publishToNT && !value.equals(previousValue)) {
                ntEntry.forceSetString((String) value);
            }
        } else {
            if (!logEntryExists) {
                stringLogEntry = new StringLogEntry(log, logEntryIdentifier);
                logEntryExists = true;
            }
            if (recordInLog && !value.equals(previousValue))
                stringLogEntry.append(value.toString());
            if (publishToNT && !value.equals(previousValue)) {
                ntEntry.forceSetString(value.toString());
            }
        }
        previousValue = value;
    }
}
