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
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import cern.colt.matrix.ObjectFactory3D;
import cern.colt.matrix.ObjectMatrix3D;

import com.google.common.base.Preconditions;

public class ColtVolume<T> implements Volume<T> {

  public static <T extends Bloxel> List<T> getLeafs(final ColtVolume<T> o) {
    final List<T> result = newArrayList();
    for (int slices = 0; slices < o.matrix3d.slices(); slices++) {
      for (int rows = 0; rows < o.matrix3d.rows(); rows++) {
        for (int columns = 0; columns < o.matrix3d.columns(); columns++) {
          final T element = (T) o.matrix3d.get(slices, rows, columns);
          if (element != null) {
            result.add(element);
          }
        }
      }
    }
    return result;
  }

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

  @SuppressWarnings("unchecked")
  @Override
  public T get(final int x, final int y, final int z) {
    Preconditions.checkArgument(x >= 0);
    Preconditions.checkArgument(y >= 0);
    Preconditions.checkArgument(z >= 0);
    Preconditions.checkArgument(x < sizeX);
    Preconditions.checkArgument(y < sizeY);
    Preconditions.checkArgument(z < sizeZ);
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
    Preconditions.checkArgument(x >= 0);
    Preconditions.checkArgument(y >= 0);
    Preconditions.checkArgument(z >= 0);
    Preconditions.checkArgument(x < sizeX);
    Preconditions.checkArgument(y < sizeY);
    Preconditions.checkArgument(z < sizeZ);
    matrix3d.set(x, y, z, bloxel);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add("x", x).add("y", y).add("z", z).add("size-x", sizeX).add("size-y", sizeY)
        .add("size-z", sizeZ).toString();
  }
}