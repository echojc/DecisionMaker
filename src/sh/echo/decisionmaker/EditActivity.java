package sh.echo.decisionmaker;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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
	
	// saved variables
	private ArrayList<String> options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        getSupportActionBar().setIcon(R.drawable.ic_action_note);
        
        // initialise options list
        if (savedInstanceState != null) {
        	options = savedInstanceState.getStringArrayList("options");
        } else {
            options = new ArrayList<String>();
        }
        
        // get views
        list = (ListView)findViewById(R.id.editor_option_list);
        programNameText = (EditText)findViewById(R.id.editor_program_name);
        optionText = (EditText)findViewById(R.id.editor_option_text);
        
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
	    	finish();
	    	return true;
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
    	super.onBackPressed();
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
}
