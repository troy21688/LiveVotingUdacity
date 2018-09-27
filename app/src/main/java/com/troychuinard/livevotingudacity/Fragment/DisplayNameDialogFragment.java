package com.troychuinard.livevotingudacity.Fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.troychuinard.livevotingudacity.R;


public class DisplayNameDialogFragment extends DialogFragment {

    //TODO: Is this going to cause compile-time exception in these versions?
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.fragment_display_name_dialog, null);
        final EditText displayName = (EditText) mView.findViewById(R.id.display_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.DISPLAY_NAME_LABEL);
        builder.setView(mView);
        builder.setPositiveButton(R.string.dialog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String displayNameEntered = displayName.getText().toString();
                listener.onFinishEditDialog(displayNameEntered);
            }
        });

        return builder.create();
    }

    public static interface EditNameDialogListener {
        public abstract void onFinishEditDialog(String inputText);
    }

    private EditNameDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (EditNameDialogListener)getActivity();
        } catch (final ClassCastException e){
            throw new ClassCastException(getActivity().toString() + "must implement listener");
        }
    }
}
