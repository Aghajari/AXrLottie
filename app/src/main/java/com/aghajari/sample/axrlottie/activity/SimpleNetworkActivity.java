package com.aghajari.sample.axrlottie.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aghajari.rlottie.AXrLottieDrawable;
import com.aghajari.rlottie.AXrLottieImageView;
import com.aghajari.sample.axrlottie.R;

public class SimpleNetworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        final AXrLottieImageView lottieView = findViewById(R.id.lottie_view);

       lottieView.setLottieDrawable(AXrLottieDrawable.fromURL("https://image-1.gapo.vn/images/kienht-gapo-sticker2.zip").setAutoStart(true).build());
       lottieView.playAnimation();

    }
}