package sh.echo.decisionmaker;

import java.util.ArrayList;
import java.util.Arrays;

import sh.echo.decisionmaker.fragments.BackConfirmDialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class EditActivity extends SherlockFragmentActivity implements TextWatcher, DialogInterface.OnClickListener {
	
	// constants
	public static final String INTENT_PROGRAM_NAME = "sh.echo.decisionmaker.program_name";
	
	// unsaved variables
	private ArrayAdapter<String> adapter;
	
	// views
	private ListView list;
	private EditText programNameText;
	private EditText optionText;
	private ImageButton cancelButton;
	
	// saved variables
	private ArrayList<String> options;
	private boolean unsavedChanges;
	private String originalProgramName;
	private boolean inEditMode;
	private int editOptionIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // set up action bar 
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        View actionBarView = getLayoutInflater().inflate(R.layout.actionbar_editor, null);
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayShowCustomEnabled(true);
        
        // get views
        list = (ListView)findViewById(R.id.editor_option_list);
        programNameText = (EditText)actionBar.getCustomView().findViewById(R.id.editor_program_name);
        optionText = (EditText)findViewById(R.id.editor_option_text);
        cancelButton = (ImageButton)findViewById(R.id.editor_cancel_button);
        
        // initialise options list
        if (savedInstanceState != null) {
        	// load previous list
        	options = savedInstanceState.getStringArrayList("options");
        	unsavedChanges = savedInstanceState.getBoolean("unsavedChanges");
        	originalProgramName = savedInstanceState.getString("originalProgramName");
        	inEditMode = savedInstanceState.getBoolean("inEditMode");
        	editOptionIndex = savedInstanceState.getInt("editOptionIndex");
        } else {
        	// create new
            options = new ArrayList<String>();
            
            // see if we should load a particular program
            Intent intent = getIntent();
            originalProgramName = intent.getStringExtra(INTENT_PROGRAM_NAME);
            if (originalProgramName != null) {
            	// set the program name
            	programNameText.setText(originalProgramName);
            	
            	// load options
            	String[] optionsArray = ProgramManager.getOptions(originalProgramName);
            	if (optionsArray != null) {
            		options.addAll(Arrays.asList(optionsArray));
            	}
            	
            	// set focus to textbox for new options
            	optionText.requestFocus();
            } else {
            	// set focus to name textbox
            	programNameText.requestFocus();
            }
        }
        
        // set up adapter for list
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
        list.setAdapter(adapter);
        
        // set context menu for list
        registerForContextMenu(list);
        
        // set default return code to cancel
        setResult(RESULT_CANCELED);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.activity_edit, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    
	    case R.id.menu_save:
	    	// try to save and only leave if success
	    	if (save())
	    		finishWithTransition();
	    	return true;
	    	
	    case android.R.id.home:
	    case R.id.menu_cancel:
	    	// handle identically to pressing back
	    	onBackPressed();
	    	return true;
	    	
        default:
            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_contextual, menu);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.menu_edit_context_edit:
			inEditMode = true;
			editOptionIndex = (int)info.id;
			
			cancelButton.setVisibility(View.VISIBLE);
			
			optionText.setText(options.get(editOptionIndex));
			optionText.selectAll();
			optionText.requestFocus();
			
			String optionText = getResources().getString(R.string.editor_editing) + options.get(editOptionIndex);
			options.remove(editOptionIndex);
			options.add(editOptionIndex, optionText);
			adapter.notifyDataSetChanged();
			return true;
		case R.id.menu_edit_context_delete:
			// remove selected option
			// must cast long to int to call the right overloaded method
			options.remove((int)info.id);
			adapter.notifyDataSetChanged();
			unsavedChanges = true;
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		programNameText.addTextChangedListener(this);
	}
	
	@Override
	public void onStop() {
		programNameText.removeTextChangedListener(this);
		super.onStop();
	}
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	outState.putStringArrayList("options", options);
    	outState.putBoolean("unsavedChanges", unsavedChanges);
    	outState.putString("originalProgramName", originalProgramName);
    	outState.putBoolean("inEditMode", inEditMode);
    	outState.putInt("editOptionIndex", editOptionIndex);
    }
    
    @Override
    public void onBackPressed() {
    	
    	// if we're editing, cancel edit
    	if (inEditMode) {
    		cancelButton_OnClick(cancelButton);
    		return;
    	}
    	
    	// check for unsaved changes
    	if (unsavedChanges) {
    		BackConfirmDialogFragment.newInstance().show(getSupportFragmentManager(), "back");
    	} else {
    		finishWithTransition();
    	}
    }
    
    /**
     * Validates and save everything into ProgramManager.
     */
    private boolean save() {
    	// make a toast
		Toast warningToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		
    	// run all validate checks
    	String programName = programNameText.getText().toString();
    	if (programName.equals("")) {
    		warningToast.setGravity(Gravity.TOP, 0, 0);
    		warningToast.setText(R.string.editor_warn_no_name);
    		warningToast.show();
    		return false;
    	} else if (ProgramManager.programExists(programName) && !programName.equals(originalProgramName)) {
    		warningToast.setGravity(Gravity.TOP, 0, 0);
    		warningToast.setText(R.string.editor_warn_duplicate_name);
    		warningToast.show();
    		return false;
    	} else if (options.isEmpty()) {
    		warningToast.setGravity(Gravity.CENTER, 0, 0);
    		warningToast.setText(R.string.editor_warn_no_options);
    		warningToast.show();
    		return false;
    	}
    	
    	// perform save
    	if (originalProgramName != null) {
    		ProgramManager.removeProgram(originalProgramName);
    	}
    	ProgramManager.addProgram(programName, options.toArray(new String[options.size()]));
    	ProgramManager.savePrograms(this);
    	
    	// saved
    	Intent data = new Intent();
    	data.putExtra(INTENT_PROGRAM_NAME, programName);
    	setResult(RESULT_OK, data);
    	return true;
    }
    
    /**
     * Callback for Add button.
     * @param v
     */
    public void omniButton_OnClick(View v) {
    	String option = optionText.getText().toString();
    	
    	// check for empty string
    	if (option == null || option.equals("")) {
    		Toast warningToast = Toast.makeText(this, R.string.editor_warn_empty_option, Toast.LENGTH_SHORT);
    		warningToast.setGravity(Gravity.CENTER, 0, 0);
    		warningToast.show();
    		return;
    	}
    	
    	// add to list
    	if (inEditMode) {
    		options.remove(editOptionIndex);
    		options.add(editOptionIndex, option);
    	} else {
    		options.add(option);
    	}
    	
    	adapter.notifyDataSetChanged();
    	unsavedChanges = true;
    	
    	cancelButton.setVisibility(View.GONE);
    	optionText.setText("");
    	inEditMode = false;
    }
    
    /**
     * Callback for cancel button (only visible in edit mode).
     * @param v
     */
    public void cancelButton_OnClick(View v) {
    	optionText.setText("");
		inEditMode = false;
		
		cancelButton.setVisibility(View.GONE);
		
		String originalOption = options.get(editOptionIndex).replace(getResources().getString(R.string.editor_editing), "");
		options.remove(editOptionIndex);
		options.add(editOptionIndex, originalOption);
		adapter.notifyDataSetChanged();
    }
    
    /**
     * Use this instead of finish().
     */
    private void finishWithTransition() {
    	finish();
    	overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    
    /* TextWatcher interface */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		unsavedChanges = true;
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
	@Override
	public void afterTextChanged(Editable s) {}
	
	/* DialogListener.OnClickListener interface */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		// check for positive button
		if (which == DialogInterface.BUTTON_POSITIVE) {
			// try to save, and if it didn't succeed then stop
			if (!save())
				return;
		}
		
		dialog.dismiss();
		finishWithTransition();
	}
}
