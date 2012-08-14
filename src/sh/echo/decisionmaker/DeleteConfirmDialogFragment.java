package sh.echo.decisionmaker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class DeleteConfirmDialogFragment extends DialogFragment {

	/**
	 * Creates an instance of DeleteConfirmDialogFragment for use.
	 * @return
	 */
	public static DeleteConfirmDialogFragment newInstance(String itemName) {
		// store into bundle
		Bundle args = new Bundle();
		args.putString("itemName", itemName);
		
		// assign bundle to fragment
		DeleteConfirmDialogFragment fragment = new DeleteConfirmDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// get saved arguments
		final String itemName = getArguments().getString("itemName");
		if (itemName == null) {
			Log.e("DeleteConfirmDialogFragment", "no program name supplied");
			return null;
		}
		
		// build message string
		String message = String.format(getResources().getString(R.string.delete_confirm_dialog_message), itemName);
		
		// create dialog
		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.delete_confirm_dialog_title)
			.setMessage(message)
			.setPositiveButton(android.R.string.yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// delete
				}
			})
			.setNegativeButton(android.R.string.no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create();
	}
}
