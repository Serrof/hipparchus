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

package org.hipparchus.geometry.euclidean.threed;

import org.hipparchus.dfp.Dfp;
import org.hipparchus.dfp.DfpField;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.MathIllegalStateException;
import org.hipparchus.exception.MathRuntimeException;
import org.hipparchus.geometry.LocalizedGeometryFormats;
import org.hipparchus.random.UnitSphereRandomVectorGenerator;
import org.hipparchus.random.Well1024a;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FieldRotationDfpTest {

    @Test
    void testIssue304Cardan() {
        for (final RotationConvention convention : RotationConvention.values()) {
            for (final RotationOrder order : Arrays.asList(RotationOrder.XYZ,
                                                           RotationOrder.XZY,
                                                           RotationOrder.YXZ,
                                                           RotationOrder.YZX,
                                                           RotationOrder.ZXY,
                                                           RotationOrder.ZYX)) {

                // first singularity
                FieldRotation<Dfp> singularPlus =
                    new FieldRotation<>(order, convention,
                                        field.getZero(), field.getPi().multiply(0.5), field.getZero().newInstance(0.125));
                assertEquals(0.0, singularPlus.getAngles(order, convention)[0].getReal(), 1.0e-20);
                assertEquals(MathUtils.SEMI_PI, singularPlus.getAngles(order, convention)[1].getReal(), 1.0e-20);
                assertEquals(0.125, singularPlus.getAngles(order, convention)[2].getReal(), 1.0e-20);

                // second singularity
                FieldRotation<Dfp> singularMinus =
                    new FieldRotation<>(order, convention,
                                        field.getZero(), field.getPi().multiply(-0.5), field.getZero().newInstance(0.125));
                assertEquals(0.0, singularMinus.getAngles(order, convention)[0].getReal(), 1.0e-20);
                assertEquals(-MathUtils.SEMI_PI, singularMinus.getAngles(order, convention)[1].getReal(), 1.0e-20);
                assertEquals(0.125, singularMinus.getAngles(order, convention)[2].getReal(), 1.0e-20);

            }
        }
    }

    @Test
    void testIssue304Euler() {
        for (final RotationConvention convention : RotationConvention.values()) {
            for (final RotationOrder order : Arrays.asList(RotationOrder.XYX,
                                                           RotationOrder.XZX,
                                                           RotationOrder.YXY,
                                                           RotationOrder.YZY,
                                                           RotationOrder.ZXZ,
                                                           RotationOrder.ZYZ)) {

                // first singularity
                FieldRotation<Dfp> singularZero =
                    new FieldRotation<>(order, convention,
                                        field.getZero().newInstance(0.125), field.getZero(), field.getZero() );
                assertEquals(0.125, singularZero.getAngles(order, convention)[0].getReal(), 1.0e-20);
                assertEquals(0.0, singularZero.getAngles(order, convention)[1].getReal(), 1.0e-20);
                assertEquals(0.0, singularZero.getAngles(order, convention)[2].getReal(), 1.0e-20);

                // second singularity
                FieldRotation<Dfp> singularPi =
                    new FieldRotation<>(order, convention,
                                        field.getZero().newInstance(0.125), field.getPi(), field.getZero());
                assertEquals(0.125, singularPi.getAngles(order, convention)[0].getReal(), 1.0e-20);
                assertEquals(FastMath.PI, singularPi.getAngles(order, convention)[1].getReal(), 1.0e-20);
                assertEquals(0.0, singularPi.getAngles(order, convention)[2].getReal(), 1.0e-20);

            }
        }
    }

    @Test
    void testIdentity() {

        FieldRotation<Dfp> r = createRotation(1, 0, 0, 0, false);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

        r = createRotation(-1, 0, 0, 0, false);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

        r = createRotation(42, 0, 0, 0, true);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

        r = FieldRotation.getIdentity(new DfpField(20));
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

    }

    @Test
    void testAxisAngleVectorOperator() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r = new FieldRotation<>(createAxis(10, 10, 10),
                                                   createAngle(2 * FastMath.PI / 3) ,
                                                   RotationConvention.VECTOR_OPERATOR);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(1, 0, 0));
        double s = 1 / FastMath.sqrt(3);
        checkVector(r.getAxis(RotationConvention.VECTOR_OPERATOR), createVector( s,  s,  s));
        checkVector(r.getAxis(RotationConvention.FRAME_TRANSFORM), createVector(-s, -s, -s));
        checkAngle(r.getAngle(), 2 * FastMath.PI / 3);

        try {
            new FieldRotation<>(createAxis(0, 0, 0),
                                createAngle(2 * FastMath.PI / 3),
                                RotationConvention.VECTOR_OPERATOR);
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            assertEquals(LocalizedGeometryFormats.ZERO_NORM_FOR_ROTATION_AXIS, e.getSpecifier());
        }

        r = new FieldRotation<>(createAxis(0, 0, 1),
                                createAngle(1.5 * FastMath.PI),
                                RotationConvention.VECTOR_OPERATOR);
        checkVector(r.getAxis(RotationConvention.VECTOR_OPERATOR), createVector(0, 0, -1));
        checkVector(r.getAxis(RotationConvention.FRAME_TRANSFORM), createVector(0, 0, +1));
        checkAngle(r.getAngle(), MathUtils.SEMI_PI);

        r = new FieldRotation<>(createAxis(0, 1, 0),
                                createAngle(FastMath.PI),
                                RotationConvention.VECTOR_OPERATOR);
        checkVector(r.getAxis(RotationConvention.VECTOR_OPERATOR), createVector(0, +1, 0));
        checkVector(r.getAxis(RotationConvention.FRAME_TRANSFORM), createVector(0, -1, 0));
        checkAngle(r.getAngle(), FastMath.PI);

        checkVector(createRotation(1, 0, 0, 0, false).getAxis(RotationConvention.VECTOR_OPERATOR), createVector(+1, 0, 0));
        checkVector(createRotation(1, 0, 0, 0, false).getAxis(RotationConvention.FRAME_TRANSFORM), createVector(-1, 0, 0));

    }

    @Test
    void testAxisAngleFrameTransform() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r = new FieldRotation<>(createAxis(10, 10, 10),
                                                   createAngle(2 * FastMath.PI / 3) ,
                                                   RotationConvention.FRAME_TRANSFORM);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 1, 0));
        double s = 1 / FastMath.sqrt(3);
        checkVector(r.getAxis(RotationConvention.FRAME_TRANSFORM), createVector( s,  s,  s));
        checkVector(r.getAxis(RotationConvention.VECTOR_OPERATOR), createVector(-s, -s, -s));
        checkAngle(r.getAngle(), 2 * FastMath.PI / 3);

        try {
            new FieldRotation<>(createAxis(0, 0, 0),
                                createAngle(2 * FastMath.PI / 3),
                                RotationConvention.FRAME_TRANSFORM);
            fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            assertEquals(LocalizedGeometryFormats.ZERO_NORM_FOR_ROTATION_AXIS, e.getSpecifier());
        }

        r = new FieldRotation<>(createAxis(0, 0, 1),
                                createAngle(1.5 * FastMath.PI),
                                RotationConvention.FRAME_TRANSFORM);
        checkVector(r.getAxis(RotationConvention.FRAME_TRANSFORM), createVector(0, 0, -1));
        checkVector(r.getAxis(RotationConvention.VECTOR_OPERATOR), createVector(0, 0, +1));
        checkAngle(r.getAngle(), MathUtils.SEMI_PI);

        r = new FieldRotation<>(createAxis(0, 1, 0),
                                createAngle(FastMath.PI),
                                RotationConvention.FRAME_TRANSFORM);
        checkVector(r.getAxis(RotationConvention.FRAME_TRANSFORM), createVector(0, +1, 0));
        checkVector(r.getAxis(RotationConvention.VECTOR_OPERATOR), createVector(0, -1, 0));
        checkAngle(r.getAngle(), FastMath.PI);

        checkVector(createRotation(1, 0, 0, 0, false).getAxis(RotationConvention.FRAME_TRANSFORM), createVector(-1, 0, 0));
        checkVector(createRotation(1, 0, 0, 0, false).getAxis(RotationConvention.VECTOR_OPERATOR), createVector(+1, 0, 0));

    }

    @Test
    void testRevert() {
        double a = 0.001;
        double b = 0.36;
        double c = 0.48;
        double d = 0.8;
        FieldRotation<Dfp> r = createRotation(a, b, c, d, true);
        FieldRotation<Dfp> reverted = r.revert();
        FieldRotation<Dfp> rrT = r.applyTo(reverted);
        checkRotationDS(rrT, 1, 0, 0, 0);
        FieldRotation<Dfp> rTr = reverted.applyTo(r);
        checkRotationDS(rTr, 1, 0, 0, 0);
        assertEquals(r.getAngle().getReal(), reverted.getAngle().getReal(), 1.0e-15);
        assertEquals(-1,
                            FieldVector3D.dotProduct(r.getAxis(RotationConvention.VECTOR_OPERATOR),
                                                     reverted.getAxis(RotationConvention.VECTOR_OPERATOR)).getReal(),
                            1.0e-15);
    }

    @Test
    void testRevertVectorOperator() {
        double a = 0.001;
        double b = 0.36;
        double c = 0.48;
        double d = 0.8;
        FieldRotation<Dfp> r = createRotation(a, b, c, d, true);
        FieldRotation<Dfp> reverted = r.revert();
        FieldRotation<Dfp> rrT = r.compose(reverted, RotationConvention.VECTOR_OPERATOR);
        checkRotationDS(rrT, 1, 0, 0, 0);
        FieldRotation<Dfp> rTr = reverted.compose(r, RotationConvention.VECTOR_OPERATOR);
        checkRotationDS(rTr, 1, 0, 0, 0);
        assertEquals(r.getAngle().getReal(), reverted.getAngle().getReal(), 1.0e-15);
        assertEquals(-1,
                            FieldVector3D.dotProduct(r.getAxis(RotationConvention.VECTOR_OPERATOR),
                                                     reverted.getAxis(RotationConvention.VECTOR_OPERATOR)).getReal(),
                            1.0e-15);
    }

    @Test
    void testRevertFrameTransform() {
        double a = 0.001;
        double b = 0.36;
        double c = 0.48;
        double d = 0.8;
        FieldRotation<Dfp> r = createRotation(a, b, c, d, true);
        FieldRotation<Dfp> reverted = r.revert();
        FieldRotation<Dfp> rrT = r.compose(reverted, RotationConvention.FRAME_TRANSFORM);
        checkRotationDS(rrT, 1, 0, 0, 0);
        FieldRotation<Dfp> rTr = reverted.compose(r, RotationConvention.FRAME_TRANSFORM);
        checkRotationDS(rTr, 1, 0, 0, 0);
        assertEquals(r.getAngle().getReal(), reverted.getAngle().getReal(), 1.0e-15);
        assertEquals(-1,
                            FieldVector3D.dotProduct(r.getAxis(RotationConvention.FRAME_TRANSFORM),
                                                     reverted.getAxis(RotationConvention.FRAME_TRANSFORM)).getReal(),
                            1.0e-15);
    }

    @Test
    void testVectorOnePair() throws MathRuntimeException {

        FieldVector3D<Dfp> u = createVector(3, 2, 1);
        FieldVector3D<Dfp> v = createVector(-4, 2, 2);
        FieldRotation<Dfp> r = new FieldRotation<>(u, v);
        checkVector(r.applyTo(u.scalarMultiply(v.getNorm())), v.scalarMultiply(u.getNorm()));

        checkAngle(new FieldRotation<>(u, u.negate()).getAngle(), FastMath.PI);

        try {
            new FieldRotation<>(u, createVector(0, 0, 0));
            fail("an exception should have been thrown");
        } catch (MathRuntimeException e) {
            // expected behavior
        }

    }

    @Test
    void testVectorTwoPairs() throws MathRuntimeException {

        FieldVector3D<Dfp> u1 = createVector(3, 0, 0);
        FieldVector3D<Dfp> u2 = createVector(0, 5, 0);
        FieldVector3D<Dfp> v1 = createVector(0, 0, 2);
        FieldVector3D<Dfp> v2 = createVector(-2, 0, 2);
        FieldRotation<Dfp> r = new FieldRotation<>(u1, u2, v1, v2);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(-1, 0, 0));

        r = new FieldRotation<>(u1, u2, u1.negate(), u2.negate());
        FieldVector3D<Dfp> axis = r.getAxis(RotationConvention.VECTOR_OPERATOR);
        if (FieldVector3D.dotProduct(axis, createVector(0, 0, 1)).getReal() > 0) {
            checkVector(axis, createVector(0, 0, 1));
        } else {
            checkVector(axis, createVector(0, 0, -1));
        }
        checkAngle(r.getAngle(), FastMath.PI);

        double sqrt = FastMath.sqrt(2) / 2;
        r = new FieldRotation<>(createVector(1, 0, 0),  createVector(0, 1, 0),
                                createVector(0.5, 0.5,  sqrt),
                                createVector(0.5, 0.5, -sqrt));
        checkRotationDS(r, sqrt, 0.5, 0.5, 0);

        r = new FieldRotation<>(u1, u2, u1, FieldVector3D.crossProduct(u1, u2));
        checkRotationDS(r, sqrt, -sqrt, 0, 0);

        checkRotationDS(new FieldRotation<>(u1, u2, u1, u2), 1, 0, 0, 0);

        try {
            new FieldRotation<>(u1, u2, createVector(0, 0, 0), v2);
            fail("an exception should have been thrown");
        } catch (MathRuntimeException e) {
            // expected behavior
        }

    }

    @Test
    void testMatrix()
        throws MathIllegalArgumentException {

        try {
            createRotation(new double[][] {
                { 0.0, 1.0, 0.0 },
                { 1.0, 0.0, 0.0 }
            }, 1.0e-7);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException nrme) {
            // expected behavior
        }

        try {
            createRotation(new double[][] {
                {  0.445888,  0.797184, -0.407040 },
                {  0.821760, -0.184320,  0.539200 },
                { -0.354816,  0.574912,  0.737280 }
            }, 1.0e-7);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException nrme) {
            // expected behavior
        }

        try {
            createRotation(new double[][] {
                {  0.4,  0.8, -0.4 },
                { -0.4,  0.6,  0.7 },
                {  0.8, -0.2,  0.5 }
            }, 1.0e-15);
            fail("Expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException nrme) {
            // expected behavior
        }

        checkRotationDS(createRotation(new double[][] {
            {  0.445888,  0.797184, -0.407040 },
            { -0.354816,  0.574912,  0.737280 },
            {  0.821760, -0.184320,  0.539200 }
        }, 1.0e-10),
        0.8, 0.288, 0.384, 0.36);

        checkRotationDS(createRotation(new double[][] {
            {  0.539200,  0.737280,  0.407040 },
            {  0.184320, -0.574912,  0.797184 },
            {  0.821760, -0.354816, -0.445888 }
        }, 1.0e-10),
        0.36, 0.8, 0.288, 0.384);

        checkRotationDS(createRotation(new double[][] {
            { -0.445888,  0.797184, -0.407040 },
            {  0.354816,  0.574912,  0.737280 },
            {  0.821760,  0.184320, -0.539200 }
        }, 1.0e-10),
        0.384, 0.36, 0.8, 0.288);

        checkRotationDS(createRotation(new double[][] {
            { -0.539200,  0.737280,  0.407040 },
            { -0.184320, -0.574912,  0.797184 },
            {  0.821760,  0.354816,  0.445888 }
        }, 1.0e-10),
        0.288, 0.384, 0.36, 0.8);

        double[][] m1 = { { 0.0, 1.0, 0.0 },
            { 0.0, 0.0, 1.0 },
            { 1.0, 0.0, 0.0 } };
        FieldRotation<Dfp> r = createRotation(m1, 1.0e-7);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 1, 0));

        double[][] m2 = { { 0.83203, -0.55012, -0.07139 },
            { 0.48293,  0.78164, -0.39474 },
            { 0.27296,  0.29396,  0.91602 } };
        r = createRotation(m2, 1.0e-12);

        Dfp[][] m3 = r.getMatrix();
        double d00 = m2[0][0] - m3[0][0].getReal();
        double d01 = m2[0][1] - m3[0][1].getReal();
        double d02 = m2[0][2] - m3[0][2].getReal();
        double d10 = m2[1][0] - m3[1][0].getReal();
        double d11 = m2[1][1] - m3[1][1].getReal();
        double d12 = m2[1][2] - m3[1][2].getReal();
        double d20 = m2[2][0] - m3[2][0].getReal();
        double d21 = m2[2][1] - m3[2][1].getReal();
        double d22 = m2[2][2] - m3[2][2].getReal();

        assertTrue(FastMath.abs(d00) < 6.0e-6);
        assertTrue(FastMath.abs(d01) < 6.0e-6);
        assertTrue(FastMath.abs(d02) < 6.0e-6);
        assertTrue(FastMath.abs(d10) < 6.0e-6);
        assertTrue(FastMath.abs(d11) < 6.0e-6);
        assertTrue(FastMath.abs(d12) < 6.0e-6);
        assertTrue(FastMath.abs(d20) < 6.0e-6);
        assertTrue(FastMath.abs(d21) < 6.0e-6);
        assertTrue(FastMath.abs(d22) < 6.0e-6);

        assertTrue(FastMath.abs(d00) > 4.0e-7);
        assertTrue(FastMath.abs(d01) > 4.0e-7);
        assertTrue(FastMath.abs(d02) > 4.0e-7);
        assertTrue(FastMath.abs(d10) > 4.0e-7);
        assertTrue(FastMath.abs(d11) > 4.0e-7);
        assertTrue(FastMath.abs(d12) > 4.0e-7);
        assertTrue(FastMath.abs(d20) > 4.0e-7);
        assertTrue(FastMath.abs(d21) > 4.0e-7);
        assertTrue(FastMath.abs(d22) > 4.0e-7);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                double m3tm3 = m3[i][0].getReal() * m3[j][0].getReal() +
                               m3[i][1].getReal() * m3[j][1].getReal() +
                               m3[i][2].getReal() * m3[j][2].getReal();
                if (i == j) {
                    assertTrue(FastMath.abs(m3tm3 - 1.0) < 1.0e-10);
                } else {
                    assertTrue(FastMath.abs(m3tm3) < 1.0e-10);
                }
            }
        }

        checkVector(r.applyTo(createVector(1, 0, 0)),
                    new FieldVector3D<>(m3[0][0], m3[1][0], m3[2][0]));
        checkVector(r.applyTo(createVector(0, 1, 0)),
                    new FieldVector3D<>(m3[0][1], m3[1][1], m3[2][1]));
        checkVector(r.applyTo(createVector(0, 0, 1)),
                    new FieldVector3D<>(m3[0][2], m3[1][2], m3[2][2]));

        double[][] m4 = { { 1.0,  0.0,  0.0 },
            { 0.0, -1.0,  0.0 },
            { 0.0,  0.0, -1.0 } };
        r = createRotation(m4, 1.0e-7);
        checkAngle(r.getAngle(), FastMath.PI);

        try {
            double[][] m5 = { { 0.0, 0.0, 1.0 },
                { 0.0, 1.0, 0.0 },
                { 1.0, 0.0, 0.0 } };
            r = createRotation(m5, 1.0e-7);
            fail("got " + r + ", should have caught an exception");
        } catch (MathIllegalArgumentException e) {
            // expected
        }

    }

    @Test
    void testAngles()
        throws MathIllegalStateException {

        DfpField field = new DfpField(15);

        for (RotationConvention convention : RotationConvention.values()) {
            RotationOrder[] CardanOrders = {
                RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
                RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
            };

            for (final RotationOrder cardanOrder : CardanOrders) {
                for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 2.0) {
                    for (double alpha2 = -1.55; alpha2 < 1.55; alpha2 += 0.8) {
                        for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 2.0) {
                            FieldRotation<Dfp> r = new FieldRotation<>(cardanOrder,
                                                                       convention,
                                                                       field.newDfp(alpha1),
                                                                       field.newDfp(alpha2),
                                                                       field.newDfp(alpha3));
                            Dfp[] angles = r.getAngles(cardanOrder, convention);
                            checkAngle(angles[0], alpha1);
                            checkAngle(angles[1], alpha2);
                            checkAngle(angles[2], alpha3);
                        }
                    }
                }
            }

            RotationOrder[] EulerOrders = {
                RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
                RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
            };

            for (final RotationOrder eulerOrder : EulerOrders) {
                for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 2.0) {
                    for (double alpha2 = 0.05; alpha2 < 3.1; alpha2 += 0.8) {
                        for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 2.0) {
                            FieldRotation<Dfp> r = new FieldRotation<>(eulerOrder,
                                                                       convention,
                                                                       field.newDfp(alpha1),
                                                                       field.newDfp(alpha2),
                                                                       field.newDfp(alpha3));
                            Dfp[] angles = r.getAngles(eulerOrder, convention);
                            checkAngle(angles[0], alpha1);
                            checkAngle(angles[1], alpha2);
                            checkAngle(angles[2], alpha3);
                        }
                    }
                }
            }
        }

    }

    @Test
    void testSingularities() {

        DfpField field = new DfpField(20);
        for (RotationConvention convention : RotationConvention.values()) {
            RotationOrder[] CardanOrders = {
                RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
                RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
            };

            double[] singularCardanAngle = {
                -FastMath.PI / 2, -FastMath.PI / 2 + 1.0e-12, -FastMath.PI / 2 + 1.0e-10,
                FastMath.PI / 2 - 1.0e-10, FastMath.PI / 2 - 1.0e-12, FastMath.PI / 2
            };
            for (final RotationOrder cardanOrder : CardanOrders) {
                for (final double v : singularCardanAngle) {
                    FieldRotation<Dfp> r = new FieldRotation<>(cardanOrder, convention,
                                                               field.newDfp(0.1), field.newDfp(v), field.newDfp(0.3));
                    assertEquals(v, r.getAngles(cardanOrder, convention)[1].getReal(), 4.5e-16);
                }
            }

            RotationOrder[] EulerOrders = {
                RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
                RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
            };

            double[] singularEulerAngle = { 0, 1.0e-12, 1.0e-10, FastMath.PI - 1.0e-10, FastMath.PI - 1.0e-12, FastMath.PI };
            for (final RotationOrder eulerOrder : EulerOrders) {
                for (final double v : singularEulerAngle) {
                    FieldRotation<Dfp> r = new FieldRotation<>(eulerOrder, convention,
                                                               field.newDfp(0.1), field.newDfp(v), field.newDfp(0.3));
                    assertEquals(v, r.getAngles(eulerOrder, convention)[1].getReal(), 1.0e-24);
                }
            }

        }
    }

    @Test
    void testQuaternion() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1 = new FieldRotation<>(createVector(2, -3, 5),
                                                    createAngle(1.7),
                                                    RotationConvention.VECTOR_OPERATOR);
        double n = 23.5;
        FieldRotation<Dfp> r2 = new FieldRotation<>(r1.getQ0().multiply(n), r1.getQ1().multiply(n),
                                                    r1.getQ2().multiply(n), r1.getQ3().multiply(n),
                                                    true);
        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r2.applyTo(u), r1.applyTo(u));
                }
            }
        }

        r1 = createRotation(0.288,  0.384,  0.36,  0.8, false);
        checkRotationDS(r1,
                        -r1.getQ0().getReal(), -r1.getQ1().getReal(),
                        -r1.getQ2().getReal(), -r1.getQ3().getReal());

    }

    @Test
    void testApplyToRotation() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1       = new FieldRotation<>(createVector(2, -3, 5),
                                                          createAngle(1.7),
                                                          RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r2       = new FieldRotation<>(createVector(-1, 3, 2),
                                                          createAngle(0.3),
                                                          RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r3       = r2.applyTo(r1);
        FieldRotation<Dfp> r3Double = r2.applyTo(new Rotation(r1.getQ0().getReal(),
                                                      r1.getQ1().getReal(),
                                                      r1.getQ2().getReal(),
                                                      r1.getQ3().getReal(),
                                                      false));

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r2.applyTo(r1.applyTo(u)), r3.applyTo(u));
                    checkVector(r2.applyTo(r1.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

    @Test
    void testComposeVectorOperator() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1       = new FieldRotation<>(createVector(2, -3, 5),
                                                          createAngle(1.7),
                                                          RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r2       = new FieldRotation<>(createVector(-1, 3, 2),
                                                          createAngle(0.3),
                                                          RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r3       = r2.compose(r1, RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r3Double = r2.compose(new Rotation(r1.getQ0().getReal(),
                                                      r1.getQ1().getReal(),
                                                      r1.getQ2().getReal(),
                                                      r1.getQ3().getReal(),
                                                      false),
                                                 RotationConvention.VECTOR_OPERATOR);

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r2.applyTo(r1.applyTo(u)), r3.applyTo(u));
                    checkVector(r2.applyTo(r1.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

    @Test
    void testComposeFrameTransform() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1       = new FieldRotation<>(createVector(2, -3, 5),
                                                          createAngle(1.7),
                                                          RotationConvention.FRAME_TRANSFORM);
        FieldRotation<Dfp> r2       = new FieldRotation<>(createVector(-1, 3, 2),
                                                          createAngle(0.3),
                                                          RotationConvention.FRAME_TRANSFORM);
        FieldRotation<Dfp> r3       = r2.compose(r1, RotationConvention.FRAME_TRANSFORM);
        FieldRotation<Dfp> r3Double = r2.compose(new Rotation(r1.getQ0().getReal(),
                                                      r1.getQ1().getReal(),
                                                      r1.getQ2().getReal(),
                                                      r1.getQ3().getReal(),
                                                      false),
                                                 RotationConvention.FRAME_TRANSFORM);
        FieldRotation<Dfp> r4 = r1.compose(r2, RotationConvention.VECTOR_OPERATOR);
        assertEquals(0.0, FieldRotation.distance(r3, r4).getReal(), 1.0e-15);

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r1.applyTo(r2.applyTo(u)), r3.applyTo(u));
                    checkVector(r1.applyTo(r2.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

    @Test
    void testApplyInverseToRotation() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1 = new FieldRotation<>(createVector(2, -3, 5),
                                                    createAngle(1.7),
                                                    RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r2 = new FieldRotation<>(createVector(-1, 3, 2),
                                                    createAngle(0.3),
                                                    RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r3 = r2.applyInverseTo(r1);
        FieldRotation<Dfp> r3Double = r2.applyInverseTo(new Rotation(r1.getQ0().getReal(),
                                                             r1.getQ1().getReal(),
                                                             r1.getQ2().getReal(),
                                                             r1.getQ3().getReal(),
                                                             false));

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r2.applyInverseTo(r1.applyTo(u)), r3.applyTo(u));
                    checkVector(r2.applyInverseTo(r1.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

    @Test
    void testComposeInverseVectorOperator() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1 = new FieldRotation<>(createVector(2, -3, 5),
                                                    createAngle(1.7),
                                                    RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r2 = new FieldRotation<>(createVector(-1, 3, 2),
                                                    createAngle(0.3),
                                                    RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r3 = r2.composeInverse(r1, RotationConvention.VECTOR_OPERATOR);
        FieldRotation<Dfp> r3Double = r2.composeInverse(new Rotation(r1.getQ0().getReal(),
                                                             r1.getQ1().getReal(),
                                                             r1.getQ2().getReal(),
                                                             r1.getQ3().getReal(),
                                                             false),
                                                        RotationConvention.VECTOR_OPERATOR);

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r2.applyInverseTo(r1.applyTo(u)), r3.applyTo(u));
                    checkVector(r2.applyInverseTo(r1.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

    @Test
    void testComposeInverseFrameTransform() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1 = new FieldRotation<>(createVector(2, -3, 5),
                                                    createAngle(1.7),
                                                    RotationConvention.FRAME_TRANSFORM);
        FieldRotation<Dfp> r2 = new FieldRotation<>(createVector(-1, 3, 2),
                                                    createAngle(0.3),
                                                    RotationConvention.FRAME_TRANSFORM);
        FieldRotation<Dfp> r3 = r2.composeInverse(r1, RotationConvention.FRAME_TRANSFORM);
        FieldRotation<Dfp> r3Double = r2.composeInverse(new Rotation(r1.getQ0().getReal(),
                                                             r1.getQ1().getReal(),
                                                             r1.getQ2().getReal(),
                                                             r1.getQ3().getReal(),
                                                             false),
                                                        RotationConvention.FRAME_TRANSFORM);
        FieldRotation<Dfp> r4 = r1.revert().composeInverse(r2.revert(), RotationConvention.VECTOR_OPERATOR);
        assertEquals(0.0, FieldRotation.distance(r3, r4).getReal(), 1.0e-15);

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r1.applyTo(r2.applyInverseTo(u)), r3.applyTo(u));
                    checkVector(r1.applyTo(r2.applyInverseTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

    @Test
    void testDoubleVectors() throws MathIllegalArgumentException {

        Well1024a random = new Well1024a(0x180b41cfeeffaf67L);
        UnitSphereRandomVectorGenerator g = new UnitSphereRandomVectorGenerator(3, random);
        for (int i = 0; i < 10; ++i) {
            double[] unit = g.nextVector();
            FieldRotation<Dfp> r = new FieldRotation<>(createVector(unit[0], unit[1], unit[2]),
                                                       createAngle(random.nextDouble()),
                                                       RotationConvention.VECTOR_OPERATOR);

            for (double x = -0.9; x < 0.9; x += 0.4) {
                for (double y = -0.9; y < 0.9; y += 0.4) {
                    for (double z = -0.9; z < 0.9; z += 0.4) {
                        FieldVector3D<Dfp> uds   = createVector(x, y, z);
                        FieldVector3D<Dfp> ruds  = r.applyTo(uds);
                        FieldVector3D<Dfp> rIuds = r.applyInverseTo(uds);
                        Vector3D   u     = new Vector3D(x, y, z);
                        FieldVector3D<Dfp> ru    = r.applyTo(u);
                        FieldVector3D<Dfp> rIu   = r.applyInverseTo(u);
                        Dfp[] ruArray = new Dfp[3];
                        r.applyTo(new double[] { x, y, z}, ruArray);
                        Dfp[] rIuArray = new Dfp[3];
                        r.applyInverseTo(new double[] { x, y, z}, rIuArray);
                        checkVector(ruds, ru);
                        checkVector(ruds, new FieldVector3D<>(ruArray));
                        checkVector(rIuds, rIu);
                        checkVector(rIuds, new FieldVector3D<>(rIuArray));
                    }
                }
            }
        }

    }

    @Test
    void testDoubleRotations() throws MathIllegalArgumentException {

        DfpField field = new DfpField(20);
        Well1024a random = new Well1024a(0x180b41cfeeffaf67L);
        UnitSphereRandomVectorGenerator g = new UnitSphereRandomVectorGenerator(3, random);
        for (int i = 0; i < 10; ++i) {
            double[] unit1 = g.nextVector();
            Rotation r1 = new Rotation(new Vector3D(unit1[0], unit1[1], unit1[2]),
                                      random.nextDouble(), RotationConvention.VECTOR_OPERATOR);
            FieldRotation<Dfp> r1Prime = new FieldRotation<>(field.newDfp(r1.getQ0()),
                                                             field.newDfp(r1.getQ1()),
                                                             field.newDfp(r1.getQ2()),
                                                             field.newDfp(r1.getQ3()),
                                                             false);
            double[] unit2 = g.nextVector();
            FieldRotation<Dfp> r2 = new FieldRotation<>(createVector(unit2[0], unit2[1], unit2[2]),
                                                        createAngle(random.nextDouble()),
                                                        RotationConvention.VECTOR_OPERATOR);

            FieldRotation<Dfp> rA = FieldRotation.applyTo(r1, r2);
            FieldRotation<Dfp> rB = r1Prime.compose(r2, RotationConvention.VECTOR_OPERATOR);
            FieldRotation<Dfp> rC = FieldRotation.applyInverseTo(r1, r2);
            FieldRotation<Dfp> rD = r1Prime.composeInverse(r2, RotationConvention.VECTOR_OPERATOR);

            for (double x = -0.9; x < 0.9; x += 0.4) {
                for (double y = -0.9; y < 0.9; y += 0.4) {
                    for (double z = -0.9; z < 0.9; z += 0.4) {

                        FieldVector3D<Dfp> uds   = createVector(x, y, z);
                        checkVector(r1Prime.applyTo(uds), FieldRotation.applyTo(r1, uds));
                        checkVector(r1Prime.applyInverseTo(uds), FieldRotation.applyInverseTo(r1, uds));
                        checkVector(rA.applyTo(uds), rB.applyTo(uds));
                        checkVector(rA.applyInverseTo(uds), rB.applyInverseTo(uds));
                        checkVector(rC.applyTo(uds), rD.applyTo(uds));
                        checkVector(rC.applyInverseTo(uds), rD.applyInverseTo(uds));

                    }
                }
            }
        }

    }

    @Test
    void testArray() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r = new FieldRotation<>(createAxis(2, -3, 5),
                                                   createAngle(1.7),
                                                   RotationConvention.VECTOR_OPERATOR);

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    FieldVector3D<Dfp> v = r.applyTo(u);
                    Dfp[] out = new Dfp[3];
                    r.applyTo(new Dfp[] { u.getX(), u.getY(), u.getZ() }, out);
                    assertEquals(v.getX().getReal(), out[0].getReal(), 1.0e-10);
                    assertEquals(v.getY().getReal(), out[1].getReal(), 1.0e-10);
                    assertEquals(v.getZ().getReal(), out[2].getReal(), 1.0e-10);
                    r.applyInverseTo(out, out);
                    assertEquals(u.getX().getReal(), out[0].getReal(), 1.0e-10);
                    assertEquals(u.getY().getReal(), out[1].getReal(), 1.0e-10);
                    assertEquals(u.getZ().getReal(), out[2].getReal(), 1.0e-10);
                }
            }
        }

    }

    @Test
    void testApplyInverseTo() throws MathIllegalArgumentException {

        Dfp[] in      = new Dfp[3];
        Dfp[] out     = new Dfp[3];
        Dfp[] rebuilt = new Dfp[3];
        FieldRotation<Dfp> r = new FieldRotation<>(createVector(2, -3, 5),
                                                   createAngle(1.7),
                                                   RotationConvention.VECTOR_OPERATOR);
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<Dfp> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                r.applyInverseTo(r.applyTo(u));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
                in[0] = u.getX();
                in[1] = u.getY();
                in[2] = u.getZ();
                r.applyTo(in, out);
                r.applyInverseTo(out, rebuilt);
                assertEquals(in[0].getReal(), rebuilt[0].getReal(), 1.0e-12);
                assertEquals(in[1].getReal(), rebuilt[1].getReal(), 1.0e-12);
                assertEquals(in[2].getReal(), rebuilt[2].getReal(), 1.0e-12);
            }
        }

        r = createRotation(1, 0, 0, 0, false);
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<Dfp> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
            }
        }

        r = new FieldRotation<>(createVector(0, 0, 1), createAngle(FastMath.PI), RotationConvention.VECTOR_OPERATOR);
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<Dfp> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
            }
        }

    }

    @Test
    void testIssue639() throws MathRuntimeException{
        FieldVector3D<Dfp> u1 = createVector(-1321008684645961.0 /  268435456.0,
                                   -5774608829631843.0 /  268435456.0,
                                   -3822921525525679.0 / 4294967296.0);
        FieldVector3D<Dfp> u2 =createVector( -5712344449280879.0 /    2097152.0,
                                   -2275058564560979.0 /    1048576.0,
                                   4423475992255071.0 /      65536.0);
        FieldRotation<Dfp> rot = new FieldRotation<>(u1, u2, createVector(1, 0, 0),createVector(0, 0, 1));
        assertEquals( 0.6228370359608200639829222, rot.getQ0().getReal(), 1.0e-15);
        assertEquals( 0.0257707621456498790029987, rot.getQ1().getReal(), 1.0e-15);
        assertEquals(-0.0000000002503012255839931, rot.getQ2().getReal(), 1.0e-15);
        assertEquals(-0.7819270390861109450724902, rot.getQ3().getReal(), 1.0e-15);
    }

    @Test
    void testIssue801() throws MathRuntimeException {
        FieldVector3D<Dfp> u1 = createVector(0.9999988431610581, -0.0015210774290851095, 0.0);
        FieldVector3D<Dfp> u2 = createVector(0.0, 0.0, 1.0);

        FieldVector3D<Dfp> v1 = createVector(0.9999999999999999, 0.0, 0.0);
        FieldVector3D<Dfp> v2 = createVector(0.0, 0.0, -1.0);

        FieldRotation<Dfp> quat = new FieldRotation<>(u1, u2, v1, v2);
        double q2 = quat.getQ0().getReal() * quat.getQ0().getReal() +
                    quat.getQ1().getReal() * quat.getQ1().getReal() +
                    quat.getQ2().getReal() * quat.getQ2().getReal() +
                    quat.getQ3().getReal() * quat.getQ3().getReal();
        assertEquals(1.0, q2, 1.0e-14);
        assertEquals(0.0, FieldVector3D.angle(v1, quat.applyTo(u1)).getReal(), 1.0e-14);
        assertEquals(0.0, FieldVector3D.angle(v2, quat.applyTo(u2)).getReal(), 1.0e-14);

    }

    private void checkAngle(Dfp a1, double a2) {
        assertEquals(a1.getReal(), MathUtils.normalizeAngle(a2, a1.getReal()), 1.0e-10);
    }

    private void checkRotationDS(FieldRotation<Dfp> r, double q0, double q1, double q2, double q3) {
        FieldRotation<Dfp> rPrime = createRotation(q0, q1, q2, q3, false);
        assertEquals(0, FieldRotation.distance(r, rPrime).getReal(), 1.0e-12);
    }

    private FieldRotation<Dfp> createRotation(double q0, double q1, double q2, double q3,
                                      boolean needsNormalization) {
        return new FieldRotation<>(field.newDfp(q0),
                                   field.newDfp(q1),
                                   field.newDfp(q2),
                                   field.newDfp(q3),
                                   needsNormalization);
    }

    private FieldRotation<Dfp> createRotation(double[][] m, double threshold) {
        Dfp[][] mds = new Dfp[m.length][m[0].length];
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[i].length; ++j) {
                mds[i][j] = field.newDfp(m[i][j]);
            }
        }
        return new FieldRotation<>(mds, threshold);
    }

    private FieldVector3D<Dfp> createVector(double x, double y, double z) {
        return new FieldVector3D<>(field.newDfp(x), field.newDfp(y), field.newDfp(z));
    }

    private FieldVector3D<Dfp> createAxis(double x, double y, double z) {
        return new FieldVector3D<>(field.newDfp(x), field.newDfp(y), field.newDfp(z));
    }

    private Dfp createAngle(double alpha) {
        return field.newDfp(alpha);
    }

    private void checkVector(FieldVector3D<Dfp> u, FieldVector3D<Dfp> v) {
        assertEquals(u.getX().getReal(), v.getX().getReal(), 1.0e-12);
        assertEquals(u.getY().getReal(), v.getY().getReal(), 1.0e-12);
        assertEquals(u.getZ().getReal(), v.getZ().getReal(), 1.0e-12);
    }

    @BeforeEach
    void setUp() {
        field = new DfpField(20);
    }

    @AfterEach
    void tearDown() {
        field = null;
    }

    private DfpField field;

}
