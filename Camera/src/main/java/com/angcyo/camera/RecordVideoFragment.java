package com.angcyo.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import com.angcyo.camera.record.RecordVideoControl;
import com.angcyo.camera.record.RecordVideoInterface;
import com.angcyo.camera.record.SizeSurfaceView;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.base.BaseFragment;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.kotlin.ExKt;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.utils.Root;
import com.angcyo.uiview.less.utils.TopToast;
import com.angcyo.uiview.less.utils.utilcode.utils.FileUtils;
import com.angcyo.uiview.less.widget.group.ExpandRecordLayout;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class RecordVideoFragment extends BaseFragment implements RecordVideoInterface {

    public RecordVideoCallback callback;
    PreviewPictureLayoutControl previewPictureLayoutControl;
    PreviewVideoLayoutControl previewVideoLayoutControl;
    /**
     * 总录制时长, 秒
     */
    int recordTime = 0;
    private SizeSurfaceView recordView;
    private RecordVideoControl recordControl;

    public static RecordVideoFragment show(FragmentManager fragmentManager, RecordVideoCallback callback) {
        RecordVideoFragment fragment = new RecordVideoFragment();
        fragment.callback = callback;
        FragmentHelper.build(fragmentManager)
                .defaultEnterAnim()
                .showFragment(fragment)
                .doIt();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_fragment_record_video;
    }

    @Override
    protected void initBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.initBaseView(viewHolder, arguments, savedInstanceState);
        viewHolder.enable(R.id.record_control_layout, false);

        viewHolder.itemView.setKeepScreenOn(true);

        if (haveCameraPermission()) {
            viewHolder.postDelay(360, new Runnable() {
                @Override
                public void run() {
                    onDelayInitView();
                }
            });
        } else {
            requestPermission();
        }
    }

    private void onDelayInitView() {
        recordView = baseViewHolder.v(R.id.recorder_view);
        previewPictureLayoutControl = new PreviewPictureLayoutControl(baseViewHolder.view(R.id.camera_confirm_layout));
        previewVideoLayoutControl = new PreviewVideoLayoutControl(baseViewHolder.view(R.id.video_confirm_layout));

        recordControl = new RecordVideoControl(requireActivity(), recordView, RecordVideoFragment.this);

        if (!isFragmentHide()) {
            recordControl.surfaceChanged(recordView.getHolder(), 0, recordView.getWidth(), recordView.getHeight());
        }

        ExpandRecordLayout recordLayout = baseViewHolder.v(R.id.record_control_layout);
        recordLayout.setMaxTime(callback.maxRecordTime);
        recordLayout.setListener(new ExpandRecordLayout.OnRecordListener() {

            @Override
            public boolean onTouchDown() {
                if (checkPermission()) {
                    baseViewHolder.enable(R.id.record_control_layout, true);
                    return true;
                }
                baseViewHolder.enable(R.id.record_control_layout, false);
                requestPermission();
                return false;
            }

            @Override
            public void onTick(@NotNull ExpandRecordLayout layout) {
                if (callback.isOnlyTakeVideo) {
                } else {
                    //拍照
                    recordControl.takePhoto();
                }
            }

            @Override
            public void onRecordStart() {
                super.onRecordStart();
                if (!recordControl.isRecording()) {
                    recordTime = 0;
                    recordControl.startRecording(Root.createExternalFilePath("camera", Root.createFileName(".mp4")));
                }
            }

            @Override
            public void onRecording(int progress) {
                super.onRecording(progress);
            }

            @Override
            public void onRecordEnd(int progress) {
                super.onRecordEnd(progress);
                recordTime = progress;
                recordControl.stopRecording(progress >= callback.minRecordTime);
            }

            @Override
            public void onExpandStateChange(int fromState, int toState) {
                super.onExpandStateChange(fromState, toState);
            }
        });
        recordLayout.setEnableLongPress(!callback.isOnlyTakePhoto);

        if (callback.isOnlyTakePhoto) {
            recordLayout.setDrawTipString("轻触拍照");
        } else if (callback.isOnlyTakeVideo) {
            recordLayout.setDrawTipString("长按摄像");
        } else {
            recordLayout.setDrawTipString("轻触拍照, 长按摄像");
        }
    }

    @Override
    public void startRecord() {
    }

    @Override
    public void onRecording(long recordTime) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        baseViewHolder.gone(R.id.camera_confirm_layout);
        baseViewHolder.gone(R.id.video_confirm_layout);
        if (previewVideoLayoutControl != null) {
            previewVideoLayoutControl.stop();
        }
    }

    @Override
    public void onRecordFinish(final String videoPath) {
        File targetFile = new File(videoPath);
        String newFileName = Root.createFileName("_t_" + recordTime + ".mp4");
        final String newFilePath = targetFile.getParent() + File.separator + newFileName;
        FileUtils.rename(targetFile, newFileName);
        //L.i("文件:" + new File(videoPath).exists());
        previewVideoLayoutControl.showPreview(newFilePath, new Runnable() {
            @Override
            public void run() {
                if (!isFragmentHide()) {
                    recordControl.surfaceChanged(recordView.getHolder(), 0, recordView.getWidth(), recordView.getHeight());
                }
            }
        });

        baseViewHolder.click(R.id.video_confirm_button, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backFragment(false);
                callback.onTakeVideo(newFilePath);
            }
        });
    }

    @Override
    public void onRecordError() {
        TopToast.INSTANCE.show("至少需要录制 " + callback.minRecordTime + " 秒", -1);

        if (!isFragmentHide()) {
            recordControl.surfaceChanged(recordView.getHolder(), 0, recordView.getWidth(), recordView.getHeight());
        }
    }

    @Override
    public void onTakePhoto(byte[] data) {
        L.i("onTakePhoto");
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            int displayOrientation = recordControl.getDisplayOrientation();
            if (displayOrientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(displayOrientation);
                Bitmap rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                if (bitmap != rotatedBitmap) {
                    // 有时候 createBitmap会复用对象
                    bitmap.recycle();
                }
                bitmap = rotatedBitmap;
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            StringBuilder builder = new StringBuilder();
            builder.append("_s_");
            builder.append(width);
            builder.append("x");
            builder.append(height);
            builder.append(".jpg");
            File outputFile = new File(Root.getAppExternalFolder("camera"), Root.createFileName(builder.toString()));

            bitmap = callback.onTakePhotoBefore(bitmap, width, height);

            showPhotoPreview(bitmap, outputFile);

            bitmap = callback.onTakePhotoAfter(bitmap, width, height);

            ExKt.save(bitmap, outputFile.getAbsolutePath(), Bitmap.CompressFormat.JPEG, 70);

//            Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            Uri fileContentUri = Uri.fromFile(mediaFile);
//            mediaScannerIntent.setData(fileContentUri);
//            getActivity().sendBroadcast(mediaScannerIntent);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void showPhotoPreview(final Bitmap bitmap, final File outputFile) {
        previewPictureLayoutControl.showPreview(bitmap);
        baseViewHolder.click(R.id.camera_confirm_button, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backFragment(false);
                callback.onTakePhoto(bitmap, outputFile);
            }
        });
    }

    private boolean haveCameraPermission() {
        return ActivityCompat.checkSelfPermission(mAttachContext, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 是否有权限
     */
    private boolean checkPermission() {
        if (haveCameraPermission() &&
                ActivityCompat.checkSelfPermission(mAttachContext, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        }
        return false;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                999);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermission()) {
            onDelayInitView();
        }
    }
}
