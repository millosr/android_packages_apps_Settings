package com.android.settings;

import com.android.internal.logging.MetricsLogger;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class UromAbout extends SettingsPreferenceFragment {

    public static final String TAG = "UromAbout";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.urom_about);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DISPLAY;
    }
}

