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
import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;

import com.android.settings.R;
import com.android.settings.urom.UromSettings;
import com.android.settings.urom.helpers.UromDialogPreference;
import com.android.settings.urom.helpers.UromSeekBar;
import com.android.settings.urom.utils.HwScreenColors;

import java.lang.NumberFormatException;

public class UromQSLayoutColumns extends UromDialogPreference
        implements UromSeekBar.Values {

    private static final int MIN_COLUMNS = 3;
    private static final int MAX_COLUMNS = 6;

    private static final int DEFAULT_COLUMNS = 3;

    private UromSeekBar mSeekBar;
    private int mNewQSColumns;
    private int mOriginalQSColumns;

    public UromQSLayoutColumns(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.urom_settings_genericseekbar);

        mOriginalQSColumns = mNewQSColumns = getQsColumns();
        updateSummary();
    }

    private int getQsColumns() {
        ContentResolver resolver = getContext().getContentResolver();
        return Settings.System.getInt(resolver, Settings.System.QS_LAYOUT_COLUMNS, 3);
    }

    private void writeQsColumns() {
        if (mOriginalQSColumns != mNewQSColumns) {
            ContentResolver resolver = getContext().getContentResolver();
            Settings.System.putInt(resolver, Settings.System.QS_LAYOUT_COLUMNS, mNewQSColumns);
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
        mSeekBar.setProgress(mOriginalQSColumns);
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
        writeQsColumns();
        mOriginalQSColumns = mNewQSColumns = getQsColumns();
    }

    @Override
    protected void onUromDialogNeutral() {
        mNewQSColumns = DEFAULT_COLUMNS;
        mSeekBar.setProgress(mNewQSColumns);
    }

    @Override
    protected void onUromDialogNegative() {
        mNewQSColumns = mOriginalQSColumns;
        mSeekBar.setProgress(mNewQSColumns);
    }

    @Override
    protected void updateSummary() {
        setSummary(getContext().getString(R.string.qs_columns_summary, mOriginalQSColumns));
    }

    @Override
    public int getMinValue(UromSeekBar helper) {
        return MIN_COLUMNS;
    }

    @Override
    public int getMaxValue(UromSeekBar helper) {
        return MAX_COLUMNS;
    }

    @Override
    public String getTextValue(UromSeekBar helper) {
        return getContext().getString(R.string.qs_columns_summary, mNewQSColumns);
    }

    @Override
    public void onValueChanged(int value, boolean fromUser, UromSeekBar helper) {
        if (fromUser || mRestoring) {
            mNewQSColumns = value;
        }
    }
}
