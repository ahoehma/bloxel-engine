package cave3d;
import com.jme3.math.FastMath;
import java.util.Random;

/**
 * @author mazander
 */
final class Noise3D {

	
	private final float[][][] noise;
	private final float waveLength;
	private final float amplitude;
	private final int size;
	private final boolean gaussian;
	
	Noise3D(Random random, final int size, final float waveLength, final float min, final float max, final boolean gaussian) {
		this.size = size;
		this.waveLength = waveLength;
		this.gaussian = gaussian;
		this.amplitude = max - min;
		this.noise = new float[size][size][size];
		if(gaussian) {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					for (int k = 0; k < size; k++) {
						noise[i][j][k] = min + amplitude * ((float) (0.5 + 0.5 * random.nextGaussian()));
					}
				}
			}
		} else {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					for (int k = 0; k < size; k++) {
						noise[i][j][k] = min + amplitude * ((float) random.nextFloat());
					}
				}
			}
		}
	}
	
	Noise3D(Random random, final int size, final float waveLength, final float amplitude, final boolean gaussian) {
		this(random, size, waveLength, -0.5f * amplitude, 0.5f * amplitude, gaussian);
	}

	Noise3D(Random random, final int size, final float strength, final boolean gaussian) {
		this(random, size, strength, strength, gaussian);
	}
	
	
	public float getNoise(float x, float y, float z) {
		float ax0 = x / waveLength;
		float ay0 = y / waveLength;
		float az0 = z / waveLength;

        int x0 = (int) FastMath.floor(ax0);
        int y0 = (int) FastMath.floor(ay0);
        int z0 = (int) FastMath.floor(az0);

    	ax0 -= x0;
    	ay0 -= y0;
    	az0 -= z0;
        
    	x0 %= size; 
    	y0 %= size; 
    	z0 %= size;
    	
    	if(x0 < 0) x0 += size;
    	if(y0 < 0) y0 += size;
    	if(z0 < 0) z0 += size;
    	
        int x1 = (x0 + 1) % size;
        int y1 = (y0 + 1) % size;
        int z1 = (z0 + 1) % size;
        float ax1 = 1f - ax0;
        float ay1 = 1f - ay0;
        float az1 = 1f - az0;
       
        return noise[x0][y0][z0] * ax1 * ay1 * az1 +
        	   noise[x0][y0][z1] * ax1 * ay1 * az0 +
               noise[x0][y1][z0] * ax1 * ay0 * az1 +
               noise[x0][y1][z1] * ax1 * ay0 * az0 +
               noise[x1][y0][z0] * ax0 * ay1 * az1 +
               noise[x1][y0][z1] * ax0 * ay1 * az0 +
               noise[x1][y1][z0] * ax0 * ay0 * az1 +
               noise[x1][y1][z1] * ax0 * ay0 * az0 ;
	}
}