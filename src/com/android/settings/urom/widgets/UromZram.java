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

public class UromZram extends UromDialogPreference
        implements UromSeekBar.Values {
    private static final String ZRAM_SIZE_PROPERTY = "persist.sys.zram_size";
    private static final String ZRAM_ENABLE_PROPERTY = "persist.sys.zram_enable";

    private static final boolean ZRAM_ENABLE_DEFAULT = true;
    private static final long ZRAM_SIZE_DEFAULT = 167772160;

    private static final long MiBtoB = 1024*1024;

    private final long ZRAM_SIZE_MAX;

    private UromSeekBar mSeekBar;
    private int mNewSwap;
    private int mOriginalSwap;

    public UromZram(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.urom_settings_genericseekbar);

        /* Get device Total memory */
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(
                memInfo);

        /* Fix the zram max size to half ot the total memory */
        ZRAM_SIZE_MAX = memInfo.totalMem / 2;

        mNewSwap = mOriginalSwap = getZramSize();

        updateSummary();
    }

    private int getZramSize() {
        boolean enable = SystemProperties.getBoolean(ZRAM_ENABLE_PROPERTY, ZRAM_ENABLE_DEFAULT);

        if (!enable)
            return 0;

        long value = SystemProperties.getLong(ZRAM_SIZE_PROPERTY, ZRAM_SIZE_DEFAULT);

        if (value < 0 || value > ZRAM_SIZE_MAX)
            return (int)(ZRAM_SIZE_DEFAULT / MiBtoB);

        return (int)(value / MiBtoB);
    }

    private void writeZramSize() {
        if (mNewSwap != mOriginalSwap) {
            if (mNewSwap == 0) {
                SystemProperties.set(ZRAM_SIZE_PROPERTY, "0");
                SystemProperties.set(ZRAM_ENABLE_PROPERTY, "false");
            } else {
                SystemProperties.set(ZRAM_SIZE_PROPERTY, Long.toString(MiBtoB * mNewSwap));
                SystemProperties.set(ZRAM_ENABLE_PROPERTY, "true");
            }

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
        mSeekBar.setProgress(mOriginalSwap);
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
        writeZramSize();

        /* reinit values to be sure that everything is fine before updateSummary */
        mNewSwap = mOriginalSwap = getZramSize();
    }

    @Override
    protected void onUromDialogNeutral() {
        mNewSwap = (int)(ZRAM_SIZE_DEFAULT / MiBtoB);
        mSeekBar.setProgress(mNewSwap);
    }

    @Override
    protected void onUromDialogNegative() {
        mNewSwap = mOriginalSwap;
    }

    @Override
    protected void updateSummary() {
        if (mOriginalSwap == 0)
            setSummary(getContext().getString(R.string.urom_generic_disabled));
        else
            setSummary(getContext().getString(R.string.zram_size_summary, mOriginalSwap));
    }

    @Override
    public int getMinValue(UromSeekBar helper) {
        return 0;
    }

    @Override
    public int getMaxValue(UromSeekBar helper) {
        return (int)(ZRAM_SIZE_MAX / MiBtoB);
    }

    @Override
    public String getTextValue(UromSeekBar helper) {
        if (mNewSwap == 0)
            return getContext().getString(R.string.urom_generic_disabled);
        else
            return getContext().getString(R.string.zram_size_summary, mNewSwap);
    }

    @Override
    public void onValueChanged(int value, boolean fromUser, UromSeekBar helper) {
        if (fromUser || mRestoring) {
            mNewSwap = value;
        }
    }
}
