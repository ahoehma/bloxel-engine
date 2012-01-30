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

  @Test
  public void testGridLoading() {
    final VolumeGrid<Integer> grid = new VolumeGrid<Integer>();
    grid.setGridSize(16, 4, 16);
    grid.setVolumeSize(64);
    grid.setVolumeFactory(new ColtVolumeFactory());
    grid.setVolumeLoader(new DummyLoader());
    grid.init();

    final Volume<Integer> v = grid.getVolume(0, 0, 0);
    assertEquals(v.getSizeX(), 64);
    assertEquals(v.getSizeY(), 64);
    assertEquals(v.getSizeZ(), 64);
    assertEquals(v.getX(), 0);
    assertEquals(v.getY(), 0);
    assertEquals(v.getZ(), 0);
    for (int x = 0; x < v.getSizeX(); x++) {
      for (int z = 0; z < v.getSizeZ(); z++) {
        for (int y = 0; y < v.getSizeY(); y++) {
          assertEquals(v.get(x, y, z), Integer.valueOf(1));
        }
      }
    }

    assertSame(v, grid.getVolume(-0.0f, 0.0f, 0.0f));
    assertSame(v, grid.getVolume(0.0f, 0.0f, 0.0f));
    assertSame(v, grid.getVolume(1.0f, 0.0f, 0.0f));
    assertSame(v, grid.getVolume(63.0f, 0.0f, 0.0f));
    assertSame(v, grid.getVolume(63.9f, 0.0f, 0.0f));

    final Volume<Integer> v2 = grid.getVolume(-1, 0, 0);
    assertEquals(v2.getSizeX(), 64);
    assertEquals(v2.getSizeY(), 64);
    assertEquals(v2.getSizeZ(), 64);
    assertEquals(v2.getX(), -64);
    assertEquals(v2.getY(), 0);
    assertEquals(v2.getZ(), 0);

    assertSame(v2, grid.getVolume(-0.1f, 0.0f, 0.0f));
    assertSame(v2, grid.getVolume(-1.0f, 0.0f, 0.0f));
    assertSame(v2, grid.getVolume(-63.9f, 0.0f, 0.0f));

    final Volume<Integer> v3 = grid.getVolume(1, 0, 0);
    assertEquals(v3.getSizeX(), 64);
    assertEquals(v3.getSizeY(), 64);
    assertEquals(v3.getSizeZ(), 64);
    assertEquals(v3.getX(), 64);
    assertEquals(v3.getY(), 0);
    assertEquals(v3.getZ(), 0);

    assertSame(v3, grid.getVolume(64.0f, 0.0f, 0.0f));
    assertSame(v3, grid.getVolume(64.1f, 0.0f, 0.0f));
    assertSame(v3, grid.getVolume(127.9f, 0.0f, 0.0f));

    final Volume<Integer> v4 = grid.getVolume(-2, 0, 0);
    assertEquals(v4.getSizeX(), 64);
    assertEquals(v4.getSizeY(), 64);
    assertEquals(v4.getSizeZ(), 64);
    assertEquals(v4.getX(), -128);
    assertEquals(v4.getY(), 0);
    assertEquals(v4.getZ(), 0);

    assertSame(v4, grid.getVolume(-64.0f, 0.0f, 0.0f));
  }

  @Test
  public void testRanges() {
    final VolumeGrid<Integer> grid = new VolumeGrid<Integer>();
    grid.setGridSize(16, 4, 16);
    grid.setVolumeSize(64);
    grid.setVolumeFactory(new ColtVolumeFactory());
    grid.setVolumeLoader(new DummyLoader());
    grid.init();
    grid.getVolume(-8, 0, 0);
    grid.getVolume(8, 0, 0);
    grid.getVolume(0, 0, -8);
    grid.getVolume(0, 0, 8);
    grid.getVolume(0, -2, -8);
    grid.getVolume(0, 2, 8);
  }
}
