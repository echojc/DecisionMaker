package sh.echo.decisionmaker;

import java.util.ArrayList;
import java.util.Arrays;

import sh.echo.decisionmaker.fragments.BackConfirmDialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class EditActivity extends SherlockFragmentActivity implements ActionMode.Callback, TextWatcher, DialogInterface.OnClickListener {
	
	// constants
	public static final String INTENT_PROGRAM_NAME = "sh.echo.decisionmaker.program_name";
	
	// statics
	private static ActionMode actionMode = null;
	
	// unsaved variables
	private ArrayAdapter<String> adapter;
	
	// views
	private ListView list;
	private EditText programNameText;
	private EditText optionText;
	private ImageButton omniButton;
	private ImageButton cancelButton;
	
	// saved variables
	private ArrayList<String> options;
	private boolean unsavedChanges;
	private String originalProgramName;
	private boolean inEditMode;
	private int editOptionIndex = -1;

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
        omniButton = (ImageButton)findViewById(R.id.editor_omni_button);
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
        
        // set up adapter for list with override for edit mode
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, options) {
        	@Override
        	public boolean isEnabled(int position) {
        		if (inEditMode) {
        			return position == editOptionIndex;
        		} else {
        			return super.isEnabled(position);
        		}
        	}
        };
        list.setAdapter(adapter);
        
        // set up listener for opening action mode
        final ActionMode.Callback self = this;
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// exit edit mode if we're in it
				if (inEditMode) {
					disableEditMode();
					return;
				}
				
				// if we're not in action mode, go into action mode
				if (actionMode == null) {
					actionMode = startActionMode(self);
				}
				
				// only <API7 method available for getting count efficiently
				@SuppressWarnings("deprecation")
				int selectedCount = list.getCheckItemIds().length;
				
				// only show edit button if one item is selected
				actionMode.getMenu().findItem(R.id.menu_edit_context_edit).setVisible(selectedCount == 1);
				
				// disable action menu if no items are selected
				if (selectedCount == 0)
					actionMode.finish();
			}
        });
        
        // set up ime listener for adding new options
        optionText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// treat "go" as triggering an add operation
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					omniButton_OnClick(omniButton);
					return true;
				}
				return false;
			}
		});
        
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
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.edit_contextual, menu);
		mode.setTitle(R.string.editor_options);
		return true;
	}
	
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}
	
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// get all selected items
		SparseBooleanArray checkedItems = list.getCheckedItemPositions();
		
		switch (item.getItemId()) {
		case R.id.menu_edit_context_edit:
			inEditMode = true;
			
			// find which item is checked
			for (int i = 0; i < options.size(); i++) {
				if (checkedItems.get(i)) {
					editOptionIndex = i;
					break;
				}
			}
			
			if (editOptionIndex == -1) {
				//Log.w("Editor", "trying to edit but nothing selected");
				return false;
			}
			
			cancelButton.setVisibility(View.VISIBLE);
			list.setItemChecked(editOptionIndex, true);
			
			String option = options.get(editOptionIndex);
			
			actionMode.getMenu().findItem(R.id.menu_edit_context_edit).setVisible(false);
			actionMode.getMenu().findItem(R.id.menu_edit_context_delete).setVisible(false);
			actionMode.setTitle(String.format(getResources().getString(R.string.editor_editing), option));
			
			optionText.setText(option);
			optionText.selectAll();
			optionText.requestFocus();
			return true;
		case R.id.menu_edit_context_delete:
			for (int i = options.size(); i >= 0; i--) {
				if (checkedItems.get(i)) {
					list.setItemChecked(i, false);
					options.remove(adapter.getItem(i));
				}
			}
			adapter.notifyDataSetChanged();
			unsavedChanges = true;
			actionMode.finish();
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// uncheck all items
		for (int i = 0; i < list.getCount(); i++) {
			list.setItemChecked(i, false);
		}
		
		actionMode = null;
		
		if (inEditMode)
			disableEditMode();
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
    		disableEditMode();
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
    		
    		// scroll to bottom
    		list.post(new Runnable() {
    			@Override
    			public void run() {
    				list.setSelection(list.getCount() - 1);
    			}
    		});
    	}
    	
    	adapter.notifyDataSetChanged();
    	unsavedChanges = true;
		disableEditMode();
    }
    
    /**
     * Callback for cancel button (only visible in edit mode).
     * @param v
     */
    public void cancelButton_OnClick(View v) {
		disableEditMode();
    }
    
    private void disableEditMode() {
    	cancelButton.setVisibility(View.GONE);
    	optionText.setText("");
		inEditMode = false;
		
		list.setItemChecked(editOptionIndex, false);
		editOptionIndex = -1;
		
		if (actionMode != null)
			actionMode.finish();
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
		
		if (which != DialogInterface.BUTTON_NEUTRAL)
			finishWithTransition();
	}
}
