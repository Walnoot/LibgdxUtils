package walnoot.libgdxutils;

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
	
	/**
	 * @return true if this state covers all states lower in the state stack.
	 */
	public boolean isOpaque() {
		return false;
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
}
