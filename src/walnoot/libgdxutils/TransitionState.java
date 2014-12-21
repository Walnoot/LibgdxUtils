package walnoot.libgdxutils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class TransitionState extends State {
	private State startState, endState;
	
	private boolean dirty = true;
	private FrameBuffer startBuffer, endBuffer;
	
	private SpriteBatch batch = new SpriteBatch(2);
	
	private float timeLeft;
	private Transition transition;
	
	private TextureRegion startRegion, endRegion;
	
	public TransitionState(State startState, State endState, Transition transition) {
		this.startState = startState;
		this.endState = endState;
		this.transition = transition;
		timeLeft = transition.time;
	}
	
	@Override
	public void update() {
		timeLeft -= getDelta();
		if (timeLeft <= 0f) manager.setState(endState);
		
		if (transition.updateStartState) startState.update();
		if (transition.updateEndState) endState.update();
	}
	
	@Override
	public void render() {
		if (transition.updateStartState || dirty) renderToBuffer(startBuffer, startState);
		if (transition.updateEndState || dirty) renderToBuffer(endBuffer, endState);
		dirty = false;
		
		float alpha = timeLeft / transition.time;
		
		batch.begin();
		transition.renderTransition(batch, startRegion, endRegion, alpha);
		batch.end();
	}
	
	private void renderToBuffer(FrameBuffer buffer, State state) {
		manager.getRenderContext().setCurrentTarget(buffer);
		state.render();
		manager.getRenderContext().endCurruntTarget();
	}
	
	@Override
	public void resize(boolean creation, int width, int height) {
		dirty = true;
		
		if (!creation) {
			startState.resize(false, width, height);
			endState.resize(false, width, height);
		}
		
		if (startBuffer != null) startBuffer.dispose();
		if (endBuffer != null) endBuffer.dispose();
		
		startBuffer = new FrameBuffer(Format.RGBA8888, width, height, true);
		endBuffer = new FrameBuffer(Format.RGBA8888, width, height, true);
		
		startRegion = new TextureRegion(startBuffer.getColorBufferTexture());
		startRegion.flip(false, true);
		endRegion = new TextureRegion(endBuffer.getColorBufferTexture());
		endRegion.flip(false, true);
		
		OrthographicCamera camera = new OrthographicCamera();
		camera.setToOrtho(false, 1f, 1f);
		camera.position.set(0.5f, 0.5f, 0f);
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
	}
	
	@Override
	public void show() {
	}
	
	@Override
	public void hide() {
		if (startBuffer != null) startBuffer.dispose();
		if (endBuffer != null) endBuffer.dispose();
		
		startBuffer = null;
		endBuffer = null;
	}
	
	@Override
	public void setManager(StateApplication manager) {
		super.setManager(manager);
		
		startState.setManager(manager);
		endState.setManager(manager);
	}
	
	@Override
	public State[] getManagedStates() {
		return new State[] { startState, endState };
	}
}
