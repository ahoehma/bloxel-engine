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
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.shadow.PssmShadowRenderer;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.ColtVolumeFactory;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.loader.RandomLoader;
import de.bloxel.engine.material.ImageAtlasBloxelAssetManager;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class CubicMeshVolumeNodeTest extends SimpleApplication implements ActionListener {

  public static void main(final String[] args) {
    new CubicMeshVolumeNodeTest().start();
  }

  private boolean debug;
  private boolean lightning;
  private VolumeGrid<Bloxel> grid;
  private ImageAtlasBloxelAssetManager bloxelAssetManager;
  private SpotLight spot;
  private Vector2f screenCenter;
  private PssmShadowRenderer pssmRenderer;

  private void addMapping(final String name, final Trigger trigger) {
    getInputManager().addMapping(name, trigger);
    getInputManager().addListener(this, name);
  }

  private void fill(final VolumeGrid<Bloxel> grid) {
    rootNode.detachAllChildren();
    rootNode.attachChild(node(grid, 0, 0, 0));
    rootNode.attachChild(node(grid, -1, 0, 0));
    rootNode.attachChild(node(grid, -1, -1, 0));
    rootNode.attachChild(node(grid, -1, -1, -1));
  }

  private AbstractVolumeNode node(final VolumeGrid<Bloxel> grid, final int x, final int y, final int z) {
    final AbstractVolumeNode volumeNode = new CubicMeshVolumeNode(grid, grid.getVolume(x, y, z), assetManager,
        bloxelAssetManager);
    volumeNode.calculate();
    volumeNode.update();
    volumeNode.debug(debug);
    return volumeNode;
  }

  @Override
  public void onAction(final String name, final boolean isPressed, final float tpf) {
    if (name.equals("debug") && isPressed) {
      debug = !debug;
      for (final Spatial n : rootNode.getChildren()) {
        ((AbstractVolumeNode) n).debug(debug);
      }
    }
    if (name.equals("lightning") && isPressed) {
      lightning = !lightning;
      bloxelAssetManager.setLightning(lightning);
      fill(grid);
    }
  }

  public void setupLighting() {
    final AmbientLight al = new AmbientLight();
    al.setColor(ColorRGBA.White.mult(0.8f));
    rootNode.addLight(al);
    spot = new SpotLight();
    spot.setSpotRange(50);
    spot.setSpotInnerAngle(5 * FastMath.DEG_TO_RAD);
    spot.setSpotOuterAngle(20 * FastMath.DEG_TO_RAD);
    spot.setPosition(cam.getLocation());
    final Vector3f lightDirection = cam.getWorldCoordinates(screenCenter, 0).subtract(spot.getPosition());
    spot.setDirection(lightDirection);
    spot.setColor(ColorRGBA.White.mult(2));
    rootNode.addLight(spot);
  }

  @Override
  public void simpleInitApp() {
    bloxelAssetManager = new ImageAtlasBloxelAssetManager(assetManager);
    screenCenter = new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2);
    grid = new VolumeGrid<Bloxel>();
    grid.setGridSize(16, 4, 16);
    grid.setVolumeSize(16);
    // grid.setVolumeLoader(new BlockmaniaTerrainLoader("cPpCzKqBpVkQpVjP"));
    grid.setVolumeLoader(new RandomLoader());
    grid.setVolumeFactory(new ColtVolumeFactory<Bloxel>());
    grid.init();
    fill(grid);
    cam.setLocation(Vector3f.ZERO.add(0, 0, 50));
    cam.setFrustumFar(1000f);
    flyCam.setMoveSpeed(50);
    flyCam.setEnabled(true);
    // pssmRenderer = new PssmShadowRenderer(assetManager, 512, 1);
    // pssmRenderer.setLambda(0.55f);
    // pssmRenderer.setShadowIntensity(0.2f);
    // pssmRenderer.setCompareMode(CompareMode.Hardware);
    // pssmRenderer.setFilterMode(FilterMode.Bilinear);
    // pssmRenderer.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
    // pssmRenderer.displayDebug();
    // viewPort.addProcessor(pssmRenderer);
    addMapping("lightning", new KeyTrigger(KeyInput.KEY_L));
    addMapping("smooth", new KeyTrigger(KeyInput.KEY_2));
    addMapping("debug", new KeyTrigger(KeyInput.KEY_SPACE));
    setupLighting();
  }

  @Override
  public void simpleUpdate(final float tpf) {
    super.simpleUpdate(tpf);
    spot.setPosition(cam.getLocation());
    final Vector3f lightDirection = cam.getWorldCoordinates(screenCenter, 0).subtract(spot.getPosition());
    spot.setDirection(lightDirection.normalizeLocal());
  }
}