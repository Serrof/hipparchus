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
package org.hipparchus.stat.inference;

import org.hipparchus.distribution.continuous.ChiSquaredDistribution;
import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.MathIllegalStateException;
import org.hipparchus.exception.NullArgumentException;
import org.hipparchus.stat.LocalizedStatFormats;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathArrays;
import org.hipparchus.util.MathUtils;

/**
 * Implements Chi-Square test statistics.
 * <p>
 * This implementation handles both known and unknown distributions.
 * <p>
 * Two samples tests can be used when the distribution is unknown <i>a priori</i>
 * but provided by one sample, or when the hypothesis under test is that the two
 * samples come from the same underlying distribution.
 */
public class ChiSquareTest { // NOPMD - this is not a Junit test class, PMD false positive here

    /** Empty constructor.
     * <p>
     * This constructor is not strictly necessary, but it prevents spurious
     * javadoc warnings with JDK 18 and later.
     * </p>
     * @since 3.0
     */
    public ChiSquareTest() { // NOPMD - unnecessary constructor added intentionally to make javadoc happy
        // nothing to do
    }

    /**
     * Computes the <a href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda35f.htm">
     * Chi-Square statistic</a> comparing <code>observed</code> and <code>expected</code>
     * frequency counts.
     * <p>
     * This statistic can be used to perform a Chi-Square test evaluating the null
     * hypothesis that the observed counts follow the expected distribution.
     * <p>
     * <strong>Preconditions</strong>:
     * <ul>
     * <li>Expected counts must all be positive.</li>
     * <li>Observed counts must all be &ge; 0.</li>
     * <li>The observed and expected arrays must have the same length and
     * their common length must be at least 2.</li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     * <p>
     * <strong>Note: </strong>This implementation rescales the
     * <code>expected</code> array if necessary to ensure that the sum of the
     * expected and observed counts are equal.
     *
     * @param observed array of observed frequency counts
     * @param expected array of expected frequency counts
     * @return chiSquare test statistic
     * @throws MathIllegalArgumentException if <code>observed</code> has negative entries
     * @throws MathIllegalArgumentException if <code>expected</code> has entries that are
     * not strictly positive
     * @throws MathIllegalArgumentException if the arrays length is less than 2
     */
    public double chiSquare(final double[] expected, final long[] observed)
        throws MathIllegalArgumentException {

        if (expected.length < 2) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                   expected.length, 2);
        }
        MathUtils.checkDimension(expected.length, observed.length);
        MathArrays.checkPositive(expected);
        MathArrays.checkNonNegative(observed);

        double sumExpected = 0d;
        double sumObserved = 0d;
        for (int i = 0; i < observed.length; i++) {
            sumExpected += expected[i];
            sumObserved += observed[i];
        }
        double ratio = 1.0d;
        boolean rescale = false;
        if (FastMath.abs(sumExpected - sumObserved) > 10E-6) {
            ratio = sumObserved / sumExpected;
            rescale = true;
        }
        double sumSq = 0.0d;
        for (int i = 0; i < observed.length; i++) {
            if (rescale) {
                final double dev = observed[i] - ratio * expected[i];
                sumSq += dev * dev / (ratio * expected[i]);
            } else {
                final double dev = observed[i] - expected[i];
                sumSq += dev * dev / expected[i];
            }
        }
        return sumSq;
    }

    /**
     * Returns the <i>observed significance level</i>, or <a href=
     * "http://www.cas.lancs.ac.uk/glossary_v1.1/hyptest.html#pvalue">
     * p-value</a>, associated with a
     * <a href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda35f.htm">
     * Chi-square goodness of fit test</a> comparing the <code>observed</code>
     * frequency counts to those in the <code>expected</code> array.
     * <p>
     * The number returned is the smallest significance level at which one can reject
     * the null hypothesis that the observed counts conform to the frequency distribution
     * described by the expected counts.
     * <p>
     * <strong>Preconditions</strong>:
     * <ul>
     * <li>Expected counts must all be positive.</li>
     * <li>Observed counts must all be &ge; 0.</li>
     * <li>The observed and expected arrays must have the same length and
     * their common length must be at least 2.</li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     * <p>
     * <strong>Note: </strong>This implementation rescales the
     * <code>expected</code> array if necessary to ensure that the sum of the
     * expected and observed counts are equal.
     *
     * @param observed array of observed frequency counts
     * @param expected array of expected frequency counts
     * @return p-value
     * @throws MathIllegalArgumentException if <code>observed</code> has negative entries
     * @throws MathIllegalArgumentException if <code>expected</code> has entries that are
     * not strictly positive
     * @throws MathIllegalArgumentException if the arrays length is less than 2
     * @throws MathIllegalStateException if an error occurs computing the p-value
     */
    public double chiSquareTest(final double[] expected, final long[] observed)
        throws MathIllegalArgumentException, MathIllegalStateException {

        final ChiSquaredDistribution distribution = new ChiSquaredDistribution(expected.length - 1.0);
        return 1.0 - distribution.cumulativeProbability(chiSquare(expected, observed));
    }

    /**
     * Performs a <a href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda35f.htm">
     * Chi-square goodness of fit test</a> evaluating the null hypothesis that the
     * observed counts conform to the frequency distribution described by the expected
     * counts, with significance level <code>alpha</code>.  Returns true iff the null
     * hypothesis can be rejected with 100 * (1 - alpha) percent confidence.
     * <p>
     * <strong>Example:</strong><br>
     * To test the hypothesis that <code>observed</code> follows
     * <code>expected</code> at the 99% level, use
     * <code>chiSquareTest(expected, observed, 0.01)</code>
     * <p>
     * <strong>Preconditions</strong>:
     * <ul>
     * <li>Expected counts must all be positive.</li>
     * <li>Observed counts must all be &ge; 0.</li>
     * <li>The observed and expected arrays must have the same length and
     * their common length must be at least 2.</li>
     * <li><code> 0 &lt; alpha &lt; 0.5</code></li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     * <p>
     * <strong>Note: </strong>This implementation rescales the
     * <code>expected</code> array if necessary to ensure that the sum of the
     * expected and observed counts are equal.
     *
     * @param observed array of observed frequency counts
     * @param expected array of expected frequency counts
     * @param alpha significance level of the test
     * @return true iff null hypothesis can be rejected with confidence
     * 1 - alpha
     * @throws MathIllegalArgumentException if <code>observed</code> has negative entries
     * @throws MathIllegalArgumentException if <code>expected</code> has entries that are
     * not strictly positive
     * @throws MathIllegalArgumentException if the arrays length is less than 2
     * @throws MathIllegalArgumentException if <code>alpha</code> is not in the range (0, 0.5]
     * @throws MathIllegalStateException if an error occurs computing the p-value
     */
    public boolean chiSquareTest(final double[] expected, final long[] observed,
                                 final double alpha)
        throws MathIllegalArgumentException, MathIllegalStateException {

        if ((alpha <= 0) || (alpha > 0.5)) {
            throw new MathIllegalArgumentException(LocalizedStatFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL,
                                          alpha, 0, 0.5);
        }
        return chiSquareTest(expected, observed) < alpha;

    }

    /**
     * Computes the Chi-Square statistic associated with a
     * <a href="http://www.itl.nist.gov/div898/handbook/prc/section4/prc45.htm">
     * chi-square test of independence</a> based on the input <code>counts</code>
     * array, viewed as a two-way table.
     * <p>
     * The rows of the 2-way table are
     * <code>count[0], ... , count[count.length - 1] </code>
     * <p>
     * <strong>Preconditions</strong>:
     * <ul>
     * <li>All counts must be &ge; 0.</li>
     * <li>The count array must be rectangular (i.e. all count[i] subarrays
     * must have the same length).</li>
     * <li>The 2-way table represented by <code>counts</code> must have at
     * least 2 columns and at least 2 rows.</li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param counts array representation of 2-way table
     * @return chiSquare test statistic
     * @throws NullArgumentException if the array is null
     * @throws MathIllegalArgumentException if the array is not rectangular
     * @throws MathIllegalArgumentException if {@code counts} has negative entries
     */
    public double chiSquare(final long[][] counts)
        throws MathIllegalArgumentException, NullArgumentException {

        checkArray(counts);
        int nRows = counts.length;
        int nCols = counts[0].length;

        // compute row, column and total sums
        double[] rowSum = new double[nRows];
        double[] colSum = new double[nCols];
        double total = 0.0d;
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                rowSum[row] += counts[row][col];
                colSum[col] += counts[row][col];
                total += counts[row][col];
            }
        }

        // compute expected counts and chi-square
        double sumSq = 0.0d;
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                final double expected = (rowSum[row] * colSum[col]) / total;
                sumSq += ((counts[row][col] - expected) *
                        (counts[row][col] - expected)) / expected;
            }
        }
        return sumSq;
    }

    /**
     * Returns the <i>observed significance level</i>, or <a href=
     * "http://www.cas.lancs.ac.uk/glossary_v1.1/hyptest.html#pvalue">
     * p-value</a>, associated with a
     * <a href="http://www.itl.nist.gov/div898/handbook/prc/section4/prc45.htm">
     * chi-square test of independence</a> based on the input <code>counts</code>
     * array, viewed as a two-way table.
     * <p>
     * The rows of the 2-way table are
     * <code>count[0], ... , count[count.length - 1] </code>
     * <p>
     * <strong>Preconditions</strong>:
     * <ul>
     * <li>All counts must be &ge; 0.</li>
     * <li>The count array must be rectangular (i.e. all count[i] subarrays must have
     * the same length).</li>
     * <li>The 2-way table represented by <code>counts</code> must have at least 2
     * columns and at least 2 rows.</li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param counts array representation of 2-way table
     * @return p-value
     * @throws NullArgumentException if the array is null
     * @throws MathIllegalArgumentException if the array is not rectangular
     * @throws MathIllegalArgumentException if {@code counts} has negative entries
     * @throws MathIllegalStateException if an error occurs computing the p-value
     */
    public double chiSquareTest(final long[][] counts)
        throws MathIllegalArgumentException, NullArgumentException, MathIllegalStateException {

        checkArray(counts);
        double df = ((double) counts.length -1) * ((double) counts[0].length - 1);
        final ChiSquaredDistribution distribution = new ChiSquaredDistribution(df);
        return 1 - distribution.cumulativeProbability(chiSquare(counts));
    }

    /**
     * Performs a <a href="http://www.itl.nist.gov/div898/handbook/prc/section4/prc45.htm">
     * chi-square test of independence</a> evaluating the null hypothesis that the
     * classifications represented by the counts in the columns of the input 2-way table
     * are independent of the rows, with significance level <code>alpha</code>.
     * Returns true iff the null hypothesis can be rejected with 100 * (1 - alpha) percent
     * confidence.
     * <p>
     * The rows of the 2-way table are
     * <code>count[0], ... , count[count.length - 1] </code>
     * <p>
     * <strong>Example:</strong><br>
     * To test the null hypothesis that the counts in
     * <code>count[0], ... , count[count.length - 1] </code>
     * all correspond to the same underlying probability distribution at the 99% level,
     * use <code>chiSquareTest(counts, 0.01)</code>.
     * <p>
     * <strong>Preconditions</strong>:
     * <ul>
     * <li>All counts must be &ge; 0.</li>
     * <li>The count array must be rectangular (i.e. all count[i] subarrays must have the
     * same length).</li>
     * <li>The 2-way table represented by <code>counts</code> must have at least 2 columns and
     * at least 2 rows.</li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param counts array representation of 2-way table
     * @param alpha significance level of the test
     * @return true iff null hypothesis can be rejected with confidence
     * 1 - alpha
     * @throws NullArgumentException if the array is null
     * @throws MathIllegalArgumentException if the array is not rectangular
     * @throws MathIllegalArgumentException if {@code counts} has any negative entries
     * @throws MathIllegalArgumentException if <code>alpha</code> is not in the range (0, 0.5]
     * @throws MathIllegalStateException if an error occurs computing the p-value
     */
    public boolean chiSquareTest(final long[][] counts, final double alpha)
        throws MathIllegalArgumentException, NullArgumentException, MathIllegalStateException {

        if ((alpha <= 0) || (alpha > 0.5)) {
            throw new MathIllegalArgumentException(LocalizedStatFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL,
                                          alpha, 0, 0.5);
        }
        return chiSquareTest(counts) < alpha;
    }

    /**
     * Computes a
     * <a href="http://www.itl.nist.gov/div898/software/dataplot/refman1/auxillar/chi2samp.htm">
     * Chi-Square two sample test statistic</a> comparing bin frequency counts
     * in <code>observed1</code> and <code>observed2</code>.
     * <p>
     * The sums of frequency counts in the two samples are not required to be the
     * same. The formula used to compute the test statistic is
     * </p>
     * <code>
     * &sum;[(K * observed1[i] - observed2[i]/K)<sup>2</sup> / (observed1[i] + observed2[i])]
     * </code>
     * <p>
     * where
     * </p>
     * <code>K = √[&sum;(observed2 / &sum;(observed1)]</code>
     * <p>
     * This statistic can be used to perform a Chi-Square test evaluating the
     * null hypothesis that both observed counts follow the same distribution.
     * </p>
     * <p><strong>Preconditions</strong>:</p>
     * <ul>
     * <li>Observed counts must be non-negative.</li>
     * <li>Observed counts for a specific bin must not both be zero.</li>
     * <li>Observed counts for a specific sample must not all be 0.</li>
     * <li>The arrays <code>observed1</code> and <code>observed2</code> must have
     * the same length and their common length must be at least 2.</li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     * </p>
     *
     * @param observed1 array of observed frequency counts of the first data set
     * @param observed2 array of observed frequency counts of the second data set
     * @return chiSquare test statistic
     * @throws MathIllegalArgumentException the the length of the arrays does not match
     * @throws MathIllegalArgumentException if any entries in <code>observed1</code> or
     * <code>observed2</code> are negative
     * @throws MathIllegalArgumentException if either all counts of <code>observed1</code> or
     * <code>observed2</code> are zero, or if the count at some index is zero
     * for both arrays
     */
    public double chiSquareDataSetsComparison(long[] observed1, long[] observed2)
        throws MathIllegalArgumentException {

        // Make sure lengths are same
        if (observed1.length < 2) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                   observed1.length, 2);
        }
        MathUtils.checkDimension(observed1.length, observed2.length);

        // Ensure non-negative counts
        MathArrays.checkNonNegative(observed1);
        MathArrays.checkNonNegative(observed2);

        // Compute and compare count sums
        long countSum1 = 0;
        long countSum2 = 0;
        for (int i = 0; i < observed1.length; i++) {
            countSum1 += observed1[i];
            countSum2 += observed2[i];
        }
        // Ensure neither sample is uniformly 0
        if (countSum1 == 0 || countSum2 == 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.ZERO_NOT_ALLOWED);
        }
        // Compare and compute weight only if different
        double weight = 0.0;
        boolean unequalCounts = countSum1 != countSum2;
        if (unequalCounts) {
            weight = FastMath.sqrt(((double) countSum1) / countSum2);
        }
        // Compute ChiSquare statistic
        double sumSq = 0.0d;
        for (int i = 0; i < observed1.length; i++) {
            if (observed1[i] == 0 && observed2[i] == 0) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.OBSERVED_COUNTS_BOTTH_ZERO_FOR_ENTRY, i);
            } else {
                final double obs1 = observed1[i];
                final double obs2 = observed2[i];
                final double dev;
                if (unequalCounts) { // apply weights
                    dev = obs1/weight - obs2 * weight;
                } else {
                    dev = obs1 - obs2;
                }
                sumSq += (dev * dev) / (obs1 + obs2);
            }
        }
        return sumSq;
    }

    /**
     * Returns the <i>observed significance level</i>, or <a href=
     * "http://www.cas.lancs.ac.uk/glossary_v1.1/hyptest.html#pvalue">
     * p-value</a>, associated with a Chi-Square two sample test comparing
     * bin frequency counts in <code>observed1</code> and
     * <code>observed2</code>.
     * <p>
     * The number returned is the smallest significance level at which one
     * can reject the null hypothesis that the observed counts conform to the
     * same distribution.
     * <p>
     * See {@link #chiSquareDataSetsComparison(long[], long[])} for details
     * on the formula used to compute the test statistic. The degrees of
     * of freedom used to perform the test is one less than the common length
     * of the input observed count arrays.
     * <p>
     * <strong>Preconditions</strong>:
     * <ul>
     * <li>Observed counts must be non-negative.</li>
     * <li>Observed counts for a specific bin must not both be zero.</li>
     * <li>Observed counts for a specific sample must not all be 0.</li>
     * <li>The arrays <code>observed1</code> and <code>observed2</code> must
     * have the same length and their common length must be at least 2.</li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param observed1 array of observed frequency counts of the first data set
     * @param observed2 array of observed frequency counts of the second data set
     * @return p-value
     * @throws MathIllegalArgumentException the the length of the arrays does not match
     * @throws MathIllegalArgumentException if any entries in <code>observed1</code> or
     * <code>observed2</code> are negative
     * @throws MathIllegalArgumentException if either all counts of <code>observed1</code> or
     * <code>observed2</code> are zero, or if the count at the same index is zero
     * for both arrays
     * @throws MathIllegalStateException if an error occurs computing the p-value
     */
    public double chiSquareTestDataSetsComparison(long[] observed1, long[] observed2)
        throws MathIllegalArgumentException,
        MathIllegalStateException {

        final ChiSquaredDistribution distribution =
                new ChiSquaredDistribution((double) observed1.length - 1);
        return 1 - distribution.cumulativeProbability(
                chiSquareDataSetsComparison(observed1, observed2));
    }

    /**
     * Performs a Chi-Square two sample test comparing two binned data
     * sets. The test evaluates the null hypothesis that the two lists of
     * observed counts conform to the same frequency distribution, with
     * significance level <code>alpha</code>.  Returns true iff the null
     * hypothesis can be rejected with 100 * (1 - alpha) percent confidence.
     * <p>
     * See {@link #chiSquareDataSetsComparison(long[], long[])} for
     * details on the formula used to compute the Chisquare statistic used
     * in the test. The degrees of of freedom used to perform the test is
     * one less than the common length of the input observed count arrays.
     * <p>
     * <strong>Preconditions</strong>:
     * <ul>
     * <li>Observed counts must be non-negative.</li>
     * <li>Observed counts for a specific bin must not both be zero.</li>
     * <li>Observed counts for a specific sample must not all be 0.</li>
     * <li>The arrays <code>observed1</code> and <code>observed2</code> must
     * have the same length and their common length must be at least 2.</li>
     * <li><code> 0 &lt; alpha &lt; 0.5</code></li>
     * </ul>
     * <p>
     * If any of the preconditions are not met, an
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param observed1 array of observed frequency counts of the first data set
     * @param observed2 array of observed frequency counts of the second data set
     * @param alpha significance level of the test
     * @return true iff null hypothesis can be rejected with confidence
     * 1 - alpha
     * @throws MathIllegalArgumentException the the length of the arrays does not match
     * @throws MathIllegalArgumentException if any entries in <code>observed1</code> or
     * <code>observed2</code> are negative
     * @throws MathIllegalArgumentException if either all counts of <code>observed1</code> or
     * <code>observed2</code> are zero, or if the count at the same index is zero
     * for both arrays
     * @throws MathIllegalArgumentException if <code>alpha</code> is not in the range (0, 0.5]
     * @throws MathIllegalStateException if an error occurs performing the test
     */
    public boolean chiSquareTestDataSetsComparison(final long[] observed1,
                                                   final long[] observed2,
                                                   final double alpha)
        throws MathIllegalArgumentException, MathIllegalStateException {

        if (alpha <= 0 ||
            alpha > 0.5) {
            throw new MathIllegalArgumentException(LocalizedStatFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL,
                                          alpha, 0, 0.5);
        }
        return chiSquareTestDataSetsComparison(observed1, observed2) < alpha;

    }

    /**
     * Checks to make sure that the input long[][] array is rectangular,
     * has at least 2 rows and 2 columns, and has all non-negative entries.
     *
     * @param in input 2-way table to check
     * @throws NullArgumentException if the array is null
     * @throws MathIllegalArgumentException if the array is not valid
     * @throws MathIllegalArgumentException if the array contains any negative entries
     */
    private void checkArray(final long[][] in)
        throws MathIllegalArgumentException, NullArgumentException {

        if (in.length < 2) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                   in.length, 2);
        }

        if (in[0].length < 2) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                   in[0].length, 2);
        }

        MathArrays.checkRectangular(in);
        MathArrays.checkNonNegative(in);
    }

}
