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

package com.android.settings.urom.widgets;

import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.settings.R;
import com.android.settings.urom.UromSettings;
import com.android.settings.urom.helpers.UromDialogPreference;
import com.android.settings.urom.helpers.UromSeekBar;
import com.android.settings.urom.utils.HwScreenColors;

public class UromAmbient extends UromDialogPreference implements UromSeekBar.Values {
    private static final String DOZE_BRIGHTNESS_PROPERTY = "persist.screen.doze_brightness";

    private static final int DOZE_BRIGHTNESS_DEFAULT = 0; /* Adaptive */
    private static final int DOZE_BRIGHTNESS_MIN = 5;
    private static final int DOZE_BRIGHTNESS_MAX = HwScreenColors.LED_BRIGHTNESS_MAX / 2;

    private int mOriginalDozeBrightness;
    private int mNewDozeBrightness; /* Used for seekbar value */

    private int[] mResIdModes = new int[]{
            R.id.ambient_default_option,
            R.id.ambient_adaptive_option,
            R.id.ambient_custom_option
    };

    private RadioGroup mRadioGroup;
    private RadioButton mRadioCustom;
    private UromSeekBar mSeekBar;

    public UromAmbient(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.urom_settings_ambient);

        mOriginalDozeBrightness = getDozeBrightness();
        mNewDozeBrightness = mOriginalDozeBrightness > 0 ? mOriginalDozeBrightness : DOZE_BRIGHTNESS_MIN;

        updateSummary();
    }

    private int getDozeBrightness() {
        int value = SystemProperties.getInt(DOZE_BRIGHTNESS_PROPERTY, DOZE_BRIGHTNESS_DEFAULT);

        if (value < -1 || value > HwScreenColors.LED_BRIGHTNESS_MAX ||
                (value > 0 && value < DOZE_BRIGHTNESS_MIN) )
            return DOZE_BRIGHTNESS_DEFAULT;

        return value;
    }

    private void writeDozeBrightness() {
        if (mOriginalDozeBrightness != mNewDozeBrightness) {
            SystemProperties.set(DOZE_BRIGHTNESS_PROPERTY, Integer.toString(mNewDozeBrightness));
            UromSettings.rebootRequired(getActivity());
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        mRadioGroup = (RadioGroup)view.findViewById(R.id.ambient_radiogroup);
        mRadioCustom = (RadioButton)view.findViewById(R.id.ambient_custom_option);
        mSeekBar = new UromSeekBar(this,
                view,
                R.id.ambient_seekbar,
                R.id.ambient_custom_option);

        super.onBindDialogView(view);
    }

    @Override
    protected void onUromDialogInit() {
        if (mOriginalDozeBrightness <= 0) {
            mRadioGroup.check(mResIdModes[mOriginalDozeBrightness + 1]);
            mSeekBar.setProgress(DOZE_BRIGHTNESS_MIN);
        }
        else {
            mRadioGroup.check(mResIdModes[2]);
            mSeekBar.setProgress(mOriginalDozeBrightness);
        }
    }

    @Override
    protected void onUromDialogPause() {
        /* Nothing to do */
    }

    @Override
    protected void onUromDialogResume() {
        /* Nothing to do */
    }

    @Override
    protected void onUromDialogPositive() {
        int mode = mRadioGroup.getCheckedRadioButtonId();

        if (mode == R.id.ambient_default_option) {
            mNewDozeBrightness = -1;
        } else if (mode == R.id.ambient_adaptive_option) {
            mNewDozeBrightness = 0;
        }

        writeDozeBrightness();

        /* reinit values to be sure that everything is fine before updateSummary */
        mOriginalDozeBrightness = getDozeBrightness();
        mNewDozeBrightness = mOriginalDozeBrightness > 0 ? mOriginalDozeBrightness : DOZE_BRIGHTNESS_MIN;
    }

    @Override
    protected void onUromDialogNeutral() {
        mRadioGroup.check(mResIdModes[DOZE_BRIGHTNESS_DEFAULT + 1]);
        mNewDozeBrightness = DOZE_BRIGHTNESS_MIN;
        mSeekBar.setProgress(DOZE_BRIGHTNESS_MIN);
    }

    @Override
    protected void onUromDialogNegative() {
        mNewDozeBrightness = DOZE_BRIGHTNESS_MIN;
    }

    @Override
    protected void updateSummary() {
        if (mOriginalDozeBrightness <= 0)
            setSummary(getContext()
                    .getResources()
                    .getStringArray(R.array.doze_brightness_entries)[mOriginalDozeBrightness + 1]);
        else
            setSummary(getContext()
                    .getString(R.string.urom_math_percentage, Math.round(100f * mOriginalDozeBrightness / HwScreenColors.LED_BRIGHTNESS_MAX)));
    }

    @Override
    public int getMinValue(UromSeekBar helper) {
        return DOZE_BRIGHTNESS_MIN;
    }

    @Override
    public int getMaxValue(UromSeekBar helper) {
        /* This is doze mode so 50% is already high. No needs to go upper ! */
        return DOZE_BRIGHTNESS_MAX;
    }

    @Override
    public String getTextValue(UromSeekBar helper) {
        return getContext()
                .getString(R.string.urom_math_percentage, Math.round(100f * mNewDozeBrightness / HwScreenColors.LED_BRIGHTNESS_MAX));
    }

    @Override
    public void onValueChanged(int value, boolean fromUser, UromSeekBar helper) {
        if (fromUser || mRestoring) {
            mNewDozeBrightness = value;
            if (!mRadioCustom.isChecked())
                mRadioCustom.setChecked(true);
        }
    }
}
