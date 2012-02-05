package de.bloxel.engine.math;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class Vertex {
  public Vector3f position;
  public Vector3f normal;
  public Vector2f texCoord;
  public int material;
  public Vector3f tangent;
  public Vector3f binormal;

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Vertex) {
      final Vertex other = (Vertex) obj;
      return position.equals(other.position) && normal.equals(other.normal) && texCoord.equals(other.texCoord)
          && material == other.material && tangent.equals(other.tangent) && binormal.equals(other.binormal);
    } else {
      return true;
    }
  }
}