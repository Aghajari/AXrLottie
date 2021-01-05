package com.aghajari.sample.axrlottie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.aghajari.emojiview.AXEmojiManager;
import com.aghajari.emojiview.emoji.iosprovider.AXIOSEmojiProvider;
import com.aghajari.emojiview.listener.StickerViewCreatorListener;
import com.aghajari.emojiview.sticker.RecentStickerManager;
import com.aghajari.emojiview.sticker.StickerCategory;
import com.aghajari.rlottie.AXrLottie;
import com.aghajari.rlottie.AXrLottieImageView;
import com.aghajari.sample.axrlottie.activity.AXEmojiViewActivity;
import com.aghajari.sample.axrlottie.activity.ColorLayerActivity;
import com.aghajari.sample.axrlottie.activity.Lottie2GifActivity;
import com.aghajari.sample.axrlottie.activity.LottieEditorActivity;
import com.aghajari.sample.axrlottie.activity.SimpleActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AXrLottie.init(this);
        initEmojiView();

        setContentView(R.layout.activity_main);

    }

    public void simpleActivity(View view){
        startActivity(new Intent(this, SimpleActivity.class));
    }

    public void colorLayerActivity(View view){
        startActivity(new Intent(this, ColorLayerActivity.class));
    }

    public void editorActivity(View view){
        startActivity(new Intent(this, LottieEditorActivity.class));
    }

    public void emojiViewActivity(View view){
        startActivity(new Intent(this, AXEmojiViewActivity.class));
    }

    public void lottie2gifActivity(View view){
        startActivity(new Intent(this, Lottie2GifActivity.class));
    }

    public void initEmojiView(){
        AXEmojiManager.install(this, new AXIOSEmojiProvider(this));

        AXEmojiManager.setStickerViewCreatorListener(new StickerViewCreatorListener() {
            @Override
            public View onCreateStickerView(@NonNull Context context, @Nullable StickerCategory category, boolean isRecent) {
                return new AXrLottieImageView(context);
            }

            @Override
            public View onCreateCategoryView(@NonNull Context context) {
                return new AXrLottieImageView(context);
            }
        });

        AXEmojiManager.getEmojiViewTheme().setFooterEnabled(true);
        AXEmojiManager.getEmojiViewTheme().setFooterSelectedItemColor(0xffFF4081);
        AXEmojiManager.getEmojiViewTheme().setSelectionColor(Color.TRANSPARENT);
        AXEmojiManager.getEmojiViewTheme().setSelectedColor(0xffFF4081);
        AXEmojiManager.getEmojiViewTheme().setCategoryColor(Color.WHITE);
        AXEmojiManager.getEmojiViewTheme().setFooterBackgroundColor(Color.WHITE);
        AXEmojiManager.getEmojiViewTheme().setAlwaysShowDivider(true);

        AXEmojiManager.getStickerViewTheme().setSelectionColor(0xffFF4081);
        AXEmojiManager.getStickerViewTheme().setSelectedColor(0xffFF4081);
        AXEmojiManager.getStickerViewTheme().setCategoryColor(Color.WHITE);
        AXEmojiManager.getStickerViewTheme().setAlwaysShowDivider(true);
    }
}