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


package com.aghajari.rlottie.network;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.util.zip.ZipInputStream;

public class AXrLottieSimpleNetworkFetcher extends AXrLottieNetworkFetcher {

    @Override
    public void load(File file) {
        if (file != null && !drawable.hasLoaded()) {
            drawable.load(file);
        }
    }

    @Override
    public void error(Exception e) {
        e.printStackTrace();
    }

    @Override
    public File getResultFromConnection(Context context, HttpURLConnection connection) {
        try {
            File file;
            String url = getDecodedUrl(this.url);
            String contentType = connection.getContentType();
            if (contentType == null) {
                // Assume JSON for best effort parsing. If it fails, it will just deliver the parse exception
                // in the result which is more useful than failing here.
                contentType = "application/json";
            }
            if (ZipCompositionFactory.isZipContent(contentType)) {
                file = NetworkCache.writeTempCacheFile(context, url,getCacheName(), connection.getInputStream(), FileExtension.ZIP());
                file = fromZipStream(file,
                        NetworkCache.getCachedFile(context, NetworkCache.filenameForUrl(url,getCacheName(), FileExtension.JSON(), true)),
                        new ZipInputStream(new FileInputStream(file)));
                Log.d("lottie", file.getAbsolutePath());
            } else {
                file = NetworkCache.writeTempCacheFile(context, url,getCacheName(), connection.getInputStream(), FileExtension.JSON());
            }
            file = NetworkCache.renameTempFile(context, url,getCacheName(), FileExtension.JSON());
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected File fromZipStream(File file, File output, ZipInputStream stream) {
        return ZipCompositionFactory.fromZipStreamSyncInternal(file, output, stream);
    }
}
