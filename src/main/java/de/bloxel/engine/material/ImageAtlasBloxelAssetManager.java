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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.cinematic.events.MotionTrack.Direction;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Vector2f;

import de.bloxel.engine.resources.TextureAtlasProvider;
import de.bloxel.engine.types.Face;
import de.bloxel.engine.types.Type;
import de.bloxel.engine.types.Types;

/**
 * This {@link BloxelAssetManager} return {@link Material textured material} for bloxel types.
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class ImageAtlasBloxelAssetManager implements BloxelAssetManager {

  @SuppressWarnings("serial")
  private static class Key extends ArrayList<Object> {
    public Key(final Integer bloxelType, final Direction direction) {
      add(bloxelType);
      add(direction);
    }
  }

  private static final String TEXTURE = "texture/";

  private final TextureAtlasProvider atlasProvider;
  private final Set<Integer> types = Sets.newHashSet();
  private final Map<Key, Vector2f> uvmap = Maps.newHashMap();
  private final Material bloxelMaterial;
  private final Material bloxelMaterialTransparent;
  private boolean wireframe;

  /**
   * @param theAssetManager
   *          for loading textures etc.
   */
  public ImageAtlasBloxelAssetManager(final AssetManager theAssetManager) {
    theAssetManager.registerLocator("/de/bloxel/engine/resources/", ClasspathLocator.class);
    atlasProvider = new TextureAtlasProvider(theAssetManager);
    for (final Type t : load().getType()) {
      for (final de.bloxel.engine.types.Texture te : t.getTexture()) {
        final Face face = te.getFace();
        final String textureId = te.getTextureId();
        checkNotNull(atlasProvider.getTexture(textureId));
      }
      for (final Face f : t.getFace()) {
        uvmap.put(new Key(t.getId(), f.getDirection()), new Vector2f(f.getTextureX(), f.getTextureY()));
      }
      types.add(t.getId());
    }

    bloxelMaterial = new Material(theAssetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    // bloxelMaterial.setTexture("DiffuseMap", texture);
    bloxelMaterial.setTexture("ColorMap", texture);
    // bloxelMaterial.setTexture("NormalMap", normalMap);
    // bloxelMaterial.setTexture("LightMap", lightMap);
    bloxelMaterial.setBoolean("SeparateTexCoord", true);
    // bloxelMaterial.setBoolean("VertexLighting", true); // need to avoid shader error! "missing vNormal" ?!
    bloxelMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
    // bloxelMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);

    bloxelMaterialTransparent = new Material(theAssetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    // bloxelMaterialTransparent.setTexture("DiffuseMap", texture);
    bloxelMaterialTransparent.setTexture("ColorMap", texture);
    // bloxelMaterialTransparent.setTexture("NormalMap", normalMap);
    // bloxelMaterialTransparent.setTexture("LightMap", lightMap);
    bloxelMaterialTransparent.setBoolean("SeparateTexCoord", true);
    // bloxelMaterialTransparent.setBoolean("VertexLighting", true);
    bloxelMaterialTransparent.setTransparent(true);
    bloxelMaterialTransparent.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
    // bloxelMaterialTransparent.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
    // Use this if you have several transparent objects obscuring
    // one another. Disables writing of the pixel's depth value to
    // the depth buffer.
    // bloxelMaterialTransparent.getAdditionalRenderState().setDepthWrite(false);
  }

  @Override
  public Set<Integer> getBloxelTypes() {
    return types;
  }

  @Override
  public Material getMaterial(final Integer bloxelType) {

  }

  @Override
  public List<Vector2f> getTextureCoordinates(final Integer bloxelType, final int face) {
    return uvMapTexture(uvmap.get(new Key(bloxelType)));
  }

  @Override
  public boolean isTransparent(final Integer bloxelType) {
    return false;
  }

  public boolean isWireframe() {
    return wireframe;
  }

  protected Types load() {
    InputStream inputStream = null;
    try {
      inputStream = new ClassPathResource("bloxel.xml").getInputStream();
    } catch (final IOException e) {
    }
    try {
      // http://jaxb.java.net/faq/index.html#classloader
      final JAXBContext jc = JAXBContext.newInstance(Types.class.getPackage().getName(), getClass().getClassLoader());
      final Unmarshaller unmarshaller = jc.createUnmarshaller();
      // http://jaxb.java.net/guide/Unmarshalling_is_not_working__Help_.html
      unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
      final JAXBElement<Types> unmarshal = (JAXBElement<Types>) unmarshaller.unmarshal(inputStream);
      return unmarshal.getValue();
    } catch (final JAXBException e) {
    } finally {
      Closeables.closeQuietly(inputStream);
    }
    return null;
  }

  public void setWireframe(final boolean wireframe) {
    this.wireframe = wireframe;
    bloxelMaterial.getAdditionalRenderState().setWireframe(wireframe);
  }

  private List<Vector2f> uvMapTexture(final Vector2f coord) {
    if (coord == null) {
      // use complete image as texture
      final Vector2f bottomLeft = new Vector2f(0, 0);
      final Vector2f bottomRight = new Vector2f(1, 0);
      final Vector2f topLeft = new Vector2f(0, 1);
      final Vector2f topRight = new Vector2f(1, 1);
      return Lists.newArrayList(bottomLeft, bottomRight, topLeft, topRight);
    }
    // each image is 32x32, the whole image-atlas is 512x512
    // coord.x: 0..15
    // coord.y: 0..15
    final float s = 32f / 512f;
    final float x = coord.x * s;
    final float y = coord.y * s;
    final Vector2f bottomLeft = new Vector2f(x, y);
    final Vector2f bottomRight = new Vector2f(x + s, y);
    final Vector2f topLeft = new Vector2f(x, y + s);
    final Vector2f topRight = new Vector2f(x + s, y + s);
    return Lists.newArrayList(bottomLeft, bottomRight, topLeft, topRight);
  }

}