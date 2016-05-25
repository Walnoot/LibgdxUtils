package walnoot.libgdxutils.world.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

import walnoot.libgdxutils.world.Component;

public class SpritesComponent extends Component {
	public final Array<ComponentSprite> sprites = new Array<ComponentSprite>();
	
	public static class ComponentSprite {
		public transient Sprite sprite;
		public String name;
		public float z;
		public float width = 1f, height = 1f;
		public float xOffset, yOffset;
		public final Color color = new Color(Color.WHITE);
	}
}
