package com.aghajari.sample.axrlottie.activity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.aghajari.emojiview.listener.OnStickerActions;
import com.aghajari.emojiview.listener.SimplePopupAdapter;
import com.aghajari.emojiview.sticker.Sticker;
import com.aghajari.emojiview.view.AXEmojiEditText;
import com.aghajari.emojiview.view.AXEmojiPager;
import com.aghajari.emojiview.view.AXEmojiPopupLayout;
import com.aghajari.emojiview.view.AXEmojiView;
import com.aghajari.emojiview.view.AXStickerView;

import com.aghajari.rlottie.AXrLottieImageView;

import com.aghajari.sample.axrlottie.R;
import com.aghajari.sample.axrlottie.Utils;
import com.aghajari.sample.axrlottie.sticker.AnimatedSticker;
import com.aghajari.sample.axrlottie.sticker.MyStickerProvider;

public class AXEmojiViewActivity extends AppCompatActivity {
    AXEmojiPopupLayout layout;

    FrameLayout edtParent;
    AXEmojiEditText edt;
    AppCompatImageView emojiImg;

    AXrLottieImageView lottieView;
    private boolean isShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emoji_view);
        init();
    }

    private void init() {
        layout = findViewById(R.id.layout);

        // get emoji edit text
        edtParent = findViewById(R.id.edt_parent);
        edt = findViewById(R.id.edt);
        emojiImg = findViewById(R.id.imageView);
        lottieView = findViewById(R.id.lottie_view);

        AXEmojiPager emojiPager = loadView();

        // create emoji popup
        layout.initPopupView(emojiPager);
        layout.hideAndOpenKeyboard();
        edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.openKeyboard();
            }
        });

        emojiImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowing) {
                    layout.openKeyboard();
                } else {
                    layout.show();
                }
            }
        });

        findViewById(R.id.send_emoji).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt.getText().length() > 0) {
                    edt.setText("");
                }
            }
        });

        layout.setPopupListener(new SimplePopupAdapter() {
            @Override
            public void onShow() {
                updateButton(true);
            }

            @Override
            public void onDismiss() {
                updateButton(false);
            }

            @Override
            public void onKeyboardOpened(int height) {
                updateButton(false);
            }

            @Override
            public void onKeyboardClosed() {
                updateButton(layout.isShowing());
            }

            private void updateButton(boolean emoji) {
                if (isShowing == emoji) return;
                isShowing = emoji;
                if (emoji) {
                    Drawable dr = AppCompatResources.getDrawable(AXEmojiViewActivity.this, R.drawable.ic_msg_panel_kb);
                    DrawableCompat.setTint(DrawableCompat.wrap(dr), Color.BLACK);
                    emojiImg.setImageDrawable(dr);
                } else {
                    Drawable dr = AppCompatResources.getDrawable(AXEmojiViewActivity.this, R.drawable.ic_msg_panel_smiles);
                    DrawableCompat.setTint(DrawableCompat.wrap(dr), Color.BLACK);
                    emojiImg.setImageDrawable(dr);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (!layout.onBackPressed())
            super.onBackPressed();
    }

    public AXEmojiPager loadView() {
        AXEmojiPager emojiPager = new AXEmojiPager(this);

        AXEmojiView emojiView = new AXEmojiView(this);
        emojiPager.addPage(emojiView, R.drawable.ic_msg_panel_smiles);

        AXStickerView stickerView = new AXStickerView(this, "stickers", new MyStickerProvider());
        emojiPager.addPage(stickerView, R.drawable.ic_msg_panel_stickers);

        stickerView.setOnStickerActionsListener(new OnStickerActions() {
            @Override
            public void onClick(View view, Sticker sticker, boolean fromRecent) {
                if (sticker instanceof AnimatedSticker) {
                    AnimatedSticker animatedSticker = (AnimatedSticker) sticker;
                    lottieView.release();
                    lottieView.setLottieDrawable(Utils.createFromSticker(AXEmojiViewActivity.this, animatedSticker, 512));
                    lottieView.playAnimation();
                }
            }

            @Override
            public boolean onLongClick(View view, Sticker sticker, boolean fromRecent) {
                return false;
            }
        });

        // set target emoji edit text to emojiViewPager
        emojiPager.setEditText(edt);

        emojiPager.setSwipeWithFingerEnabled(true);

        emojiPager.setLeftIcon(R.drawable.ic_ab_search);
        emojiPager.setOnFooterItemClicked(new AXEmojiPager.OnFooterItemClicked() {
            @Override
            public void onClick(View view, boolean leftIcon) {
                if (leftIcon)
                    Toast.makeText(AXEmojiViewActivity.this, "Search Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        return emojiPager;
    }

}
