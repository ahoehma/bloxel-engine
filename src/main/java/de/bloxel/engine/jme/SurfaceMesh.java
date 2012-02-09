package de.bloxel.engine.jme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;

import de.bloxel.engine.math.Vertex;

public class SurfaceMesh {
  private final List<Integer> index = new ArrayList<Integer>();
  private final List<Vertex> vertex = new ArrayList<Vertex>();

  private final Map<Vector3f, Integer> vertexMap = new HashMap<Vector3f, Integer>();
  private boolean reuseVertex = false;

  public SurfaceMesh() {

  }

  public void addTriangle(final int v1, final int v2, final int v3) {
    index.add(v1);
    index.add(v2);
    index.add(v3);
  }

  public int addVertex(final Vertex vertex) {
    int index = 0;
    if (reuseVertex) {
      final Integer _index = vertexMap.get(vertex.position);
      if (_index != null) {
        index = _index.intValue();
      } else {
        index = this.vertex.size();
        this.vertex.add(vertex);
        vertexMap.put(vertex.position, Integer.valueOf(index));
      }
    } else {
      index = this.vertex.size();
      this.vertex.add(vertex);
    }

    return index;
  }

  public void clear() {
    index.clear();
    vertex.clear();
    vertexMap.clear();
  }

  public Mesh createMesh() {
    final Mesh mesh = new Mesh();
    float[] vertices = null;
    float[] normals = null;
    float[] texCoords = null;
    float[] binormals = null;
    float[] tangents = null;
    int[] indices = null;

    vertices = new float[vertex.size() * 3];
    indices = new int[index.size()];
    normals = new float[vertex.size() * 3];
    texCoords = new float[vertex.size() * 3];
    tangents = new float[vertex.size() * 3];
    binormals = new float[vertex.size() * 3];
    for (int i = 0; i < vertex.size(); i++) {
      vertices[i * 3 + 0] = vertex.get(i).position.x;
      vertices[i * 3 + 1] = vertex.get(i).position.y;
      vertices[i * 3 + 2] = vertex.get(i).position.z;
    }
    for (int i = 0; i < index.size(); i++) {
      indices[i] = index.get(i);
    }
    for (int i = 0; i < vertex.size(); i++) {
      normals[i * 3 + 0] = vertex.get(i).normal.x;
      normals[i * 3 + 1] = vertex.get(i).normal.y;
      normals[i * 3 + 2] = vertex.get(i).normal.z;
    }
    // for (int i = 0; i < vertex.size(); i++) {
    // binormals[i * 3 + 0] = vertex.get(i).binormal.x;
    // binormals[i * 3 + 1] = vertex.get(i).binormal.y;
    // binormals[i * 3 + 2] = vertex.get(i).binormal.z;
    // }
    // for (int i = 0; i < vertex.size(); i++) {
    // tangents[i * 3 + 0] = vertex.get(i).tangent.x;
    // tangents[i * 3 + 1] = vertex.get(i).tangent.y;
    // tangents[i * 3 + 2] = vertex.get(i).tangent.z;
    // }
    for (int i = 0; i < vertex.size(); i++) {
      texCoords[i * 3 + 0] = vertex.get(i).texCoord.x;
      texCoords[i * 3 + 1] = vertex.get(i).texCoord.y;
      texCoords[i * 3 + 2] = vertex.get(i).material;
    }
    mesh.setBuffer(Type.Position, 3, vertices);
    mesh.setBuffer(Type.Index, 1, indices);
    mesh.setBuffer(Type.Normal, 3, normals);
    // mesh.setBuffer(Type.Tangent, 3, tangents);
    // mesh.setBuffer(Type.Binormal, 3, binormals);
    mesh.setBuffer(Type.Normal, 3, normals);
    mesh.setBuffer(Type.TexCoord, 3, texCoords);
    mesh.updateBound();
    mesh.updateCounts();
    mesh.setStatic();
    return mesh;
  }

  public void setReuseVertex(final boolean reuseVertex) {
    this.reuseVertex = reuseVertex;
  }
}