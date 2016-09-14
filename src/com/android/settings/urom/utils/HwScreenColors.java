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

package com.android.settings.urom.utils;

public class HwScreenColors {
    private static final String KCAL_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal";
    private static final String KCAL_MINIMUM_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal_min";
    private static final String KCAL_ENABLE_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal_enable";

    public static final int KCAL_MAX = 255;
    public static final int KCAL_MIN = 0;
    public static final int LED_BRIGHTNESS_MAX = 255;
    public static final String KCAL_DEFAULT = KCAL_MAX + " " + KCAL_MAX + " " + KCAL_MAX;
    public static final int KCAL_MINIMUM_DEFAULT = 35;
    public static final boolean KCAL_ENABLE_DEFAULT = true;

    /* KCAL Support */
    public static boolean isSupported() {
        return (FileUtils.readOneLine(KCAL_ENABLE_FILE) != null);
    }

    /* KCAL Enable (kcal_enable) */
    public static boolean isEnable() {
        String value = FileUtils.readOneLine(KCAL_ENABLE_FILE);

        return (value != null && value.contentEquals("1"));
    }

    public static boolean setEnable(boolean value) {
        return FileUtils.writeLine(KCAL_ENABLE_FILE, value ? "1\n" : "0\n");
    }

    /* KCAL Minimum (kcal_min) */
    public static boolean writeMinimum(int min) {
        return FileUtils.writeLine(KCAL_MINIMUM_FILE, Integer.toString(min));
    }

    /* KCAL RGB (kcal) */
    public static String readRGB() {
        return FileUtils.readOneLine(KCAL_FILE);
    }

    public static boolean writeRGB(String rgb) {
        return FileUtils.writeLine(KCAL_FILE, rgb);
    }

    public static int[] readRGBtoInt() {
        String rgb = readRGB();
        if (rgb == null)
            return null;

        return convertRGBtoInt(rgb);
    }

    public static int[] convertRGBtoInt(String rgb) {
        String[] colors = rgb.split(" ");

        if (colors.length != 3)
            return null;

        int[] intColors = new int[3];
        for (int i = 0; i < 3; i++) {
            intColors[i] = Integer.parseInt(colors[i]);
        }

        return intColors;
    }

    public static boolean writeRGBfromInt(int[] colors) {
        return writeRGB(convertIntToRGB(colors));
    }

    public static String convertIntToRGB(int[] colors) {
        return Integer.toString(colors[0]) + " " +
                Integer.toString(colors[1]) + " " +
                Integer.toString(colors[2]);
    }

}
