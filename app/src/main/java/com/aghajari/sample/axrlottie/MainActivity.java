package com.aghajari.sample.axrlottie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.aghajari.sample.axrlottie.activity.AXEmojiViewActivity;
import com.aghajari.sample.axrlottie.activity.ColorLayerActivity;
import com.aghajari.sample.axrlottie.activity.Lottie2GifActivity;
import com.aghajari.sample.axrlottie.activity.LottieEditorActivity;
import com.aghajari.sample.axrlottie.activity.MarkerActivity;
import com.aghajari.sample.axrlottie.activity.SimpleActivity;
import com.aghajari.sample.axrlottie.activity.SimpleNetworkActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void simpleActivity(View view) {
        startActivity(new Intent(this, SimpleActivity.class));
    }

    public void simpleNetworkActivity(View view) {
        startActivity(new Intent(this, SimpleNetworkActivity.class));
    }

    public void colorLayerActivity(View view) {
        startActivity(new Intent(this, ColorLayerActivity.class));
    }

    public void editorActivity(View view) {
        startActivity(new Intent(this, LottieEditorActivity.class));
    }

    public void markerActivity(View view) {
        startActivity(new Intent(this, MarkerActivity.class));
    }

    public void emojiViewActivity(View view) {
        startActivity(new Intent(this, AXEmojiViewActivity.class));
    }

    public void lottie2gifActivity(View view) {
        startActivity(new Intent(this, Lottie2GifActivity.class));
    }
}