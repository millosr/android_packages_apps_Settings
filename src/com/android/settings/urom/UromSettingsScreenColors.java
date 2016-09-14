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

import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.widget.Switch;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.urom.utils.HwScreenColors;
import com.android.settings.widget.SwitchBar;

public class UromSettingsScreenColors extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, SwitchBar.OnSwitchChangeListener {

    private static final String SCREEN_COLOR_DAY_KEY = "screen_color_day";
    private static final String SCREEN_COLOR_NIGHT_KEY = "screen_color_night";
    private static final String SCREEN_COLOR_MINIMUM_RGB_KEY = "screen_color_minimum_rgb";

    private static final String COLOR_ENABLE_PROPERTY = "persist.screen.color_enable";

    private Preference mColorDay;
    private Preference mColorNight;
    private Preference mColorMinimumRGB;

    private SwitchBar mSwitchBar;
    private boolean mRefreshing;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DEVELOPMENT;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.urom_settings_screencolors);

        mColorDay = findPreference(SCREEN_COLOR_DAY_KEY);
        mColorNight = findPreference(SCREEN_COLOR_NIGHT_KEY);
        mColorMinimumRGB = findPreference(SCREEN_COLOR_MINIMUM_RGB_KEY);

        mRefreshing = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mSwitchBar.removeOnSwitchChangeListener(this);
        mSwitchBar.hide();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SettingsActivity sa = (SettingsActivity) getActivity();
        mSwitchBar = sa.getSwitchBar();
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.show();
    }

    private Preference addPreference(String prefKey) {
        Preference pref = findPreference(prefKey);
        pref.setOnPreferenceChangeListener(this);
        return pref;
    }

    private void updateAllOptions() {
        //Switch bar
        updateStatusBarOptions();
    }

    private void updateStatusBarOptions() {
        mRefreshing = true;

        boolean enable = SystemProperties.getBoolean(COLOR_ENABLE_PROPERTY, HwScreenColors.KCAL_ENABLE_DEFAULT);

        if (mSwitchBar.isChecked() != enable)
            mSwitchBar.setChecked(enable);

        mColorDay.setEnabled(enable);
        mColorNight.setEnabled(enable);
        mColorMinimumRGB.setEnabled(enable);

        mRefreshing = false;
    }

    private void writeStatusBarOptions(boolean enable) {
        if (enable != HwScreenColors.isEnable() && HwScreenColors.setEnable(enable))
            SystemProperties.set(COLOR_ENABLE_PROPERTY, enable ? "true" : "false");

        updateStatusBarOptions();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateAllOptions();
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (!mRefreshing)
            writeStatusBarOptions(isChecked);
    }
}
