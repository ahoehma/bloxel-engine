package de.bloxel.engine.jme;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.data.VolumeGrid;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public abstract class AbstractVolumeNode extends Node implements VolumeNode {

  static enum State {
    DIRTY, CALCULATED, UP2DATE
  }

  private static final Logger LOG = Logger.getLogger(AbstractVolumeNode.class);

  private final Volume<Bloxel> volume;
  private final VolumeGrid<Bloxel> grid;
  private final List<Spatial> geometries = Lists.newArrayList();
  private State state;

  AbstractVolumeNode(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume) {
    this.grid = grid;
    this.volume = volume;
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
    detachAllChildren();
    for (final Spatial spatial : geometries) {
      attachChild(spatial);
    }
    state = State.UP2DATE;
    final float duration = System.currentTimeMillis() - startTime;
    LOG.debug("Update time was " + duration + "ms");
  }
}