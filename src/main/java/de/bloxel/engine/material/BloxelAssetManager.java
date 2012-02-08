package de.bloxel.engine.material;

import com.google.common.collect.ImmutableList;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public interface BloxelAssetManager {

  public static enum BloxelSide {
    UP, DOWN, LEFT, RIGHT, FRONT, BACK
  }

  /**
   * The returned {@link Material} would be used for the {@link Geometry} created for the bloxel with the given type.
   * The engine will give the same {@link Material} for the same bloxel type textures. If the bloxel is defined
   * "transparent" the engine give a transparent {@link Material}.
   * 
   * @param bloxelType
   *          must not be <code>null</code>
   * @param side
   *          if not null then could be used to return a material for this side of the given bloxel type
   * @return the material for the given bloxel type
   */
  Material getMaterial(final Integer bloxelType, final BloxelSide side);

  /**
   * Return the texture coordinates for a bloxel type and a side.
   * 
   * @param bloxelType
   *          must not be <code>null</code>
   * @param side
   *          must not be <code>null</code>
   * @return a list with four {@link Vector2f elements}
   */
  ImmutableList<Vector2f> getTextureCoordinates(final Integer bloxelType, final BloxelSide side);

  /**
   * @param bloxelType
   *          must not be <code>null</code>
   * @return <code>true</code> if the bloxel type represents a transparent material else <code>false</code>
   */
  boolean isTransparent(Integer bloxelType);
}
