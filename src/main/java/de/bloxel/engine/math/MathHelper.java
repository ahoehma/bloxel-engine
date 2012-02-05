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

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class MathHelper {

  /**
   * @param x
   * @param y
   * @param q11
   * @param q12
   * @param q21
   * @param q22
   * @param x1
   * @param x2
   * @param y1
   * @param y2
   * @return
   */
  public static float biLerp(final float x, final float y, final float q11, final float q12, final float q21,
      final float q22, final float x1, final float x2, final float y1, final float y2) {
    final float r1 = lerp(x, x1, x2, q11, q21);
    final float r2 = lerp(x, x1, x2, q12, q22);
    return lerp(y, y1, y2, r1, r2);
  }

  /*
     * 
     */

  /**
   * Applies Cantor's pairing function to 2D coordinates.
   * 
   * @param k1
   *          X-coordinate
   * @param k2
   *          Y-coordinate
   * @return Unique 1D value
   */
  public static int cantorize(final int k1, final int k2) {
    return (k1 + k2) * (k1 + k2 + 1) / 2 + k2;
  }

  /**
   * Inverse function of Cantor's pairing function.
   * 
   * @param c
   *          Cantor value
   * @return Value along the x-axis
   */
  public static int cantorX(final int c) {
    final int j = (int) Math.floor(Math.sqrt(0.25 + 2 * c) - 0.5);
    return j - cantorY(c);
  }

  /**
   * Inverse function of Cantor's pairing function.
   * 
   * @param c
   *          Cantor value
   * @return Value along the y-axis
   */
  private static int cantorY(final int c) {
    final int j = (int) Math.floor(Math.sqrt(0.25 + 2 * c) - 0.5);
    return c - j * (j + 1) / 2;
  }

  /**
   * @param x
   * @param q00
   * @param q01
   * @param x1
   * @param x2
   * @return
   */
  private static float lerp(final float x, final float x1, final float x2, final float q00, final float q01) {
    return (x2 - x) / (x2 - x1) * q00 + (x - x1) / (x2 - x1) * q01;
  }

  /**
   * @param x
   * @param y
   * @param z
   * @param q000
   * @param q001
   * @param q010
   * @param q011
   * @param q100
   * @param q101
   * @param q110
   * @param q111
   * @param x1
   * @param x2
   * @param y1
   * @param y2
   * @param z1
   * @param z2
   * @return
   */
  public static float triLerp(final float x, final float y, final float z, final float q000, final float q001,
      final float q010, final float q011, final float q100, final float q101, final float q110, final float q111,
      final float x1, final float x2, final float y1, final float y2, final float z1, final float z2) {
    final float x00 = lerp(x, x1, x2, q000, q100);
    final float x10 = lerp(x, x1, x2, q010, q110);
    final float x01 = lerp(x, x1, x2, q001, q101);
    final float x11 = lerp(x, x1, x2, q011, q111);
    final float r0 = lerp(y, y1, y2, x00, x01);
    final float r1 = lerp(y, y1, y2, x10, x11);
    return lerp(z, z1, z2, r0, r1);
  }
}
