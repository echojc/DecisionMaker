package sh.echo.decisionmaker;

import java.util.Arrays;

import sh.echo.decisionmaker.ProgramManager.ProgramsChangedListener;
import sh.echo.helpers.ShakeGestureManager;
import sh.echo.helpers.ShakeGestureManager.ShakeGestureListener;
import sh.echo.helpers.UserActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {

	// constants
	private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVXYZ0123456789!@#$%^&*()_+=[]\\{}|;':\",./<>?`~".toCharArray();
	
	// unsaved variables
	private OnItemSelectedListener spinnerHandler;
	private ShakeGestureListener accelHandler;
	private ProgramsChangedListener programHandler;
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

		// hook into program management and load existing programs
		ProgramManager.addProgramsChangedListener(getProgramsChangedListener());
		ProgramManager.loadPrograms(this);

		// first run stuff
		boolean firstRun = true;//getPreferences(Context.MODE_PRIVATE).getBoolean("first_run", true);
		if (firstRun) {
			// add a default program if none exist
			if (ProgramManager.getProgramCount() == 0)
				ProgramManager.addProgram(getResources().getString(R.string.default_program_name), getResources().getStringArray(R.array.default_program_options));
			
			// show welcome toast
			Toast.makeText(this, getResources().getString(R.string.welcome_message), Toast.LENGTH_LONG).show();
			
			// write to preferences to prevent re-run in the future
			Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
			editor.putBoolean("first_run", false);
			editor.commit();
		}
		
		// create listener for item select on spinner
		programSpinner.setOnItemSelectedListener(getOnItemSelectedListener());
		
		// select last selected spinner option (0 by default)
		programSpinner.setSelection(currentSpinnerPosition);
		
		// initialise shake handling
		ShakeGestureManager.initialize(this);
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_new:
	    	Intent intentNew = new Intent(this, EditActivity.class);
	    	startActivity(intentNew);
	    	return true;
	    case R.id.menu_edit:
	    	Intent intentEdit = new Intent(this, EditActivity.class);
	    	intentEdit.putExtra(EditActivity.INTENT_PROGRAM_NAME, getCurrentProgramName());
	    	startActivity(intentEdit);
	    	return true;
	    case R.id.menu_delete:
	    	DeleteConfirmDialogFragment.newInstance(getCurrentProgramName()).show(getSupportFragmentManager(), "delete");
	    	return true;
        case R.id.menu_about:
        	AboutDialogFragment.newInstance().show(getSupportFragmentManager(), "about");
            return true;
        default:
            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		// disable edit/delete options if there are no more programs left
	    if (getCurrentProgramName() == null)
	        menu.setGroupEnabled(R.id.menu_group_modify, false);
	    else
	    	menu.setGroupEnabled(R.id.menu_group_modify, true);
	    return true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentSpinnerPosition", currentSpinnerPosition);
		outState.putString("currentOption", currentOption);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ShakeGestureManager.enable();
		ShakeGestureManager.addShakeGestureListener(getShakeGestureListener());
	}
	
	@Override
	public void onPause() {
		ShakeGestureManager.disable();
		ShakeGestureManager.removeShakeGestureListener(getShakeGestureListener());
		super.onPause();
	}
	
	@Override
	public void onStop() {
		// save all programs
		ProgramManager.savePrograms(this);
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
		// make a single toast object to prevent it coming up more than once
		if (randomizeToast == null)
			randomizeToast = Toast.makeText(this, R.string.no_options_message, Toast.LENGTH_SHORT);
		
		// cache program name
		String currentProgramName = getCurrentProgramName();
		if (currentProgramName == null) {
			// display toast
			randomizeToast.show();
			//Log.i("randomize()", "could not get program name");
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
						setDisplayToDefault();
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
	private ShakeGestureListener getShakeGestureListener() {
		if (accelHandler == null) {
			accelHandler = new ShakeGestureListener() {

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
	 * Gets the callback handler for program changes.
	 * @return
	 */
	private ProgramsChangedListener getProgramsChangedListener() {
		if (programHandler == null) {
			// need to pass a context
			final Context finalContext = this;
			
			programHandler = new ProgramsChangedListener() {
				
				public void programsChanged(int what) {
					// get a sorted array of all programs
					String[] programNames = ProgramManager.getProgramNames();
					Arrays.sort(programNames);
					
					// put into an adapter and assign to spinner
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(finalContext, android.R.layout.simple_spinner_item, programNames);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					programSpinner.setAdapter(adapter);
					
					// reset
					setDisplayToDefault();
				}
			};
		}
		
		return programHandler;
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
	
	/**
	 * Sets the display back to the default message.
	 */
	private void setDisplayToDefault() {
		warpText(getResources().getString(R.string.option_display_default), R.color.nice_shade_of_light_green);
	}
}
