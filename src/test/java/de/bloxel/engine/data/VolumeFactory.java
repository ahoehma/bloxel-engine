package de.bloxel.engine.data;

public interface VolumeFactory<T> {

  /**
   * @param x
   * @param y
   * @param z
   * @param sizeX
   * @param sizeY
   * @param sizeZ
   * @return a new {@link Volume}
   */
  Volume<T> create(int x, int y, int z, int sizeX, int sizeY, int sizeZ);
}