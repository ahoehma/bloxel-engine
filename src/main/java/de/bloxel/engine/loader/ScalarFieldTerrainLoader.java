package de.bloxel.engine.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cave3d.ScalarField;

import com.jme3.math.Vector3f;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;

public class ScalarFieldTerrainLoader implements BloxelLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ScalarFieldTerrainLoader.class);
  private final ScalarField scalarField;

  public ScalarFieldTerrainLoader(final ScalarField scalarField) {
    this.scalarField = scalarField;
  }

  @Override
  public void fill(final Volume<Bloxel> volume) {
    final float startTime = System.currentTimeMillis();
    final int xv = volume.getX();
    final int yv = volume.getY();
    final int zv = volume.getZ();
    for (int z = 0; z < volume.getSizeZ(); z++) {
      for (int y = 0; y < volume.getSizeY(); y++) {
        for (int x = 0; x < volume.getSizeX(); x++) {
          final float xf = xv + x;
          final float yf = yv + y;
          final float zf = zv + z;
          final float density = scalarField.calculate(new Vector3f(xf, yf, zf));
          if (density > 0) {
            volume.set(x, y, z, new Bloxel(z % 7 + 1, density));
          }
        }
      }
    }
    final float duration = System.currentTimeMillis() - startTime;
    LOG.debug("fill chunk with noise time was " + duration + "ms");
  }
}