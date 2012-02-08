package de.bloxel.engine.jme;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
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
    DIRTY, NEEDUPDATE, UP2DATE
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

  protected final BloxelAssetManager bloxelAssetManager;
  private final AssetManager assetManager;

  AbstractVolumeNode(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume, final AssetManager assetManager,
      final BloxelAssetManager bloxelAssetManager) {
    super();
    attachChild(new Node("volume"));
    this.grid = grid;
    this.volume = volume;
    this.assetManager = assetManager;
    this.bloxelAssetManager = bloxelAssetManager;
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
    state = State.NEEDUPDATE;
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
      final float sizeX = volume.getSizeX() / 2;
      final float sizeY = volume.getSizeY() / 2;
      final float sizeZ = volume.getSizeZ() / 2;
      final Geometry debug = GeometryBuilder.geometry("debug").mesh(new WireBox(sizeX, sizeY, sizeZ))
          .material(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")).get();
      debug.setQueueBucket(Bucket.Translucent);
      debug.setShadowMode(ShadowMode.Off);
      debug.setLocalTranslation(new Vector3f(volume.getX() + sizeX, volume.getY() + sizeY, volume.getZ() + sizeZ));
      attachChild(debug);
    }
  }

  @Override
  public boolean update() {
    if (state != State.NEEDUPDATE) {
      LOG.debug(String.format("'%s' need no update", this));
      return false;
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
    return true;
  }
}