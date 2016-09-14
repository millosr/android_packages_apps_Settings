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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.urom.utils.HwScreenColors;

public class UromSettings extends SettingsPreferenceFragment
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener,
                   OnPreferenceChangeListener {
    private static final String TAG = "UromSettings";

    //urom
    private static final String LIGHTBAR_MODE_KEY = "lightbar_mode";
    private static final String COLOR_CALIBRATION_KEY = "color_calibration";

    private static final String MAINKEYS_MUSIC_KEY = "mainkeys_music";
    private static final String MAINKEYS_MUSIC_PROPERTY = "persist.qemu.hw.mainkeys_music";
    private static final String CAMERAKEY_KEY = "camerakey";
    private static final String CAMERAKEY_PROPERTY = "persist.qemu.hw.camerakey";
    private static final String MAINKEYS_NAVBAR_KEY = "mainkeys_navbar";
    private static final String MAINKEYS_NAVBAR_PROPERTY = "persist.qemu.hw.mainkeys";

    private static final String KSM_KEY = "ksm";
    private static final String KSM_PROPERTY = "persist.ksm.enable";
    private static final String ZRAM_KEY = "zram_size";

    private static final String ALLOW_SIGNATURE_FAKE_KEY = "allow_signature_fake";
    private static final String ALLOW_SIGNATURE_FAKE_PROPERTY = "persist.sys.fake-signature";

    private static final String SENSORS_KEY = "sensors";
    private static final String AUTOPOWER_KEY = "autopower";
    public static final String AUTOPOWER_PROPERTY = "persist.sys.autopower";
 
    private static final String QS_ONEFINGER_KEY = "qs_onefinger";
    private static final String QS_ONEFINGER_PROPERTY = "persist.sys.qs_onefinger";
    private static final String STATUSBAR_DOUBLETAP_KEY = "statusbar_doubletap";
    private static final String STATUSBAR_DOUBLETAP_PROPERTY = "persist.sys.statusbar.dt2s";

    private static final String LOCKSCREEN_PHONE_KEY = "lockscreen_phone";
    private static final String LOCKSCREEN_PHONE_PROPERTY = "persist.lock.force_phone";
    private static final String DIALER_KEY = "dialer";

    //urom
    private SwitchPreference mKsm;
    private SwitchPreference mMainkeysMusic;
    private ListPreference mCamerakey;
    private SwitchPreference mMainkeysNavBar;
    private SwitchPreference mAllowSignatureFake;
    private PreferenceScreen mSensors;
    private SwitchPreference mAutoPower;
    private SwitchPreference mLockscreenPhone;
    private SwitchPreference mQsOneFinger;
    private SwitchPreference mStatusbarDt2s;
    private PreferenceScreen mDialer;

    //Dialog
    private Dialog mAllowSignatureFakeDialog;

    public static void rebootRequired(Activity activity) {
        SettingsActivity sa = null;
        if (activity instanceof SettingsActivity)
            sa = (SettingsActivity)activity;

        if (sa != null)
            sa.getRebootBar().show();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.urom_settings);
        
        //urom
        mKsm = (SwitchPreference) findPreference(KSM_KEY);
        mMainkeysMusic = (SwitchPreference) findPreference(MAINKEYS_MUSIC_KEY);
        mCamerakey = addListPreference(CAMERAKEY_KEY);
        mMainkeysNavBar = (SwitchPreference) findPreference(MAINKEYS_NAVBAR_KEY);
        mAllowSignatureFake = (SwitchPreference) findPreference(ALLOW_SIGNATURE_FAKE_KEY);
        mSensors = (PreferenceScreen) findPreference(SENSORS_KEY);
        mAutoPower = (SwitchPreference) findPreference(AUTOPOWER_KEY);
        mLockscreenPhone = (SwitchPreference) findPreference(LOCKSCREEN_PHONE_KEY);
        mQsOneFinger = (SwitchPreference) findPreference(QS_ONEFINGER_KEY);
        mStatusbarDt2s = (SwitchPreference) findPreference(STATUSBAR_DOUBLETAP_KEY);
        mDialer = (PreferenceScreen) findPreference(DIALER_KEY);

        //Dialog
        mAllowSignatureFakeDialog = null;

        //Hide not supported features
        PreferenceCategory mCategory = null;

	mCategory = (PreferenceCategory) findPreference("urom_display_category");
	if (!getResources().getBoolean(R.bool.config_urom_lightbar)) {
            mCategory.removePreference(findPreference(LIGHTBAR_MODE_KEY));
        }
	if (!HwScreenColors.isSupported()) {
	    mCategory.removePreference(findPreference(COLOR_CALIBRATION_KEY));
	}

        mCategory = (PreferenceCategory) findPreference("urom_buttons_category");
	if (!getResources().getBoolean(R.bool.config_urom_mainkeys)) {
	    mCategory.removePreference(mMainkeysNavBar);
	}
	if (!getResources().getBoolean(R.bool.config_urom_camerakey)) {
	    mCategory.removePreference(mCamerakey);
	}

        mCategory = (PreferenceCategory) findPreference("urom_memory_category");
	if (!getResources().getBoolean(R.bool.config_urom_zram)) {
	    mCategory.removePreference(findPreference(ZRAM_KEY));
	}
	if (!getResources().getBoolean(R.bool.config_urom_ksm)) {
	    mCategory.removePreference(mKsm);
	}

        mCategory = (PreferenceCategory) findPreference("urom_system_category");
	if (!getResources().getBoolean(R.bool.config_urom_autopower)) {
	    mCategory.removePreference(mAutoPower);
	}
	if (!getResources().getBoolean(R.bool.config_urom_sensors)) {
	    mCategory.removePreference(mSensors);
	}

	if (!getResources().getBoolean(R.bool.config_urom_speakerprox)) {
            mCategory = (PreferenceCategory) findPreference("urom_other_category");
	    mCategory.removePreference(mDialer);
	}
    }

    private ListPreference addListPreference(String prefKey) {
        ListPreference pref = (ListPreference) findPreference(prefKey);
        pref.setOnPreferenceChangeListener(this);
        return pref;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateAllOptions();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.DEVELOPMENT;
    }

    private void updateAllOptions() {       
        //urom
        updateKsmOptions();
        updateMainkeysMusicOptions();
        updateCamerakeyOptions();
        updateMainkeysNavBarOptions();
        updateAllowSignatureFakeOptions();
        updateSensorsOptions();
        updateAutoPowerOptions();
        updateLockscreenPhoneOptions();
        updateQsOneFingerOptions();
        updateStatusbarDt2sOptions();
	updateDialerOptions();
    }
    
    //urom
    private void updateKsmOptions() {
        mKsm.setChecked(SystemProperties.getBoolean(KSM_PROPERTY, false));
    }
    
    private void writeKsmOptions() {
        SystemProperties.set(KSM_PROPERTY, 
                mKsm.isChecked() ? "true" : "false");
        updateKsmOptions();
    }
    
    private void updateMainkeysMusicOptions() {
        mMainkeysMusic.setChecked(!SystemProperties.get(MAINKEYS_MUSIC_PROPERTY, "1").contentEquals("0"));
    }
    
    private void writeMainkeysMusicOptions() {
        SystemProperties.set(MAINKEYS_MUSIC_PROPERTY, 
                mMainkeysMusic.isChecked() ? "1" : "0");
        rebootRequired(getActivity());
        updateMainkeysMusicOptions();
    }
    
    private void updateCamerakeyOptions() {
        String value = SystemProperties.get(CAMERAKEY_PROPERTY, "1");
        int index = mCamerakey.findIndexOfValue(value);
        if (index == -1) {
            index = 1;
        }
        mCamerakey.setValueIndex(index);
        mCamerakey.setSummary(mCamerakey.getEntries()[index]);
    }
    
    private void writeCamerakeyOptions(Object newValue) {
        SystemProperties.set(CAMERAKEY_PROPERTY, newValue.toString());
        rebootRequired(getActivity());
        updateCamerakeyOptions();
    }

    private void updateMainkeysNavBarOptions() {
        mMainkeysNavBar.setChecked(!SystemProperties.get(MAINKEYS_NAVBAR_PROPERTY, "1").contentEquals("1"));
    }
    
    private void writeMainkeysNavBarOptions() {
        SystemProperties.set(MAINKEYS_NAVBAR_PROPERTY, 
                mMainkeysNavBar.isChecked() ? "0" : "1");
        rebootRequired(getActivity());
        updateMainkeysNavBarOptions();
    }

    private void updateAllowSignatureFakeOptions() {
        mAllowSignatureFake.setChecked(SystemProperties.getBoolean(ALLOW_SIGNATURE_FAKE_PROPERTY, false));
    }
    
    private void writeAllowSignatureFakeOptions(boolean value) {
        SystemProperties.set(ALLOW_SIGNATURE_FAKE_PROPERTY, 
                value ? "true" : "false");
        updateAllowSignatureFakeOptions();
    }

    private void updateSensorsOptions() {
        mSensors.setSummary(UromSettingsSensors.getSummaryPreference(getContext()));
    }

    private void updateAutoPowerOptions() {
        mAutoPower.setChecked(SystemProperties.getBoolean(AUTOPOWER_PROPERTY, true));
    }
    
    private void writeAutoPowerOptions() {
        SystemProperties.set(AUTOPOWER_PROPERTY, 
                mAutoPower.isChecked() ? "true" : "false");
        rebootRequired(getActivity());
        updateAutoPowerOptions();
    }
    
    private void updateLockscreenPhoneOptions() {
        mLockscreenPhone.setChecked(SystemProperties.getBoolean(LOCKSCREEN_PHONE_PROPERTY, false));
    }
    
    private void writeLockscreenPhoneOptions() {
        SystemProperties.set(LOCKSCREEN_PHONE_PROPERTY, 
                mLockscreenPhone.isChecked() ? "true" : "false");
        updateLockscreenPhoneOptions();
    }
    
    private void updateQsOneFingerOptions() {
        mQsOneFinger.setChecked(SystemProperties.getBoolean(QS_ONEFINGER_PROPERTY, true));
    }
    
    private void writeQsOneFingerOptions() {
        SystemProperties.set(QS_ONEFINGER_PROPERTY, 
                mQsOneFinger.isChecked() ? "true" : "false");
        rebootRequired(getActivity());
        updateQsOneFingerOptions();
    }

    private void updateStatusbarDt2sOptions() {
        mStatusbarDt2s.setChecked(SystemProperties.getBoolean(STATUSBAR_DOUBLETAP_PROPERTY, true));
    }

    private void writeStatusbarDt2sOptions() {
        SystemProperties.set(STATUSBAR_DOUBLETAP_PROPERTY,
                mStatusbarDt2s.isChecked() ? "true" : "false");
        rebootRequired(getActivity());
        updateStatusbarDt2sOptions();
    }

    private void updateDialerOptions() {
        mDialer.setSummary(UromSettingsDialer.getSummaryPreference(getContext()));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mMainkeysMusic) {
            writeMainkeysMusicOptions();
        } else if (preference == mMainkeysNavBar) {
            writeMainkeysNavBarOptions();
        } else if (preference == mKsm) {
            writeKsmOptions();
        } else if (preference == mAllowSignatureFake) {
            if (mAllowSignatureFake.isChecked()) {
                if (mAllowSignatureFakeDialog != null) {
                    dismissDialogs();
                }
                mAllowSignatureFakeDialog = new AlertDialog.Builder(getActivity()).setMessage(
                        getResources().getString(R.string.allow_signature_fake_warning))
                        .setTitle(R.string.allow_signature_fake)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
                mAllowSignatureFakeDialog.setOnDismissListener(this);
            } else {
                writeAllowSignatureFakeOptions(false);
            }
        } else if (preference == mAutoPower) {
            writeAutoPowerOptions();
        } else if (preference == mLockscreenPhone) {
            writeLockscreenPhoneOptions();
        } else if (preference == mQsOneFinger) {
            writeQsOneFingerOptions();
        } else if (preference == mStatusbarDt2s) {
            writeStatusbarDt2sOptions();
        } else {
            return super.onPreferenceTreeClick(preference);
        }
        
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCamerakey) {
            writeCamerakeyOptions(newValue);
            return true;
        }
        return false;
    }

    private void dismissDialogs() {
        if (mAllowSignatureFakeDialog != null) {
            mAllowSignatureFakeDialog.dismiss();
            mAllowSignatureFakeDialog = null;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mAllowSignatureFakeDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                writeAllowSignatureFakeOptions(true);
            } else {
                // Reset the toggle
                mAllowSignatureFake.setChecked(false);
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (dialog == mAllowSignatureFakeDialog) {
            updateAllowSignatureFakeOptions();
            mAllowSignatureFakeDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        dismissDialogs();
        super.onDestroy();
    } 
}
