package com.angcyo.rtbs.dialog;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import com.angcyo.rtbs.R;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.utils.RDialog;
import com.angcyo.uiview.less.utils.RUtils;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/14
 */
public class OpenAppDialog {
    /**
     * 打开APP对话框
     */
    public static void show(@NonNull final Context context,
                            final RUtils.QueryAppBean appBean) {
        RDialog.build(context)
                .setContentLayoutId(R.layout.base_dialog_open_app_layout)
                .setInitListener(new RDialog.OnInitListener() {
                    @Override
                    public void onInitDialog(@NonNull final Dialog dialog, @NonNull RBaseViewHolder dialogViewHolder) {
                        dialogViewHolder.imageView(R.id.app_ico_view).setImageDrawable(appBean.mAppInfo.appIcon);
                        dialogViewHolder.visible(R.id.base_dialog_top_content_view, true)
                                .tv(R.id.base_dialog_top_content_view)
                                .setText(appBean.mAppInfo.appName);
                        
                        dialogViewHolder.tv(R.id.base_dialog_content_view).setText("请求打开应用");

                        dialogViewHolder.text(R.id.base_dialog_cancel_view, "取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                        dialogViewHolder.text(R.id.base_dialog_ok_view, "打开", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                                context.startActivity(appBean.startIntent);
                            }
                        });
                    }
                })
                .showAlertDialog();
    }
}
