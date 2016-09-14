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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class UromSettingsSensors extends SettingsPreferenceFragment {
    private static final int SENSORS_PICKUP_DEFAULT = 0;
    private static final int SENSORS_SIGNIFICANT_DEFAULT = 0;
    private static final int SENSORS_ACCEL_DEFAULT = 1;
    private static final String SUMMARY_SEPARATOR = " | ";

    private static final String SENSORS_PICKUP_KEY = "sensors_pickup";
    private static final String SENSORS_PICKUP_PROPERTY = "persist.sensors.pickup";
    private static final String SENSORS_SIGNIFICANT_KEY = "sensors_significant";
    private static final String SENSORS_SIGNIFICANT_PROPERTY = "persist.sensors.significant";
    private static final String SENSORS_ACCEL_KEY = "sensors_accelerometer";
    private static final String SENSORS_ACCEL_PROPERTY = "persist.sensors.accelerometer";

    private SwitchPreference mSensorsPickup;
    private SwitchPreference mSensorsSignificant;
    private SwitchPreference mSensorsAccel;

    //Dialog
    private Dialog mSensorsDialog;

    private void rebootRequired() {
        UromSettings.rebootRequired(getActivity());
    }

    public static final boolean hasPickupSensor() {
        return (SystemProperties.getInt(SENSORS_PICKUP_PROPERTY,
                SENSORS_PICKUP_DEFAULT) != 0);
    }

    public static final boolean hasSignificantMotionSensor() {
        return (SystemProperties.getInt(SENSORS_SIGNIFICANT_PROPERTY,
                SENSORS_SIGNIFICANT_DEFAULT) != 0);
    }

    public static final boolean hasAccelSensor() {
        return (SystemProperties.getInt(SENSORS_ACCEL_PROPERTY,
                SENSORS_ACCEL_DEFAULT) != 0);
    }

    public static String getSummaryPreference(Context context) {
        StringBuilder sb = new StringBuilder();
        if (hasPickupSensor()) {
            sb.append(context.getString(R.string.sensors_pickup_title));
            sb.append(SUMMARY_SEPARATOR);
        }
        if (hasSignificantMotionSensor()) {
            sb.append(context.getString(R.string.sensors_significant_title));
            sb.append(SUMMARY_SEPARATOR);
        }
        if (hasAccelSensor()) {
            sb.append(context.getString(R.string.sensors_accelerometer_title));
            sb.append(SUMMARY_SEPARATOR);
        }

        // nothing enable
        if (sb.length() == 0)
            return context.getString(R.string.sensors_summary);

        // delete last separator
        sb.delete(sb.length() - SUMMARY_SEPARATOR.length(), sb.length());

        return sb.toString();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.urom_settings_sensors);

        mSensorsPickup = (SwitchPreference) findPreference(SENSORS_PICKUP_KEY);
        mSensorsSignificant = (SwitchPreference) findPreference(SENSORS_SIGNIFICANT_KEY);
        mSensorsAccel = (SwitchPreference) findPreference(SENSORS_ACCEL_KEY);

        //Dialog
        mSensorsDialog = null;
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
        updateSensorsPickupOptions();
        updateSensorsSignificantOptions();
        updateSensorsAccelOptions();
    }
    
    private void updateSensorsPickupOptions() {
        mSensorsPickup.setChecked(hasPickupSensor());
    }

    private void writeSensorsPickupOptions() {
        if (mSensorsPickup.isChecked()) {
            if (mSensorsDialog != null) {
                dismissDialogs();
            }
            mSensorsDialog = new AlertDialog.Builder(getActivity()).setMessage(
                    getResources().getString(R.string.sensors_experimental_warning))
                    .setTitle(R.string.sensors_pickup_enable)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SystemProperties.set(SENSORS_PICKUP_PROPERTY, "1");
                            rebootRequired();
                            updateSensorsPickupOptions();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Reset
                            mSensorsPickup.setChecked(false);
                        }
                    })
                    .show();
            mSensorsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mSensorsDialog = null;
                    updateSensorsPickupOptions();
                }
            });
        } else {
            SystemProperties.set(SENSORS_PICKUP_PROPERTY, "0");
            rebootRequired();
            updateSensorsPickupOptions();
        }
    }

    private void updateSensorsSignificantOptions() {
        mSensorsSignificant.setChecked(hasSignificantMotionSensor());
    }

    private void writeSensorsSignificantOptions() {
        if (mSensorsSignificant.isChecked()) {
            if (mSensorsDialog != null) {
                dismissDialogs();
            }
            mSensorsDialog = new AlertDialog.Builder(getActivity()).setMessage(
                    getResources().getString(R.string.sensors_experimental_warning))
                    .setTitle(R.string.sensors_significant_enable)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SystemProperties.set(SENSORS_SIGNIFICANT_PROPERTY, "1");
                            rebootRequired();
                            updateSensorsSignificantOptions();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Reset
                            mSensorsSignificant.setChecked(false);
                        }
                    })
                    .show();
            mSensorsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mSensorsDialog = null;
                    updateSensorsSignificantOptions();
                }
            });
        } else {
            SystemProperties.set(SENSORS_SIGNIFICANT_PROPERTY, "0");
            rebootRequired();
            updateSensorsSignificantOptions();
        }
    }

    private void updateSensorsAccelOptions() {
        mSensorsAccel.setChecked(hasAccelSensor());
    }

    private void writeSensorsAccelOptions() {
        SystemProperties.set(SENSORS_ACCEL_PROPERTY,
                mSensorsAccel.isChecked() ? "1" : "0");
        rebootRequired();
        updateSensorsAccelOptions();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mSensorsPickup) {
            writeSensorsPickupOptions();
        } else if (preference == mSensorsSignificant) {
            writeSensorsSignificantOptions();
        } else if (preference == mSensorsAccel) {
            writeSensorsAccelOptions();
        } else {
            return super.onPreferenceTreeClick(preference);
        }
        
        return false;
    }

    private void dismissDialogs() {
        if (mSensorsDialog != null) {
            mSensorsDialog.dismiss();
            mSensorsDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        dismissDialogs();
        super.onDestroy();
    } 
}
