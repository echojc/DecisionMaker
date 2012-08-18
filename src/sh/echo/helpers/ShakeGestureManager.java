package sh.echo.helpers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeGestureManager {

	private static SensorManager sensorManager;
	private static SensorEventListener sensorHandler;
	
	private static List<ShakeGestureListener> listeners = new ArrayList<ShakeGestureListener>();
	
	private static final float SHAKE_THRESHOLD = 15.0f;

	/**
	 * Initialises this helper. Call once in onCreate().
	 * @param context
	 */
	public static void initialize(Context context) {
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	}

	/**
	 * Enables the accelerometer.
	 */
	public static void enable() {
		if (sensorManager == null)
			throw new IllegalStateException("SensorManager is null (did you forget to call initialize()?)");

		sensorManager.registerListener(getSensorEventListener(),
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		//Log.i("Accelerometer", "enabled accelerometer");
	}

	/**
	 * Disables the accelerometer.
	 */
	public static void disable() {
		if (sensorManager == null)
			throw new IllegalStateException("SensorManager is null (did you forget to call initialize()?)");

		sensorManager.unregisterListener(getSensorEventListener());
		//Log.i("Accelerometer", "disabled accelerometer");
	}

	private static SensorEventListener getSensorEventListener() {
		if (sensorHandler == null) {
			sensorHandler = new SensorEventListener() {

				@Override
				public void onSensorChanged(SensorEvent event) {
					float x = Math.abs(event.values[0]);
					
					if (x > SHAKE_THRESHOLD)
						fireOnShake();
				}

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					// do nothing
				}
			};
		}
		return sensorHandler;
	}
	
	/**
	 * Register a listener for shake gesture events.
	 * @param listener
	 */
	public static void addShakeGestureListener(ShakeGestureListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	/**
	 * Unregister a listener from receiving shake gesture events.
	 * @param listener
	 */
	public static void removeShakeGestureListener(ShakeGestureListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	
	private static void fireOnShake() {
		if (listeners.isEmpty())
			return;
		
		for (ShakeGestureListener l : listeners)
			l.onShake();
	}
	
	/**
	 * Listener interface for accelerometer events.
	 */
	public static interface ShakeGestureListener {
		void onShake();
	}
}
