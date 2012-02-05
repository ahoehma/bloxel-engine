package de.bloxel.engine.jme;

import static com.google.common.collect.Lists.newArrayList;
import static com.jme3.renderer.queue.RenderQueue.Bucket.Transparent;
import static com.jme3.scene.VertexBuffer.Type.Index;
import static com.jme3.scene.VertexBuffer.Type.Normal;
import static com.jme3.scene.VertexBuffer.Type.Position;
import static com.jme3.scene.VertexBuffer.Type.TexCoord;
import static com.jme3.util.BufferUtils.createFloatBuffer;
import static com.jme3.util.BufferUtils.createIntBuffer;
import static de.bloxel.engine.jme.GeometryBuilder.geometry;
import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.toPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.material.BloxelAssetManager;

/**
 * This {@link VolumeNode} create a {@link Mesh}. The tesselation algorithm creates one mesh per
 * {@link de.bloxel.engine.data.Bloxel#getType()} in the {@link Volume bloxel volume}.
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class CubicMeshVolumeNode extends AbstractVolumeNode {

  private static final Logger LOG = Logger.getLogger(CubicMeshVolumeNode.class);

  private static final ArrayList<Vector3f> NORMALS_DOWNFACE = newArrayList(NORMAL_DOWN, NORMAL_DOWN, NORMAL_DOWN,
      NORMAL_DOWN);
  private static final ArrayList<Vector3f> NORMALS_UPFACE = newArrayList(NORMAL_UP, NORMAL_UP, NORMAL_UP, NORMAL_UP);
  private static final ArrayList<Vector3f> NORMALS_LEFTFACE = newArrayList(NORMAL_LEFT, NORMAL_LEFT, NORMAL_LEFT,
      NORMAL_LEFT);
  private static final ArrayList<Vector3f> NORMALS_RIGHTFACE = newArrayList(NORMAL_RIGHT, NORMAL_RIGHT, NORMAL_RIGHT,
      NORMAL_RIGHT);
  private static final ArrayList<Vector3f> NORMALS_FRONTFACE = newArrayList(NORMAL_FRONT, NORMAL_FRONT, NORMAL_FRONT,
      NORMAL_FRONT);
  private static final ArrayList<Vector3f> NORMALS_BACKFACE = newArrayList(NORMAL_BACK, NORMAL_BACK, NORMAL_BACK,
      NORMAL_BACK);

  private static final int FACE_NO = 0;
  private static final int FACE_RIGHT = 1;
  private static final int FACE_LEFT = 2;
  private static final int FACE_UP = 4;
  private static final int FACE_DOWN = 8;
  private static final int FACE_FRONT = 16;
  private static final int FACE_BACK = 32;

  /**
   * <pre>
   *     pg-----ph
   *    /|      /|
   *   pc-----pd |
   *   | pe----|pf
   *   |/      | /  
   *   pa-----pb
   *   
   *   2\2--3
   *   | \  | Counter-clockwise -> then this is the front of the polygon
   *   |  \ |
   *   0--1\1
   * 
   * </pre>
   */
  private static final ArrayList<Integer> TRIANGLE_INDIZES = Lists.newArrayList(2, 0, 1, 1, 3, 2);

  private final Multimap<Integer, Vector3f> vertices = ArrayListMultimap.create();
  private final Multimap<Integer, Vector3f> normals = ArrayListMultimap.create();
  private final Multimap<Integer, Vector2f> textureCoord = ArrayListMultimap.create();
  private final Multimap<Integer, Integer> indexes = ArrayListMultimap.create();

  public CubicMeshVolumeNode(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume,
      final BloxelAssetManager assetManager) {
    super(grid, volume, assetManager);
  }

  /**
   * Calculate which faces are needed for the given bloxel.
   * 
   * @param grid
   * @param volume
   * @param data
   * @param x
   * @param y
   * @param z
   * @return
   */
  private int checkFaces(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume, final Bloxel data, final int x,
      final int y, final int z) {
    LOG.trace(String.format("Check faces for %d, %d, %d", x, y, z));
    int faces = FACE_NO;
    if (needFace(grid, volume, data, x + 1, y, z)) {
      faces |= FACE_RIGHT;
    }
    if (needFace(grid, volume, data, x - 1, y, z)) {
      faces |= FACE_LEFT;
    }
    if (needFace(grid, volume, data, x, y + 1, z)) {
      faces |= FACE_UP;
    }
    if (needFace(grid, volume, data, x, y - 1, z)) {
      faces |= FACE_DOWN;
    }
    if (needFace(grid, volume, data, x, y, z + 1)) {
      faces |= FACE_BACK;
    }
    if (needFace(grid, volume, data, x, y, z - 1)) {
      faces |= FACE_FRONT;
    }
    LOG.trace(format("Faces [%s] for %d, %d, %d", facesToString(faces), x, y, z));
    return faces;
  }

  private void clear() {
    vertices.clear();
    normals.clear();
    textureCoord.clear();
    indexes.clear();
  }

  private boolean createFaces(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume, final Bloxel data,
      final int x, final int y, final int z) {
    final int faces = checkFaces(grid, volume, data, x, y, z);
    if ((faces & FACE_NO) > 0) {
      return false;
    }
    final float scale = 0.5f;
    final float wx = volume.getX() + x;
    final float wy = volume.getY() + y;
    final float wz = volume.getZ() + z;
    final int bloxelType = data.getType();
    /**
     * <pre>
     *     pg-----ph
     *    /|      /|
     *   pc-----pd |
     *   | pe----|pf
     *   |/      | /  
     *   pa-----pb
     *   
     *   2\2--3
     *   | \  | Counter-clockwise
     *   |  \ |
     *   0--1\1
     * 
     * </pre>
     */
    final Vector3f pa = new Vector3f(wx - scale, wy - scale, wz + scale);
    final Vector3f pb = new Vector3f(wx + scale, wy - scale, wz + scale);
    final Vector3f pc = new Vector3f(wx - scale, wy + scale, wz + scale);
    final Vector3f pd = new Vector3f(wx + scale, wy + scale, wz + scale);
    final Vector3f pe = new Vector3f(wx - scale, wy - scale, wz - scale);
    final Vector3f pf = new Vector3f(wx + scale, wy - scale, wz - scale);
    final Vector3f pg = new Vector3f(wx - scale, wy + scale, wz - scale);
    final Vector3f ph = new Vector3f(wx + scale, wy + scale, wz - scale);
    boolean materialUsed = false;
    if ((faces & FACE_BACK) > 0) {
      final int verticesSize = vertices.get(bloxelType).size();
      vertices.get(bloxelType).addAll(Lists.newArrayList(pa, pb, pc, pd));
      normals.get(bloxelType).addAll(NORMALS_BACKFACE);
      textureCoord.get(bloxelType).addAll(assetManager.getTextureCoordinates(bloxelType, FACE_BACK));
      indexes.get(bloxelType).addAll(verticesIndex(verticesSize, TRIANGLE_INDIZES));
      materialUsed = true;
    }
    if ((faces & FACE_FRONT) > 0) {
      final int verticesSize = vertices.get(bloxelType).size();
      vertices.get(bloxelType).addAll(Lists.newArrayList(pf, pe, ph, pg));
      normals.get(bloxelType).addAll(NORMALS_FRONTFACE);
      textureCoord.get(bloxelType).addAll(assetManager.getTextureCoordinates(bloxelType, FACE_FRONT));
      indexes.get(bloxelType).addAll(verticesIndex(verticesSize, TRIANGLE_INDIZES));
      materialUsed = true;
    }
    if ((faces & FACE_RIGHT) > 0) {
      final int verticesSize = vertices.get(bloxelType).size();
      vertices.get(bloxelType).addAll(Lists.newArrayList(pb, pf, pd, ph));
      normals.get(bloxelType).addAll(NORMALS_RIGHTFACE);
      textureCoord.get(bloxelType).addAll(assetManager.getTextureCoordinates(bloxelType, FACE_RIGHT));
      indexes.get(bloxelType).addAll(verticesIndex(verticesSize, TRIANGLE_INDIZES));
      materialUsed = true;
    }
    if ((faces & FACE_LEFT) > 0) {
      final int verticesSize = vertices.get(bloxelType).size();
      vertices.get(bloxelType).addAll(Lists.newArrayList(pe, pa, pg, pc));
      normals.get(bloxelType).addAll(NORMALS_LEFTFACE);
      textureCoord.get(bloxelType).addAll(assetManager.getTextureCoordinates(bloxelType, FACE_LEFT));
      indexes.get(bloxelType).addAll(verticesIndex(verticesSize, TRIANGLE_INDIZES));
      materialUsed = true;
    }
    if ((faces & FACE_UP) > 0) {
      final int verticesSize = vertices.get(bloxelType).size();
      vertices.get(bloxelType).addAll(Lists.newArrayList(pc, pd, pg, ph));
      normals.get(bloxelType).addAll(NORMALS_UPFACE);
      textureCoord.get(bloxelType).addAll(assetManager.getTextureCoordinates(bloxelType, FACE_UP));
      indexes.get(bloxelType).addAll(verticesIndex(verticesSize, TRIANGLE_INDIZES));
      materialUsed = true;
    }
    if ((faces & FACE_DOWN) > 0) {
      final int verticesSize = vertices.get(bloxelType).size();
      vertices.get(bloxelType).addAll(Lists.newArrayList(pe, pf, pa, pb));
      normals.get(bloxelType).addAll(NORMALS_DOWNFACE);
      textureCoord.get(bloxelType).addAll(assetManager.getTextureCoordinates(bloxelType, FACE_DOWN));
      indexes.get(bloxelType).addAll(verticesIndex(verticesSize, TRIANGLE_INDIZES));
      materialUsed = true;
    }
    return materialUsed;
  }

  @Override
  List<Geometry> createGeometries(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume) {
    LOG.debug(String.format("Tesselate volume %s", volume));
    clear();
    int c = 0;
    final Set<Integer> usedBloxeTypes = Sets.newHashSet();
    for (int x = 0; x < volume.getSizeX(); x++) {
      for (int z = 0; z < volume.getSizeZ(); z++) {
        for (int y = 0; y < volume.getSizeY(); y++) {
          final Bloxel data = volume.get(x, y, z);
          Preconditions.checkNotNull(data);
          if (data == Bloxel.AIR) {
            continue;
          }
          if (createFaces(grid, volume, data, x, y, z)) {
            c++;
            usedBloxeTypes.add(data.getType());
          }
        }
      }
    }
    LOG.debug("Found " + c + " boxes with " + usedBloxeTypes.size() + " different types");
    final List<Geometry> result = newArrayList();
    for (final Integer bloxelType : usedBloxeTypes) {
      LOG.debug("Build mesh for material " + bloxelType + " ...");
      final Mesh mesh = new Mesh();
      mesh.setBuffer(Position, 3, createFloatBuffer(vertices.get(bloxelType).toArray(new Vector3f[vertices.size()])));
      LOG.debug("Material " + bloxelType + " have " + vertices.get(bloxelType).size() + " vertices");
      mesh.setBuffer(Normal, 3, createFloatBuffer(normals.get(bloxelType).toArray(new Vector3f[normals.size()])));
      LOG.debug("Material " + bloxelType + " have " + normals.get(bloxelType).size() + " normals");
      mesh.setBuffer(TexCoord, 2,
          createFloatBuffer(textureCoord.get(bloxelType).toArray(new Vector2f[textureCoord.size()])));
      LOG.debug("Material " + bloxelType + " have " + textureCoord.get(bloxelType).size() + " texCoord");
      mesh.setBuffer(Index, 1, createIntBuffer(toPrimitive(indexes.get(bloxelType).toArray(new Integer[0]))));
      LOG.debug("Material " + bloxelType + " have " + indexes.get(bloxelType).size() + " indexes");
      mesh.updateBound();
      if (mesh.getVertexCount() != 0) {
        final Material material = assetManager.getMaterial(bloxelType);
        final Geometry geometry = geometry("bloxel-" + bloxelType).mesh(mesh).material(material).get();
        if (material.isTransparent()) {
          geometry.setQueueBucket(Transparent);
        }
        result.add(geometry);
      }
    }
    clear();
    return result;
  }

  private String facesToString(final int faces) {
    String result = "";
    if ((faces & FACE_BACK) > 0) {
      result += "back,";
    }
    if ((faces & FACE_FRONT) > 0) {
      result += "front,";
    }
    if ((faces & FACE_LEFT) > 0) {
      result += "left,";
    }
    if ((faces & FACE_RIGHT) > 0) {
      result += "right,";
    }
    if ((faces & FACE_UP) > 0) {
      result += "top,";
    }
    if ((faces & FACE_DOWN) > 0) {
      result += "bottom";
    }
    return result;
  }

  private boolean isTranslucentBloxel(final Bloxel checkBloxel) {
    return isTranslucentBloxel(checkBloxel.getType());
  }

  private boolean isTranslucentBloxel(final Integer bloxelType) {
    return assetManager.isTransparent(bloxelType);
  }

  private List<Vector2f> lightTextureCoord(final int lightValue) {
    // each image is 32x32, the whole image-atlas is 512x512
    // coord.x: 0..10
    // coord.y: 0..1
    final float sx = 32f / 512f;
    final float sy = 32f / 64f;
    final float x = lightValue * sx;
    final float y = sy;
    final Vector2f bottomLeft = new Vector2f(x, y);
    final Vector2f bottomRight = new Vector2f(x + sx, y);
    final Vector2f topLeft = new Vector2f(x, y + sy);
    final Vector2f topRight = new Vector2f(x + sx, y + sy);
    return Lists.newArrayList(bottomLeft, bottomRight, topLeft, topRight);
  }

  private boolean needFace(final Bloxel currentBloxel, final Bloxel neighborBloxelToCheck) {
    Preconditions.checkNotNull(currentBloxel);
    Preconditions.checkNotNull(neighborBloxelToCheck);
    if (neighborBloxelToCheck == Bloxel.AIR) {
      // neighbor bloxel is air then it doesn't matter if current bloxel is translucent we always need a face here
      return true;
    }
    if (isTranslucentBloxel(currentBloxel)) {
      // current bloxel is translucent
      // we need a face if the neighbor bloxel is not translucent
      // we need a face if the neighbor bloxel is also translucent but with a different type
      return !isTranslucentBloxel(neighborBloxelToCheck) || currentBloxel.getType() != neighborBloxelToCheck.getType();
    }
    // normal current bloxel
    // we need a face if the neighbor bloxel is translucent
    return isTranslucentBloxel(neighborBloxelToCheck);
  }

  /**
   * Check joint face for <code>currentBloxel</code> with neighborBloxel (defined by position x, y and z). If this
   * method return <code>true</code> the the <code>currentBloxel</code> need a (solid) face to separate currentBloxel
   * from the neighborBloxel.
   * 
   * @param grid
   *          the grid
   * @param v
   *          the volume
   * @param currentBloxel
   *          never <code>null</code>, never {@link Bloxel#AIR}
   * @param x
   * @param y
   * @param z
   * @return
   */
  private boolean needFace(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> v, final Bloxel currentBloxel,
      final int x, final int y, final int z) {
    Preconditions.checkNotNull(currentBloxel);
    Preconditions.checkArgument(currentBloxel != Bloxel.AIR);
    if (y == -1) {
      return needFace(currentBloxel, grid.get(x, y, z));
    }
    if (y == v.getSizeY()) {
      // this face would be created (or not) by the neighbor volume node
      // only if neighbor is air then this volume must have a face
      return grid.get(x, y, z) == Bloxel.AIR;
    }
    if (x == -1) {
      return needFace(currentBloxel, grid.get(x, y, z));
    }
    if (x == v.getSizeX()) {
      // this face would be created (or not) by the neighbor volume node
      // only if neighbor is air then this volume must have a face
      return grid.get(x, y, z) == Bloxel.AIR;
    }
    if (z == -1) {
      return needFace(currentBloxel, grid.get(x, y, z));
    }
    if (z == v.getSizeZ()) {
      // this face would be created (or not) by the neighbor volume node
      // only if neighbor is air then this volume must have a face
      return grid.get(x, y, z) == Bloxel.AIR;
    }
    return needFace(currentBloxel, v.get(x, y, z));
  }

  private ArrayList<Integer> verticesIndex(final int verticesSize, final ArrayList<Integer> indexes) {
    final ArrayList<Integer> result = Lists.newArrayList();
    for (final Integer i : indexes) {
      result.add(i + verticesSize);
    }
    return result;
  }
}