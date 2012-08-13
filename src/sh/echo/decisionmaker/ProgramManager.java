package sh.echo.decisionmaker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

public class ProgramManager {
	
	private static final int WORD_LENGTH_MIN = 10;
	private static final int WORD_LENGTH_DELTA = 4;

	private static Map<String, String[]> programs = new HashMap<String, String[]>();
	
	/**
	 * Loads all files associated with the specified context.
	 * @param context
	 */
	public static void loadPrograms(Context context) {
		String[] files = context.fileList();
		for (String fileName : files) {
			FileInputStream fis = null;
			try {
				fis = context.openFileInput(fileName);
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
	 * Adds the specified options to the program name if it exists already,
	 * or create a new program with these options.
	 * @param programName
	 * @param options Options to be associated with this program.
	 */
	public static void addOptions(String programName, String... options) {
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
