package walnoot.libgdxutils.ashley;

import java.util.Comparator;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class SpriteBatchRenderer implements Comparator<Entity> {
	private SpriteBatch batch = new SpriteBatch();
	private OrthographicCamera camera = new OrthographicCamera();
	
	private PositionComponent cameraPos;
	private ImmutableArray<Entity> entities;
	private Array<Entity> sortedEntities = new Array<Entity>();
	
	private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);
	private ComponentMapper<SpriteComponent> spriteMapper = ComponentMapper.getFor(SpriteComponent.class);
	
	public SpriteBatchRenderer(Engine engine, PositionComponent cameraPos) {
		this.cameraPos = cameraPos;
		
		entities = engine.getEntitiesFor(Family.getFor(PositionComponent.class, SpriteComponent.class));
	}
	
	public void render() {
		camera.viewportWidth = 2f * Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
		camera.viewportHeight = 2f;
		camera.position.set(cameraPos.pos);
		
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		
		sortedEntities.clear();
		for (int i = 0; i < entities.size(); i++) {
			sortedEntities.add(entities.get(i));
		}
		sortedEntities.sort(this);
		
		batch.begin();
		for (Entity entity : sortedEntities) {
			SpriteComponent spriteComponent = spriteMapper.get(entity);
			Vector3 pos = posMapper.get(entity).pos;
			Sprite sprite = spriteComponent.sprite;
			
			if (sprite != null) {
				sprite.setSize(spriteComponent.size, spriteComponent.size);
				sprite.setCenter(pos.x, pos.y);
				
				sprite.draw(batch);
			}
		}
		batch.end();
	}
	
	public void setZoom(float zoom) {
		camera.zoom = zoom;
	}
	
	@Override
	public int compare(Entity o1, Entity o2) {
		PositionComponent pos1 = posMapper.get(o1);
		PositionComponent pos2 = posMapper.get(o2);
		
		if (pos1.pos.z > pos2.pos.z) return 1;
		return -1;
	}
}
