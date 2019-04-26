package com.angcyo.tesstwo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.angcyo.lib.L;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class Tesstwo {
    //识别语言英文
    static final String DEFAULT_LANGUAGE = "eng";
    private static final String TAG = "tesstwo";
    //训练数据路径，必须包含tesseract文件夹
    static String TESSBASE_PATH;
    final Object lock = new Object();
    Thread parseThread;
    volatile Bitmap frame;
    OnResultCallback onResultCallback;

    NetOcr netOcr;

    AtomicBoolean cancel = new AtomicBoolean(false);

    public Tesstwo() {
        netOcr = new NetOcr();
        netOcr.onResultCallback = new OnResultCallback() {
            @Override
            public void onResult(@Nullable Bitmap cardBitmap, @Nullable Bitmap cardNoBitmap, @NonNull String idCardNo) {
                cancel.set(true);
                if (onResultCallback != null) {
                    onResultCallback.onResult(null, null, idCardNo);
                }
            }
        };
        parseThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (!isInterrupted()) {
                    synchronized (lock) {
                        if (frame == null) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    long startTime = System.currentTimeMillis();
                    localreInner(frame);
                    long endTime = System.currentTimeMillis();
                    L.e("耗时:" + (endTime - startTime));
                    if (frame != null) {
                        frame.recycle();
                        frame = null;
                    }
                }
            }
        };
        parseThread.start();
    }

    public static void init(Context context) {
        TESSBASE_PATH = context.getCacheDir().getAbsolutePath();
        new AssestUtils(context, TESSBASE_PATH).init();
    }

    static int dip2px(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * density + 0.5);
    }

    /**
     * @param bitmap 包含身份证轮廓的照片
     */
    public void localre(Bitmap bitmap) {
        if (cancel.get()) {
            return;
        }

        if (frame == null) {
            if (!netOcr.isRequestIng()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                netOcr.uploadAndRecognize(outputStream.toByteArray());
            }

//            frame = bitmap;
//            synchronized (lock) {
//                lock.notify();
//            }
        } else {
            //L.d("pass..." + Thread.currentThread().getName());
        }
    }

    public void localreInner(Bitmap bitmap) {
        try {
            if (bitmap == null) {
                return;
            }

            int x, y, w, h;
            x = (int) (bitmap.getWidth() * 0.330);
            y = (int) (bitmap.getHeight() * 0.750);
            w = (int) (bitmap.getWidth() * 0.6 + 0.5f);
            h = (int) (bitmap.getHeight() * 0.12 + 0.5f);

            //定位到身份证号码的位置
            Bitmap numBitmap = Bitmap.createBitmap(bitmap, x, y, w, h);
            Bitmap bm = numBitmap.copy(Bitmap.Config.ARGB_8888, true);

            String content = null;

            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.init(TESSBASE_PATH, DEFAULT_LANGUAGE);
            //设置识别模式
            baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);//PSM_SINGLE_LINE
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789Xx");
            //设置要识别的图片
            bm = ImageFilter.gray2Binary(bm);
            bm = ImageFilter.grayScaleImage(bm);
            baseApi.setImage(bm);
            content = baseApi.getUTF8Text();

            L.d("localre: " + content);

            if (!TextUtils.isEmpty(content) && IDCardUtil.isIdCard(content) && !cancel.get()) {
                cancel.set(true);
                if (onResultCallback != null) {
                    onResultCallback.onResult(bitmap, bm, content);
                }
            } else {
                bitmap.recycle();
                numBitmap.recycle();
                bm.recycle();
            }
            baseApi.clear();
            baseApi.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        parseThread.interrupt();
        parseThread = null;
    }

    public void setOnResultCallback(OnResultCallback onResultCallback) {
        this.onResultCallback = onResultCallback;
    }

    public interface OnResultCallback {
        /**
         * @param cardBitmap   身份证图片
         * @param cardNoBitmap 身份证中, 身份证号码图片
         * @param idCardNo     15/18位 有效的身份证号码
         */
        void onResult(@Nullable Bitmap cardBitmap, @Nullable Bitmap cardNoBitmap, @NonNull String idCardNo);
    }
}
