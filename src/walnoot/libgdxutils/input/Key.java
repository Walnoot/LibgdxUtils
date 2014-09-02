package walnoot.libgdxutils.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;

public class Key implements Serializable {
	private final IntArray keyCodes = new IntArray();
	private String name;
	
	private boolean justTouched;
	
	public Key(String name, int... keyCodes) {
		this.name = name;
		this.keyCodes.addAll(keyCodes);
	}
	
	public Key() {
	}
	
	public boolean contains(int keyCode) {
		for (int i = 0; i < keyCodes.size; i++) {
			if (keyCodes.get(i) == keyCode) return true;
		}
		
		return false;
	}
	
	public void addKeyCode(int keyCode) {
		keyCodes.add(keyCode);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isJustTouched() {
		return justTouched;
	}
	
	public void setJustTouched(boolean justTouched) {
		this.justTouched = justTouched;
	}
	
	public boolean isTouched() {
		for (int i = 0; i < keyCodes.size; i++) {
			if (Gdx.input.isKeyJustPressed(keyCodes.get(i))) return true;
		}
		
		return false;
	}
	
	@Override
	public void write(Json json) {
		json.writeValue("name", name);
		json.writeArrayStart("keycodes");
		for (int i = 0; i < keyCodes.size; i++) {
			json.writeValue(Keys.toString(keyCodes.get(i)));
		}
		json.writeArrayEnd();
	}
	
	@Override
	public void read(Json json, JsonValue jsonData) {
		name = jsonData.getString("name");
		
		JsonValue keys = jsonData.get("keycodes");
		for (int i = 0; i < keys.size; i++) {
			addKeyCode(Keys.valueOf(keys.getString(i)));
		}
	}
}
