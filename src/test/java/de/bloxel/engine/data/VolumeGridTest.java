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

import static java.lang.Integer.valueOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;

import de.bloxel.engine.loader.Loader;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
@Test
public class VolumeGridTest {

  static class DummyLoader implements Loader<Integer> {

    @Override
    public void fill(final Volume<Integer> aVolume) {
      for (int x = 0; x < aVolume.getSizeX(); x++) {
        for (int z = 0; z < aVolume.getSizeZ(); z++) {
          for (int y = 0; y < aVolume.getSizeY(); y++) {
            aVolume.set(x, y, z, 1);
          }
        }
      }
    }
  }

  static class TestLoader implements Loader<Integer> {

    @Override
    public void fill(final Volume<Integer> aVolume) {
      for (int x = 0; x < aVolume.getSizeX(); x++) {
        for (int z = 0; z < aVolume.getSizeZ(); z++) {
          for (int y = 0; y < aVolume.getSizeY(); y++) {
            aVolume.set(x, y, z, 100 * aVolume.getX() + 10 * aVolume.getY() + aVolume.getZ());
          }
        }
      }
    }
  }

  private static void assertAllValues(final Volume<Integer> v, final int i) {
    for (int x = 0; x < v.getSizeX(); x++) {
      for (int z = 0; z < v.getSizeZ(); z++) {
        for (int y = 0; y < v.getSizeY(); y++) {
          assertEquals(v.get(x, y, z), valueOf(i));
        }
      }
    }
  }

  @Test
  public void testGet() {
    final VolumeGrid<Integer> grid = new VolumeGrid<Integer>();
    grid.setGridSize(2, 2, 2);
    grid.setVolumeSize(8);
    grid.setVolumeFactory(new ColtVolumeFactory());
    grid.setVolumeLoader(new TestLoader());
    grid.init();
    assertAllValues(grid.getVolumeWithIndex(0, 0, 0), 0);
    assertAllValues(grid.getVolumeForWorldPosition(0f, 0f, 0f), 0);
    assertAllValues(grid.getVolumeForWorldPosition(-1f, 0f, 0f), -800);
    assertAllValues(grid.getVolumeForWorldPosition(1f, 0f, 0f), 0);
    assertAllValues(grid.getVolumeWithIndex(1, 0, 0), 800);
    assertEquals(grid.get(1, 0, 0), valueOf(0));
    assertEquals(grid.get(8, 0, 0), valueOf(800));
    assertEquals(grid.get(0, 8, 0), valueOf(80));
    assertEquals(grid.get(0, 0, 8), valueOf(8));
    assertEquals(grid.get(8, 8, 8), valueOf(888));
  }

  @Test
  public void testGetNeighbor() {
    final VolumeGrid<Integer> grid = new VolumeGrid<Integer>();
    grid.setGridSize(2, 2, 2);
    grid.setVolumeSize(32);
    grid.setVolumeFactory(new ColtVolumeFactory());
    grid.setVolumeLoader(new TestLoader());
    grid.init();
    final Volume<Integer> v0 = grid.getVolumeWithIndex(0, 0, 0);
    assertAllValues(v0, 0);
    assertEquals(v0.getX(), 0);
    assertEquals(v0.getY(), 0);
    assertEquals(v0.getZ(), 0);
    final Volume<Integer> v1 = grid.getVolumeForWorldPosition(-1, 0, 0);
    assertAllValues(v1, -800);
    assertEquals(grid.get(-1f, 0, 0), valueOf(-800));
  }

  @Test
  public void testGridLoading() {
    final VolumeGrid<Integer> grid = new VolumeGrid<Integer>();
    grid.setGridSize(16, 4, 16);
    grid.setVolumeSize(64);
    grid.setVolumeFactory(new ColtVolumeFactory());
    grid.setVolumeLoader(new DummyLoader());
    grid.init();

    final Volume<Integer> v = grid.getVolumeWithIndex(0, 0, 0);
    assertEquals(v.getSizeX(), 64);
    assertEquals(v.getSizeY(), 64);
    assertEquals(v.getSizeZ(), 64);
    assertEquals(v.getX(), 0);
    assertEquals(v.getY(), 0);
    assertEquals(v.getZ(), 0);
    for (int x = 0; x < v.getSizeX(); x++) {
      for (int z = 0; z < v.getSizeZ(); z++) {
        for (int y = 0; y < v.getSizeY(); y++) {
          assertEquals(v.get(x, y, z), valueOf(1));
        }
      }
    }

    assertSame(v, grid.getVolumeForWorldPosition(-0.0f, 0.0f, 0.0f));
    assertSame(v, grid.getVolumeForWorldPosition(0.0f, 0.0f, 0.0f));
    assertSame(v, grid.getVolumeForWorldPosition(1.0f, 0.0f, 0.0f));
    assertSame(v, grid.getVolumeForWorldPosition(63.0f, 0.0f, 0.0f));
    assertSame(v, grid.getVolumeForWorldPosition(63.9f, 0.0f, 0.0f));

    final Volume<Integer> v2 = grid.getVolumeWithIndex(-1, 0, 0);
    assertEquals(v2.getSizeX(), 64);
    assertEquals(v2.getSizeY(), 64);
    assertEquals(v2.getSizeZ(), 64);
    assertEquals(v2.getX(), -64);
    assertEquals(v2.getY(), 0);
    assertEquals(v2.getZ(), 0);

    assertSame(v2, grid.getVolumeForWorldPosition(-0.1f, 0.0f, 0.0f));
    assertSame(v2, grid.getVolumeForWorldPosition(-1.0f, 0.0f, 0.0f));
    assertSame(v2, grid.getVolumeForWorldPosition(-63.9f, 0.0f, 0.0f));

    final Volume<Integer> v3 = grid.getVolumeWithIndex(1, 0, 0);
    assertEquals(v3.getSizeX(), 64);
    assertEquals(v3.getSizeY(), 64);
    assertEquals(v3.getSizeZ(), 64);
    assertEquals(v3.getX(), 64);
    assertEquals(v3.getY(), 0);
    assertEquals(v3.getZ(), 0);

    assertSame(v3, grid.getVolumeForWorldPosition(64.0f, 0.0f, 0.0f));
    assertSame(v3, grid.getVolumeForWorldPosition(64.1f, 0.0f, 0.0f));
    assertSame(v3, grid.getVolumeForWorldPosition(127.9f, 0.0f, 0.0f));

    final Volume<Integer> v4 = grid.getVolumeWithIndex(-2, 0, 0);
    assertEquals(v4.getSizeX(), 64);
    assertEquals(v4.getSizeY(), 64);
    assertEquals(v4.getSizeZ(), 64);
    assertEquals(v4.getX(), -128);
    assertEquals(v4.getY(), 0);
    assertEquals(v4.getZ(), 0);

    assertSame(v4, grid.getVolumeForWorldPosition(-64.0f, 0.0f, 0.0f));
  }

  @Test
  public void testRanges() {
    final VolumeGrid<Integer> grid = new VolumeGrid<Integer>();
    grid.setGridSize(16, 4, 16);
    grid.setVolumeSize(64);
    grid.setVolumeFactory(new ColtVolumeFactory());
    grid.setVolumeLoader(new DummyLoader());
    grid.init();
    grid.getVolumeWithIndex(-8, 0, 0);
    grid.getVolumeWithIndex(8, 0, 0);
    grid.getVolumeWithIndex(0, 0, -8);
    grid.getVolumeWithIndex(0, 0, 8);
    grid.getVolumeWithIndex(0, -2, -8);
    grid.getVolumeWithIndex(0, 2, 8);
  }
}
