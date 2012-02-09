package de.bloxel.engine.math;

import java.io.Serializable;

import com.jme3.math.Vector3f;

/**
 * Version of {@link Vector3f} but with integers.
 * 
 * @author Andreas Höhmann
 * @since 1.0.0
 */
public final class Vector3i implements Serializable, Cloneable {

  public static Vector3i valueOf(final String s) {
    final String[] split = s.substring(1, s.length() - 1).split(" ");
    return new Vector3i(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
  }

  public int x;
  public int y;
  public int z;

  public final static Vector3i ZERO = new Vector3i(0, 0, 0);

  public Vector3i(final float x, final float y, final float z) {
    this((int) x, (int) y, (int) z);
  }

  public Vector3i(final int x, final int y, final int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vector3i(final Vector3f vec) {
    this(vec.x, vec.y, vec.z);
  }

  public Vector3i(final Vector3i vec) {
    x = vec.x;
    y = vec.y;
    z = vec.z;
  }

  public Vector3i add(final Vector3i other) {
    return new Vector3i(x + other.x, y + other.y, z + other.z);
  }

  public Vector3i addLocal(final Vector3i other) {
    x += other.x;
    y += other.y;
    z += other.z;
    return this;
  }

  @Override
  public Vector3i clone() {
    return new Vector3i(x, y, z);
  }

  public Vector3i div(final int scalar) {
    return new Vector3i(x / scalar, y / scalar, z / scalar);
  }

  public Vector3i divLocal(final int skalar) {
    x /= skalar;
    y /= skalar;
    z /= skalar;
    return this;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof Vector3i)) {
      return false;
    }
    final Vector3i vec = (Vector3i) other;
    return vec.x == x && vec.y == y && vec.z == z;
  }

  @Override
  public int hashCode() {
    int hash = 37;
    hash += 37 * hash + x;
    hash += 37 * hash + y;
    hash += 37 * hash + z;
    return hash;
  }

  public Vector3i mult(final int scalar) {
    return new Vector3i(x * scalar, y * scalar, z * scalar);
  }

  public Vector3i multLocal(final int skalar) {
    x *= skalar;
    y *= skalar;
    z *= skalar;
    return this;
  }

  public Vector3i sub(final Vector3i other) {
    return new Vector3i(x - other.x, y - other.y, z - other.z);
  }

  public Vector3i subLocal(final Vector3i other) {
    x -= other.x;
    y -= other.y;
    z -= other.z;
    return this;
  }

  public Vector3f toFloat() {
    return new Vector3f(x, y, z);
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + "," + z + ")";
  }
}