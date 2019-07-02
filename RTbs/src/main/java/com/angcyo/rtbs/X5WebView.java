package com.angcyo.rtbs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.utils.RUtils;
import com.angcyo.uiview.less.utils.Reflect;
import com.angcyo.uiview.less.widget.IWebView;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.tencent.smtt.export.external.interfaces.*;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient.CustomViewCallback;
import com.tencent.smtt.sdk.*;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;

import java.util.HashMap;
import java.util.Map;

//import com.tencent.smtt.sdk.WebBackForwardList;
//import com.tencent.smtt.sdk.WebHistoryItem;
//import com.tencent.smtt.sdk.WebStorage;
//import com.tencent.smtt.sdk.WebViewDatabase;

public class X5WebView extends BridgeWebView implements IWebView {
    public static final int FILE_CHOOSER = 0;
    private static boolean isSmallWebViewDisplayed = false;
    RelativeLayout.LayoutParams layoutParams;
    TextView title;
    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String resourceUrl = "";
    private WebView smallWebView;
    private Map<String, Object> mJsBridges;
    private TextView tog;
    private RelativeLayout refreshRela;

    /**
     * Y轴滚动的距离, 用来下拉刷新判断使用
     */
    private int scrollY = 0;
    /**
     * 手指按下的坐标
     */
    private float mDownY;

    private int mLastDeltaY;
    private MyDownloadListener mDownloadListener;
    private OnOpenAppListener mOnOpenAppListener;
    private OnWebViewListener mOnWebViewListener;

    private WebChromeClient webChromeClient = new WebChromeClient() {

        View myVideoView;
        View myNormalView;
        CustomViewCallback callback;

        @Override
        public boolean onJsConfirm(WebView arg0, String arg1, String arg2, JsResult arg3) {
            return super.onJsConfirm(arg0, arg1, arg2, arg3);
        }

        ///////////////////////////////////////////////////////////
        //

        /**
         * 全屏播放配置
         */
        @Override
        public void onShowCustomView(View view, CustomViewCallback customViewCallback) {
//            FrameLayout normalView = (FrameLayout) ((Activity) getContext()).findViewById(R.id.web_filechooser);
//            ViewGroup viewGroup = (ViewGroup) normalView.getParent();
//            viewGroup.removeView(normalView);
//            viewGroup.addView(view);
//            myVideoView = view;
//            myNormalView = normalView;
//            callback = customViewCallback;
        }

        @Override
        public void onHideCustomView() {
//            if (callback != null) {
//                callback.onCustomViewHidden();
//                callback = null;
//            }
//            if (myVideoView != null) {
//                ViewGroup viewGroup = (ViewGroup) myVideoView.getParent();
//                viewGroup.removeView(myVideoView);
//                viewGroup.addView(myNormalView);
//            }
        }

        @Override
        public boolean onShowFileChooser(WebView arg0,
                                         ValueCallback<Uri[]> arg1, FileChooserParams arg2) {
            // TODO Auto-generated method stub
            L.e("X5WebView", "onShowFileChooser " + arg2);
            //返回true之后, openFileChooser 就不会被执行
            if (arg2 != null) {
                if (arg2.getAcceptTypes() == null ||
                        arg2.getAcceptTypes().length == 0 ||
                        TextUtils.isEmpty(arg2.getAcceptTypes()[0])) {
                    L.e("X5WebView", "onShowFileChooser 不支持的操作");
                    return true;
                }
            }
            return super.onShowFileChooser(arg0, arg1, arg2);
        }

        @Override
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String captureType) {
            L.e("X5WebView", "openFileChooser " + acceptType + " " + captureType);
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("*/*");
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            try {
//                ((Activity) (X5WebView.this.getContext())).startActivityForResult(Intent.createChooser(intent, "choose files"),
//                        1);
//            } catch (android.content.ActivityNotFoundException ex) {
//
//            }
            if (mOnWebViewListener != null) {
                if (mOnWebViewListener.onOpenFileChooser(uploadFile, acceptType, captureType)) {
                    return;
                }
            }
            super.openFileChooser(uploadFile, acceptType, captureType);
        }

        /**
         * webview 的窗口转移
         */
        @Override
        public boolean onCreateWindow(WebView arg0, boolean arg1, boolean arg2, Message msg) {
//            // TODO Auto-generated method stub
//            if (X5WebView.isSmallWebViewDisplayed == true) {
//
//                WebView.WebViewTransport webViewTransport = (WebView.WebViewTransport) msg.obj;
//                WebView webView = new WebView(X5WebView.this.getContext()) {
//
//                    protected void onDraw(Canvas canvas) {
//                        super.onDraw(canvas);
//                        Paint paint = new Paint();
//                        paint.setColor(Color.GREEN);
//                        paint.setTextSize(15);
//                        canvas.drawText("新建窗口", 10, 10, paint);
//                    }
//
//                    ;
//                };
//                webView.setWebViewClient(new WebViewClient() {
//                    public boolean shouldOverrideUrlLoading(WebView arg0, String arg1) {
//                        arg0.loadUrl(arg1);
//                        return true;
//                    }
//
//                    ;
//                });
//                LayoutParams lp = new LayoutParams(400, 600);
//                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
//                X5WebView.this.addView(webView, lp);
//                webViewTransport.setWebView(webView);
//                msg.sendToTarget();
//            }
//            return true;
            return super.onCreateWindow(arg0, arg1, arg2, msg);
        }

        @Override
        public boolean onJsAlert(WebView arg0, String arg1, String arg2, JsResult arg3) {
            /**
             * 这里写入你自定义的window alert
             */
            // AlertDialog.Builder build = new Builder(getContext());
            // build.setTitle("X5内核");
            // build.setPositiveButton("确定", new
            // DialogInterface.OnClickListener() {
            //
            // @Override
            // public void onClick(DialogInterface dialog, int which) {
            // // TODO Auto-generated method stub
            // dialog.dismiss();
            // }
            // });
            // build.show();
            // arg3.confirm();
            // return true;
            //Log.i("yuanhaizhou", "setX5webview = null");
            return super.onJsAlert(null, "www.baidu.com", "aa", arg3);
        }

        /**
         * 对应js 的通知弹框 ，可以用来实现js 和 android之间的通信
         */
        @Override
        public boolean onJsPrompt(WebView arg0, String arg1, String arg2, String arg3, JsPromptResult arg4) {
            // 在这里可以判定js传过来的数据，用于调起android native 方法
            if (X5WebView.this.isMsgPrompt(arg1)) {
                if (X5WebView.this.onJsPrompt(arg2, arg3)) {
                    return true;
                } else {
                    return false;
                }
            }
            return super.onJsPrompt(arg0, arg1, arg2, arg3, arg4);
        }

        @Override
        public void onReceivedTitle(WebView webView, final String title) {
            super.onReceivedTitle(webView, title);
            if (mOnWebViewListener != null) {
                mOnWebViewListener.onReceivedTitle(webView, title);
            }

        }

        @Override
        public void onProgressChanged(WebView webView, int progress) {
            super.onProgressChanged(webView, progress);
            if (mOnWebViewListener != null) {
                mOnWebViewListener.onProgressChanged(webView, progress);
            }
        }
    };

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            L.e("call: shouldOverrideUrlLoading([webView, url])-> " + url + " title:" + webView.getTitle());
            RTbs.log("title:" + webView.getTitle() + " url:" + url);

            if (mOnWebViewListener != null) {
                mOnWebViewListener.shouldOverrideUrlLoading(webView, url);
            }

            mBridgeWebViewClient.shouldOverrideUrlLoading(webView, url);

            if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                webView.loadUrl(url);

                //切换网页后, 重置
                lastMoveDy = 0;
                touchMoveDy = 0;
            } else if (!TextUtils.isEmpty(url)) {
                //RUtils.openAppFromUrl(getContext(), url);

                RUtils.QueryAppBean appBean = RUtils.queryIntentFromUrl(url);
                if (appBean != null) {
                    if (mOnOpenAppListener != null) {
                        mOnOpenAppListener.onOpenApp(appBean);
                    }
                }

//                Uri webPage = Uri.parse(url);
//                Intent webIntent = new Intent(Intent.ACTION_VIEW);
//                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                webIntent.setData(webPage);
//                //webIntent.setDataAndType(webPage, "application/vnd.android.package-archive");
//                try {
//                    getContext().startActivity(webIntent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
            return true;
        }

//        @Override
//        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
//            mBridgeWebViewClient.shouldOverrideUrlLoading(webView, webResourceRequest);
//            return shouldOverrideUrlLoading(webView, webResourceRequest.getUrl().toString());
//        }

        @Override
        public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
            super.onPageStarted(webView, s, bitmap);
            mBridgeWebViewClient.onPageStarted(webView, s, bitmap);
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            mBridgeWebViewClient.onPageFinished(webView, url);
            //L.e("call: onPageFinished([webView, url])-> ");
            if (mOnWebViewListener != null) {
                mOnWebViewListener.onPageFinished(webView, url);
            }
        }

        @Override
        public void onReceivedError(WebView webView, int i, String s, String s1) {
            super.onReceivedError(webView, i, s, s1);
            //L.e("call: onReceivedError([webView, i, s, s1])-> ");
        }

        @Override
        public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
            super.onReceivedError(webView, webResourceRequest, webResourceError);
            //L.e("call: onReceivedError([webView, webResourceRequest, webResourceError])-> ");
        }

        @Override
        public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
            super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
            //L.e("call: onReceivedHttpError([webView, webResourceRequest, webResourceResponse])-> ");
        }

        @Override
        public void onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest) {
            super.onReceivedClientCertRequest(webView, clientCertRequest);
            //L.e("call: onReceivedClientCertRequest([webView, clientCertRequest])-> ");
        }

        @Override
        public void onLoadResource(WebView webView, String s) {
            super.onLoadResource(webView, s);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            return super.shouldOverrideUrlLoading(webView, webResourceRequest);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
            return super.shouldInterceptRequest(webView, s);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
            return super.shouldInterceptRequest(webView, webResourceRequest);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
            return super.shouldInterceptRequest(webView, webResourceRequest, bundle);
        }

        //        /**
//         * 防止加载网页时调起系统浏览器
//         */
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.getSettings().setDefaultTextEncodingName("utf-8");
//            view.loadUrl(url);
//            return true;
//        }
//
//        public void onReceivedHttpAuthRequest(WebView webview,
//                                              com.tencent.smtt.export.external.interfaces.HttpAuthHandler httpAuthHandlerhost, String host,
//                                              String realm) {
//            boolean flag = httpAuthHandlerhost.useHttpAuthUsernamePassword();
//        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    public X5WebView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        initWebView();
    }

    public X5WebView(Context arg0) {
        super(arg0);
        initWebView();
    }

    public static void setSmallWebViewEnabled(boolean enabled) {
        isSmallWebViewDisplayed = enabled;
    }

    /**
     * 清除Cookie
     *
     * @param context
     */
    public static void removeCookie(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
    }

    protected void initWebView() {
        if (isInEditMode()) {
            return;
        }

        //setBackgroundColor(85621);
        //setOverScrollMode(View.OVER_SCROLL_NEVER);

        // 配置X5webview的事件处理
        this.setWebViewClientExtension(new X5WebViewEventHandler(this));
        this.setWebViewClient(webViewClient);
        this.setWebChromeClient(webChromeClient);
        //WebStorage webStorage = WebStorage.getInstance();
        initWebViewSettings();
        this.getView().setClickable(true);
        this.getView().setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        this.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url /*下载地址*/, String userAgent /*user agent*/,
                                        String contentDisposition, String mime /*文件mime application/zip*/,
                                        long length /*文件大小 kb*/) {
                L.e("call: onDownloadStart([url, userAgent, s2, s3, l])-> \n" + url + "\n" + userAgent + "\n" + contentDisposition + "\n" + mime + "\n" + length);
//                if (RUtils.downLoadFile(getContext(), url) != -1) {
//                    T_.show("正在下载文件:" + RUtils.getFileNameFromUrl(url));
//                    goBack();
//                }

                if (mDownloadListener != null) {
                    mDownloadListener.onDownloadStart(url, userAgent, contentDisposition, mime, length);
                }
            }
        });

        resetOverScrollMode();
    }

//    @Override
//    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
//        boolean ret = super.drawChild(canvas, child, drawingTime);
//        canvas.save();
//        Paint paint = new Paint();
//        paint.setColor(0x7fff0000);
//        paint.setTextSize(24.f);
//        paint.setAntiAlias(true);
//        if (getX5WebViewExtension() != null) {
//            canvas.drawText(this.getContext().getPackageName() + "-pid:" + android.os.Process.myPid(), 10, 50, paint);
//            canvas.drawText("X5  Core:" + QbSdk.getTbsVersion(this.getContext()), 10, 100, paint);
//        } else {
//            canvas.drawText(this.getContext().getPackageName() + "-pid:" + android.os.Process.myPid(), 10, 50, paint);
//            canvas.drawText("Sys Core", 10, 100, paint);
//        }
//        canvas.drawText(Build.MANUFACTURER, 10, 150, paint);
//        canvas.drawText(Build.MODEL, 10, 200, paint);
//        canvas.restore();
//        return ret;
//    }

    protected void resetOverScrollMode() {
        Object f = Reflect.getMember(WebView.class, this, "f");
        Object g = Reflect.getMember(WebView.class, this, "g");
        if (f instanceof IX5WebViewBase) {
            try {
                ((IX5WebViewBase) f).getView().setOverScrollMode(View.OVER_SCROLL_NEVER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (g instanceof View) {
            ((View) g).setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }

    public MyDownloadListener getMyDownloadListener() {
        return mDownloadListener;
    }

    public void setMyDownloadListener(MyDownloadListener downloadListener) {
        mDownloadListener = downloadListener;
    }

    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        webSetting.setDefaultTextEncodingName("utf-8");

        webSetting.setJavaScriptEnabled(true);
        //webSetting.setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);//星期二 2017-10-31
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(getContext().getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(getContext().getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(getContext().getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);

        webSetting.setUserAgent(webSetting.getUserAgentString() + " angcyo");

        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // webSetting.setPreFectch(true);
        long time = System.currentTimeMillis();
        CookieSyncManager.createInstance(getContext());
        CookieSyncManager.getInstance().sync();
    }

    public void addJavascriptBridge(SecurityJsBridgeBundle jsBridgeBundle) {
        if (this.mJsBridges == null) {
            this.mJsBridges = new HashMap<String, Object>(5);
        }

        if (jsBridgeBundle != null) {
            String tag = SecurityJsBridgeBundle.BLOCK + jsBridgeBundle.getJsBlockName() + "-"
                    + SecurityJsBridgeBundle.METHOD + jsBridgeBundle.getMethodName();
            this.mJsBridges.put(tag, jsBridgeBundle);
        }
    }

    /**
     * 当webchromeClient收到 web的prompt请求后进行拦截判断，用于调起本地android方法
     *
     * @param methodName 方法名称
     * @param blockName  区块名称
     * @return true ：调用成功 ； false ：调用失败
     */
    private boolean onJsPrompt(String methodName, String blockName) {
        String tag = SecurityJsBridgeBundle.BLOCK + blockName + "-" + SecurityJsBridgeBundle.METHOD + methodName;

        if (this.mJsBridges != null && this.mJsBridges.containsKey(tag)) {
            ((SecurityJsBridgeBundle) this.mJsBridges.get(tag)).onCallMethod();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判定当前的prompt消息是否为用于调用native方法的消息
     *
     * @param msg 消息名称
     * @return true 属于prompt消息方法的调用
     */
    private boolean isMsgPrompt(String msg) {
        if (msg != null && msg.startsWith(SecurityJsBridgeBundle.PROMPT_START_OFFSET)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 记录最后一次手势移动的距离
     */
    float lastMoveDy = 0;

    float touchMoveDy = 0;

    /**
     * 在有些页面, 不可以滚动, 但是手势move之后 tbs_onOverScrolled 没有回调.
     * 此时 重置 lastMoveDy=0
     */
    Runnable checkScrollRunnable = new Runnable() {
        @Override
        public void run() {
            lastMoveDy = 0;
        }
    };

    // TBS: Do not use @Override to avoid false calls
    public boolean tbs_dispatchTouchEvent(MotionEvent event, View view) {
        int actionMasked = MotionEventCompat.getActionMasked(event);
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mDownY = event.getY();
        } else if (actionMasked == MotionEvent.ACTION_MOVE) {
            float eventY = event.getY();
            float dy = mDownY - eventY;
            touchMoveDy = dy;

            if (dy < 0) {
                //本次向下滚动
                if (scrollY == 0) {
                    //H5内嵌滚动条判断
                    if (lastMoveDy > 0) {
                        //曾经向上滚动过

                    } else {
                        lastMoveDy = dy;
                    }
                } else {
                    lastMoveDy = dy;
                }
            } else {
                //手指向上滑动
                lastMoveDy = dy;
                removeCallbacks(checkScrollRunnable);
                postDelayed(checkScrollRunnable, 360);
            }
        } else if (actionMasked == MotionEvent.ACTION_UP) {
        }
        boolean r = super.super_dispatchTouchEvent(event);
        //L.d("Bran", "dispatchTouchEvent " + event.getAction() + " " + r);
        return r;
    }

    // TBS: Do not use @Override to avoid false calls
    public boolean tbs_onInterceptTouchEvent(MotionEvent ev, View view) {
        boolean r = super.super_onInterceptTouchEvent(ev);
        return r;
    }

    protected void tbs_onScrollChanged(int l, int t, int oldl, int oldt, View view) {
        super_onScrollChanged(l, t, oldl, oldt);
        if (mOnWebViewListener != null) {
            mOnWebViewListener.onScroll(l, t, l - oldl, t - oldt);
        }
    }

    /**
     * 当WebView在边界滚动的时候回调
     */
    protected void tbs_onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY, View view) {
//        if (getContext() instanceof RefreshActivity) {
//            if (this.tog == null) {
//                this.tog = (TextView) ((Activity) getContext()).findViewById(R.id.refreshText);
//                layoutParams = (RelativeLayout.LayoutParams) (this.tog.getLayoutParams());
//                this.refreshRela = (RelativeLayout) ((Activity) getContext()).findViewById(R.id.refreshPool);
//            }
//            if (isClampedY && !clampedY) {
//                this.reload();
//            }
//            if (clampedY) {
//                this.isClampedY = true;
//
//            } else {
//                this.isClampedY = false;
//            }
//        }
//        this.isClampedY = clampedY && scrollY == 0 && isScrollToBottom;
//        isStopInTop = /*clampedY &&*/ scrollY == 0 /*&& mLastDeltaY <= 0;*/;
//        L.e("tbs_onOverScrolled()-> " + scrollY + " " + clampedY + " " + mLastDeltaY);

        removeCallbacks(checkScrollRunnable);

        this.scrollY = scrollY;
        if (scrollY == 0) {
            //H5内嵌滚动条, 不管滚动到顶部和底部, scrollY 都是0
            if (touchMoveDy < 0) {
                //本次滚到0, 是由手指向下滚动触发的.
                lastMoveDy = 0;
            }
        }

        super_onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }


    protected void tbs_computeScroll(View view) {
        removeCallbacks(checkScrollRunnable);
        super_computeScroll();
    }

    /**
     * 当WebView滚动了多少距离时, 回调
     */
    protected boolean tbs_overScrollBy(int deltaX, int deltaY, /*本次滚动多少距离*/
                                       int scrollX, int scrollY, /*总共滚动了多少距离*/
                                       int scrollRangeX, int scrollRangeY, /*滚动的范围*/
                                       int maxOverScrollX, int maxOverScrollY,/**/
                                       boolean isTouchEvent, View view) {
//        if (getContext() instanceof RefreshActivity) {
//            if (this.isClampedY) {
//                if ((refreshRela.getTop() + (-deltaY)) / 2 < 255) {
//                    this.tog.setAlpha((refreshRela.getTop() + (-deltaY)) / 2);
//                } else
//                    this.tog.setAlpha(255);
//                this.refreshRela.layout(refreshRela.getLeft(), refreshRela.getTop() + (-deltaY), refreshRela.getRight(),
//                        refreshRela.getBottom() + (-deltaY));
//                this.layout(this.getLeft(), this.getTop() + (-deltaY) / 2, this.getRight(),
//                        this.getBottom() + (-deltaY) / 2);
//            }
//        }

        if (scrollY == 0) {
            if (mOnWebViewListener != null) {
                mOnWebViewListener.onOverScroll(deltaY);
            }
        }
        boolean overScrollBy = super_overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX,
                maxOverScrollY, isTouchEvent);

        //L.e("tbs_overScrollBy()-> " + deltaY + " " + scrollY + " " + scrollRangeY + " " + maxOverScrollY + " " + overScrollBy);

        return overScrollBy;
    }

    protected boolean tbs_onTouchEvent(MotionEvent event, View view) {
//        if (getContext() instanceof RefreshActivity) {
//            if (event.getAction() == MotionEvent.ACTION_UP && this.tog != null) {
//                this.isClampedY = false;
//                this.tog.setAlpha(0);
//                this.refreshRela.layout(refreshRela.getLeft(), 0, refreshRela.getRight(), refreshRela.getBottom());
//                this.layout(this.getLeft(), 0, this.getRight(), this.getBottom());
//            }
//
//        }
//        resetOverScrollMode();

        boolean touchEvent = super_onTouchEvent(event);
//        L.e("call: tbs_onTouchEvent([event, view])-> " + touchEvent);
        return touchEvent;
    }

    public OnWebViewListener getOnWebViewListener() {
        return mOnWebViewListener;
    }

    public void setOnWebViewListener(OnWebViewListener onWebViewListener) {
        mOnWebViewListener = onWebViewListener;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    /**
     * 顶部是否还可以滚动, 顶部是否还有距离滚动可以滚动
     */
    public boolean topCanScroll() {
        if (scrollY == 0) {
            //未滚动, 有2中情况.
            //1:真的是没有滚动
            //2:H5内嵌的滚动
            if (lastMoveDy > 0) {
                //针对第二种情况, 只要手指曾经向上滚动过, 就认为top不可以滚动
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        //L.e("call: onMeasure([widthMeasureSpec, heightMeasureSpec])-> " + measuredWidth + " " + measuredHeight);
        if (measuredHeight > 50_00_00) {
//            reload();
//            if (BuildConfig.SHOW_DEBUG) {
//                T_.show("[内容]正在重新加载...");
//            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
//        if (BuildConfig.DEBUG) {
//            canvas.save();
//            mPaint.setTextSize(30f);
//            mPaint.setColor(Color.RED);
//
//            int top = 0;
//            if (getParent() instanceof View) {
//                top = ((View) getParent()).getScrollY();
//            }
//
//            canvas.drawText("contentHeight:" + getContentHeight(), 10, top + 300, mPaint);
//            canvas.drawText("measureHeight:" + getMeasuredHeight(), 10, top + 600, mPaint);
//            canvas.drawText("screenHeight:" + ScreenUtil.screenHeight, 10, top + 900, mPaint);
//            canvas.restore();
//        }
        return result;
    }

    @Override
    public int getWebViewContentHeight() {
        return getContentHeight();
    }

    @Override
    public int getWebViewVerticalScrollRange() {
        return computeVerticalScrollRange();
    }

    public OnOpenAppListener getOnOpenAppListener() {
        return mOnOpenAppListener;
    }

    public void setOnOpenAppListener(OnOpenAppListener onOpenAppListener) {
        mOnOpenAppListener = onOpenAppListener;
    }

    /**
     * 设置Cookie
     * synCookies(this, "www.baidu.com", "age=20;sex=1;time=today");
     *
     * @param context
     * @param url
     * @param cookie  格式：uid=21233 如需设置多个，需要多次调用
     */
    public void synCookies(Context context, String url, String cookie) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(url, cookie);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();
    }

    public interface OnWebViewListener {
        void onScroll(int left, int top, int dx, int dy);

        void onOverScroll(int scrollY);

        void onPageFinished(@NonNull WebView webView, String url);

        void onReceivedTitle(@NonNull WebView webView, final String title);

        void onProgressChanged(@NonNull WebView webView, int progress);

        boolean onOpenFileChooser(@NonNull ValueCallback<Uri> uploadFile, String acceptType, String captureType);

        void shouldOverrideUrlLoading(WebView webView, String url);
    }

    public interface MyDownloadListener {
        void onDownloadStart(String url /*下载地址*/, String userAgent /*user agent*/,
                             String contentDisposition, String mime /*文件mime application/zip*/,
                             long length /*文件大小 kb*/);
    }

    public interface OnOpenAppListener {
        void onOpenApp(RUtils.QueryAppBean appBean);
    }
}
