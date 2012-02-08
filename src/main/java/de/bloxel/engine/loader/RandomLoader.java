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

import java.util.Random;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class RandomLoader implements BloxelLoader {

  @Override
  public void fill(final Volume<Bloxel> volume) {
    final Random r = new Random(19760901);
    for (int x = 0; x < volume.getSizeX(); x++) {
      for (int z = 0; z < volume.getSizeZ(); z++) {
        for (int y = 0; y < volume.getSizeY(); y++) {
          final Bloxel bloxel = new Bloxel(r.nextInt(8) + 1, r.nextFloat() / 10);
          volume.set(x, y, z, r.nextBoolean() ? bloxel : Bloxel.AIR);
        }
      }
    }
  }
}
