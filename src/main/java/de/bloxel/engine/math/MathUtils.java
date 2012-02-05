/*******************************************************************************
 * Copyright (c) 2012 Andreas Höhmann
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *******************************************************************************/
package de.bloxel.engine.math;

import java.util.Random;

/**
 * Miscellaneous math utilities.
 */
public class MathUtils {

  /**
   * Square root of 2
   */
  public static final float SQRT2 = (float) Math.sqrt(2);

  /**
   * Square root of 3
   */
  public static final float SQRT3 = (float) Math.sqrt(3);

  /**
   * Log(2)
   */
  public static final float LOG2 = (float) Math.log(2);

  /**
   * PI
   */
  public static final float PI = 3.14159265358979323846f;

  /**
   * The reciprocal of PI: (1/PI)
   */
  public static final float INV_PI = 1f / PI;

  /**
   * PI/2
   */
  public static final float HALF_PI = PI / 2;

  /**
   * PI/3
   */
  public static final float THIRD_PI = PI / 3;

  /**
   * PI/4
   */
  public static final float QUARTER_PI = PI / 4;

  /**
   * PI*2
   */
  public static final float TWO_PI = PI * 2;

  /**
   * PI*1.5
   */
  public static final float THREE_HALVES_PI = TWO_PI - HALF_PI;

  /**
   * PI*PI
   */
  public static final float PI_SQUARED = PI * PI;

  /**
   * Epsilon value
   */
  public static final float EPS = 1.2e-7f; // was 1.1920928955078125E-7f;

  /**
   * Degrees to radians conversion factor
   */
  public static final float DEG2RAD = PI / 180;

  /**
   * Radians to degrees conversion factor
   */
  public static final float RAD2DEG = 180 / PI;

  private static final float SHIFT23 = 1 << 23;
  private static final float INV_SHIFT23 = 1.0f / SHIFT23;

  private final static double SIN_A = -4d / (PI * PI);
  private final static double SIN_B = 4d / PI;
  private final static double SIN_P = 9d / 40;

  /**
   * @param x
   * @return absolute value of x
   */
  public static final double abs(final double x) {
    return x < 0 ? -x : x;
  }

  /**
   * @param x
   * @return absolute value of x
   */
  public static final float abs(final float x) {
    return x < 0 ? -x : x;
  }

  /**
   * @param x
   * @return absolute value of x
   */
  public static final int abs(final int x) {
    final int y = x >> 31;
    return (x ^ y) - y;
  }

  /**
   * Rounds up the value to the nearest higher power^2 value.
   * 
   * @param x
   * @return power^2 value
   */
  public static final int ceilPowerOf2(final int x) {
    int pow2 = 1;
    while (pow2 < x) {
      pow2 <<= 1;
    }
    return pow2;
  }

  public static final double clip(final double a, final double min, final double max) {
    return a < min ? min : a > max ? max : a;
  }

  public static final float clip(final float a, final float min, final float max) {
    return a < min ? min : a > max ? max : a;
  }

  public static final int clip(final int a, final int min, final int max) {
    return a < min ? min : a > max ? max : a;
  }

  /**
   * Clips the value to the 0.0 .. 1.0 interval.
   * 
   * @param a
   * @return clipped value
   * @since 0012
   */
  public static final float clipNormalized(final float a) {
    if (a < 0) {
      return 0;
    } else if (a > 1) {
      return 1;
    }
    return a;
  }

  /**
   * Returns fast cosine approximation of a value. Note: code from <a
   * href="http://wiki.java.net/bin/view/Games/JeffGems">wiki posting on java.net by jeffpk</a>
   * 
   * @param theta
   *          angle in radians.
   * @return cosine of theta.
   */
  public static final float cos(final float theta) {
    return sin(theta + HALF_PI);
  }

  public static final float degrees(final float radians) {
    return radians * RAD2DEG;
  }

  /**
   * Fast cosine approximation.
   * 
   * @param x
   *          angle in -PI/2 .. +PI/2 interval
   * @return cosine
   */
  public static final double fastCos(final double x) {
    return fastSin(x + (x > HALF_PI ? -THREE_HALVES_PI : HALF_PI));
  }

  /**
   * @deprecated renamed into {@link #floor(float)}
   */
  @Deprecated
  public static final int fastFloor(final float x) {
    return floor(x);
  }

  /**
   * @deprecated
   */
  @Deprecated
  public static final float fastInverseSqrt(float x) {
    final float half = 0.5F * x;
    int i = Float.floatToIntBits(x);
    i = 0x5f375a86 - (i >> 1);
    x = Float.intBitsToFloat(i);
    return x * (1.5F - half * x * x);
  }

  /**
   * Computes a fast approximation to <code>Math.pow(a, b)</code>. Adapted from
   * http://www.dctsystems.co.uk/Software/power.html.
   * 
   * @param a
   *          a positive number
   * @param b
   *          a number
   * @return a^b
   * 
   */
  public static final float fastPow(final float a, float b) {
    float x = Float.floatToRawIntBits(a);
    x *= INV_SHIFT23;
    x -= 127;
    float y = x - (x >= 0 ? (int) x : (int) x - 1);
    b *= x + (y - y * y) * 0.346607f;
    y = b - (b >= 0 ? (int) b : (int) b - 1);
    y = (y - y * y) * 0.33971f;
    return Float.intBitsToFloat((int) ((b + 127 - y) * SHIFT23));
  }

  /**
   * Fast sine approximation.
   * 
   * @param x
   *          angle in -PI/2 .. +PI/2 interval
   * @return sine
   */
  public static final double fastSin(double x) {
    x = SIN_A * x * abs(x) + SIN_B * x;
    return SIN_P * (x * abs(x) - x) + x;
  }

  public static final boolean flipCoin() {
    return Math.random() < 0.5;
  }

  public static final boolean flipCoin(final Random rnd) {
    return rnd.nextBoolean();
  }

  public static final int floor(final double x) {
    int y = (int) x;
    if (x < 0 && x != y) {
      y--;
    }
    return y;
  }

  /**
   * This method is a *lot* faster than using (int)Math.floor(x).
   * 
   * @param x
   *          value to be floored
   * @return floored value as integer
   * @since 0012
   */
  public static final int floor(final float x) {
    int y = (int) x;
    if (x < 0 && x != y) {
      y--;
    }
    return y;
  }

  /**
   * Rounds down the value to the nearest lower power^2 value.
   * 
   * @param x
   * @return power^2 value
   */
  public static final int floorPowerOf2(final int x) {
    return (int) Math.pow(2, (int) (Math.log(x) / LOG2));
  }

  public static final double max(final double a, final double b) {
    return a > b ? a : b;
  }

  public static final double max(final double a, final double b, final double c) {
    return a > b ? a > c ? a : c : b > c ? b : c;
  }

  public static final float max(final float a, final float b) {
    return a > b ? a : b;
  }

  /**
   * Returns the maximum value of three floats.
   * 
   * @param a
   * @param b
   * @param c
   * @return max val
   */
  public static final float max(final float a, final float b, final float c) {
    return a > b ? a > c ? a : c : b > c ? b : c;
  }

  public static final int max(final int a, final int b) {
    return a > b ? a : b;
  }

  /**
   * Returns the maximum value of three ints.
   * 
   * @param a
   * @param b
   * @param c
   * @return max val
   */
  public static final int max(final int a, final int b, final int c) {
    return a > b ? a > c ? a : c : b > c ? b : c;
  }

  public static final double min(final double a, final double b) {
    return a < b ? a : b;
  }

  public static final double min(final double a, final double b, final double c) {
    return a < b ? a < c ? a : c : b < c ? b : c;
  }

  public static final float min(final float a, final float b) {
    return a < b ? a : b;
  }

  /**
   * Returns the minimum value of three floats.
   * 
   * @param a
   * @param b
   * @param c
   * @return min val
   */
  public static final float min(final float a, final float b, final float c) {
    return a < b ? a < c ? a : c : b < c ? b : c;
  }

  public static final int min(final int a, final int b) {
    return a < b ? a : b;
  }

  /**
   * Returns the minimum value of three ints.
   * 
   * @param a
   * @param b
   * @param c
   * @return min val
   */
  public static final int min(final int a, final int b, final int c) {
    return a < b ? a < c ? a : c : b < c ? b : c;
  }

  /**
   * Returns a random number in the interval -1 .. +1.
   * 
   * @return random float
   */
  public static final float normalizedRandom() {
    return (float) Math.random() * 2 - 1;
  }

  /**
   * Returns a random number in the interval -1 .. +1 using the {@link Random} instance provided.
   * 
   * @return random float
   */
  public static final float normalizedRandom(final Random rnd) {
    return rnd.nextFloat() * 2 - 1;
  }

  public static final float radians(final float degrees) {
    return degrees * DEG2RAD;
  }

  public static final float random(final float max) {
    return (float) Math.random() * max;
  }

  public static final float random(final float min, final float max) {
    return (float) Math.random() * (max - min) + min;
  }

  public static final int random(final int max) {
    return (int) (Math.random() * max);
  }

  public static final int random(final int min, final int max) {
    return (int) (Math.random() * (max - min)) + min;
  }

  public static final double random(final Random rnd, final double max) {
    return rnd.nextDouble() * max;
  }

  public static final double random(final Random rnd, final double min, final double max) {
    return rnd.nextDouble() * (max - min) + min;
  }

  public static final float random(final Random rnd, final float max) {
    return rnd.nextFloat() * max;
  }

  public static final float random(final Random rnd, final float min, final float max) {
    return rnd.nextFloat() * (max - min) + min;
  }

  public static final int random(final Random rnd, final int max) {
    return (int) (rnd.nextDouble() * max);
  }

  public static final int random(final Random rnd, final int min, final int max) {
    return (int) (rnd.nextDouble() * (max - min)) + min;
  }

  /**
   * Reduces the given angle into the -PI/4 ... PI/4 interval. This method is use by {@link #sin(float)} &
   * {@link #cos(float)}.
   * 
   * @param theta
   *          angle in radians
   * @return reduced angle
   * @see #sin(float)
   * @see #cos(float)
   */
  public static final float reduceAngle(float theta) {
    theta %= TWO_PI;
    if (abs(theta) > PI) {
      theta = theta - TWO_PI;
    }
    if (abs(theta) > HALF_PI) {
      theta = PI - theta;
    }
    return theta;
  }

  /**
   * Returns a fast sine approximation of a value. Note: code from <a
   * href="http://wiki.java.net/bin/view/Games/JeffGems">wiki posting on java.net by jeffpk</a>
   * 
   * @param theta
   *          angle in radians.
   * @return sine of theta.
   */
  public static final float sin(float theta) {
    theta = reduceAngle(theta);
    if (abs(theta) <= QUARTER_PI) {
      return (float) fastSin(theta);
    }
    return (float) fastCos(HALF_PI - theta);
  }

  /**
   * @deprecated
   */
  @Deprecated
  public static final float sqrt(float x) {
    x = fastInverseSqrt(x);
    if (x > 0) {
      return 1.0f / x;
    } else {
      return 0;
    }
  }
}