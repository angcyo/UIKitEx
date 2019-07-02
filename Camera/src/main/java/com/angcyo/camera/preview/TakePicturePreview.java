package com.angcyo.camera.preview;

import android.content.Context;
import android.graphics.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.angcyo.camera.PreviewPictureLayoutControl;
import com.angcyo.camera.R;
import com.angcyo.camera.control.CameraThreadPool;
import com.angcyo.camera.control.ICameraControl;
import com.angcyo.camera.util.ImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class TakePicturePreview extends CameraPreviewView {

    ImageView takePhotoButton, lightButton;

    PreviewPictureLayoutControl previewPictureLayoutControl;

    OnTakePictureCallback onTakePictureCallback;

    File outputFile;

    /**
     * 拍照后, 是否需要确定
     */
    boolean needConfirm = true;
    Bitmap takePicture;

    public TakePicturePreview(Context context) {
        super(context);
    }

    public TakePicturePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 保存图片到指定文件
     */
    public static boolean saveBitmap(File outputFile, Bitmap bitmap) {
        boolean result = true;
        try {
            if (!outputFile.exists()) {
                result = outputFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * 拍摄后的照片。需要进行裁剪。有些手机（比如三星）不会对照片数据进行旋转，而是将旋转角度写入EXIF信息当中，
     * 所以需要做旋转处理。
     *
     * @param outputFile 写入照片的文件。
     * @param data       原始照片数据。
     * @param rotation   照片exif中的旋转角度。
     * @return 裁剪好的bitmap。
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Bitmap crop(File outputFile, byte[] data, int rotation) {
        try {
            // BitmapRegionDecoder不会将整个图片加载到内存。
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(data, 0, data.length, true);

            Rect region = new Rect();
            region.left = 0;
            region.top = 0;
            region.right = decoder.getWidth();
            region.bottom = decoder.getHeight();

            BitmapFactory.Options options = new BitmapFactory.Options();

            // 最大图片大小。
            int maxPreviewImageSize = 2560;
            int size = Math.min(decoder.getWidth(), decoder.getHeight());
            size = Math.min(size, maxPreviewImageSize);

            options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size);
            options.inScaled = true;
            options.inDensity = Math.max(options.outWidth, options.outHeight);
            options.inTargetDensity = size;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = decoder.decodeRegion(region, options);

            if (rotation != 0) {
                // 只能是裁剪完之后再旋转了。有没有别的更好的方案呢？
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                if (bitmap != rotatedBitmap) {
                    // 有时候 createBitmap会复用对象
                    bitmap.recycle();
                }
                bitmap = rotatedBitmap;
            }
            if (outputFile == null) {
                return bitmap;
            }

            saveBitmap(outputFile, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 监听拍照
     */
    public void setOnTakePictureCallback(OnTakePictureCallback onTakePictureCallback) {
        this.onTakePictureCallback = onTakePictureCallback;
    }

    /**
     * 设置照片输出文件
     */
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setNeedConfirm(boolean needConfirm) {
        this.needConfirm = needConfirm;
    }

    @Override
    protected void init() {
        super.init();

        inflate(getContext(), R.layout.camera_take_picture, this);

        //拍照
        takePhotoButton = findViewById(R.id.camera_take_picture);
        takePhotoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onTakePicture();
            }
        });

        //闪光灯
        lightButton = findViewById(R.id.camera_light_button);
        lightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFlashMode();
                updateFlashMode();
            }
        });

        //确认
        findViewById(R.id.camera_confirm_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onTakePictureCallback != null) {
                    onTakePictureCallback.onPictureTaken(takePicture, outputFile);
                }
            }
        });

        previewPictureLayoutControl = new PreviewPictureLayoutControl(findViewById(R.id.camera_confirm_layout));

        //取消
//        findViewById(R.id.camera_cancel_button).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                findViewById(R.id.camera_confirm_layout).setVisibility(View.GONE);
//            }
//        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void updateFlashMode() {
        int flashMode = cameraControl.getFlashMode();
        if (flashMode == ICameraControl.FLASH_MODE_TORCH) {
            lightButton.setImageResource(R.drawable.bd_ocr_light_on);
        } else {
            lightButton.setImageResource(R.drawable.bd_ocr_light_off);
        }
    }

    protected void onTakePicture() {
        cameraControl.takePicture(new ICameraControl.OnTakePictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data) {
                CameraThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final int rotation = ImageUtil.getOrientation(data);
                        takePicture = crop(outputFile, data, rotation);
                        post(new Runnable() {
                            @Override
                            public void run() {
                                if (needConfirm) {
                                    previewPictureLayoutControl.showPreview(takePicture);
                                } else if (onTakePictureCallback != null) {
                                    onTakePictureCallback.onPictureTaken(takePicture, outputFile);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 照相回调
     */
    public interface OnTakePictureCallback {
        /**
         * 主线程回调
         */
        void onPictureTaken(@NonNull Bitmap bitmap, @Nullable File outputFile);
    }

}
