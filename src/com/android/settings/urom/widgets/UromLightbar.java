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
import android.widget.RadioGroup;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.urom.UromSettings;
import com.android.settings.urom.helpers.UromDialogPreference;

/**
 * Created by aosp on 22/08/16.
 */
public class UromLightbar extends UromDialogPreference {
    private static final String LIGHTBAR_MODE_PROPERTY = "persist.sys.lightbar_mode";
    private static final String LIGHTBAR_FLASH_PROPERTY = "persist.sys.lightbar_flash";

    private static final int LIGHTBAR_MODE_DEFAULT = 1;
    private static final boolean LIGHTBAR_FLASH_DEFAULT = true;

    private int mOriginalWhenScreenOn;
    private boolean mOriginalFlashOnNotification;

    private int[] mResIdModes = new int[]{
            R.id.lightbar_radioOff_option,
            R.id.lightbar_radioOn5s_option,
            R.id.lightbar_radioOn_option
    };

    private RadioGroup mRadioGroup;
    private Switch mFlashOnNotification;

    public UromLightbar(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.urom_settings_lightbar);

        mOriginalWhenScreenOn = getLightbarMode();
        mOriginalFlashOnNotification = getLightbarFlash();

        updateSummary();
    }

    private int getLightbarMode() {
        int value = SystemProperties.getInt(LIGHTBAR_MODE_PROPERTY, LIGHTBAR_MODE_DEFAULT);
        if (value < 0 || value > 2)
            return LIGHTBAR_MODE_DEFAULT;

        return value;
    }

    private boolean getLightbarFlash() {
        int value = SystemProperties.getInt(LIGHTBAR_FLASH_PROPERTY, LIGHTBAR_FLASH_DEFAULT ? 1 : 0);
        if (value != 0)
            return true;

        return false;
    }

    private void writeLightbar(int mode, boolean flash) {
        boolean needreboot = false;

        if (mode != mOriginalWhenScreenOn) {
            SystemProperties.set(LIGHTBAR_MODE_PROPERTY, Integer.toString(mode));
            needreboot = true;
        }

        if (flash != mOriginalFlashOnNotification) {
            SystemProperties.set(LIGHTBAR_FLASH_PROPERTY, flash ? "1" : "0");
            needreboot = true;
        }

        if(needreboot)
            UromSettings.rebootRequired(getActivity());
    }

    @Override
    protected void onBindDialogView(View view) {
        mFlashOnNotification = (Switch)view.findViewById(R.id.lightbar_flash_notification);
        mRadioGroup = (RadioGroup)view.findViewById(R.id.lightbar_radiogroup);

        super.onBindDialogView(view);
    }

    @Override
    protected void onUromDialogInit() {
        mRadioGroup.check(mResIdModes[mOriginalWhenScreenOn]);
        mFlashOnNotification.setChecked(mOriginalFlashOnNotification);
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
        int mNewWhenScreenOn = LIGHTBAR_MODE_DEFAULT;
        int mode = mRadioGroup.getCheckedRadioButtonId();
        for (int i = 0; i < mResIdModes.length; i++) {
            if (mode == mResIdModes[i]) {
                mNewWhenScreenOn = i;
                break;
            }
        }

        writeLightbar(mNewWhenScreenOn, mFlashOnNotification.isChecked());

        /* reinit values to be sure that everything is fine before updateSummary */
        mOriginalWhenScreenOn = getLightbarMode();
        mOriginalFlashOnNotification = getLightbarFlash();
    }

    @Override
    protected void onUromDialogNeutral() {
        mRadioGroup.check(mResIdModes[LIGHTBAR_MODE_DEFAULT]);
        mFlashOnNotification.setChecked(LIGHTBAR_FLASH_DEFAULT);
    }

    @Override
    protected void onUromDialogNegative() {
        /* Nothing to do */
    }

    @Override
    protected void updateSummary() {
        if(mOriginalFlashOnNotification)
            setSummary(getContext().getString(R.string.lightbar_summary_with_flash,
                    getContext()
                            .getResources()
                            .getStringArray(R.array.lightbar_mode_entries)[mOriginalWhenScreenOn]));
        else
            setSummary(getContext()
                    .getResources()
                    .getStringArray(R.array.lightbar_mode_entries)[mOriginalWhenScreenOn]);
    }
}
