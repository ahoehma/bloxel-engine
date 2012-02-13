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
import static de.bloxel.engine.util.JAXBUtils.unmarschal;
import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.testng.collections.Sets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector2f;
import com.jme3.texture.Texture;

import de.bloxel.engine.resources.TextureAtlasProvider;
import de.bloxel.engine.types.BloxelType;
import de.bloxel.engine.types.Side;
import de.bloxel.engine.types.SideType;
import de.bloxel.engine.types.Types;

/**
 * This {@link BloxelAssetManager} return {@link Material textured material} for bloxel types.
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class ImageAtlasBloxelAssetManager implements BloxelAssetManager {

  private static final Logger LOG = Logger.getLogger(ImageAtlasBloxelAssetManager.class);

  private final Map<String, Material> sideTextureMaterial = Maps.newHashMap();
  private final Map<Integer, Material> bloxelMaterial = Maps.newHashMap();
  private final Map<Integer, BloxelType> bloxel = Maps.newHashMap();
  private final Set<Integer> transparent = Sets.newHashSet();
  private final TextureAtlasProvider atlasProvider;
  private final AssetManager assetManager;
  private boolean lighting;

  /**
   * @param assetManager
   *          for loading textures etc.
   */
  public ImageAtlasBloxelAssetManager(final AssetManager assetManager) {
    this.assetManager = assetManager;
    this.assetManager.registerLocator("/de/bloxel/engine/resources/", ClasspathLocator.class);
    this.atlasProvider = new TextureAtlasProvider(assetManager);
    loadMaterials();
  }

  @Override
  public Material getMaterial(final Integer bloxelType, final BloxelSide side) {
    checkNotNull(bloxelType);
    if (side == null) {
      return bloxelMaterial.get(bloxelType);
    }
    return sideTextureMaterial.get(getTextureId(bloxelType, side));
  }

  @Override
  public ImmutableList<Vector2f> getTextureCoordinates(final Integer bloxelType, final BloxelSide side) {
    checkNotNull(bloxelType);
    checkNotNull(side);
    return atlasProvider.getTextureCoordinates(checkNotNull(getTextureId(bloxelType, side),
        format("no texture id for type '%d', side '%s'", bloxelType, side)));
  }

  String getTextureId(final Integer id, final BloxelSide face) {
    checkNotNull(id);
    checkNotNull(face);
    String result = null;
    for (final Side f : checkNotNull(bloxel.get(id), format("Unknown bloxel id '%d'", id)).getSide()) {
      if (f.getType() == SideType.UP && face == BloxelSide.UP) {
        result = f.getTextureId();
        break;
      }
      if (f.getType() == SideType.DOWN && face == BloxelSide.DOWN) {
        result = f.getTextureId();
        break;
      }
      if (f.getType() == SideType.LEFT && face == BloxelSide.LEFT) {
        result = f.getTextureId();
        break;
      }
      if (f.getType() == SideType.RIGHT && face == BloxelSide.RIGHT) {
        result = f.getTextureId();
        break;
      }
      if (f.getType() == SideType.BACK && face == BloxelSide.BACK) {
        result = f.getTextureId();
        break;
      }
      if (f.getType() == SideType.FRONT && face == BloxelSide.FRONT) {
        result = f.getTextureId();
        break;
      }
    }
    return result;
  }

  @Override
  public boolean isTransparent(final Integer bloxelType) {
    return transparent.contains(bloxelType);
  }

  protected Types load() {
    InputStream inputStream = null;
    try {
      inputStream = new ClassPathResource("bloxel-types.xml").getInputStream();
      return unmarschal(inputStream, Types.class);
    } catch (final IOException e) {
      throw new RuntimeException("Can't load bloxel-types.xml", e);
    } finally {
      closeQuietly(inputStream);
    }
  }

  private Material loadMaterial(final Texture texture) {
    if (lighting) {
      final Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
      material.setTexture("DiffuseMap", texture);
      material.setBoolean("SeparateTexCoord", true);
      return material;
    }
    final Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    material.setTexture("ColorMap", texture);
    material.setBoolean("SeparateTexCoord", true);
    return material;
  }

  private void loadMaterials() {
    bloxel.clear();
    bloxelMaterial.clear();
    sideTextureMaterial.clear();
    transparent.clear();
    for (final BloxelType b : load().getBloxel()) {
      for (final Side side : b.getSide()) {
        bloxel.put(b.getId(), b);
        final String sideTextureId = side.getTextureId();
        final Texture texture = this.atlasProvider.getTexture(sideTextureId);
        checkNotNull(texture,
            format("Missing texture with id '%s' for bloxel '%d', side '%s'", sideTextureId, b.getId(), side.getType()));
        final Material material = loadMaterial(texture);
        if (b.isTransparent()) {
          transparent.add(b.getId());
          material.setTransparent(true);
          material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        }
        bloxelMaterial.put(b.getId(), material);
        sideTextureMaterial.put(sideTextureId, material);
      }
    }
  }

  public void setLightning(final boolean lightning) {
    if (lightning != this.lighting) {
      this.lighting = lightning;
      loadMaterials();
    }
  }
}