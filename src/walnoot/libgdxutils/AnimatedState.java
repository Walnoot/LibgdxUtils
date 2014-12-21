package walnoot.libgdxutils;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;

public abstract class AnimatedState extends State {
	private FrameBuffer frameBuffer;
	private SpriteBatch batch = new SpriteBatch(1);
	
	private Transition transition = getTransition();
	
	private float progress = 0f;
	
	private boolean updateInTransition;
	
	public AnimatedState() {
		this(false);
	}
	
	public AnimatedState(boolean updateInTransition) {
		this.updateInTransition = updateInTransition;
	}
	
	@Override
	public void show() {
		Matrix4 projection = new Matrix4();
		projection.setToOrtho2D(0f, 1f, 1f, -1f);
		batch.setProjectionMatrix(projection);
	}
	
	@Override
	public final void update() {
		if (updateInTransition) updateLogic();
		
		if (!shouldFadeOut()) {
			progress += getDelta() / getDuration();
			if (progress >= 1f) {
				progress = 1f;
				
				if (!updateInTransition) updateLogic();
			}
		} else {
			progress -= getDelta() / getDuration();
			if (progress < 0f) {
				progress = 0f;
				
				onFadeOut();
			}
		}
		
		transition.progress = progress;
	}
	
	@Override
	public void render() {
		manager.getRenderContext().setCurrentTarget(frameBuffer);
		renderScene();
		manager.getRenderContext().endCurruntTarget();
		
		batch.begin();
		batch.setColor(1f, 1f, 1f, transition.getAlpha());
		batch.draw(frameBuffer.getColorBufferTexture(), transition.getX(), transition.getY(), 1f, 1f);
		batch.end();
	}
	
	protected abstract void renderScene();
	
	protected void updateLogic() {
	}
	
	protected float getDuration() {
		return 1f;
	}
	
	protected boolean shouldFadeOut() {
		return false;
	}
	
	/**
	 * Method that's called when fade out is completed, signals that this state
	 * can go to the next.
	 */
	protected void onFadeOut() {
	}
	
	protected Transition getTransition() {
		return new Transition(0f, -1f, 0f, Interpolation.sine);
	}
	
	@Override
	public void resize(boolean creation, int width, int height) {
		if (frameBuffer != null) frameBuffer.dispose();
		frameBuffer = new FrameBuffer(Format.RGBA8888, width, height, true);
	}
	
	@Override
	public void hide() {
		if (frameBuffer != null) frameBuffer.dispose();
		frameBuffer = null;
	}
	
	public static class Transition {
		private float startX = 0f;
		private float startY = 0f;
		private float startAlpha = 0f;
		
		private Interpolation interpolation = Interpolation.linear;
		
		private float progress;
		
		public Transition() {
		}
		
		public Transition(float startX, float startY, float startAlpha, Interpolation interpolation) {
			this.startX = startX;
			this.startY = startY;
			this.startAlpha = startAlpha;
			this.interpolation = interpolation;
		}
		
		public float getX() {
			return interpolation.apply(startX, 0f, progress);
		}
		
		public float getY() {
			return interpolation.apply(startY, 0f, progress);
		}
		
		public float getAlpha() {
			return interpolation.apply(startAlpha, 1f, progress);
		}
	}
}
