package walnoot.libgdxutils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;

public abstract class Transition {
	public boolean updateStartState, updateEndState;
	public final float time;
	
	public Transition(float time) {
		this(false, false, time);
	}
	
	public Transition(boolean updateStartState, boolean updateEndState, float time) {
		this.updateStartState = updateStartState;
		this.updateEndState = updateEndState;
		this.time = time;
	}
	
	public abstract void renderTransition(SpriteBatch batch, TextureRegion startRegion, TextureRegion endRegion,
			float alpha);
	
	public Transition updateStart() {
		updateStartState = true;
		return this;
	}
	
	public Transition updateEnd() {
		updateEndState = true;
		return this;
	}
	
	public static class FadeTransition extends Transition {
		public FadeTransition(float time) {
			super(time);
		}
		
		@Override
		public void renderTransition(SpriteBatch batch, TextureRegion startRegion, TextureRegion endRegion, float alpha) {
			batch.setColor(1f, 1f, 1f, 1f);
			batch.draw(startRegion, 0f, 0f, 1f, 1f);
			batch.setColor(1f, 1f, 1f, 1f - alpha);
			batch.draw(endRegion, 0f, 0f, 1f, 1f);
		}
	}
	
	public static class SlideTransition extends Transition {
		private boolean animateStartScreen;
		private Direction dir;
		private Interpolation interpolation = Interpolation.linear;
		
		public SlideTransition(float time, Direction dir) {
			super(time);
			this.dir = dir;
		}
		
		public SlideTransition(float time) {
			this(time, Direction.UP);
		}
		
		public void renderTransition(SpriteBatch batch, TextureRegion startRegion, TextureRegion endRegion, float alpha) {
			float endX = dir.x * interpolation.apply(alpha);
			float endY = dir.y * interpolation.apply(alpha);
			
			float startX = animateStartScreen ? endX - dir.x : 0f;
			float startY = animateStartScreen ? endY - dir.y : 0f;
			
			batch.draw(startRegion, startX, startY, 1f, 1f);
			batch.draw(endRegion, endX, endY, 1f, 1f);
		}
		
		public SlideTransition animateStartScreen() {
			animateStartScreen = true;
			return this;
		}
		
		public SlideTransition interpolate(Interpolation i) {
			this.interpolation = i;
			return this;
		}
		
		public enum Direction {
			UP(0f, 1f), RIGHT(1f, 0f), DOWN(0f, -1f), LEFT(-1f, 0f);
			
			private float x, y;
			
			private Direction(float x, float y) {
				this.x = x;
				this.y = y;
			}
		}
	}
}
