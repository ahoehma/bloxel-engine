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
package de.bloxel.engine.data;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import cern.colt.matrix.ObjectFactory3D;
import cern.colt.matrix.ObjectMatrix3D;

public class ColtVolume<T> implements Volume<T> {

  private ObjectMatrix3D matrix3d;
  private final int x;
  private final int y;
  private final int z;
  private final int sizeX;
  private final int sizeY;
  private final int sizeZ;

  /**
   * @param x
   *          position
   * @param y
   *          position
   * @param z
   *          position
   */
  public ColtVolume(final int x, final int y, final int z, final int sizeX, final int sizeY, final int sizeZ) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    this.sizeZ = sizeZ;
    clear();
  }

  @Override
  public void clear() {
    matrix3d = ObjectFactory3D.sparse.make(sizeX, sizeY, sizeZ);
  }

  @Override
  public T get(final int x, final int y, final int z) {
    checkArgument(x >= 0);
    checkArgument(y >= 0);
    checkArgument(z >= 0);
    checkArgument(x < sizeX, format("x must be lower then size-x %d but was %d", sizeX, x));
    checkArgument(y < sizeY, format("y must be lower then size-y %d but was %d", sizeY, y));
    checkArgument(z < sizeZ, format("z must be lower then size-z %d but was %d", sizeZ, z));
    return (T) firstNonNull(matrix3d.get(x, y, z), Bloxel.AIR);
  }

  @Override
  public int getSizeX() {
    return sizeX;
  }

  @Override
  public int getSizeY() {
    return sizeY;
  }

  @Override
  public int getSizeZ() {
    return sizeZ;
  }

  @Override
  public int getX() {
    return x;
  }

  @Override
  public int getY() {
    return y;
  }

  @Override
  public int getZ() {
    return z;
  }

  @Override
  public void set(final int x, final int y, final int z, final T bloxel) {
    checkArgument(x >= 0);
    checkArgument(y >= 0);
    checkArgument(z >= 0);
    checkArgument(x < sizeX, format("x must be lower then size-x %d but was %d", sizeX, x));
    checkArgument(y < sizeY, format("y must be lower then size-y %d but was %d", sizeY, y));
    checkArgument(z < sizeZ, format("z must be lower then size-z %d but was %d", sizeZ, z));
    matrix3d.set(x, y, z, bloxel);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add("x", x).add("y", y).add("z", z).add("size-x", sizeX).add("size-y", sizeY)
        .add("size-z", sizeZ).toString();
  }
}