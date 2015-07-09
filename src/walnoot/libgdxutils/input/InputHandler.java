package walnoot.libgdxutils.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class InputHandler extends InputAdapter {
	private Array<Key> keys = new Array<Key>();
	
	private Vector3 tmp = new Vector3();
	
	public void update() {
		for (Key key : keys) {
			key.setJustTouched(false);
		}
	}
	
	public static InputHandler read(FileHandle file) {
		return new Json().fromJson(InputHandler.class, file);
	}
	
	public void write(FileHandle file) {
		new Json().toJson(this, file);
	}
	
	public Key getKey(String name) {
		for (Key key : keys) {
			if (key.getName().equals(name)) return key;
		}
		
		return null;
	}
	
	public Vector2 getMousePosition(Camera camera, Vector2 result) {
		tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
		camera.unproject(tmp);
		
		return result.set(tmp.x, tmp.y);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		for (Key key : keys) {
			if (key.contains(keycode)) key.setJustTouched(true);
		}
		
		return false;
	}
	
	public void addKey(Key key) {
		keys.add(key);
	}
	
	public Array<Key> getKeys() {
		return keys;
	}
}
