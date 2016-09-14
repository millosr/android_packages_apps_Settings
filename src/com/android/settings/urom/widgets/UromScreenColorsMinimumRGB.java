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

import com.android.settings.R;
import com.android.settings.urom.helpers.UromDialogPreference;
import com.android.settings.urom.helpers.UromSeekBar;
import com.android.settings.urom.utils.HwScreenColors;

import java.lang.NumberFormatException;

public class UromScreenColorsMinimumRGB extends UromDialogPreference
        implements UromSeekBar.Values {
    private static final String COLOR_MIN_PROPERTY = "persist.screen.color_min";

    private UromSeekBar mSeekBar;
    private int mCurrentMinimum;
    private int mOriginalMinimum;

    public UromScreenColorsMinimumRGB(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.urom_settings_minimumrgb);

        mCurrentMinimum = mOriginalMinimum = getMinimumRGB();

        updateSummary();
    }

    private static int getMinimumRGB() {
        String value = SystemProperties.get(COLOR_MIN_PROPERTY, "");

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            
        }

        return HwScreenColors.KCAL_MINIMUM_DEFAULT;
    }

    private void restoreScreen() {
        HwScreenColors.writeMinimum(mOriginalMinimum);
    }

    private void updateUIandScreen() {
        mSeekBar.setProgress(mCurrentMinimum);
        HwScreenColors.writeMinimum(mCurrentMinimum);
    }

    @Override
    protected void onBindDialogView(View view) {
        mSeekBar = new UromSeekBar(this, view,
                R.id.minimum_rgb_seekbar,
                R.id.minimum_rgb_value,
                R.id.minimum_rgb_button_less,
                R.id.minimum_rgb_button_more);

        super.onBindDialogView(view);
    }

    @Override
    protected void onUromDialogInit() {
        updateUIandScreen();
    }

    @Override
    protected void onUromDialogPause() {
        restoreScreen();
    }

    @Override
    protected void onUromDialogResume() {
        //Nothing to do
    }

    @Override
    protected void onUromDialogPositive() {
        if (mCurrentMinimum != mOriginalMinimum)
            SystemProperties.set(COLOR_MIN_PROPERTY, Integer.toString(mCurrentMinimum));

        /* reinit values to be sure that everything is fine before updateSummary */
        mCurrentMinimum = mOriginalMinimum = getMinimumRGB();
    }

    @Override
    protected void onUromDialogNeutral() {
        mCurrentMinimum = HwScreenColors.KCAL_MINIMUM_DEFAULT;
        updateUIandScreen();
    }

    @Override
    protected void onUromDialogNegative() {
        mCurrentMinimum = mOriginalMinimum;
        restoreScreen();
    }

    @Override
    protected void updateSummary() {
        setSummary(Integer.toString(mOriginalMinimum));
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
        return Integer.toString(mCurrentMinimum);
    }

    @Override
    public void onValueChanged(int value, boolean fromUser, UromSeekBar helper) {
        if (fromUser || mRestoring) {
            mCurrentMinimum = value;
            HwScreenColors.writeMinimum(mCurrentMinimum);
        }
    }
}
