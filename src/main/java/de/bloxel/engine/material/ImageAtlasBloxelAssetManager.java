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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Vector2f;
import com.jme3.texture.Texture;

import de.bloxel.engine.resources.TextureAtlasProvider;
import de.bloxel.engine.types.Face;
import de.bloxel.engine.types.FaceType;
import de.bloxel.engine.types.Type;
import de.bloxel.engine.types.Types;
import de.bloxel.engine.util.JAXBUtils;

/**
 * This {@link BloxelAssetManager} return {@link Material textured material} for bloxel types.
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class ImageAtlasBloxelAssetManager implements BloxelAssetManager {

  private final TextureAtlasProvider atlasProvider;
  private final Map<String, Material> texturMaterial = Maps.newHashMap();
  private final Map<Integer, Type> types = Maps.newHashMap();
  private final AssetManager assetManager;

  /**
   * @param assetManager
   *          for loading textures etc.
   */
  public ImageAtlasBloxelAssetManager(final AssetManager assetManager) {
    this.assetManager = assetManager;
    this.assetManager.registerLocator("/de/bloxel/engine/resources/", ClasspathLocator.class);
    this.atlasProvider = new TextureAtlasProvider(assetManager);
    for (final Type t : load().getType()) {
      for (final Face f : t.getFace()) {
        final String textureId = f.getTextureId();
        final Texture texture = this.atlasProvider.getTexture(textureId);
        checkNotNull(texture,
            format("Missing texture '%s' for type '%d', face '%s'", textureId, t.getId(), f.getFaceType()));
        addMaterial(textureId, texture);
      }
      types.put(t.getId(), t);
    }
  }

  private void addMaterial(final String textureId, final Texture texture) {
    if (texturMaterial.containsKey(textureId)) {
      return;
    }
    final Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    material.setTexture("ColorMap", texture);
    material.setBoolean("SeparateTexCoord", true);
    material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
    texturMaterial.put(textureId, material);
  }

  @Override
  public Material getMaterial(final Integer bloxelType, final BloxelFace face) {
    Preconditions.checkNotNull(bloxelType);
    Preconditions.checkNotNull(face);
    Material result = null;
    for (final Face f : types.get(bloxelType).getFace()) {
      if (f.getFaceType() == FaceType.ALL) {
        result = texturMaterial.get(f.getTextureId());
      }
      if (f.getFaceType() == FaceType.SIDES && face != FACE.UP && face != BloxelFace.DOWN) {
        result = texturMaterial.get(f.getTextureId());
      }
      if (f.getFaceType() == FaceType.UP && face != FACE.UP) {
        result = texturMaterial.get(f.getTextureId());
        break;
      }
      if (f.getFaceType() == FaceType.DOWN && face != FACE.DOWN) {
        result = texturMaterial.get(f.getTextureId());
        break;
      }
      if (f.getFaceType() == FaceType.LEFT && face != FACE.LEFT) {
        result = texturMaterial.get(f.getTextureId());
        break;
      }
      if (f.getFaceType() == FaceType.RIGHT && face != FACE.RIGHT) {
        result = texturMaterial.get(f.getTextureId());
        break;
      }
      if (f.getFaceType() == FaceType.BACK && face != FACE.BACK) {
        result = texturMaterial.get(f.getTextureId());
        break;
      }
      if (f.getFaceType() == FaceType.FRONT && face != FACE.FRONT) {
        result = texturMaterial.get(f.getTextureId());
        break;
      }
    }
    return result;
  }

  @Override
  public List<Vector2f> getTextureCoordinates(final Integer bloxelType, final BloxelFace face) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Vector2f> getTextureCoordinates(final Integer bloxelType, final FACE face) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isTransparent(final Integer bloxelType) {
    // TODO Auto-generated method stub
    return false;
  }

  protected Types load() {
    InputStream inputStream = null;
    try {
      inputStream = new ClassPathResource("bloxel-types.xml").getInputStream();
      return JAXBUtils.unmarschal(inputStream, Types.class);
    } catch (final IOException e) {
      throw new RuntimeException("Can't load bloxel-types.xml", e);
    } finally {
      closeQuietly(inputStream);
    }
  }
}