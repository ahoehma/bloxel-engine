package de.bloxel.engine.jme;

import org.apache.log4j.Logger;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

public class WorldNodeUpdateControl extends AbstractControl {

  private static final Logger LOG = Logger.getLogger(WorldNodeUpdateControl.class);

  private final WorldNode world;
  private final Camera camera;

  /**
   * Last Ray for last position of world update.
   */
  private Ray lastRay;

  /**
   * Only uses the first camera right now.
   * 
   * @param world
   *          to act upon (must be a Spatial)
   * @param camera
   *          one cameras to reference for update
   */
  public WorldNodeUpdateControl(final WorldNode world, final Camera camera) {
    this.world = world;
    this.camera = camera;
  }

  @Override
  public Control cloneForSpatial(final Spatial spatial) {
    return null;
  }

  @Override
  protected void controlRender(final RenderManager rm, final ViewPort vp) {
  }

  @Override
  protected void controlUpdate(final float tpf) {
    final Vector3f location = camera.getLocation();
    final Vector3f direction = camera.getDirection();
    if (world.needUpdate()) {
      LOG.debug("World need a update");
      world.update(location, direction);
      return;
    }
    final Ray ray = new Ray(location, direction);
    if (needUpdate(ray)) {
      LOG.debug("Player position changed, trigger world update");
      world.update(location, direction);
    }
  }

  private boolean needUpdate(final Ray ray) {
    if (lastRay == null) {
      lastRay = ray.clone();
      return true;
    }
    if (lastRay.origin.distance(ray.origin) > world.getChunkSize() / 2 // wenn x meter von der letzten position
        || !lastRay.direction.equals(ray.direction) // TODO oder wenn x grad vom letzten blickwinkel
    ) {
      lastRay = ray.clone();
      return true;
    }
    return false;
  }
}