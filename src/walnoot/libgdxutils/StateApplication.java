package walnoot.libgdxutils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;

public abstract class StateApplication extends ApplicationAdapter {
	private Array<State> stateStack = new Array<State>();
	
	private float updateDelta;
	private float unprocessedSeconds;
	
	public StateApplication(float updateFPS) {
		updateDelta = 1f / updateFPS;
	}
	
	@Override
	public void create() {
		pushState(getFirstState());
	}
	
	protected abstract State getFirstState();
	
	@Override
	public void render() {
		unprocessedSeconds += Gdx.graphics.getDeltaTime();
		while (unprocessedSeconds > updateDelta) {
			unprocessedSeconds -= updateDelta;
			
			update();
		}
		
		if (stateStack.size > 0) {
			Color col = stateStack.get(0).getBackgroundColor();
			
			Gdx.gl20.glClearColor(col.r, col.g, col.b, col.a);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		}
		
		for (int i = 0; i < stateStack.size; i++) {
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
