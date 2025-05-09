/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This is not the original file distributed by the Apache Software Foundation
 * It has been modified by the Hipparchus project
 */

package org.hipparchus.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.hipparchus.Field;
import org.hipparchus.FieldElement;
import org.hipparchus.CalculusFieldElement;
import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.MathRuntimeException;
import org.hipparchus.exception.NullArgumentException;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well19937c;

/**
 * Arrays utilities.
 */
public class MathArrays {

    /**
     * Private constructor.
     */
    private MathArrays() {}

    /**
     * Real-valued function that operates on an array or a part of it.
     */
    public interface Function {

        /** Operates on an entire array.
         * @param array Array to operate on.
         * @return the result of the operation.
         */
        double evaluate(double[] array);

        /** Operates on a sub-array.
         * @param array Array to operate on.
         * @param startIndex Index of the first element to take into account.
         * @param numElements Number of elements to take into account.
         * @return the result of the operation.
         */
        double evaluate(double[] array,
                        int startIndex,
                        int numElements);

    }

    /**
     * Create a copy of an array scaled by a value.
     *
     * @param arr Array to scale.
     * @param val Scalar.
     * @return scaled copy of array with each entry multiplied by val.
     */
    public static double[] scale(double val, final double[] arr) {
        double[] newArr = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            newArr[i] = arr[i] * val;
        }
        return newArr;
    }

    /**
     * Multiply each element of an array by a value.
     * <p>
     * The array is modified in place (no copy is created).
     *
     * @param arr Array to scale
     * @param val Scalar
     */
    public static void scaleInPlace(double val, final double[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] *= val;
        }
    }

    /**
     * Creates an array whose contents will be the element-by-element
     * addition of the arguments.
     *
     * @param a First term of the addition.
     * @param b Second term of the addition.
     * @return a new array {@code r} where {@code r[i] = a[i] + b[i]}.
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static double[] ebeAdd(double[] a, double[] b)
        throws MathIllegalArgumentException {
        checkEqualLength(a, b);

        final double[] result = a.clone();
        for (int i = 0; i < a.length; i++) {
            result[i] += b[i];
        }
        return result;
    }
    /**
     * Creates an array whose contents will be the element-by-element
     * subtraction of the second argument from the first.
     *
     * @param a First term.
     * @param b Element to be subtracted.
     * @return a new array {@code r} where {@code r[i] = a[i] - b[i]}.
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static double[] ebeSubtract(double[] a, double[] b)
        throws MathIllegalArgumentException {
        checkEqualLength(a, b);

        final double[] result = a.clone();
        for (int i = 0; i < a.length; i++) {
            result[i] -= b[i];
        }
        return result;
    }
    /**
     * Creates an array whose contents will be the element-by-element
     * multiplication of the arguments.
     *
     * @param a First factor of the multiplication.
     * @param b Second factor of the multiplication.
     * @return a new array {@code r} where {@code r[i] = a[i] * b[i]}.
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static double[] ebeMultiply(double[] a, double[] b)
        throws MathIllegalArgumentException {
        checkEqualLength(a, b);

        final double[] result = a.clone();
        for (int i = 0; i < a.length; i++) {
            result[i] *= b[i];
        }
        return result;
    }
    /**
     * Creates an array whose contents will be the element-by-element
     * division of the first argument by the second.
     *
     * @param a Numerator of the division.
     * @param b Denominator of the division.
     * @return a new array {@code r} where {@code r[i] = a[i] / b[i]}.
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static double[] ebeDivide(double[] a, double[] b)
        throws MathIllegalArgumentException {
        checkEqualLength(a, b);

        final double[] result = a.clone();
        for (int i = 0; i < a.length; i++) {
            result[i] /= b[i];
        }
        return result;
    }

    /**
     * Calculates the L<sub>1</sub> (sum of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>1</sub> distance between the two points
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static double distance1(double[] p1, double[] p2)
        throws MathIllegalArgumentException {
        checkEqualLength(p1, p2);
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += FastMath.abs(p1[i] - p2[i]);
        }
        return sum;
    }

    /**
     * Calculates the L<sub>1</sub> (sum of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>1</sub> distance between the two points
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static int distance1(int[] p1, int[] p2)
        throws MathIllegalArgumentException {
        checkEqualLength(p1, p2);
        int sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += FastMath.abs(p1[i] - p2[i]);
        }
        return sum;
    }

    /**
     * Calculates the L<sub>2</sub> (Euclidean) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>2</sub> distance between the two points
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static double distance(double[] p1, double[] p2)
        throws MathIllegalArgumentException {
        checkEqualLength(p1, p2);
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            final double dp = p1[i] - p2[i];
            sum += dp * dp;
        }
        return FastMath.sqrt(sum);
    }

    /**
     * Calculates the cosine of the angle between two vectors.
     *
     * @param v1 Cartesian coordinates of the first vector.
     * @param v2 Cartesian coordinates of the second vector.
     * @return the cosine of the angle between the vectors.
     */
    public static double cosAngle(double[] v1, double[] v2) {
        return linearCombination(v1, v2) / (safeNorm(v1) * safeNorm(v2));
    }

    /**
     * Calculates the L<sub>2</sub> (Euclidean) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>2</sub> distance between the two points
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static double distance(int[] p1, int[] p2)
        throws MathIllegalArgumentException {
        checkEqualLength(p1, p2);
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            final double dp = p1[i] - p2[i];
            sum += dp * dp;
        }
        return FastMath.sqrt(sum);
    }

    /**
     * Calculates the L<sub>&infin;</sub> (max of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>&infin;</sub> distance between the two points
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static double distanceInf(double[] p1, double[] p2)
        throws MathIllegalArgumentException {
        checkEqualLength(p1, p2);
        double max = 0;
        for (int i = 0; i < p1.length; i++) {
            max = FastMath.max(max, FastMath.abs(p1[i] - p2[i]));
        }
        return max;
    }

    /**
     * Calculates the L<sub>&infin;</sub> (max of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>&infin;</sub> distance between the two points
     * @throws MathIllegalArgumentException if the array lengths differ.
     */
    public static int distanceInf(int[] p1, int[] p2)
        throws MathIllegalArgumentException {
        checkEqualLength(p1, p2);
        int max = 0;
        for (int i = 0; i < p1.length; i++) {
            max = FastMath.max(max, FastMath.abs(p1[i] - p2[i]));
        }
        return max;
    }

    /**
     * Specification of ordering direction.
     */
    public enum OrderDirection {
        /** Constant for increasing direction. */
        INCREASING,
        /** Constant for decreasing direction. */
        DECREASING
    }

    /**
     * Check that an array is monotonically increasing or decreasing.
     *
     * @param <T> the type of the elements in the specified array
     * @param val Values.
     * @param dir Ordering direction.
     * @param strict Whether the order should be strict.
     * @return {@code true} if sorted, {@code false} otherwise.
     */
    public static <T extends Comparable<? super T>> boolean isMonotonic(T[] val,
                                                                        OrderDirection dir,
                                                                        boolean strict) {
        T previous = val[0];
        final int max = val.length;
        for (int i = 1; i < max; i++) {
            final int comp;
            switch (dir) {
            case INCREASING:
                comp = previous.compareTo(val[i]);
                if (strict) {
                    if (comp >= 0) {
                        return false;
                    }
                } else {
                    if (comp > 0) {
                        return false;
                    }
                }
                break;
            case DECREASING:
                comp = val[i].compareTo(previous);
                if (strict) {
                    if (comp >= 0) {
                        return false;
                    }
                } else {
                    if (comp > 0) {
                       return false;
                    }
                }
                break;
            default:
                // Should never happen.
                throw MathRuntimeException.createInternalError();
            }

            previous = val[i];
        }
        return true;
    }

    /**
     * Check that an array is monotonically increasing or decreasing.
     *
     * @param val Values.
     * @param dir Ordering direction.
     * @param strict Whether the order should be strict.
     * @return {@code true} if sorted, {@code false} otherwise.
     */
    public static boolean isMonotonic(double[] val, OrderDirection dir, boolean strict) {
        return checkOrder(val, dir, strict, false);
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @param abort Whether to throw an exception if the check fails.
     * @return {@code true} if the arrays have the same length.
     * @throws MathIllegalArgumentException if the lengths differ and
     * {@code abort} is {@code true}.
     */
    public static boolean checkEqualLength(double[] a,
                                           double[] b,
                                           boolean abort) {
        if (a.length == b.length) {
            return true;
        } else {
            if (abort) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                       a.length, b.length);
            }
            return false;
        }
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @throws MathIllegalArgumentException if the lengths differ.
     */
    public static void checkEqualLength(double[] a,
                                        double[] b) {
        checkEqualLength(a, b, true);
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @param abort Whether to throw an exception if the check fails.
     * @return {@code true} if the arrays have the same length.
     * @throws MathIllegalArgumentException if the lengths differ and
     * {@code abort} is {@code true}.
     * @param <T> the type of the field elements
     * @since 1.5
     */
    public static <T extends CalculusFieldElement<T>> boolean checkEqualLength(final T[] a,
                                                                           final T[] b,
                                           boolean abort) {
        if (a.length == b.length) {
            return true;
        } else {
            if (abort) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                       a.length, b.length);
            }
            return false;
        }
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @throws MathIllegalArgumentException if the lengths differ.
     * @param <T> the type of the field elements
     * @since 1.5
     */
    public static <T extends CalculusFieldElement<T>> void checkEqualLength(final T[] a, final T[] b) {
        checkEqualLength(a, b, true);
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @param abort Whether to throw an exception if the check fails.
     * @return {@code true} if the arrays have the same length.
     * @throws MathIllegalArgumentException if the lengths differ and
     * {@code abort} is {@code true}.
     */
    public static boolean checkEqualLength(int[] a,
                                           int[] b,
                                           boolean abort) {
        if (a.length == b.length) {
            return true;
        } else {
            if (abort) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                       a.length, b.length);
            }
            return false;
        }
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @throws MathIllegalArgumentException if the lengths differ.
     */
    public static void checkEqualLength(int[] a,
                                        int[] b) {
        checkEqualLength(a, b, true);
    }

    /**
     * Check that the given array is sorted.
     *
     * @param val Values.
     * @param dir Ordering direction.
     * @param strict Whether the order should be strict.
     * @param abort Whether to throw an exception if the check fails.
     * @return {@code true} if the array is sorted.
     * @throws MathIllegalArgumentException if the array is not sorted
     * and {@code abort} is {@code true}.
     */
    public static boolean checkOrder(double[] val, OrderDirection dir,
                                     boolean strict, boolean abort)
        throws MathIllegalArgumentException {
        double previous = val[0];
        final int max = val.length;

        int index;
        ITEM:
        for (index = 1; index < max; index++) {
            switch (dir) {
            case INCREASING:
                if (strict) {
                    if (val[index] <= previous) {
                        break ITEM;
                    }
                } else {
                    if (val[index] < previous) {
                        break ITEM;
                    }
                }
                break;
            case DECREASING:
                if (strict) {
                    if (val[index] >= previous) {
                        break ITEM;
                    }
                } else {
                    if (val[index] > previous) {
                        break ITEM;
                    }
                }
                break;
            default:
                // Should never happen.
                throw MathRuntimeException.createInternalError();
            }

            previous = val[index];
        }

        if (index == max) {
            // Loop completed.
            return true;
        }

        // Loop early exit means wrong ordering.
        if (abort) {
            throw new MathIllegalArgumentException(dir == MathArrays.OrderDirection.INCREASING ?
                                                    (strict ?
                                                     LocalizedCoreFormats.NOT_STRICTLY_INCREASING_SEQUENCE :
                                                     LocalizedCoreFormats.NOT_INCREASING_SEQUENCE) :
                                                    (strict ?
                                                     LocalizedCoreFormats.NOT_STRICTLY_DECREASING_SEQUENCE :
                                                     LocalizedCoreFormats.NOT_DECREASING_SEQUENCE),
                                                    val[index], previous, index, index - 1);
        } else {
            return false;
        }
    }

    /**
     * Check that the given array is sorted.
     *
     * @param val Values.
     * @param dir Ordering direction.
     * @param strict Whether the order should be strict.
     * @throws MathIllegalArgumentException if the array is not sorted.
     */
    public static void checkOrder(double[] val, OrderDirection dir,
                                  boolean strict) throws MathIllegalArgumentException {
        checkOrder(val, dir, strict, true);
    }

    /**
     * Check that the given array is sorted in strictly increasing order.
     *
     * @param val Values.
     * @throws MathIllegalArgumentException if the array is not sorted.
     */
    public static void checkOrder(double[] val) throws MathIllegalArgumentException {
        checkOrder(val, OrderDirection.INCREASING, true);
    }

    /**
     * Check that the given array is sorted.
     *
     * @param val Values.
     * @param dir Ordering direction.
     * @param strict Whether the order should be strict.
     * @param abort Whether to throw an exception if the check fails.
     * @return {@code true} if the array is sorted.
     * @throws MathIllegalArgumentException if the array is not sorted
     * and {@code abort} is {@code true}.
     * @param <T> the type of the field elements
     * @since 1.5
     */
    public static <T extends CalculusFieldElement<T>>boolean checkOrder(T[] val, OrderDirection dir,
                                                                    boolean strict, boolean abort)
        throws MathIllegalArgumentException {
        double previous = val[0].getReal();
        final int max = val.length;

        int index;
        ITEM:
        for (index = 1; index < max; index++) {
            switch (dir) {
            case INCREASING:
                if (strict) {
                    if (val[index].getReal() <= previous) {
                        break ITEM;
                    }
                } else {
                    if (val[index].getReal() < previous) {
                        break ITEM;
                    }
                }
                break;
            case DECREASING:
                if (strict) {
                    if (val[index].getReal() >= previous) {
                        break ITEM;
                    }
                } else {
                    if (val[index].getReal() > previous) {
                        break ITEM;
                    }
                }
                break;
            default:
                // Should never happen.
                throw MathRuntimeException.createInternalError();
            }

            previous = val[index].getReal();
        }

        if (index == max) {
            // Loop completed.
            return true;
        }

        // Loop early exit means wrong ordering.
        if (abort) {
            throw new MathIllegalArgumentException(dir == MathArrays.OrderDirection.INCREASING ?
                                                    (strict ?
                                                     LocalizedCoreFormats.NOT_STRICTLY_INCREASING_SEQUENCE :
                                                     LocalizedCoreFormats.NOT_INCREASING_SEQUENCE) :
                                                    (strict ?
                                                     LocalizedCoreFormats.NOT_STRICTLY_DECREASING_SEQUENCE :
                                                     LocalizedCoreFormats.NOT_DECREASING_SEQUENCE),
                                                    val[index], previous, index, index - 1);
        } else {
            return false;
        }
    }

    /**
     * Check that the given array is sorted.
     *
     * @param val Values.
     * @param dir Ordering direction.
     * @param strict Whether the order should be strict.
     * @throws MathIllegalArgumentException if the array is not sorted.
     * @param <T> the type of the field elements
     * @since 1.5
     */
    public static <T extends CalculusFieldElement<T>> void checkOrder(T[] val, OrderDirection dir,
                                                                  boolean strict) throws MathIllegalArgumentException {
        checkOrder(val, dir, strict, true);
    }

    /**
     * Check that the given array is sorted in strictly increasing order.
     *
     * @param val Values.
     * @throws MathIllegalArgumentException if the array is not sorted.
     * @param <T> the type of the field elements
     * @since 1.5
     */
    public static <T extends CalculusFieldElement<T>> void checkOrder(T[] val) throws MathIllegalArgumentException {
        checkOrder(val, OrderDirection.INCREASING, true);
    }

    /**
     * Throws MathIllegalArgumentException if the input array is not rectangular.
     *
     * @param in array to be tested
     * @throws NullArgumentException if input array is null
     * @throws MathIllegalArgumentException if input array is not rectangular
     */
    public static void checkRectangular(final long[][] in)
        throws MathIllegalArgumentException, NullArgumentException {
        MathUtils.checkNotNull(in);
        for (int i = 1; i < in.length; i++) {
            if (in[i].length != in[0].length) {
                throw new MathIllegalArgumentException(
                        LocalizedCoreFormats.DIFFERENT_ROWS_LENGTHS,
                        in[i].length, in[0].length);
            }
        }
    }

    /**
     * Check that all entries of the input array are strictly positive.
     *
     * @param in Array to be tested
     * @throws MathIllegalArgumentException if any entries of the array are not
     * strictly positive.
     */
    public static void checkPositive(final double[] in)
        throws MathIllegalArgumentException {
        for (double v : in) {
            if (v <= 0) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NUMBER_TOO_SMALL_BOUND_EXCLUDED,
                        v, 0);
            }
        }
    }

    /**
     * Check that no entry of the input array is {@code NaN}.
     *
     * @param in Array to be tested.
     * @throws MathIllegalArgumentException if an entry is {@code NaN}.
     */
    public static void checkNotNaN(final double[] in)
        throws MathIllegalArgumentException {
        for(int i = 0; i < in.length; i++) {
            if (Double.isNaN(in[i])) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NAN_ELEMENT_AT_INDEX, i);
            }
        }
    }

    /**
     * Check that all entries of the input array are &gt;= 0.
     *
     * @param in Array to be tested
     * @throws MathIllegalArgumentException if any array entries are less than 0.
     */
    public static void checkNonNegative(final long[] in)
        throws MathIllegalArgumentException {
        for (long l : in) {
            if (l < 0) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NUMBER_TOO_SMALL, l, 0);
            }
        }
    }

    /**
     * Check all entries of the input array are &gt;= 0.
     *
     * @param in Array to be tested
     * @throws MathIllegalArgumentException if any array entries are less than 0.
     */
    public static void checkNonNegative(final long[][] in)
        throws MathIllegalArgumentException {
        for (long[] longs : in) {
            for (int j = 0; j < longs.length; j++) {
                if (longs[j] < 0) {
                    throw new MathIllegalArgumentException(LocalizedCoreFormats.NUMBER_TOO_SMALL, longs[j], 0);
                }
            }
        }
    }

    /**
     * Returns the Cartesian norm (2-norm), handling both overflow and underflow.
     * Translation of the minpack enorm subroutine.
     *
     * <p>
     * The redistribution policy for MINPACK is available
     * <a href="http://www.netlib.org/minpack/disclaimer">here</a>, for
     * convenience, it is reproduced below.
     * </p>
     *
     * <blockquote>
     * <p>
     *    Minpack Copyright Notice (1999) University of Chicago.
     *    All rights reserved
     * </p>
     * <p>
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions
     * are met:
     * </p>
     * <ol>
     *  <li>Redistributions of source code must retain the above copyright
     *      notice, this list of conditions and the following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above
     *     copyright notice, this list of conditions and the following
     *     disclaimer in the documentation and/or other materials provided
     *     with the distribution.</li>
     * <li>The end-user documentation included with the redistribution, if any,
     *     must include the following acknowledgment:
     *     {@code This product includes software developed by the University of
     *           Chicago, as Operator of Argonne National Laboratory.}
     *     Alternately, this acknowledgment may appear in the software itself,
     *     if and wherever such third-party acknowledgments normally appear.</li>
     * <li><strong>WARRANTY DISCLAIMER. THE SOFTWARE IS SUPPLIED "AS IS"
     *     WITHOUT WARRANTY OF ANY KIND. THE COPYRIGHT HOLDER, THE
     *     UNITED STATES, THE UNITED STATES DEPARTMENT OF ENERGY, AND
     *     THEIR EMPLOYEES: (1) DISCLAIM ANY WARRANTIES, EXPRESS OR
     *     IMPLIED, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES
     *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE
     *     OR NON-INFRINGEMENT, (2) DO NOT ASSUME ANY LEGAL LIABILITY
     *     OR RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR
     *     USEFULNESS OF THE SOFTWARE, (3) DO NOT REPRESENT THAT USE OF
     *     THE SOFTWARE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS, (4)
     *     DO NOT WARRANT THAT THE SOFTWARE WILL FUNCTION
     *     UNINTERRUPTED, THAT IT IS ERROR-FREE OR THAT ANY ERRORS WILL
     *     BE CORRECTED.</strong></li>
     * <li><strong>LIMITATION OF LIABILITY. IN NO EVENT WILL THE COPYRIGHT
     *     HOLDER, THE UNITED STATES, THE UNITED STATES DEPARTMENT OF
     *     ENERGY, OR THEIR EMPLOYEES: BE LIABLE FOR ANY INDIRECT,
     *     INCIDENTAL, CONSEQUENTIAL, SPECIAL OR PUNITIVE DAMAGES OF
     *     ANY KIND OR NATURE, INCLUDING BUT NOT LIMITED TO LOSS OF
     *     PROFITS OR LOSS OF DATA, FOR ANY REASON WHATSOEVER, WHETHER
     *     SUCH LIABILITY IS ASSERTED ON THE BASIS OF CONTRACT, TORT
     *     (INCLUDING NEGLIGENCE OR STRICT LIABILITY), OR OTHERWISE,
     *     EVEN IF ANY OF SAID PARTIES HAS BEEN WARNED OF THE
     *     POSSIBILITY OF SUCH LOSS OR DAMAGES.</strong></li>
     * </ol>
     * </blockquote>
     *
     * @param v Vector of doubles.
     * @return the 2-norm of the vector.
     */
    public static double safeNorm(double[] v) {
        double rdwarf = 3.834e-20;
        double rgiant = 1.304e+19;
        double s1 = 0;
        double s2 = 0;
        double s3 = 0;
        double x1max = 0;
        double x3max = 0;
        double floatn = v.length;
        double agiant = rgiant / floatn;
        for (double value : v) {
            double xabs = FastMath.abs(value);
            if (xabs < rdwarf || xabs > agiant) {
                if (xabs > rdwarf) {
                    if (xabs > x1max) {
                        double r = x1max / xabs;
                        s1 = 1 + s1 * r * r;
                        x1max = xabs;
                    } else {
                        double r = xabs / x1max;
                        s1 += r * r;
                    }
                } else {
                    if (xabs > x3max) {
                        double r = x3max / xabs;
                        s3 = 1 + s3 * r * r;
                        x3max = xabs;
                    } else {
                        if (xabs != 0) {
                            double r = xabs / x3max;
                            s3 += r * r;
                        }
                    }
                }
            } else {
                s2 += xabs * xabs;
            }
        }
        double norm;
        if (s1 != 0) {
            norm = x1max * Math.sqrt(s1 + (s2 / x1max) / x1max);
        } else {
            if (s2 == 0) {
                norm = x3max * Math.sqrt(s3);
            } else {
                if (s2 >= x3max) {
                    norm = Math.sqrt(s2 * (1 + (x3max / s2) * (x3max * s3)));
                } else {
                    norm = Math.sqrt(x3max * ((s2 / x3max) + (x3max * s3)));
                }
            }
        }
        return norm;
    }

    /**
     * Sort an array in ascending order in place and perform the same reordering
     * of entries on other arrays. For example, if
     * {@code x = [3, 1, 2], y = [1, 2, 3]} and {@code z = [0, 5, 7]}, then
     * {@code sortInPlace(x, y, z)} will update {@code x} to {@code [1, 2, 3]},
     * {@code y} to {@code [2, 3, 1]} and {@code z} to {@code [5, 7, 0]}.
     *
     * @param x Array to be sorted and used as a pattern for permutation
     * of the other arrays.
     * @param yList Set of arrays whose permutations of entries will follow
     * those performed on {@code x}.
     * @throws MathIllegalArgumentException if any {@code y} is not the same
     * size as {@code x}.
     * @throws NullArgumentException if {@code x} or any {@code y} is null.
     */
    public static void sortInPlace(double[] x, double[]... yList)
        throws MathIllegalArgumentException, NullArgumentException {
        sortInPlace(x, OrderDirection.INCREASING, yList);
    }

    /**
     * Helper data structure holding a (double, integer) pair.
     */
    private static class PairDoubleInteger {
        /** Key */
        private final double key;
        /** Value */
        private final int value;

        /**
         * @param key Key.
         * @param value Value.
         */
        PairDoubleInteger(double key, int value) {
            this.key = key;
            this.value = value;
        }

        /** @return the key. */
        public double getKey() {
            return key;
        }

        /** @return the value. */
        public int getValue() {
            return value;
        }
    }

    /**
     * Sort an array in place and perform the same reordering of entries on
     * other arrays.  This method works the same as the other
     * {@link #sortInPlace(double[], double[][]) sortInPlace} method, but
     * allows the order of the sort to be provided in the {@code dir}
     * parameter.
     *
     * @param x Array to be sorted and used as a pattern for permutation
     * of the other arrays.
     * @param dir Order direction.
     * @param yList Set of arrays whose permutations of entries will follow
     * those performed on {@code x}.
     * @throws MathIllegalArgumentException if any {@code y} is not the same
     * size as {@code x}.
     * @throws NullArgumentException if {@code x} or any {@code y} is null
     */
    public static void sortInPlace(double[] x,
                                   final OrderDirection dir,
                                   double[]... yList)
        throws MathIllegalArgumentException, NullArgumentException {

        // Consistency checks.
        if (x == null) {
            throw new NullArgumentException();
        }

        final int len = x.length;

        for (final double[] y : yList) {
            if (y == null) {
                throw new NullArgumentException();
            }
            if (y.length != len) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                        y.length, len);
            }
        }

        // Associate each abscissa "x[i]" with its index "i".
        final List<PairDoubleInteger> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            list.add(new PairDoubleInteger(x[i], i));
        }

        // Create comparators for increasing and decreasing orders.
        final Comparator<PairDoubleInteger> comp =
            dir == MathArrays.OrderDirection.INCREASING ?
            new Comparator<PairDoubleInteger>() {
                /** {@inheritDoc} */
                @Override
                public int compare(PairDoubleInteger o1,
                                   PairDoubleInteger o2) {
                    return Double.compare(o1.getKey(), o2.getKey());
                }
            } :
            new Comparator<PairDoubleInteger>() {
                /** {@inheritDoc} */
                @Override
                public int compare(PairDoubleInteger o1,
                                   PairDoubleInteger o2) {
                    return Double.compare(o2.getKey(), o1.getKey());
                }
            };

        // Sort.
        list.sort(comp);

        // Modify the original array so that its elements are in
        // the prescribed order.
        // Retrieve indices of original locations.
        final int[] indices = new int[len];
        for (int i = 0; i < len; i++) {
            final PairDoubleInteger e = list.get(i);
            x[i] = e.getKey();
            indices[i] = e.getValue();
        }

        // In each of the associated arrays, move the
        // elements to their new location.
        for (final double[] yInPlace : yList) {
            // Input array will be modified in place.
            final double[] yOrig = yInPlace.clone();

            for (int i = 0; i < len; i++) {
                yInPlace[i] = yOrig[indices[i]];
            }
        }
    }

    /**
     * Compute a linear combination accurately.
     * This method computes the sum of the products
     * <code>a<sub>i</sub> b<sub>i</sub></code> to high accuracy.
     * It does so by using specific multiplication and addition algorithms to
     * preserve accuracy and reduce cancellation effects.
     * <br>
     * It is based on the 2005 paper
     * <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
     * Accurate Sum and Dot Product</a> by Takeshi Ogita, Siegfried M. Rump,
     * and Shin'ichi Oishi published in SIAM J. Sci. Comput.
     *
     * @param a Factors.
     * @param b Factors.
     * @return <code>&Sigma;<sub>i</sub> a<sub>i</sub> b<sub>i</sub></code>.
     * @throws MathIllegalArgumentException if arrays dimensions don't match
     */
    public static double linearCombination(final double[] a, final double[] b)
        throws MathIllegalArgumentException {
        checkEqualLength(a, b);
        final int len = a.length;

        if (len == 1) {
            // Revert to scalar multiplication.
            return a[0] * b[0];
        }

        final double[] prodHigh = new double[len];
        double prodLowSum = 0;

        for (int i = 0; i < len; i++) {
            final double ai    = a[i];
            final double aHigh = Double.longBitsToDouble(Double.doubleToRawLongBits(ai) & ((-1L) << 27));
            final double aLow  = ai - aHigh;

            final double bi    = b[i];
            final double bHigh = Double.longBitsToDouble(Double.doubleToRawLongBits(bi) & ((-1L) << 27));
            final double bLow  = bi - bHigh;
            prodHigh[i] = ai * bi;
            final double prodLow = aLow * bLow - (((prodHigh[i] -
                                                    aHigh * bHigh) -
                                                   aLow * bHigh) -
                                                  aHigh * bLow);
            prodLowSum += prodLow;
        }


        final double prodHighCur = prodHigh[0];
        double prodHighNext = prodHigh[1];
        double sHighPrev = prodHighCur + prodHighNext;
        double sPrime = sHighPrev - prodHighNext;
        double sLowSum = (prodHighNext - (sHighPrev - sPrime)) + (prodHighCur - sPrime);

        final int lenMinusOne = len - 1;
        for (int i = 1; i < lenMinusOne; i++) {
            prodHighNext = prodHigh[i + 1];
            final double sHighCur = sHighPrev + prodHighNext;
            sPrime = sHighCur - prodHighNext;
            sLowSum += (prodHighNext - (sHighCur - sPrime)) + (sHighPrev - sPrime);
            sHighPrev = sHighCur;
        }

        double result = sHighPrev + (prodLowSum + sLowSum);

        if (Double.isNaN(result) || result == 0.0) {
            // either we have split infinite numbers or some coefficients were NaNs or signed zeros,
            // just rely on the naive implementation and let IEEE754 handle this
            // we do this for zeros too as we want to preserve the sign of zero (see issue #76)
            result = a[0] * b[0];
            for (int i = 1; i < len; ++i) {
                result += a[i] * b[i];
            }
        }

        return result;
    }

    /**
     * Compute a linear combination accurately.
     * <p>
     * This method computes a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub> to high accuracy. It does
     * so by using specific multiplication and addition algorithms to
     * preserve accuracy and reduce cancellation effects. It is based
     * on the 2005 paper <a
     * href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
     * Accurate Sum and Dot Product</a> by Takeshi Ogita,
     * Siegfried M. Rump, and Shin'ichi Oishi published in SIAM J. Sci. Comput.
     * </p>
     * @param a1 first factor of the first term
     * @param b1 second factor of the first term
     * @param a2 first factor of the second term
     * @param b2 second factor of the second term
     * @return a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub>
     * @see #linearCombination(double, double, double, double, double, double)
     * @see #linearCombination(double, double, double, double, double, double, double, double)
     */
    public static double linearCombination(final double a1, final double b1,
                                           final double a2, final double b2) {

        // the code below is split in many additions/subtractions that may
        // appear redundant. However, they should NOT be simplified, as they
        // use IEEE754 floating point arithmetic rounding properties.
        // The variable naming conventions are that xyzHigh contains the most significant
        // bits of xyz and xyzLow contains its least significant bits. So theoretically
        // xyz is the sum xyzHigh + xyzLow, but in many cases below, this sum cannot
        // be represented in only one double precision number so we preserve two numbers
        // to hold it as long as we can, combining the high and low order bits together
        // only at the end, after cancellation may have occurred on high order bits

        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & ((-1L) << 27));
        final double a1Low      = a1 - a1High;
        final double b1High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & ((-1L) << 27));
        final double b1Low      = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High  = a1 * b1;
        final double prod1Low   = a1Low * b1Low - (((prod1High - a1High * b1High) - a1Low * b1High) - a1High * b1Low);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & ((-1L) << 27));
        final double a2Low      = a2 - a2High;
        final double b2High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & ((-1L) << 27));
        final double b2Low      = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High  = a2 * b2;
        final double prod2Low   = a2Low * b2Low - (((prod2High - a2High * b2High) - a2Low * b2High) - a2High * b2Low);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High    = prod1High + prod2High;
        final double s12Prime   = s12High - prod2High;
        final double s12Low     = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // final rounding, s12 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s12High + (prod1Low + prod2Low + s12Low);

        if (Double.isNaN(result) || result == 0.0) {
            // either we have split infinite numbers or some coefficients were NaNs or signed zeros,
            // just rely on the naive implementation and let IEEE754 handle this
            // we do this for zeros too as we want to preserve the sign of zero (see issue #76)
            result = a1 * b1 + a2 * b2;
        }

        return result;
    }

    /**
     * Compute a linear combination accurately.
     * <p>
     * This method computes a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub> + a<sub>3</sub>&times;b<sub>3</sub>
     * to high accuracy. It does so by using specific multiplication and
     * addition algorithms to preserve accuracy and reduce cancellation effects.
     * It is based on the 2005 paper <a
     * href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
     * Accurate Sum and Dot Product</a> by Takeshi Ogita,
     * Siegfried M. Rump, and Shin'ichi Oishi published in SIAM J. Sci. Comput.
     * </p>
     * @param a1 first factor of the first term
     * @param b1 second factor of the first term
     * @param a2 first factor of the second term
     * @param b2 second factor of the second term
     * @param a3 first factor of the third term
     * @param b3 second factor of the third term
     * @return a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub> + a<sub>3</sub>&times;b<sub>3</sub>
     * @see #linearCombination(double, double, double, double)
     * @see #linearCombination(double, double, double, double, double, double, double, double)
     */
    public static double linearCombination(final double a1, final double b1,
                                           final double a2, final double b2,
                                           final double a3, final double b3) {

        // the code below is split in many additions/subtractions that may
        // appear redundant. However, they should NOT be simplified, as they
        // do use IEEE754 floating point arithmetic rounding properties.
        // The variables naming conventions are that xyzHigh contains the most significant
        // bits of xyz and xyzLow contains its least significant bits. So theoretically
        // xyz is the sum xyzHigh + xyzLow, but in many cases below, this sum cannot
        // be represented in only one double precision number so we preserve two numbers
        // to hold it as long as we can, combining the high and low order bits together
        // only at the end, after cancellation may have occurred on high order bits

        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & ((-1L) << 27));
        final double a1Low      = a1 - a1High;
        final double b1High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & ((-1L) << 27));
        final double b1Low      = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High  = a1 * b1;
        final double prod1Low   = a1Low * b1Low - (((prod1High - a1High * b1High) - a1Low * b1High) - a1High * b1Low);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & ((-1L) << 27));
        final double a2Low      = a2 - a2High;
        final double b2High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & ((-1L) << 27));
        final double b2Low      = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High  = a2 * b2;
        final double prod2Low   = a2Low * b2Low - (((prod2High - a2High * b2High) - a2Low * b2High) - a2High * b2Low);

        // split a3 and b3 as one 26 bits number and one 27 bits number
        final double a3High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a3) & ((-1L) << 27));
        final double a3Low      = a3 - a3High;
        final double b3High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b3) & ((-1L) << 27));
        final double b3Low      = b3 - b3High;

        // accurate multiplication a3 * b3
        final double prod3High  = a3 * b3;
        final double prod3Low   = a3Low * b3Low - (((prod3High - a3High * b3High) - a3Low * b3High) - a3High * b3Low);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High    = prod1High + prod2High;
        final double s12Prime   = s12High - prod2High;
        final double s12Low     = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3
        final double s123High   = s12High + prod3High;
        final double s123Prime  = s123High - prod3High;
        final double s123Low    = (prod3High - (s123High - s123Prime)) + (s12High - s123Prime);

        // final rounding, s123 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s123High + (prod1Low + prod2Low + prod3Low + s12Low + s123Low);

        if (Double.isNaN(result) || result == 0.0) {
            // either we have split infinite numbers or some coefficients were NaNs or signed zeros,
            // just rely on the naive implementation and let IEEE754 handle this
            // we do this for zeros too as we want to preserve the sign of zero (see issue #76)
            result = a1 * b1 + a2 * b2 + a3 * b3;
        }

        return result;
    }

    /**
     * Compute a linear combination accurately.
     * <p>
     * This method computes a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub> + a<sub>3</sub>&times;b<sub>3</sub> +
     * a<sub>4</sub>&times;b<sub>4</sub>
     * to high accuracy. It does so by using specific multiplication and
     * addition algorithms to preserve accuracy and reduce cancellation effects.
     * It is based on the 2005 paper <a
     * href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.2.1547">
     * Accurate Sum and Dot Product</a> by Takeshi Ogita,
     * Siegfried M. Rump, and Shin'ichi Oishi published in SIAM J. Sci. Comput.
     * </p>
     * @param a1 first factor of the first term
     * @param b1 second factor of the first term
     * @param a2 first factor of the second term
     * @param b2 second factor of the second term
     * @param a3 first factor of the third term
     * @param b3 second factor of the third term
     * @param a4 first factor of the third term
     * @param b4 second factor of the third term
     * @return a<sub>1</sub>&times;b<sub>1</sub> +
     * a<sub>2</sub>&times;b<sub>2</sub> + a<sub>3</sub>&times;b<sub>3</sub> +
     * a<sub>4</sub>&times;b<sub>4</sub>
     * @see #linearCombination(double, double, double, double)
     * @see #linearCombination(double, double, double, double, double, double)
     */
    public static double linearCombination(final double a1, final double b1,
                                           final double a2, final double b2,
                                           final double a3, final double b3,
                                           final double a4, final double b4) {

        // the code below is split in many additions/subtractions that may
        // appear redundant. However, they should NOT be simplified, as they
        // do use IEEE754 floating point arithmetic rounding properties.
        // The variables naming conventions are that xyzHigh contains the most significant
        // bits of xyz and xyzLow contains its least significant bits. So theoretically
        // xyz is the sum xyzHigh + xyzLow, but in many cases below, this sum cannot
        // be represented in only one double precision number so we preserve two numbers
        // to hold it as long as we can, combining the high and low order bits together
        // only at the end, after cancellation may have occurred on high order bits

        // split a1 and b1 as one 26 bits number and one 27 bits number
        final double a1High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a1) & ((-1L) << 27));
        final double a1Low      = a1 - a1High;
        final double b1High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b1) & ((-1L) << 27));
        final double b1Low      = b1 - b1High;

        // accurate multiplication a1 * b1
        final double prod1High  = a1 * b1;
        final double prod1Low   = a1Low * b1Low - (((prod1High - a1High * b1High) - a1Low * b1High) - a1High * b1Low);

        // split a2 and b2 as one 26 bits number and one 27 bits number
        final double a2High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a2) & ((-1L) << 27));
        final double a2Low      = a2 - a2High;
        final double b2High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b2) & ((-1L) << 27));
        final double b2Low      = b2 - b2High;

        // accurate multiplication a2 * b2
        final double prod2High  = a2 * b2;
        final double prod2Low   = a2Low * b2Low - (((prod2High - a2High * b2High) - a2Low * b2High) - a2High * b2Low);

        // split a3 and b3 as one 26 bits number and one 27 bits number
        final double a3High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a3) & ((-1L) << 27));
        final double a3Low      = a3 - a3High;
        final double b3High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b3) & ((-1L) << 27));
        final double b3Low      = b3 - b3High;

        // accurate multiplication a3 * b3
        final double prod3High  = a3 * b3;
        final double prod3Low   = a3Low * b3Low - (((prod3High - a3High * b3High) - a3Low * b3High) - a3High * b3Low);

        // split a4 and b4 as one 26 bits number and one 27 bits number
        final double a4High     = Double.longBitsToDouble(Double.doubleToRawLongBits(a4) & ((-1L) << 27));
        final double a4Low      = a4 - a4High;
        final double b4High     = Double.longBitsToDouble(Double.doubleToRawLongBits(b4) & ((-1L) << 27));
        final double b4Low      = b4 - b4High;

        // accurate multiplication a4 * b4
        final double prod4High  = a4 * b4;
        final double prod4Low   = a4Low * b4Low - (((prod4High - a4High * b4High) - a4Low * b4High) - a4High * b4Low);

        // accurate addition a1 * b1 + a2 * b2
        final double s12High    = prod1High + prod2High;
        final double s12Prime   = s12High - prod2High;
        final double s12Low     = (prod2High - (s12High - s12Prime)) + (prod1High - s12Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3
        final double s123High   = s12High + prod3High;
        final double s123Prime  = s123High - prod3High;
        final double s123Low    = (prod3High - (s123High - s123Prime)) + (s12High - s123Prime);

        // accurate addition a1 * b1 + a2 * b2 + a3 * b3 + a4 * b4
        final double s1234High  = s123High + prod4High;
        final double s1234Prime = s1234High - prod4High;
        final double s1234Low   = (prod4High - (s1234High - s1234Prime)) + (s123High - s1234Prime);

        // final rounding, s1234 may have suffered many cancellations, we try
        // to recover some bits from the extra words we have saved up to now
        double result = s1234High + (prod1Low + prod2Low + prod3Low + prod4Low + s12Low + s123Low + s1234Low);

        if (Double.isNaN(result) || result == 0.0) {
            // either we have split infinite numbers or some coefficients were NaNs or signed zeros,
            // just rely on the naive implementation and let IEEE754 handle this
            // we do this for zeros too as we want to preserve the sign of zero (see issue #76)
            result = a1 * b1 + a2 * b2 + a3 * b3 + a4 * b4;
        }

        return result;
    }

    /**
     * Returns true iff both arguments are null or have same dimensions and all
     * their elements are equal as defined by
     * {@link Precision#equals(float,float)}.
     *
     * @param x first array
     * @param y second array
     * @return true if the values are both null or have same dimension
     * and equal elements.
     */
    public static boolean equals(float[] x, float[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!Precision.equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true iff both arguments are null or have same dimensions and all
     * their elements are equal as defined by
     * {@link Precision#equalsIncludingNaN(double,double) this method}.
     *
     * @param x first array
     * @param y second array
     * @return true if the values are both null or have same dimension and
     * equal elements
     */
    public static boolean equalsIncludingNaN(float[] x, float[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!Precision.equalsIncludingNaN(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} iff both arguments are {@code null} or have same
     * dimensions and all their elements are equal as defined by
     * {@link Precision#equals(double,double)}.
     *
     * @param x First array.
     * @param y Second array.
     * @return {@code true} if the values are both {@code null} or have same
     * dimension and equal elements.
     */
    public static boolean equals(double[] x, double[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!Precision.equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} iff both arguments are {@code null} or have same
     * dimensions and all their elements are equal as defined by
     * {@link Object#equals(Object)}.
     *
     * @param <T> type of the field elements
     * @param x First array.
     * @param y Second array.
     * @return {@code true} if the values are both {@code null} or have same
     * dimension and equal elements.
     * @since 4.0
     */
    public static <T extends FieldElement<T>> boolean equals(T[] x, T[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!x[i].equals(y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} iff both arguments are {@code null} or have same
     * dimensions and all their elements are equal as defined by
     * {@link Precision#equalsIncludingNaN(double,double) this method}.
     *
     * @param x First array.
     * @param y Second array.
     * @return {@code true} if the values are both {@code null} or have same
     * dimension and equal elements.
     */
    public static boolean equalsIncludingNaN(double[] x, double[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!Precision.equalsIncludingNaN(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if both arguments are {@code null} or have same
     * dimensions and all their elements are equals.
     *
     * @param x First array.
     * @param y Second array.
     * @return {@code true} if the values are both {@code null} or have same
     * dimension and equal elements.
     */
    public static boolean equals(long[] x, long[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if both arguments are {@code null} or have same
     * dimensions and all their elements are equals.
     *
     * @param x First array.
     * @param y Second array.
     * @return {@code true} if the values are both {@code null} or have same
     * dimension and equal elements.
     */
    public static boolean equals(int[] x, int[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if both arguments are {@code null} or have same
     * dimensions and all their elements are equals.
     *
     * @param x First array.
     * @param y Second array.
     * @return {@code true} if the values are both {@code null} or have same
     * dimension and equal elements.
     */
    public static boolean equals(byte[] x, byte[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if both arguments are {@code null} or have same
     * dimensions and all their elements are equals.
     *
     * @param x First array.
     * @param y Second array.
     * @return {@code true} if the values are both {@code null} or have same
     * dimension and equal elements.
     */
    public static boolean equals(short[] x, short[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Normalizes an array to make it sum to a specified value.
     * Returns the result of the transformation
     * <pre>
     *    x ↦ x * normalizedSum / sum
     * </pre>
     * applied to each non-NaN element x of the input array, where sum is the
     * sum of the non-NaN entries in the input array.
     * <p>
     * Throws IllegalArgumentException if {@code normalizedSum} is infinite
     * or NaN and ArithmeticException if the input array contains any infinite elements
     * or sums to 0.
     * <p>
     * Ignores (i.e., copies unchanged to the output array) NaNs in the input array.
     * The input array is unchanged by this method.
     *
     * @param values Input array to be normalized
     * @param normalizedSum Target sum for the normalized array
     * @return the normalized array
     * @throws MathRuntimeException if the input array contains infinite
     * elements or sums to zero
     * @throws MathIllegalArgumentException if the target sum is infinite or {@code NaN}
     */
    public static double[] normalizeArray(double[] values, double normalizedSum)
        throws MathIllegalArgumentException, MathRuntimeException {
        if (Double.isInfinite(normalizedSum)) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.NORMALIZE_INFINITE);
        }
        if (Double.isNaN(normalizedSum)) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.NORMALIZE_NAN);
        }
        double sum = 0d;
        final int len = values.length;
        double[] out = new double[len];
        for (int i = 0; i < len; i++) {
            if (Double.isInfinite(values[i])) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.INFINITE_ARRAY_ELEMENT, values[i], i);
            }
            if (!Double.isNaN(values[i])) {
                sum += values[i];
            }
        }
        if (sum == 0) {
            throw new MathRuntimeException(LocalizedCoreFormats.ARRAY_SUMS_TO_ZERO);
        }
        for (int i = 0; i < len; i++) {
            if (Double.isNaN(values[i])) {
                out[i] = Double.NaN;
            } else {
                out[i] = values[i] * normalizedSum / sum;
            }
        }
        return out;
    }

    /** Build an array of elements.
     * <p>
     * Arrays are filled with {@code field.getZero()}
     *
     * @param <T> the type of the field elements
     * @param field field to which array elements belong
     * @param length of the array
     * @return a new array
     */
    public static <T extends FieldElement<T>> T[] buildArray(final Field<T> field, final int length) {
        @SuppressWarnings("unchecked") // OK because field must be correct class
        T[] array = (T[]) Array.newInstance(field.getRuntimeClass(), length);
        Arrays.fill(array, field.getZero());
        return array;
    }

    /** Build a double dimension array of elements.
     * <p>
     * Arrays are filled with {@code field.getZero()}
     *
     * @param <T> the type of the field elements
     * @param field field to which array elements belong
     * @param rows number of rows in the array
     * @param columns number of columns (may be negative to build partial
     * arrays in the same way {@code new Field[rows][]} works)
     * @return a new array
     */
    @SuppressWarnings("unchecked")
    public static <T extends FieldElement<T>> T[][] buildArray(final Field<T> field, final int rows, final int columns) {
        final T[][] array;
        if (columns < 0) {
            T[] dummyRow = buildArray(field, 0);
            array = (T[][]) Array.newInstance(dummyRow.getClass(), rows);
        } else {
            array = (T[][]) Array.newInstance(field.getRuntimeClass(), rows, columns);
            for (int i = 0; i < rows; ++i) {
                Arrays.fill(array[i], field.getZero());
            }
        }
        return array;
    }

    /** Build a triple dimension array of elements.
     * <p>
     * Arrays are filled with {@code field.getZero()}
     *
     * @param <T> the type of the field elements
     * @param field field to which array elements belong
     * @param l1 number of elements along first dimension
     * @param l2 number of elements along second dimension
     * @param l3 number of elements along third dimension (may be negative to build partial
     * arrays in the same way {@code new Field[l1][l2][]} works)
     * @return a new array
     * @since 1.4
     */
    @SuppressWarnings("unchecked")
    public static <T extends FieldElement<T>> T[][][] buildArray(final Field<T> field, final int l1, final int l2, final int l3) {
        final T[][][] array;
        if (l3 < 0) {
            T[] dummyRow = buildArray(field, 0);
            array = (T[][][]) Array.newInstance(dummyRow.getClass(), l1, l2);
        } else {
            array = (T[][][]) Array.newInstance(field.getRuntimeClass(), l1, l2, l3);
            for (int i = 0; i < l1; ++i) {
                for (int j = 0; j < l2; ++j) {
                    Arrays.fill(array[i][j], field.getZero());
                }
            }
        }
        return array;
    }

    /**
     * Calculates the <a href="http://en.wikipedia.org/wiki/Convolution">
     * convolution</a> between two sequences.
     * <p>
     * The solution is obtained via straightforward computation of the
     * convolution sum (and not via FFT). Whenever the computation needs
     * an element that would be located at an index outside the input arrays,
     * the value is assumed to be zero.
     *
     * @param x First sequence.
     * Typically, this sequence will represent an input signal to a system.
     * @param h Second sequence.
     * Typically, this sequence will represent the impulse response of the system.
     * @return the convolution of {@code x} and {@code h}.
     * This array's length will be {@code x.length + h.length - 1}.
     * @throws NullArgumentException if either {@code x} or {@code h} is {@code null}.
     * @throws MathIllegalArgumentException if either {@code x} or {@code h} is empty.
     */
    public static double[] convolve(double[] x, double[] h)
        throws MathIllegalArgumentException, NullArgumentException {
        MathUtils.checkNotNull(x);
        MathUtils.checkNotNull(h);

        final int xLen = x.length;
        final int hLen = h.length;

        if (xLen == 0 || hLen == 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.NO_DATA);
        }

        // initialize the output array
        final int totalLength = xLen + hLen - 1;
        final double[] y = new double[totalLength];

        // straightforward implementation of the convolution sum
        for (int n = 0; n < totalLength; n++) {
            double yn = 0;
            int k = FastMath.max(0, n + 1 - xLen);
            int j = n - k;
            while (k < hLen && j >= 0) {
                yn += x[j--] * h[k++];
            }
            y[n] = yn;
        }

        return y;
    }

    /**
     * Specification for indicating that some operation applies
     * before or after a given index.
     */
    public enum Position {
        /** Designates the beginning of the array (near index 0). */
        HEAD,
        /** Designates the end of the array. */
        TAIL
    }

    /**
     * Shuffle the entries of the given array.
     * The {@code start} and {@code pos} parameters select which portion
     * of the array is randomized and which is left untouched.
     *
     * @see #shuffle(int[],int,Position,RandomGenerator)
     *
     * @param list Array whose entries will be shuffled (in-place).
     * @param start Index at which shuffling begins.
     * @param pos Shuffling is performed for index positions between
     * {@code start} and either the end (if {@link Position#TAIL})
     * or the beginning (if {@link Position#HEAD}) of the array.
     */
    public static void shuffle(int[] list,
                               int start,
                               Position pos) {
        shuffle(list, start, pos, new Well19937c());
    }

    /**
     * Shuffle the entries of the given array, using the
     * <a href="https://en.wikipedia.org/wiki/Fisher-Yates_shuffle#The_modern_algorithm">
     * Fisher–Yates</a> algorithm.
     * The {@code start} and {@code pos} parameters select which portion
     * of the array is randomized and which is left untouched.
     *
     * @param list Array whose entries will be shuffled (in-place).
     * @param start Index at which shuffling begins.
     * @param pos Shuffling is performed for index positions between
     * {@code start} and either the end (if {@link Position#TAIL})
     * or the beginning (if {@link Position#HEAD}) of the array.
     * @param rng Random number generator.
     */
    public static void shuffle(int[] list,
                               int start,
                               Position pos,
                               RandomGenerator rng) {
        switch (pos) {
        case TAIL:
            for (int i = list.length - 1; i > start; i--) {
                final int target = start + rng.nextInt(i - start + 1);
                final int temp = list[target];
                list[target] = list[i];
                list[i] = temp;
            }
            break;

        case HEAD:
            for (int i = 0; i < start; i++) {
                final int target = i + rng.nextInt(start - i + 1);
                final int temp = list[target];
                list[target] = list[i];
                list[i] = temp;
            }
            break;

        default:
            throw MathRuntimeException.createInternalError(); // Should never happen.
        }
    }

    /**
     * Shuffle the entries of the given array.
     *
     * @see #shuffle(int[],int,Position,RandomGenerator)
     *
     * @param list Array whose entries will be shuffled (in-place).
     * @param rng Random number generator.
     */
    public static void shuffle(int[] list,
                               RandomGenerator rng) {
        shuffle(list, 0, Position.TAIL, rng);
    }

    /**
     * Shuffle the entries of the given array.
     *
     * @see #shuffle(int[],int,Position,RandomGenerator)
     *
     * @param list Array whose entries will be shuffled (in-place).
     */
    public static void shuffle(int[] list) {
        shuffle(list, new Well19937c());
    }

    /**
     * Returns an array representing the natural number {@code n}.
     *
     * @param n Natural number.
     * @return an array whose entries are the numbers 0, 1, ..., {@code n}-1.
     * If {@code n == 0}, the returned array is empty.
     */
    public static int[] natural(int n) {
        return sequence(n, 0, 1);
    }

    /**
     * Returns an array of {@code size} integers starting at {@code start},
     * skipping {@code stride} numbers.
     *
     * @param size Natural number.
     * @param start Natural number.
     * @param stride Natural number.
     * @return an array whose entries are the numbers
     * {@code start, start + stride, ..., start + (size - 1) * stride}.
     * If {@code size == 0}, the returned array is empty.
     */
    public static int[] sequence(int size,
                                 int start,
                                 int stride) {
        final int[] a = new int[size];
        for (int i = 0; i < size; i++) {
            a[i] = start + i * stride;
        }
        return a;
    }

    /**
     * This method is used
     * to verify that the input parameters designate a subarray of positive length.
     * <ul>
     * <li>returns {@code true} iff the parameters designate a subarray of
     * positive length</li>
     * <li>throws {@code MathIllegalArgumentException} if the array is null or
     * or the indices are invalid</li>
     * <li>returns {@code false} if the array is non-null, but
     * {@code length} is 0.</li>
     * </ul>
     *
     * @param values the input array
     * @param begin index of the first array element to include
     * @param length the number of elements to include
     * @return true if the parameters are valid and designate a subarray of positive length
     * @throws MathIllegalArgumentException if the indices are invalid or the array is null
     */
    public static boolean verifyValues(final double[] values, final int begin, final int length)
            throws MathIllegalArgumentException {
        return verifyValues(values, begin, length, false);
    }

    /**
     * This method is used
     * to verify that the input parameters designate a subarray of positive length.
     * <ul>
     * <li>returns {@code true} iff the parameters designate a subarray of
     * non-negative length</li>
     * <li>throws {@code IllegalArgumentException} if the array is null or
     * or the indices are invalid</li>
     * <li>returns {@code false} if the array is non-null, but
     * {@code length} is 0 unless {@code allowEmpty} is {@code true}</li>
     * </ul>
     *
     * @param values the input array
     * @param begin index of the first array element to include
     * @param length the number of elements to include
     * @param allowEmpty if <code>true</code> then zero length arrays are allowed
     * @return true if the parameters are valid
     * @throws MathIllegalArgumentException if the indices are invalid or the array is null
     */
    public static boolean verifyValues(final double[] values, final int begin,
            final int length, final boolean allowEmpty) throws MathIllegalArgumentException {

        MathUtils.checkNotNull(values, LocalizedCoreFormats.INPUT_ARRAY);

        if (begin < 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.START_POSITION, begin);
        }

        if (length < 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.LENGTH, length);
        }

        if (begin + length > values.length) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.SUBARRAY_ENDS_AFTER_ARRAY_END,
                    begin + length, values.length, true);
        }

        if (length == 0 && !allowEmpty) {
            return false;
        }

        return true;
    }

    /**
     * This method is used
     * to verify that the begin and length parameters designate a subarray of positive length
     * and the weights are all non-negative, non-NaN, finite, and not all zero.
     * <ul>
     * <li>returns {@code true} iff the parameters designate a subarray of
     * positive length and the weights array contains legitimate values.</li>
     * <li>throws {@code IllegalArgumentException} if any of the following are true:
     * <ul><li>the values array is null</li>
     *     <li>the weights array is null</li>
     *     <li>the weights array does not have the same length as the values array</li>
     *     <li>the weights array contains one or more infinite values</li>
     *     <li>the weights array contains one or more NaN values</li>
     *     <li>the weights array contains negative values</li>
     *     <li>the start and length arguments do not determine a valid array</li></ul>
     * </li>
     * <li>returns {@code false} if the array is non-null, but {@code length} is 0.</li>
     * </ul>
     *
     * @param values the input array
     * @param weights the weights array
     * @param begin index of the first array element to include
     * @param length the number of elements to include
     * @return true if the parameters are valid and designate a subarray of positive length
     * @throws MathIllegalArgumentException if the indices are invalid or the array is null
     */
    public static boolean verifyValues(
        final double[] values,
        final double[] weights,
        final int begin,
        final int length) throws MathIllegalArgumentException {
        return verifyValues(values, weights, begin, length, false);
    }

    /**
     * This method is used
     * to verify that the begin and length parameters designate a subarray of positive length
     * and the weights are all non-negative, non-NaN, finite, and not all zero.
     * <ul>
     * <li>returns {@code true} iff the parameters designate a subarray of
     * non-negative length and the weights array contains legitimate values.</li>
     * <li>throws {@code MathIllegalArgumentException} if any of the following are true:
     * <ul><li>the values array is null</li>
     *     <li>the weights array is null</li>
     *     <li>the weights array does not have the same length as the values array</li>
     *     <li>the weights array contains one or more infinite values</li>
     *     <li>the weights array contains one or more NaN values</li>
     *     <li>the weights array contains negative values</li>
     *     <li>the start and length arguments do not determine a valid array</li></ul>
     * </li>
     * <li>returns {@code false} if the array is non-null, but
     * {@code length} is 0 unless {@code allowEmpty} is {@code true}</li>
     * </ul>
     *
     * @param values the input array.
     * @param weights the weights array.
     * @param begin index of the first array element to include.
     * @param length the number of elements to include.
     * @param allowEmpty if {@code true} than allow zero length arrays to pass.
     * @return {@code true} if the parameters are valid.
     * @throws NullArgumentException if either of the arrays are null
     * @throws MathIllegalArgumentException if the array indices are not valid,
     * the weights array contains NaN, infinite or negative elements, or there
     * are no positive weights.
     */
    public static boolean verifyValues(final double[] values, final double[] weights,
            final int begin, final int length, final boolean allowEmpty) throws MathIllegalArgumentException {

        MathUtils.checkNotNull(weights, LocalizedCoreFormats.INPUT_ARRAY);
        MathUtils.checkNotNull(values, LocalizedCoreFormats.INPUT_ARRAY);

        checkEqualLength(weights, values);

        boolean containsPositiveWeight = false;
        for (int i = begin; i < begin + length; i++) {
            final double weight = weights[i];
            if (Double.isNaN(weight)) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NAN_ELEMENT_AT_INDEX, i);
            }
            if (Double.isInfinite(weight)) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.INFINITE_ARRAY_ELEMENT, weight, i);
            }
            if (weight < 0) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NEGATIVE_ELEMENT_AT_INDEX, i, weight);
            }
            if (!containsPositiveWeight && weight > 0.0) {
                containsPositiveWeight = true;
            }
        }

        if (!containsPositiveWeight) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.WEIGHT_AT_LEAST_ONE_NON_ZERO);
        }

        return verifyValues(values, begin, length, allowEmpty);
    }

    /**
     * Concatenates a sequence of arrays. The return array consists of the
     * entries of the input arrays concatenated in the order they appear in
     * the argument list.  Null arrays cause NullPointerExceptions; zero
     * length arrays are allowed (contributing nothing to the output array).
     *
     * @param x list of double[] arrays to concatenate
     * @return a new array consisting of the entries of the argument arrays
     * @throws NullPointerException if any of the arrays are null
     */
    public static double[] concatenate(double[]... x) {
        int combinedLength = 0;
        for (double[] a : x) {
            combinedLength += a.length;
        }
        int offset = 0;
        final double[] combined = new double[combinedLength];
        for (double[] doubles : x) {
            final int curLength = doubles.length;
            System.arraycopy(doubles, 0, combined, offset, curLength);
            offset += curLength;
        }
        return combined;
    }

    /**
     * Returns an array consisting of the unique values in {@code data}.
     * The return array is sorted in descending order.  Empty arrays
     * are allowed, but null arrays result in NullPointerException.
     * Infinities are allowed.  NaN values are allowed with maximum
     * sort order - i.e., if there are NaN values in {@code data},
     * {@code Double.NaN} will be the first element of the output array,
     * even if the array also contains {@code Double.POSITIVE_INFINITY}.
     *
     * @param data array to scan
     * @return descending list of values included in the input array
     * @throws NullPointerException if data is null
     */
    public static double[] unique(double[] data) {
        NavigableSet<Double> values = new TreeSet<>();
        for (double datum : data) {
            values.add(datum);
        }
        final int count = values.size();
        final double[] out = new double[count];
        Iterator<Double> iterator = values.descendingIterator();
        int i = 0;
        while (iterator.hasNext()) {
            out[i++] = iterator.next();
        }
        return out;
    }
}
