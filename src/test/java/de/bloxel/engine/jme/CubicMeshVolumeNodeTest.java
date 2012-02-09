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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.slf4j.bridge.SLF4JBridgeHandler;

import cave3d.CaveScalarField;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import de.bloxel.engine.concurrent.VolumeNodeUpdater;
import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.ColtVolumeFactory;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.loader.ScalarFieldTerrainLoader;
import de.bloxel.engine.material.ImageAtlasBloxelAssetManager;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class CubicMeshVolumeNodeTest extends SimpleApplication implements ActionListener {

  static {
    final java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
    final Handler[] handlers = rootLogger.getHandlers();
    for (int i = 0; i < handlers.length; i++) {
      rootLogger.removeHandler(handlers[i]);
    }
    SLF4JBridgeHandler.install();
  }

  public static void main(final String[] args) {
    new CubicMeshVolumeNodeTest().start();
  }

  private boolean debug;
  private boolean lightning = false;
  private VolumeGrid<Bloxel> grid;
  private ImageAtlasBloxelAssetManager bloxelAssetManager;
  private SpotLight spot;
  private Vector2f screenCenter;
  private final LinkedBlockingQueue<AbstractVolumeNode> output = new LinkedBlockingQueue<AbstractVolumeNode>();
  private final LinkedBlockingQueue<AbstractVolumeNode> input = new LinkedBlockingQueue<AbstractVolumeNode>(2);
  private final ExecutorService threadPool = Executors.newFixedThreadPool(8, new ThreadFactory() {
    @Override
    public Thread newThread(final Runnable r) {
      final Thread th = new Thread(r);
      th.setDaemon(true);
      return th;
    }
  });

  private void addMapping(final String name, final Trigger trigger) {
    getInputManager().addMapping(name, trigger);
    getInputManager().addListener(this, name);
  }

  private AbstractVolumeNode node(final VolumeGrid<Bloxel> grid, final int x, final int y, final int z) {
    return new CubicMeshVolumeNode(grid, grid.getVolume(x, y, z), assetManager, bloxelAssetManager);
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
    }
  }

  public void setupLighting() {
    final AmbientLight al = new AmbientLight();
    al.setColor(ColorRGBA.White.mult(0.8f));
    rootNode.addLight(al);
    final DirectionalLight sun = new DirectionalLight();
    sun.setColor(ColorRGBA.Yellow.mult(2f));
    sun.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
    rootNode.addLight(sun);
    // spot = new SpotLight();
    // spot.setSpotRange(50);
    // spot.setSpotInnerAngle(5 * FastMath.DEG_TO_RAD);
    // spot.setSpotOuterAngle(20 * FastMath.DEG_TO_RAD);
    // spot.setPosition(cam.getLocation());
    // final Vector3f lightDirection = cam.getWorldCoordinates(screenCenter, 0).subtract(spot.getPosition());
    // spot.setDirection(lightDirection);
    // spot.setColor(ColorRGBA.White.mult(2));
    // rootNode.addLight(spot);
  }

  @Override
  public void simpleInitApp() {
    bloxelAssetManager = new ImageAtlasBloxelAssetManager(assetManager);
    bloxelAssetManager.setLightning(lightning);
    screenCenter = new Vector2f(settings.getWidth() / 2, settings.getHeight() / 2);
    grid = new VolumeGrid<Bloxel>();
    grid.setGridSize(32, 32, 32);
    grid.setVolumeSize(16);
    // grid.setVolumeLoader(new PerlinNoiseTerrainLoader());
    // grid.setVolumeLoader(new BlockmaniaTerrainLoader("cPpCzKqBpVkQpVjP"));
    // grid.setVolumeLoader(new RandomLoader());
    final CaveScalarField scalarField = new CaveScalarField("jme".hashCode(), 128f, 1f);
    grid.setVolumeLoader(new ScalarFieldTerrainLoader(scalarField));
    grid.setVolumeFactory(new ColtVolumeFactory<Bloxel>());
    grid.init();
    cam.setLocation(Vector3f.ZERO.add(0, 0, 50));
    cam.setFrustumFar(1000f);
    flyCam.setMoveSpeed(50);
    flyCam.setEnabled(true);
    addMapping("lightning", new KeyTrigger(KeyInput.KEY_L));
    addMapping("debug", new KeyTrigger(KeyInput.KEY_SPACE));
    setupLighting();
    threadPool.execute(new VolumeNodeUpdater<AbstractVolumeNode>(input, output));
    threadPool.execute(new VolumeNodeUpdater<AbstractVolumeNode>(input, output));
    threadPool.execute(new VolumeNodeUpdater<AbstractVolumeNode>(input, output));
    // threadPool.execute(new VolumeNodeUpdater<AbstractVolumeNode>(input, output));
    // threadPool.execute(new VolumeNodeUpdater<AbstractVolumeNode>(input, output));
    // threadPool.execute(new VolumeNodeUpdater<AbstractVolumeNode>(input, output));
    // threadPool.execute(new VolumeNodeUpdater<AbstractVolumeNode>(input, output));
    threadPool.submit(new Runnable() {

      @Override
      public void run() {
        final int min = -8;
        final int max = 8;
        for (int y = min; y <= max; y++) {
          for (int z = min; z <= max; z++) {
            for (int x = min; x <= max; x++) {
              try {
                input.put(node(grid, x, y, z));
              } catch (final InterruptedException e) {
              }
            }
          }
        }
      }
    });
  }

  @Override
  public void simpleUpdate(final float tpf) {
    super.simpleUpdate(tpf);
    // spot.setPosition(cam.getLocation());
    // spot.setDirection(cam.getWorldCoordinates(screenCenter, 0).subtract(spot.getPosition()).normalizeLocal());
    for (int cpf = 0; cpf < 6; cpf++) {
      final AbstractVolumeNode n = output.poll();
      if (n != null) {
        n.debug(debug);
        rootNode.attachChild(n);
      }
    }
  }
}