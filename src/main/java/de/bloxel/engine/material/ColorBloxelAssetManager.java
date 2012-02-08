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

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class ColorBloxelAssetManager implements BloxelAssetManager {

  private static class MapKey extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

    public MapKey(final Integer bloxelType, final int face) {
      add(bloxelType);
      add(face);
    }
  }

  private static final String COLORMAP_PROPERTIES = "colormap.properties";
  private static final Set<Integer> TRANSPARENT_BOXELS = Sets.newHashSet(7, 6);

  private final Map<MapKey, ColorRGBA> colormap = Maps.newHashMap();
  private final Map<MapKey, Material> colorcache = Maps.newHashMap();
  private final AssetManager assetManager;

  /**
   * @param theAssetManager
   *          for loading textures etc.
   */
  public ColorBloxelAssetManager(final AssetManager theAssetManager) {
    this.assetManager = theAssetManager;
    try {
      final Properties p = new Properties();
      p.load(new ClassPathResource(COLORMAP_PROPERTIES).getInputStream());
      final Set<Entry<Object, Object>> definitions = p.entrySet();
      for (final Entry<Object, Object> d : definitions) {
        final Integer bloxelType = Integer.valueOf(d.getKey().toString());
        final String value = String.format("%s", d.getValue());
        final Iterable<String> rgb = Splitter.on(",").split(value);
        final float r = Integer.valueOf(Iterables.get(rgb, 0));
        final float g = Integer.valueOf(Iterables.get(rgb, 1));
        final float b = Integer.valueOf(Iterables.get(rgb, 2));
        colormap.put(new MapKey(bloxelType, -1), new ColorRGBA(r, g, b, 1f));
      }
    } catch (final IOException e) {
      throw new RuntimeException(format("Can't load bloxel definition file '%s' from classpath", COLORMAP_PROPERTIES),
          e);
    }
    theAssetManager.registerLocator("/de/bloxel/engine/material/", ClasspathLocator.class);
  }

  @Override
  public Material getMaterial(final Integer bloxelType, final BloxelSide face) {
    final MapKey key = new MapKey(bloxelType, -1);
    if (colorcache.containsKey(key)) {
      return colorcache.get(key);
    }
    final Material bloxelMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    bloxelMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
    bloxelMaterial.setColor("Color", colormap.get(key));
    if (isTransparent(bloxelType)) {
      bloxelMaterial.setTransparent(true);
      bloxelMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    }
    colorcache.put(key, bloxelMaterial);
    return bloxelMaterial;
  }

  @Override
  public ImmutableList<Vector2f> getTextureCoordinates(final Integer bloxelType, final BloxelSide face) {
    return ImmutableList.of(new Vector2f(0, 0), new Vector2f(1, 0), new Vector2f(0, 1), new Vector2f(1, 1));
  }

  @Override
  public boolean isTransparent(final Integer bloxelType) {
    return TRANSPARENT_BOXELS.contains(bloxelType);
  }
}