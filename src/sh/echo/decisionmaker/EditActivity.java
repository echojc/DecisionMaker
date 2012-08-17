package sh.echo.decisionmaker;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class EditActivity extends SherlockFragmentActivity {
	
	// constants
	public static final String INTENT_PROGRAM_NAME = "sh.echo.decisionmaker.program_name";
	
	// unsaved variables
	private ArrayAdapter<String> adapter;
	
	// views
	private ListView list;
	private EditText programNameText;
	private EditText optionText;
	private ImageButton omniButton;
	
	// saved variables
	private ArrayList<String> options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        
        // initialise options list
        if (savedInstanceState != null) {
        	options = savedInstanceState.getStringArrayList("options");
        } else {
            options = new ArrayList<String>();
        }
        
        // set up action bar 
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        View actionBarView = getLayoutInflater().inflate(R.layout.actionbar_editor, null);
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");
        
        // get views
        list = (ListView)findViewById(R.id.editor_option_list);
        programNameText = (EditText)actionBar.getCustomView().findViewById(R.id.editor_program_name);
        optionText = (EditText)findViewById(R.id.editor_option_text);
        omniButton = (ImageButton)findViewById(R.id.editor_omni_button);
        
        // see if we should load a particular program
        Intent intent = getIntent();
        String programName = intent.getStringExtra(INTENT_PROGRAM_NAME);
        if (programName != null) {
        	// set the program name
        	programNameText.setText(programName);
        	
        	// load options
        	String[] optionsArray = ProgramManager.getOptions(programName);
        	if (optionsArray != null) {
        		options.addAll(Arrays.asList(optionsArray));
        	}
        	
        	// set focus to textbox for new options
        	optionText.requestFocus();
        }
        
        // set up adapter for list
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
        list.setAdapter(adapter);
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
	    	// TODO: save
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
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	outState.putStringArrayList("options", options);
    }
    
    @Override
    public void onBackPressed() {
    	finishWithTransition();
    }
    
    /**
     * Callback for Add button.
     * @param v
     */
    public void addButton_OnClick(View v) {
    	String option = optionText.getText().toString();
    	options.add(option);
    	adapter.notifyDataSetChanged();
    	
    	optionText.setText("");
    }
    
    /**
     * Use this instead of finish().
     */
    private void finishWithTransition() {
    	finish();
    	overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
