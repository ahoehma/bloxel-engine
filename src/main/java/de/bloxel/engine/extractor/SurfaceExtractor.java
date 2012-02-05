import de.bloxel.engine.data.Volume;
import de.bloxel.engine.math.Vector3i;

public abstract class SurfaceExtractor<V> {

  protected Volume<V> volume;

  public SurfaceExtractor(final Volume<V> volume) {
    this.volume = volume;
  }

  public abstract void execute(Vector3i min, Vector3i max, float isoLevel);

  public Volume<V> getVolume() {
    return volume;
  }

  public void setVolume(final Volume<V> volume) {
    this.volume = volume;
  }

}