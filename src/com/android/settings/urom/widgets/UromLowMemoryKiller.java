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

import android.app.ActivityManager;
import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;

import com.android.settings.R;
import com.android.settings.urom.UromSettings;
import com.android.settings.urom.helpers.UromDialogPreference;
import com.android.settings.urom.helpers.UromSeekBar;
import com.android.settings.urom.utils.HwScreenColors;

import java.lang.NumberFormatException;

public class UromLowMemoryKiller extends UromDialogPreference
        implements UromSeekBar.Values {
    private static final String RAM_MINFREE_PROPERTY = "persist.sys.ram_minfree";

    private static final long RAM_MINFREE_DEFAULT = 138240;

    private static final long MiBtoKiB = 1024;

    private final long RAM_MINFREE_MAX;

    private UromSeekBar mSeekBar;
    private int mNewRamMinFree;
    private int mOriginalRamMinFree;

    public UromLowMemoryKiller(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.urom_settings_genericseekbar);

        /* Get device Total memory */
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(
                memInfo);

        /* Fix the ram min free max to 1/4 ot the total memory */
        RAM_MINFREE_MAX = memInfo.totalMem / MiBtoKiB / 4;

        mNewRamMinFree = mOriginalRamMinFree = getRamMinFree();

        updateSummary();
    }

    private int getRamMinFree() {
        long value = SystemProperties.getLong(RAM_MINFREE_PROPERTY, RAM_MINFREE_DEFAULT);

        if (value < 0 || value > RAM_MINFREE_MAX)
            return (int)(RAM_MINFREE_DEFAULT / MiBtoKiB);

        return (int)(value / MiBtoKiB);
    }

    private void writeRamMinFree() {
        if (mOriginalRamMinFree != mNewRamMinFree) {
            SystemProperties.set(RAM_MINFREE_PROPERTY, Long.toString(MiBtoKiB * mNewRamMinFree));
            UromSettings.rebootRequired(getActivity());
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        mSeekBar = new UromSeekBar(this, view,
                R.id.generic_seekbar,
                R.id.generic_value,
                R.id.generic_button_less,
                R.id.generic_button_more);

        super.onBindDialogView(view);
    }

    @Override
    protected void onUromDialogInit() {
        mSeekBar.setProgress(mOriginalRamMinFree);
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
        writeRamMinFree();

        /* reinit values to be sure that everything is fine before updateSummary */
        mNewRamMinFree = mOriginalRamMinFree = getRamMinFree();
    }

    @Override
    protected void onUromDialogNeutral() {
        mNewRamMinFree = (int)(RAM_MINFREE_DEFAULT / MiBtoKiB);
        mSeekBar.setProgress(mNewRamMinFree);
    }

    @Override
    protected void onUromDialogNegative() {
        mNewRamMinFree = mOriginalRamMinFree;
    }

    @Override
    protected void updateSummary() {
        if (mOriginalRamMinFree == 0)
            setSummary(getContext().getString(R.string.ram_minfree_automatic));
        else
            setSummary(getContext().getString(R.string.ram_minfree_summary, mOriginalRamMinFree));
    }

    @Override
    public int getMinValue(UromSeekBar helper) {
        return 0;
    }

    @Override
    public int getMaxValue(UromSeekBar helper) {
        return (int)(RAM_MINFREE_MAX / MiBtoKiB);
    }

    @Override
    public String getTextValue(UromSeekBar helper) {
        if (mNewRamMinFree == 0)
            return getContext().getString(R.string.ram_minfree_automatic);
        else
            return getContext().getString(R.string.ram_minfree_summary, mNewRamMinFree);
    }

    @Override
    public void onValueChanged(int value, boolean fromUser, UromSeekBar helper) {
        if (fromUser || mRestoring) {
            mNewRamMinFree = value;
        }
    }
}
