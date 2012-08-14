package sh.echo.decisionmaker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialogFragment extends DialogFragment {
	
	/**
	 * Creates an instance of AboutDialogFragment for use.
	 * @return
	 */
	public static AboutDialogFragment newInstance() {
		return new AboutDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// turn url into a link
		SpannableString message = new SpannableString(getResources().getString(R.string.about_dialog_message));
		Linkify.addLinks(message, Linkify.WEB_URLS);
		
		// create dialog
		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.app_name)
			.setMessage(message)
			.setNeutralButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// set link to be clickable
		((TextView)getDialog().findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
}
