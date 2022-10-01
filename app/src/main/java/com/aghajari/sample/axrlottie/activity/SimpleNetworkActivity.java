package com.aghajari.sample.axrlottie.activity;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieDrawable;
import com.aghajari.rlottie.AXrLottieImageView;
import com.aghajari.rlottie.AXrLottieOptions;
import com.aghajari.rlottie.glide.AXrLottieGlideOptions;
import com.aghajari.sample.axrlottie.R;
import com.bumptech.glide.Glide;

public class SimpleNetworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        final AXrLottieImageView lottieView = findViewById(R.id.lottie_view);

        //final String url = "https://image-1.gapo.vn/images/AnimatedSticker.tgs";
        final String url = "https://image-1.gapo.vn/images/kienht-gapo-sticker2.zip";

        /*
        lottieView.setLottieDrawable(
         AXrLottieDrawable.fromURL(url)
         .setAutoStart(true)
         .build());
         */

        Glide.with(this)
                .load(url)
                .set(AXrLottieGlideOptions.ENABLED, true)
                .set(AXrLottieGlideOptions.EXTENSION, ".zip")
                .set(AXrLottieGlideOptions.NAME, url)
                .set(AXrLottieGlideOptions.NETWORK, true)
                .thumbnail(
                        Glide.with(this)
                                .load(Uri.parse("file:///android_asset/loader.json"))
                                .set(AXrLottieGlideOptions.ENABLED, true)
                                .set(AXrLottieGlideOptions.NAME, "loader.json")
                                .set(AXrLottieGlideOptions.NETWORK, false)
                )
                .into(lottieView);
    }
}