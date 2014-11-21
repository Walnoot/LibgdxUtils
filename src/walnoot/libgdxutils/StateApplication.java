package walnoot.libgdxutils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

public abstract class StateApplication extends ApplicationAdapter {
	private State state;
	
	private float updateDelta;
	private float unprocessedSeconds;
	
	public StateApplication(float updateFPS) {
		updateDelta = 1f / updateFPS;
	}
	
	@Override
	public void create() {
		setState(getFirstState());
	}
	
	protected abstract State getFirstState();
	
	@Override
	public void render() {
		unprocessedSeconds += Gdx.graphics.getDeltaTime();
		while (unprocessedSeconds > updateDelta) {
			unprocessedSeconds -= updateDelta;
			
			update();
		}
		
		Color col = state.getBackgroundColor();
		
		Gdx.gl20.glClearColor(col.r, col.g, col.b, col.a);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		state.render();
	}
	
	private void update() {
		state.update();
	}
	
	public void setState(State state) {
		if (this.state != null) this.state.hide();
		
		this.state = state;
		state.setManager(this);
		
		state.show();
		state.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	@Override
	public void resize(int width, int height) {
		state.resize(width, height);
	}
	
	public float getDelta() {
		return updateDelta;
	}
}
