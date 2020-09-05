/*
 * Copyright (C) 2020 - Amir Hossein Aghajari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.aghajari.rlottie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

class CacheWriter {

    public static File load(final String json, final String name) {
        if (AXrLottie.context==null) return null;
        File f = new File(new File(AXrLottie.context.getCacheDir(), "lottie"), name + ".cache");
        if (f.exists()) return f;
        return write(json, name);
    }

    private static File write(final String json, final String name) {
        try {
            File f = new File(AXrLottie.context.getCacheDir(), "lottie");
            if (!f.mkdir()) return null;
            File f2 = new File(f, name + ".cache");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(f2));
            outputStreamWriter.write(json);
            outputStreamWriter.close();

            return f2;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
