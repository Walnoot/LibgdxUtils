package walnoot.libgdxutils.ashley;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

public abstract class UpdateComponent extends Component {
	protected Engine engine;
	protected Entity entity;
	
	public UpdateComponent(Engine engine, Entity entity) {
		this.engine = engine;
		this.entity = entity;
	}
	
	/**
	 * Allows for logic in components, screw elegance.
	 */
	public abstract void update(float deltaTime);
}
