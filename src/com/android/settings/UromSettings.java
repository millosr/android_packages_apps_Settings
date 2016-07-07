/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.android.internal.logging.MetricsLogger;

/*
 * Displays preferences for urom.
 */
public class UromSettings extends SettingsPreferenceFragment
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener,
                   OnPreferenceChangeListener {
    private static final String TAG = "UromSettings";

    //urom
    private static final String RAM_MINFREE_KEY = "ram_minfree";
    private static final String RAM_MINFREE_PROPERTY = "persist.sys.ram_minfree";
    
    private static final String ZRAM_SIZE_KEY = "zram_size";
    private static final String ZRAM_SIZE_PROPERTY = "persist.sys.zram_size";
    private static final String ZRAM_ENABLE_PROPERTY = "persist.sys.zram_enable";
    private static final String KSM_KEY = "ksm";
    private static final String KSM_PROPERTY = "persist.ksm.enable";
    
    private static final String DOZE_BRIGHTNESS_KEY = "doze_brightness";
    private static final String DOZE_BRIGHTNESS_PROPERTY = "persist.screen.doze_brightness";
    private static final String DOZE_INVERT_KEY = "doze_invert";
    private static final String DOZE_INVERT_PROPERTY = "persist.screen.doze_invert";
    
    private static final String LIGHTBAR_MODE_KEY = "lightbar_mode";
    private static final String LIGHTBAR_MODE_PROPERTY = "persist.sys.lightbar_mode";
    private static final String LIGHTBAR_FLASH_KEY = "lightbar_flash";
    private static final String LIGHTBAR_FLASH_PROPERTY = "persist.sys.lightbar_flash";

    private static final String MAINKEYS_LAYOUT_KEY = "mainkeys_layout";
    private static final String MAINKEYS_LAYOUT_PROPERTY = "persist.qemu.hw.mainkeys_layout";
    private static final String MAINKEYS_MUSIC_KEY = "mainkeys_music";
    private static final String MAINKEYS_MUSIC_PROPERTY = "persist.qemu.hw.mainkeys_music";
    private static final String MAINKEYS_NAVBAR_KEY = "mainkeys_navbar";
    private static final String MAINKEYS_NAVBAR_PROPERTY = "persist.qemu.hw.mainkeys";

    private static final String ALLOW_SIGNATURE_FAKE_KEY = "allow_signature_fake";
    private static final String ALLOW_SIGNATURE_FAKE_PROPERTY = "persist.sys.fake-signature";
    
    private static final String AUTOPOWER_KEY = "autopower";
    private static final String AUTOPOWER_PROPERTY = "persist.sys.autopower";
    
    private static final String LOCKSCREEN_PHONE_KEY = "lockscreen_phone";
    private static final String LOCKSCREEN_PHONE_PROPERTY = "persist.lock.force_phone";
    
    private static final String QS_ONEFINGER_KEY = "qs_onefinger";
    private static final String QS_ONEFINGER_PROPERTY = "persist.sys.qs_onefinger";
    
    private static final String STATUSBAR_DOUBLETAP_KEY = "statusbar_doubletap";
    private static final String STATUSBAR_DOUBLETAP_PROPERTY = "persist.sys.statusbar.dt2s";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private static final String UROM_DISPLAY_CATEGORY = "urom_display_category";
    private static final String UROM_BUTTONS_CATEGORY = "urom_buttons_category";
    private static final String UROM_MEMORY_CATEGORY = "urom_memory_category";
    private static final String UROM_OTHER_CATEGORY = "urom_other_category";

    //urom
    private ListPreference mRamMinfree;
    private ListPreference mZramSize;
    private SwitchPreference mKsm;
    private ListPreference mDozeBrightness;
    private SwitchPreference mDozeInvert;
    private ListPreference mLightbarMode;
    private SwitchPreference mLightbarFlash;
    private ListPreference mMainkeysLayout;
    private SwitchPreference mMainkeysMusic;
    private SwitchPreference mMainkeysNavBar;
    private SwitchPreference mAllowSignatureFake;
    private SwitchPreference mAutoPower;
    private SwitchPreference mLockscreenPhone;
    private SwitchPreference mQsOneFinger;
    private SwitchPreference mStatusbarDt2s;
    private ListPreference mStatusBarBatteryShowPercent;

    //Dialog
    private Dialog mAllowSignatureFakeDialog;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.urom_settings);
        
        //urom
        mRamMinfree = addListPreference(RAM_MINFREE_KEY);
        mZramSize = addListPreference(ZRAM_SIZE_KEY);
        mKsm = (SwitchPreference) findPreference(KSM_KEY);
        mDozeBrightness = addListPreference(DOZE_BRIGHTNESS_KEY);
        mDozeInvert = (SwitchPreference) findPreference(DOZE_INVERT_KEY);
        mLightbarMode = addListPreference(LIGHTBAR_MODE_KEY);
        mLightbarFlash = (SwitchPreference) findPreference(LIGHTBAR_FLASH_KEY);
        mMainkeysLayout = addListPreference(MAINKEYS_LAYOUT_KEY);
        mMainkeysMusic = (SwitchPreference) findPreference(MAINKEYS_MUSIC_KEY);
        mMainkeysNavBar = (SwitchPreference) findPreference(MAINKEYS_NAVBAR_KEY);
        mAllowSignatureFake = (SwitchPreference) findPreference(ALLOW_SIGNATURE_FAKE_KEY);
        mAutoPower = (SwitchPreference) findPreference(AUTOPOWER_KEY);
        mLockscreenPhone = (SwitchPreference) findPreference(LOCKSCREEN_PHONE_KEY);
        mQsOneFinger = (SwitchPreference) findPreference(QS_ONEFINGER_KEY);
        mStatusbarDt2s = (SwitchPreference) findPreference(STATUSBAR_DOUBLETAP_KEY);
        mStatusBarBatteryShowPercent = addListPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);

        //Dialog
        mAllowSignatureFakeDialog = null;

        //Hide not supported features
        if (! SystemProperties.get("ro.product.device","").equals("nozomi")) {
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removePreference(findPreference(UROM_DISPLAY_CATEGORY));
            preferenceScreen.removePreference(findPreference(UROM_BUTTONS_CATEGORY));
            preferenceScreen.removePreference(findPreference(UROM_OTHER_CATEGORY));

            PreferenceCategory memoryCategory = (PreferenceCategory) findPreference(UROM_MEMORY_CATEGORY);
            memoryCategory.removePreference(mKsm);
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
        return MetricsLogger.DEVELOPMENT;
    }

    private void updateAllOptions() {       
        //urom
        updateRamMinfreeOptions();
        updateZramSizeOptions();
        updateKsmOptions();
        updateDozeBrightnessOptions();
        updateDozeInvertOptions();
        updateLightbarModeOptions();
        updateLightbarFlashOptions();
        updateMainkeysLayoutOptions();
        updateMainkeysMusicOptions();
        updateMainkeysNavBarOptions();
        updateAllowSignatureFakeOptions();
        updateAutoPowerOptions();
        updateLockscreenPhoneOptions();
        updateQsOneFingerOptions();
        updateStatusbarDt2sOptions();
    }
    
    //urom
    private void updateRamMinfreeOptions() {
        String value = SystemProperties.get(RAM_MINFREE_PROPERTY, "-1");
        int index = mRamMinfree.findIndexOfValue(value);
        if (index == -1) {
            index = mRamMinfree.getEntryValues().length - 1;
        }
        mRamMinfree.setValueIndex(index);
        mRamMinfree.setSummary(mRamMinfree.getEntries()[index]);
    }

    private void writeRamMinfreeOptions(Object newValue) {
        if (newValue.toString().contentEquals("-2")) {
            // custom
            return;
        }
        
        SystemProperties.set(RAM_MINFREE_PROPERTY, newValue.toString());
        updateRamMinfreeOptions();
    }

    private void updateZramSizeOptions() {
        String value = SystemProperties.get(ZRAM_SIZE_PROPERTY, "0");
        int index = mZramSize.findIndexOfValue(value);
        if (index == -1) {
            index = mZramSize.getEntryValues().length - 1;
        }
        mZramSize.setValueIndex(index);
        mZramSize.setSummary(mZramSize.getEntries()[index]);
    }

    private void writeZramSizeOptions(Object newValue) {
        String value = newValue.toString();
    
        if (value.contentEquals("-2")) {
            // custom
            return;
        }
        
        SystemProperties.set(ZRAM_SIZE_PROPERTY, value);
        
        if (value.contentEquals("0")) {
            SystemProperties.set(ZRAM_ENABLE_PROPERTY, "false");
        } else {
            SystemProperties.set(ZRAM_ENABLE_PROPERTY, "true");
        }
        
        updateZramSizeOptions();
    }

    private void updateKsmOptions() {
        mKsm.setChecked(SystemProperties.getBoolean(KSM_PROPERTY, false));
    }
    
    private void writeKsmOptions() {
        SystemProperties.set(KSM_PROPERTY, 
                mKsm.isChecked() ? "true" : "false");
        updateKsmOptions();
    }
    
    private void updateDozeBrightnessOptions() {
        String value = SystemProperties.get(DOZE_BRIGHTNESS_PROPERTY, "-1");
        int index = mDozeBrightness.findIndexOfValue(value);
        if (index == -1) {
            index = mDozeBrightness.getEntryValues().length - 1;
        }
        mDozeBrightness.setValueIndex(index);
        mDozeBrightness.setSummary((mDozeBrightness.getEntries()[index]).toString().replace("%","%%"));
    }

    private void writeDozeBrightnessOptions(Object newValue) {
        if (newValue.toString().contentEquals("-2")) {
            // custom
            return;
        }
        
        SystemProperties.set(DOZE_BRIGHTNESS_PROPERTY, newValue.toString());
        updateDozeBrightnessOptions();
    }
    
    private void updateDozeInvertOptions() {
        mDozeInvert.setChecked(SystemProperties.getBoolean(DOZE_INVERT_PROPERTY, true));
    }
    
    private void writeDozeInvertOptions() {
        SystemProperties.set(DOZE_INVERT_PROPERTY, 
                mDozeInvert.isChecked() ? "true" : "false");
        updateDozeInvertOptions();
    }
    
    private void updateLightbarModeOptions() {
        String value = SystemProperties.get(LIGHTBAR_MODE_PROPERTY, "1");
        int index = mLightbarMode.findIndexOfValue(value);
        if (index == -1) {
            index = 1;
        }
        mLightbarMode.setValueIndex(index);
        mLightbarMode.setSummary(mLightbarMode.getEntries()[index]);
    }
    
    private void writeLightbarModeOptions(Object newValue) {
        SystemProperties.set(LIGHTBAR_MODE_PROPERTY, newValue.toString());
        updateLightbarModeOptions();
    }
    
    private void updateLightbarFlashOptions() {
        mLightbarFlash.setChecked(!SystemProperties.get(LIGHTBAR_FLASH_PROPERTY, "1").contentEquals("0"));
    }
    
    private void writeLightbarFlashOptions() {
        SystemProperties.set(LIGHTBAR_FLASH_PROPERTY, 
                mLightbarFlash.isChecked() ? "1" : "0");
        updateLightbarFlashOptions();
    }
    
    private void updateMainkeysLayoutOptions() {
        String value = SystemProperties.get(MAINKEYS_LAYOUT_PROPERTY, "1");
        int index = mMainkeysLayout.findIndexOfValue(value);
        if (index == -1) {
            index = 1;
        }
        mMainkeysLayout.setValueIndex(index);
        mMainkeysLayout.setSummary(mMainkeysLayout.getEntries()[index]);
    }
    
    private void writeMainkeysLayoutOptions(Object newValue) {
        SystemProperties.set(MAINKEYS_LAYOUT_PROPERTY, newValue.toString());
        updateMainkeysLayoutOptions();
    }
    
    private void updateMainkeysMusicOptions() {
        mMainkeysMusic.setChecked(!SystemProperties.get(MAINKEYS_MUSIC_PROPERTY, "1").contentEquals("0"));
    }
    
    private void writeMainkeysMusicOptions() {
        SystemProperties.set(MAINKEYS_MUSIC_PROPERTY, 
                mMainkeysMusic.isChecked() ? "1" : "0");
        updateMainkeysMusicOptions();
    }

    private void updateMainkeysNavBarOptions() {
        mMainkeysNavBar.setChecked(!SystemProperties.get(MAINKEYS_NAVBAR_PROPERTY, "1").contentEquals("1"));
    }
    
    private void writeMainkeysNavBarOptions() {
        SystemProperties.set(MAINKEYS_NAVBAR_PROPERTY, 
                mMainkeysNavBar.isChecked() ? "0" : "1");
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

    private void updateAutoPowerOptions() {
        mAutoPower.setChecked(SystemProperties.getBoolean(AUTOPOWER_PROPERTY, true));
    }
    
    private void writeAutoPowerOptions() {
        SystemProperties.set(AUTOPOWER_PROPERTY, 
                mAutoPower.isChecked() ? "true" : "false");
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
        updateQsOneFingerOptions();
    }

    private void updateStatusbarDt2sOptions() {
        mStatusbarDt2s.setChecked(SystemProperties.getBoolean(STATUSBAR_DOUBLETAP_PROPERTY, true));
    }

    private void writeStatusbarDt2sOptions() {
        SystemProperties.set(STATUSBAR_DOUBLETAP_PROPERTY,
                mStatusbarDt2s.isChecked() ? "true" : "false");
        updateStatusbarDt2sOptions();
        updateStatusBarBatteryShowPercent();
    }

    private void updateStatusBarBatteryShowPercent() {
        int batteryShowPercent = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
    }

    private void writeStatusBarBatteryShowPercent(Object newValue) {
        int batteryShowPercent = Integer.valueOf((String) newValue);
        int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntries()[index]);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mMainkeysMusic) {
            writeMainkeysMusicOptions();
        } else if (preference == mMainkeysNavBar) {
            writeMainkeysNavBarOptions();
        } else if (preference == mLightbarFlash) {
            writeLightbarFlashOptions();
        } else if (preference == mDozeInvert) {
            writeDozeInvertOptions();
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
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRamMinfree) {
            writeRamMinfreeOptions(newValue);
            return true;
        } else if (preference == mZramSize) {
            writeZramSizeOptions(newValue);
            return true;
        } else if (preference == mDozeBrightness) {
            writeDozeBrightnessOptions(newValue);
            return true;
        } else if (preference == mLightbarMode) {
            writeLightbarModeOptions(newValue);
            return true;
        } else if (preference == mMainkeysLayout) {
            writeMainkeysLayoutOptions(newValue);
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            writeStatusBarBatteryShowPercent(newValue);
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
