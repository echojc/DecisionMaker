package sh.echo.decisionmaker.fragments;

import sh.echo.decisionmaker.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class DeleteConfirmDialogFragment extends SherlockDialogFragment {
	
	/**
	 * Creates an instance of DeleteConfirmDialogFragment for use.
	 * @return
	 */
	public static DeleteConfirmDialogFragment newInstance(String itemName) {
		DeleteConfirmDialogFragment fragment = new DeleteConfirmDialogFragment();
		Bundle args = new Bundle();
		args.putString("itemName", itemName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// get arguments from bundle
		String itemName = getArguments().getString("itemName");
		
		// validate arguments
		if (itemName == null) {
			Log.w("DeleteConfirmDialogFragment", "no program name supplied");
			itemName = "";
		}
		
		// build message string
		String message = String.format(getResources().getString(R.string.delete_confirm_dialog_message), itemName);
		
		// create dialog
		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.delete_confirm_dialog_title)
			.setMessage(message)
			.setPositiveButton(R.string.yes, (OnClickListener)getActivity())
			.setNegativeButton(R.string.no, (OnClickListener)getActivity())
			.create();
	}
}
