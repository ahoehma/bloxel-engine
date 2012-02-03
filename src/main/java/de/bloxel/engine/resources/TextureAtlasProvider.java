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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.core.io.ClassPathResource;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.texture.Texture;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class TextureAtlasProvider {

  private final Map<String, List<Vector2f>> textureCoordinates = Maps.newHashMap();
  private final Map<String, Texture> textures = Maps.newHashMap();

  /**
   * @param theAssetManager
   *          for loading textures etc.
   */
  public TextureAtlasProvider(final AssetManager theAssetManager) {
    for (final TextureAtlas ta : load().getTextureAtlas()) {
      final Texture texture = theAssetManager.loadTexture(ta.getImage());
      for (final de.bloxel.engine.resources.Texture t : ta.getTexture()) {
        textures.put(t.getId(), texture);
        textureCoordinates.put(t.getId(),
            uvMapTexture(ta.getAtlasSize(), ta.getImageSize(), t.getTextureX(), t.getTextureY()));
      }
    }
  }

  public Texture getTexture(final String id) {
    return textures.get(id);
  }

  public List<Vector2f> getTextureCoordinates(final String id) {
    return textureCoordinates.get(id);
  }

  protected Resources load() {
    InputStream inputStream = null;
    try {
      inputStream = new ClassPathResource("resources.xml").getInputStream();
    } catch (final IOException e) {
      throw new RuntimeException("Can't load resources.xml", e);
    }
    try {
      // http://jaxb.java.net/faq/index.html#classloader
      final JAXBContext jc = JAXBContext.newInstance(Resources.class.getPackage().getName(), getClass()
          .getClassLoader());
      final Unmarshaller unmarshaller = jc.createUnmarshaller();
      // http://jaxb.java.net/guide/Unmarshalling_is_not_working__Help_.html
      unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
      final JAXBElement<Resources> unmarshal = (JAXBElement<Resources>) unmarshaller.unmarshal(inputStream);
      return unmarshal.getValue();
    } catch (final JAXBException e) {
      throw new RuntimeException("Can't load resources.xml", e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }

  private List<Vector2f> uvMapTexture(final int atlasSize, final int imageSize, final int col, final int row) {
    final float s = imageSize / atlasSize;
    final float x = col * s;
    final float y = row * s;
    final Vector2f bottomLeft = new Vector2f(x, y);
    final Vector2f bottomRight = new Vector2f(x + s, y);
    final Vector2f topLeft = new Vector2f(x, y + s);
    final Vector2f topRight = new Vector2f(x + s, y + s);
    return Lists.newArrayList(bottomLeft, bottomRight, topLeft, topRight);
  }
}