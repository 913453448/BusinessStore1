package com.businessstore.reactutils;

import android.os.Build;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.module.annotations.ReactModule;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by leo on 17/1/10.
 */
/**
 * {@link NativeModule} that allows changing the appearance of the status bar.
 */
@ReactModule(name = "VersionAndroid")
public class VersionAndroid extends ReactContextBaseJavaModule {
    private static final String SDK_INT  = "SDK_INT";
    private static final String SDK  = "SDK";
    public VersionAndroid(ReactApplicationContext reactContext) {
        super(reactContext);
    }
    @Override
    public @Nullable
    Map<String, Object> getConstants() {
        final Map<String, Object> constants = MapBuilder.newHashMap();
        int sdk_int=Build.VERSION.SDK_INT;
        String sdk=Build.VERSION.SDK;
        constants.put(SDK,sdk);
        constants.put(SDK_INT,sdk_int);
        return constants;
    }
    @Override
    public String getName() {
        return "VersionAndroid";
    }
}
