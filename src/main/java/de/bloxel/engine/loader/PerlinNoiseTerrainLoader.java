package de.bloxel.engine.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bloxel.engine.data.Bloxel;
import de.bloxel.engine.data.Volume;
import de.bloxel.engine.math.SimplexNoise3;

public class PerlinNoiseTerrainLoader implements BloxelLoader {

  private static final Logger LOG = LoggerFactory.getLogger(PerlinNoiseTerrainLoader.class);

  @Override
  public void fill(final Volume<Bloxel> volume) {
    final float startTime = System.currentTimeMillis();
    final int xv = volume.getX();
    final int yv = volume.getY();
    final int zv = volume.getZ();
    for (int z = 0; z <= volume.getSizeZ() - 1; z++) {
      for (int y = 0; y <= volume.getSizeY() - 1; y++) {
        for (int x = 0; x <= volume.getSizeX() - 1; x++) {
          final float xf = (xv + x + 512) / 1024f;
          final float yf = (yv + y + 512) / 1024f;
          final float zf = (zv + z + 512) / 1024f;
          float plateau_falloff;
          if (yf <= 0.8) {
            plateau_falloff = 1.0f;
          } else if (0.8 < yf && yf < 0.9) {
            plateau_falloff = 1.0f - (yf - 0.8f) * 10.0f;
          } else {
            plateau_falloff = 0.0f;
          }
          final float center_falloff = (float) (0.1 / (Math.pow((xf - 0.5) * 1.5, 2) + Math.pow((yf - 1.0) * 0.8, 2) + Math
              .pow((zf - 0.5) * 1.5, 2)));
          final float caves = (float) Math.pow(SimplexNoise3.simplex_noise(1, xf * 5, yf * 5, zf * 5), 3);
          float density = SimplexNoise3.simplex_noise(5, xf, yf * 0.5f, zf) * center_falloff * plateau_falloff;
          density *= Math.pow(SimplexNoise3.noise((xf + 1) * 3.0f, (yf + 1) * 3.0f, (zf + 1) * 3.0f) + 0.4f, 1.8f);
          if (caves < 0.5) {
            density = 0;
          }
          // System.out.println(String.format("%d,%d,%d=>%d,%d,%d=%f,%f,%f=%f", xv, yv, zv, x, y, z, xf, yf, zf,
          // density));
          if (density >= 3.1f) {
            volume.set(x, y, z, new Bloxel(1, density));
          }
        }
      }
    }
    final float duration = System.currentTimeMillis() - startTime;
    LOG.debug("fill chunk with noise time was " + duration + "ms");
  }
}