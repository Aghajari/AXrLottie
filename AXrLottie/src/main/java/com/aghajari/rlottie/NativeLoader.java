/*
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package com.aghajari.rlottie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class NativeLoader {

    private final static String LIB_NAME = "jlottie";
    private final static String LIB_SO_NAME = "lib" + LIB_NAME + ".so";
    private final static String LOCALE_LIB_SO_NAME = "lib" + LIB_NAME + "loc.so";

    private static volatile boolean nativeLoaded = false;

    @SuppressLint({"UnsafeDynamicallyLoadedCode", "SetWorldReadable"})
    private static boolean loadFromZip(Context context, File destDir, File destLocalFile, String folder) {
        try {
            for (File file : destDir.listFiles()) {
                file.delete();
            }
        } catch (Exception ignore) {
        }

        ZipFile zipFile = null;
        InputStream stream = null;
        try {
            zipFile = new ZipFile(context.getApplicationInfo().sourceDir);
            ZipEntry entry = zipFile.getEntry("lib/" + folder + "/" + LIB_SO_NAME);
            if (entry == null) {
                throw new Exception("Unable to find file in apk:" + "lib/" + folder + "/" + LIB_NAME);
            }
            stream = zipFile.getInputStream(entry);

            OutputStream out = new FileOutputStream(destLocalFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = stream.read(buf)) > 0) {
                Thread.yield();
                out.write(buf, 0, len);
            }
            out.close();

            destLocalFile.setReadable(true, false);
            destLocalFile.setExecutable(true, false);
            destLocalFile.setWritable(true);

            try {
                System.load(destLocalFile.getAbsolutePath());
                nativeLoaded = true;
            } catch (Error ignore) {
            }
            return true;
        } catch (Exception ignore) {
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ignore) {
                }
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception ignore) {
                }
            }
        }
        return false;
    }

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    static synchronized void initNativeLibs(Context context) {
        if (nativeLoaded) {
            return;
        }

        try {
            try {
                System.loadLibrary(LIB_NAME);
                nativeLoaded = true;
                return;
            } catch (Error ignore) {
            }

            int index = 0;
            int max = getAbiCount();

            while (!nativeLoaded && index < max) {
                String folder;
                try {
                    String abi = getAbi(index);
                    if (abi.equalsIgnoreCase("x86_64")) {
                        folder = "x86_64";
                    } else if (abi.equalsIgnoreCase("arm64-v8a")) {
                        folder = "arm64-v8a";
                    } else if (abi.equalsIgnoreCase("armeabi-v7a")) {
                        folder = "armeabi-v7a";
                    } else if (abi.equalsIgnoreCase("armeabi")) {
                        folder = "armeabi";
                    } else if (abi.equalsIgnoreCase("x86")) {
                        folder = "x86";
                    } else if (abi.equalsIgnoreCase("mips")) {
                        folder = "mips";
                    } else {
                        folder = "armeabi";
                    }
                } catch (Exception ignore) {
                    folder = "armeabi";
                }

                String javaArch = System.getProperty("os.arch");
                if (javaArch != null && (javaArch.contains("686") || javaArch.contains("x86"))) {
                    folder = "x86";
                }

                File destDir = new File(context.getFilesDir(), "lib");
                destDir.mkdirs();

                File destLocalFile = new File(destDir, LOCALE_LIB_SO_NAME);
                if (destLocalFile.exists()) {
                    try {
                        System.load(destLocalFile.getAbsolutePath());
                        nativeLoaded = true;
                        return;
                    } catch (Error ignore) {
                    }
                    destLocalFile.delete();
                }

                if (loadFromZip(context, destDir, destLocalFile, folder)) {
                    if (nativeLoaded)
                        return;
                }
                index++;
            }
        } catch (Throwable ignore) {
        }

        try {
            System.loadLibrary(LIB_NAME);
            nativeLoaded = true;
        } catch (Error e) {
            e.printStackTrace();
        }

        if (!nativeLoaded) {
            System.err.println("Couldn't load native AXrLottie library!");
        }
    }

    private static String getAbi(int index) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return Build.SUPPORTED_ABIS[index];
        } else {
            return Build.CPU_ABI;
        }
    }

    private static int getAbiCount() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return Build.SUPPORTED_ABIS.length;
        } else {
            return 1;
        }
    }
}