package com.aghajari.sample.axrlottie.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.aghajari.emojiview.sticker.RecentStickerManager;

import com.aghajari.rlottie.AXrLottie2Gif;
import com.aghajari.rlottie.AXrLottieImageView;

import com.aghajari.sample.axrlottie.R;
import com.aghajari.sample.axrlottie.Utils;
import com.aghajari.sample.axrlottie.sticker.AnimatedSticker;

import java.io.File;

public class Lottie2GifActivity extends AppCompatActivity {
    AXrLottieImageView lottieView;
    TextView tv, size_tv;
    AppCompatEditText size;
    CheckBox bgColor, async, dithering;

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottie2gif);

        lottieView = findViewById(R.id.lottie_view);
        tv = findViewById(R.id.log_tv);
        size_tv = findViewById(R.id.size_log);
        size = findViewById(R.id.size);
        bgColor = findViewById(R.id.bg_color);
        async = findViewById(R.id.async_task);
        dithering = findViewById(R.id.dithering);

        size.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                size_tv.setText("Size : " + findSize() + "x" + findSize());
            }
        });

        RecentStickerManager recent = new RecentStickerManager(this, "stickers");
        if (recent.isEmpty()) {
            lottieView.setLottieDrawable(Utils.createFromSticker(this, new AnimatedSticker("KangarooFighter/sticker3.json", "KangarooFighter.sticker3"), 512));
        } else {
            try {
                AnimatedSticker sticker = (AnimatedSticker) recent.getRecentStickers().toArray()[0];
                lottieView.setLottieDrawable(Utils.createFromSticker(this, sticker, 512));
            } catch (Exception ignore) {
                lottieView.setLottieDrawable(Utils.createFromSticker(this, new AnimatedSticker("KangarooFighter/sticker3.json", "KangarooFighter.sticker3"), 512));
            }
        }
        lottieView.playAnimation();

        if (checkPermission()) {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "lottie.gif");
        } else {
            file = new File(getExternalCacheDir(), "lottie.gif");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "lottie.gif");
    }

    public void convert(View view) {
        final int gifSize = findSize();

        AXrLottie2Gif.create(lottieView.getLottieDrawable().getBuilder().setSize(gifSize, gifSize).build())
                .setListener(new AXrLottie2Gif.Lottie2GifListener() {
                    long start;
                    String logs;

                    @Override
                    public void onStarted() {
                        start = System.currentTimeMillis();
                        logs = "Wait a moment...\r\n";
                        log(logs);
                    }

                    @Override
                    public void onProgress(int frame, int totalFrame) {
                        log(logs + "progress : " + frame + "/" + totalFrame);
                    }

                    @Override
                    public void onFinished() {
                        logs = "GIF created (" + (System.currentTimeMillis() - start) + "ms)\r\n" +
                                "Resolution : " + gifSize + "x" + gifSize + "\r\n" +
                                "Path : " + file.getAbsolutePath() + "\r\n" +
                                "File Size : " + (file.length() / 1024) + "kb";
                        log(logs);
                    }
                })
                .setBackgroundColor(bgColor.isChecked() ? Color.WHITE : Color.TRANSPARENT)
                .setOutputPath(file)
                .setSize(gifSize, gifSize)
                .setBackgroundTask(async.isChecked())
                .setDithering(dithering.isChecked())
                .setDestroyable(true)
                .build();
    }

    private int findSize() {
        if (size.getText().toString().isEmpty()) return 256;
        try {
            return Integer.parseInt(size.getText().toString());
        } catch (Exception ignore) {
            return 256;
        }
    }

    private void log(final String log) {
        tv.post(new Runnable() {
            @Override
            public void run() {
                tv.setText(log.trim());
            }
        });
    }
}