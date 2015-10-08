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

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

/*
 * Displays preferences for urom.
 */
public class UromSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {
    private static final String TAG = "UromSettings";

    //urom
    private static final String RAM_MINFREE_KEY = "ram_minfree";
    private static final String RAM_MINFREE_PROPERTY = "persist.sys.ram_minfree";
    
    private static final String ZRAM_SIZE_KEY = "zram_size";
    private static final String ZRAM_SIZE_PROPERTY = "persist.sys.zram_size";
    private static final String ZRAM_ENABLE_PROPERTY = "persist.sys.zram_enable";
    
    private static final String DOZE_BRIGHTNESS_KEY = "doze_brightness";
    private static final String DOZE_BRIGHTNESS_PROPERTY = "persist.screen.doze_brightness";
    
    private static final String LIGHTBAR_MODE_KEY = "lightbar_mode";
    private static final String LIGHTBAR_MODE_PROPERTY = "persist.sys.lightbar_mode";
    private static final String LIGHTBAR_FLASH_KEY = "lightbar_flash";
    private static final String LIGHTBAR_FLASH_PROPERTY = "persist.sys.lightbar_flash";

    private static final String MAINKEYS_LAYOUT_KEY = "mainkeys_layout";
    private static final String MAINKEYS_LAYOUT_PROPERTY = "persist.qemu.hw.mainkeys_layout";
    
    private static final String MAINKEYS_MUSIC_KEY = "mainkeys_music";
    private static final String MAINKEYS_MUSIC_PROPERTY = "persist.qemu.hw.mainkeys_music";
    
    //urom
    private ListPreference mRamMinfree;
    private ListPreference mZramSize;
    private ListPreference mDozeBrightness;
    private ListPreference mLightbarMode;
    private SwitchPreference mLightbarFlash;
    private ListPreference mMainkeysLayout;
    private SwitchPreference mMainkeysMusic;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.urom_settings);
        
        //urom
        mRamMinfree = addListPreference(RAM_MINFREE_KEY);
        mZramSize = addListPreference(ZRAM_SIZE_KEY);
        mDozeBrightness = addListPreference(DOZE_BRIGHTNESS_KEY);
        mLightbarMode = addListPreference(LIGHTBAR_MODE_KEY);
        mLightbarFlash = (SwitchPreference) findPreference(LIGHTBAR_FLASH_KEY);
        mMainkeysLayout = addListPreference(MAINKEYS_LAYOUT_KEY);
        mMainkeysMusic = (SwitchPreference) findPreference(MAINKEYS_MUSIC_KEY);
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

    private void updateAllOptions() {       
        //urom
        updateRamMinfreeOptions();
        updateZramSizeOptions();
        updateDozeBrightnessOptions();
        updateLightbarModeOptions();
        updateLightbarFlashOptions();
        updateMainkeysLayoutOptions();
        updateMainkeysMusicOptions();
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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mMainkeysMusic) {
            writeMainkeysMusicOptions();
        } else if (preference == mLightbarFlash) {
            writeLightbarFlashOptions();
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
        }
        return false;
    }
}
