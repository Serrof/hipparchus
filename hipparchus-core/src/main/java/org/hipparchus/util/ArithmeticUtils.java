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

import java.math.BigInteger;

import org.hipparchus.exception.Localizable;
import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathRuntimeException;
import org.hipparchus.exception.MathIllegalArgumentException;

/**
 * Some useful, arithmetics related, additions to the built-in functions in
 * {@link Math}.
 */
public final class ArithmeticUtils {

    /** Private constructor. */
    private ArithmeticUtils() {
        super();
    }

    /**
     * Add two integers, checking for overflow.
     *
     * @param x an addend
     * @param y an addend
     * @return the sum {@code x+y}
     * @throws MathRuntimeException if the result can not be represented
     * as an {@code int}.
     */
    public static int addAndCheck(int x, int y)
            throws MathRuntimeException {
        long s = (long) x + (long) y; // NOPMD - casts are intentional here
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new MathRuntimeException(LocalizedCoreFormats.OVERFLOW_IN_ADDITION, x, y);
        }
        return (int)s;
    }

    /**
     * Add two long integers, checking for overflow.
     *
     * @param a an addend
     * @param b an addend
     * @return the sum {@code a+b}
     * @throws MathRuntimeException if the result can not be represented as an long
     */
    public static long addAndCheck(long a, long b) throws MathRuntimeException {
        return addAndCheck(a, b, LocalizedCoreFormats.OVERFLOW_IN_ADDITION);
    }

    /**
     * Computes the greatest common divisor of the absolute value of two
     * numbers, using a modified version of the "binary gcd" method.
     * See Knuth 4.5.2 algorithm B.
     * The algorithm is due to Josef Stein (1961).
     * <br>
     * Special cases:
     * <ul>
     *  <li>The invocations
     *   {@code gcd(Integer.MIN_VALUE, Integer.MIN_VALUE)},
     *   {@code gcd(Integer.MIN_VALUE, 0)} and
     *   {@code gcd(0, Integer.MIN_VALUE)} throw an
     *   {@code ArithmeticException}, because the result would be 2^31, which
     *   is too large for an int value.</li>
     *  <li>The result of {@code gcd(x, x)}, {@code gcd(0, x)} and
     *   {@code gcd(x, 0)} is the absolute value of {@code x}, except
     *   for the special cases above.</li>
     *  <li>The invocation {@code gcd(0, 0)} is the only one which returns
     *   {@code 0}.</li>
     * </ul>
     *
     * @param p Number.
     * @param q Number.
     * @return the greatest common divisor (never negative).
     * @throws MathRuntimeException if the result cannot be represented as
     * a non-negative {@code int} value.
     */
    public static int gcd(int p, int q) throws MathRuntimeException {
        int a = p;
        int b = q;
        if (a == 0 ||
            b == 0) {
            if (a == Integer.MIN_VALUE ||
                b == Integer.MIN_VALUE) {
                throw new MathRuntimeException(LocalizedCoreFormats.GCD_OVERFLOW_32_BITS,
                                               p, q);
            }
            return FastMath.abs(a + b);
        }

        long al = a;
        long bl = b;
        boolean useLong = false;
        if (a < 0) {
            if(Integer.MIN_VALUE == a) {
                useLong = true;
            } else {
                a = -a;
            }
            al = -al;
        }
        if (b < 0) {
            if (Integer.MIN_VALUE == b) {
                useLong = true;
            } else {
                b = -b;
            }
            bl = -bl;
        }
        if (useLong) {
            if(al == bl) {
                throw new MathRuntimeException(LocalizedCoreFormats.GCD_OVERFLOW_32_BITS,
                                               p, q);
            }
            long blbu = bl;
            bl = al;
            al = blbu % al;
            if (al == 0) {
                if (bl > Integer.MAX_VALUE) {
                    throw new MathRuntimeException(LocalizedCoreFormats.GCD_OVERFLOW_32_BITS,
                                                   p, q);
                }
                return (int) bl;
            }
            blbu = bl;

            // Now "al" and "bl" fit in an "int".
            b = (int) al;
            a = (int) (blbu % al);
        }

        return gcdPositive(a, b);
    }

    /**
     * Computes the greatest common divisor of two <em>positive</em> numbers
     * (this precondition is <em>not</em> checked and the result is undefined
     * if not fulfilled) using the "binary gcd" method which avoids division
     * and modulo operations.
     * See Knuth 4.5.2 algorithm B.
     * The algorithm is due to Josef Stein (1961).
     * <p>
     * Special cases:
     * <ul>
     *  <li>The result of {@code gcd(x, x)}, {@code gcd(0, x)} and
     *   {@code gcd(x, 0)} is the value of {@code x}.</li>
     *  <li>The invocation {@code gcd(0, 0)} is the only one which returns
     *   {@code 0}.</li>
     * </ul>
     *
     * @param a Positive number.
     * @param b Positive number.
     * @return the greatest common divisor.
     */
    private static int gcdPositive(int a, int b) {
        if (a == 0) {
            return b;
        }
        else if (b == 0) {
            return a;
        }

        // Make "a" and "b" odd, keeping track of common power of 2.
        final int aTwos = Integer.numberOfTrailingZeros(a);
        a >>= aTwos;
        final int bTwos = Integer.numberOfTrailingZeros(b);
        b >>= bTwos;
        final int shift = FastMath.min(aTwos, bTwos);

        // "a" and "b" are positive.
        // If a > b then "gdc(a, b)" is equal to "gcd(a - b, b)".
        // If a < b then "gcd(a, b)" is equal to "gcd(b - a, a)".
        // Hence, in the successive iterations:
        //  "a" becomes the absolute difference of the current values,
        //  "b" becomes the minimum of the current values.
        while (a != b) {
            final int delta = a - b;
            b = Math.min(a, b);
            a = Math.abs(delta);

            // Remove any power of 2 in "a" ("b" is guaranteed to be odd).
            a >>= Integer.numberOfTrailingZeros(a);
        }

        // Recover the common power of 2.
        return a << shift;
    }

    /**
     * Gets the greatest common divisor of the absolute value of two numbers,
     * using the "binary gcd" method which avoids division and modulo
     * operations. See Knuth 4.5.2 algorithm B. This algorithm is due to Josef
     * Stein (1961).
     * <p>
     * Special cases:
     * <ul>
     * <li>The invocations
     * {@code gcd(Long.MIN_VALUE, Long.MIN_VALUE)},
     * {@code gcd(Long.MIN_VALUE, 0L)} and
     * {@code gcd(0L, Long.MIN_VALUE)} throw an
     * {@code ArithmeticException}, because the result would be 2^63, which
     * is too large for a long value.</li>
     * <li>The result of {@code gcd(x, x)}, {@code gcd(0L, x)} and
     * {@code gcd(x, 0L)} is the absolute value of {@code x}, except
     * for the special cases above.
     * <li>The invocation {@code gcd(0L, 0L)} is the only one which returns
     * {@code 0L}.</li>
     * </ul>
     *
     * @param p Number.
     * @param q Number.
     * @return the greatest common divisor, never negative.
     * @throws MathRuntimeException if the result cannot be represented as
     * a non-negative {@code long} value.
     */
    public static long gcd(final long p, final long q) throws MathRuntimeException {
        long u = p;
        long v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Long.MIN_VALUE) || (v == Long.MIN_VALUE)){
                throw new MathRuntimeException(LocalizedCoreFormats.GCD_OVERFLOW_64_BITS,
                                               p, q);
            }
            return FastMath.abs(u) + FastMath.abs(v);
        }
        // keep u and v negative, as negative integers range down to
        // -2^63, while positive numbers can only be as large as 2^63-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 63) { // while u and v are
                                                            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 63) {
            throw new MathRuntimeException(LocalizedCoreFormats.GCD_OVERFLOW_64_BITS,
                                           p, q);
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        long t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1L << k); // gcd is u*2^k
    }

    /**
     * Returns the least common multiple of the absolute value of two numbers,
     * using the formula {@code lcm(a,b) = (a / gcd(a,b)) * b}.
     * <p>
     * Special cases:
     * <ul>
     * <li>The invocations {@code lcm(Integer.MIN_VALUE, n)} and
     * {@code lcm(n, Integer.MIN_VALUE)}, where {@code abs(n)} is a
     * power of 2, throw an {@code ArithmeticException}, because the result
     * would be 2^31, which is too large for an int value.</li>
     * <li>The result of {@code lcm(0, x)} and {@code lcm(x, 0)} is
     * {@code 0} for any {@code x}.
     * </ul>
     *
     * @param a Number.
     * @param b Number.
     * @return the least common multiple, never negative.
     * @throws MathRuntimeException if the result cannot be represented as
     * a non-negative {@code int} value.
     */
    public static int lcm(int a, int b) throws MathRuntimeException {
        if (a == 0 || b == 0){
            return 0;
        }
        int lcm = FastMath.abs(mulAndCheck(a / gcd(a, b), b));
        if (lcm == Integer.MIN_VALUE) {
            throw new MathRuntimeException(LocalizedCoreFormats.LCM_OVERFLOW_32_BITS, a, b);
        }
        return lcm;
    }

    /**
     * Returns the least common multiple of the absolute value of two numbers,
     * using the formula {@code lcm(a,b) = (a / gcd(a,b)) * b}.
     * <p>
     * Special cases:
     * <ul>
     * <li>The invocations {@code lcm(Long.MIN_VALUE, n)} and
     * {@code lcm(n, Long.MIN_VALUE)}, where {@code abs(n)} is a
     * power of 2, throw an {@code ArithmeticException}, because the result
     * would be 2^63, which is too large for an int value.</li>
     * <li>The result of {@code lcm(0L, x)} and {@code lcm(x, 0L)} is
     * {@code 0L} for any {@code x}.
     * </ul>
     *
     * @param a Number.
     * @param b Number.
     * @return the least common multiple, never negative.
     * @throws MathRuntimeException if the result cannot be represented
     * as a non-negative {@code long} value.
     */
    public static long lcm(long a, long b) throws MathRuntimeException {
        if (a == 0 || b == 0){
            return 0;
        }
        long lcm = FastMath.abs(mulAndCheck(a / gcd(a, b), b));
        if (lcm == Long.MIN_VALUE){
            throw new MathRuntimeException(LocalizedCoreFormats.LCM_OVERFLOW_64_BITS, a, b);
        }
        return lcm;
    }

    /**
     * Multiply two integers, checking for overflow.
     *
     * @param x Factor.
     * @param y Factor.
     * @return the product {@code x * y}.
     * @throws MathRuntimeException if the result can not be
     * represented as an {@code int}.
     */
    public static int mulAndCheck(int x, int y) throws MathRuntimeException {
        long m = ((long) x) * ((long) y);  // NOPMD - casts are intentional here
        if (m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
            throw new MathRuntimeException(LocalizedCoreFormats.ARITHMETIC_EXCEPTION);
        }
        return (int)m;
    }

    /**
     * Multiply two long integers, checking for overflow.
     *
     * @param a Factor.
     * @param b Factor.
     * @return the product {@code a * b}.
     * @throws MathRuntimeException if the result can not be represented
     * as a {@code long}.
     */
    public static long mulAndCheck(long a, long b) throws MathRuntimeException {
        long ret;
        if (a > b) {
            // use symmetry to reduce boundary cases
            ret = mulAndCheck(b, a);
        } else {
            if (a < 0) {
                if (b < 0) {
                    // check for positive overflow with negative a, negative b
                    if (a >= Long.MAX_VALUE / b) {
                        ret = a * b;
                    } else {
                        throw new MathRuntimeException(LocalizedCoreFormats.ARITHMETIC_EXCEPTION);
                    }
                } else if (b > 0) {
                    // check for negative overflow with negative a, positive b
                    if (Long.MIN_VALUE / b <= a) {
                        ret = a * b;
                    } else {
                        throw new MathRuntimeException(LocalizedCoreFormats.ARITHMETIC_EXCEPTION);

                    }
                } else {
                    // assert b == 0
                    ret = 0;
                }
            } else if (a > 0) {
                // assert a > 0
                // assert b > 0

                // check for positive overflow with positive a, positive b
                if (a <= Long.MAX_VALUE / b) {
                    ret = a * b;
                } else {
                    throw new MathRuntimeException(LocalizedCoreFormats.ARITHMETIC_EXCEPTION);
                }
            } else {
                // assert a == 0
                ret = 0;
            }
        }
        return ret;
    }

    /**
     * Subtract two integers, checking for overflow.
     *
     * @param x Minuend.
     * @param y Subtrahend.
     * @return the difference {@code x - y}.
     * @throws MathRuntimeException if the result can not be represented
     * as an {@code int}.
     */
    public static int subAndCheck(int x, int y) throws MathRuntimeException {
        long s = (long) x - (long) y; // NOPMD - casts are intentional here
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new MathRuntimeException(LocalizedCoreFormats.OVERFLOW_IN_SUBTRACTION, x, y);
        }
        return (int)s;
    }

    /**
     * Subtract two long integers, checking for overflow.
     *
     * @param a Value.
     * @param b Value.
     * @return the difference {@code a - b}.
     * @throws MathRuntimeException if the result can not be represented as a
     * {@code long}.
     */
    public static long subAndCheck(long a, long b) throws MathRuntimeException {
        long ret;
        if (b == Long.MIN_VALUE) {
            if (a < 0) {
                ret = a - b;
            } else {
                throw new MathRuntimeException(LocalizedCoreFormats.OVERFLOW_IN_ADDITION, a, -b);
            }
        } else {
            // use additive inverse
            ret = addAndCheck(a, -b, LocalizedCoreFormats.OVERFLOW_IN_ADDITION);
        }
        return ret;
    }

    /**
     * Raise an int to an int power.
     *
     * @param k Number to raise.
     * @param e Exponent (must be positive or zero).
     * @return \( k^e \)
     * @throws MathIllegalArgumentException if {@code e < 0}.
     * @throws MathRuntimeException if the result would overflow.
     */
    public static int pow(final int k,
                          final int e)
        throws MathIllegalArgumentException,
               MathRuntimeException {
        if (e < 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.EXPONENT, e);
        }

        int exp = e;
        int result = 1;
        int k2p    = k;
        while (true) {
            if ((exp & 0x1) != 0) {
                result = mulAndCheck(result, k2p);
            }

            exp >>= 1;
        if (exp == 0) {
            break;
        }

        k2p = mulAndCheck(k2p, k2p);
        }

        return result;
    }

    /**
     * Raise a long to an int power.
     *
     * @param k Number to raise.
     * @param e Exponent (must be positive or zero).
     * @return \( k^e \)
     * @throws MathIllegalArgumentException if {@code e < 0}.
     * @throws MathRuntimeException if the result would overflow.
     */
    public static long pow(final long k,
                           final int e)
        throws MathIllegalArgumentException,
               MathRuntimeException {
        if (e < 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.EXPONENT, e);
        }

        int exp = e;
        long result = 1;
        long k2p    = k;
        while (true) {
            if ((exp & 0x1) != 0) {
                result = mulAndCheck(result, k2p);
            }

            exp >>= 1;
        if (exp == 0) {
            break;
        }

        k2p = mulAndCheck(k2p, k2p);
        }

        return result;
    }

    /**
     * Raise a BigInteger to an int power.
     *
     * @param k Number to raise.
     * @param e Exponent (must be positive or zero).
     * @return k<sup>e</sup>
     * @throws MathIllegalArgumentException if {@code e < 0}.
     */
    public static BigInteger pow(final BigInteger k, int e) throws MathIllegalArgumentException {
        if (e < 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.EXPONENT, e);
        }

        return k.pow(e);
    }

    /**
     * Raise a BigInteger to a long power.
     *
     * @param k Number to raise.
     * @param e Exponent (must be positive or zero).
     * @return k<sup>e</sup>
     * @throws MathIllegalArgumentException if {@code e < 0}.
     */
    public static BigInteger pow(final BigInteger k, long e) throws MathIllegalArgumentException {
        if (e < 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.EXPONENT, e);
        }

        BigInteger result = BigInteger.ONE;
        BigInteger k2p    = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result = result.multiply(k2p);
            }
            k2p = k2p.multiply(k2p);
            e >>= 1;
        }

        return result;

    }

    /**
     * Raise a BigInteger to a BigInteger power.
     *
     * @param k Number to raise.
     * @param e Exponent (must be positive or zero).
     * @return k<sup>e</sup>
     * @throws MathIllegalArgumentException if {@code e < 0}.
     */
    public static BigInteger pow(final BigInteger k, BigInteger e) throws MathIllegalArgumentException {
        if (e.compareTo(BigInteger.ZERO) < 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.EXPONENT, e);
        }

        BigInteger result = BigInteger.ONE;
        BigInteger k2p    = k;
        while (!BigInteger.ZERO.equals(e)) {
            if (e.testBit(0)) {
                result = result.multiply(k2p);
            }
            k2p = k2p.multiply(k2p);
            e = e.shiftRight(1);
        }

        return result;
    }

    /**
     * Add two long integers, checking for overflow.
     *
     * @param a Addend.
     * @param b Addend.
     * @param pattern Pattern to use for any thrown exception.
     * @return the sum {@code a + b}.
     * @throws MathRuntimeException if the result cannot be represented
     * as a {@code long}.
     */
     private static long addAndCheck(long a, long b, Localizable pattern) throws MathRuntimeException {
         final long result = a + b;
         if (!((a ^ b) < 0 || (a ^ result) >= 0)) {
             throw new MathRuntimeException(pattern, a, b);
         }
         return result;
    }

    /**
     * Returns true if the argument is a power of two.
     *
     * @param n the number to test
     * @return true if the argument is a power of two
     */
    public static boolean isPowerOfTwo(long n) {
        return (n > 0) && ((n & (n - 1)) == 0);
    }

    /**
     * Returns the unsigned remainder from dividing the first argument
     * by the second where each argument and the result is interpreted
     * as an unsigned value.
     * <p>
     * This method does not use the {@code long} datatype.
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the unsigned remainder of the first argument divided by
     * the second argument.
     */
    public static int remainderUnsigned(int dividend, int divisor) {
        if (divisor >= 0) {
            if (dividend >= 0) {
                return dividend % divisor;
            }
            // The implementation is a Java port of algorithm described in the book
            // "Hacker's Delight" (section "Unsigned short division from signed division").
            int q = ((dividend >>> 1) / divisor) << 1;
            dividend -= q * divisor;
            if (dividend < 0 || dividend >= divisor) {
                dividend -= divisor;
            }
            return dividend;
        }
        return dividend >= 0 || dividend < divisor ? dividend : dividend - divisor;
    }

    /**
     * Returns the unsigned remainder from dividing the first argument
     * by the second where each argument and the result is interpreted
     * as an unsigned value.
     * <p>
     * This method does not use the {@code BigInteger} datatype.
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the unsigned remainder of the first argument divided by
     * the second argument.
     */
    public static long remainderUnsigned(long dividend, long divisor) {
        if (divisor >= 0L) {
            if (dividend >= 0L) {
                return dividend % divisor;
            }
            // The implementation is a Java port of algorithm described in the book
            // "Hacker's Delight" (section "Unsigned short division from signed division").
            long q = ((dividend >>> 1) / divisor) << 1;
            dividend -= q * divisor;
            if (dividend < 0L || dividend >= divisor) {
                dividend -= divisor;
            }
            return dividend;
        }
        return dividend >= 0L || dividend < divisor ? dividend : dividend - divisor;
    }

    /**
     * Returns the unsigned quotient of dividing the first argument by
     * the second where each argument and the result is interpreted as
     * an unsigned value.
     * <p>
     * Note that in two's complement arithmetic, the three other
     * basic arithmetic operations of add, subtract, and multiply are
     * bit-wise identical if the two operands are regarded as both
     * being signed or both being unsigned. Therefore separate {@code
     * addUnsigned}, etc. methods are not provided.
     * <p>
     * This method does not use the {@code long} datatype.
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the unsigned quotient of the first argument divided by
     * the second argument
     */
    public static int divideUnsigned(int dividend, int divisor) {
        if (divisor >= 0) {
            if (dividend >= 0) {
                return dividend / divisor;
            }
            // The implementation is a Java port of algorithm described in the book
            // "Hacker's Delight" (section "Unsigned short division from signed division").
            int q = ((dividend >>> 1) / divisor) << 1;
            dividend -= q * divisor;
            if (dividend < 0L || dividend >= divisor) {
                q++;
            }
            return q;
        }
        return dividend >= 0 || dividend < divisor ? 0 : 1;
    }

    /**
     * Returns the unsigned quotient of dividing the first argument by
     * the second where each argument and the result is interpreted as
     * an unsigned value.
     * <p>
     * Note that in two's complement arithmetic, the three other
     * basic arithmetic operations of add, subtract, and multiply are
     * bit-wise identical if the two operands are regarded as both
     * being signed or both being unsigned. Therefore separate {@code
     * addUnsigned}, etc. methods are not provided.
     * <p>
     * This method does not use the {@code BigInteger} datatype.
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the unsigned quotient of the first argument divided by
     * the second argument.
     */
    public static long divideUnsigned(long dividend, long divisor) {
        if (divisor >= 0L) {
            if (dividend >= 0L) {
                return dividend / divisor;
            }
            // The implementation is a Java port of algorithm described in the book
            // "Hacker's Delight" (section "Unsigned short division from signed division").
            long q = ((dividend >>> 1) / divisor) << 1;
            dividend -= q * divisor;
            if (dividend < 0L || dividend >= divisor) {
                q++;
            }
            return q;
        }
        return dividend >= 0L || dividend < divisor ? 0L : 1L;
    }

}
