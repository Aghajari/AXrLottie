package com.aghajari.rlottie.network;

/**
 * @author kienht
 * @since 29/01/2021
 * <p>
 * Helpers for known Lottie downloader file types.
 */
enum AXrFileExtension {
    JSON(".json"),
    ZIP(".zip");

    public final String extension;

    AXrFileExtension(String extension) {
        this.extension = extension;
    }

    public String tempExtension() {
        return ".temp" + extension;
    }

    @Override
    public String toString() {
        return extension;
    }

    public static AXrFileExtension forFile(String filename) {
        for (AXrFileExtension e : values()) {
            if (filename.endsWith(e.extension)) {
                return e;
            }
        }
        // Default to Json.
        return JSON;
    }
}