package walnoot.libgdxutils.world.components;

import walnoot.libgdxutils.world.Component;

public class CameraComponent extends Component {
	public float zoom = 8f, tween = 0.2f;
	
	//position of camera, tweened towards pos of body of this component
	private float x, y;
	
	@Override
	public void update() {
		x += (e.getX() - x) * tween;
		y += (e.getY() - y) * tween;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
}
