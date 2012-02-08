package de.bloxel.engine.jme;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

import de.bloxel.engine.data.Volume;

/**
 * A {@link VolumeNode} is the visual representation of a {@link Volume}.
 * 
 * A {@link VolumeNode}S must use the {@link #calculate() calculation method} to create {@link Geometry geometries} for
 * his volume information, i.e. using boxes, tesselation. If this method return <code>true</code> then later the
 * "world manager" will call the {@link #update()} method in the render loop to update the scene graph. The calculation
 * can be done in a parallel thread.
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public interface VolumeNode {

  /**
   * Prepare internal state for later update (if necessary), i.e. update chunk node geometries. <b>Here you must not
   * change the scene graph.</b> Just do the internal stuff. For scene graph update, see
   * {@link #update(Vector3f, Vector3f)}.
   * 
   * @return <code>true</code> if the calculation create a result which must update the scene graph else
   *         <code>false</code>
   */
  boolean calculate();

  boolean update();
}
