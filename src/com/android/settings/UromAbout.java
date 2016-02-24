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

    Preference mSourceUrl;
    Preference mXdaUrl;
    Preference mTranslationUrl;
    Preference mDonationUrl;
    Preference mDevs1Url;
    Preference mMentions1Url;
    Preference mMentions2Url;
    Preference mMentions3Url;
    Preference mMentions4Url;
    Preference mMentions5Url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.urom_about);

        mSourceUrl = findPreference("urom_source");
        mXdaUrl = findPreference("urom_xda");
        mTranslationUrl = findPreference("urom_translation");
        mDonationUrl = findPreference("urom_donation");
        mDevs1Url = findPreference("urom_devs_field1");
        mMentions1Url = findPreference("urom_mentions_field1");
        mMentions2Url = findPreference("urom_mentions_field2");
        mMentions3Url = findPreference("urom_mentions_field3");
        mMentions4Url = findPreference("urom_mentions_field4");
        mMentions5Url = findPreference("urom_mentions_field5");

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSourceUrl) {
            launchUrl("https://github.com/mickybart");
        } else if (preference == mXdaUrl) {
            launchUrl("http://forum.xda-developers.com/xperia-s/s-development/rom-naosprom-xperia-s-t2958516");
        } else if (preference == mTranslationUrl) {
            launchUrl("http://forum.xda-developers.com/showpost.php?p=65159347&postcount=3644");
        } else if (preference == mDonationUrl) {
            launchUrl("http://forum.xda-developers.com/donatetome.php?u=6043081");
        } else if (preference == mDevs1Url) {
            launchUrl("http://forum.xda-developers.com/member.php?u=6043081");
        } else if (preference == mMentions1Url) {
            launchUrl("http://forum.xda-developers.com/member.php?u=5064452");
        } else if (preference == mMentions2Url) {
            launchUrl("http://forum.xda-developers.com/showthread.php?t=2191223");
        } else if (preference == mMentions3Url) {
            launchUrl("http://www.cyanogenmod.com");
        } else if (preference == mMentions4Url) {
            launchUrl("http://www.teamw.in");
        } else if (preference == mMentions5Url) {
            launchUrl("http://source.android.com/");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent intentUrl = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(intentUrl);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DISPLAY;
    }
}

