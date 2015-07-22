package walnoot.libgdxutils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.Array;

public abstract class StateApplication extends ApplicationAdapter {
	private State state;
	
	private float updateDelta;
	private float unprocessedSeconds;
	
	private int[] updateTimes = new int[60], renderTimes = new int[60];//in nanoseconds
	private int updateTimesIndex, renderTimesIndex;
	private BitmapFont font;
	private SpriteBatch batch;
	
	private RenderContext renderContext = new RenderContext();
	
	private int currentLine;//current line of the debug text
	
	private boolean debug;
	private boolean debugActive;
	
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
			
			GLProfiler.enable();
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
		
		state.render();
		
		renderTimes[renderTimesIndex] = (int) (System.nanoTime() - startTime);
		renderTimesIndex = (renderTimesIndex + 1) % renderTimes.length;
		
		if(Gdx.input.isKeyJustPressed(Keys.F1)) debugActive = !debugActive;
		if (debug && debugActive) drawDebug();
		if (debug && Gdx.input.isKeyPressed(getExitKey())) Gdx.app.exit();
		
		GLProfiler.reset();
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
		font.draw(batch, "Java heap: " + (Gdx.app.getJavaHeap() / 1024) + " kB", 0f, getNextLineHeight());
		font.draw(batch, "Native heap: " + (Gdx.app.getNativeHeap() / 1024) + " kB", 0f, getNextLineHeight());
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
	
	public void setState(State newState) {
		State oldState = this.state;
		this.state = newState;
		
		Array<State> oldStates = new Array<State>(false, 4), newStates = new Array<State>(false, 4);
		fillStateList(oldStates, oldState);
		fillStateList(newStates, newState);
		
		//hide any state that's no longer rendered
		for (State s : oldStates) {
			if (!newStates.contains(s, true)) {
				s.hide();
			}
		}
		
		//init any state that wasn't rendered before
		for (State s : newStates) {
			if (!oldStates.contains(s, true)) {
				s.setManager(this);//the state might not have a reference to this instance yet
				s.show();
				s.resize(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			}
		}
	}
	
	/**
	 * Fills the specified list with the state and its managed states,
	 * recursively.
	 */
	private void fillStateList(Array<State> states, State state) {
		if (state != null) {
			states.add(state);
			
			State[] managedStates = state.getManagedStates();
			if (managedStates != null) {
				for (int i = 0; i < managedStates.length; i++) {
					fillStateList(states, managedStates[i]);
				}
			}
		}
	}
	
	public void transitionTo(State newState, float fadeTime) {
		transitionTo(newState, new Transition.FadeTransition(fadeTime));
	}
	
	public void transitionTo(State newState, Transition transition) {
		setState(new TransitionState(state, newState, transition));
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
	
	public State getCurrentState() {
		return state;
	}
	
	public RenderContext getRenderContext() {
		return renderContext;
	}
	
	public float getDelta() {
		return updateDelta;
	}
	
	public boolean isTransitioning() {
		return state instanceof TransitionState;
	}
}
