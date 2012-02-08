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

import java.util.Random;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.Spatial;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.ColtVolumeFactory;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.loader.Loader;
import de.bloxel.engine.material.ImageAtlasBloxelAssetManager;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class CubicMeshVolumeNodeTest extends SimpleApplication implements ActionListener {

  static class DummyLoader implements Loader<Bloxel> {

    @Override
    public void fill(final Volume<Bloxel> aVolume) {
      final Random r = new Random(19760901);
      for (int x = 0; x < aVolume.getSizeX(); x++) {
        for (int z = 0; z < aVolume.getSizeZ(); z++) {
          for (int y = 0; y < aVolume.getSizeY(); y++) {
            final Bloxel bloxel = new Bloxel(r.nextInt(6) + 1, r.nextFloat() / 10);
            aVolume.set(x, y, z, r.nextBoolean() ? bloxel : Bloxel.AIR);
          }
        }
      }
    }
  }

  public static void main(final String[] args) {
    new CubicMeshVolumeNodeTest().start();
  }

  private boolean cubic = true;
  private boolean debug;
  private VolumeGrid<Bloxel> grid;
  private ImageAtlasBloxelAssetManager bloxelAssetManager;

  private void addMapping(final String name, final Trigger trigger) {
    getInputManager().addMapping(name, trigger);
    getInputManager().addListener(this, name);
  }

  private void fill(final VolumeGrid<Bloxel> grid) {
    rootNode.detachAllChildren();
    for (int z = -1; z < 1; z++) {
      for (int y = -1; y < 1; y++) {
        rootNode.attachChild(node(grid, 0, y, z));
      }
    }
  }

  private AbstractVolumeNode node(final VolumeGrid<Bloxel> grid, final int x, final int y, final int z) {
    final AbstractVolumeNode volumeNode = cubic ? new CubicMeshVolumeNode(grid, grid.getVolume(x, y, z),
        bloxelAssetManager) : new SmoothSurfaceVolumeNode(grid, grid.getVolume(x, y, z), bloxelAssetManager);
    volumeNode.calculate();
    volumeNode.update();
    volumeNode.debug(debug);
    return volumeNode;
  }

  @Override
  public void onAction(final String name, final boolean isPressed, final float tpf) {
    if (name.equals("cubic") && isPressed) {
      cubic = true;
      fill(grid);
    }
    if (name.equals("smooth") && isPressed) {
      cubic = false;
      fill(grid);
    }
    if (name.equals("debug") && isPressed) {
      debug = !debug;
      for (final Spatial n : rootNode.getChildren()) {
        ((AbstractVolumeNode) n).debug(debug);
      }
    }
  }

  @Override
  public void simpleInitApp() {
    bloxelAssetManager = new ImageAtlasBloxelAssetManager(assetManager);
    grid = new VolumeGrid<Bloxel>();
    grid.setGridSize(16, 4, 16);
    grid.setVolumeSize(16);
    // grid.setVolumeLoader(new BlockmaniaTerrainLoader("cPpCzKqBpVkQpVjP"));
    grid.setVolumeLoader(new DummyLoader());
    grid.setVolumeFactory(new ColtVolumeFactory<Bloxel>());
    grid.init();
    fill(grid);
    cam.setLocation(Vector3f.ZERO);
    cam.setFrustumFar(3000f);
    flyCam.setMoveSpeed(50);
    flyCam.setEnabled(true);
    final AmbientLight l = new AmbientLight();
    l.setColor(ColorRGBA.White.mult(3));
    rootNode.addLight(l);
    final FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
    final SSAOFilter ssaoFilter = new SSAOFilter();
    ssaoFilter.setEnabled(false);
    fpp.addFilter(ssaoFilter);
    viewPort.addProcessor(fpp);
    addMapping("cubic", new KeyTrigger(KeyInput.KEY_1));
    addMapping("smooth", new KeyTrigger(KeyInput.KEY_2));
    addMapping("debug", new KeyTrigger(KeyInput.KEY_SPACE));
  }
}
