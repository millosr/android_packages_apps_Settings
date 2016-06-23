/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;

import com.android.settings.R;
import com.android.settings.util.FileUtils;

import java.lang.CharSequence;

/**
 * Special preference type that allows configuration of Color settings
 */
public class DisplayColor extends DialogPreference {
    private static final String TAG = "ColorCalibration";
    private static final String COLOR_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal";
    private static final String COLOR_MODE_PROPERTY = "screen.color_isday";
    private static final String COLOR_MODE_DAY_PROPERTY = "persist.screen.color_day";
    private static final String COLOR_MODE_NIGHT_PROPERTY = "persist.screen.color_night";
    
    private static final String COLOR_MODE_DEFAULT_VALUE = "255 255 255";

    // These arrays must all match in length and order
    private static final int[] SEEKBAR_ID = new int[] {
        R.id.color_red_seekbar,
        R.id.color_green_seekbar,
        R.id.color_blue_seekbar
    };

    private static final int[] SEEKBAR_VALUE_ID = new int[] {
        R.id.color_red_value,
        R.id.color_green_value,
        R.id.color_blue_value
    };

    private Spinner mModeSpinner;
    private Spinner mPresetSpinner;
    private ColorSeekBar[] mSeekBars = new ColorSeekBar[SEEKBAR_ID.length];
    private String[] mCurrentColors;
    private String mOriginalColors;
    private boolean mCurrentEditModeIsDay;
    private String[] mPresetValues;

    public DisplayColor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mCurrentEditModeIsDay = isDayMode();
        mOriginalColors = null;

        setDialogLayoutResource(R.layout.display_color_calibration);
    }
    
    private boolean isDayMode() {
        return SystemProperties.getBoolean(COLOR_MODE_PROPERTY, true);
    }

    private String getCurColors() {
        return FileUtils.readOneLine(COLOR_FILE);
    }
    
    private String getDayColors() {
        return SystemProperties.get(COLOR_MODE_DAY_PROPERTY, COLOR_MODE_DEFAULT_VALUE);
    }
    
    private String getNightColors() {
        return SystemProperties.get(COLOR_MODE_NIGHT_PROPERTY, COLOR_MODE_DEFAULT_VALUE);
    }

    private boolean setColors(String colors) {
        return FileUtils.writeLine(COLOR_FILE, colors);
    }

    public int getMaxValue()  {
        return 255;
    }
    public int getMinValue()  {
        return 0;
    }
    public int getDefValue() {
        return getMaxValue();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setNeutralButton(R.string.urom_generic_reset,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        mPresetValues = getContext().getResources().getStringArray(R.array.color_preset_values);
        
        //Mode Spinner
        mModeSpinner = (Spinner) view.findViewById(R.id.mode_spinner);
        
        ArrayAdapter<CharSequence> adapterMode = ArrayAdapter.createFromResource(getContext(),
                                                 R.array.color_mode_entries,
                                                 android.R.layout.simple_spinner_item);
                                                 
        adapterMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mModeSpinner.setAdapter(adapterMode);
        
        //Preset Spinner
        mPresetSpinner = (Spinner) view.findViewById(R.id.preset_spinner);
        
        ArrayAdapter<CharSequence> adapterPreset = ArrayAdapter.createFromResource(getContext(),
                                                 R.array.color_preset_entries,
                                                 android.R.layout.simple_spinner_item);
                                                 
        adapterPreset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPresetSpinner.setAdapter(adapterPreset);
        
        // select mode
        mModeSpinner.setSelection(mCurrentEditModeIsDay ? 0 : 1);
        
        // Seekbar
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            TextView value = (TextView) view.findViewById(SEEKBAR_VALUE_ID[i]);
            mSeekBars[i] = new ColorSeekBar(seekBar, value, i);
        }
        
        //Spinner listener
        mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        
                mCurrentEditModeIsDay = (pos == 0);
                mOriginalColors = null;
                initUIandScreen();
            }
    
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        
        
        mPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            
                if (pos != 0) {
                    mCurrentColors = mPresetValues[pos].split(" ");
                    updateUIandScreen();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //initUIandScreen
        initUIandScreen();
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        // Can't use onPrepareDialogBuilder for this as we want the dialog
        // to be kept open on click
        AlertDialog d = (AlertDialog) getDialog();
        Button defaultsButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
        defaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentColors = COLOR_MODE_DEFAULT_VALUE.split(" ");
                updateUIandScreen();
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            SystemProperties.set(mCurrentEditModeIsDay ? COLOR_MODE_DAY_PROPERTY : COLOR_MODE_NIGHT_PROPERTY, getCurColors());
        }
        
        restoreScreen();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return superState;
        }

        // Save the dialog state
        final SavedState myState = new SavedState(superState);
        myState.currentColors = mCurrentColors;
        myState.originalColors = mOriginalColors;
        myState.currentEditModeIsDay = mCurrentEditModeIsDay ? 1 : 0;

        // Restore the old state when the activity or dialog is being paused
        restoreScreen();
        mOriginalColors = null;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mOriginalColors = myState.originalColors;
        mCurrentColors = myState.currentColors;
        mCurrentEditModeIsDay = myState.currentEditModeIsDay == 1 ? true : false;
        
        updateUIandScreen();
    }

    public void restoreScreen() {
        String value = isDayMode() ? getDayColors() : getNightColors();

        setColors(value);
    }
    
    private void initUIandScreen() {
        if(mOriginalColors == null) {
            mOriginalColors = mCurrentEditModeIsDay ? getDayColors() : getNightColors();
            mCurrentColors = mOriginalColors.split(" ");
        }
        
        changePresetSelection(mOriginalColors);
        updateUIandScreen();
    }
    
    private void updateUIandScreen() {        
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            mSeekBars[i].setCurrentValue();
        }
        
        setColors(TextUtils.join(" ", mCurrentColors));
    }
    
    private void changePresetSelection(String valueToFind) {
        for (int i = 0; i < mPresetValues.length; i++) {
            if(mPresetValues[i].contentEquals(valueToFind)) {
                mPresetSpinner.setSelection(i);
                return;
            }
        }
        
        mPresetSpinner.setSelection(0);
    }
    
    private static class SavedState extends BaseSavedState {
        String originalColors;
        String[] currentColors;
        int currentEditModeIsDay;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            originalColors = source.readString();
            currentColors = source.createStringArray();
            currentEditModeIsDay = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(originalColors);
            dest.writeStringArray(currentColors);
            dest.writeInt(currentEditModeIsDay);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private class ColorSeekBar implements SeekBar.OnSeekBarChangeListener {
        private int mIndex;
        private SeekBar mSeekBar;
        private TextView mValue;

        public ColorSeekBar(SeekBar seekBar, TextView value, int index) {
            mSeekBar = seekBar;
            mValue = value;
            mIndex = index;

            mSeekBar.setMax(getMaxValue() - getMinValue());
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void setValueFromString(String valueString) {
            mSeekBar.setProgress(Integer.valueOf(valueString));
        }
        
        public void setCurrentValue() {
            setValueFromString(mCurrentColors[mIndex]);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int min = getMinValue();
            int max = getMaxValue();

            if (fromUser) {
                mCurrentColors[mIndex] = String.valueOf(progress + min);
                String value = TextUtils.join(" ", mCurrentColors);
                changePresetSelection(value);
                setColors(value);
            }

            int percent = Math.round(100F * progress / (max - min));
            mValue.setText(String.format("%d%%", percent));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Do nothing here
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Do nothing here
        }
    }
}
