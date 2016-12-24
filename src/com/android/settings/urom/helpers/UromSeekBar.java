package com.android.settings.urom.helpers;

import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class UromSeekBar implements SeekBar.OnSeekBarChangeListener, Button.OnClickListener {
    private Values mValues;
    private SeekBar mSeekBar;
    private TextView mInfo;
    private Button mLess;
    private Button mMore;

    private boolean mFromButtons;

    public UromSeekBar(Values values, View view, int seekbar, int info) {
        this(values, view, seekbar, info, -1, -1);
    }

    public UromSeekBar(Values values, View view, int seekbar, int info, int less, int more) {
        mFromButtons = false;
        mValues = values;
        mSeekBar = (SeekBar)view.findViewById(seekbar);
        mInfo = (TextView)view.findViewById(info);
        mLess = (Button)view.findViewById(less);
        mMore = (Button)view.findViewById(more);

        mSeekBar.setMax(mValues.getMaxValue(this) - mValues.getMinValue(this));
        mSeekBar.setOnSeekBarChangeListener(this);

        if (mLess != null) {
            mLess.setOnClickListener(this);
        }
        if (mMore != null) {
            mMore.setOnClickListener(this);
        }
    }

    public void setProgressFromString(String progress) {
        mSeekBar.setProgress(Integer.valueOf(progress));
    }

    public void setProgress(int value) {
        mSeekBar.setProgress(value - mValues.getMinValue(this));

        mInfo.setText(mValues.getTextValue(this));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mValues.onValueChanged(progress + mValues.getMinValue(this), fromUser || mFromButtons, this);
        mFromButtons = false;

        mInfo.setText(mValues.getTextValue(this));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        if (v == mLess) {
            int progress = mSeekBar.getProgress() - 1;
            if (progress >= 0) {
                mFromButtons = true;
                mSeekBar.setProgress(progress);
            }
        } else if (v == mMore) {
            int progress = mSeekBar.getProgress() + 1;
            if (progress <= mSeekBar.getMax()) {
                mFromButtons = true;
                mSeekBar.setProgress(progress);
            }
        }
    }

    public interface Values {
        int getMinValue(UromSeekBar helper);
        int getMaxValue(UromSeekBar helper);
        String getTextValue(UromSeekBar helper);
        void onValueChanged(int value, boolean fromUser, UromSeekBar helper);
    }
}
