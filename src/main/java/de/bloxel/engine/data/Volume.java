/*******************************************************************************
 * Copyright (c) 2012 Andreas Höhmann
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
 *******************************************************************************/
package de.bloxel.engine.data;

/**
 * A three dimensional volume of information points.
 * 
 * @param <T>
 *          type of elements in the volume
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public interface Volume<T> {

  /**
   * Remove all elements.
   */
  void clear();

  /**
   * @param x
   *          from 0 to {@link #getSizeX()}
   * @param y
   *          from 0 to {@link #getSizeY()}
   * @param z
   *          from 0 to {@link #getSizeZ()}
   * 
   * @return the {@link Bloxel} at the given position, maybe <code>null</code>
   */
  T get(int x, int y, int z);

  /**
   * @return dimension x
   */
  int getSizeX();

  /**
   * @return dimension y (height)
   */
  int getSizeY();

  /**
   * @return dimension z
   */
  int getSizeZ();

  /**
   * @return position x of the volume in the universe
   */
  int getX();

  /**
   * @return position y of the volume in the universe
   */
  int getY();

  /**
   * @return position z of the volume in the universe
   */
  int getZ();

  /**
   * @param x
   *          from 0 to {@link #getSizeX()}
   * @param y
   *          from 0 to {@link #getSizeY()}
   * @param z
   *          from 0 to {@link #getSizeZ()}
   * @param bloxel
   */
  void set(int x, int y, int z, T bloxel);
}
