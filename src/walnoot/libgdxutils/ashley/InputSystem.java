package walnoot.libgdxutils.ashley;

import walnoot.libgdxutils.input.InputHandler;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;

public class InputSystem extends EntitySystem {
	public InputHandler input;
	
	public InputSystem(String internalFile) {
		input = InputHandler.read(Gdx.files.internal(internalFile));
	}
	
	@Override
	public void addedToEngine(Engine engine) {
	}
}
