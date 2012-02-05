/*******************************************************************************
 * Copyright (c) 2010, 2011 Bloxel Team.
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
 * Contributors:
 *     Klaus Hauschild
 *     Patrick Lorenz
 *     Stephan Dorer
 *     Andreas H�hmann - mymita.com
 *******************************************************************************/
package de.bloxel.engine.jme;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.data.VolumeGrid;
import de.bloxel.engine.material.BloxelAssetManager;

public class WorldNode extends Node {

  private final VolumeGrid<Bloxel> grid;
  private final BloxelAssetManager assetManager;

  public WorldNode(final VolumeGrid<Bloxel> grid, final BloxelAssetManager assetManager) {
    super("bloxel-node");
    this.grid = grid;
    this.assetManager = assetManager;
  }

  private AbstractVolumeNode createChunkNode(final Vector3f aChunkLocation, final boolean lazyFilling) {
    final ColtChunk chunk = new ColtChunk(aChunkLocation, chunkSize);
    final AbstractVolumeNode result = new CubicMeshVolumeNode(bloxelFactory, chunk, this);
    if (lazyFilling) {
      LOG.debug("lazy fill new chunk for " + aChunkLocation + "/" + result);
      chunksToFill.add(result);
    } else {
      LOG.debug("create new chunk for " + aChunkLocation + "...");
      fillChunk(result, terrainLoader, filledChunks);
      LOG.debug("created new chunk for " + aChunkLocation);
    }
    return result;
  }

  private void fillChunk(final AbstractVolumeNode chunk, final BloxelLoader<Bloxel> terrainLoader,
      final BlockingQueue<AbstractVolumeNode> resultQueue) {
    chunk.getTerrainChunk().fill(terrainLoader);
    chunk.calculate();
    resultQueue.add(chunk);
    // updateSceneOctree();
  }

  /**
   * @param theLocation
   * @return
   * 
   * @deprecated use {@link #getChunkNode(Vector3f)} to get a bloxel for a position
   */
  @Deprecated
  public Bloxel getBloxel(final Vector3f theLocation) {
    // XXX dies hier f�hrt sofort zu einem zulaufen des speichers, da der
    // hier erzeugte chunk gleich wieder gefuellt und
    // berechnet wird, bei der berechnung wird natuerlich wieder der
    // "naechste" nachbar chunk benoetig usw.
    // (FacesMeshChunkNode:needFace)
    // final AbstractChunkNode n = getOrCreateChunkNode(theLocation, false);
    final AbstractVolumeNode n = getChunkNode(theLocation);
    if (n == null) {
      return null;
    }
    return n.getTerrainChunk().getVolume().getBloxel(theLocation.x, theLocation.y, theLocation.z);
  }

  /**
   * Calculate the center position of a {@link BloxelChunk} which contains the given point <code>aWorldLocation</code>.
   * The returned {@link Vector3f} would be used to get the right {@link BloxelChunk} from the loaded chunks (or to
   * create a chunk for that position).
   * 
   * @param aWorldLocation
   *          in world coordinates
   * @return the location of a {@link BloxelChunk} which contains the given position
   */
  private Vector3f getChunkLocation(final Vector3f aWorldLocation) {
    return calcChunkLocation(aWorldLocation, chunkSize);
  }

  public AbstractVolumeNode getChunkNode(final Vector3f aLocation) {
    final Vector3f chunkLocation = getChunkLocation(aLocation);
    synchronized (getChunkLock) {
      return loadedChunks.get(chunkLocation);
    }
  }

  public float getChunkSize() {
    return chunkSize;
  }

  public int getChunkToFillCount() {
    return chunksToFill.size();
  }

  @Override
  public PickResult getClosestBloxelCollideWith(final Ray ray) {
    PickResult result = null;
    float distance = Float.MAX_VALUE;
    for (final AbstractVolumeNode cn : Lists.newArrayList(visibleChunks)) {
      final PickResult pr = cn.getClosestBloxelCollideWith(ray);
      if (pr == null) {
        continue;
      }
      final float d = pr.bloxel.getCenter().distance(ray.origin);
      if (d < distance) {
        distance = d;
        result = pr;
      }
    }
    return result;
  }

  public float getHeightAt(final Vector3f theLocation) {
    // da wir ein 3d grid haben, muessen wir an der stelle x und z den chunk
    // suchen, der den h�chsten block enth�lt
    Vector3f l = theLocation;
    float lastHeight = 0;
    float lastCheck = 0;
    while (true) {
      final AbstractVolumeNode cn = getOrCreateChunkNode(l, false);
      final float h[] = getHeightAt(cn, theLocation);
      LOG.debug(String.format("%s: lc:%f,lh:%f : h0:%f,h1:%f", cn, lastCheck, lastHeight, h[0], h[1]));
      if (h[0] == -1) {
        if (lastCheck == 1) {
          // avoid endless loop between two chunks
          return lastHeight;
        }
        lastCheck = h[0];
        lastHeight = h[1];
        l = l.subtract(0, chunkSize * 2, 0);
      } else if (h[0] == 1) {
        if (lastCheck == -1) {
          // avoid endless loop between two chunks
          return lastHeight;
        }
        lastCheck = h[0];
        lastHeight = h[1];
        l = l.add(0, chunkSize * 2, 0);
      } else if (h[0] == 0) {
        return h[1];
      }
    }
  }

  public int getLoadedChunkCount() {
    return loadedChunks.size();
  }

  /**
   * This method will find the correct {@link BloxelChunk} which contains the given position. If the loaded chunks
   * doesn't contain the position more chunks must loaded, still "visible" chunks must moved (x,y,z), "invisible" chunks
   * must persists etc.
   * 
   * @param aLocation
   *          the location relative to the center of this {@link WorldNode}
   * @param lazyLoading
   * @return the {@link BloxelChunk} which contains the given location
   */
  private AbstractVolumeNode getOrCreateChunkNode(final Vector3f aLocation, final boolean lazyLoading) {
    final Vector3f chunkLocation = getChunkLocation(aLocation);
    synchronized (getChunkLock) {
      AbstractVolumeNode chunk = loadedChunks.get(chunkLocation);
      if (chunk == null) {
        loadedChunks.put(chunkLocation, chunk = createChunkNode(chunkLocation, lazyLoading));
      }
      return chunk;
    }
  }

  public int getVisibleChunkCount() {
    return visibleChunks.size();
  }

  private Set<AbstractVolumeNode> getVisibleChunks(final Vector3f theLocation, final Vector3f direction) {
    horizontBoundingBox.setCenter(theLocation);
    final Set<AbstractVolumeNode> result = Sets.newHashSet();
    for (final AbstractVolumeNode cn : loadedChunks.values()) {
      if (horizontBoundingBox.contains(cn.getTerrainChunk().getVolume().getBoundingBox().getCenter())) {
        result.add(cn);
      }
    }
    return result;
  }

  public void initPlayerPosition(final Vector3f playerPosition) {
    final Volume<Bloxel> volume = grid.getVolume(playerPosition.x, playerPosition.y, playerPosition.z);
    new CubicMeshVolumeNode(grid, volume, assetManager);
  }

  /**
   * @return
   */
  public boolean needUpdate() {
    return !updatedChunks.isEmpty() || !filledChunks.isEmpty();
  }

  public void removeBloxel(final Vector3f theLocation) {
    final AbstractVolumeNode c = getChunkNode(theLocation);
    if (c == null) {
      return;
    }
    LOG.debug(String.format("Remove bloxel %s from chunk %s", theLocation, c));
    c.removeBloxel(theLocation);
    // immediately update the chunk
    c.calculate();
    updatedChunks.add(c);
    updateNeighborChunks(c, theLocation);
  }

  public void setBloxel(final Vector3f theLocation, final Integer theBoxelType) {
    final AbstractVolumeNode c = getChunkNode(theLocation);
    if (c == null) {
      return;
    }
    LOG.debug(String.format("Add bloxel %s to chunk %s", theLocation, c));
    c.setBloxel(theLocation, theBoxelType);
    // immediately update the chunk
    c.calculate();
    updatedChunks.add(c);
    updateNeighborChunks(c, theLocation);
  }

  public void update(final Vector3f location, final Vector3f direction) {
    grid.getVolume(location.x, location.y, location.z);

    AbstractVolumeNode n = filledChunks.poll();
    if (n != null) {
      LOG.debug("Insert new filled chunk " + n);
      n.update(theLocation, direction);
      updateScene = true;
    }
    n = updatedChunks.poll();
    if (n != null) {
      LOG.debug("Update changed chunk " + n);
      n.update(theLocation, direction);
      updateScene = true;
    }
    horizontBoundingBox.setCenter(theLocation);
    debugNode.setLocalTranslation(theLocation);
    for (final AbstractVolumeNode cn : Lists.newArrayList(loadedChunks.values())) {
      if (horizontBoundingBox.contains(cn.getTerrainChunk().getVolume().getBoundingBox().getCenter())) {
        chunks.attachChild(cn);
      } else {
        chunks.detachChild(cn);
      }
    }
  }

  /**
   * @param centerChunk
   *          the changed chunk
   * @param theChangedBloxelLocation
   *          of the bloxel inside the given {@link AbstractVolumeNode centerChunk}
   */
  private void updateNeighborChunks(final AbstractVolumeNode centerChunk, final Vector3f theChangedBloxelLocation) {
    final BoundingBox bb = centerChunk.getTerrainChunk().getVolume().getBoundingBox();
    if (!bb.intersects(theChangedBloxelLocation)) {
      throw new IllegalArgumentException("Changed bloxel is not inside the chunk can't do neighbor chunk update");
    }
    // find faces for the changed bloxel, then update the chunks at this
    // faces
    final Vector3f center = bb.getCenter();
    final float elementSize = centerChunk.getTerrainChunk().getVolume().getElementSize();
    final Set<AbstractVolumeNode> neighbors = Sets.newHashSet();
    final float x = theChangedBloxelLocation.x;
    final float y = theChangedBloxelLocation.y;
    final float z = theChangedBloxelLocation.z;
    final float edgeLeft = center.x - bb.getXExtent() + elementSize;
    final float edgeRight = center.x + bb.getXExtent() - elementSize;
    final float edgeBottom = center.y - bb.getYExtent() + elementSize;
    final float edgeTop = center.y - bb.getYExtent() - elementSize;
    final float edgeBack = center.z - bb.getZExtent() + elementSize;
    final float edgeFront = center.z - bb.getZExtent() - elementSize;
    if (x <= edgeLeft) {
      // left face
      final Vector3f cl = new Vector3f(edgeLeft - chunkSize, y, z);
      final AbstractVolumeNode cn = getChunkNode(cl);
      System.out.println("Left:" + cl + "/" + x + "/" + edgeLeft + "::" + cn);
      neighbors.add(cn);
    }
    if (x == edgeRight) {
      // right face
      final Vector3f cl = new Vector3f(edgeRight + chunkSize, y, z);
      final AbstractVolumeNode cn = getChunkNode(cl);
      System.out.println("Right:" + cl + "/" + x + "/" + edgeRight + "::" + cn);
      neighbors.add(cn);
    }
    if (y <= edgeBottom) {
      // bottom face
      final Vector3f cl = new Vector3f(x, edgeBottom - chunkSize, z);
      final AbstractVolumeNode cn = getChunkNode(cl);
      System.out.println("Bottom:" + cl + "/" + y + "/" + edgeBottom + "::" + cn);
      neighbors.add(cn);
    }
    if (y == edgeTop) {
      // top face
      final Vector3f cl = new Vector3f(x, edgeTop + chunkSize, z);
      final AbstractVolumeNode cn = getChunkNode(cl);
      System.out.println("Top:" + cl + "/" + y + "/" + edgeTop + "::" + cn);
      neighbors.add(cn);
    }
    if (z == edgeBack) {
      // back face
      final Vector3f cl = new Vector3f(x, y, edgeBack - chunkSize);
      final AbstractVolumeNode cn = getChunkNode(cl);
      System.out.println("Back:" + cl + "/" + z + "/" + edgeBack + "::" + cn);
      neighbors.add(cn);
    }
    if (z == edgeFront) {
      // front face
      final Vector3f cl = new Vector3f(x, y, edgeFront + chunkSize);
      final AbstractVolumeNode cn = getChunkNode(cl);
      System.out.println("Front:" + cl + "/" + z + "/" + edgeFront + "::" + cn);
      neighbors.add(cn);
    }
    neighbors.remove(centerChunk);
    LOG.debug(String.format("Changed bloxel force lazy update of %d neigbor chunks", neighbors.size()));
    for (final AbstractVolumeNode n : neighbors) {
      if (n == null) {
        // skip none existing neighbor chunks
        continue;
      }
      LOG.debug(String.format("Changed bloxel force lazy update neigbor chunk '%s'", n));
      n.getTerrainChunk().setDirty(true);
      chunksToUpdate.add(n);
    }
  }
}