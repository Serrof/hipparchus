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
package org.hipparchus.analysis.interpolation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hipparchus.analysis.differentiation.Derivative;
import org.hipparchus.analysis.differentiation.UnivariateDifferentiableVectorFunction;
import org.hipparchus.analysis.polynomials.PolynomialFunction;
import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.MathRuntimeException;
import org.hipparchus.exception.NullArgumentException;
import org.hipparchus.util.CombinatoricsUtils;
import org.hipparchus.util.MathArrays;
import org.hipparchus.util.MathUtils;

/** Polynomial interpolator using both sample values and sample derivatives.
 * <p>
 * The interpolation polynomials match all sample points, including both values
 * and provided derivatives. There is one polynomial for each component of
 * the values vector. All polynomials have the same degree. The degree of the
 * polynomials depends on the number of points and number of derivatives at each
 * point. For example the interpolation polynomials for n sample points without
 * any derivatives all have degree n-1. The interpolation polynomials for n
 * sample points with the two extreme points having value and first derivative
 * and the remaining points having value only all have degree n+1. The
 * interpolation polynomial for n sample points with value, first and second
 * derivative for all points all have degree 3n-1.
 * </p>
 *
 */
public class HermiteInterpolator implements UnivariateDifferentiableVectorFunction {

    /** Sample abscissae. */
    private final List<Double> abscissae;

    /** Top diagonal of the divided differences array. */
    private final List<double[]> topDiagonal;

    /** Bottom diagonal of the divided differences array. */
    private final List<double[]> bottomDiagonal;

    /** Create an empty interpolator.
     */
    public HermiteInterpolator() {
        this.abscissae      = new ArrayList<>();
        this.topDiagonal    = new ArrayList<>();
        this.bottomDiagonal = new ArrayList<>();
    }

    /** Add a sample point.
     * <p>
     * This method must be called once for each sample point. It is allowed to
     * mix some calls with values only with calls with values and first
     * derivatives.
     * </p>
     * <p>
     * The point abscissae for all calls <em>must</em> be different.
     * </p>
     * @param x abscissa of the sample point
     * @param value value and derivatives of the sample point
     * (if only one row is passed, it is the value, if two rows are
     * passed the first one is the value and the second the derivative
     * and so on)
     * @exception MathIllegalArgumentException if the abscissa difference between added point
     * and a previous point is zero (i.e. the two points are at same abscissa)
     * @exception MathRuntimeException if the number of derivatives is larger
     * than 20, which prevents computation of a factorial
     */
    public void addSamplePoint(final double x, final double[] ... value)
        throws MathRuntimeException {

        for (int i = 0; i < value.length; ++i) {

            final double[] y = value[i].clone();
            if (i > 1) {
                double inv = 1.0 / CombinatoricsUtils.factorial(i);
                for (int j = 0; j < y.length; ++j) {
                    y[j] *= inv;
                }
            }

            // update the bottom diagonal of the divided differences array
            final int n = abscissae.size();
            bottomDiagonal.add(n - i, y);
            double[] bottom0 = y;
            for (int j = i; j < n; ++j) {
                final double[] bottom1 = bottomDiagonal.get(n - (j + 1));
                final double inv = 1.0 / (x - abscissae.get(n - (j + 1)));
                if (Double.isInfinite(inv)) {
                    throw new MathIllegalArgumentException(LocalizedCoreFormats.DUPLICATED_ABSCISSA_DIVISION_BY_ZERO, x);
                }
                for (int k = 0; k < y.length; ++k) {
                    bottom1[k] = inv * (bottom0[k] - bottom1[k]);
                }
                bottom0 = bottom1;
            }

            // update the top diagonal of the divided differences array
            topDiagonal.add(bottom0.clone());

            // update the abscissae array
            abscissae.add(x);

        }

    }

    /** Compute the interpolation polynomials.
     * @return interpolation polynomials array
     * @exception MathIllegalArgumentException if sample is empty
     */
    public PolynomialFunction[] getPolynomials()
        throws MathIllegalArgumentException {

        // safety check
        checkInterpolation();

        // iteration initialization
        final PolynomialFunction zero = polynomial(0);
        PolynomialFunction[] polynomials = new PolynomialFunction[topDiagonal.get(0).length];
        for (int i = 0; i < polynomials.length; ++i) {
            polynomials[i] = zero;
        }
        PolynomialFunction coeff = polynomial(1);

        // build the polynomials by iterating on the top diagonal of the divided differences array
        for (int i = 0; i < topDiagonal.size(); ++i) {
            double[] tdi = topDiagonal.get(i);
            for (int k = 0; k < polynomials.length; ++k) {
                polynomials[k] = polynomials[k].add(coeff.multiply(polynomial(tdi[k])));
            }
            coeff = coeff.multiply(polynomial(-abscissae.get(i), 1.0));
        }

        return polynomials;

    }

    /** Interpolate value at a specified abscissa.
     * <p>
     * Calling this method is equivalent to call the {@link PolynomialFunction#value(double)
     * value} methods of all polynomials returned by {@link #getPolynomials() getPolynomials},
     * except it does not build the intermediate polynomials, so this method is faster and
     * numerically more stable.
     * </p>
     * @param x interpolation abscissa
     * @return interpolated value
     * @exception MathIllegalArgumentException if sample is empty
     */
    @Override
    public double[] value(double x) throws MathIllegalArgumentException {

        // safety check
        checkInterpolation();

        final double[] value = new double[topDiagonal.get(0).length];
        double valueCoeff = 1;
        for (int i = 0; i < topDiagonal.size(); ++i) {
            double[] dividedDifference = topDiagonal.get(i);
            for (int k = 0; k < value.length; ++k) {
                value[k] += dividedDifference[k] * valueCoeff;
            }
            final double deltaX = x - abscissae.get(i);
            valueCoeff *= deltaX;
        }

        return value;

    }

    /** {@inheritDoc}. */
    @Override
    public <T extends Derivative<T>> T[] value(T x)
        throws MathIllegalArgumentException {

        // safety check
        checkInterpolation();

        final T[] value = MathArrays.buildArray(x.getField(), topDiagonal.get(0).length);
        Arrays.fill(value, x.getField().getZero());
        T valueCoeff = x.getField().getOne();
        for (int i = 0; i < topDiagonal.size(); ++i) {
            double[] dividedDifference = topDiagonal.get(i);
            for (int k = 0; k < value.length; ++k) {
                value[k] = value[k].add(valueCoeff.multiply(dividedDifference[k]));
            }
            final T deltaX = x.subtract(abscissae.get(i));
            valueCoeff = valueCoeff.multiply(deltaX);
        }

        return value;

    }


    /** Interpolate value and first derivatives at a specified abscissa.
     * @param x interpolation abscissa
     * @param order maximum derivation order
     * @return interpolated value and derivatives (value in row 0,
     * 1<sup>st</sup> derivative in row 1, ... n<sup>th</sup> derivative in row n)
     * @exception MathIllegalArgumentException if sample is empty
     * @throws NullArgumentException if x is null
     */
    public double[][] derivatives(double x, int order)
        throws MathIllegalArgumentException, NullArgumentException {

        // safety check
        MathUtils.checkNotNull(x);
        if (abscissae.isEmpty()) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.EMPTY_INTERPOLATION_SAMPLE);
        }

        final double[] tj = new double[order + 1];
        tj[0] = 0.0;
        for (int i = 0; i < order; ++i) {
            tj[i + 1] = tj[i] + 1;
        }

        final double[][] derivatives = new double[order + 1][topDiagonal.get(0).length];
        final double[] valueCoeff = new double[order + 1];
        valueCoeff[0] = 1.0;
        for (int i = 0; i < topDiagonal.size(); ++i) {
            double[] dividedDifference = topDiagonal.get(i);
            final double deltaX = x - abscissae.get(i);
            for (int j = order; j >= 0; --j) {
                for (int k = 0; k < derivatives[j].length; ++k) {
                    derivatives[j][k] += dividedDifference[k] * valueCoeff[j];
                }
                valueCoeff[j] *= deltaX;
                if (j > 0) {
                    valueCoeff[j] += tj[j] * valueCoeff[j - 1];
                }
            }
        }

        return derivatives;

    }

    /** Check interpolation can be performed.
     * @exception MathIllegalArgumentException if interpolation cannot be performed
     * because sample is empty
     */
    private void checkInterpolation() throws MathIllegalArgumentException {
        if (abscissae.isEmpty()) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.EMPTY_INTERPOLATION_SAMPLE);
        }
    }

    /** Create a polynomial from its coefficients.
     * @param c polynomials coefficients
     * @return polynomial
     */
    private PolynomialFunction polynomial(double ... c) {
        return new PolynomialFunction(c);
    }

}
