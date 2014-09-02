package walnoot.libgdxutils.ashley;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;

/**
 * Allows you to use logic inside Components by extending UpdateComponent. This
 * is a design faux pas, but (imo) it beats making a system that only operates
 * on one component.
 */
public class UpdateSystem extends EntitySystem implements EntityListener {
	private Array<Entity> entities = new Array<Entity>();
	
	@Override
	public void update(float deltaTime) {
		for (Entity entity : entities) {
			ImmutableArray<Component> components = entity.getComponents();
			
			for (int i = 0; i < components.size(); i++) {
				Component component = components.get(i);
				if (component instanceof UpdateComponent) {
					((UpdateComponent) component).update(deltaTime);
				}
			}
		}
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		engine.addEntityListener(this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		engine.removeEntityListener(this);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		entities.add(entity);
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		entities.removeValue(entity, true);
	}
}
