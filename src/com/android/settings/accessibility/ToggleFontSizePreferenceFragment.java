/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.settings.accessibility;

import android.annotation.Nullable;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.PreviewSeekBarPreferenceFragment;
import com.android.settings.R;
import java.text.DecimalFormat;

/**
 * Preference fragment used to control font size.
 */
public class ToggleFontSizePreferenceFragment extends PreviewSeekBarPreferenceFragment {
    private static final int DEFAULT_NUM_VALUES = 4;
    private static final float MEDIUM_SIZE_TRESHOLD = 0.075f;
    private static final float DEFAULT_SIZE = 1.0f;
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#0.##");

    private float[] mValues;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityLayoutResId = R.layout.font_size_activity;
        mPreviewSampleResIds = new int[]{R.layout.font_size_preview};

        Resources res = getContext().getResources();
        final ContentResolver resolver = getContext().getContentResolver();
        // Mark the appropriate item in the preferences list.
        mEntries = res.getStringArray(R.array.entries_font_size);
        String[] strEntryValues = null;
        if (mEntries.length == DEFAULT_NUM_VALUES) {
            strEntryValues = res.getStringArray(R.array.entryvalues_font_size_granular);
            if (strEntryValues.length > DEFAULT_NUM_VALUES) {
                populateValues(strEntryValues);
                createGranularEntries(strEntryValues);
            }
        }
        if (mValues == null) {
            strEntryValues = res.getStringArray(R.array.entryvalues_font_size);
            populateValues(strEntryValues);
        }

        final float currentScale =
                Settings.System.getFloat(resolver, Settings.System.FONT_SCALE, 1.0f);
        mInitialIndex = fontSizeValueToIndex(currentScale, strEntryValues);
    }

    private void populateValues(String[] strEntryValues) {
        mValues = new float[strEntryValues.length];
        for (int i = 0; i < strEntryValues.length; i++) {
            mValues[i] = Float.parseFloat(strEntryValues[i]);
        }
    }

    private void createGranularEntries(String[] strEntryValues) {
        final Resources res = getContext().getResources();
        final String[] strEntriesGranular = new String[mValues.length];
        for (int i = 0; i < mValues.length; i++) {
            strEntriesGranular[i] = getFontSizeLabel(res, mValues[i], mEntries, strEntryValues);
        }
        mEntries = strEntriesGranular;
    }

    @Override
    protected Configuration createConfig(Configuration origConfig, int index) {
        // Populate the sample layouts.
        final Configuration config = new Configuration(origConfig);
        config.fontScale = mValues[index];
        return config;
    }

    /**
     * Persists the selected font size.
     */
    @Override
    protected void commit() {
        if (getContext() == null) return;
        final ContentResolver resolver = getContext().getContentResolver();
        Settings.System.putFloat(resolver, Settings.System.FONT_SCALE, mValues[mCurrentIndex]);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ACCESSIBILITY_FONT_SIZE;
    }

    /**
     *  Utility function that returns the index in a string array with which the represented value is
     *  the closest to a given float value.
     */
    public static int fontSizeValueToIndex(float val, String[] indices) {
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }

    public static String getFontSizeLabel(Resources res, float value, String[] strEntries, String[] indices) {
        if (strEntries.length == DEFAULT_NUM_VALUES) {
            String label = null;
            final float lastValue = Float.parseFloat(indices[indices.length - 1]);
            if (value < DEFAULT_SIZE - MEDIUM_SIZE_TRESHOLD) {
                label = strEntries[0];
            } else if (value == DEFAULT_SIZE) {
                label = strEntries[1];
            } else if (value <= DEFAULT_SIZE + MEDIUM_SIZE_TRESHOLD) {
                label = res.getString(R.string.medium_font);
            } else if (value < lastValue) {
                label = strEntries[2];
            } else {
                label = strEntries[3];
            }
            return label +  " (" + PERCENT_FORMAT.format(value * 100.0) + "%)";
        } else {
            return strEntries[fontSizeValueToIndex(value, indices)];
        }
    }
}

