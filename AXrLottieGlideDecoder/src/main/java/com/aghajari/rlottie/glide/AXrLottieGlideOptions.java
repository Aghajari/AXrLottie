/*
 * Copyright (C) 2021 - Amir Hossein Aghajari
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


package com.aghajari.rlottie.glide;

import com.aghajari.rlottie.AXrLottieOptions;
import com.bumptech.glide.load.Option;

/**
 * @author Amir Hossein Aghajari
 * @version 1.4.0
 */
public final class AXrLottieGlideOptions {

    /**
     * [Required]
     * Determines whether the request is related to AXrLottie or not
     */
    public static final Option<Boolean> ENABLED = Option.memory(
            "com.aghajari.rlottie.glide#Enabled", false);

    /**
     * Used only to specify the cache path
     */
    public static final Option<Boolean> NETWORK = Option.memory(
            "com.aghajari.rlottie.glide#Network", true);

    /**
     * Specifies the type of file format.
     * Default : .json
     */
    public static final Option<String> EXTENSION = Option.memory(
            "com.aghajari.rlottie.glide#Extension", ".json");

    /**
     * [Required]
     * Specifies the cache name.
     */
    public static final Option<String> NAME = Option.memory(
            "com.aghajari.rlottie.glide#Name", null);

    /**
     * Determines whether the animation starts immediately or not
     * Default : TRUE
     */
    public static final Option<Boolean> AUTO_START = Option.memory(
            "com.aghajari.rlottie.glide#AutoStart", true);

    /**
     * Customizes the size of the lottie drawable
     */
    public static final Option<Integer> SIZE = Option.memory(
            "com.aghajari.rlottie.glide#Size", -1);

    /**
     * Can be used to activate other AXrLottie options
     * {@link AXrLottieOptions}
     */
    public static final Option<AXrLottieOptions> OPTIONS = Option.memory(
            "com.aghajari.rlottie.glide#Options", null);

}
