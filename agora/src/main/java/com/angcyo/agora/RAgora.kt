package com.angcyo.agora

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.angcyo.lib.L
import com.angcyo.uiview.less.utils.RUtils
import com.angcyo.uiview.less.utils.Root
import io.agora.rtc.Constants
import io.agora.rtc.Constants.LOG_FILTER_INFO
import io.agora.rtc.IRtcEngineEventHandlerEx
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

/**
 * 声网 音视频通话
 * https://docs.agora.io/cn
 * */
class RAgora(val context: Activity) {
    companion object {
        const val PERMISSION_REQ_ID = 22

        val REQUESTED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun checkPermission(callback: () -> Unit) {
        if (havePermission()) {
            callback()
        } else {
            ActivityCompat.requestPermissions(
                context,
                REQUESTED_PERMISSIONS,
                PERMISSION_REQ_ID
            )
        }
    }

    fun havePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                REQUESTED_PERMISSIONS[0]
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                REQUESTED_PERMISSIONS[1]
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                REQUESTED_PERMISSIONS[2]
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    var mRtcEngine: RtcEngine? = null

    //Wayto 声网 默认appid https://dashboard.agora.io/projects
    //APP 证书 9f944e8c86e****fa413c22001b95d4c****0428
    var appid: String = "960644f3218845e5aef0ab9166256473"
    var logFilePath =
        Root.createExternalFilePath(
            RUtils.DEFAULT_LOG_FOLDER_NAME,
            "agora${Root.createTimeFileName("yyyy-MM-dd")}.log"
        )
    var logFileSize = 10 * 1024  /*10mb*/
    var logFileFilter = LOG_FILTER_INFO

    /**用户唯一标识符*/
    var localUserUid = 0

    //所有回调, 就会在主线程进行

    /**
     * 用户离开, 或者进入
     * */
    var onUserChange: (uid: Int, isLeave: Boolean) -> Unit = { _, _ ->

    }

    /**第一帧解码成功, 可以表示用户连接完全成功*/
    var onFirstRemoteDecoded: (uid: Int, isVideo: Boolean) -> Unit = { _, _ ->

    }

    /**激活/关闭 视频模块回调*/
    var onUserEnableVideo: (uid: Int, isLocal: Boolean, enable: Boolean) -> Unit = { _, _, _ ->

    }

    /**关闭流, 静音*/
    var onUserMute: (uid: Int, isVideo: Boolean, mute: Boolean) -> Unit = { _, _, _ ->

    }

    var onError: (err: Int) -> Unit = {
        L.e("发生异常:$it")
    }

    fun init(appid: String = this.appid) {
        try {
            this.appid = appid
            destroy()
            mRtcEngine = RtcEngine.create(context, appid, object : IRtcEngineEventHandlerEx() {

                /**SDK无法处理的异常*/
                override fun onError(err: Int) {
                    super.onError(err)
                    context.runOnUiThread {
                        this@RAgora.onError(err)
                    }
                }

                /**用户离线*/
                override fun onUserOffline(uid: Int, reason: Int) {
                    super.onUserOffline(uid, reason)
                    context.runOnUiThread {
                        onUserChange(uid, true)
                    }
                }

                /**@param elapsed 时间, (毫秒)*/
                override fun onUserJoined(uid: Int, elapsed: Int) {
                    super.onUserJoined(uid, elapsed)
                    context.runOnUiThread {
                        onUserChange(uid, false)
                    }
                }


                override fun onFirstRemoteAudioDecoded(uid: Int, elapsed: Int) {
                    super.onFirstRemoteAudioDecoded(uid, elapsed)
                    context.runOnUiThread {
                        onFirstRemoteDecoded(uid, false)
                    }
                }

                override fun onFirstRemoteVideoDecoded(
                    uid: Int,
                    width: Int,
                    height: Int,
                    elapsed: Int
                ) {
                    super.onFirstRemoteVideoDecoded(uid, width, height, elapsed)
                    context.runOnUiThread {
                        onFirstRemoteDecoded(uid, true)
                    }
                }

                override fun onUserEnableLocalVideo(uid: Int, enabled: Boolean) {
                    super.onUserEnableLocalVideo(uid, enabled)
                    context.runOnUiThread {
                        onUserEnableVideo(uid, true, enabled)
                    }
                }

                override fun onUserEnableVideo(uid: Int, enabled: Boolean) {
                    super.onUserEnableVideo(uid, enabled)
                    context.runOnUiThread {
                        onUserEnableVideo(uid, false, enabled)
                    }
                }

                /**用户激活/关闭视频流*/
                override fun onUserMuteVideo(uid: Int, muted: Boolean) {
                    super.onUserMuteVideo(uid, muted)
                    context.runOnUiThread {
                        onUserMute(uid, true, muted)
                    }
                }

                override fun onUserMuteAudio(uid: Int, muted: Boolean) {
                    super.onUserMuteAudio(uid, muted)
                    context.runOnUiThread {
                        onUserMute(uid, false, muted)
                    }
                }
            })
        } catch (e: Exception) {
            L.e(Log.getStackTraceString(e))
            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(
                    e
                )
            )
        }

        mRtcEngine?.apply {
            setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)//通讯模式
            setLogFile(logFilePath)
            setLogFileSize(logFileSize)
            setLogFilter(logFileFilter)

            //激活视频模块, 但是可以关闭视频流.
            //this@RAgora.enableVideo()

            //视频编码信息
            setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_1280x720,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
            )
        }
    }

    /**
     * 加入频道, 核心方法
     * @param uid 用户id, 为0, 系统自动分配, 用户唯一标识符
     * */
    fun joinChannel(
        token: String,
        channelName: String = context.packageName,
        uid: Int = localUserUid,
        optionalInfo: String = context.packageName,
        appid: String = this.appid
    ) {
        if (mRtcEngine == null) {
            init(appid)
        }
        checkInit()

        localUserUid = uid

        //相同appid的相同channelName的用户, 就会进行通讯.
        checkPermission {
            mRtcEngine?.joinChannel(token, channelName, optionalInfo, uid)
        }
    }

    /**激活/关闭视频模块*/
    fun enableVideo(enable: Boolean = true) {
        checkInit()

        mRtcEngine?.apply {
            if (enable) {
                enableVideo()
            } else {
                disableVideo()
            }
        }
    }

    /**激活/关闭 本地视频流*/
    fun enableLocalVideoStream(enable: Boolean = true) {
        checkInit()

        mRtcEngine?.apply {
            muteLocalVideoStream(!enable)
        }
    }

    /**激活/关闭 指定用户视频流*/
    fun enableRemoteVideoStream(uid: Int, enable: Boolean = true) {
        checkInit()

        mRtcEngine?.apply {
            muteRemoteVideoStream(uid, !enable)
        }
    }

    fun enableLocalAudioStream(enable: Boolean = true) {
        checkInit()

        mRtcEngine?.apply {
            muteLocalAudioStream(!enable)
        }
    }

    /**激活/关闭 指定用户音频流*/
    fun enableRemoteAudioStream(uid: Int, enable: Boolean = true) {
        checkInit()

        mRtcEngine?.apply {
            muteRemoteAudioStream(uid, !enable)
        }
    }

    /**显示本地视频流*/
    fun showLocalVideo(viewGroup: ViewGroup) {
        checkInit()

        val findViewWithTag = viewGroup.findViewWithTag<View>(localUserUid)
        if (findViewWithTag == null) {
            val surfaceView = RtcEngine.CreateRendererView(context)
            surfaceView.setZOrderMediaOverlay(true)
            viewGroup.addView(surfaceView)
            surfaceView.tag = localUserUid
            mRtcEngine?.setupLocalVideo(
                VideoCanvas(
                    surfaceView,
                    VideoCanvas.RENDER_MODE_FIT,
                    localUserUid
                )
            )
            mRtcEngine?.startPreview()
        }
    }

    /**显示远程视频流*/
    fun showRemoteVideo(viewGroup: ViewGroup, uid: Int) {
        checkInit()

        val findViewWithTag = viewGroup.findViewWithTag<View>(uid)

        if (findViewWithTag == null) {
            val surfaceView = RtcEngine.CreateRendererView(context)
            viewGroup.addView(surfaceView)
            surfaceView.tag = uid
            mRtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
        }
    }

    fun switchCamera() {
        mRtcEngine?.switchCamera()
    }

    /**离开频道*/
    fun leaveChannel() {
        mRtcEngine?.leaveChannel()
    }

    /**释放资源*/
    fun destroy() {
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    private fun checkInit() {
        if (mRtcEngine == null) {
            throw RuntimeException("需要先调用init()方法.")
        }
    }
}