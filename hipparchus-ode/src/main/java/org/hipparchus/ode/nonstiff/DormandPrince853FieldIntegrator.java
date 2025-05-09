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

package org.hipparchus.ode.nonstiff;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.Field;
import org.hipparchus.ode.FieldEquationsMapper;
import org.hipparchus.ode.FieldODEStateAndDerivative;
import org.hipparchus.ode.nonstiff.interpolators.DormandPrince853FieldStateInterpolator;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathArrays;


/**
 * This class implements the 8(5,3) Dormand-Prince integrator for Ordinary
 * Differential Equations.
 *
 * <p>This integrator is an embedded Runge-Kutta integrator
 * of order 8(5,3) used in local extrapolation mode (i.e. the solution
 * is computed using the high order formula) with stepsize control
 * (and automatic step initialization) and continuous output. This
 * method uses 12 functions evaluations per step for integration and 4
 * evaluations for interpolation. However, since the first
 * interpolation evaluation is the same as the first integration
 * evaluation of the next step, we have included it in the integrator
 * rather than in the interpolator and specified the method was an
 * <i>fsal</i>. Hence, despite we have 13 stages here, the cost is
 * really 12 evaluations per step even if no interpolation is done,
 * and the overcost of interpolation is only 3 evaluations.</p>
 *
 * <p>This method is based on an 8(6) method by Dormand and Prince
 * (i.e. order 8 for the integration and order 6 for error estimation)
 * modified by Hairer and Wanner to use a 5th order error estimator
 * with 3rd order correction. This modification was introduced because
 * the original method failed in some cases (wrong steps can be
 * accepted when step size is too large, for example in the
 * Brusselator problem) and also had <i>severe difficulties when
 * applied to problems with discontinuities</i>. This modification is
 * explained in the second edition of the first volume (Nonstiff
 * Problems) of the reference book by Hairer, Norsett and Wanner:
 * <i>Solving Ordinary Differential Equations</i> (Springer-Verlag,
 * ISBN 3-540-56670-8).</p>
 *
 * @param <T> the type of the field elements
 */

public class DormandPrince853FieldIntegrator<T extends CalculusFieldElement<T>>
    extends EmbeddedRungeKuttaFieldIntegrator<T> {

    /** Name of integration scheme. */
    public static final String METHOD_NAME = DormandPrince853Integrator.METHOD_NAME;

    /** Simple constructor.
     * Build an eighth order Dormand-Prince integrator with the given step bounds
     * @param field field to which the time and state vector elements belong
     * @param minStep minimal step (sign is irrelevant, regardless of
     * integration direction, forward or backward), the last step can
     * be smaller than this
     * @param maxStep maximal step (sign is irrelevant, regardless of
     * integration direction, forward or backward), the last step can
     * be smaller than this
     * @param scalAbsoluteTolerance allowed absolute error
     * @param scalRelativeTolerance allowed relative error
     */
    public DormandPrince853FieldIntegrator(final Field<T> field,
                                           final double minStep, final double maxStep,
                                           final double scalAbsoluteTolerance,
                                           final double scalRelativeTolerance) {
        super(field, METHOD_NAME, 12,
              minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);
    }

    /** Simple constructor.
     * Build an eighth order Dormand-Prince integrator with the given step bounds
     * @param field field to which the time and state vector elements belong
     * @param minStep minimal step (sign is irrelevant, regardless of
     * integration direction, forward or backward), the last step can
     * be smaller than this
     * @param maxStep maximal step (sign is irrelevant, regardless of
     * integration direction, forward or backward), the last step can
     * be smaller than this
     * @param vecAbsoluteTolerance allowed absolute error
     * @param vecRelativeTolerance allowed relative error
     */
    public DormandPrince853FieldIntegrator(final Field<T> field,
                                           final double minStep, final double maxStep,
                                           final double[] vecAbsoluteTolerance,
                                           final double[] vecRelativeTolerance) {
        super(field, DormandPrince853Integrator.METHOD_NAME, 12,
              minStep, maxStep, vecAbsoluteTolerance, vecRelativeTolerance);
    }

    /** {@inheritDoc} */
    @Override
    public T[] getC() {

        final T sqrt6 = getField().getOne().newInstance(6).sqrt();

        final T[] c = MathArrays.buildArray(getField(), 15);
        c[ 0] = sqrt6.add(-6).divide(-67.5);
        c[ 1] = sqrt6.add(-6).divide(-45.0);
        c[ 2] = sqrt6.add(-6).divide(-30.0);
        c[ 3] = sqrt6.add( 6).divide( 30.0);
        c[ 4] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 1, 3);
        c[ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 1, 4);
        c[ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 4, 13);
        c[ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 127, 195);
        c[ 8] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 3, 5);
        c[ 9] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 6, 7);
        c[10] = getField().getOne();
        c[11] = getField().getOne();
        c[12] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 1.0, 10.0);
        c[13] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 1.0, 5.0);
        c[14] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),7.0, 9.0);

        return c;

    }

    /** {@inheritDoc} */
    @Override
    public T[][] getA() {

        final T sqrt6 = getField().getOne().newInstance(6).sqrt();

        final T[][] a = MathArrays.buildArray(getField(), 15, -1);
        for (int i = 0; i < a.length; ++i) {
            a[i] = MathArrays.buildArray(getField(), i + 1);
        }

        a[ 0][ 0] = sqrt6.add(-6).divide(-67.5);

        a[ 1][ 0] = sqrt6.add(-6).divide(-180);
        a[ 1][ 1] = sqrt6.add(-6).divide( -60);

        a[ 2][ 0] = sqrt6.add(-6).divide(-120);
        a[ 2][ 1] = getField().getZero();
        a[ 2][ 2] = sqrt6.add(-6).divide( -40);

        a[ 3][ 0] = sqrt6.multiply(107).add(462).divide( 3000);
        a[ 3][ 1] = getField().getZero();
        a[ 3][ 2] = sqrt6.multiply(197).add(402).divide(-1000);
        a[ 3][ 3] = sqrt6.multiply( 73).add(168).divide(  375);

        a[ 4][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 1, 27);
        a[ 4][ 1] = getField().getZero();
        a[ 4][ 2] = getField().getZero();
        a[ 4][ 3] = sqrt6.add( 16).divide( 108);
        a[ 4][ 4] = sqrt6.add(-16).divide(-108);

        a[ 5][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 19, 512);
        a[ 5][ 1] = getField().getZero();
        a[ 5][ 2] = getField().getZero();
        a[ 5][ 3] = sqrt6.multiply( 23).add(118).divide(1024);
        a[ 5][ 4] = sqrt6.multiply(-23).add(118).divide(1024);
        a[ 5][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -9, 512);

        a[ 6][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 13772, 371293);
        a[ 6][ 1] = getField().getZero();
        a[ 6][ 2] = getField().getZero();
        a[ 6][ 3] = sqrt6.multiply( 4784).add(51544).divide(371293);
        a[ 6][ 4] = sqrt6.multiply(-4784).add(51544).divide(371293);
        a[ 6][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -5688, 371293);
        a[ 6][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),  3072, 371293);

        a[ 7][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 58656157643.0, 93983540625.0);
        a[ 7][ 1] = getField().getZero();
        a[ 7][ 2] = getField().getZero();
        a[ 7][ 3] = sqrt6.multiply(-318801444819.0).add(-1324889724104.0).divide(626556937500.0);
        a[ 7][ 4] = sqrt6.multiply( 318801444819.0).add(-1324889724104.0).divide(626556937500.0);
        a[ 7][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 96044563816.0, 3480871875.0);
        a[ 7][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 5682451879168.0, 281950621875.0);
        a[ 7][ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -165125654.0, 3796875.0);

        a[ 8][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),8909899.0, 18653125.0);
        a[ 8][ 1] = getField().getZero();
        a[ 8][ 2] = getField().getZero();
        a[ 8][ 3] = sqrt6.multiply(-1137963.0).add(-4521408.0).divide(2937500.0);
        a[ 8][ 4] = sqrt6.multiply( 1137963.0).add(-4521408.0).divide(2937500.0);
        a[ 8][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 96663078.0, 4553125.0);
        a[ 8][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 2107245056.0, 137915625.0);
        a[ 8][ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -4913652016.0, 147609375.0);
        a[ 8][ 8] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -78894270.0, 3880452869.0);

        a[ 9][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -20401265806.0, 21769653311.0);
        a[ 9][ 1] = getField().getZero();
        a[ 9][ 2] = getField().getZero();
        a[ 9][ 3] = sqrt6.multiply( 94326.0).add(354216.0).divide(112847.0);
        a[ 9][ 4] = sqrt6.multiply(-94326.0).add(354216.0).divide(112847.0);
        a[ 9][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -43306765128.0, 5313852383.0);
        a[ 9][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -20866708358144.0, 1126708119789.0);
        a[ 9][ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 14886003438020.0, 654632330667.0);
        a[ 9][ 8] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 35290686222309375.0, 14152473387134411.0);
        a[ 9][ 9] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -1477884375.0, 485066827.0);

        a[10][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 39815761.0, 17514443.0);
        a[10][ 1] = getField().getZero();
        a[10][ 2] = getField().getZero();
        a[10][ 3] = sqrt6.multiply(-960905.0).add(-3457480.0).divide(551636.0);
        a[10][ 4] = sqrt6.multiply( 960905.0).add(-3457480.0).divide(551636.0);
        a[10][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -844554132.0, 47026969.0);
        a[10][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),8444996352.0, 302158619.0);
        a[10][ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -2509602342.0, 877790785.0);
        a[10][ 8] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -28388795297996250.0, 3199510091356783.0);
        a[10][ 9] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 226716250.0, 18341897.0);
        a[10][10] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 1371316744.0, 2131383595.0);

        // the following stage is both for interpolation and the first stage in next step
        // (the coefficients are identical to the B array)
        a[11][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 104257.0, 1920240.0);
        a[11][ 1] = getField().getZero();
        a[11][ 2] = getField().getZero();
        a[11][ 3] = getField().getZero();
        a[11][ 4] = getField().getZero();
        a[11][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 3399327.0, 763840.0);
        a[11][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 66578432.0, 35198415.0);
        a[11][ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -1674902723.0, 288716400.0);
        a[11][ 8] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 54980371265625.0, 176692375811392.0);
        a[11][ 9] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -734375.0, 4826304.0);
        a[11][10] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 171414593.0, 851261400.0);
        a[11][11] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 137909.0, 3084480.0);

        // the following stages are for interpolation only
        a[12][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),       13481885573.0, 240030000000.0);
        a[12][ 1] = getField().getZero();
        a[12][ 2] = getField().getZero();
        a[12][ 3] = getField().getZero();
        a[12][ 4] = getField().getZero();
        a[12][ 5] = getField().getZero();
        a[12][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),      139418837528.0, 549975234375.0);
        a[12][ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),   -11108320068443.0, 45111937500000.0);
        a[12][ 8] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -1769651421925959.0, 14249385146080000.0);
        a[12][ 9] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),          57799439.0, 377055000.0);
        a[12][10] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),      793322643029.0, 96734250000000.0);
        a[12][11] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),        1458939311.0, 192780000000.0);
        a[12][12]  = FieldExplicitRungeKuttaIntegrator.fraction(getField(),             -4149.0, 500000.0);

        a[13][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),     1595561272731.0, 50120273500000.0);
        a[13][ 1] = getField().getZero();
        a[13][ 2] = getField().getZero();
        a[13][ 3] = getField().getZero();
        a[13][ 4] = getField().getZero();
        a[13][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),      975183916491.0, 34457688031250.0);
        a[13][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),    38492013932672.0, 718912673015625.0);
        a[13][ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), -1114881286517557.0, 20298710767500000.0);
        a[13][ 8] = getField().getZero();
        a[13][ 9] = getField().getZero();
        a[13][10] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),    -2538710946863.0, 23431227861250000.0);
        a[13][11] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),        8824659001.0, 23066716781250.0);
        a[13][12] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),      -11518334563.0, 33831184612500.0);
        a[13][13] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),        1912306948.0, 13532473845.0);

        a[14][ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),      -13613986967.0, 31741908048.0);
        a[14][ 1] = getField().getZero();
        a[14][ 2] = getField().getZero();
        a[14][ 3] = getField().getZero();
        a[14][ 4] = getField().getZero();
        a[14][ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),       -4755612631.0, 1012344804.0);
        a[14][ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),    42939257944576.0, 5588559685701.0);
        a[14][ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),    77881972900277.0, 19140370552944.0);
        a[14][ 8] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),    22719829234375.0, 63689648654052.0);
        a[14][ 9] = getField().getZero();
        a[14][10] = getField().getZero();
        a[14][11] = getField().getZero();
        a[14][12] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),       -1199007803.0, 857031517296.0);
        a[14][13] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),      157882067000.0, 53564469831.0);
        a[14][14] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),     -290468882375.0, 31741908048.0);

        return a;

    }

    /** {@inheritDoc} */
    @Override
    public T[] getB() {
        final T[] b = MathArrays.buildArray(getField(), 16);
        b[ 0] = FieldExplicitRungeKuttaIntegrator.fraction(getField(), 104257, 1920240);
        b[ 1] = getField().getZero();
        b[ 2] = getField().getZero();
        b[ 3] = getField().getZero();
        b[ 4] = getField().getZero();
        b[ 5] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),         3399327.0,          763840.0);
        b[ 6] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),        66578432.0,        35198415.0);
        b[ 7] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),     -1674902723.0,       288716400.0);
        b[ 8] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),  54980371265625.0, 176692375811392.0);
        b[ 9] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),         -734375.0,         4826304.0);
        b[10] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),       171414593.0,       851261400.0);
        b[11] = FieldExplicitRungeKuttaIntegrator.fraction(getField(),          137909.0,         3084480.0);
        b[12] = getField().getZero();
        b[13] = getField().getZero();
        b[14] = getField().getZero();
        b[15] = getField().getZero();
        return b;
    }

    /** {@inheritDoc} */
    @Override
    protected DormandPrince853FieldStateInterpolator<T>
        createInterpolator(final boolean forward, T[][] yDotK,
                           final FieldODEStateAndDerivative<T> globalPreviousState,
                           final FieldODEStateAndDerivative<T> globalCurrentState, final FieldEquationsMapper<T> mapper) {
        return new DormandPrince853FieldStateInterpolator<>(getField(), forward, yDotK,
                                                            globalPreviousState, globalCurrentState,
                                                            globalPreviousState, globalCurrentState,
                                                            mapper);
    }

    /** {@inheritDoc} */
    @Override
    public int getOrder() {
        return 8;
    }

    /** {@inheritDoc} */
    @Override
    protected double estimateError(final T[][] yDotK, final T[] y0, final T[] y1, final T h) {

        final StepsizeHelper helper = getStepSizeHelper();
        double error1 = 0;
        double error2 = 0;

        for (int j = 0; j < helper.getMainSetDimension(); ++j) {
            final double errSum1 = DormandPrince853Integrator.E1_01 * yDotK[ 0][j].getReal() + DormandPrince853Integrator.E1_06 * yDotK[ 5][j].getReal() +
                                   DormandPrince853Integrator.E1_07 * yDotK[ 6][j].getReal() + DormandPrince853Integrator.E1_08 * yDotK[ 7][j].getReal() +
                                   DormandPrince853Integrator.E1_09 * yDotK[ 8][j].getReal() + DormandPrince853Integrator.E1_10 * yDotK[ 9][j].getReal() +
                                   DormandPrince853Integrator.E1_11 * yDotK[10][j].getReal() + DormandPrince853Integrator.E1_12 * yDotK[11][j].getReal();
            final double errSum2 = DormandPrince853Integrator.E2_01 * yDotK[ 0][j].getReal() + DormandPrince853Integrator.E2_06 * yDotK[ 5][j].getReal() +
                                   DormandPrince853Integrator.E2_07 * yDotK[ 6][j].getReal() + DormandPrince853Integrator.E2_08 * yDotK[ 7][j].getReal() +
                                   DormandPrince853Integrator.E2_09 * yDotK[ 8][j].getReal() + DormandPrince853Integrator.E2_10 * yDotK[ 9][j].getReal() +
                                   DormandPrince853Integrator.E2_11 * yDotK[10][j].getReal() + DormandPrince853Integrator.E2_12 * yDotK[11][j].getReal();
            final double tol     = helper.getTolerance(j, FastMath.max(FastMath.abs(y0[j].getReal()), FastMath.abs(y1[j].getReal())));
            final double ratio1  = errSum1 / tol;
            error1        += ratio1 * ratio1;
            final double ratio2  = errSum2 / tol;
            error2        += ratio2 * ratio2;

        }

        double den = error1 + 0.01 * error2;
        if (den <= 0.0) {
            den = 1.0;
        }

        return FastMath.abs(h.getReal()) * error1 / FastMath.sqrt(helper.getMainSetDimension() * den);

    }

}
