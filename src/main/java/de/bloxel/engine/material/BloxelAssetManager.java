/*******************************************************************************
 * Copyright (c) 2010, 2011 Bloxel Team.
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
 * Contributors:
 *     Klaus Hauschild
 *     Patrick Lorenz
 *     Stephan Dorer
 *     Andreas H�hmann - mymita.com
 *******************************************************************************/
package de.bloxel.engine.material;

import java.util.List;
import java.util.Set;

import com.jme3.material.Material;
import com.jme3.math.Vector2f;

/**
 * @author Andreas H�hmann
 * @since 0.2.0
 */
public interface BloxelAssetManager {

  /**
   * @return a collection of available bloxel types, not case sensitive
   */
  Set<Integer> getBloxelTypes();

  /**
   * @param bloxelType
   *          must not be <code>null</code>
   * @return the material for the given type
   */
  Material getMaterial(final Integer bloxelType);

  /**
   * Return the texture coordinates for a bloxel type and a face (left, right, up, down, front and back).
   * 
   * @param bloxelType
   *          must not be <code>null</code>
   * @param face
   *          (left, right, up, down, front and back)
   * @return a list with four {@link Vector2f elements}
   */
  List<Vector2f> getTextureCoordinates(final Integer bloxelType, final int face);

  /**
   * @param bloxelType
   *          must not be <code>null</code>
   * @return <code>true</code> if the bloxel type represents a transparent material else <code>false</code>
   */
  boolean isTransparent(Integer bloxelType);
}
