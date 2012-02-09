package de.bloxel.engine.jme;

import static com.google.common.collect.Lists.newArrayList;
import static com.jme3.renderer.queue.RenderQueue.Bucket.Transparent;
import static de.bloxel.engine.jme.GeometryBuilder.geometry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.material.BloxelAssetManager;
import de.bloxel.engine.math.Vertex;

/**
 * This is a java port of Cory Bloyd's Marching Cubes implementation. Original code can be found at
 * http://paulbourke.net/geometry/polygonise/marchingsource.cpp. The code is based on this discription
 * http://astronomy.swin.edu.au/pbourke/modelling/polygonise/
 * 
 * TODO blending http://www.wheatchex.com/projects/mcubes/
 */
public class SmoothSurfaceVolumeNode extends AbstractVolumeNode {

  public enum Mode {
    MARCHING_CUBES, MARCHING_TETRAHEDRON;
  }

  private static final Logger LOG = Logger.getLogger(SmoothSurfaceVolumeNode.class);

  private Mode mode = Mode.MARCHING_CUBES;

  private final Map<Integer, SurfaceMesh> mesh = Maps.newHashMap();

  private static final float a2fVertexOffset[][] = {
      {
          0.0f, 0.0f, 0.0f
      }, {
          1.0f, 0.0f, 0.0f
      }, {
          1.0f, 1.0f, 0.0f
      }, {
          0.0f, 1.0f, 0.0f
      }, {
          0.0f, 0.0f, 1.0f
      }, {
          1.0f, 0.0f, 1.0f
      }, {
          1.0f, 1.0f, 1.0f
      }, {
          0.0f, 1.0f, 1.0f
      }
  };

  private static final int a2iEdgeConnection[][] = {
      {
          0, 1
      }, {
          1, 2
      }, {
          2, 3
      }, {
          3, 0
      }, {
          4, 5
      }, {
          5, 6
      }, {
          6, 7
      }, {
          7, 4
      }, {
          0, 4
      }, {
          1, 5
      }, {
          2, 6
      }, {
          3, 7
      }
  };

  private static final float a2fEdgeDirection[][] = {
      {
          1.0f, 0.0f, 0.0f
      }, {
          0.0f, 1.0f, 0.0f
      }, {
          -1.0f, 0.0f, 0.0f
      }, {
          0.0f, -1.0f, 0.0f
      }, {
          1.0f, 0.0f, 0.0f
      }, {
          0.0f, 1.0f, 0.0f
      }, {
          -1.0f, 0.0f, 0.0f
      }, {
          0.0f, -1.0f, 0.0f
      }, {
          0.0f, 0.0f, 1.0f
      }, {
          0.0f, 0.0f, 1.0f
      }, {
          0.0f, 0.0f, 1.0f
      }, {
          0.0f, 0.0f, 1.0f
      }
  };

  private static final int a2iTetrahedronEdgeConnection[][] = {
      {
          0, 1
      }, {
          1, 2
      }, {
          2, 0
      }, {
          0, 3
      }, {
          1, 3
      }, {
          2, 3
      }
  };

  private static final int a2iTetrahedronsInACube[][] = {
      {
          0, 5, 1, 6
      }, {
          0, 1, 2, 6
      }, {
          0, 2, 3, 6
      }, {
          0, 3, 7, 6
      }, {
          0, 7, 4, 6
      }, {
          0, 4, 5, 6
      },
  };

  private static final int aiTetrahedronEdgeFlags[] = {
      0x00, 0x0d, 0x13, 0x1e, 0x26, 0x2b, 0x35, 0x38, 0x38, 0x35, 0x2b, 0x26, 0x1e, 0x13, 0x0d, 0x00,
  };

  private static final int a2iTetrahedronTriangles[][] = {
      {
          -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 3, 2, -1, -1, -1, -1
      }, {
          0, 1, 4, -1, -1, -1, -1
      }, {
          1, 4, 2, 2, 4, 3, -1
      },

      {
          1, 2, 5, -1, -1, -1, -1
      }, {
          0, 3, 5, 0, 5, 1, -1
      }, {
          0, 2, 5, 0, 5, 4, -1
      }, {
          5, 4, 3, -1, -1, -1, -1
      },

      {
          3, 4, 5, -1, -1, -1, -1
      }, {
          4, 5, 0, 5, 2, 0, -1
      }, {
          1, 5, 0, 5, 3, 0, -1
      }, {
          5, 2, 1, -1, -1, -1, -1
      },

      {
          3, 4, 2, 2, 4, 1, -1
      }, {
          4, 1, 0, -1, -1, -1, -1
      }, {
          2, 3, 0, -1, -1, -1, -1
      }, {
          -1, -1, -1, -1, -1, -1, -1
      },
  };

  private static final int aiCubeEdgeFlags[] = {
      0x000,
      0x109,
      0x203,
      0x30a,
      0x406,
      0x50f,
      0x605,
      0x70c,
      0x80c,
      0x905,
      0xa0f,
      0xb06,
      0xc0a,
      0xd03,
      0xe09,
      0xf00,
      0x190,
      0x099,
      0x393,
      0x29a,
      0x596,
      0x49f,
      0x795,
      0x69c,
      0x99c,
      0x895,
      0xb9f,
      0xa96,
      0xd9a,
      0xc93,
      0xf99,
      0xe90,
      0x230,
      0x339,
      0x033,
      0x13a,
      0x636,
      0x73f,
      0x435,
      0x53c,
      0xa3c,
      0xb35,
      0x83f,
      0x936,
      0xe3a,
      0xf33,
      0xc39,
      0xd30,
      0x3a0,
      0x2a9,
      0x1a3,
      0x0aa,
      0x7a6,
      0x6af,
      0x5a5,
      0x4ac,
      0xbac,
      0xaa5,
      0x9af,
      0x8a6,
      0xfaa,
      0xea3,
      0xda9,
      0xca0,
      0x460,
      0x569,
      0x663,
      0x76a,
      0x066,
      0x16f,
      0x265,
      0x36c,
      0xc6c,
      0xd65,
      0xe6f,
      0xf66,
      0x86a,
      0x963,
      0xa69,
      0xb60,
      0x5f0,
      0x4f9,
      0x7f3,
      0x6fa,
      0x1f6,
      0x0ff,
      0x3f5,
      0x2fc,
      0xdfc,
      0xcf5,
      0xfff,
      0xef6,
      0x9fa,
      0x8f3,
      0xbf9,
      0xaf0,
      0x650,
      0x759,
      0x453,
      0x55a,
      0x256,
      0x35f,
      0x055,
      0x15c,
      0xe5c,
      0xf55,
      0xc5f,
      0xd56,
      0xa5a,
      0xb53,
      0x859,
      0x950,
      0x7c0,
      0x6c9,
      0x5c3,
      0x4ca,
      0x3c6,
      0x2cf,
      0x1c5,
      0x0cc,
      0xfcc,
      0xec5,
      0xdcf,
      0xcc6,
      0xbca,
      0xac3,
      0x9c9,
      0x8c0,
      0x8c0,
      0x9c9,
      0xac3,
      0xbca,
      0xcc6,
      0xdcf,
      0xec5,
      0xfcc,
      0x0cc,
      0x1c5,
      0x2cf,
      0x3c6,
      0x4ca,
      0x5c3,
      0x6c9,
      0x7c0,
      0x950,
      0x859,
      0xb53,
      0xa5a,
      0xd56,
      0xc5f,
      0xf55,
      0xe5c,
      0x15c,
      0x055,
      0x35f,
      0x256,
      0x55a,
      0x453,
      0x759,
      0x650,
      0xaf0,
      0xbf9,
      0x8f3,
      0x9fa,
      0xef6,
      0xfff,
      0xcf5,
      0xdfc,
      0x2fc,
      0x3f5,
      0x0ff,
      0x1f6,
      0x6fa,
      0x7f3,
      0x4f9,
      0x5f0,
      0xb60,
      0xa69,
      0x963,
      0x86a,
      0xf66,
      0xe6f,
      0xd65,
      0xc6c,
      0x36c,
      0x265,
      0x16f,
      0x066,
      0x76a,
      0x663,
      0x569,
      0x460,
      0xca0,
      0xda9,
      0xea3,
      0xfaa,
      0x8a6,
      0x9af,
      0xaa5,
      0xbac,
      0x4ac,
      0x5a5,
      0x6af,
      0x7a6,
      0x0aa,
      0x1a3,
      0x2a9,
      0x3a0,
      0xd30,
      0xc39,
      0xf33,
      0xe3a,
      0x936,
      0x83f,
      0xb35,
      0xa3c,
      0x53c,
      0x435,
      0x73f,
      0x636,
      0x13a,
      0x033,
      0x339,
      0x230,
      0xe90,
      0xf99,
      0xc93,
      0xd9a,
      0xa96,
      0xb9f,
      0x895,
      0x99c,
      0x69c,
      0x795,
      0x49f,
      0x596,
      0x29a,
      0x393,
      0x099,
      0x190,
      0xf00,
      0xe09,
      0xd03,
      0xc0a,
      0xb06,
      0xa0f,
      0x905,
      0x80c,
      0x70c,
      0x605,
      0x50f,
      0x406,
      0x30a,
      0x203,
      0x109,
      0x000
  };

  private static final int a2iTriangleConnectionTable[][] = {
      {
          -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 8, 3, 9, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 3, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 2, 10, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 8, 3, 2, 10, 8, 10, 9, 8, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 11, 2, 8, 11, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 9, 0, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 11, 2, 1, 9, 11, 9, 8, 11, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 10, 1, 11, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 10, 1, 0, 8, 10, 8, 11, 10, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 9, 0, 3, 11, 9, 11, 10, 9, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 3, 0, 7, 3, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 10, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 4, 7, 3, 0, 4, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 2, 10, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, -1
      }, {
          8, 4, 7, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          11, 4, 7, 11, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 0, 1, 8, 4, 7, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, -1, -1, -1, -1
      }, {
          3, 10, 1, 3, 11, 10, 7, 8, 4, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, -1, -1, -1, -1
      }, {
          4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, -1, -1, -1, -1
      }, {
          4, 7, 11, 4, 11, 9, 9, 11, 10, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 10, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 0, 8, 1, 2, 10, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 2, 10, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, -1
      }, {
          9, 5, 4, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 11, 2, 0, 8, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 5, 4, 0, 1, 5, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, -1, -1, -1, -1
      }, {
          10, 3, 11, 10, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, -1, -1, -1, -1
      }, {
          5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, -1, -1, -1, -1
      }, {
          5, 4, 8, 5, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 7, 8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 7, 8, 9, 5, 7, 10, 1, 2, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, -1
      }, {
          8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, -1, -1, -1, -1
      }, {
          2, 10, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1
      }, {
          7, 9, 5, 7, 8, 9, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, -1, -1, -1, -1
      }, {
          2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1, -1, -1
      }, {
          11, 2, 1, 11, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, -1, -1, -1, -1
      }, {
          5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, -1
      }, {
          11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, -1
      }, {
          11, 10, 5, 7, 11, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 3, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 0, 1, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 8, 3, 1, 9, 8, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1, -1, -1
      }, {
          2, 3, 11, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          11, 0, 8, 11, 2, 0, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 1, 9, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, -1, -1, -1, -1
      }, {
          6, 3, 11, 6, 5, 3, 5, 1, 3, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, -1, -1, -1, -1
      }, {
          3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, -1, -1, -1, -1
      }, {
          6, 5, 9, 6, 9, 11, 11, 9, 8, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 10, 6, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 3, 0, 4, 7, 3, 6, 5, 10, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 9, 0, 5, 10, 6, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, -1
      }, {
          6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, -1
      }, {
          8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, -1
      }, {
          7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, -1
      }, {
          3, 11, 2, 7, 8, 4, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, -1, -1, -1, -1
      }, {
          0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1
      }, {
          9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, -1
      }, {
          8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, -1, -1, -1, -1
      }, {
          5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, -1
      }, {
          0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, -1
      }, {
          6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, -1, -1, -1, -1
      }, {
          10, 4, 9, 6, 4, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 10, 6, 4, 9, 10, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 0, 1, 10, 6, 0, 6, 4, 0, -1, -1, -1, -1, -1, -1, -1
      }, {
          8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, -1, -1, -1, -1
      }, {
          1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, -1
      }, {
          0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          8, 3, 2, 8, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 4, 9, 10, 6, 4, 11, 2, 3, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, -1, -1, -1, -1
      }, {
          3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, -1, -1, -1, -1
      }, {
          6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, -1
      }, {
          9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, -1, -1, -1, -1
      }, {
          8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, -1
      }, {
          3, 11, 6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, -1
      }, {
          6, 4, 8, 11, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          7, 10, 6, 7, 8, 10, 8, 9, 10, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, -1, -1, -1, -1
      }, {
          10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, -1, -1, -1, -1
      }, {
          10, 6, 7, 10, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, -1, -1, -1, -1
      }, {
          2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, -1
      }, {
          7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1, -1, -1
      }, {
          7, 3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, -1, -1, -1, -1
      }, {
          2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, -1
      }, {
          1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, -1
      }, {
          11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, -1, -1, -1, -1
      }, {
          8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, -1
      }, {
          0, 9, 1, 11, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, -1, -1, -1, -1
      }, {
          7, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 0, 8, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 1, 9, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          8, 1, 9, 8, 3, 1, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 1, 2, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 10, 3, 0, 8, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 9, 0, 2, 10, 9, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1
      }, {
          6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, -1, -1, -1, -1
      }, {
          7, 2, 3, 6, 2, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 7, 6, 2, 3, 7, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, -1
      }, {
          10, 7, 6, 10, 1, 7, 1, 3, 7, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, -1, -1, -1, -1
      }, {
          0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, -1, -1, -1, -1
      }, {
          7, 6, 10, 7, 10, 8, 8, 10, 9, -1, -1, -1, -1, -1, -1, -1
      }, {
          6, 8, 4, 11, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 6, 11, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, -1
      }, {
          8, 6, 11, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, -1, -1, -1, -1
      }, {
          6, 8, 4, 6, 11, 8, 2, 10, 1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, -1, -1, -1, -1
      }, {
          4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, -1, -1, -1, -1
      }, {
          10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, -1
      }, {
          8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, -1
      }, {
          1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, -1
      }, {
          8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, -1, -1, -1, -1
      }, {
          10, 1, 0, 10, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, -1
      }, {
          10, 9, 4, 6, 10, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 9, 5, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 3, 4, 9, 5, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 0, 1, 5, 4, 0, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1
      }, {
          11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, -1
      }, {
          9, 5, 4, 10, 1, 2, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1
      }, {
          6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, -1, -1, -1, -1
      }, {
          7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, -1, -1, -1, -1
      }, {
          3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, -1
      }, {
          7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, -1, -1, -1, -1
      }, {
          3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, -1
      }, {
          6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, -1
      }, {
          9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, -1
      }, {
          1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, -1
      }, {
          4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, -1
      }, {
          7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, -1, -1, -1, -1
      }, {
          6, 9, 5, 6, 11, 9, 11, 8, 9, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, -1
      }, {
          0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, -1, -1, -1, -1
      }, {
          6, 11, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, -1, -1, -1, -1
      }, {
          0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, -1
      }, {
          11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, -1
      }, {
          6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, -1, -1, -1, -1
      }, {
          5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, -1
      }, {
          9, 5, 6, 9, 6, 0, 0, 6, 2, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, -1
      }, {
          1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, -1
      }, {
          10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, -1
      }, {
          0, 3, 8, 5, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          11, 5, 10, 7, 5, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          11, 5, 10, 11, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 11, 7, 5, 10, 11, 1, 9, 0, -1, -1, -1, -1, -1, -1, -1
      }, {
          10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, -1
      }, {
          11, 1, 2, 11, 7, 1, 7, 5, 1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, -1, -1, -1, -1
      }, {
          9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, -1, -1, -1, -1
      }, {
          7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, -1
      }, {
          2, 5, 10, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1
      }, {
          8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, -1, -1, -1, -1
      }, {
          9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, -1, -1, -1, -1
      }, {
          9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, -1
      }, {
          1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 8, 4, 5, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1
      }, {
          5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, -1, -1, -1, -1
      }, {
          0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, -1, -1, -1, -1
      }, {
          10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, -1
      }, {
          2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, -1, -1, -1, -1
      }, {
          0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, -1
      }, {
          0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, -1
      }, {
          9, 4, 5, 2, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, -1, -1, -1, -1
      }, {
          5, 10, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, -1
      }, {
          5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, -1
      }, {
          8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 4, 5, 1, 0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, -1
      }, {
          9, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 11, 7, 4, 9, 11, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, -1, -1, -1, -1
      }, {
          1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, -1, -1, -1, -1
      }, {
          3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, -1
      }, {
          4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, -1, -1, -1, -1
      }, {
          9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, -1
      }, {
          11, 7, 4, 11, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, -1
      }, {
          11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, -1
      }, {
          2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, -1, -1, -1, -1
      }, {
          9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, -1
      }, {
          3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, -1
      }, {
          1, 10, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, -1
      }, {
          4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          4, 8, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 0, 9, 3, 9, 11, 11, 9, 10, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 1, 10, 0, 10, 8, 8, 10, 11, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 1, 10, 11, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 2, 11, 1, 11, 9, 9, 11, 8, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, -1, -1, -1, -1
      }, {
          0, 2, 11, 8, 0, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          3, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 3, 8, 2, 8, 10, 10, 8, 9, -1, -1, -1, -1, -1, -1, -1
      }, {
          9, 10, 2, 0, 9, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, -1, -1, -1, -1
      }, {
          1, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 9, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }, {
          -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
      }
  };

  private static float fGetOffset(final float fValue1, final float fValue2, final float isoLevel) {
    final float fDelta = fValue2 - fValue1;
    if (Math.abs(fDelta) < 0.000001) {
      return 0.5f;
    }
    return (isoLevel - fValue1) / fDelta;
  }

  private static Vector3f vGetNormal(final Vector3f v1, final Vector3f v2, final Vector3f v3) {
    return v1.subtract(v2).cross(v1.subtract(v3)).normalize();
  }

  public SmoothSurfaceVolumeNode(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume,
      final AssetManager assetManager, final BloxelAssetManager bloxelAssetManager) {
    super(grid, volume, assetManager, bloxelAssetManager);
  }

  @Override
  List<Geometry> createGeometries(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume) {
    LOG.debug(String.format("Tesselate volume %s", volume));
    mesh.clear();
    int c = 0;
    final Set<Integer> usedBloxeTypes = Sets.newHashSet();
    for (int x = 1; x < volume.getSizeX() - 1; x++) {
      for (int z = 1; z < volume.getSizeZ() - 1; z++) {
        for (int y = 1; y < volume.getSizeY() - 1; y++) {
          final Bloxel data = volume.get(x, y, z);
          Preconditions.checkNotNull(data);
          if (data == Bloxel.AIR) {
            continue;
          }
          if (mesh.get(data.getType()) == null) {
            mesh.put(data.getType(), new SurfaceMesh());
          }
          if (mode == Mode.MARCHING_CUBES) {
            vMarchCube1(grid, volume, data, x, y, z, 0);
          } else if (mode == Mode.MARCHING_TETRAHEDRON) {
            vMarchCube2(grid, volume, data, x, y, z, 0);
          }
          c++;
          usedBloxeTypes.add(data.getType());
        }
      }
    }
    LOG.debug("Found " + c + " boxes with " + usedBloxeTypes.size() + " different types");
    final List<Geometry> result = newArrayList();
    for (final Integer bloxelType : usedBloxeTypes) {
      LOG.debug("Build mesh for material " + bloxelType + " ...");
      final Material material = bloxelAssetManager.getMaterial(bloxelType, null);
      final Geometry geometry = geometry("bloxel-" + 1).mesh(mesh.get(bloxelType).createMesh()).material(material)
          .get();
      if (material.isTransparent()) {
        geometry.setQueueBucket(Transparent);
      }
      result.add(geometry);
    }
    mesh.clear();
    return result;
  }

  public void setMode(final Mode mode) {
    this.mode = mode;
  }

  private void vMarchCube1(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume, final Bloxel data, final int fX,
      final int fY, final int fZ, final float isoLevel) {
    int iCorner, iVertex, iVertexTest, iEdge, iTriangle, iFlagIndex, iEdgeFlags;
    float fOffset;
    final float[] afCubeValue = new float[8];
    final Vector3f[] asEdgeVertex = new Vector3f[12];
    // Make a local copy of the values at the cube's corners
    for (iVertex = 0; iVertex < 8; iVertex++) {
      afCubeValue[iVertex] = volume.get((int) (fX + a2fVertexOffset[iVertex][0]),
          (int) (fY + a2fVertexOffset[iVertex][1]), (int) (fZ + a2fVertexOffset[iVertex][2])).getDensity();
    }
    // Find which vertices are inside of the surface and which are outside
    iFlagIndex = 0;
    for (iVertexTest = 0; iVertexTest < 8; iVertexTest++) {
      if (afCubeValue[iVertexTest] <= isoLevel) {
        iFlagIndex |= 1 << iVertexTest;
      }
    }
    // Find which edges are intersected by the surface
    iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];
    // If the cube is entirely inside or outside of the surface, then there
    // will be no intersections
    if (iEdgeFlags == 0) {
      return;
    }
    // Find the point of intersection of the surface with each edge
    // Then find the normal to the surface at those points
    for (iEdge = 0; iEdge < 12; iEdge++) {
      // if there is an intersection on this edge
      if ((iEdgeFlags & 1 << iEdge) > 0) {
        fOffset = fGetOffset(afCubeValue[a2iEdgeConnection[iEdge][0]], afCubeValue[a2iEdgeConnection[iEdge][1]],
            isoLevel);
        asEdgeVertex[iEdge] = new Vector3f();
        asEdgeVertex[iEdge].x = fX
            + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][0] + fOffset * a2fEdgeDirection[iEdge][0]);
        asEdgeVertex[iEdge].y = fY
            + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][1] + fOffset * a2fEdgeDirection[iEdge][1]);
        asEdgeVertex[iEdge].z = fZ
            + (a2fVertexOffset[a2iEdgeConnection[iEdge][0]][2] + fOffset * a2fEdgeDirection[iEdge][2]);
      }
    }

    // Draw the triangles that were found. There can be up to five per cube
    for (iTriangle = 0; iTriangle < 5; iTriangle++) {
      if (a2iTriangleConnectionTable[iFlagIndex][3 * iTriangle] < 0) {
        break;
      }
      final int[] index = new int[3];
      final Vertex[] vert = new Vertex[3];
      for (iCorner = 0; iCorner < 3; iCorner++) {
        iVertex = a2iTriangleConnectionTable[iFlagIndex][3 * iTriangle + iCorner];
        vert[iCorner] = new Vertex();
        vert[iCorner].position = asEdgeVertex[iVertex];
        index[iCorner] = mesh.get(data.getType()).addVertex(vert[iCorner]);
      }
      final Vector3f normal = vGetNormal(vert[0].position, vert[1].position, vert[2].position);
      vert[0].normal = normal;
      vert[1].normal = normal;
      vert[2].normal = normal;
      final List<Vector2f> textureCoordinates = bloxelAssetManager.getTextureCoordinates(1, null);
      vert[0].texCoord = textureCoordinates.get(0);
      vert[1].texCoord = textureCoordinates.get(1);
      vert[2].texCoord = textureCoordinates.get(2);
      mesh.get(data.getType()).addTriangle(index[0], index[1], index[2]);
    }
  }

  private void vMarchCube2(final VolumeGrid<Bloxel> grid, final Volume<Bloxel> volume, final Bloxel data, final int fX,
      final int fY, final int fZ, final float isoLevel) {
    int iVertex, iTetrahedron, iVertexInACube;
    final Vector3f[] asCubePosition = new Vector3f[8];
    final float[] afCubeValue = new float[8];
    final Vector3f[] asTetrahedronPosition = new Vector3f[4];
    final float[] afTetrahedronValue = new float[4];
    // Make a local copy of the cube's corner positions
    for (iVertex = 0; iVertex < 8; iVertex++) {
      asCubePosition[iVertex] = new Vector3f();
      asCubePosition[iVertex].x = fX + a2fVertexOffset[iVertex][0];
      asCubePosition[iVertex].y = fY + a2fVertexOffset[iVertex][1];
      asCubePosition[iVertex].z = fZ + a2fVertexOffset[iVertex][2];
    }
    // Make a local copy of the cube's corner values
    for (iVertex = 0; iVertex < 8; iVertex++) {
      afCubeValue[iVertex] = volume.get((int) asCubePosition[iVertex].x, (int) asCubePosition[iVertex].y,
          (int) asCubePosition[iVertex].z).getDensity();
    }
    for (iTetrahedron = 0; iTetrahedron < 6; iTetrahedron++) {
      for (iVertex = 0; iVertex < 4; iVertex++) {
        iVertexInACube = a2iTetrahedronsInACube[iTetrahedron][iVertex];
        asTetrahedronPosition[iVertex] = new Vector3f();
        asTetrahedronPosition[iVertex].x = asCubePosition[iVertexInACube].x;
        asTetrahedronPosition[iVertex].y = asCubePosition[iVertexInACube].y;
        asTetrahedronPosition[iVertex].z = asCubePosition[iVertexInACube].z;
        afTetrahedronValue[iVertex] = afCubeValue[iVertexInACube];
      }
      vMarchTetrahedron(asTetrahedronPosition, afTetrahedronValue, isoLevel, mesh.get(data.getType()));
    }
  }

  private void vMarchTetrahedron(final Vector3f[] pasTetrahedronPosition, final float[] pafTetrahedronValue,
      final float isoLevel, final SurfaceMesh surfaceMesh) {
    int iEdge, iVert0, iVert1, iEdgeFlags, iTriangle, iCorner, iVertex, iFlagIndex = 0;
    float fOffset, fInvOffset;
    final Vector3f[] asEdgeVertex = new Vector3f[6];
    // Find which vertices are inside of the surface and which are outside
    for (iVertex = 0; iVertex < 4; iVertex++) {
      if (pafTetrahedronValue[iVertex] <= isoLevel) {
        iFlagIndex |= 1 << iVertex;
      }
    }
    // Find which edges are intersected by the surface
    iEdgeFlags = aiTetrahedronEdgeFlags[iFlagIndex];
    // If the tetrahedron is entirely inside or outside of the surface, then
    // there will be no intersections
    if (iEdgeFlags == 0) {
      return;
    }
    // Find the point of intersection of the surface with each edge
    // Then find the normal to the surface at those points
    for (iEdge = 0; iEdge < 6; iEdge++) {
      // if there is an intersection on this edge
      if ((iEdgeFlags & 1 << iEdge) > 0) {
        iVert0 = a2iTetrahedronEdgeConnection[iEdge][0];
        iVert1 = a2iTetrahedronEdgeConnection[iEdge][1];
        fOffset = fGetOffset(pafTetrahedronValue[iVert0], pafTetrahedronValue[iVert1], isoLevel);
        fInvOffset = 1.0f - fOffset;
        asEdgeVertex[iEdge] = new Vector3f();
        asEdgeVertex[iEdge].x = fInvOffset * pasTetrahedronPosition[iVert0].x + fOffset
            * pasTetrahedronPosition[iVert1].x;
        asEdgeVertex[iEdge].y = fInvOffset * pasTetrahedronPosition[iVert0].y + fOffset
            * pasTetrahedronPosition[iVert1].y;
        asEdgeVertex[iEdge].z = fInvOffset * pasTetrahedronPosition[iVert0].z + fOffset
            * pasTetrahedronPosition[iVert1].z;
      }
    }
    // Draw the triangles that were found. There can be up to 2 per tetrahedron
    for (iTriangle = 0; iTriangle < 2; iTriangle++) {
      if (a2iTetrahedronTriangles[iFlagIndex][3 * iTriangle] < 0) {
        break;
      }
      final int[] index = new int[3];
      final Vertex[] vert = new Vertex[3];
      for (iCorner = 0; iCorner < 3; iCorner++) {
        iVertex = a2iTetrahedronTriangles[iFlagIndex][3 * iTriangle + iCorner];
        vert[iCorner] = new Vertex();
        vert[iCorner].position = asEdgeVertex[iVertex];
        index[iCorner] = surfaceMesh.addVertex(vert[iCorner]);
      }
      final Vector3f normal = vGetNormal(vert[0].position, vert[1].position, vert[2].position);
      vert[0].normal = normal;
      vert[1].normal = normal;
      vert[2].normal = normal;
      final List<Vector2f> textureCoordinates = bloxelAssetManager.getTextureCoordinates(1, null);
      vert[0].texCoord = textureCoordinates.get(0);
      vert[1].texCoord = textureCoordinates.get(1);
      vert[2].texCoord = textureCoordinates.get(2);
      surfaceMesh.addTriangle(index[0], index[1], index[2]);
    }
  }
}