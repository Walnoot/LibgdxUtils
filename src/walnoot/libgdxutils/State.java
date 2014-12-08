package walnoot.libgdxutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

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
	
	/**
	 * @param target
	 *            - The framebuffer the state should render to, or null if
	 *            rendering to the screen. Can be ignored if the State does not
	 *            render to framebuffers.
	 */
	public void render(FrameBuffer target) {
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
}
