package sh.echo.decisionmaker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {

	// constants
	private final static char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVXYZ0123456789!@#$%^&*()_+=[]\\{}|;':\",./<>?`~".toCharArray();
	
	// unsaved variables
	private static Handler handler;
	private Map<String, String[]> programs = new HashMap<String, String[]>();
	
	// saved state variables
	private int longestChoiceLength;
	private String currentOption;
	private boolean skipWarpText;
	
	// views
	private Spinner programSpinner;
	private TextView outputDisplay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// set views
		programSpinner = (Spinner)findViewById(R.id.current_program);
		outputDisplay = (TextView)findViewById(R.id.do_randomize);
		
		// create a handler for tasks
		handler = new Handler();

		// load existing programs
		String[] files = fileList();
		for (String fileName : files) {
			FileInputStream fis = null;
			try {
				fis = openFileInput(fileName);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				String options = br.readLine();
				programs.put(fileName, options.split(","));
			} catch (FileNotFoundException e) {
				// we can ignore disappearing files
				continue;
			} catch (IOException e) {
				Log.e("FileLoad", "IOException: " + e.getMessage());
			} finally {
				try {
					if (fis != null)
						fis.close();
				} catch (IOException e) {
				}
			}
		}

		// TEST: add a default option
		programs.put("Fast food", new String[] { "McDonald's", "Burger King", "KFC", "Carl's Jr." });
		
		// add in option to create a new program
		programs.put("Create new...", new String[0]);
		
		// put programs into spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, programs.keySet().toArray(new String[programs.size()]));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		programSpinner.setAdapter(adapter);
		
		// create listener for item select on spinner
		programSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (pos + 1 == parent.getAdapter().getCount()) {
					// TODO: create new activity
					Log.i("OnItemSelectedListener", "creating new activity");
				}
				
				String name = (String)parent.getItemAtPosition(pos);
				Log.i("OnItemSelectedListener", "selected item " + name);
				
				// set length of longest option within program
				for (String choice : programs.get(name)) {
					if (choice.length() > longestChoiceLength)
						longestChoiceLength = choice.length();
				}
				
				// reset text
				if (!skipWarpText)
					warpText(getResources().getString(R.string.do_randomize), R.color.nice_shade_of_light_green);
				skipWarpText = false;
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// do nothing
				Log.i("OnItemSelectedListener", "nothing selected");
			}
		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("longestChoiceLength", longestChoiceLength);
		outState.putString("currentOption", currentOption);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		longestChoiceLength = savedInstanceState.getInt("longestChoiceLength");
		currentOption = savedInstanceState.getString("currentOption");
		outputDisplay.setText(currentOption);
		skipWarpText = true;
		
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
	 * @param s
	 * @param colorId The colour for the background.
	 */
	public void warpText(String s, int colorId) {
		final int startLength = longestChoiceLength;
		final String targetString = s;
		final int finalColorId = colorId;

		// set what the current option is
		currentOption = s;
		
		// run this not on the main UI thread
		Thread t = new Thread(new Runnable() {
			public void run() {
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
					
					// set alpha (with easing)
					int alpha = (int)(Math.pow(f / startLength, 2) * 255);
					int color = getResources().getColor(finalColorId);
					setBackgroundColorAsync((color & 0x00ffffff) | (alpha << 24));
					
					// run this at roughly 50fps
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {}
				}
			}
		});
		t.start();
	}
	
	/**
	 * Set the text of the main output.
	 * Thread-safe.
	 * @param message
	 */
	public void setTextAsync(String message) {
		final String finalMsg = message;
		
		handler.post(new Runnable() {
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
		
		handler.post(new Runnable() {
			public void run() {
				outputDisplay.setBackgroundColor(finalColor);
			}
		});
	}
	
	/**
	 * OnClick callback for making a choice. Selects a random option and displays it.
	 * @param view
	 */
	public void doRandomizeOnClick(View view) {
		// get options for current program
		String currentProgram = (String)programSpinner.getSelectedItem();
		String[] options = programs.get(currentProgram);
		
		// select a random one
		int random = (int)(Math.random() * options.length);
		warpText(options[random]);
	}
}
