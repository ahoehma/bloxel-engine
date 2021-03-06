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
package de.bloxel.engine.resources;

import static com.google.common.io.Closeables.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.ImmutableList;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;

import de.bloxel.engine.util.JAXBUtils;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class TextureAtlasProvider {

  private static final Logger LOG = Logger.getLogger(TextureAtlasProvider.class);

  private final Map<String, ImmutableList<Vector2f>> textureCoordinates = Maps.newHashMap();
  private final Map<String, com.jme3.texture.Texture> textures = Maps.newHashMap();

  /**
   * @param theAssetManager
   *          for loading textures etc.
   */
  public TextureAtlasProvider(final AssetManager theAssetManager) {
    for (final TextureAtlas ta : load().getTextureAtlas()) {
      for (final Texture t : ta.getTexture()) {
        textures.put(t.getId(), theAssetManager.loadTexture(ta.getImage()));
        textureCoordinates.put(t.getId(),
            uvMapTexture(ta.getAtlasSize(), ta.getImageSize(), t.getTextureColum(), t.getTextureRow()));
      }
    }
    LOG.info(String.format("Load %d textures '%s'", textures.size(), textures));
  }

  public com.jme3.texture.Texture getTexture(final String textureId) {
    return textures.get(textureId);
  }

  public ImmutableList<Vector2f> getTextureCoordinates(final String textureId) {
    return textureCoordinates.get(textureId);
  }

  protected Resources load() {
    InputStream inputStream = null;
    try {
      inputStream = new ClassPathResource("bloxel-resources.xml").getInputStream();
      return JAXBUtils.unmarschal(inputStream, Resources.class);
    } catch (final IOException e) {
      throw new RuntimeException("Can't load bloxel-resources.xml", e);
    } finally {
      closeQuietly(inputStream);
    }
  }

  /**
   * @param atlasSize
   *          size of the whole atlas image in pixel
   * @param imageSize
   *          size of each tile image in pixel
   * @param col
   *          horizontal position of the texture tile starting with <code>0</code>
   * @param row
   *          vertical position of the texture tile starting with <code>0</code>
   * @return
   */
  private ImmutableList<Vector2f> uvMapTexture(final float atlasSize, final float imageSize, final int col,
      final int row) {
    final float s = imageSize / atlasSize;
    final float x = col * s;
    final float y = row * s;
    final Vector2f bottomLeft = new Vector2f(x, y);
    final Vector2f bottomRight = new Vector2f(x + s, y);
    final Vector2f topLeft = new Vector2f(x, y + s);
    final Vector2f topRight = new Vector2f(x + s, y + s);
    return ImmutableList.of(bottomLeft, bottomRight, topLeft, topRight);
  }
}