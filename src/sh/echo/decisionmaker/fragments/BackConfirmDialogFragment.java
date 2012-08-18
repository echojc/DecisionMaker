package sh.echo.decisionmaker.fragments;

import sh.echo.decisionmaker.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class BackConfirmDialogFragment extends DialogFragment {
	
	/**
	 * Creates an instance of AboutDialogFragment for use.
	 * @return
	 */
	public static BackConfirmDialogFragment newInstance() {
		return new BackConfirmDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// create dialog
		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.back_confirm_dialog_title)
			.setMessage(R.string.back_confirm_dialog_message)
			.setPositiveButton(R.string.yes, (OnClickListener)getActivity())
			.setNegativeButton(R.string.no, (OnClickListener)getActivity())
			.create();
	}
}
