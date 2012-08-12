package sh.echo.helpers;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class UserActivityManager {
	private static Window window;
	private static Handler handler;
	private static Timer timer;
	
	private static final long SCREEN_DIM_DELAY = 6000;
	
	/**
	 * Initialises this helper class.
	 * @param activity
	 * @param handler A Handler object created on the main thread.
	 */
	public static void initialize(Activity activity, Handler handler) {
		window = activity.getWindow();
		UserActivityManager.handler = handler; 
	}
	
	/**
	 * Prevent the screen from dimming for a set amount of time.
	 */
	public static void poke() {
		window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		if (timer != null)
			timer.cancel();
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						window.clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
					}
				});
			}
		}, SCREEN_DIM_DELAY);
	}
}
