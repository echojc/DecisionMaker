package sh.echo.decisionmaker;

import sh.echo.helpers.ShakeGestureManager;
import sh.echo.helpers.ShakeGestureManager.ShakeGestureListener;
import sh.echo.helpers.UserActivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class MainActivity extends SherlockActivity {

	// constants
	private final static char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVXYZ0123456789!@#$%^&*()_+=[]\\{}|;':\",./<>?`~".toCharArray();
	
	// unsaved variables
	private OnItemSelectedListener spinnerHandler;
	private ShakeGestureListener accelHandler;
	private Thread warpTextThread;
	private Toast randomizeToast;
	
	// saved state variables
	private int currentSpinnerPosition = 0;
	private String currentOption = null;
	private boolean skipWarpText = false;
	
	// views
	private Spinner programSpinner;
	private TextView outputDisplay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// set views
		programSpinner = (Spinner)findViewById(R.id.current_program);
		outputDisplay = (TextView)findViewById(R.id.option_display);
		
		// restore state variables
		if (savedInstanceState != null) {
			Log.i("OnCreate", "restoring state");
			currentSpinnerPosition = savedInstanceState.getInt("currentSpinnerPosition");
			currentOption = savedInstanceState.getString("currentOption");
			outputDisplay.setText(currentOption);
			outputDisplay.setBackgroundColor(0x00ffffff);
			skipWarpText = true;
		}

		// load existing programs
		ProgramManager.loadPrograms(this);

		// TEST: add a default option
		ProgramManager.addOptions("Fast food", "McDonald's", "Wendy's", "KFC", "Carl's Jr.");
		ProgramManager.addOptions("Empty");
		
		// BAD UI - USE OPTION MENU INSTEAD
		// add in option to create a new program
		//ProgramManager.addOptions("Create new...");
		
		// put programs into spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ProgramManager.getProgramNames());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		programSpinner.setAdapter(adapter);
		
		// create listener for item select on spinner
		programSpinner.setOnItemSelectedListener(getOnItemSelectedListener());
		
		// select last selected spinner option (0 by default)
		programSpinner.setSelection(currentSpinnerPosition);
		
		// initialise shake handling
		ShakeGestureManager.initialize(this);
		ShakeGestureManager.enable();
		ShakeGestureManager.addShakeGestureListener(getAccelerometerListener());
		
		// initialise user activity manager for manually delaying screen from dimming
		UserActivityManager.initialize(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.activity_main, menu);
	    return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("currentSpinnerPosition", currentSpinnerPosition);
		outState.putString("currentOption", currentOption);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ShakeGestureManager.enable();
	}
	
	@Override
	public void onStop() {
		ShakeGestureManager.disable();
		super.onStop();
	}
	
	/**
	 * Animates the text into the specified string, using the default colour.
	 * Thread-safe.
	 * @param s
	 */
	public void warpText(String s) {
		warpText(s, R.color.nice_shade_of_light_blue);
	}
	
	/**
	 * Animates the text into the specified string.
	 * Thread-safe.
	 * @param message
	 * @param colorId The colour for the background.
	 */
	public void warpText(String message, int colorId) {
		final String cachedProgramName = getCurrentProgramName();
		final String targetString = message;
		final int finalColorId = colorId;

		// set what the current option is
		currentOption = message;
		
		// ensure only one animation runs at a time
		if (warpTextThread != null && warpTextThread.isAlive())
			warpTextThread.interrupt();

		// run apart from the main UI thread
		warpTextThread = new Thread(new Runnable() {
			public void run() {
				// initialise local cached variables
				int startLength = ProgramManager.getLongestOptionLength(cachedProgramName);
				int maskedColor = getResources().getColor(finalColorId) & 0x00ffffff;
				
				// animate text
				for (float f = startLength; f >= 0; f -= 0.3f) {
					// stretch out animation even more
					int i = (int)f;
					
					// create random string
					int targetLength = Math.max(i, targetString.length());
					char[] randomChars = new char[targetLength];
					for (int j = 0; j < targetLength; j++)
						randomChars[j] = CHARS[(int)(Math.random() * CHARS.length)];
					
					// overwrite with values from original
					for (int j = 0; j < targetString.length() - i; j++) {
						int pos = (j % 2 == 0) ? (j/2) : (targetString.length() - (j/2) - 1);
						randomChars[pos] = targetString.charAt(pos);
					}
					setTextAsync(new String(randomChars));
					
					// set alpha
					int alpha = (int)(f /startLength * 255);
					setBackgroundColorAsync(maskedColor | (alpha << 24));
					
					// run this at roughly 50fps
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// we got interrupted, so stop immediately
						return;
					}
					
					// check for interrupt
					if (Thread.currentThread().isInterrupted())
						return;
				}
				
				// set to final step to prevent half-finished animations
				setTextAsync(targetString);
				setBackgroundColorAsync(maskedColor);
			}
		});
		warpTextThread.start();
	}
	
	/**
	 * Set the text of the main output.
	 * Thread-safe.
	 * @param message
	 */
	public void setTextAsync(String message) {
		final String finalMsg = message;
		
		runOnUiThread(new Runnable() {
			public void run() {
				outputDisplay.setText(finalMsg);
			}
		});
	}
	
	/**
	 * Set the background colour of the main output.
	 * Thread-safe.
	 * @param message
	 */
	public void setBackgroundColorAsync(int color) {
		final int finalColor = color;
		
		runOnUiThread(new Runnable() {
			public void run() {
				outputDisplay.setBackgroundColor(finalColor);
			}
		});
	}
	
	/**
	 * Displays a random option from the current program.
	 */
	public void randomize() {
		// cache program name
		String currentProgramName = getCurrentProgramName();
		if (currentProgramName == null) {
			Log.w("randomize()", "could not get program name");
			return;
		}
		
		// get options for current program
		String[] options = ProgramManager.getOptions(currentProgramName);
		if (options != null && options.length > 0) {
			// select a random one
			int random = (int)(Math.random() * options.length);
			warpText(options[random]);
		} else {
			// display toast
			if (randomizeToast == null)
				randomizeToast = Toast.makeText(this, "No options to display!", Toast.LENGTH_SHORT);
			randomizeToast.show();
			
			Log.w("randomize()", "no options in " + currentProgramName);
		}
	}
	
	/**
	 * Gets the callback handler for the main spinner.
	 * @return
	 */
	private OnItemSelectedListener getOnItemSelectedListener() {
		if (spinnerHandler == null) {
			spinnerHandler = new OnItemSelectedListener() {
				
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (pos + 1 == parent.getAdapter().getCount()) {
						// TODO: create new activity
						Log.i("OnItemSelectedListener", "creating new activity");
					}
					
					currentSpinnerPosition = pos;
					Log.i("OnItemSelectedListener", "selected item " + getCurrentProgramName());
					
					// reset text
					if (!skipWarpText)
						warpText(getResources().getString(R.string.option_display_default), R.color.nice_shade_of_light_green);
					skipWarpText = false;
				}

				public void onNothingSelected(AdapterView<?> parent) {
					// do nothing
					Log.i("OnItemSelectedListener", "nothing selected");
				}
			};
		}
		return spinnerHandler;
	}
	
	/**
	 * Gets the callback handler for shake gestures.
	 * @return
	 */
	private ShakeGestureListener getAccelerometerListener() {
		if (accelHandler == null) {
			accelHandler = new ShakeGestureListener() {

				@Override
				public void onShake() {
					// display a new option
					randomize();
					
					// keep screen on for a while
					UserActivityManager.poke();
				}
			};
		}
		return accelHandler;
	}
	
	/**
	 * Gets the program name of the currently selected item.
	 * @return A new String object containing the name, or null on error.
	 */
	private String getCurrentProgramName() {
		if (currentSpinnerPosition < programSpinner.getAdapter().getCount())
			return new String((String)programSpinner.getItemAtPosition(currentSpinnerPosition));
		else
			return null;
	}
}
