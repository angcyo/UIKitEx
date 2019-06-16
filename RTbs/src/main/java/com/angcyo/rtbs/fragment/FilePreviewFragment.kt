package com.angcyo.rtbs.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.angcyo.lib.L
import com.angcyo.okdownload.FDown
import com.angcyo.okdownload.FDownListener
import com.angcyo.rtbs.BuildConfig
import com.angcyo.rtbs.R
import com.angcyo.rtbs.RTbs
import com.angcyo.rtbs.TbsFileRender
import com.angcyo.rtbs.fragment.X5WebFragment.*
import com.angcyo.uiview.less.base.BaseTitleFragment
import com.angcyo.uiview.less.base.helper.FragmentHelper
import com.angcyo.uiview.less.iview.AffectUI
import com.angcyo.uiview.less.kotlin.mimeType
import com.angcyo.uiview.less.kotlin.toast_tip
import com.angcyo.uiview.less.recycler.RBaseViewHolder
import com.angcyo.uiview.less.resources.ResUtil
import com.angcyo.uiview.less.widget.SimpleProgressBar
import com.liulishuo.okdownload.DownloadTask

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/15
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**
 * 不支持双开, 否则tbs 无法正常工作
 * 支持打开 本地/在线 文档.
 * 格式 有:标准的 office 文档, pdf 图片, txt
 * */
open class FilePreviewFragment : BaseTitleFragment() {

    var filePreviewConfig = FilePreviewConfig()

    companion object {

        /** tbs 打开文件 */
        fun show(builder: FragmentHelper.Builder, config: FilePreviewConfig.() -> Unit = {}) {
            builder
                .showFragment(FilePreviewFragment().apply {
                    filePreviewConfig.config()
                })
                .doIt()
        }

        /**tbs 打开网页*/
        fun openUrl(builder: FragmentHelper.Builder, config: WebPreviewConfig.() -> Unit = {}) {
            val webConfig = WebPreviewConfig()
            webConfig.config()
            val arguments = Bundle()
            arguments.putString(KEY_TARGET_URL, webConfig.targetUrl)
            arguments.putBoolean(KEY_SHOW_DEFAULT_MENU, webConfig.showDefaultMenu)
            arguments.putBoolean(KEY_HIDE_TITLE, webConfig.hideTitle)
            arguments.putBoolean(KEY_HIDE_TITLE_BAR, webConfig.hideTitleBar)
            arguments.putBoolean(KEY_FLOAT_TITLE_BAR, webConfig.floatTitleBar)
            arguments.putBoolean(KEY_PADDING_TOP, webConfig.paddingTop)

            builder.setArgs(arguments)
                .showFragment(X5WebFragment::class.java)
                .doIt()
        }
    }

    override fun createAffectUI(): AffectUI {
        return getUiFragment()
            .createAffectUI(this)
            .setParent(baseViewHolder.group(R.id.content_wrap_layout))
            .create()
    }

    override fun getContentLayoutId(): Int {
        return R.layout.base_file_preview_layout
    }

    override fun getFragmentTitle(): CharSequence {
        return filePreviewConfig.fragmentTitle
    }

    var tbsFileRender: TbsFileRender? = null
    override fun onInitBaseView(viewHolder: RBaseViewHolder, arguments: Bundle?, savedInstanceState: Bundle?) {
        super.onInitBaseView(viewHolder, arguments, savedInstanceState)

        fragmentContentWrapperLayout?.setCollapsingTitle(true)

        tbsFileRender = TbsFileRender.wrap(viewHolder.group(R.id.content_wrap_layout))

        switchAffectUI(AffectUI.AFFECT_LOADING)
    }

    override fun onAffectChange(affectUI: AffectUI, fromAffect: Int, toAffect: Int, fromView: View?, toView: View?) {
        super.onAffectChange(affectUI, fromAffect, toAffect, fromView, toView)
        if (toAffect == AffectUI.AFFECT_LOADING) {
            if (filePreviewConfig.filePath?.isEmpty() == true) {
            } else {
                doOpen(filePreviewConfig.filePath)
            }
        } else if (toAffect == AffectUI.AFFECT_ERROR) {
            baseViewHolder.click(ResUtil.getThemeIdentifier(mAttachContext, "base_retry_button", "id")) {
                switchAffectUI(AffectUI.AFFECT_LOADING)
            }

            if (affectUI.extraObj is CharSequence) {
                baseViewHolder.tv(ResUtil.getThemeIdentifier(mAttachContext, "base_error_tip_view", "id"))?.text =
                    "${affectUI.extraObj}"
            }
        }
    }

    fun doOpen(path: String?) {
        if (TextUtils.isEmpty(path)) {
            //
            L.i("无法打开非法路径:$path")
            RTbs.log("无法打开非法路径:$path")
            switchAffectUI(AffectUI.AFFECT_ERROR, "非法路径:$path")
        } else {
            if (path!!.startsWith("http") || path.startsWith("https")) {
                downFile(path)
            } else {
                doOpenFile(path, TbsFileRender.getExtensionName(path))
            }
        }
    }

    fun doOpenFile(path: String?, type: String?) {
        if (TextUtils.isEmpty(path)) {
            //
            L.i("无法打开非法路径:$path")
            RTbs.log("无法打开非法路径:$path")
            switchAffectUI(AffectUI.AFFECT_ERROR, "非法路径:$path")
            return
        }

        switchAffectUI(AffectUI.AFFECT_CONTENT)

        var type = type
        L.i("尝试打开文件:$path $type")
        RTbs.log("尝试打开文件:$path $type")

        if (type != null) {
            type = type.toLowerCase()
            if (type.startsWith("do") ||
                type.startsWith("pd") ||
                type.startsWith("pp") ||
                type.startsWith("xp") ||
                type.startsWith("xls") ||
                type.startsWith("pdf")
            ) {
                if (tbsFileRender?.openFile(path, type) == true) {

                } else {
                    switchAffectUI(AffectUI.AFFECT_ERROR, "内核加载失败, 请稍后重试.")

                    toast_tip("x5内核加载失败, 请稍后重试.")
                    RTbs.log("x5内核加载失败, 请稍后重试.")
                }
            } else if (path?.mimeType()?.startsWith("image") == true) {
                tbsFileRender?.openBitmap(path)
            } else if (type.startsWith("tx") ||
                type.startsWith("log") ||
                path?.mimeType()?.startsWith("text") == true
            ) {
                tbsFileRender?.openText(path)
            } else {
                if (BuildConfig.DEBUG) {
                    tbsFileRender?.openText(path)
                    return
                }

                L.i("不支持的文件格式:$type $path")
                RTbs.log("不支持的文件格式:$type $path")

                toast_tip("不支持的文件格式")

                switchAffectUI(AffectUI.AFFECT_ERROR, "不支持的文件格式:$type $path")
            }
        }
    }

    var downId: Int = 0
    /**下载 http 开头的文件*/
    fun downFile(url: String) {
        downId = FDown.down(url, object : FDownListener() {
            override fun onTaskProgress(
                task: DownloadTask,
                totalLength: Long,
                totalOffset: Long,
                percent: Int,
                increaseBytes: Long
            ) {
                super.onTaskProgress(task, totalLength, totalOffset, percent, increaseBytes)

                baseViewHolder.v<SimpleProgressBar>(R.id.progress_bar_view)?.setProgress(percent)
            }

            override fun onTaskEnd(task: DownloadTask, isCompleted: Boolean, realCause: Exception?) {
                super.onTaskEnd(task, isCompleted, realCause)

                baseViewHolder.post {
                    baseViewHolder.v<SimpleProgressBar>(R.id.progress_bar_view)?.setProgress(100)
                }

                if (isCompleted) {
                    doOpenFile(task.file?.absolutePath, TbsFileRender.getExtensionName(task.file?.absolutePath))
                } else {
                    RTbs.log("下载异常:${realCause?.message}")
                    switchAffectUI(AffectUI.AFFECT_ERROR, "${realCause?.message}")
                }
            }
        }).id
    }

    override fun onDestroy() {
        super.onDestroy()
        tbsFileRender?.release()
        FDown.cancel(downId)
    }
}

class FilePreviewConfig {

    /**需要打开的文件路径*/
    var filePath: String? = null

    var fragmentTitle: CharSequence = ""
}

class WebPreviewConfig {
    var targetUrl: String? = null
    var showDefaultMenu = true
    var floatTitleBar = false
    var hideTitle = false
    var hideTitleBar = false
    var paddingTop = false
}