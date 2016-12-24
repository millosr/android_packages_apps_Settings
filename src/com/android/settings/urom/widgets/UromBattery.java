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

import android.annotation.IdRes;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;

import com.android.settings.R;
import com.android.settings.urom.helpers.UromDialogPreference;

/**
 * Created by aosp on 22/08/16.
 */
public class UromBattery extends UromDialogPreference implements RadioGroup.OnCheckedChangeListener {
    private static final int BATTERY_STYLE_DEFAULT = 0;
    private static final int BATTERY_PERCENTAGE_DEFAULT = 0;

    private static final int BATTERY_STYLE_HIDDEN = 4;
    private static final int BATTERY_STYLE_TEXT = 6;

    private int mOriginalStyle;
    private int mOriginalPercentage;

    //TODO: Rewrite to dynamically create the GUI based on resource files ONLY ??

    private int[] mResIdStyle = new int[]{
            R.id.battery_style_hidden_option,
            R.id.battery_style_portrait_option,
            R.id.battery_style_landscape_option,
            R.id.battery_style_circle_option,
            R.id.battery_style_text_option
    };

    private int[] mValuesStyle = new int[]{
            BATTERY_STYLE_HIDDEN,
            0,
            5,
            2,
            BATTERY_STYLE_TEXT
    };

    private int[] mStringIdStyle = new int[]{
            R.string.status_bar_battery_style_hidden,
            R.string.status_bar_battery_style_icon_portrait,
            R.string.status_bar_battery_style_icon_landscape,
            R.string.status_bar_battery_style_circle,
            R.string.status_bar_battery_style_text
    };

    private int[] mResIdPercentage = new int[]{
            R.id.battery_percentage_hidden_option,
            R.id.battery_percentage_inside_option,
            R.id.battery_percentage_next_option
    };

    private int[] mValuesPercentage = new int[]{
            0,
            1,
            2
    };

    private int[] mStringIdPercentage = new int[]{
            R.string.status_bar_battery_percentage_default,
            R.string.status_bar_battery_percentage_text_inside,
            R.string.status_bar_battery_percentage_text_next
    };

    private RadioGroup mStyleRadioGroup;
    private RadioGroup mPercentageRadioGroup;

    public UromBattery(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.urom_settings_battery);

        mOriginalStyle = getStyle();
        mOriginalPercentage = getPercentage();

        updateSummary();
    }

    private int findIndexOfValue(int value, int[] src) {
        for (int i = 0; i < src.length; i++) {
            if (src[i] == value)
                return i;
        }

        return -1;
    }

    private int getStyle() {
        return Settings.Secure.getInt(getContext().getContentResolver(),
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_DEFAULT);
    }

    private int getPercentage() {
        return Settings.Secure.getInt(getContext().getContentResolver(),
                Settings.Secure.STATUS_BAR_SHOW_BATTERY_PERCENT, BATTERY_PERCENTAGE_DEFAULT);
    }

    private void writeStyle(int mode) {
        Settings.Secure.putInt(getContext().getContentResolver(),
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, mode);
    }

    private void writePercentage(int mode) {
        Settings.Secure.putInt(getContext().getContentResolver(),
                Settings.Secure.STATUS_BAR_SHOW_BATTERY_PERCENT, mode);
    }

    private void updateUI(int style) {
        if (style == BATTERY_STYLE_HIDDEN ||
                style == BATTERY_STYLE_TEXT) {
            mPercentageRadioGroup.check(R.id.battery_percentage_hidden_option);
            for(int i = 0; i < mPercentageRadioGroup.getChildCount(); i++){
                mPercentageRadioGroup.getChildAt(i).setEnabled(false);
            }
        } else {
            mPercentageRadioGroup.setEnabled(true);
            for(int i = 0; i < mPercentageRadioGroup.getChildCount(); i++){
                mPercentageRadioGroup.getChildAt(i).setEnabled(true);
            }
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        mStyleRadioGroup = (RadioGroup)view.findViewById(R.id.battery_style_radiogroup);
        mPercentageRadioGroup = (RadioGroup)view.findViewById(R.id.battery_percentage_radiogroup);

        mStyleRadioGroup.setOnCheckedChangeListener(this);
        mPercentageRadioGroup.setOnCheckedChangeListener(this);

        super.onBindDialogView(view);
    }

    @Override
    protected void onUromDialogInit() {
        mStyleRadioGroup.check(mResIdStyle[findIndexOfValue(mOriginalStyle,
                mValuesStyle)]);
        mPercentageRadioGroup.check(mResIdPercentage[findIndexOfValue(mOriginalPercentage,
                mValuesPercentage)]);
    }

    @Override
    protected void onUromDialogPause() {
        /* Nothing to do */
    }

    @Override
    protected void onUromDialogResume() {
        /* Nothing to do */
    }

    private void writeCheckedStyleAndPercentage() {
        int style = mValuesStyle[findIndexOfValue(
                mStyleRadioGroup.getCheckedRadioButtonId(), mResIdStyle)];
        writeStyle(style);

        if (style != BATTERY_STYLE_HIDDEN && style != BATTERY_STYLE_TEXT) {
            int percentage = mValuesPercentage[findIndexOfValue(
                    mPercentageRadioGroup.getCheckedRadioButtonId(), mResIdPercentage)];
            writePercentage(percentage);
        }
    }

    @Override
    protected void onUromDialogPositive() {
        writeCheckedStyleAndPercentage();

        /* reinit values to be sure that everything is fine before updateSummary */
        mOriginalStyle = getStyle();
        mOriginalPercentage = getPercentage();
    }

    @Override
    protected void onUromDialogNeutral() {
        mStyleRadioGroup.check(mResIdStyle[findIndexOfValue(BATTERY_STYLE_DEFAULT,
                mValuesStyle)]);
        mPercentageRadioGroup.check(mResIdPercentage[findIndexOfValue(BATTERY_PERCENTAGE_DEFAULT,
                mValuesPercentage)]);

        writeCheckedStyleAndPercentage();
    }

    @Override
    protected void onUromDialogNegative() {
        writeStyle(mOriginalStyle);
        writePercentage(mOriginalPercentage);
    }

    @Override
    protected void updateSummary() {
        int styleResString = mStringIdStyle[findIndexOfValue(mOriginalStyle, mValuesStyle)];

        if (mOriginalStyle == BATTERY_STYLE_HIDDEN ||
                mOriginalStyle == BATTERY_STYLE_TEXT) {
            setSummary(getContext().getString(styleResString));
        } else {
            int percentageResString = mStringIdPercentage[findIndexOfValue(mOriginalPercentage,
                    mValuesPercentage)];

            setSummary(getContext().getString(R.string.status_bar_battery_summary,
                    getContext().getString(styleResString),
                    getContext().getString(percentageResString)));
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (group == mStyleRadioGroup) {
            updateUI(mValuesStyle[findIndexOfValue(
                    checkedId,
                    mResIdStyle)]);
        }
        writeCheckedStyleAndPercentage();
    }
}
