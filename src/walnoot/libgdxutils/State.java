package walnoot.libgdxutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

public class State {
	protected StateApplication manager;
	
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
	public void resize(boolean creation, int width, int height) {
	}
	
	public float getDelta() {
		return manager.getDelta();
	}
	
	public void setManager(StateApplication manager) {
		this.manager = manager;
	}
	
	protected void clearScreen(Color color) {
		Gdx.gl20.glClearColor(color.r, color.g, color.b, color.a);
		Gdx.gl20.glClear(getClearBits());
	}
	
	public int getClearBits() {
		return GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT;
	}
	
	/**
	 * @return a list of states that this State manages and wants rendered.
	 *         Returns null if none.
	 */
	public State[] getManagedStates() {
		return null;
	}
}
