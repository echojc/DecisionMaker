package sh.echo.decisionmaker;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class ProgramManager {
	
	private static final int WORD_LENGTH_MIN = 10;
	private static final int WORD_LENGTH_DELTA = 4;
	
	private static final String PROGRAM_PREFS_NAME = "sh.echo.decisionmaker.programs";

	private static Map<String, String[]> programs = new HashMap<String, String[]>();
	
	/**
	 * Loads all saved programs from shared preferences.
	 * @param context Any context in the application.
	 */
	@SuppressWarnings("unchecked")
	public static void loadPrograms(Context context) {
		// get shared preferences
		SharedPreferences prefs = context.getSharedPreferences(PROGRAM_PREFS_NAME, Context.MODE_PRIVATE);
		
		// grab all key-value pairs
		Map<String, ?> data = prefs.getAll();
		for (String programName : data.keySet()) {
			
			// try to cast value to Set
			Set<String> options;
			try {
				options = (Set<String>)data.get(programName);
			} catch (ClassCastException e) {
				Log.w("ProgramManager", "key with prefix but illegal value");
				continue;
			}
			
			// add to hash
			programs.put(programName, options.toArray(new String[options.size()]));
		}
	}
	
	/**
	 * Saves all current programs into shared preferences.
	 * @param context Any context in the application.
	 */
	public static void savePrograms(Context context) {
		// get shared preferences editor
		SharedPreferences prefs = context.getSharedPreferences(PROGRAM_PREFS_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		
		// empty preferences and write everything back into it
		for (String programName : programs.keySet()) {
			
			// put options into a set
			Set<String> options = new HashSet<String>();
			Collections.addAll(options, programs.get(programName));
			
			// write to prefs
			editor.putStringSet(programName, options);
		}
		
		// commit
		editor.commit();
	}
	
	/**
	 * Gets all options associated with this program name.
	 * @param programName 
	 * @return A String array of options if the program name exists, otherwise null.
	 */
	public static String[] getOptions(String programName) {
		if (!programs.containsKey(programName))
			return null;
		return programs.get(programName);
	}
	
	/**
	 * Determines whether a given program exists already.
	 * @param programName
	 * @return
	 */
	public static boolean programExists(String programName) {
		return programs.containsKey(programName);
	}
	
	/**
	 * Creates a program with the specified options or, if it exists already, 
	 * append the options to the program.
	 * @param programName
	 * @param options Options to be associated with this program.
	 */
	public static void addProgram(String programName, String... options) {
		// check if something exists already
		String[] existingOptions = getOptions(programName);
		if (existingOptions == null) {
			programs.put(programName, options);
		} else {
			Log.i("ProgramManager", "appending " + options.length + " options to " + programName);
			// append new options
			String[] combinedOptions = new String[existingOptions.length + options.length];
			System.arraycopy(existingOptions, 0, combinedOptions, 0, existingOptions.length);
			System.arraycopy(options, 0, combinedOptions, existingOptions.length, options.length);
			// overwrite in map
			programs.put(programName, combinedOptions);
		}
	}
	
	/**
	 * Removes a program from the application.
	 * @param programName
	 */
	public static void removeProgram(String programName) {
		// check if this program exists
		if (!programs.containsKey(programName)) {
			Log.w("ProgramManager", "tried to remove non-existent program");
			return;
		}
		
		// remove it
		programs.remove(programName);
	}
	
	/**
	 * Gets an array representing all program names.
	 * @return
	 */
	public static String[] getProgramNames() {
		return programs.keySet().toArray(new String[programs.size()]);
	}
	
	/**
	 * Gets the length of the longest option in the given program.
	 * @param programName
	 * @return
	 */
	public static int getLongestOptionLength(String programName) {
		// default value
		if (!programs.containsKey(programName)) {
			return WORD_LENGTH_MIN;
		}
		
		// iterate over all options and find the longest length
		String[] options = getOptions(programName);
		int maxLength = 0;
		for (String s : options) {
			if (s.length() > maxLength)
				maxLength = s.length();
		}
		return Math.max(WORD_LENGTH_MIN, maxLength + WORD_LENGTH_DELTA);
	}
}
