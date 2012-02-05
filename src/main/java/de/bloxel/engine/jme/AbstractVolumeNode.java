package de.bloxel.engine.jme;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.WireBox;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.material.BloxelAssetManager;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public abstract class AbstractVolumeNode extends Node implements VolumeNode {

  static enum State {
    DIRTY, CALCULATED, UP2DATE
  }

  static final Vector3f NORMAL_UP = new Vector3f(0, 1, 0);
  static final Vector3f NORMAL_DOWN = new Vector3f(0, -1, 0);
  static final Vector3f NORMAL_RIGHT = new Vector3f(1, 0, 0);
  static final Vector3f NORMAL_LEFT = new Vector3f(-1, 0, 0);
  static final Vector3f NORMAL_FRONT = new Vector3f(0, 0, 1);
  static final Vector3f NORMAL_BACK = new Vector3f(0, 0, -1);

  private static final Logger LOG = Logger.getLogger(AbstractVolumeNode.class);

  private final Volume<Bloxel> volume;
  private final VolumeGrid<Bloxel> grid;
  private final List<Geometry> geometries = Lists.newArrayList();
  private State state;

  protected final BloxelAssetManager assetManager;

  AbstractVolumeNode(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume, final BloxelAssetManager assetManager) {
    super();
    attachChild(new Node("volume"));
    this.grid = grid;
    this.volume = volume;
    this.assetManager = assetManager;
    this.state = State.DIRTY;
  }

  @Override
  public boolean calculate() {
    if (state != State.DIRTY) {
      LOG.debug(String.format("'%s' is not dirty - skip calculation", this));
      return false;
    }
    LOG.debug(String.format("Calculate geometries for '%s'", this));
    final long startTime = System.currentTimeMillis();
    geometries.clear();
    geometries.addAll(createGeometries(grid, volume));
    state = State.CALCULATED;
    final float duration = System.currentTimeMillis() - startTime;
    LOG.debug("Calculate time was " + duration + "ms");
    return true;
  }

  /**
   * Subclasses have to implement this method. Here you must return {@link Geometry geometries} based on the given
   * {@link Volume}.
   * 
   * @param grid
   *          never <code>null</code>
   * @param volume
   *          never <code>null</code>
   */
  abstract List<Geometry> createGeometries(VolumeGrid<Bloxel> grid, Volume<Bloxel> volume);

  public void debug(final boolean b) {
    detachChildNamed("debug");
    if (b) {
      final Geometry debug = GeometryBuilder.geometry("debug")
          .mesh(new WireBox(volume.getSizeX() / 2, volume.getSizeY() / 2, volume.getSizeZ() / 2))
          .material(assetManager.getMaterial(-1, null)).get();
      debug.setQueueBucket(Bucket.Opaque);
      attachChild(debug);
      for (final Geometry g : geometries) {
        g.getMaterial().getAdditionalRenderState().setWireframe(true);
      }
    } else {
      for (final Geometry g : geometries) {
        g.getMaterial().getAdditionalRenderState().setWireframe(false);
      }
    }
  }

  /**
   * Update the geometries if necessary.
   */
  public void update() {
    if (state != State.CALCULATED) {
      LOG.debug(String.format("'%s' is not calculate - skip update", this));
      return;
    }
    LOG.debug(String.format("Update geometries for '%s'", this));
    final long startTime = System.currentTimeMillis();
    ((Node) getChild("volume")).detachAllChildren();
    for (final Geometry g : geometries) {
      ((Node) getChild("volume")).attachChild(g);
    }
    state = State.UP2DATE;
    final float duration = System.currentTimeMillis() - startTime;
    LOG.debug("Update time was " + duration + "ms");
  }
}