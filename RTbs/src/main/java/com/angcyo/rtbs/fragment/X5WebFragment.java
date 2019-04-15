package com.angcyo.rtbs.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.angcyo.lib.L;
import com.angcyo.rtbs.*;
import com.angcyo.rtbs.dialog.FileDownloadDialog;
import com.angcyo.rtbs.dialog.OpenAppDialog;
import com.angcyo.uiview.less.base.BaseTitleFragment;
import com.angcyo.uiview.less.base.helper.TitleItemHelper;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.resources.ResUtil;
import com.angcyo.uiview.less.utils.RSheetDialog;
import com.angcyo.uiview.less.utils.RUtils;
import com.angcyo.uiview.less.widget.EmptyView;
import com.angcyo.uiview.less.widget.SimpleProgressBar;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.impl.ScrollBoundaryDeciderAdapter;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：浏览网页的界面
 * 创建人员：Robi
 * 创建时间：2017/02/15 17:23
 * 修改人员：Robi
 * 修改时间：2017/02/15 17:23
 * 修改备注：
 * Version: 1.0.0
 */
public class X5WebFragment extends BaseTitleFragment {


    //<editor-fold desc="定制界面的扩展参数">

    /**
     * 扩展参数:
     * 目标url
     */
    public static final String KEY_TARGET_URL = "key_target_url";
    /**
     * 扩展参数:
     * 浮动的标题栏, 开启会自动设置透明标题栏, 和 内核提示的padding
     */
    public static final String KEY_FLOAT_TITLE_BAR = "key_float_title_bar";

    /**
     * 扩展参数:
     * 隐藏默认的标题文本
     */
    public static final String KEY_HIDE_TITLE = "key_hide_title";

    /**
     * 扩展参数:
     * 隐藏默认的标题Bar
     */
    public static final String KEY_HIDE_TITLE_BAR = "key_hide_title_bar";

    /**
     * 扩展参数:
     * 用状态栏的高度, 填充内容布局的top
     */
    public static final String KEY_PADDING_TOP = "key_padding_top";

    /**
     * 扩展参数:
     * 显示默认的菜单按钮
     */
    public static final String KEY_SHOW_DEFAULT_MENU = "key_show_default_menu";

    //</editor-fold>

    //<editor-fold desc="界面特性成员变量">

    /**
     * 需要打开的url
     */
    protected String mTargetUrl = "";
    protected boolean floatTitleBar = false;
    protected boolean hideTitle = false;
    protected boolean hideTitleBar = false;

    /**
     * 显示默认的菜单按钮
     */
    protected boolean showDefaultMenu = true;

    //</editor-fold>

    protected X5WebView mWebView;
    protected SimpleProgressBar mProgressBarView;
    protected EmptyView mEmptyView;
    protected WebCallback mWebCallback;
    protected SmartRefreshLayout mRefreshLayout;

    //<editor-fold desc="界面状态成员变量">

    private int mSoftInputMode;

    /**
     * 正在处理的下载链接
     */
    private String downloadUrl = "";

    /**
     * 界面加载是否完成
     */
    public boolean isPageLoadFinish = false;

    /**
     * 页面标题
     */
    protected String pageTitle = "";

    protected boolean paddingTop = false;

    //</editor-fold>

    @Override
    public String getFragmentTitle() {
        return super.getFragmentTitle();
    }

    @Override
    public void onTitleBackClick(@NonNull View view) {
        super.onTitleBackClick(view);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.base_x5_web_layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mTargetUrl = arguments.getString(KEY_TARGET_URL);
            showDefaultMenu = arguments.getBoolean(KEY_SHOW_DEFAULT_MENU, showDefaultMenu);
            hideTitle = arguments.getBoolean(KEY_HIDE_TITLE, hideTitle);
            hideTitleBar = arguments.getBoolean(KEY_HIDE_TITLE_BAR, hideTitleBar);
            floatTitleBar = arguments.getBoolean(KEY_FLOAT_TITLE_BAR, floatTitleBar);
            paddingTop = arguments.getBoolean(KEY_PADDING_TOP, paddingTop);
        }

        //        String encode = targetUrl;
//        try {
//            if (targetUrl.contains("klgwl.com") || targetUrl.contains("120.78.182.253")) {
//                mTargetUrl = encode;
//                return;
//            } else {
//                encode = URLEncoder.encode(targetUrl, "utf8");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //mTargetUrl = Api.Companion.h5Url("/link/to?url=" + encode);
    }

    @Override
    protected void onInitBaseView(@NonNull RBaseViewHolder viewHolder,
                                  @Nullable Bundle arguments,
                                  @Nullable Bundle savedInstanceState) {
        super.onInitBaseView(viewHolder, arguments, savedInstanceState);

        mWebView = baseViewHolder.v(R.id.base_x5_web_view);

        mProgressBarView = baseViewHolder.v(R.id.progress_bar_view);
        mRefreshLayout = baseViewHolder.v(R.id.base_refresh_layout);
        mEmptyView = baseViewHolder.v(R.id.base_empty_view);

        initWebView();

        //越界滚动
        mRefreshLayout.setEnableOverScrollDrag(false);
        //回弹
        mRefreshLayout.setEnableOverScrollBounce(false);
        mRefreshLayout.setScrollBoundaryDecider(new ScrollBoundaryDeciderAdapter() {
            @Override
            public boolean canRefresh(View content) {
                //return super.canRefresh(content);
                return !mWebView.topCanScroll();
            }

            @Override
            public boolean canLoadMore(View content) {
                return super.canLoadMore(content);
            }
        });

        //在不使用默认的标题栏下, 页面padding状态栏的高度
        if (paddingTop) {
            if (contentView != null) {
                contentView.setPadding(contentView.getPaddingLeft(),
                        contentView.getPaddingTop() + RUtils.getStatusBarHeight(mAttachContext),
                        contentView.getPaddingRight(), contentView.getPaddingBottom());
            }
        }
    }

    @Override
    protected void initBaseTitleLayout(@Nullable Bundle arguments) {
        super.initBaseTitleLayout(arguments);
        setTitleString("加载中...");
        if (hideTitle) {
            hideTitleView();
        }
        if (hideTitleBar) {
            hideTitleBar();
        }
        if (floatTitleBar) {
            floatTitleBar();
            TextView headerView = baseViewHolder.v(R.id.base_web_header_view);
            if (headerView != null) {
                headerView.setPadding(headerView.getPaddingLeft(),
                        (int) (headerView.getPaddingTop() + ResUtil.dpToPx(65)),
                        headerView.getPaddingRight(),
                        headerView.getPaddingBottom());
            }

            if (mRefreshLayout == null) {
                mRefreshLayout = baseViewHolder.v(R.id.base_refresh_layout);
            }
            if (mRefreshLayout != null) {
                //mRefreshLayout.setHeaderMaxDragRate(4f);
                //mRefreshLayout.setHeaderTriggerRate(4f);
                mRefreshLayout.setHeaderHeight(200);
            }
        }
    }

    @Override
    protected void initRightControlLayout() {
        super.initRightControlLayout();
        if (showDefaultMenu) {
            addDefaultMenu();
        }
    }

    /**
     * 默认菜单
     */
    public void addDefaultMenu() {
        //更多按钮
        rightControl()
                .addView(TitleItemHelper.createItem(mAttachContext, R.drawable.base_more, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RSheetDialog.build(mAttachContext)
                                .addItem("在浏览器中打开", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mWebView != null) {
                                            RUtils.openUrl(mAttachContext, mWebView.getUrl());
                                        }
                                    }
                                })
                                .addItem("复制链接", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        RUtils.copyText(mTargetUrl);
                                    }
                                })
                                .addItem("刷新", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mWebView != null) {
                                            mWebView.reload();
                                        }
                                    }
                                })
                                .show();
                    }
                }));
    }

    @Override
    protected void initLeftControlLayout() {
        super.initLeftControlLayout();
        addLeftItem(createCloseItem());
    }

    /**
     * 创建关闭按钮
     */
    protected View createCloseItem() {
        return TitleItemHelper.build(mAttachContext)
                .setSrc(R.drawable.base_close)
                .setClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        backFragment(false);
                    }
                })
                .setLeftMargin((int) ResUtil.dpToPx(-20))
                .setVisibility(View.GONE)
                .setViewId(R.id.base_title_close_view)
                .build();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSoftInputMode = getActivity().getWindow().getAttributes().softInputMode;
        if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 11) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        onLoadUrl();
    }

    protected void showDebugUrlView(String url) {
        if (RTbs.DEBUG) {
            baseViewHolder.tv(R.id.url_view).setVisibility(View.VISIBLE);
            baseViewHolder.tv(R.id.url_view).setText(url + "\n\n" + mWebView.getSettings().getUserAgentString());
        }
    }

    protected void showPageHeader(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (TextUtils.isEmpty(host)) {
            return;
        }

        TextView headerView = baseViewHolder.v(R.id.base_web_header_view);
        if (headerView != null) {
            headerView.setText("网页由 " + host + " 提供" + "\n腾讯X5提供技术支持");
        }
    }

    protected void initWebView() {
        mWebView.setOnWebViewListener(new X5WebView.OnWebViewListener() {
            @Override
            public void onScroll(int left, int top, int dx, int dy) {
                //getUITitleBarContainer().evaluateBackgroundColorSelf(top);
                //L.e("call: onScroll([left, top, dx, dy])-> " + top);
            }

            @Override
            public void onOverScroll(int scrollY) {
                //L.e("call: onOverScroll([scrollY])-> " + scrollY);
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                X5WebFragment.this.onPageFinished(webView, url);
            }

            @Override
            public void onReceivedTitle(WebView webView, String title) {
                pageTitle = title;
                setTitleString(pageTitle);
            }

            @Override
            public void onProgressChanged(WebView webView, int progress) {
                X5WebFragment.this.onProgressChanged(webView, progress);
            }

            @Override
            public boolean onOpenFileChooser(final ValueCallback<Uri> uploadFile, String acceptType, String captureType) {
                if (!TextUtils.isEmpty(acceptType)) {
                    if (acceptType.startsWith("image")) {
//                        RPicker.INSTANCE.pickerImage(mParentILayout, 1, new OnMediaSelectorObserver() {
//                            @Override
//                            public void onMediaSelector(@NotNull List<MediaItem> mediaItemList) {
//                                uploadFile.onReceiveValue(Uri.fromFile(new File(mediaItemList.get(0).getPath())));
//                            }
//                        });
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void shouldOverrideUrlLoading(WebView webView, String url) {

                showDebugUrlView(url);
                //showPageHeader(url);
            }
        });

        mWebView.addJavascriptInterface(new AndroidJs() {

//            @JavascriptInterface
//            public void getCurrentUser(String appid) {
//                String url = mWebView.getUrl();
//                L.e(url + " getVerifyToken " + appid);
//                Uri parse = Uri.parse(url);
//                if (!TextUtils.isEmpty(url) &&
//                        (parse.getHost().contains("klgwl.com") ||
//                                parse.getHost().contains("120.78.182.253"))) {
//                    X5WebFragment.this.getVerifyToken(appid);
//                }
//            }
        }, "android");

        mWebView.setMyDownloadListener(new X5WebView.MyDownloadListener() {
            @Override
            public void onDownloadStart(String url /*下载地址*/, String userAgent /*user agent*/,
                                        String contentDisposition, String mime /*文件mime application/zip*/,
                                        long length /*文件大小 kb*/) {
                if (TextUtils.equals(downloadUrl, url)) {

                } else {
                    downloadUrl = url;
                    DownloadFileBean mDownloadFileBean = new DownloadFileBean();
                    mDownloadFileBean.url = url;
                    mDownloadFileBean.userAgent = userAgent;
                    mDownloadFileBean.fileType = mime;
                    mDownloadFileBean.fileSize = length;
                    mDownloadFileBean.fileName = RUtils.getFileNameFromAttachment(contentDisposition);
                    mDownloadFileBean.contentDisposition = contentDisposition;

                    FileDownloadDialog.show(mAttachContext,
                            mDownloadFileBean,
                            new FileDownloadDialog.OnDownloadListener() {
                                @Override
                                public boolean onDownload(DownloadFileBean bean) {
                                    downloadUrl = "";
                                    return false;
                                }
                            });
                }
            }
        });

        mWebView.setOnOpenAppListener(new X5WebView.OnOpenAppListener() {
            @Override
            public void onOpenApp(final RUtils.QueryAppBean appBean) {
                if (appBean.mAppInfo.packageName.contains("hn")) {
                    getActivity().startActivity(appBean.startIntent);
                    return;
                }

                OpenAppDialog.show(mAttachContext, appBean);
            }
        });
    }

    public void invokeJs(String method, String params) {
        String js = "javascript:" + method + "(" + params + ");";
        L.e("call: invokeJs -> " + js);
        mWebView.loadUrl(js);
    }

    protected void onLoadUrl() {
        L.i("call: onLoadUrl([])-> 加载网页:" + mTargetUrl);
        RTbs.log("call: onLoadUrl([])-> 加载网页:" + mTargetUrl);

        isPageLoadFinish = false;

        mWebView.loadUrl(mTargetUrl);
        mProgressBarView.setProgress(10);

        showDebugUrlView(mTargetUrl);
        showPageHeader(mTargetUrl);
    }

    protected void onPageFinished(WebView webView, String url) {
        isPageLoadFinish = true;

        if (mWebCallback != null) {
            mWebCallback.onPageFinished(webView, url);
        }
        if (mWebView.canGoBack()) {
            leftControl().selector(R.id.base_title_close_view).visible();
        } else {
            leftControl().selector(R.id.base_title_close_view).gone();
        }
        //L.e("call: onPageFinished([webView, url])-> ");
    }

    protected void onProgressChanged(WebView webView, int progress) {
        mProgressBarView.setProgress(progress);
        if (progress >= 90) {
            mEmptyView.setVisibility(View.GONE);
            //L.e("call: onProgressChanged([webView, progress])-> " + progress);
        }
        if (mWebCallback != null) {
            mWebCallback.onProgressChanged(webView, progress);
        }
    }

    @Override
    public void onFragmentShow(@Nullable Bundle bundle) {
        super.onFragmentShow(bundle);
        mWebView.onResume();
        mWebView.resumeTimers();
        // mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public void onFragmentHide() {
        super.onFragmentHide();
        mWebView.onPause();
        mWebView.pauseTimers();
        //mActivity.getWindow().setSoftInputMode(mSoftInputMode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    @Override
    public boolean onBackPressed(@NonNull Activity activity) {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            if (mWebView.canGoBack()) {
                leftControl().selector(R.id.base_title_close_view).visible();
            } else {
                leftControl().selector(R.id.base_title_close_view).gone();
            }
            return false;
        }
        return true;
    }

    public X5WebFragment setWebCallback(WebCallback webCallback) {
        mWebCallback = webCallback;
        return this;
    }

    public static abstract class WebCallback {
        public void onPageFinished(WebView webView, String url) {

        }

        public void onProgressChanged(WebView webView, int progress) {
        }
    }
}
