package com.angcyo.rtbs;

import android.content.Context;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.utils.RUtils;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

/**
 * Created by angcyo on 2018/04/06 17:31
 */
public class RTbs {
    public static boolean DEBUG = BuildConfig.DEBUG;

    public static void init(Context context, boolean debug) {
        DEBUG = debug;
        //腾讯TBS X5内核浏览器初始化
        try {
            QbSdk.initX5Environment(context.getApplicationContext(), new QbSdk.PreInitCallback() {
                @Override
                public void onCoreInitFinished() {
                    L.i("腾讯X5 onCoreInitFinished");
                }

                @Override
                public void onViewInitFinished(boolean isX5Core) {
                    L.i("腾讯X5 onViewInitFinished isX5Core =" + isX5Core);
                }
            });

            QbSdk.setTbsListener(new TbsListener() {
                @Override
                public void onDownloadFinish(int i) {
                    L.i("腾讯X5 onDownloadFinish:" + i);
                }

                @Override
                public void onInstallFinish(int i) {
                    L.i("腾讯X5 onInstallFinish:" + i);
                }

                @Override
                public void onDownloadProgress(int i) {
                    L.i("腾讯X5 onDownloadProgress:" + i);
                }
            });

            L.i("腾讯X5 canLoadX5:" + QbSdk.canLoadX5(context) +
                    " isTbsCoreInited:" + QbSdk.isTbsCoreInited());

            QbSdk.checkTbsValidity(context);

        } catch (Exception e) {
            L.i("腾讯X5 " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建json参数
     */
    public static String createJSParams(String data, int result) {
        StringBuilder builder = new StringBuilder();
        builder.append("'{");

        builder.append("\"");
        if (result > 0) {
            builder.append("data");
        } else {
            builder.append("error");
        }

        builder.append("\"");
        builder.append(":");
        builder.append(data);
        builder.append(",");

        builder.append("\"");
        builder.append("result");
        builder.append("\"");
        builder.append(":");
        builder.append(result);

        builder.append("}'");
        return builder.toString();
    }

    public static void log(String log) {
        RUtils.saveToSDCard("webview.log", log);
    }
}
