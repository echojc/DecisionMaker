package sh.echo.helpers;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesHelper {
	
	private static final String STRING_SEP = "_";
	private static final String STRING_SIZE = "size";

	/**
	 * Writes a string array to shared preferences. Editor.commit() is NOT called.
	 * @param editor An editor to the destination shared preferences. 
	 * @param name
	 * @param array
	 */
	public static void putStringArray(Editor editor, String name, String[] array) {
		editor.putInt(name + STRING_SEP + STRING_SIZE, array.length);
		
		for (int i = 0; i < array.length; i++) {
			editor.putString(name + STRING_SEP + i, array[i]);
		}
	}
	
	/**
	 * Retrieves a string array from shared preferences.
	 * @param prefs
	 * @param name
	 * @return
	 */
	public static String[] getStringArray(SharedPreferences prefs, String name) {
		int size = prefs.getInt(name + STRING_SEP + STRING_SIZE, -1);
		if (size < 0)
			return null;
		
		String[] array = new String[size];
		for (int i = 0; i < array.length; i++)
			array[i] = prefs.getString(name + STRING_SEP + i, "");
		
		return array;
	}
}
