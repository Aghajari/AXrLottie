package com.aghajari.sample.axrlottie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.aghajari.rlottie.AXrLottieMarker;
import com.aghajari.sample.axrlottie.R;

import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieImageView;
import com.aghajari.rlottie.AXrLottieLayerInfo;
import com.aghajari.rlottie.AXrLottieProperty;

import java.util.Random;

public class ColorLayerActivity extends AppCompatActivity {
    AXrLottieImageView lottieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_layer);

        lottieView = findViewById(R.id.lottie_view);

        lottieView.setLottieDrawable(AXrLottie.Loader.createFromAssets(this, "tractor.json",
                "tractor", 512, 512, false, false));

        lottieView.playAnimation();

        // layers
        Log.i("AXrLottie", "Layers : ");
        for (AXrLottieLayerInfo layerInfo : lottieView.getLottieDrawable().getLayers()) {
            Log.i("AXrLottie", layerInfo.toString());
        }
    }

    public void changeBgColor(View v) {
        lottieView.setLayerProperty("BG Outlines.**", AXrLottieProperty.fillColor(findColor()));
    }

    public void changeCloudsColor(View v) {
        int color = findColor();
        lottieView.setLayerProperty("Cloud 1 Outlines.**", AXrLottieProperty.fillColor(color));
        lottieView.setLayerProperty("Cloud 2 Outlines.**", AXrLottieProperty.fillColor(color));
        lottieView.setLayerProperty("Cloud 3 Outlines.**", AXrLottieProperty.fillColor(color));
        lottieView.setLayerProperty("Cloud 4 Outlines.**", AXrLottieProperty.fillColor(color));
    }

    Random rnd = new Random();

    private int findColor() {
        return Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
    }
}