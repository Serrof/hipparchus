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
package org.hipparchus.distribution.multivariate;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.linear.Array2DRowRealMatrix;
import org.hipparchus.linear.EigenDecompositionSymmetric;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well19937c;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.Precision;

/**
 * Implementation of the multivariate normal (Gaussian) distribution.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Multivariate_normal_distribution">
 * Multivariate normal distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/MultivariateNormalDistribution.html">
 * Multivariate normal distribution (MathWorld)</a>
 */
public class MultivariateNormalDistribution
    extends AbstractMultivariateRealDistribution {
    /** Default singular matrix tolerance check value **/
    private static final double DEFAULT_TOLERANCE = Precision.EPSILON;

    /** Vector of means. */
    private final double[] means;
    /** Covariance matrix. */
    private final RealMatrix covarianceMatrix;
    /** The matrix inverse of the covariance matrix. */
    private final RealMatrix covarianceMatrixInverse;
    /** The determinant of the covariance matrix. */
    private final double covarianceMatrixDeterminant;
    /** Matrix used in computation of samples. */
    private final RealMatrix samplingMatrix;
    /** Inverse singular check tolerance when testing if invertable **/
    private final double singularMatrixCheckTolerance;

    /**
     * Creates a multivariate normal distribution with the given mean vector and
     * covariance matrix.<br>
     * The number of dimensions is equal to the length of the mean vector
     * and to the number of rows and columns of the covariance matrix.
     * It is frequently written as "p" in formulae.
     * <p>
     * <b>Note:</b> this constructor will implicitly create an instance of
     * {@link Well19937c} as random generator to be used for sampling only (see
     * {@link #sample()} and {@link #sample(int)}). In case no sampling is
     * needed for the created distribution, it is advised to pass {@code null}
     * as random generator via the appropriate constructors to avoid the
     * additional initialisation overhead.
     *
     * @param means Vector of means.
     * @param covariances Covariance matrix.
     * @throws MathIllegalArgumentException if the arrays length are
     * inconsistent.
     * @throws MathIllegalArgumentException if the eigenvalue decomposition cannot
     * be performed on the provided covariance matrix.
     * @throws MathIllegalArgumentException if any of the eigenvalues is
     * negative.
     */
    public MultivariateNormalDistribution(final double[] means,
                                          final double[][] covariances)
        throws MathIllegalArgumentException {
        this(means, covariances, DEFAULT_TOLERANCE);
    }

    /**
     * Creates a multivariate normal distribution with the given mean vector and
     * covariance matrix.<br>
     * The number of dimensions is equal to the length of the mean vector
     * and to the number of rows and columns of the covariance matrix.
     * It is frequently written as "p" in formulae.
     * <p>
     * <b>Note:</b> this constructor will implicitly create an instance of
     * {@link Well19937c} as random generator to be used for sampling only (see
     * {@link #sample()} and {@link #sample(int)}). In case no sampling is
     * needed for the created distribution, it is advised to pass {@code null}
     * as random generator via the appropriate constructors to avoid the
     * additional initialisation overhead.
     *
     * @param means Vector of means.
     * @param covariances Covariance matrix.
     * @param singularMatrixCheckTolerance Tolerance used during the singular matrix check before inversion
     * @throws MathIllegalArgumentException if the arrays length are
     * inconsistent.
     * @throws MathIllegalArgumentException if the eigenvalue decomposition cannot
     * be performed on the provided covariance matrix.
     * @throws MathIllegalArgumentException if any of the eigenvalues is
     * negative.
     */
    public MultivariateNormalDistribution(final double[] means,
                                          final double[][] covariances,
                                          final double singularMatrixCheckTolerance)
        throws MathIllegalArgumentException {
        this(new Well19937c(), means, covariances, singularMatrixCheckTolerance);
    }


    /**
     * Creates a multivariate normal distribution with the given mean vector and
     * covariance matrix.
     * <br>
     * The number of dimensions is equal to the length of the mean vector
     * and to the number of rows and columns of the covariance matrix.
     * It is frequently written as "p" in formulae.
     *
     * @param rng Random Number Generator.
     * @param means Vector of means.
     * @param covariances Covariance matrix.
     * @throws MathIllegalArgumentException if the arrays length are
     * inconsistent.
     * @throws MathIllegalArgumentException if the eigenvalue decomposition cannot
     * be performed on the provided covariance matrix.
     * @throws MathIllegalArgumentException if any of the eigenvalues is
     * negative.
     */
    public MultivariateNormalDistribution(RandomGenerator rng,
                                          final double[] means,
                                          final double[][] covariances) {
        this(rng, means, covariances, DEFAULT_TOLERANCE);
    }

    /**
     * Creates a multivariate normal distribution with the given mean vector and
     * covariance matrix.
     * <br>
     * The number of dimensions is equal to the length of the mean vector
     * and to the number of rows and columns of the covariance matrix.
     * It is frequently written as "p" in formulae.
     *
     * @param rng Random Number Generator.
     * @param means Vector of means.
     * @param covariances Covariance matrix.
     * @param singularMatrixCheckTolerance Tolerance used during the singular matrix check before inversion
     * @throws MathIllegalArgumentException if the arrays length are
     * inconsistent.
     * @throws MathIllegalArgumentException if the eigenvalue decomposition cannot
     * be performed on the provided covariance matrix.
     * @throws MathIllegalArgumentException if any of the eigenvalues is
     * negative.
     */
    public MultivariateNormalDistribution(RandomGenerator rng,
                                          final double[] means,
                                          final double[][] covariances,
                                          final double singularMatrixCheckTolerance)
            throws MathIllegalArgumentException {
        super(rng, means.length);

        final int dim = means.length;

        if (covariances.length != dim) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                   covariances.length, dim);
        }

        for (int i = 0; i < dim; i++) {
            if (dim != covariances[i].length) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                       covariances[i].length, dim);
            }
        }

        this.means = means.clone();
        this.singularMatrixCheckTolerance = singularMatrixCheckTolerance;

        covarianceMatrix = new Array2DRowRealMatrix(covariances);

        // Covariance matrix eigen decomposition.
        final EigenDecompositionSymmetric covMatDec =
                        new EigenDecompositionSymmetric(covarianceMatrix, singularMatrixCheckTolerance, true);

        // Compute and store the inverse.
        covarianceMatrixInverse = covMatDec.getSolver().getInverse();
        // Compute and store the determinant.
        covarianceMatrixDeterminant = covMatDec.getDeterminant();

        // Eigenvalues of the covariance matrix.
        final double[] covMatEigenvalues = covMatDec.getEigenvalues();

        for (double covMatEigenvalue : covMatEigenvalues) {
            if (covMatEigenvalue < 0) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.NOT_POSITIVE_DEFINITE_MATRIX);
            }
        }

        // Matrix where each column is an eigenvector of the covariance matrix.
        final Array2DRowRealMatrix covMatEigenvectors = new Array2DRowRealMatrix(dim, dim);
        for (int v = 0; v < dim; v++) {
            final double[] evec = covMatDec.getEigenvector(v).toArray();
            covMatEigenvectors.setColumn(v, evec);
        }

        final RealMatrix tmpMatrix = covMatEigenvectors.transpose();

        // Scale each eigenvector by the square root of its eigenvalue.
        for (int row = 0; row < dim; row++) {
            final double factor = FastMath.sqrt(covMatEigenvalues[row]);
            for (int col = 0; col < dim; col++) {
                tmpMatrix.multiplyEntry(row, col, factor);
            }
        }

        samplingMatrix = covMatEigenvectors.multiply(tmpMatrix);
    }

    /**
     * Gets the mean vector.
     *
     * @return the mean vector.
     */
    public double[] getMeans() {
        return means.clone();
    }

    /**
     * Gets the covariance matrix.
     *
     * @return the covariance matrix.
     */
    public RealMatrix getCovariances() {
        return covarianceMatrix.copy();
    }

    /**
     * Gets the current setting for the tolerance check used during singular checks before inversion
     * @return tolerance
     */
    public double getSingularMatrixCheckTolerance() { return singularMatrixCheckTolerance; }

    /** {@inheritDoc} */
    @Override
    public double density(final double[] vals) throws MathIllegalArgumentException {
        final int dim = getDimension();
        if (vals.length != dim) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                   vals.length, dim);
        }

        return FastMath.pow(2 * FastMath.PI, -0.5 * dim) *
            FastMath.pow(covarianceMatrixDeterminant, -0.5) *
            getExponentTerm(vals);
    }

    /**
     * Gets the square root of each element on the diagonal of the covariance
     * matrix.
     *
     * @return the standard deviations.
     */
    public double[] getStandardDeviations() {
        final int dim = getDimension();
        final double[] std = new double[dim];
        final double[][] s = covarianceMatrix.getData();
        for (int i = 0; i < dim; i++) {
            std[i] = FastMath.sqrt(s[i][i]);
        }
        return std;
    }

    /** {@inheritDoc} */
    @Override
    public double[] sample() {
        final int dim = getDimension();
        final double[] normalVals = new double[dim];

        for (int i = 0; i < dim; i++) {
            normalVals[i] = random.nextGaussian();
        }

        final double[] vals = samplingMatrix.operate(normalVals);

        for (int i = 0; i < dim; i++) {
            vals[i] += means[i];
        }

        return vals;
    }

    /**
     * Computes the term used in the exponent (see definition of the distribution).
     *
     * @param values Values at which to compute density.
     * @return the multiplication factor of density calculations.
     */
    private double getExponentTerm(final double[] values) {
        final double[] centered = new double[values.length];
        for (int i = 0; i < centered.length; i++) {
            centered[i] = values[i] - getMeans()[i];
        }
        final double[] preMultiplied = covarianceMatrixInverse.preMultiply(centered);
        double sum = 0;
        for (int i = 0; i < preMultiplied.length; i++) {
            sum += preMultiplied[i] * centered[i];
        }
        return FastMath.exp(-0.5 * sum);
    }
}
