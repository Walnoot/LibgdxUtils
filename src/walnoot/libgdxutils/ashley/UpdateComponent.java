package walnoot.libgdxutils.ashley;

import com.badlogic.ashley.core.Component;

public abstract class UpdateComponent extends Component {
	/**
	 * Allows for logic in components, screw elegance.
	 */
	public abstract void update(float deltaTime);
}
