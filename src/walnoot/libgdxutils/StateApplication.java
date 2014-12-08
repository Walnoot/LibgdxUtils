package walnoot.libgdxutils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public abstract class StateApplication extends ApplicationAdapter {
	private static final String[] TIME_SUFFIXES = { "nanos", "micros", "millis", "seconds" };
	
	private State state;
	
	private float updateDelta;
	private float unprocessedSeconds;
	
	private int[] updateTimes = new int[60], renderTimes = new int[60];//in nanoseconds
	private int updateTimesIndex, renderTimesIndex;
	private BitmapFont font;
	private SpriteBatch batch;
	
	private int currentLine;//current line of the debug text
	
	private boolean debug;
	
	private boolean firstTimeResize = true;
	
	public StateApplication(float updateFPS) {
		this(updateFPS, false);
	}
	
	public StateApplication(float updateFPS, boolean debug) {
		this.debug = debug;
		updateDelta = 1f / updateFPS;
	}
	
	@Override
	public void create() {
		if (debug) {
			font = new BitmapFont();
			font.setFixedWidthGlyphs("1234567890");
			batch = new SpriteBatch();
		}
		
		init();
		
		setState(getFirstState());
	}
	
	protected void init() {
	}
	
	protected abstract State getFirstState();
	
	@Override
	public void render() {
		unprocessedSeconds += Gdx.graphics.getDeltaTime();
		while (unprocessedSeconds > updateDelta) {
			unprocessedSeconds -= updateDelta;
			
			update();
		}
		
		long startTime = System.nanoTime();
		
		state.render(null);
		
		renderTimes[renderTimesIndex] = (int) (System.nanoTime() - startTime);
		renderTimesIndex = (renderTimesIndex + 1) % renderTimes.length;
		
		if (debug && Gdx.input.isKeyPressed(Keys.F1)) drawDebug();
		if (debug && Gdx.input.isKeyPressed(getExitKey())) Gdx.app.exit();
	}
	
	protected int getExitKey() {
		return Keys.ESCAPE;
	}
	
	private void drawDebug() {
		batch.begin();
		
		currentLine = 0;
		
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0f, getNextLineHeight());
		font.draw(batch, "Render time (avg): " + getAverage(renderTimes), 0f, getNextLineHeight());
		font.draw(batch, "Render time (peak): " + getPeak(renderTimes), 0f, getNextLineHeight());
		font.draw(batch, "Update time (avg): " + getAverage(updateTimes), 0f, getNextLineHeight());
		font.draw(batch, "Update time (peak): " + getPeak(updateTimes), 0f, getNextLineHeight());
		font.draw(batch, FrameBuffer.getManagedStatus(), 0f, getNextLineHeight());
		font.draw(batch, Texture.getManagedStatus(), 0f, getNextLineHeight());
		batch.end();
	}
	
	private int getNextLineHeight() {
		return (int) (Gdx.graphics.getHeight() - (currentLine++ * font.getLineHeight()));
	}
	
	private String formatTime(int time) {
		return String.format("%.2f ms", time / 1000000f);
	}
	
	private String getAverage(int[] times) {
		int sum = 0;
		
		for (int i = 0; i < times.length; i++) {
			sum += times[i];
		}
		
		return formatTime(sum / times.length);
	}
	
	private String getPeak(int[] times) {
		int peak = 0;
		
		for (int i = 0; i < times.length; i++) {
			if (times[i] > peak) peak = times[i];
		}
		
		return formatTime(peak);
	}
	
	protected void update() {
		long startTime = System.nanoTime();
		state.update();
		updateTimes[updateTimesIndex] = (int) (System.nanoTime() - startTime);
		updateTimesIndex = (updateTimesIndex + 1) % updateTimes.length;
	}
	
	public void setState(State state) {
		setState(state, true);
	}
	
	void setState(State state, boolean show) {
		if (this.state != null) this.state.hide();
		
		this.state = state;
		state.setManager(this);
		
		if (show) {
			state.show();
			state.resize(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}
	
	public void transitionTo(State newState, Transition transition) {
		state = new TransitionState(state, newState, transition);
		state.setManager(this);
		
		state.show();
		state.resize(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	@Override
	public void resize(int width, int height) {
		if (debug) {
			OrthographicCamera camera = new OrthographicCamera();
			camera.setToOrtho(false);
			batch.setProjectionMatrix(camera.combined);
		}
		
		if (!firstTimeResize) state.resize(false, width, height);
		firstTimeResize = false;
	}
	
	public float getDelta() {
		return updateDelta;
	}
	
	public boolean isTransitioning() {
		return state instanceof TransitionState;
	}
}
