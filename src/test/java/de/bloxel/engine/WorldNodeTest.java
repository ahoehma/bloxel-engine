package de.bloxel.engine;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.debug.WireBox;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.ColtVolumeFactory;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.jme.WorldNode;
import de.bloxel.engine.jme.WorldNodeUpdateControl;
import de.bloxel.engine.loader.BlockmaniaTerrainLoader;
import de.bloxel.engine.material.ImageAtlasBloxelAssetManager;

public class WorldNodeTest extends SimpleApplication {

  public static void main(final String[] args) {
    new WorldNodeTest().start();
  }

  @Override
  public void simpleInitApp() {
    final VolumeGrid<Bloxel> grid = new VolumeGrid<Bloxel>();
    grid.setGridSize(16, 4, 16);
    grid.setVolumeSize(64);
    grid.setVolumeLoader(new BlockmaniaTerrainLoader("cPpCzKqBpVkQpVjP"));
    grid.setVolumeFactory(new ColtVolumeFactory<Bloxel>());
    grid.init();

    final WorldNode worldNode = new WorldNode(grid, new ImageAtlasBloxelAssetManager(assetManager));
    final WorldNodeUpdateControl wnuc = new WorldNodeUpdateControl(worldNode, cam);
    worldNode.initPlayerPosition(new Vector3f(0, 70, 0));
    rootNode.attachChild(worldNode);
    final WorldNodeUpdateControl wnuc = new WorldNodeUpdateControl(worldNode, cam);
    worldNode.addControl(wnuc);

    cam.setLocation(initialPlayerPosition);
    cam.setFrustumFar(3000f);
    flyCam.setMoveSpeed(50);
    flyCam.setEnabled(true);

    pickerControl = new PickerControl(worldNode, cam, -1);
    pickerControl.setEnabled(true);
    worldNode.addControl(pickerControl);
    final WireBox wb = new WireBox(0.5f, 0.5f, 0.5f);
    wb.setLineWidth(4.0f);
    wb.setStatic();
    pickerMarkerBox = new Geometry("marker", wb);
    final Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    material.setColor("Color", ColorRGBA.Red);
    pickerMarkerBox.setMaterial(material);
    pickerMarkerBox.setQueueBucket(Bucket.Transparent);
    rootNode.attachChild(pickerMarkerBox);

    final AmbientLight ambientLight = new AmbientLight();
    ambientLight.setColor(ColorRGBA.Yellow.mult(2));
    // rootNode.addLight(ambientLight);
    final DirectionalLight sunDirectionalLight = new DirectionalLight();
    sunDirectionalLight.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
    sunDirectionalLight.setColor(ColorRGBA.White);
    rootNode.addLight(sunDirectionalLight);

    // final FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
    // final SSAOFilter ssaoFilter = new SSAOFilter();
    // fpp.addFilter(ssaoFilter);
    // viewPort.addProcessor(fpp);

    inputManager.addMapping("Remove", new MouseButtonTrigger(0));
    inputManager.addListener(new ActionListener() {
      @Override
      public void onAction(final String name, final boolean pressed, final float tpf) {
        if ("Remove".equals(name) && !pressed) {
          final PickResult p = pickerControl.getPickResult();
          if (p != null) {
            worldNode.removeBloxel(p.bloxel.getCenter());
            pickerControl.reset();
          }
        }
      }
    }, "Remove");
    inputManager.addMapping("Add", new MouseButtonTrigger(1));
    inputManager.addListener(new ActionListener() {
      @Override
      public void onAction(final String name, final boolean pressed, final float tpf) {
        if ("Add".equals(name) && !pressed) {
          final PickResult p = pickerControl.getPickResult();
          if (p != null) {
            final Vector3f nl = p.contactNormal;
            final Vector3f np = p.bloxel.getCenter().add(nl);
            worldNode.setBloxel(np, 0); // add debug bloxel
            pickerControl.reset();
          }
        }
      }
    }, "Add");

    inputManager.addMapping("PickerToogle", new KeyTrigger(KeyInput.KEY_P));
    inputManager.addListener(new ActionListener() {
      @Override
      public void onAction(final String name, final boolean pressed, final float tpf) {
        if ("PickerToogle".equals(name) && !pressed) {
          pickerControl.setEnabled(!pickerControl.isEnabled());
        }
      }
    }, "PickerToogle");

    inputManager.addMapping("WireframeToogle", new KeyTrigger(KeyInput.KEY_O));
    inputManager.addListener(new ActionListener() {
      @Override
      public void onAction(final String name, final boolean pressed, final float tpf) {
        if ("WireframeToogle".equals(name) && !pressed) {
          bloxelFactory.setWireframe(!bloxelFactory.isWireframe());
        }
      }
    }, "WireframeToogle");
  }

  @Override
  public void simpleUpdate(final float tpf) {
    final Vector3f p = cam.getLocation();
    playerPositionTex.setText(String.format("pos: %.2f;%.2f;%.2f (%d/%d chunks, %d chunks)", p.x, p.y, p.z,
        worldNode.getVisibleChunkCount(), worldNode.getLoadedChunkCount(), worldNode.getChunkToFillCount()));
    memoryTex.setText(String.format("Mem: %08.2f/%08.2f", Runtime.getRuntime().freeMemory() / 1024f, Runtime
        .getRuntime().totalMemory() / 1024f));
    final PickResult pickResult = pickerControl.getPickResult();
    if (pickResult != null) {
      final BoundingBox bb = pickResult.chunk.getVolume().getBoundingBox();
      final Vector3f center = pickResult.bloxel.getCenter();
      pickerMarkerBox.setLocalTranslation(center);
      // pickerMarkerBox.setLocalScale(1.05f);
      pickerMarkerBox.setCullHint(CullHint.Never);
      pickerDebugText.setText(String.format("bloxel(%.2f,%.2f,%.2f,%d)\n" + //
          "chunk(x:%.2f %.2f %.2f/" + //
          "y:%.2f %.2f %.2f/" + //
          "z:%.2f %.2f %.2f)", //
          center.x, center.y, center.z, pickResult.bloxel.getType(), //
          bb.getCenter().x - bb.getXExtent(), bb.getCenter().x, bb.getCenter().x + bb.getXExtent(),//
          bb.getCenter().y - bb.getYExtent(), bb.getCenter().y, bb.getCenter().y + bb.getYExtent(),//
          bb.getCenter().z - bb.getZExtent(), bb.getCenter().z, bb.getCenter().z + bb.getZExtent()//
      ));
    } else {
      pickerMarkerBox.setCullHint(CullHint.Always);
      pickerDebugText.setText("");
    }
  }
}
