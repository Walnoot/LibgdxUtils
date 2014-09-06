package walnoot.libgdxutils.ashley;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class PositionComponent extends Component {
	public final Vector3 pos = new Vector3();
	
	public PositionComponent() {
	}
	
	public PositionComponent(float x, float y, float z) {
		pos.set(x, y, z);
	}
	
	public void add(Vector2 vec) {
		pos.add(vec.x, vec.y, 0f);
	}
}
