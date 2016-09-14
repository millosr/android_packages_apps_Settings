/*
 * Copyright (C) 2016 nAOSProm
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

package com.android.settings.urom;

import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.settings.R;
import com.android.settings.urom.helpers.UromDialogPreference;
import com.android.settings.urom.helpers.UromSeekBar;
import com.android.settings.urom.utils.HwScreenColors;

public class UromScreenColors extends UromDialogPreference implements UromSeekBar.Values {
    private static final String COLOR_MODE_PROPERTY = "screen.color_isday";
    private static final String COLOR_MODE_DAY_PROPERTY = "persist.screen.color_day";
    private static final String COLOR_MODE_NIGHT_PROPERTY = "persist.screen.color_night";

    private static final String COLOR_MODE_DAY_DEFAULT = HwScreenColors.KCAL_DEFAULT;
    private static final String COLOR_MODE_NIGHT_DEFAULT = "255 197 143";

    /* RGB indexes for Kcal */
    private static final int RED=0;
    private static final int GREEN=1;
    private static final int BLUE=2;

    private Spinner mPresetSpinner;
    private UromSeekBar mSeekBarR;
    private UromSeekBar mSeekBarG;
    private UromSeekBar mSeekBarB;

    private boolean mDay;

    private int[] mCurrentColors;
    private String mOriginalRGB;
    private String[] mPresetValues;

    private boolean mLockUpdateUI;

    public UromScreenColors(Context context, AttributeSet attrs, boolean day) {
        super(context, attrs, R.layout.urom_settings_colorcalibration);

        mDay = day;
        mLockUpdateUI = false;
        mOriginalRGB = day ? getDayColors() : getNightColors();
        mCurrentColors = HwScreenColors.convertRGBtoInt(mOriginalRGB);

        updateSummary();
    }

    private boolean isDayMode() {
        return SystemProperties.getBoolean(COLOR_MODE_PROPERTY, true);
    }

    private static String getDayColors() {
        return SystemProperties.get(COLOR_MODE_DAY_PROPERTY, COLOR_MODE_DAY_DEFAULT);
    }

    private static String getNightColors() {
        return SystemProperties.get(COLOR_MODE_NIGHT_PROPERTY, COLOR_MODE_NIGHT_DEFAULT);
    }

    public void restoreScreen() {
        HwScreenColors.writeRGB(isDayMode() ? getDayColors() : getNightColors());
    }

    private void updateUIandScreen(boolean updatePreset) {
        mSeekBarR.setProgress(mCurrentColors[RED]);
        mSeekBarG.setProgress(mCurrentColors[GREEN]);
        mSeekBarB.setProgress(mCurrentColors[BLUE]);

        HwScreenColors.writeRGBfromInt(mCurrentColors);

        if (updatePreset)
            changePresetSelection(HwScreenColors.convertIntToRGB(mCurrentColors));
    }

    private void changePresetSelection(String valueToFind) {
        mLockUpdateUI = true;

        for (int i = 0; i < mPresetValues.length; i++) {
            if(mPresetValues[i].contentEquals(valueToFind)) {
                mPresetSpinner.setSelection(i);
                mLockUpdateUI = false;
                return;
            }
        }

        mPresetSpinner.setSelection(0);
        mLockUpdateUI = false;
    }

    @Override
    protected void onBindDialogView(View view) {
        /* Seekbars */
        mSeekBarR = new UromSeekBar(this, view,
                R.id.color_red_seekbar,
                R.id.color_red_value,
                R.id.color_red_button_less,
                R.id.color_red_button_more);
        mSeekBarG = new UromSeekBar(this, view,
                R.id.color_green_seekbar,
                R.id.color_green_value,
                R.id.color_green_button_less,
                R.id.color_green_button_more);
        mSeekBarB = new UromSeekBar(this, view,
                R.id.color_blue_seekbar,
                R.id.color_blue_value,
                R.id.color_blue_button_less,
                R.id.color_blue_button_more);

        /* Preset */
        mPresetSpinner = (Spinner)view.findViewById(R.id.preset_spinner);
        mPresetValues = getContext().getResources().getStringArray(R.array.color_preset_values);

        ArrayAdapter<CharSequence> adapterPreset = ArrayAdapter.createFromResource(getContext(),
                R.array.color_preset_entries,
                android.R.layout.simple_spinner_item);

        adapterPreset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPresetSpinner.setAdapter(adapterPreset);

        mPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (mLockUpdateUI)
                    return;

                if (pos != 0) {
                    mCurrentColors = HwScreenColors.convertRGBtoInt(mPresetValues[pos]);
                    updateUIandScreen(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        super.onBindDialogView(view);
    }

    @Override
    protected void onUromDialogInit() {
        updateUIandScreen(true);
    }

    @Override
    protected void onUromDialogPause() {
        restoreScreen();
    }

    @Override
    protected void onUromDialogResume() {
        /* Nothing to do */
    }

    @Override
    protected void onUromDialogPositive() {
        mOriginalRGB = HwScreenColors.convertIntToRGB(mCurrentColors);
        SystemProperties.set(mDay ? COLOR_MODE_DAY_PROPERTY : COLOR_MODE_NIGHT_PROPERTY, mOriginalRGB);
        restoreScreen();

        /* reinit values to be sure that everything is fine before updateSummary */
        mOriginalRGB = mDay ? getDayColors() : getNightColors();
        mCurrentColors = HwScreenColors.convertRGBtoInt(mOriginalRGB);
    }

    @Override
    protected void onUromDialogNeutral() {
        if (mDay)
            mCurrentColors = HwScreenColors.convertRGBtoInt(COLOR_MODE_DAY_DEFAULT);
        else
            mCurrentColors = HwScreenColors.convertRGBtoInt(COLOR_MODE_NIGHT_DEFAULT);

        updateUIandScreen(true);
    }

    @Override
    protected void onUromDialogNegative() {
        mCurrentColors = HwScreenColors.convertRGBtoInt(mOriginalRGB);
        restoreScreen();
    }

    @Override
    protected void updateSummary() {
        String[] presetValues = getContext().getResources().getStringArray(R.array.color_preset_values);
        String[] presetEntries = getContext().getResources().getStringArray(R.array.color_preset_entries);

        for (int i = 0; i < presetValues.length; i++) {
            if(presetValues[i].contentEquals(mOriginalRGB)) {
                setSummary(presetEntries[i]);
                return;
            }
        }

        /* Not found so we select 'custom' */
        setSummary(presetEntries[0]);
    }

    @Override
    public int getMinValue(UromSeekBar helper) {
        return HwScreenColors.KCAL_MIN;
    }

    @Override
    public int getMaxValue(UromSeekBar helper) {
        return HwScreenColors.KCAL_MAX;
    }

    @Override
    public String getTextValue(UromSeekBar helper) {
        if (helper == mSeekBarR) {
            return Integer.toString(mCurrentColors[RED]);
        } else if (helper == mSeekBarG) {
            return Integer.toString(mCurrentColors[GREEN]);
        } else {
            return Integer.toString(mCurrentColors[BLUE]);
        }
    }

    @Override
    public void onValueChanged(int value, boolean fromUser, UromSeekBar helper) {
        if (fromUser || mRestoring) {
            if (helper == mSeekBarR) {
                mCurrentColors[RED] = value;
            } else if (helper == mSeekBarG) {
                mCurrentColors[GREEN] = value;
            } else {
                mCurrentColors[BLUE] = value;
            }

            changePresetSelection(HwScreenColors.convertIntToRGB(mCurrentColors));
            HwScreenColors.writeRGBfromInt(mCurrentColors);
        }
    }
}
