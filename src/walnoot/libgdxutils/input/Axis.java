package walnoot.libgdxutils.input;

import com.badlogic.gdx.math.Vector2;

public class Axis {
	private final Key positive, negative;
	
	public Axis(Key positive, Key negative) {
		this.positive = positive;
		this.negative = negative;
	}
	
	public Axis(String x, String y, InputHandler input) {
		this(input.getKey(x), input.getKey(y));
	}
	
	public float getValue() {
		float result = 0f;
		if (positive.isTouched()) result += 1f;
		if (negative.isTouched()) result -= 1f;
		
		return result;
	}
	
	/**
	 * Calculates the normalized vector of the two combined axis, useful for
	 * input code.
	 * 
	 * @return - The normalized vector, result is a parameter to avoid garbage
	 *         collection.
	 */
	public static Vector2 getNorCoords(Vector2 result, Axis x, Axis y) {
		return result.set(x.getValue(), y.getValue()).nor();
	}
}
