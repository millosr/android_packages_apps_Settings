package com.android.settings.urom.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.android.settings.CustomDialogPreference;
import com.android.settings.R;

public abstract class UromDialogPreference extends CustomDialogPreference {
    protected boolean mRestoring;

    public UromDialogPreference(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);

        mRestoring = false;

        setDialogLayoutResource(layoutId);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener listener) {
	super.onPrepareDialogBuilder(builder, listener);

        builder.setNeutralButton(R.string.urom_generic_reset,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onUromDialogNegative();
                    }
                });
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        /*
         * Only init the view if we are not restoring a state
         */
        if (!mRestoring) {
            onUromDialogInit();
        }
    }

    @Override
    protected void onShowDialog() {
        /*
         * Do not dismiss on neutral click
         */
        AlertDialog d = (AlertDialog) getDialog();
        Button defaultsButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
        defaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUromDialogNeutral();
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            onUromDialogPositive();
            updateSummary();
            callChangeListener(null);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        /*
         * Only run some actions if the dialog view is visible
         */
        if (getDialog() != null && getDialog().isShowing()) {
            onUromDialogPause();
        }

        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        /*
         * Restore everything. Notify restoration in progress
         * with mRestoring mainly for onBindDialogView
         */
        mRestoring = true;

        onUromDialogResume();

        super.onRestoreInstanceState(state);

	mRestoring = false;
    }

    /* Call during dialog binding if we are not restoring it */
    protected abstract void onUromDialogInit();

    /* Call during pause/save of the Activity */
    protected abstract void onUromDialogPause();

    /* Call during restore of the Activity */
    protected abstract void onUromDialogResume();

    /* Call if the user pressed ok button */
    protected abstract void onUromDialogPositive();

    /* Call if the user pressed reset button */
    protected abstract void onUromDialogNeutral();

    /* Call if the user pressed cancel button */
    protected abstract void onUromDialogNegative();

    /* Display a summary on the preference view */
    protected abstract void updateSummary();
}
