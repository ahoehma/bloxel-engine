package cave3d;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import java.util.HashMap;

/**
 * @author mazander
 */
final class CaveTriMesh extends Geometry {

	private static final long serialVersionUID = 1L;
	
	private static final HashMap<Vector3f, CaveTriMesh> cache = new HashMap<Vector3f, CaveTriMesh>();

	private final Vector3f center;
	
	private final Vector3f worldCenter;

	private final float meshSize;
	
	private long lastFrustumTime = System.currentTimeMillis();

	public CaveTriMesh(Vector3f cornerCoordinates, float meshSize) {
            super();
            this.setName(cornerCoordinates.toString());
            this.meshSize = meshSize;
            this.center = new Vector3f(cornerCoordinates);
            this.worldCenter = new Vector3f(center).multLocal(meshSize);
            this.mesh = new Mesh();
	}
	
	public Vector3f getCenter() {
		return center;
	}
	
	public Vector3f getWorldCenter() {
		return worldCenter;
	}

	
	@Override
	public String toString() {
		return "caveTriMesh: "+ center + ": " + getTriangleCount();
	}

	public boolean isInFrustum(long time, FrustumIntersect fi) {
		if(fi == FrustumIntersect.Outside) {
			System.out.println(fi);
			long outsideTime = time - lastFrustumTime;
			return outsideTime < 5000L;
		} else {
			lastFrustumTime = time;
			return true;
		}
	}
}
