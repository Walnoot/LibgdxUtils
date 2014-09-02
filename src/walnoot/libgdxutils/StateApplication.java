package walnoot.libgdxutils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

public class StateApplication extends ApplicationAdapter {
	private Array<State> stateStack = new Array<State>();
	
	private float updateDelta;
	private float unprocessedSeconds;
	
	public StateApplication(float updateFPS) {
		updateDelta = 1f / updateFPS;
	}
	
	@Override
	public void render() {
		while (unprocessedSeconds > updateDelta) {
			unprocessedSeconds -= updateDelta;
			
			update();
		}
		
		int highestIndex = 0;
		
		for (int i = stateStack.size - 1; i >= 0; i--) {
			if (stateStack.get(i).isOpaque()) {
				highestIndex = i;
				break;
			}
		}
		
		for (int i = highestIndex; i < stateStack.size; i++) {
			stateStack.get(i).render();
		}
	}
	
	private void update() {
		if (stateStack.size > 0) {
			stateStack.get(stateStack.size - 1).update();
		}
	}
	
	public void pushState(State state) {
		state.manager = this;
		
		stateStack.add(state);
		
		state.show();
		state.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public void popState() {
		if (stateStack.size > 0) removeState(stateStack.get(stateStack.size - 1));
	}
	
	public void removeState(State state) {
		boolean removed = stateStack.removeValue(state, true);
		if (removed) state.hide();
	}
	
	public void setState(State state) {
		for (State remove : stateStack) {
			removeState(remove);
		}
		
		pushState(state);
	}
	
	@Override
	public void resize(int width, int height) {
		for (State state : stateStack) {
			state.resize(width, height);
		}
	}
	
	public float getDelta() {
		return updateDelta;
	}
}
