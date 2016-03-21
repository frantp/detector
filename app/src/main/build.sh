#!/bin/bash

export OPENCV_ANDROID_SDK=/home/fran/OpenCV-android-sdk
/home/fran/Android/Sdk/ndk-bundle/ndk-build NDK_LIBS_OUT=./jniLibs $@
