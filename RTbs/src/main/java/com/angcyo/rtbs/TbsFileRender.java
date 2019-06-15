package com.angcyo.rtbs;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.view.ViewGroup;
import android.widget.TextView;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.utils.Root;
import com.angcyo.uiview.less.utils.utilcode.utils.FileUtils;
import com.luck.picture.lib.widget.longimage.ImageSource;
import com.luck.picture.lib.widget.longimage.ImageViewState;
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView;
import com.tencent.smtt.sdk.TbsReaderView;

/**
 * Tbs 文件预览服务 doc/excel/pdf等
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/15
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class TbsFileRender {
    ViewGroup parent;
    TbsReaderView tbsReaderView;

    private TbsFileRender(ViewGroup parent) {
        this.parent = parent;
    }

    public static TbsFileRender wrap(ViewGroup viewGroup) {
        return new TbsFileRender(viewGroup);
    }

    // 获取文件扩展名
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    public static String getName(String path) {
        if ((path != null) && (path.length() > 0)) {
            int dot = path.lastIndexOf('/');
            if ((dot > -1) && (dot < (path.length() - 1))) {
                return path.substring(dot + 1);
            }
        }
        return "unknown";
    }

    public boolean openFile(String path) {
        return openFile(path, getExtensionName(path));
    }

    public boolean openFile(String path, String type) {
        parent.removeAllViews();
        stop();
        tbsReaderView = new TbsReaderView(parent.getContext(), new TbsReaderView.ReaderCallback() {

            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {
                //L.w("文件浏览服务日志输出:$p0 $p1 $p2")
                RTbs.log("文件浏览服务日志输出:" + integer + " " + o + " " + o1);
            }
        });
        parent.addView(tbsReaderView, new ViewGroup.LayoutParams(-1, -1));
        if (tbsReaderView.preOpen(type, false)) {
            Bundle bundle = new Bundle();
            bundle.putString("filePath", path);
            bundle.putString("tempPath", Root.getAppExternalFolder("tbs"));
            tbsReaderView.openFile(bundle);
            return true;
        } else {
            L.e("不支持文件:" + path);
            RTbs.log("不支持文件:" + path);
            return false;
        }
    }

    public boolean openBitmap(String path) {
        parent.removeAllViews();

        //长图, 大图支持
        SubsamplingScaleImageView subsamplingScaleImageView = new SubsamplingScaleImageView(parent.getContext());
        //PhotoView photoView = new PhotoView(parent.getContext());

        parent.addView(subsamplingScaleImageView, new ViewGroup.LayoutParams(-1, -1));
        //Glide.with(parent.getContext()).load(path).into(photoView);

        subsamplingScaleImageView.setQuickScaleEnabled(true);
        subsamplingScaleImageView.setZoomEnabled(true);
        subsamplingScaleImageView.setPanEnabled(true);
        subsamplingScaleImageView.setDoubleTapZoomDuration(100);
        subsamplingScaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        subsamplingScaleImageView.setDoubleTapZoomDpi(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        subsamplingScaleImageView.setImage(ImageSource.uri(path), new ImageViewState(0, new PointF(0, 0), 0));

        return true;
    }

    public boolean openText(String path) {
        parent.removeAllViews();
        NestedScrollView scrollView = new NestedScrollView(parent.getContext());
        TextView textView = new TextView(parent.getContext());
        scrollView.addView(textView, new ViewGroup.LayoutParams(-1, -2));

        parent.addView(scrollView, new ViewGroup.LayoutParams(-1, -1));

        textView.setText(FileUtils.readFile2String(path, "UTF8"));
        return true;
    }

    private void stop() {
        if (tbsReaderView != null) {
            tbsReaderView.onStop();
        }
    }

    public void release() {
        stop();
    }
}
