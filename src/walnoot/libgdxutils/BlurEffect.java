package walnoot.libgdxutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class BlurEffect {
	private static final float BLUR_FACTOR = 4f;
	
	private FrameBuffer blurTargetA, blurTargetB;
	private TextureRegion fboRegion;
	
	private ShaderProgram shader;
	
	private SpriteBatch batch = new SpriteBatch(1);
	private OrthographicCamera cam;
	
	private int dirLocation;
	
	float downScale = 4f;
	
	private RenderContext context;
	
	private Color tint = Color.WHITE;
	
	public void create() {
		shader = new ShaderProgram(Gdx.files.classpath("walnoot/libgdxutils/shaders/default.vertex"),
				Gdx.files.classpath("walnoot/libgdxutils/shaders/blur.fragment"));
		
		if (shader.getLog().length() != 0) System.out.println(shader.getLog());
		
		dirLocation = shader.getUniformLocation("dir");
	}
	
	private void resizeBatch(int width, int height) {
		cam.setToOrtho(false, width, height);
		batch.setProjectionMatrix(cam.combined);
	}
	
	public void begin(RenderContext renderContext) {
		if (isActive()) throw new IllegalStateException("Need to call end() before begin()");
		
		this.context = renderContext;
		
		//Start rendering to an offscreen color buffer
//		blurTargetA.begin();
		context.setCurrentTarget(blurTargetA);
		
		//before rendering, ensure we are using the default shader
		batch.setShader(null);
		
		//resize the batch projection matrix before drawing with it
		resizeBatch(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		//now we can start drawing...
		batch.begin();
	}
	
	public void end() {
		batch.flush();
		
//		blurTargetA.end();
		context.endCurruntTarget();
		genMipMaps(blurTargetA);
		
		//start blurring the offscreen image
		batch.setShader(shader);
		shader.setUniformf(dirLocation, (BLUR_FACTOR * downScale) / Gdx.graphics.getWidth(), 0f);//x axis
//		shader.setUniformf(dirLocation, 0f, 0f);
		
//		blurTargetB.begin();
		context.setCurrentTarget(blurTargetB);
		
		fboRegion.setTexture(blurTargetA.getColorBufferTexture());
		
		batch.draw(fboRegion, 0, 0);
		
		batch.flush();
		
//		if (context == null) blurTargetB.end();
//		else context.begin();
		context.endCurruntTarget();
		
		//update our projection matrix with the screen size
		resizeBatch(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		shader.setUniformf(dirLocation, 0f, (BLUR_FACTOR * downScale) / Gdx.graphics.getWidth());//y axis
//		shader.setUniformf(dirLocation, 0f, 0f);
		
		//draw target B to the screen with a vertical blur effect 
		fboRegion.setTexture(blurTargetB.getColorBufferTexture());
		batch.setColor(tint);
		batch.draw(fboRegion, 0, 0);
		batch.setColor(Color.WHITE);
		
		batch.setShader(null);
		batch.end();
		
		context = null;
	}
	
	private void genMipMaps(FrameBuffer buffer) {
		buffer.getColorBufferTexture().bind();
		Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_2D);
	}
	
	public void resize(int width, int height) {
		if (blurTargetA != null) blurTargetA.dispose();
		if (blurTargetB != null) blurTargetB.dispose();
		
		blurTargetA = new MipMapFrameBuffer(Format.RGBA8888, width, height, true);
		blurTargetB = new FrameBuffer(Format.RGBA8888, (int) (width / downScale), (int) (width / downScale), false);
		
		fboRegion = new TextureRegion(blurTargetA.getColorBufferTexture());
		fboRegion.flip(false, true);
		
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.setToOrtho(false);
	}
	
	public void setTint(Color tint) {
		this.tint = tint;
	}
	
	public void dispose() {
		blurTargetA.dispose();
		blurTargetB.dispose();
		shader.dispose();
	}
	
	public boolean isActive() {
		return context != null;
	}
	
	public FrameBuffer getRenderTarget() {
		return blurTargetA;
	}
	
	private class MipMapFrameBuffer extends FrameBuffer {
		public MipMapFrameBuffer(Format format, int width, int height, boolean hasDepth) {
			super(format, width, height, hasDepth);
		}
		
		@Override
		protected void setupTexture() {
			colorTexture = new Texture(width, height, format);
			colorTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
			colorTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		}
	}
}
