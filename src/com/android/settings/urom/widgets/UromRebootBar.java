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

package com.android.settings.urom.widgets;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.android.settings.R;

public class UromRebootBar extends LinearLayout {

    public UromRebootBar(Context context) {
        this(context, null);
    }

    public UromRebootBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UromRebootBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public UromRebootBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.urom_reboot_bar, this);

        // Default is hide
        setVisibility(View.GONE);
    }

    private void reboot() {
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        pm.reboot(null);
    }

    public void show() {
        if (!isShowing()) {
            setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        if (isShowing()) {
            setVisibility(View.GONE);
        }
    }

    public boolean isShowing() {
        return (getVisibility() == View.VISIBLE);
    }

    static class SavedState extends BaseSavedState {
        boolean visible;

        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            visible = (Boolean)in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(visible);
        }

        @Override
        public String toString() {
            return "UromRebootBar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " visible=" + visible + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.visible = isShowing();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());

        setVisibility(ss.visible ? View.VISIBLE : View.GONE);

        requestLayout();
    }
}
