/*
 * Copyright (C) 2016 The Pure Nexus Project
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
 * limitations under the License
 */

package com.android.settings.urom;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.util.Arrays;

public class UromSettingsDialer extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PROXIMITY_AUTO_SPEAKER  = "proximity_auto_speaker";
    private static final String PROXIMITY_AUTO_SPEAKER_DELAY  = "proximity_auto_speaker_delay";
    private static final String PROXIMITY_AUTO_SPEAKER_INCALL_ONLY  = "proximity_auto_speaker_incall_only";

    private SwitchPreference mProxSpeaker;
    private ListPreference mProxSpeakerDelay;
    private SwitchPreference mProxSpeakerIncallOnly;

    public static String getSummaryPreference(Context context) {
        final ContentResolver resolver = context.getContentResolver();

        boolean isAuto = (Settings.System.getInt(resolver,
                Settings.System.PROXIMITY_AUTO_SPEAKER, 0) == 1);

	if (isAuto)
	    return context.getString(R.string.prox_auto_speaker_title);
	else
	    return context.getString(R.string.prox_auto_speaker_manual_title);
	
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.urom_settings_dialer);

        mProxSpeaker = (SwitchPreference) findPreference(PROXIMITY_AUTO_SPEAKER);
        mProxSpeaker.setOnPreferenceChangeListener(this);

        mProxSpeakerDelay = (ListPreference) findPreference(PROXIMITY_AUTO_SPEAKER_DELAY);
        mProxSpeakerDelay.setOnPreferenceChangeListener(this);

        mProxSpeakerIncallOnly = (SwitchPreference) findPreference(PROXIMITY_AUTO_SPEAKER_INCALL_ONLY);
        mProxSpeakerIncallOnly.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
	super.onResume();

	updateAllOptions();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mProxSpeaker) {
            Settings.System.putInt(resolver, Settings.System.PROXIMITY_AUTO_SPEAKER,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mProxSpeakerDelay) {
            int proxDelay = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.PROXIMITY_AUTO_SPEAKER_DELAY, proxDelay);
            updateProximityDelaySummary(proxDelay);
            return true;
        } else if (preference == mProxSpeakerIncallOnly) {
            Settings.System.putInt(resolver, Settings.System.PROXIMITY_AUTO_SPEAKER_INCALL_ONLY,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
	return MetricsEvent.DEVELOPMENT;
    }

    private void updateAllOptions() {
        final ContentResolver resolver = getActivity().getContentResolver();

        mProxSpeaker.setChecked(Settings.System.getInt(resolver,
                Settings.System.PROXIMITY_AUTO_SPEAKER, 0) == 1);

        int proxDelay = Settings.System.getInt(resolver,
                Settings.System.PROXIMITY_AUTO_SPEAKER_DELAY, 100);
        mProxSpeakerDelay.setValue(String.valueOf(proxDelay));
        updateProximityDelaySummary(proxDelay);

        mProxSpeakerIncallOnly.setChecked(Settings.System.getInt(resolver,
                Settings.System.PROXIMITY_AUTO_SPEAKER_INCALL_ONLY, 0) == 1);
    }

    private void updateProximityDelaySummary(int value) {
        String summary = getResources().getString(R.string.prox_auto_speaker_delay_summary, value);
        mProxSpeakerDelay.setSummary(summary);
    }
}
