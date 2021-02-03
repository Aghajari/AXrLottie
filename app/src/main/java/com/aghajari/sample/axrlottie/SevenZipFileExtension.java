package com.aghajari.sample.axrlottie;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.extension.AXrFileExtension;
import com.aghajari.rlottie.extension.JsonFileExtension;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author kienht
 * @since 03/02/2021
 * A demo for 7zip extension. Need check before use it.
 */
public class SevenZipFileExtension extends AXrFileExtension {

    public SevenZipFileExtension() {
        super(".7z");
    }

    @Override
    public boolean canParseContent(String contentType) {
        // check content-type
        return contentType.contains("application/x-7z-compressed");
    }

    @Override
    public File toFile(String cache, File input, boolean fromNetwork) throws IOException {
        SevenZFile sevenZFile = new SevenZFile(input);
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        if (((List<SevenZArchiveEntry>) sevenZFile.getEntries()).size() > 1) {
            throw new IllegalArgumentException("7zip file must contains only one json file!");
        }
        File output = AXrLottie.getLottieCacheManager().getCachedFile(cache, JsonFileExtension.JSON, fromNetwork, false);
        if (entry != null) {
            FileOutputStream out = new FileOutputStream(output);
            byte[] content = new byte[(int) entry.getSize()];
            sevenZFile.read(content, 0, content.length);
            out.write(content);
            out.close();
        }
        sevenZFile.close();
        return output;
    }
}