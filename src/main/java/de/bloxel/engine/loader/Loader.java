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
package de.bloxel.engine.loader;

import de.bloxel.engine.data.Volume;

/**
 * A Loader initialize a {@link Volume}.
 * 
 * @param <T>
 *          type of created elements, needs a {@link Volume} which can handle this type of elements
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public interface Loader<T> {

  /**
   * @param volume
   *          to fill
   */
  void fill(Volume<T> volume);
}