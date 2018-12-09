/*
 * Copyright (C) 2018 nAOSProm
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

import java.util.Arrays;
import java.util.List;

public class UromUtils {
    private static final List<String> DISABABLE_APPS = Arrays.asList(new String [] {
        //"com.android.camera2",
        //"com.android.documentsui",
        "com.android.fmradio",
        "com.android.gallery3d",
        //"com.android.launcher3",
        "com.android.messaging",
        "com.android.phone",
        //"com.google.android.launcher"
    });

    public static boolean isSystemAppDisabable(String packageName) {
        return DISABABLE_APPS.contains(packageName);
    }
}

