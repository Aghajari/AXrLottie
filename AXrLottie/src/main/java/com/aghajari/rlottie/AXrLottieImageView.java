/*
 * Copyright (C) 2020 - Amir Hossein Aghajari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.aghajari.rlottie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.ArrayList;

public class AXrLottieImageView extends AppCompatImageView {

    private ArrayList<AXrLottieProperty.PropertyUpdate> layerProperties;
    private AXrLottieDrawable drawable;
    private int autoRepeat = AXrLottieOptions.DEFAULT;
    private boolean attachedToWindow;
    private boolean playing;

    public AXrLottieImageView(@NonNull Context context) {
        super(context);
    }

    public AXrLottieImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AXrLottieImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLayerProperty(String layer, AXrLottieProperty property) {
        if (layerProperties == null) {
            layerProperties = new ArrayList<>();
        }
        layerProperties.add(new AXrLottieProperty.PropertyUpdate(property, layer));
        if (drawable != null) {
            drawable.setLayerProperty(layer, property);
        }
    }

    public boolean setLottieDrawable(AXrLottieDrawable lottieDrawable) {
        if (drawable != null && drawable.equals(lottieDrawable)) return false;
        setImageDrawable(lottieDrawable);
        return true;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        //release();
    }

    @Override
    public void setImageDrawable(@Nullable Drawable mDrawable) {
        //release();
        if (mDrawable instanceof AXrLottieDrawable) {
            drawable = (AXrLottieDrawable) mDrawable;
            if (autoRepeat != AXrLottieOptions.DEFAULT)
                drawable.setAutoRepeat(autoRepeat);

            if (layerProperties != null) {
                drawable.setLayerProperties(layerProperties);
            }
            drawable.setAllowDecodeSingleFrame(true);
            playing = drawable.isRunning();
        }
        super.setImageDrawable(mDrawable);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        //release();
    }

    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
        //release();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
        if (drawable != null) {
            drawable.setCallback(this);
            if (playing) {
                drawable.start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
        if (drawable != null) {
            drawable.stop();
        }
    }

    public boolean isPlaying() {
        return drawable != null && drawable.isRunning();
    }

    public void setAutoRepeat(boolean repeat) {
        autoRepeat = repeat ? AXrLottieDrawable.AUTO_REPEAT_INFINITE : 0;
    }

    public void setAutoRepeat(int repeat) {
        autoRepeat = repeat;
    }

    public void playAnimation() {
        playing = true;
        if (drawable == null) {
            return;
        }
        if (attachedToWindow) {
            drawable.start();
        }
    }

    public void stopAnimation() {
        playing = false;
        if (drawable == null) {
            return;
        }
        if (attachedToWindow) {
            drawable.stop();
        }
    }

    public void release() {
        playing = false;
        if (drawable != null) {
            drawable.recycle();
            drawable = null;
        }
    }

    public AXrLottieDrawable getLottieDrawable() {
        return drawable;
    }
}
