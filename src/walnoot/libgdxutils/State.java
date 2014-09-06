package walnoot.libgdxutils;

import com.badlogic.gdx.graphics.Color;

public class State {
	public StateApplication manager;
	
	public State() {
	}
	
	public void show() {
	}
	
	public void hide() {
	}
	
	public void update() {
	}
	
	public void render() {
	}
	
	/**
	 * Called when the window resizes or this state is created
	 */
	public void resize(int width, int height) {
	}
	
	public float getDelta() {
		return manager.getDelta();
	}
	
	public Color getBackgroundColor() {
		return Color.WHITE;
	}
}
