package de.bloxel.engine.jme;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;

/**
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public final class GeometryBuilder {

  public static GeometryBuilder geometry() {
    return new GeometryBuilder("");
  }

  public static GeometryBuilder geometry(final String name) {
    return new GeometryBuilder(name);
  }

  private final Geometry g;

  public GeometryBuilder(final String name) {
    g = new Geometry(name);
  }

  public Geometry get() {
    return g;
  }

  public GeometryBuilder localTranslation(final float x, final float y, final float z) {
    g.setLocalTranslation(x, y, z);
    return this;
  }

  public GeometryBuilder material(final Material material) {
    g.setMaterial(material);
    return this;
  }

  public GeometryBuilder mesh(final Mesh mesh) {
    g.setMesh(mesh);
    return this;
  }
}