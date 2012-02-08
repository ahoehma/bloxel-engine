package de.bloxel.engine.concurrent;

import java.util.concurrent.LinkedBlockingQueue;

import de.bloxel.engine.jme.VolumeNode;

public class VolumeNodeUpdater<T extends VolumeNode> implements Runnable {

  private final LinkedBlockingQueue<T> output;
  private final LinkedBlockingQueue<T> input;

  public VolumeNodeUpdater() {
    this(new LinkedBlockingQueue<T>(), new LinkedBlockingQueue<T>());
  }

  public VolumeNodeUpdater(final LinkedBlockingQueue<T> input, final LinkedBlockingQueue<T> output) {
    this.input = input;
    this.output = output;
  }

  public void add(final T node) {
    input.add(node);
  }

  public T poll() {
    return output.poll();
  }

  @Override
  public void run() {
    T v;
    try {
      while ((v = input.take()) != null) {
        if (v.calculate()) {
          v.update();
          output.add(v);
        }
      }
    } catch (final InterruptedException e) {
      //
    }
  }
}