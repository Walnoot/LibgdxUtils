package walnoot.libgdxutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class StackState extends State {
	private Array<State> stack = new Array<State>();
	
	public StackState(State initialState) {
		pushState(initialState);
	}
	
	@Override
	public void render() {
		for (State state : stack) {
			state.render();
		}
	}
	
	@Override
	public void update() {
		stack.get(stack.size - 1).update();
	}
	
	public void pushState(State state) {
		state.stack = this;
		state.manager = this.manager;
		stack.add(state);
		state.show();
		state.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	public void popState() {
		if (stack.size <= 1) throw new IllegalStateException("Can't pop; last element in stack");
		
		State state = stack.removeIndex(stack.size - 1);
		state.hide();
	}
	
	/**
	 * @return Returns the number of elements in this stack.
	 */
	public int getSize() {
		return stack.size;
	}
	
	@Override
	public void resize(int width, int height) {
		for (State state : stack) {
			state.resize(width, height);
		}
	}
	
	@Override
	public void setManager(StateApplication manager) {
		super.setManager(manager);
		stack.get(0).setManager(manager);
	}
	
	@Override
	public Color getBackgroundColor() {
		return stack.get(0).getBackgroundColor();
	}
}
