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
package com.team957.lib.controllers.feedback;

import com.team957.lib.math.UtilityMath;
import com.team957.lib.util.DeltaTimeUtil;
import com.team957.lib.util.SizedStack;
import edu.wpi.first.math.MathUtil;
import java.util.Objects;
import monologue.Annotations.Log;
import monologue.Logged;

/**
 * Implementation of a simple Proportional-Integral-Derivative feedback controller.
 *
 * <p>The PID controller is a function with the goal of bringing the difference (or error) between a
 * setpoint (target) and an actual measurement to zero. It does this using feedback control, where
 * the past state(s) of a controlled system are leveraged to find the current output.
 *
 * <p>Since the values of the gains are tuned, they and the output are dimensionless. However, the
 * gains need to be optimized to output what would be sensible values of other units, such as motor
 * controller voltage or duty cycle.
 *
 * <p>The PID controller has three internal components: fittingly, P, I, and D.
 *
 * <p>P, or Proportional, simply consists of multiplication of the error by a constant. This term
 * does the bulk of lifting in the controller and is usually primarily responsible for the bulk of
 * the output. In a theoretical world without static friction and motor input limits, it's enough to
 * get to the setpoint.
 *
 * <p>I, or Integral, consists of the multiplication of the *sum* of past error by a constant. This
 * past sum should be reset at sensible times to keep it from excessively accumulating. The Integral
 * term, as it sums up error over time, is able to turn a small amount of constant error the P term
 * can't solve (steady-state error) into enough of a control input to get to the setpoint.
 *
 * <p>D, Derivative and the final term, consists of the multiplication of the *rate of change* of
 * the error by a constant. This is useful to prevent the P term of the controller from entering a
 * series of oscillations where it goes back and forth with high amplitude around the setpoint.
 *
 * <p>Methods for tuning the constants of the PID Controller are a matter of opinion, however, the
 * original author of this doc recommends to start with all constants at 0. Tweak the P term
 * slightly until you arrive at a situation where the P term rapidly gets to the setpoint without
 * overshooting. Any steady-state error that is left can be sparingly corrected with the I term. The
 * D term should be used when the P controller needed for good performance near the setpoint causes
 * the controller to overshoot the setpoint.
 *
 * <p>When docs in this class refer to "position" or "velocity", "position" refers to the quantity
 * of the thing being controlled, "velocity" to the rate of change of that thing. So it's possible
 * to make a velocity PID controller, or something else with a controlled quantity other than
 * position.
 */
public class PID implements Logged {

    /** Data class for holding the gains to a PID controller. */
    public static class PIDConstants {
        private final double kP;
        private final double kI;
        private final double kD;

        /**
         * Constructs a PIDConstants out of provided gains.
         *
         * @param kP The proportional gain.
         * @param kI The integral gain.
         * @param kD The derivative gain.
         */
        public PIDConstants(double kP, double kI, double kD) {
            this.kP = kP;
            this.kI = kI;
            this.kD = kD;
        }

        /**
         * Returns the proportional gain.
         *
         * @return The kP of the constants.
         */
        public double getkP() {
            return kP;
        }

        /**
         * Returns the integral gain.
         *
         * @return The kI of the constants.
         */
        public double getkI() {
            return kI;
        }

        /**
         * Returns the derivative gain.
         *
         * @return The kD of the constants.
         */
        public double getkD() {
            return kD;
        }

        @Override
        public boolean equals(Object other) {
            double epsilon = 0.0001;
            if (other instanceof PIDConstants) {
                PIDConstants rhs = (PIDConstants) other;
                return (Math.abs(kP - rhs.kP) < epsilon
                        && Math.abs(kI - rhs.kI) < epsilon
                        && Math.abs(kD - rhs.kD) < epsilon);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(kP, kI, kD);
        }

        @Override
        public String toString() {
            return ("PIDConstants: " + kP + ", " + kI + ", " + kD);
        }
    }

    private final boolean angular;

    private double kP;
    private double kI;
    private double kD;

    private double lastMeasurement = 0;
    private double lastSetpoint = 0;

    private double lastPContribution = 0;
    private double lastIContribution = 0;
    private double lastDContribution = 0;

    private double maxAbsControlEffort = 0;

    private double maxAbsPContribution = 0;
    private double maxAbsIContribution = 0;
    private double maxAbsDContribution = 0;

    private double setpoint;

    private double velocity = 0;

    private double positionTolerance = 0.02;
    private double velocityTolerance = 0.02;

    private double currentValue = 0;

    private final SizedStack<Double> integrationStack;

    private final DeltaTimeUtil dtUtil = new DeltaTimeUtil();

    /**
     * Constructs a PID with given gains and a finite integration window.
     *
     * @param kP The initial proportional gain of the controller.
     * @param kI The initial integral gain of the controller.
     * @param kD The initial derivative gain of the controller.
     * @param integrationWindow The number of past values to consider for integral accumulation. If
     *     less than or equal to 0, will be an infinite window.
     * @param initialSetpoint The initial setpoint (or target) of the controller.
     * @param angular Whether the controller is controlling an angular quantity that "wraps".
     *     Measurements and setpoints are expected to be in radians if this is true.
     */
    public PID(
            double kP,
            double kI,
            double kD,
            int integrationWindow,
            double initialSetpoint,
            boolean angular) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;

        integrationStack = new SizedStack<>(integrationWindow);

        if (angular) {
            setpoint = MathUtil.angleModulus(initialSetpoint);
            lastSetpoint = setpoint;
        } else {
            setpoint = initialSetpoint;
            lastSetpoint = initialSetpoint;
        }

        this.angular = angular;
    }

    /**
     * Constructs a non-angular PID with given gains and a finite integration window.
     *
     * @param kP The initial proportional gain of the controller.
     * @param kI The initial integral gain of the controller.
     * @param kD The initial derivative gain of the controller.
     * @param integrationWindow The number of past values to consider for integral accumulation. If
     *     less than or equal to 0, will be an infinite window.
     * @param initialSetpoint The initial setpoint (or target) of the controller.
     */
    public PID(double kP, double kI, double kD, int integrationWindow, double initialSetpoint) {
        this(kP, kI, kD, integrationWindow, initialSetpoint, false);
    }

    /**
     * Constructs a PID with a given PIDConstants and a finite integration window.
     *
     * @param constants The PIDConstants containing the gains for this controller.
     * @param integrationWindow The number of past values to consider for integral accumulation. If
     *     less than or equal to 0, will be an infinite window.
     * @param initialSetpoint The initial setpoint (or target) of the controller.
     * @param angular Whether the controller is controlling an angular quantity that "wraps".
     *     Measurements and setpoints are expected to be in radians if this is true.
     */
    public PID(
            PIDConstants constants,
            int integrationWindow,
            double initialSetpoint,
            boolean angular) {
        this(
                constants.getkP(),
                constants.getkI(),
                constants.getkD(),
                integrationWindow,
                initialSetpoint,
                angular);
    }

    /**
     * Constructs a non-angular PID with a given PIDConstants and a finite integration window.
     *
     * @param constants The PIDConstants containing the gains for this controller.
     * @param integrationWindow The number of past values to consider for integral accumulation. If
     *     less than or equal to 0, will be an infinite window.
     * @param initialSetpoint The initial setpoint (or target) of the controller.
     */
    public PID(PIDConstants constants, int integrationWindow, double initialSetpoint) {
        this(
                constants.getkP(),
                constants.getkI(),
                constants.getkD(),
                integrationWindow,
                initialSetpoint,
                false);
    }

    /**
     * Constructs a PID with given gains and an infinite integration window.
     *
     * @param kP The initial proportional gain of the controller.
     * @param kI The initial integral gain of the controller.
     * @param kD The initial derivative gain of the controller.
     * @param initialSetpoint The initial setpoint (or target) of the controller.
     * @param angular Whether the controller is controlling an angular quantity that "wraps".
     *     Measurements and setpoints are expected to be in radians if this is true.
     */
    public PID(double kP, double kI, double kD, double initialSetpoint, boolean angular) {
        this(kP, kI, kD, 0, initialSetpoint, angular);
    }

    /**
     * Constructs a non-angular PID with given gains and an infinite integration window.
     *
     * @param kP The initial proportional gain of the controller.
     * @param kI The initial integral gain of the controller.
     * @param kD The initial derivative gain of the controller.
     * @param initialSetpoint The initial setpoint (or target) of the controller.
     */
    public PID(double kP, double kI, double kD, double initialSetpoint) {
        this(kP, kI, kD, 0, initialSetpoint, false);
    }

    /**
     * Constructs a PID with a given PIDConstants and an infinite integration window.
     *
     * @param constants The PIDConstants containing the gains for this controller.
     * @param initialSetpoint The initial setpoint (or target) of the controller.
     * @param angular Whether the controller is controlling an angular quantity that "wraps".
     *     Measurements and setpoints are expected to be in radians if this is true.
     */
    public PID(PIDConstants constants, double initialSetpoint, boolean angular) {
        this(constants, 0, initialSetpoint, angular);
    }

    /**
     * Constructs a non-angular PID with a given PIDConstants and an infinite integration window.
     *
     * @param constants The PIDConstants containing the gains for this controller.
     * @param initialSetpoint The initial setpoint (or target) of the controller.
     */
    public PID(PIDConstants constants, double initialSetpoint) {
        this(constants, 0, initialSetpoint);
    }

    /**
     * Sets the gains of the controller using provided gains.
     *
     * @param kP The proportional gain for the controller.
     * @param kI The integral gain for the controller.
     * @param kD The derivative gain for the controller.
     */
    public void setConstants(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    /**
     * Sets the gains of the controller using a provided PIDConstants.
     *
     * @param constants The PIDConstants containing the gains for the controller.
     */
    public void setConstants(PIDConstants constants) {
        this.kP = constants.getkP();
        this.kI = constants.getkI();
        this.kD = constants.getkD();
    }

    /**
     * Returns the current gains of the controller.
     *
     * @return A PIDConstants containing the gains of the controller.
     */
    public PIDConstants getConstants() {
        return new PIDConstants(kP, kI, kD);
    }

    /**
     * Returns the current proportional gain of the controller.
     *
     * @return The current kP.
     */
    @Log
    public double getkP() {
        return kP;
    }

    /**
     * Sets the proportional gain of the controller.
     *
     * @param kP The new kP for the controller.
     */
    public void setkP(double kP) {
        this.kP = kP;
    }

    /**
     * Returns the current integral gain of the controller.
     *
     * @return The current kI.
     */
    @Log
    public double getkI() {
        return kI;
    }

    /**
     * Sets the integral gain of the controller.
     *
     * @param kI The new kI for the controller.
     */
    public void setkI(double kI) {
        this.kI = kI;
    }

    /**
     * Returns the current derivative gain of the controller.
     *
     * @return The current kD.
     */
    @Log
    public double getkD() {
        return kD;
    }

    /**
     * Sets the derivative gain of the controller.
     *
     * @param kD The new kD for the controller.
     */
    public void setkD(double kD) {
        this.kD = kD;
    }

    /**
     * Sets the setpoint (target) of the controller.
     *
     * @param value The new target of the controller.
     */
    public void setSetpoint(double value) {
        if (angular) setpoint = MathUtil.angleModulus(value);
        else setpoint = value;
    }

    /**
     * Returns the current setpoint (target) of the controller.
     *
     * @return The current setpoint.
     */
    @Log
    public double getSetpoint() {
        return setpoint;
    }

    /**
     * Returns the accumulated past error in the integral term. Not equal to the output of the I
     * term: this is not multiplied by the gain.
     *
     * @return The integral of error with respect to time from the last reset to now.
     */
    @Log
    public double getIntegralAccumulation() {
        double integrationSum = 0;

        for (double entry : integrationStack) {
            integrationSum += entry;
        }

        return integrationSum;
    }

    /** Resets accumulation of past error in the integral term. */
    public void resetIntegralAccumulation() {
        integrationStack.clear();
    }

    /** Resets the previous measurement used for velocity approximation for the derivative term. */
    public void resetPreviousMeasurement() {
        lastMeasurement = 0;
        lastSetpoint = setpoint;
    }

    /** Resets all references to past states in the controller, effectively restarting it. */
    public void reset() {
        resetIntegralAccumulation();
        resetPreviousMeasurement();
    }

    /**
     * Sets the maximum error of both position and velocity allowed for {@code isAtSetpoint()} to
     * return true.
     *
     * @param positionTolerance The maximum allowed position error, as a proportion of the setpoint.
     * @param velocityTolerance The maximum allowed absolute velocity per second, as a proportion of
     *     the setpoint / second.
     */
    public void setSetpointTolerance(double positionTolerance, double velocityTolerance) {
        this.positionTolerance = positionTolerance;
        this.velocityTolerance = velocityTolerance;
    }

    /**
     * Returns the maximum allowed position error for {@code isAtSetpoint()} to return true.
     *
     * @return The maximum allowed position error, as a proportion of the setpoint.
     */
    @Log
    public double getSetpointPositionTolerance() {
        return positionTolerance;
    }

    /**
     * Returns the maximum allowed velocity error for {@code isAtSetpoint()} to return true.
     *
     * @return The maximum allowed absolute velocity per second, as a proportion of the setpoint.
     */
    @Log
    public double getSetpointVelocityTolerance() {
        return velocityTolerance;
    }

    /**
     * Returns an output from the controller with a given dt.
     *
     * @param measurement The value of the measured feedback.
     * @param dt The time, in seconds, since the last update of this controller.
     * @return The output of the controller.
     */
    public double calculate(double measurement, double dt) {
        if (angular) measurement = MathUtil.angleModulus(measurement);

        double error =
                angular
                        ? MathUtil.inputModulus(setpoint - measurement, -Math.PI, Math.PI)
                        : setpoint - measurement;

        integrationStack.push(dt * error);

        if (dt == 0) { // sensible way to handle dt of zero
            velocity = 0;
        } else {
            velocity = ((error - (lastSetpoint - lastMeasurement)) / dt);
        }

        double integrationSum = 0;

        for (double entry : integrationStack) {
            integrationSum += entry;
        }

        double rawP = kP * (error);
        double rawI = kI * integrationSum;
        double rawD = kD * velocity;

        if (Math.abs(maxAbsPContribution) == 0) lastPContribution = rawP;
        else lastPContribution = UtilityMath.clamp(maxAbsPContribution, rawP);

        if (Math.abs(maxAbsIContribution) == 0) lastIContribution = rawI;
        else lastIContribution = UtilityMath.clamp(maxAbsIContribution, rawI);

        if (Math.abs(maxAbsDContribution) == 0) lastDContribution = rawD;
        else lastDContribution = UtilityMath.clamp(maxAbsDContribution, rawD);

        lastMeasurement = measurement;
        lastSetpoint = setpoint;

        double effortsSum = lastPContribution + lastIContribution + lastDContribution;

        if (Math.abs(maxAbsControlEffort) == 0) currentValue = effortsSum;
        else currentValue = UtilityMath.clamp(maxAbsControlEffort, effortsSum);

        return currentValue;
    }

    /**
     * Returns the last output of the controller without updating with a new value.
     *
     * @return The last output of the controller.
     */
    @Log
    public double getCurrentValue() {
        return currentValue;
    }

    /**
     * Returns the last result of just the P term.
     *
     * @return The last P result. Equal to 0 if {@code calculate()} has not been called.
     */
    @Log
    public double getPContribution() {
        return lastPContribution;
    }

    /**
     * Returns the last result of just the I term.
     *
     * @return The last I result. Equal to 0 if {@code calculate()} has not been called.
     */
    @Log
    public double getIContribution() {
        return lastIContribution;
    }

    /**
     * Returns the last result of just the D term.
     *
     * @return The last D result. Equal to 0 if {@code calculate()} has not been called.
     */
    @Log
    public double getDContribution() {
        return lastDContribution;
    }

    /**
     * Sets a new value to use to constrain the maximum absolute control effort from the controller.
     *
     * @param newValue The maximum absolute control effort. If zero, no limits are applied.
     */
    public void setMaxAbsControlEffort(double newValue) {
        maxAbsControlEffort = newValue;
    }

    /**
     * Sets a new value to use to constrain the maximum absolute contribution from the Proportional
     * term of the controller.
     *
     * @param newValue The maximum absolute contribution of the P term. If zero, no limits are
     *     applied.
     */
    public void setMaxAbsPContribution(double newValue) {
        maxAbsPContribution = Math.abs(newValue);
    }

    /**
     * Sets a new value to use to constrain the maximum absolute contribution from the Integral term
     * of the controller.
     *
     * @param newValue The maximum absolute contribution of the I term. If zero, no limits are
     *     applied.
     */
    public void setMaxAbsIContribution(double newValue) {
        maxAbsIContribution = Math.abs(newValue);
    }

    /**
     * Sets a new value to use to constrain the maximum absolute contribution from the Derivative
     * term of the controller.
     *
     * @param newValue The maximum absolute contribution of the D term. If zero, no limits are
     *     applied.
     */
    public void setMaxDContribution(double newValue) {
        maxAbsDContribution = Math.abs(newValue);
    }

    /**
     * Returns the maximum absolute control effort allowed from the controller.
     *
     * @return The maximum absolute P contribution. If zero, no limits are being applied.
     */
    @Log
    public double getMaxAbsControlEffort() {
        return maxAbsControlEffort;
    }

    /**
     * Returns the maximum absolute contribution allowed from the Proportional term of the
     * controller.
     *
     * @return The maximum absolute P contribution. If zero, no limits are being applied.
     */
    @Log
    public double getMaxAbsPContribution() {
        return maxAbsPContribution;
    }

    /**
     * Returns the maximum absolute contribution allowed from the Integral term of the controller.
     *
     * @return The maximum absolute I contribution. If zero, no limits are being applied.
     */
    @Log
    public double getMaxAbsIContribution() {
        return maxAbsIContribution;
    }

    /**
     * Returns the maximum absolute contribution allowed from the Derivative term of the controller.
     *
     * @return The maximum absolute D contribution. If zero, no limits are being applied.
     */
    @Log
    public double getMaxAbsDContribution() {
        return maxAbsDContribution;
    }

    /**
     * Returns an output from the controller with a given dt.
     *
     * <p>Uses the time elapsed since last calling this method as a parameter. If this method is
     * being called for the first time, uses the time since construction.
     *
     * @param measurement The value of the measured feedback.
     * @return The output of the controller.
     */
    public double calculate(double measurement) {
        return calculate(measurement, dtUtil.getTimeSecondsSinceLastCall());
    }

    /**
     * Returns whether the controller has reached the setpoint with minimal velocity.
     *
     * @return Whether the controller is within the minimum position and velocity errors.
     */
    @Log
    public boolean atSetpoint() {
        return (Math.abs(setpoint - lastMeasurement) < Math.abs(positionTolerance * setpoint)
                && Math.abs(velocity) < Math.abs(velocityTolerance * setpoint));
    }
}
