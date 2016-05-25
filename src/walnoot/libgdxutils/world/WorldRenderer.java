package walnoot.libgdxutils.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;

import walnoot.libgdxutils.world.components.CameraComponent;
import walnoot.libgdxutils.world.components.SpritesComponent;
import walnoot.libgdxutils.world.components.SpritesComponent.ComponentSprite;

public class WorldRenderer {
	private final GameWorld world;
	
	private Box2DDebugRenderer debug = new Box2DDebugRenderer();
	private ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	private SpriteBatch batch = new SpriteBatch();
	private OrthographicCamera camera = new OrthographicCamera();

	private Array<ComponentSprite> componentSprites = new Array<>();
	
	private Vector2 tmp = new Vector2();
	
	public WorldRenderer(GameWorld world) {
		this.world = world;
	}
	
	public void render() {
		Color color = Color.BLACK;
		Gdx.gl20.glClearColor(color.r, color.g, color.b, color.a);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		world.forAllEntities(e -> {
			e.ifPresent(CameraComponent.class, c -> {
				camera.position.x = c.getX();
				camera.position.y = c.getY();
			});
		});
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		batch.begin();
		
		componentSprites.clear();
		
		world.forAllEntities(e -> {
			e.ifPresent(SpritesComponent.class, c -> {
				for (ComponentSprite cs : c.sprites) {
					if(cs.sprite == null) {
						cs.sprite = world.getAssetHandler().getSprite(cs.name);
					}
					
					if(cs.sprite != null) {
						Sprite s = cs.sprite;
						
						s.setSize(cs.width, cs.height);
						s.setOrigin(s.getWidth() / 2f, s.getHeight() / 2f);
						
						tmp.set(cs.xOffset, cs.yOffset).rotateRad(e.getBody().getAngle());
						s.setCenter(e.getX() + tmp.x, e.getY() + tmp.y);
						s.setRotation(e.getBody().getAngle() * MathUtils.radiansToDegrees);
						s.setColor(cs.color);
						
						componentSprites.add(cs);
					}
				}
			});
		});
		
		componentSprites.sort((s1, s2) -> Float.compare(s1.z, s2.z));
		
		for (ComponentSprite s : componentSprites) {
			s.sprite.draw(batch);
		}
		
		batch.end();
	}
	
	public void renderDebug() {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		debug.render(world.getBox2d(), camera.combined);
		
		batch.end();
	}
	
	public void renderGrid(float size) {
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.DARK_GRAY);

		float x1 = camera.position.x - (camera.viewportWidth * camera.zoom * 0.5f);
		float x2 = camera.position.x + (camera.viewportWidth * camera.zoom * 0.5f);
		float y1 = camera.position.y - (camera.viewportHeight * camera.zoom * 0.5f);
		float y2 = camera.position.y + (camera.viewportHeight * camera.zoom * 0.5f);
		
		for(float i = MathUtils.floor(x1 / size) * size; i <= MathUtils.ceil(x2 / size) * size; i++) {
			shapeRenderer.line(i, y1, i, y2);
		}
		
		for(float i = MathUtils.floor(y1 / size) * size; i <= MathUtils.ceil(y2 / size) * size; i++) {
			shapeRenderer.line(x1, i, x2, i);
		}
		
		shapeRenderer.end();
		
		batch.end();
	}
	
	public void resize(int width, int height) {
		camera.viewportHeight = 2f;
		camera.viewportWidth = 2f * width / height;
		camera.zoom = 8f;
	}
	
	public OrthographicCamera getCamera() {
		return camera;
	}
}
