package walnoot.libgdxutils;

import com.badlogic.gdx.graphics.Color;

public class State {
	protected StateApplication manager;
	
	/**
	 * The stack that this state is part of, is null when not part of a
	 * StackState.
	 */
	public StackState stack;
	
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
	
	public void setManager(StateApplication manager) {
		this.manager = manager;
	}
	
	public Color getBackgroundColor() {
		return Color.WHITE;
	}
}
