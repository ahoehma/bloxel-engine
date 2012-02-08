package cave3d;

import java.util.HashMap;
import java.util.Stack;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

/**
 * @author mazander
 */
public class CaveGenerator extends SimpleApplication {

  private final class PolygonizationThread implements Runnable {
    private final Stack<CaveTriMesh> stack = new Stack<CaveTriMesh>();

    private final Object lock = new Object();

    private void addcaveMesh(final Vector3f center) {
      if (caveMeshes.get(center) == null) {

        final CaveTriMesh caveInstance = new CaveTriMesh(center, MESH_SIZE);

        caveInstance.setMaterial(caveMaterial);

        stack.add(caveInstance);
        caveMeshes.put(caveInstance.getCenter(), caveInstance);
      }

      synchronized (lock) {
        lock.notify();
      }

    }

    @Override
    public void run() {
      while (true) {
        while (!stack.isEmpty()) {
          final CaveTriMesh caveInstance = stack.pop();
          polygonisator.calculate(caveInstance, caveInstance.getWorldCenter(), 0f);
          caveInstance.setModelBound(new BoundingBox());
          caveInstance.updateModelBound();

          instancesToAdd.add(0, caveInstance);
        }
        try {
          synchronized (lock) {
            lock.wait();
          }
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private final static long SEED = "jme".hashCode();

  private final static float MESH_SIZE = 64f;

  private final static float HALF_MESH_SIZE = MESH_SIZE / 2f;

  public static void main(final String[] args) {
    final CaveGenerator app = new CaveGenerator();
    app.start();
  }

  private final HashMap<Vector3f, CaveTriMesh> caveMeshes = new HashMap<Vector3f, CaveTriMesh>();

  private int lastMeshCount = 0;

  private Node caveNode = new Node("cave node");

  private PointLight pl = new PointLight();
  private CaveScalarField scalarField;

  private ScalarFieldPolygonisator polygonisator;

  private final Vector3f key = new Vector3f();

  private final Vector3f camGridCoord = new Vector3f();

  private final PolygonizationThread generatorThread = new PolygonizationThread();

  private final BoundingBox box = new BoundingBox(new Vector3f(), HALF_MESH_SIZE, HALF_MESH_SIZE, HALF_MESH_SIZE);

  private final Stack<CaveTriMesh> instancesToAdd = new Stack<CaveTriMesh>();

  private Material caveMaterial;

  @Override
  public void simpleInitApp() {

    settings.setTitle("Cave Generator");

    /*
     * lightState.setLocalViewer(true); lightState.setEnabled(true);
     * 
     * PointLight pl = (PointLight)lightState.get(0);
     */
    pl = new PointLight();
    pl.setRadius(210);
    rootNode.addLight(pl);

    caveNode = new Node();
    scalarField = new CaveScalarField(SEED, 128f, 4f);
    polygonisator = new ScalarFieldPolygonisator(MESH_SIZE, 8, scalarField, true, false);

    /*
     * FilterPostProcessor fpp=new FilterPostProcessor(assetManager); SSAOFilter ssaoFilter= new
     * SSAOFilter(0.92f,2.2f,0.46f,0.4f); fpp.addFilter(ssaoFilter);
     * 
     * FogFilter fog=new FogFilter(); fog.setFogColor(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f)); fog.setFogDistance(505);
     * fog.setFogDensity(2.0f); fpp.addFilter(fog);
     * 
     * viewPort.addProcessor(fpp);
     */

    caveMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    caveMaterial.setColor("Color", ColorRGBA.Blue);

    // caveMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    // Texture diffuse = assetManager.loadTexture("Textures/stone.jpg");
    // diffuse.setWrap(Texture.WrapMode.Repeat);
    // diffuse.setMagFilter(Texture.MagFilter.Bilinear);
    // diffuse.setMinFilter(Texture.MinFilter.Trilinear);
    // Texture normal = assetManager.loadTexture("Textures/stone-normal.jpg");
    // normal.setMagFilter(Texture.MagFilter.Bilinear);
    // normal.setMinFilter(Texture.MinFilter.Trilinear);
    // normal.setWrap(Texture.WrapMode.Repeat);
    // caveMaterial.setTexture("m_DiffuseMap", diffuse);
    // caveMaterial.setTexture("m_NormalMap", normal);
    // caveMaterial.setColor("m_Specular", ColorRGBA.White);
    // caveMaterial.setFloat("m_Shininess", 0.25f);
    //
    rootNode.attachChild(caveNode);

    final float aspect = (float) settings.getWidth() / (float) settings.getHeight();
    cam.setFrustumPerspective(75f, aspect, 1, 4 * MESH_SIZE);

    getFlyByCamera().setMoveSpeed(50f);

    final Thread thread = new Thread(generatorThread, "Generator Thread");
    thread.start();
  }

  @Override
  public void simpleUpdate(final float tpf) {

    // TODO: set pl config
    pl.getPosition().set(cam.getLocation());

    camGridCoord.set(cam.getLocation()).divideLocal(MESH_SIZE);
    camGridCoord.set(Math.round(camGridCoord.x), Math.round(camGridCoord.y), Math.round(camGridCoord.z));

    final int d = 3;
    for (int x = -d; x <= d; x++) {
      for (int y = -d; y <= d; y++) {
        for (int z = -d; z <= d; z++) {
          key.set(camGridCoord).addLocal(x, y, z);
          box.getCenter().set(key).multLocal(MESH_SIZE);
          if (cam.contains(box) != Camera.FrustumIntersect.Outside) {
            generatorThread.addcaveMesh(key);
          }
        }
      }
    }

    while (!instancesToAdd.isEmpty()) {
      // get the first element, to make sure nothing gets "buried" in the stack
      caveNode.attachChild(instancesToAdd.pop());
      // instancesToAdd.remove(0);
      System.out.println("stack size " + instancesToAdd.size());
    }

    final long time = System.currentTimeMillis();
    final int quantity = caveNode.getQuantity();
    for (int i = quantity - 1; i >= 0; i--) {
      final CaveTriMesh caveInstance = (CaveTriMesh) caveNode.getChild(i);
      final Vector3f worldCenter = caveInstance.getWorldBound().getCenter();
      if (worldCenter.distance(cam.getLocation()) > MESH_SIZE * 6) {
        caveNode.detachChildAt(i);
        caveMeshes.remove(caveInstance.getCenter());
      }
    }

    if (caveMeshes.size() != lastMeshCount) {
      lastMeshCount = caveMeshes.size();
      // caveNode.updateGeometricState();
      System.out.println("Mesh count: " + caveNode.getQuantity());
    }
  }

}