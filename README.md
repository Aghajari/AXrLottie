<div align="center">
  <br><b>AXrLottie</b> Renders animations<br>and vectors exported in the bodymovin JSON format
  <br><a href="https://github.com/Aghajari/AXrLottie">GitHub</a> • <a href="https://github.com/Aghajari/AXrLottie/releases">Releases</a>
  <br><br><img width="40" alt="LCoders | AmirHosseinAghajari" src="https://user-images.githubusercontent.com/30867537/90538314-a0a79200-e193-11ea-8d90-0a3576e28a18.png"><br><img width="250" alt="picker" src="https://github.com/Samsung/rlottie/raw/master/.Gifs/logo.png">
</div>


What is **lottie**?

> Lottie loads and renders animations and vectors exported in the bodymovin JSON format. Bodymovin JSON can be created and exported from After Effects with bodymovin, Sketch with Lottie Sketch Export, and from Haiku.
>
> For the first time, designers can create and ship beautiful animations without an engineer painstakingly recreating it by hand. Since the animation is backed by > JSON they are extremely small in size but can be large in complexity!

What is [**rlottie**](https://github.com/Samsung/rlottie)?

> rlottie is a platform independent standalone c++ library for rendering vector based animations and art in realtime.

What is **AXrLottie**?

**AXrLottie** includes [rlottie](https://github.com/Samsung/rlottie) into the Android. Easy A!

## Lottie Examples
<img height=200 src="https://github.com/airbnb/lottie-ios/blob/master/_Gifs/Examples1.gif">
<img height=250 src="./images/lottie.gif">

## Screenshot
<img src="./images/screen.png" width=300 title="Screen"> <img src="./images/editor.png" width=300 title="Screen">

## Table of Contents  
- [Installation](#installation)  
- [Usage](#usage)
  - [Install AXrLottie](#install-axrlottie)
  - [Basic Usage](#basic-usage)
  - [LayerProperty](#layerproperty)
    - [KeyPath](#keypath)
    - [Properties](#properties)
  - [Layers](#layers)
  - [Markers](#markers)
  - [Lottie2Gif](#lottie2gif)
  - [Listeners](#listeners)
- [AnimatedSticker (AXEmojiView)](#animatedsticker---axemojiview)
- [Author](#author)
- [License](#license)

## Installation
AXrLottie is available in the JCenter, so you just need to add it as a dependency (Module gradle)

Gradle
```gradle
implementation 'com.aghajari.rlottie:AXrLottie:1.0.2'
```

Maven
```xml
<dependency>
  <groupId>com.aghajari.rlottie</groupId>
  <artifactId>AXrLottie</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```

## Changelogs

**1.0.2 :**
- Updated to the latest version of [rlottie](https://github.com/Samsung/rlottie)
- [AXrLottieMarker](#markers) added.
- StrokeColor added to AXrLottieProperty.
- configureModelCacheSize added to AXrLottie. (Method)
- Now AXrLottieLayerInfo contains the type of layer.
- Speed, RepeatMode(RESTART,REVERSE), AutoRepeatCount, CustomStartFrame, Marker added to AXrLottieDrawable.
- onRpeat, onStart, onStop, onRecycle added to listener.
- Some improvements & Bugs fixed

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
- JSON (String)
- URL
- Assets
- Resource
- InputStram

lottie will cache animations/files by default.
you can disable cache in AXrLottieDrawable Builder

### Output
<img src="./images/simple.gif" width=300 title="Screen">

[Back to contents](#table-of-contents)

## LayerProperty

To update a property at runtime, you need 3 things:
1. KeyPath
2. AXrLottieProperty
3. setLayerProperty(KeyPath, AXrLottieProperty)

```java
lottieDrawable.setLayerProperty("**" /**KeyPath*/, AXrLottieProperty.colorProperty(color) /**AXrLottieProperty*/);
```
### Output
<img src="./images/layer.gif" width=300 title="Screen">

### KeyPath

A KeyPath is used to target a specific content or a set of contents that will be updated. A KeyPath is specified by a list of strings that correspond to the hierarchy of After Effects contents in the original animation.
KeyPaths can include the specific name of the contents or wildcards:
- Wildcard *
	- Wildcards match any single content name in its position in the keypath.
- Globstar **
	- Globstars match zero or more layers.
  
Keypath should contains object names separated by (.) and can handle globe(`**`) or wildchar(`*`).
- To change the property of fill1 object in the layer1->group1->fill1 : KeyPath = `layer1.group1.fill1`
- If all the property inside group1 needs to be changed : KeyPath = `**.group1.**`

### Properties
- FillColor
- FillOpacity
- StrokeColor
- StrokeOpacity
- StrokeWidth
- TrAnchor
- TrOpacity
- TrPosition
- TrRotation
- TrScale

[Back to contents](#table-of-contents)

## Layers
AXrLottieLayerInfo contains Layer's name,type,startFrame and endFrame.

```java
for (AXrLottieLayerInfo layerInfo : lottieDrawable.getLayers()) {
    Log.i("AXrLottie", layerInfo.toString());
}
```

[Back to contents](#table-of-contents)

## Markers
<img src="./images/marker.png" width=300 title="Screen">

Markers exported form AE are used to describe a segment of an animation {comment/tag , startFrame, endFrame} 
Marker can be use to divide a resource in to separate animations by tagging the segment with comment string ,
start frame and duration of that segment.

[More...](https://helpx.adobe.com/after-effects/using/layer-markers-composition-markers.html)

```java
for (AXrLottieMarker marker : lottieDrawable.getMarkers()) {
    Log.i("AXrLottie", marker.toString());
}
```

You can select a marker in AXrLottieDrawable and set start&end frame of the animation with an AXrLottieMarker :
```java
lottieDrawable.selectMarker(MARKER);
```

[Back to contents](#table-of-contents)

## Lottie2Gif
<img src="./images/gif.png" width=300 title="Screen">

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
<img height=250 src="./images/lottie2.gif">

*[lottie.gif](./images/lottie.gif) has been exported by AXrLottie2Gif*

[Back to contents](#table-of-contents)

## Listeners
OnFrameChangedListener:
```java
void onFrameChanged(AXrLottieDrawable drawable, int frame);
void onRepeat (int repeatedCount,boolean lastFrame);
void onStop();
void onStart();
void onRecycle();
```

OnFrameRenderListener: 
```java
void onUpdate(AXrLottieDrawable drawable, int frame, long timeDiff, boolean force);
Bitmap renderFrame(AXrLottieDrawable drawable, Bitmap bitmap, int frame);
```

[Back to contents](#table-of-contents)

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

[Back to contents](#table-of-contents)

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
