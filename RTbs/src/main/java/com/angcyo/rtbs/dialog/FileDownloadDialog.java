package com.angcyo.rtbs.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.angcyo.rtbs.DownloadFileBean;
import com.angcyo.rtbs.R;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.skin.SkinHelper;
import com.angcyo.uiview.less.utils.RDialog;
import com.angcyo.uiview.less.utils.RUtils;
import com.angcyo.uiview.less.utils.T_;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/14
 */
public class FileDownloadDialog {

    /**
     * 下载文件的对话框
     */
    public static void show(@NonNull final Context context,
                            @Nullable final DownloadFileBean mDownloadFileBean,
                            @Nullable final OnDownloadListener downloadListener) {
        if (mDownloadFileBean == null) {
            return;
        }
        RDialog.build(context)
                .setContentLayoutId(R.layout.dialog_x5_file_download)
                .setInitListener(new RDialog.OnInitListener() {
                    @Override
                    public void onInitDialog(@NonNull final Dialog dialog, @NonNull RBaseViewHolder dialogViewHolder) {
                        String tempFilename;
                        if (TextUtils.isEmpty(mDownloadFileBean.fileName)) {
                            tempFilename = RUtils.getFileNameFromUrl(mDownloadFileBean.url);
                        } else {
                            tempFilename = mDownloadFileBean.fileName;
                        }

                        //            RUtils.trimMarks("s", "s");
                        //            RUtils.trimMarks("s", "S");
                        //            RUtils.trimMarks("ss", "s");
                        //            RUtils.trimMarks(fileName, "\"");

                        final String fileName = RUtils.trimMarks(tempFilename, "\"");

                        dialogViewHolder.tv(R.id.target_url_view).setText(mDownloadFileBean.url);
                        dialogViewHolder.tv(R.id.file_name_view).setText(fileName);
                        dialogViewHolder.tv(R.id.file_size_view).setText(RUtils.formatFileSize(mDownloadFileBean.fileSize));
                        dialogViewHolder.tv(R.id.file_type_view).setText(mDownloadFileBean.fileType);
                        dialogViewHolder.tv(R.id.download_button).setTextColor(SkinHelper.getSkin().getThemeSubColor());

                        dialogViewHolder.click(R.id.download_button, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //是否已经被处理
                                boolean handle = false;
                                if (downloadListener != null) {
                                    handle = downloadListener.onDownload(mDownloadFileBean);
                                }

                                if (!handle) {
                                    if (RUtils.downLoadFile(context, mDownloadFileBean.url, fileName) != -1) {
                                        T_.show("开始下载:" + fileName);
                                        //goBack();
                                    }
                                }

                                dialog.cancel();
                            }
                        });
                    }
                })
                .showAlertDialog();
    }

    public interface OnDownloadListener {
        /**
         * 需要拦截默认处理, 请返回true
         */
        boolean onDownload(DownloadFileBean bean);
    }
}
