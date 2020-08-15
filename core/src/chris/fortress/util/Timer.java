package chris.fortress.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**A class that counts down from the given time. Used to show how much time is left before the game ends*/
public class Timer {
	private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
	
	private int time;
	private ScheduledFuture<?> timer;
	
	public Timer(int time) {
		this.time = time;
		timer = scheduledExecutorService.scheduleAtFixedRate(()->lowerTime(), 1, 1, TimeUnit.SECONDS);
	}
	private void lowerTime() {
		if (time <= 0) timer.cancel(false);
		else time--;
	}
	public int getTimeRemaining() {
		return time;
	}
	public void stopTimer() {
		timer.cancel(true);
	}
	public static void dispose() {
		scheduledExecutorService.shutdown();
	}
	public static ScheduledExecutorService getScheduledExecutorService() {
		return scheduledExecutorService;
	}
}