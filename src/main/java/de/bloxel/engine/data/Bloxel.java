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

import static java.lang.String.format;

/**
 * The smallest volume information in the bloxel engine.
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class Bloxel {

  public static final Bloxel AIR = new Bloxel(-1);

  private final int type;
  private final float density;

  public Bloxel(final int type) {
    this(type, 0);
  }

  public Bloxel(final int type, final float density) {
    this.type = type;
    this.density = density;
  }

  public float getDensity() {
    return density;
  }

  public int getType() {
    return type;
  }

  @Override
  public String toString() {
    return format("type: %s, density: %f", type, density);
  }
}
