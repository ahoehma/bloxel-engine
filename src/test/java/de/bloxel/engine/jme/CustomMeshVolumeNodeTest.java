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
package de.bloxel.engine.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.ColtVolumeFactory;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.loader.BlockmaniaTerrainLoader;
import de.bloxel.engine.loader.Loader;
import de.bloxel.engine.material.ImageAtlasBloxelAssetManager;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class CustomMeshVolumeNodeTest extends SimpleApplication {

  static class DummyLoader implements Loader<Bloxel> {

    @Override
    public void fill(final Volume<Bloxel> aVolume) {
      final Bloxel water = new Bloxel(6);
      final Bloxel sand = new Bloxel(2);
      final Bloxel grass = new Bloxel(1);
      for (int x = 0; x < aVolume.getSizeX(); x++) {
        for (int z = 0; z < aVolume.getSizeZ(); z++) {
          for (int y = 0; y < aVolume.getSizeY(); y++) {
            if (aVolume.getY() + y <= 0) {
              aVolume.set(x, y, z, sand);
              continue;
            }
            if (aVolume.getY() + y < 8) {
              if (aVolume.getX() + x >= 8) {
                aVolume.set(x, y, z, grass);
                continue;
              }
              aVolume.set(x, y, z, water);
              continue;
            }
            if (Math.abs(aVolume.getX() + x) >= 8) {
              aVolume.set(x, y, z, sand);
              continue;
            }
          }
        }
      }
    }
  }

  public static void main(final String[] args) {
    new CustomMeshVolumeNodeTest().start();
  }

  private CustomMeshVolumeNode node(final VolumeGrid<Bloxel> grid, final int x, final int y, final int z) {
    final CustomMeshVolumeNode volumeNode = new CustomMeshVolumeNode(grid, grid.getVolume(x, y, z),
        new ImageAtlasBloxelAssetManager(assetManager));
    volumeNode.calculate();
    volumeNode.update();
    return volumeNode;
  }

  @Override
  public void simpleInitApp() {
    final VolumeGrid<Bloxel> grid = new VolumeGrid<Bloxel>();
    grid.setGridSize(16, 4, 16);
    grid.setVolumeSize(32);
    grid.setVolumeLoader(new BlockmaniaTerrainLoader("cPpCzKqBpVkQpVjP"));
    // grid.setVolumeLoader(new DummyLoader());
    grid.setVolumeFactory(new ColtVolumeFactory<Bloxel>());
    grid.init();
    for (int z = -1; z < 1; z++) {
      for (int y = 1; y < 2; y++) {
        rootNode.attachChild(node(grid, 4, y, z));
      }
    }

    cam.setLocation(Vector3f.ZERO);
    cam.setFrustumFar(3000f);
    flyCam.setMoveSpeed(50);
    flyCam.setEnabled(true);

    // final DirectionalLight sunDirectionalLight = new DirectionalLight();
    // sunDirectionalLight.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
    // sunDirectionalLight.setColor(ColorRGBA.White);
    // rootNode.addLight(sunDirectionalLight);

    final AmbientLight l = new AmbientLight();
    l.setColor(ColorRGBA.White.mult(3));
    rootNode.addLight(l);
  }
}