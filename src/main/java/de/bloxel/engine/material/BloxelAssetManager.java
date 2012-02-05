package de.bloxel.engine.material;

import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public interface BloxelAssetManager {
	
  public static enum BloxelFace {
    UP, DOWN, LEFT, RIGHT, FRONT, BACK
  }

  /**
   * @param bloxelType
   *          must not be <code>null</code>
   * @param face
   * @return the material for the given type
   */
  Material getMaterial(final Integer bloxelType, final BloxelFace face);

  /** 
   * Return the texture coordinates for a bloxel type and a face.
   * 
   * @param bloxelType
   *          must not be <code>null</code>
   * @param face
   * @return a list with four {@link Vector2f elements}
   */
  ImmutableList<Vector2f> getTextureCoordinates(final Integer bloxelType, final BloxelFace face);

  /**
   * @param bloxelType
   *          must not be <code>null</code>
   * @return <code>true</code> if the bloxel type represents a transparent material else <code>false</code>
   */
  boolean isTransparent(Integer bloxelType);
}
