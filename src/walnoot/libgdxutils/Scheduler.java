package walnoot.libgdxutils;

import com.badlogic.gdx.utils.Array;

public class Scheduler {
	private float fps;
	
	private Array<ScheduledEvent> events = new Array<ScheduledEvent>(false, 16);
	
	public Scheduler(float delta) {
		this.fps = delta;
	}
	
	public void update() {
		for (ScheduledEvent event : events) {
			event.ticksLeft--;
			
			event.callback.reportProgress(1f - (event.ticksLeft / fps) / event.time, event.isDone(), event.argument);
		}
	}
	
	public void addScheduledEvent(SchedulerCallback callback, float time, Object argument) {
		ScheduledEvent event = getFreeEvent();
		event.callback = callback;
		event.time = time;
		event.ticksLeft = (int) (time * fps);
		event.argument = argument;
	}
	
	private ScheduledEvent getFreeEvent() {
		for (ScheduledEvent event : events) {
			if (event.isDone()) return event;
		}
		
		ScheduledEvent event = new ScheduledEvent();
		events.add(event);
		
		return event;
	}
	
	private class ScheduledEvent {
		private int ticksLeft;
		private float time;
		private SchedulerCallback callback;
		private Object argument;
		
		private boolean isDone() {
			return ticksLeft <= 0;
		}
	}
	
	public static interface SchedulerCallback {
		public void reportProgress(float progress, boolean done, Object argument);
	}
}
