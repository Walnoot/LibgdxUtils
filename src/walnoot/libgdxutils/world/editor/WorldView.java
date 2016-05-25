package walnoot.libgdxutils.world.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Actor;

import walnoot.libgdxutils.world.GameWorld;
import walnoot.libgdxutils.world.WorldRenderer;

public class WorldView extends Actor {
	private GameWorld world;
	private WorldRenderer renderer;
	
	private FrameBuffer activeBuffer, renderBuffer;
	private float x, y;
	
	public WorldView(GameWorld world) {
		this.world = world;
		renderer = new WorldRenderer(world);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
//		renderer.renderGrid(1f);
		
		if(renderBuffer != null) {
			batch.draw(renderBuffer.getColorBufferTexture(), x, renderBuffer.getHeight() - y, renderBuffer.getWidth(), -renderBuffer.getHeight());
		}
	}
	
	public void render() {
		if(activeBuffer != null) {
			activeBuffer.begin();
			Gdx.gl20.glClearColor(0f, 0f, 0f, 1f);
			Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
			renderer.render();
			renderer.renderGrid(1f);
			
			activeBuffer.end();
			
			if(renderBuffer != null && renderBuffer != activeBuffer) {
				renderBuffer.dispose();
			}
			renderBuffer = activeBuffer;
		}
	}
	
	@Override
	public void setBounds(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		
		float w = width;
		float h = height;
		
		if(w == 0 || h == 0) {
			activeBuffer = null;
		} else {
			if(activeBuffer == null || activeBuffer.getWidth() != w || activeBuffer.getHeight() != h) {
				activeBuffer = new FrameBuffer(Format.RGBA8888, (int) w, (int) h, false);
				
				renderer.resize((int) w, (int) h);
			}
		}
	}
}
