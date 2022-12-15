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
package org.chsrobotics.lib.math;

/** Various useful small math functions. */
public class UtilityMath {
    private static final double defaultProportionEpsilon = 1E-3;
    private static final double defaultAbsoluteEpsilon = 1E-5;

    /**
     * Normalizes an angle in radians between 0 and 2 pi.
     *
     * @param angleRadians Value in radians of the angle.
     * @return The normalized value.
     */
    public static double normalizeAngleRadians(double angleRadians) {
        return ((angleRadians % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI);
        // remainder of the angle and 2pi, positive coterminal'd, and then remainder'd again
    }

    /**
     * Normalizes an angle in degrees between 0 and 360.
     *
     * @param angleDegrees Value of the angle.
     * @return The normalized value.
     */
    public static double normalizeAngleDegrees(double angleDegrees) {
        return ((angleDegrees % 360) + 360) % 360;
        // remainder of the angle and 360, positive coterminal'd, and then remainder'd again
    }

    /**
     * Returns the angle in radians between two other angles with the smallest absolute value.
     *
     * <p>A negative number indicates a clockwise rotation from angle a to angle b.
     *
     * @param angleA The first angle, in radians.
     * @param angleB The second angle, in raidans.
     * @return A signed angle in radians representing the smallest (positive is counterclockwise)
     *     angle between angles a and b.
     */
    public static double smallestAngleRadiansBetween(double angleA, double angleB) {
        double normA = UtilityMath.normalizeAngleRadians(angleA);
        double normB = UtilityMath.normalizeAngleRadians(angleB);

        double diff = (normB - normA + Math.PI) % (2 * Math.PI) - Math.PI;
        return (diff < -Math.PI) ? diff + (2 * Math.PI) : diff;
    }

    /**
     * Returns the angle in degrees between two other angles with the smallest absolute value.
     *
     * <p>A negative number indicates a clockwise rotation from angle a to angle b.
     *
     * @param angleA The first angle, in degrees.
     * @param angleB The second angle, in degrees.
     * @return A signed angle in degrees representing the smallest (positive is counterclockwise)
     *     angle between angles a and b.
     */
    public static double smallestAngleDegreesBetween(double angleA, double angleB) {
        double normA = UtilityMath.normalizeAngleDegrees(angleA);
        double normB = UtilityMath.normalizeAngleDegrees(angleB);

        double diff = (normB - normA + 180) % 360 - 180;
        return (diff < -180) ? diff + 360 : diff;
    }

    /**
     * Interpolates a value between two points.
     *
     * <p>For more complex, multi-point interpolation, use the {@link MultiPointInterpolator} class.
     *
     * @param y0 The initial value of the dependent variable.
     * @param x0 The initial value of the independent variable.
     * @param y1 The final value of the dependent variable.
     * @param x1 The final value of the independent variable.
     * @param x The place at which to sample for the returned value.
     * @return The linearly interpolated value.
     */
    public static double simpleLinearInterpolation(
            double y0, double x0, double y1, double x1, double x) {
        return y0 + ((x - x0) * (y1 - y0) / (x1 - x0));
        // starting value, plus the distance of the sample from the start multiplied by
        // the slope
    }

    /**
     * Scales a set of doubles symmetrically such that they sum to a desired number, while
     * maintaining the same ratio.
     *
     * @param inputs An array of doubles. If empty, this will return an empty array.
     * @param desiredSum The desired sum, positive or negative, of the outputs. If equal to zero,
     *     this will return an array of zeros of length equal to the input's length.
     * @return An array of doubles with the same ratios between each other as the inputs.
     */
    public static double[] scaleToSum(double[] inputs, double desiredSum) {
        if (inputs.length == 0) return inputs;
        if (desiredSum == 0) return new double[inputs.length];

        double sum = 0;
        for (double value : inputs) {
            sum += value;
        }

        double[] outputs = new double[inputs.length];

        double scalingFactor = desiredSum / sum;

        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = inputs[i] * (scalingFactor);
        }

        return outputs;
    }

    /**
     * Scales a set of doubles symmetrically to ensure that none of them exceed a maximum absolute
     * value, while still maintaining the same ratio.
     *
     * @param inputs An array of the input values. If empty, this will return an empty array.
     * @param maxAbsoluteValue The maximum absolute value allowed for an output.
     * @return An array of the scaled values, in the same order as they were input.
     */
    public static double[] normalizeSet(double[] inputs, double maxAbsoluteValue) {
        if (inputs.length == 0) return inputs;
        int highestIndex = 0; // find the largest absolute value element in the list
        for (int i = 0; i < inputs.length; i++) {
            if (Math.abs(inputs[highestIndex]) < Math.abs(inputs[i])) {
                highestIndex = i;
            }
        }
        if (Math.abs(inputs[highestIndex]) <= maxAbsoluteValue) {
            return inputs; // if it's <= the max absolute value, just return the inputs
        } else {
            double[] outputs = new double[inputs.length];
            double scalingFactor = maxAbsoluteValue / Math.abs(inputs[highestIndex]);
            for (int i = 0;
                    i < inputs.length;
                    i++) { // get the scaling factor and apply it to each element
                outputs[i] = inputs[i] * scalingFactor;
            }
            return outputs;
        }
    }

    /**
     * Takes in a series of numbers and returns their arithmetic mean.
     *
     * @param inputs The values to average. If empty, this will return {@code 0}.
     * @return The arithmetic mean of the values.
     */
    public static double arithmeticMean(double... inputs) {
        if (inputs.length == 0) return 0;

        double sum = 0;

        for (double value : inputs) sum += value;

        return sum / inputs.length;
    }

    /**
     * Takes in a series of numbers and returns their geometric mean.
     *
     * @param inputs The values to average. If empty, this will return {@code 0}.
     * @return The geometric mean of the values.
     */
    public static double geometricMean(double... inputs) {
        if (inputs.length == 0) return 0;

        double product = 1;

        for (double value : inputs) product = product * value;

        return Math.pow(product, 1 / (inputs.length));
    }

    /**
     * Returns whether two values are equal to within an epsilon (calculated by their difference and
     * expected from floating-point arithmetic). Essentially just "close enough".
     *
     * @param valueA The first number to compare.
     * @param valueB The second number to compare.
     * @param absoluteEpsilon The maximum allowed difference between the two to return true.
     * @return Whether two values are "close enough" to each other.
     */
    public static boolean epsilonEqualsAbsolute(
            double valueA, double valueB, double absoluteEpsilon) {
        return (Math.abs(valueA - valueB) <= absoluteEpsilon);
    }

    /**
     * Returns whether two values are equal to within an epsilon (calculated by their difference and
     * expected from floating-point arithmetic). Essentially just "close enough".
     *
     * <p>Uses a default epsilon value of 1E-5 (0.00001).
     *
     * @param valueA The first number to compare.
     * @param valueB The second number to compare.
     * @return Whether two values are "close enough" to each other.
     */
    public static boolean epsilonEqualsAbsolute(double valueA, double valueB) {
        return epsilonEqualsAbsolute(valueA, valueB, defaultAbsoluteEpsilon);
    }

    /**
     * Returns whether two values are equal to within an epsilon (calculated multiplicatively and
     * expected from floating-point arithmetic). Essentially just "close enough".
     *
     * <p>To avoid division by zero, if only one of the two terms is exactly equal to zero, this
     * will return false. If *both* are exactly equal to zero, this returns true.
     *
     * @param valueA The first number to compare.
     * @param valueB The second number to compare.
     * @param proportionEpsilon If one value is bigger than another by a factor of at least {@code 1
     *     + proportionEpsilon}, this will return false.
     * @return Whether two values are "close enough" to each other.
     */
    public static boolean epsilonEqualsProportion(
            double valueA, double valueB, double proportionEpsilon) {
        if (valueA == 0 && valueB == 0) return true;
        if (valueA == 0 || valueB == 0) return false;
        return (Math.abs((valueA / valueB) - 1) <= proportionEpsilon);
    }

    /**
     * Returns whether two values are equal to within an epsilon (calculated multiplicatively and
     * expected from floating-point arithmetic). Essentially just "close enough".
     *
     * <p>Uses a default epsilon value of 1E-3 (0.001).
     *
     * <p>To avoid division by zero, if only one of the two terms is exactly equal to zero, this
     * will return false. If *both* are exactly equal to zero, this returns true.
     *
     * @param valueA The first number to compare.
     * @param valueB The second number to compare.
     * @return Whether two values are "close enough" to each other.
     */
    public static boolean epsilonEqualsProportion(double valueA, double valueB) {
        return epsilonEqualsProportion(valueA, valueB, defaultProportionEpsilon);
    }
}
