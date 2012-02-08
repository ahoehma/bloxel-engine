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
package de.bloxel.engine.material;

import static com.google.common.collect.ImmutableList.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.logging.Handler;
import java.util.logging.LogManager;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testng.annotations.Test;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.system.JmeSystem;

import de.bloxel.engine.material.BloxelAssetManager.BloxelSide;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class ImageAtlasBloxelAssetManagerTest {

  static {
    final java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
    final Handler[] handlers = rootLogger.getHandlers();
    for (int i = 0; i < handlers.length; i++) {
      rootLogger.removeHandler(handlers[i]);
    }
    SLF4JBridgeHandler.install();
  }

  private final AssetManager assetManager = JmeSystem.newAssetManager(Thread.currentThread().getContextClassLoader()
      .getResource("com/jme3/asset/Desktop.cfg"));

  @Test
  public void testLoading() throws Exception {
    final ImageAtlasBloxelAssetManager imageAtlasProvider = new ImageAtlasBloxelAssetManager(assetManager);
    assertNotNull(imageAtlasProvider.getMaterial(1, BloxelSide.UP));
    assertEquals(imageAtlasProvider.getTextureCoordinates(1, BloxelSide.UP),
        of(new Vector2f(0, 0.9375f), new Vector2f(0.0625f, 0.9375f), new Vector2f(0, 1), new Vector2f(0.0625f, 1)));
    assertTrue(imageAtlasProvider.isTransparent(6));
    assertTrue(imageAtlasProvider.isTransparent(7));
  }
}
