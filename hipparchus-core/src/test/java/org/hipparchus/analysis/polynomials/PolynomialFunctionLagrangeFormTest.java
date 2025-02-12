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
package org.hipparchus.analysis.polynomials;

import org.hipparchus.exception.MathIllegalArgumentException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test case for Lagrange form of polynomial function.
 * <p>
 * We use n+1 points to interpolate a polynomial of degree n. This should
 * give us the exact same polynomial as result. Thus we can use a very
 * small tolerance to account only for round-off errors.
 *
 */
final class PolynomialFunctionLagrangeFormTest {

    @Test
    void testGetter() {
        // GIVEN
        double[] x = { 1., 2. };
        double[] y = { -1., 4. };
        final PolynomialFunctionLagrangeForm lagrangeForm = new PolynomialFunctionLagrangeForm(x, y);
        // WHEN & THEN
        assertArrayEquals(x, lagrangeForm.getInterpolatingPoints());
        assertArrayEquals(y, lagrangeForm.getInterpolatingValues());
        assertArrayEquals(new double[] { -6., 5. }, lagrangeForm.getCoefficients());
    }

    @Test
    void testGetCoefficients() {
        // GIVEN
        double[] x = { 1., 2. };
        double[] y = { -1., 4. };
        final PolynomialFunctionLagrangeForm lagrangeForm = new PolynomialFunctionLagrangeForm(x, y);
        // WHEN
        final double[] coefficients = lagrangeForm.getCoefficients();
        // WHEN & THEN
        assertArrayEquals(lagrangeForm.getCoefficients(), coefficients);
    }

    @Test
    void testEvaluate() {
        // GIVEN
        double[] x = { 1., 2. };
        double[] y = { -1., 4. };
        final double point = 3.;
        // WHEN
        final double value = PolynomialFunctionLagrangeForm.evaluate(x, y, point);
        // THEN
        final PolynomialFunctionLagrangeForm lagrangeForm = new PolynomialFunctionLagrangeForm(x, y);
        assertEquals(lagrangeForm.value(point), value);
    }

    /**
     * Test of polynomial for the linear function.
     */
    @Test
    void testLinearFunction() {
        PolynomialFunctionLagrangeForm p;
        double[] c;
        double z;
        double expected;
        double result;
        double tolerance = 1E-12;

        // p(x) = 1.5x - 4
        double[] x = { 0.0, 3.0 };
        double[] y = { -4.0, 0.5 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 2.0; expected = -1.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = 4.5; expected = 2.75; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = 6.0; expected = 5.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        assertEquals(1, p.degree());

        c = p.getCoefficients();
        assertEquals(2, c.length);
        assertEquals(-4.0, c[0], tolerance);
        assertEquals(1.5, c[1], tolerance);
    }

    /**
     * Test of polynomial for the quadratic function.
     */
    @Test
    void testQuadraticFunction() {
        PolynomialFunctionLagrangeForm p;
        double[] c;
        double z;
        double expected;
        double result;
        double tolerance = 1E-12;

        // p(x) = 2x^2 + 5x - 3 = (2x - 1)(x + 3)
        double[] x = { 0.0, -1.0, 0.5 };
        double[] y = { -3.0, -6.0, 0.0 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 1.0; expected = 4.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = 2.5; expected = 22.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = -2.0; expected = -5.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        assertEquals(2, p.degree());

        c = p.getCoefficients();
        assertEquals(3, c.length);
        assertEquals(-3.0, c[0], tolerance);
        assertEquals(5.0, c[1], tolerance);
        assertEquals(2.0, c[2], tolerance);
    }

    /**
     * Test of polynomial for the quintic function.
     */
    @Test
    void testQuinticFunction() {
        PolynomialFunctionLagrangeForm p;
        double[] c;
        double z;
        double expected;
        double result;
        double tolerance = 1E-12;

        // p(x) = x^5 - x^4 - 7x^3 + x^2 + 6x = x(x^2 - 1)(x + 2)(x - 3)
        double[] x = { 1.0, -1.0, 2.0, 3.0, -3.0, 0.5 };
        double[] y = { 0.0, 0.0, -24.0, 0.0, -144.0, 2.34375 };
        p = new PolynomialFunctionLagrangeForm(x, y);

        z = 0.0; expected = 0.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = -2.0; expected = 0.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        z = 4.0; expected = 360.0; result = p.value(z);
        assertEquals(expected, result, tolerance);

        assertEquals(5, p.degree());

        c = p.getCoefficients();
        assertEquals(6, c.length);
        assertEquals(0.0, c[0], tolerance);
        assertEquals(6.0, c[1], tolerance);
        assertEquals(1.0, c[2], tolerance);
        assertEquals(-7.0, c[3], tolerance);
        assertEquals(-1.0, c[4], tolerance);
        assertEquals(1.0, c[5], tolerance);
    }

    /**
     * Test of parameters for the polynomial.
     */
    @Test
    void testParameters() {

        try {
            // bad input array length
            double[] x = { 1.0 };
            double[] y = { 2.0 };
            new PolynomialFunctionLagrangeForm(x, y);
            fail("Expecting MathIllegalArgumentException - bad input array length");
        } catch (MathIllegalArgumentException ex) {
            // expected
        }
        try {
            // mismatch input arrays
            double[] x = { 1.0, 2.0, 3.0, 4.0 };
            double[] y = { 0.0, -4.0, -24.0 };
            new PolynomialFunctionLagrangeForm(x, y);
            fail("Expecting MathIllegalArgumentException - mismatch input arrays");
        } catch (MathIllegalArgumentException ex) {
            // expected
        }
    }
}
