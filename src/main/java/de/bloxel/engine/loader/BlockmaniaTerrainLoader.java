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
package de.bloxel.engine.loader;

import org.apache.log4j.Logger;

import com.jme3.math.FastMath;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.math.MathHelper;
import de.bloxel.engine.math.PerlinNoise;

/**
 * Based on the terrain generator of "block mania".
 * 
 * @see http://blog.movingblocks.net/blockmania/
 */
public class BlockmaniaTerrainLoader implements Loader<Bloxel> {

  private static final Logger LOG = Logger.getLogger(BlockmaniaTerrainLoader.class);

  private static final int SAMPLE_RATE_3D_HOR = 8; // 8->16
  private static final int SAMPLE_RATE_3D_VERT = 8; // 16->128
  private static final int WATER_LEVEL_Y = 30;

  private static Bloxel bloxel(final byte blockTailpiece, final float dens) {
    switch (blockTailpiece) {
    case 1:
      return new Bloxel(1, dens);
    case 2:
      return new Bloxel(2, dens);
    case 4:
      return new Bloxel(4, dens);
    case 5:
      return new Bloxel(5, dens);
    }
    throw new IllegalArgumentException("Unsupported material " + blockTailpiece);
  }

  private final PerlinNoise _pGen1;
  private final PerlinNoise _pGen2;
  private final PerlinNoise _pGen3;

  private final PerlinNoise _pGen4;

  public BlockmaniaTerrainLoader(final int seed) {
    _pGen1 = new PerlinNoise(seed);
    _pGen2 = new PerlinNoise(seed + 1);
    _pGen3 = new PerlinNoise(seed + 2);
    _pGen4 = new PerlinNoise(seed + 3);
  }

  float calcDensity(final float x, final float y, final float z) {
    final float height = (calcTerrainElevation(x, z) + 1) / 2 + (calcTerrainRoughness(x, z) + 1) / 2;
    float density = calcMountainDensity(x, y, z);
    density = height - density;
    if (y < 120) {
      density /= (y + 1) * 1.7f;
    } else {
      density /= (y + 1) * 2.0f;
    }
    return FastMath.abs(density);
  }

  float calcLakeIntensity(final float x, final float z) {
    float result = 0.0f;
    // result += _pGen3.multiFractalNoise(x * 0.01f, 0.01f, 0.01f * z, 3, 1.9836171f);
    result += _pGen3.multiFractalNoise(x * 0.01f, 0.01f, 0.01f * z, 8, 2.1836171f);
    return (float) Math.sqrt(Math.abs(result));
  }

  float calcMountainDensity(final float x, final float y, final float z) {
    float result = 0.0f;

    float x1, y1, z1;

    x1 = x * 0.0007f;
    y1 = y * 0.0009f;
    z1 = z * 0.0007f;

    final float oct1 = 1.432f, oct2 = 4.4281f, oct3 = 7.371f, oct4 = 11.463819f, oct5 = 14.62819f, oct6 = 22.3672891f, oct7 = 44.47381f, oct8 = 53.47381f, oct9 = 64.47381f;

    result += _pGen2.noise(x1 * oct9, y1 * oct9, z1 * oct9) * 0.2;
    result += _pGen2.noise(x1 * oct8, y1 * oct8, z1 * oct8) * 0.3;
    result += _pGen2.noise(x1 * oct7, y1 * oct7, z1 * oct7) * 0.4;
    result += _pGen2.noise(x1 * oct6, y1 * oct6, z1 * oct6) * 0.5;
    result += _pGen2.noise(x1 * oct5, y1 * oct5, z1 * oct5) * 0.6;
    result += _pGen2.noise(x1 * oct4, y1 * oct4, z1 * oct4) * 0.7;
    result += _pGen2.noise(x1 * oct3, y1 * oct3, z1 * oct3) * 0.8;
    result += _pGen2.noise(x1 * oct2, y1 * oct2, z1 * oct2) * 0.9;
    result += _pGen2.noise(x1 * oct1, y1 * oct1, z1 * oct1) * 1.0;

    return result;
  }

  /**
   * Returns the detail level for the base terrain.
   * 
   * @param x
   * @param z
   * @return
   */
  float calcTerrainDetail(final float x, final float z) {
    float result = 0.0f;
    result += _pGen3.ridgedMultiFractalNoise(x * 0.004f, 0.004f, z * 0.004f, 8, 1.2f, 3f, 1f);
    return result;
  }

  /**
   * Returns the base elevation for the terrain.
   * 
   * @param x
   * @param z
   * @return
   */
  float calcTerrainElevation(final float x, final float z) {
    float result = 0.0f;
    result += _pGen1.noise(0.0008f * x, 0f, 0.0008f * z);
    return result;
  }

  /**
   * Returns the roughness for the base terrain.
   * 
   * @param x
   * @param z
   * @return
   */
  float calcTerrainRoughness(final float x, final float z) {
    float result = 0.0f;
    result += _pGen2.multiFractalNoise(0.001f * x, 0.00f, 0.001f * z, 7, 2.151421f);
    return result;
  }

  private float[][][] createDensityData(final int dimX, final int dimY, final int dimZ, final float offsetX,
      final float offsetY, final float offsetZ) {
    final float[][][] densityMap = new float[dimX + 1][dimY + 1][dimZ + 1];
    // Create the density map at a lower sample rate.
    final int rateX = SAMPLE_RATE_3D_HOR; // / (16 * dimX);
    final int rateZ = SAMPLE_RATE_3D_HOR; // / (16 * dimZ);
    final int rateY = SAMPLE_RATE_3D_VERT; // / (128 * dimY);
    for (int x = 0; x <= dimX; x += rateX) {
      for (int z = 0; z <= dimZ; z += rateZ) {
        for (int y = 0; y <= dimY; y += rateY) {
          densityMap[x][y][z] = calcDensity(x + offsetX, y + offsetY, z + offsetZ);
        }
      }
    }
    // Trilinear interpolate the missing values.
    triLerpDensityMap(densityMap, dimX, dimY, dimZ);
    return densityMap;
  }

  @Override
  public void fill(final Volume<Bloxel> volume) {

    LOG.debug(String.format("Fill volume %s", volume));

    final long startTime = System.currentTimeMillis();

    final int sizeX = volume.getSizeX();
    final int sizeY = volume.getSizeY();
    final int sizeZ = volume.getSizeZ();
    final int vx = volume.getX();
    final int vy = volume.getY();
    final int vz = volume.getZ();

    boolean densityDataInitialized = false;
    float[][][] densityData = null;

    // Generate the chunk from the density map.30
    for (int x = 0; x < sizeX; x++) {
      for (int z = 0; z < sizeZ; z++) {
        float firstBlockHeight = -1;
        for (int y = sizeY - 1; y >= 0; y--) {
          // local to global location mapping
          final int globalX = vx + x;
          final int globalY = vy + y;
          final int globalZ = vz + z;
          // some density independent rules ...
          if (globalY <= 0) {
            // Stone ground layer with caves
            float caveNoise = _pGen4.noise(globalX * 0.009f, globalY * 0.009f, globalZ * 0.009f) * 0.25f;
            caveNoise += _pGen4.noise(globalX * 0.04f, globalY * 0.04f, globalZ * 0.04f) * 0.15f;
            caveNoise += _pGen4.noise(globalX * 0.08f, globalY * 0.08f, globalZ * 0.08f) * 0.05f;
            if (caveNoise > 0.24f) {
              volume.set(x, y, z, new Bloxel(4, 0));
            }
            continue;
          }
          if (globalY < WATER_LEVEL_Y && globalY > 0) {
            // Ocean
            volume.set(x, y, z, new Bloxel(6));
          }
          // perlin noise based rules ...
          if (!densityDataInitialized) {
            densityData = createDensityData(sizeX, sizeY, sizeZ, vx, vy, vz);
            densityDataInitialized = true;
          }
          final float dens = densityData[x][y][z];
          if (dens >= 0.01f && dens < 0.012f) {
            // The outer layer is made of dirt and grass.
            if (firstBlockHeight == -1) {
              volume.set(x, y, z, bloxel(getBlockTailpiece(getBlockTypeForPosition(globalY, 1.0f), globalY), dens));
              // Generate lakes
              final float lakeIntensity = calcLakeIntensity(x + vx, z + vy);
              if (lakeIntensity < 0.1) {
                volume.set(x, y, z, new Bloxel(7, dens));
              }
              firstBlockHeight = globalY;
            } else {
              volume.set(
                  x,
                  y,
                  z,
                  bloxel(getBlockTypeForPosition(globalY, 1.0f - (firstBlockHeight - globalY) / SAMPLE_RATE_3D_VERT),
                      dens));
            }
          } else if (dens >= 0.012f) {
            volume.set(x, y, z, bloxel(getBlockTailpiece(getBlockTypeForPosition(globalY, 0.2f), globalY), dens));
            if (firstBlockHeight == -1) {
              firstBlockHeight = globalY;
            }
          }
        }
      }
    }

    final float duration = System.currentTimeMillis() - startTime;
    LOG.debug("Fill time was " + duration + "ms");
  }

  byte getBlockTailpiece(final byte type, final float y) {
    // Sand
    if (type == 2) {
      return 2;
    } else if (type == 4) {
      return 4;
    }
    // No grass below the water surface
    if (y > WATER_LEVEL_Y + 2) {
      return 1;
    } else {
      return 5;
    }
  }

  byte getBlockTypeForPosition(final float y, final float heightPercentage) {
    // Sand
    if (y >= 28 && y <= 32) {
      return (byte) 2;
    }
    if (heightPercentage <= 0.8) {
      return (byte) 4;
    }
    return 5;
  }

  void triLerpDensityMap(final float[][][] densityMap, final int dimX, final int dimY, final int dimZ) {
    final int rateX = SAMPLE_RATE_3D_HOR; // / (16 * dimX);
    final int rateZ = SAMPLE_RATE_3D_HOR; // / (16 * dimZ);
    final int rateY = SAMPLE_RATE_3D_VERT; // / (128 * dimY);
    for (int x = 0; x < dimX; x++) {
      for (int y = 0; y < dimY; y++) {
        for (int z = 0; z < dimZ; z++) {
          if (!(x % rateX == 0 && y % rateY == 0 && z % rateZ == 0)) {
            final int offsetX = x / rateX * rateX;
            final int offsetY = y / rateY * rateY;
            final int offsetZ = z / rateZ * rateZ;
            densityMap[x][y][z] = MathHelper.triLerp(x, y, z, densityMap[offsetX][offsetY][offsetZ],
                densityMap[offsetX][rateY + offsetY][offsetZ], densityMap[offsetX][offsetY][offsetZ + rateZ],
                densityMap[offsetX][offsetY + rateY][offsetZ + rateZ], densityMap[rateX + offsetX][offsetY][offsetZ],
                densityMap[rateX + offsetX][offsetY + rateY][offsetZ], densityMap[rateX + offsetX][offsetY][offsetZ
                    + rateZ], densityMap[rateX + offsetX][offsetY + rateY][offsetZ + rateZ], offsetX, rateX + offsetX,
                offsetY, rateY + offsetY, offsetZ, offsetZ + rateZ);
          }
        }
      }
    }
  }
}