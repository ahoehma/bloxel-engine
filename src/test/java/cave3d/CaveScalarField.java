package cave3d;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import java.util.Random;

import com.jme3.math.Vector3f;

/**
 * @author mazander
 */
final class CaveScalarField implements ScalarField {

	private final Vector3f tmp = new Vector3f();
	
	private final float d;
        private float u;
        private float v;
	
	private final Noise3D[] noises;
	
	private final Noise3D stalactites;

	private int voxelCount;
	
	CaveScalarField(long seed, float size, float voxelsize) {
		long time = System.currentTimeMillis();
        Random random = new Random(seed);
		this.d = voxelsize;
		this.voxelCount = (int) (size / voxelsize);
		this.noises = new Noise3D[] {
				new Noise3D(random, voxelCount, size / 1f, false),
				new Noise3D(random, voxelCount, size / 2f, false),
				new Noise3D(random, voxelCount, size / 4f, false),
//				new Noise3D(random, voxelCount, size / 8f, false),
//				new Noise3D(random, voxelCount, size / 16f),
//				new Noise3D(random, voxelCount, size / 32f),
//				new Noise3D(random, voxelCount, size / 64f),
//				new Noise3D(random, voxelCount, size / 128f),
			};
		stalactites = new Noise3D(random, voxelCount, size / 8f, 0, size / 8f, true);
	}


	public float calculate(Vector3f point) {
		float density = 0;
		float x = point.x;
		float y = point.y;
		float z = point.z;
		for (int i = noises.length - 1; i >= 0; i--) {
			density += noises[i].getNoise(x, y, z);
		}
		density += stalactites.getNoise(2f*x, 0.2f * y, 2f*z);
		return density;
	}
	
	/**
	 * Computing the Normal via a Gradient
	 */
	public void normal(Vector3f point, Vector3f result) {	
		// x
		tmp.set(point.x - d, point.y, point.z);
		result.x = calculate(tmp);
		tmp.set(point.x + d, point.y, point.z);
		result.x -= calculate(tmp);
		// y
		tmp.set(point.x, point.y - d, point.z);
		result.y = calculate(tmp);
		tmp.set(point.x, point.y + d, point.z);
		result.y -= calculate(tmp);
		// z
		tmp.set(point.x, point.y, point.z - d);
		result.z = calculate(tmp);
		tmp.set(point.x, point.y, point.z + d);
		result.z -= calculate(tmp);
		
		result.normalizeLocal();
	}
	
	public void textureCoords(Vector3f point, Vector2f result) {
            u = (float) Math.atan2( (float) -point.x, point.z )*3;
            v = (float) Math.atan( point.y / Math.sqrt(point.x * point.x + point.z * point.z) )*3 ;
            result.set( 
                    (float) (u - Math.floor(u)), v );
            //System.out.println(result.toString());
        }
	
	public void color(Vector3f point, ColorRGBA result) {}

}
