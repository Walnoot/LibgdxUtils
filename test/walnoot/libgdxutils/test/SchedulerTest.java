package walnoot.libgdxutils.test;

import junit.framework.Assert;

import org.junit.Test;

import walnoot.libgdxutils.Scheduler;
import walnoot.libgdxutils.Scheduler.SchedulerCallback;

public class SchedulerTest {
	@Test
	public void test() {
		Scheduler scheduler = new Scheduler(60f);
		
		TestCallback callback = new TestCallback();
		scheduler.addScheduledEvent(callback, 1f, null);
		
		for (int i = 0; i < 60; i++) {
			Assert.assertFalse(callback.done);
			scheduler.update();
		}
		
		Assert.assertTrue(callback.done);
	}
	
	private class TestCallback implements SchedulerCallback {
		private boolean done;
		
		@Override
		public void reportProgress(float progress, boolean done, Object argument) {
			System.out.println(progress);
			
			this.done = done;
		}
	}
}
