package walnoot.libgdxutils.ashley;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteComponent extends Component {
	public Sprite sprite;
	public float size = 1f;
	
	public SpriteComponent() {
	}
	
	public SpriteComponent(Sprite sprite) {
		this.sprite = sprite;
	}
}
