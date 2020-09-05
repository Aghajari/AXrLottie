<div align="center">
  <br><b>AXrLottie</b> Renders animations<br>and vectors exported in the bodymovin JSON format
  <br><a href="https://github.com/Aghajari/AXrLottie">GitHub</a> • <a href="https://github.com/Aghajari/AXrLottie/releases">Releases</a>
  <br><br><img width="40" alt="LCoders | AmirHosseinAghajari" src="https://user-images.githubusercontent.com/30867537/90538314-a0a79200-e193-11ea-8d90-0a3576e28a18.png"><br><img width="250" alt="picker" src="https://github.com/Samsung/rlottie/raw/master/.Gifs/logo.png">
</div>

## Screenshot
<img src="./images/screen.png" width=300 title="Screen">

## Table of Contents  
- [Installation](#installation)  
- [Usage](#usage)
  - [Install AXrLottie](#install-axrlottie)
  - [Basic Usage](#basic-usage)
  - [LayerProperty](#layerproperty)
    - [AnimationLayers](#animationlayers)
  - [Lottie2Gif](#lottie2gif)
  - [Listeners](#listeners)
- [AnimatedSticker (AXEmojiView)](#animatedsticker)
- [Author](#author)
- [License](#license)

## Installation

AXrLottie is available in the JCenter, so you just need to add it as a dependency (Module gradle)

Gradle
```gradle
implementation 'com.aghajari.rlottie:AXrLottie:1.0.0'
```

Maven
```xml
<dependency>
  <groupId>com.aghajari.rlottie</groupId>
  <artifactId>AXrLottie</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

# Usage
Let's START! :smiley:


## Install AXrLottie 
First step, you should install AXrLottie

```java
AXrLottie.init(this);
```

## Basic Usage

Create an AXrLottieImageView in your layout.

```xml
<com.aghajari.rlottie.AXrLottieImageView
        android:id="@+id/lottie_view"
        android:layout_width="180dp"
        android:layout_height="180dp"
        android:layout_gravity="center"/>
```

Now you just need to load your lottie Animation
```java
lottieView.setLottieDrawable(AXrLottieDrawable.fromAssets(this,fileName)
                .setSize(width,height)
                .build());
lottieView.playAnimation();
```
you can load lottie file from following sources :
- File
- Json (String)
- Url
- Assets
- Resource
- InputStram

lottie will cache animations and files
you can disable cache in AXrLottieDrawable Builder

### Output
<img src="./images/simple.gif" width=300 title="Screen">

## LayerProperty

```java
lottieView.setLayerProperty("layer_name.**", AXrLottieProperty.colorProperty(color));
```

Properties :
- Color
- FillOpacity
- StrokeOpacity
- StrokeWidth
- TrAnchor
- TrOpacity
- TrPosition
- TrRotation
- TrScale

### Output
<img src="./images/layer.gif" width=300 title="Screen">

### AnimationLayers

```java
for (AXrLottieLayerInfo layerInfo : lottieDrawable.getLayers()) {
    Log.i("AXrLottie", "layerName: " + layerInfo.getName());
}
```

## Lottie2Gif
you can export lottie animations as a GIF!
thanks to [gif-h](https://github.com/charlietangora/gif-h)

```java
AXrLottie2Gif.create(lottieDrawable)
                .setListener(new AXrLottie2Gif.Lottie2GifListener() {
                    long start;

                    @Override
                    public void onStarted() {
                        start = System.currentTimeMillis();
                    }

                    @Override
                    public void onProgress(int frame, int totalFrame) {
                        log("progress : " + frame + "/" + totalFrame);
                    }

                    @Override
                    public void onFinished() {
                        log("GIF created (" + (System.currentTimeMillis() - start) + "ms)\r\n" +
                                "Resolution : " + gifSize + "x" + gifSize + "\r\n" +
                                "Path : " + file.getAbsolutePath() + "\r\n" +
                                "File Size : " + (file.length() / 1024) + "kb");
                    }
                })
                .setBackgroundColor(Color.WHITE)
                .setOutputPath(file)
                .setSize(gifSize, gifSize)
                .setBackgroundTask(true)
                .setDithering(false)
                .setDestroyable(true)
                .build();
```

### Output
<img src="./images/gif.png" width=300 title="Screen">

## Listeners
OnFrameChangedListener:
```java
void onFrameChanged(AXrLottieDrawable drawable, int frame);
```

OnFrameRenderListener: 
```java
void onUpdate(AXrLottieDrawable drawable, int frame, long timeDiff, boolean force);
Bitmap renderFrame(AXrLottieDrawable drawable, Bitmap bitmap, int frame);
```

## AnimatedSticker - AXEmojiView
you can create AXrLottieImageView in AXEmojiView/StickerView using this code :

```java
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
```
add this just after `AXEmojiManager.install`

and you can load your animations in StickerProvider
```java
  @Override
    public StickerLoader getLoader() {
        return new StickerLoader() {
            @Override
            public void onLoadSticker(View view, Sticker sticker) {
                if (view instanceof AXrLottieImageView && sticker instanceof AnimatedSticker) {
                    AXrLottieImageView lottieImageView = (AXrLottieImageView) view;
                    AnimatedSticker animatedSticker = (AnimatedSticker) sticker;
                    if (animatedSticker.drawable==null){
                        animatedSticker.drawable = Utils.createFromSticker(view.getContext(),animatedSticker,100);
                    }
                    lottieImageView.setLottieDrawable(animatedSticker.drawable);
                    lottieImageView.playAnimation();
                }
            }

            @Override
            public void onLoadStickerCategory(View view, StickerCategory stickerCategory, boolean selected) {
                if (view instanceof AXrLottieImageView) {
                    AXrLottieImageView lottieImageView = (AXrLottieImageView) view;
                    AnimatedSticker animatedSticker = (AnimatedSticker) stickerCategory.getCategoryData();
                    if (animatedSticker.drawable==null){
                        animatedSticker.drawable = Utils.createFromSticker(view.getContext(),animatedSticker,50);
                    }
                    lottieImageView.setLottieDrawable(animatedSticker.drawable);
                    //lottieImageView.playAnimation();
                }
            }
        };
    }
```

### Output
<img src="./images/screen.png" width=300 title="Screen">

## Author 
- **Amir Hossein Aghajari**

[Samsung/rlottie](https://github.com/Samsung/rlottie)

License
=======

    Copyright 2020 Amir Hossein Aghajari
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


<br><br>
<div align="center">
  <img width="64" alt="LCoders | AmirHosseinAghajari" src="https://user-images.githubusercontent.com/30867537/90538314-a0a79200-e193-11ea-8d90-0a3576e28a18.png">
  <br><a>Amir Hossein Aghajari</a> • <a href="mailto:amirhossein.aghajari.82@gmail.com">Email</a> • <a href="https://github.com/Aghajari">GitHub</a>
</div>
