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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Ranges.range;
import static java.lang.String.format;
import cern.colt.matrix.ObjectFactory3D;
import cern.colt.matrix.ObjectMatrix3D;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import de.bloxel.engine.loader.Loader;

/**
 * A {@link VolumeGrid} is a 3d grid of {@link Volume volumes}.
 * 
 * <pre>
 * 
 * The 'XX' is the central {@link Volume volume} at position (0,0,0). In all directions (x: left/right , y: up/down 
 * and z: front/back) are connected neighbor volumes:
 * 
 *       _________
 *      /__/__/__/|
 *     /__/__/__/|/   .
 *    /__/__/__/|/   .
 *    |__|__|__|/   .
 *           _________
 *          /__/__/__/|
 *   . . . /__/XX/__/|/  . . .
 *        /__/__/__/|/
 *        |__|__|__|/
 *            .  _________
 *           .  /__/__/__/|
 *          .  /__/__/__/|/
 *            /__/__/__/|/
 *            |__|__|__|/
 * 
 * </pre>
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class VolumeGrid<T> {

  private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VolumeGrid.class);

  /**
   * x: 0..gridSizeX
   * 
   * y: 0..gridSizeY
   * 
   * z: 0..gridSizeZ
   * 
   * 0,0,0 -> 0,0,0 -1,0,0 ->
   */
  private ObjectMatrix3D grid;
  private Loader<T> volumeLoader;
  private VolumeFactory<T> volumeFactory;
  private int volumeSize;
  private int gridSizeX;
  private int gridSizeZ;
  private int gridSizeY;
  private Range<Integer> rangeX;
  private Range<Integer> rangeY;
  private Range<Integer> rangeZ;

  public T get(final float x, final float y, final float z) {
    final Volume<T> volume = getVolume(x, y, z);
    final int vx = (int) Math.abs(x - volume.getX());
    final int vy = (int) Math.abs(y - volume.getY());
    final int vz = (int) Math.abs(z - volume.getZ());
    LOG.trace(format("Transform global position (x:%f,y:%f,z:%f) into volume position (x:%d,y:%d,z:%d) for volume %s",
        x, y, z, vx, vy, vz, volume));
    return volume.get(vx, vy, vz);
  }

  public Volume<T> getVolume(final float x, final float y, final float z) {
    final int vx = (int) (x / volumeSize) - (x < 0 ? 1 : 0);
    final int vy = (int) (y / volumeSize) - (y < 0 ? 1 : 0);
    final int vz = (int) (z / volumeSize) - (z < 0 ? 1 : 0);
    LOG.trace(format("Transform global position (x:%f,y:%f,z:%f) into local position (x:%d,y:%d,z:%d)", x, y, z, vx,
        vy, vz));
    return getVolume(vx, vy, vz);
  }

  public Volume<T> getVolume(final int x, final int y, final int z) {
    checkArgument(rangeX.contains(x), format("volume position x must be in range %s", rangeX));
    checkArgument(rangeY.contains(y), format("volume position y must be in range %s", rangeY));
    checkArgument(rangeZ.contains(z), format("volume position z must be in range %s", rangeZ));
    final int gx = (gridSizeX >> 1) - x;
    final int gy = (gridSizeY >> 1) - y;
    final int gz = (gridSizeZ >> 1) - z;
    LOG.trace(format("Transform local position (x:%d,y:%d,z:%d) into grid position (x:%d,y:%d,z:%d)", x, y, z, gx, gy,
        gz));
    Volume<T> v = (Volume<T>) grid.get(gx, gy, gz);
    if (v == null) {
      v = volumeFactory.create(x * volumeSize, y * volumeSize, z * volumeSize, volumeSize, volumeSize, volumeSize);
      volumeLoader.fill(v);
      grid.set(gx, gy, gz, v);
    }
    return v;
  }

  public void init() {
    final int x1 = -(gridSizeX >> 1);
    final int x2 = -x1;
    final int y1 = -(gridSizeY >> 1);
    final int y2 = -y1;
    final int z1 = -(gridSizeZ >> 1);
    final int z2 = -z1;
    rangeX = range(x1, BoundType.CLOSED, x2, BoundType.CLOSED);
    rangeY = range(y1, BoundType.CLOSED, y2, BoundType.CLOSED);
    rangeZ = range(z1, BoundType.CLOSED, z2, BoundType.CLOSED);
    grid = ObjectFactory3D.sparse.make(gridSizeX + 1, gridSizeY + 1, gridSizeZ + 1);
    LOG.debug(format("Init volume grid (x:%s,y:%s,z:%s)", rangeX, rangeY, rangeZ));
  }

  public void setGridSize(final int x, final int y, final int z) {
    this.gridSizeX = x;
    this.gridSizeY = y;
    this.gridSizeZ = z;
  }

  public void setVolumeFactory(final VolumeFactory<T> volumeFactory) {
    this.volumeFactory = volumeFactory;
  }

  public void setVolumeLoader(final Loader<T> volumeLoader) {
    this.volumeLoader = volumeLoader;
  }

  public void setVolumeSize(final int size) {
    this.volumeSize = size;
  }
}