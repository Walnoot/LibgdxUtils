package walnoot.libgdxutils;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;

public class RenderContext {
	private Array<FrameBuffer> bufferStack = new Array<FrameBuffer>();
	
	public void setCurrentTarget(FrameBuffer target) {
		bufferStack.add(target);
		target.begin();
	}
	
	public void endCurruntTarget() {
		int index = bufferStack.size - 1;//index of the buffer that will be popped
		if (index < 0) throw new IllegalStateException("Buffer stack empty, can't pop");
		
		if (index == 0) {
			bufferStack.get(0).end();//return drawing to default framebuffer
		} else {
			bufferStack.get(index - 1).begin();
		}
		
		bufferStack.pop();
	}
	
	public Array<FrameBuffer> getBufferStack() {
		return bufferStack;
	}
}
