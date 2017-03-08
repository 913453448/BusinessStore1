package com.businessstore.reactutils;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.JSCConfig;
import com.facebook.react.ReactApplication;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.cxxbridge.JSBundleLoader;
import com.facebook.react.cxxbridge.JSCJavaScriptExecutor;
import com.facebook.react.cxxbridge.JavaScriptExecutor;
import com.facebook.react.module.annotations.ReactModule;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.facebook.react.common.ReactConstants.TAG;

/**
 * Created by leo on 17/3/7.
 */
@ReactModule(name = "UpdateAndroid")
public class UpdateAndroid extends ReactContextBaseJavaModule {
    private UpdateTask task;

    public UpdateAndroid(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public void doUpdate(String url, Callback callback) {
        if (task == null) {
            task = new UpdateTask(callback);
            task.execute("index.android.bundle_2.0");
        }
    }

    @Override
    public String getName() {
        return "UpdateAndroid";
    }

    private class UpdateTask extends AsyncTask<String, Float, File> {
        private Callback callback;
        private static final String FILE_NAME = "index.android";

        private UpdateTask(Callback callback) {
            this.callback = callback;
        }

        @Override
        protected File doInBackground(String... params) {
            return downloadBundle(params[0]);
        }

        @Override
        protected void onProgressUpdate(Float... values) {
//            if (callback != null && values != null && values.length > 0){
//                callback.invoke(values[0]);
//                Log.e("TAG", "progress-->" + values[0]);
//            }
        }

        @Override
        protected void onPostExecute(File file) {
            if (callback != null) callback.invoke(100f);
            //重写初始化rn组件
            onJSBundleLoadedFromServer(file);
        }

        private void onJSBundleLoadedFromServer(File file) {
            if (file == null || !file.exists()) {
                Log.i(TAG, "download error, check URL or network state");
                return;
            }

            Log.i(TAG, "download success, reload js bundle");

            Toast.makeText(getCurrentActivity(), "Downloading complete", Toast.LENGTH_SHORT).show();
            try {
                ReactApplication application = (ReactApplication) getCurrentActivity().getApplication();
                Class<?> RIManagerClazz = application.getReactNativeHost().getReactInstanceManager().getClass();
                Method method = RIManagerClazz.getDeclaredMethod("recreateReactContextInBackground",
                        JavaScriptExecutor.Factory.class, JSBundleLoader.class);
                method.setAccessible(true);
                method.invoke(application.getReactNativeHost().getReactInstanceManager(),
                        new JSCJavaScriptExecutor.Factory(JSCConfig.EMPTY.getConfigMap()),
                        JSBundleLoader.createFileLoader(file.getAbsolutePath()));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        /**
         * 模拟bundle下载链接url
         *
         * @param url
         */
        private File downloadBundle(String url) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return null;
            }
            //删除以前的文件
            File file = new File(getReactApplicationContext().getExternalCacheDir(), FILE_NAME);
            if (file != null && file.length() > 0) {
                file.delete();
            }
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                //模拟网络下载过程，我这直接放在了assert目录了
                long size = getReactApplicationContext().getAssets().open(url).available();
                bis = new BufferedInputStream(getReactApplicationContext().getAssets().open(url));
                bos = new BufferedOutputStream(new FileOutputStream(file));
                int len = -1;
                long total = 0;
                byte[] buffer = new byte[100];
                while ((len = bis.read(buffer)) != -1) {
                    total += len;
                    bos.write(buffer, 0, len);
                    bos.flush();
                    float progress = total * 1.0f / size;
                    publishProgress(progress);
                }
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
