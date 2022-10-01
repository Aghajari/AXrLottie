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


package com.aghajari.rlottie.extension;

import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.WorkerThread;

/**
 * Helper class to extract json animation from a zip
 */
class ZipCompositionFactory {

    private static final String TAG = ZipCompositionFactory.class.getSimpleName();

    public static boolean isZipContent(String contentType) {
        return contentType.toLowerCase().contains("application/zip") ||
                contentType.toLowerCase().contains("application/x-zip") ||
                contentType.toLowerCase().contains("application/x-zip-compressed");
    }

    @WorkerThread
    static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    @WorkerThread
    public static File toFile(File f, File output, ZipInputStream zis, ZipEntry ze) {
        int count;
        byte[] buffer = new byte[8192];
        File file = new File(output.getAbsolutePath());
        File dir = ze.isDirectory() ? file : file.getParentFile();
        if (!dir.isDirectory() && !dir.mkdirs()) return null;
        if (ze.isDirectory()) return null;
        FileOutputStream fout;
        try {
            if (output.exists()) {
                output.delete();
            }
            fout = new FileOutputStream(file);
            try {
                while ((count = zis.read(buffer)) != -1)
                    fout.write(buffer, 0, count);
            } finally {
                fout.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "toFile: ", e);
            return null;
        }
        if (f.exists()) {
            f.delete();
        }
        return file;
    }

    @WorkerThread
    public static File fromZipStreamSyncInternal(File file, File output, ZipInputStream inputStream) {
        try {
            ZipEntry entry = inputStream.getNextEntry();
            while (entry != null) {
                final String entryName = entry.getName();
                if (entryName.contains("__MACOSX")) {
                    inputStream.closeEntry();
                } else if (entry.getName().toLowerCase().contains(".json")) {
                    File f = toFile(file, output, inputStream, entry);
                    if (f != null) {
                        closeQuietly(inputStream);
                        return f;
                    }
                    /*
                    } else if (entryName.contains(".png") || entryName.contains(".webp")) {
                     } else {
                     File f = toFile(file, output, inputStream, entry);
                     if (f != null) {
                     closeQuietly(inputStream);
                     return f;
                     } */
                }
                entry = inputStream.getNextEntry();
            }
        } catch (IOException e) {
            Log.e(TAG, "fromZipStreamSyncInternal: ", e);
            e.printStackTrace();
        }
        closeQuietly(inputStream);
        return null;
    }
}