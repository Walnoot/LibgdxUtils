package walnoot.libgdxutils.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Json;

public class InputTest {
	public static void main(String[] args) {
		InputHandler inputHandler = new InputHandler();
		
		inputHandler.addKey(new Key("test", Keys.A));
		
		Json jsonWriter = new Json();
		String json = jsonWriter.toJson(inputHandler);
		System.out.println(json);
		
		InputHandler fromJson = jsonWriter.fromJson(InputHandler.class, json);
		
		System.out.println(jsonWriter.toJson(fromJson));
	}
}
