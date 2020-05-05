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

import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.util.Log;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UromSettingsAbout extends SettingsPreferenceFragment {
    private static final String TAG = "UromSettingsAbout";

    private Preference mVersion;
    private Preference mBuildDate;
    private Preference mSelinuxStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.urom_about);

        mVersion = findPreference("urom_version");
        mBuildDate = findPreference("urom_build_date");
        mSelinuxStatus = findPreference("urom_selinux_status");
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY;
    }

    @Override
    public void onResume() {
        super.onResume();
        Resources res = getContext().getResources();

        mVersion.setSummary(SystemProperties.get("ro.naosp.version"));
        mBuildDate.setSummary(SystemProperties.get("ro.build.date"));
        mSelinuxStatus.setSummary(getSelinuxStatus());
    }

    public String getSelinuxStatus() {
        String status = null;
        try {
            Process p = Runtime.getRuntime().exec("getenforce");
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            status = reader.readLine().trim();
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "OS does not support getenforce", e);
        }
        return status;
    }
}

