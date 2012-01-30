package de.bloxel.engine.data;

import static java.lang.String.format;

import org.apache.log4j.Logger;

/**
 * Create {@link ColtVolume}S.
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public class ColtVolumeFactory<T> implements VolumeFactory<T> {

  private static final Logger LOG = Logger.getLogger(ColtVolumeFactory.class);

  @Override
  public Volume<T> create(final int x, final int y, final int z, final int sizeX, final int sizeY, final int sizeZ) {
    LOG.debug(format("Create volume for position (x:%d,y:%d,z:%d)", x, y, z));
    return new ColtVolume<T>(x, y, z, sizeX, sizeY, sizeZ);
  }
}