package com.angcyo.camera;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;
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

    PreviewPictureLayoutControl previewPictureLayoutControl;
    PreviewVideoLayoutControl previewVideoLayoutControl;
    /**
     * 总录制时长, 秒
     */
    int recordTime = 0;
    private SizeSurfaceView recordView;
    private RecordVideoControl recordControl;
    private RecordVideoCallback callback;

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
        viewHolder.itemView.setKeepScreenOn(true);
        viewHolder.postDelay(360, new Runnable() {
            @Override
            public void run() {
                onDelayInitView();
            }
        });
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
            public void onTick(@NotNull ExpandRecordLayout layout) {
                super.onTick(layout);
                //拍照
                recordControl.takePhoto();
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
            File outputFile = new File(Root.getAppExternalFolder("camera"), Root.createFileName(".jpg"));
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

            ExKt.save(bitmap, outputFile.getAbsolutePath(), Bitmap.CompressFormat.JPEG, 70);
            showPhotoPreview(bitmap, outputFile);

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
}