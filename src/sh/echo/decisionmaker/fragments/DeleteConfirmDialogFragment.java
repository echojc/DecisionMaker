package sh.echo.decisionmaker.fragments;

import sh.echo.decisionmaker.ProgramManager;
import sh.echo.decisionmaker.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class DeleteConfirmDialogFragment extends DialogFragment {
	
	// for displaying which option is being deleted
	private String itemName;

	/**
	 * Creates an instance of DeleteConfirmDialogFragment for use.
	 * @return
	 */
	public static DeleteConfirmDialogFragment newInstance(String itemName) {
		DeleteConfirmDialogFragment fragment = new DeleteConfirmDialogFragment();
		fragment.itemName = itemName;
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// get saved arguments
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
			.setPositiveButton(R.string.yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// confirmed by user, so remove
					ProgramManager.removeProgram(itemName);
				}
			})
			.setNegativeButton(R.string.no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create();
	}
}
