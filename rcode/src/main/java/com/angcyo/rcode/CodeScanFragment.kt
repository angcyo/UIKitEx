package com.angcyo.rcode

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Vibrator
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.angcyo.http.HttpSubscriber
import com.angcyo.http.RFunc
import com.angcyo.http.Rx
import com.angcyo.lib.L
import com.angcyo.rcode.camera.CameraManager
import com.angcyo.rcode.encode.QRCodeEncoder.HINTS_DECODE
import com.angcyo.uiview.less.base.BaseFragment
import com.angcyo.uiview.less.base.helper.FragmentHelper
import com.angcyo.uiview.less.recycler.RBaseViewHolder
import com.angcyo.uiview.less.resources.AnimUtil
import com.angcyo.uiview.less.skin.SkinHelper
import com.angcyo.uiview.less.utils.RDialog
import com.angcyo.uiview.less.utils.T_
import com.angcyo.uiview.less.widget.RImageCheckView
import com.google.zxing.*
import com.google.zxing.client.android.DecodeHandlerJni
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.tbruyelle.rxpermissions.RxPermissions
import net.sourceforge.zbar.Image
import net.sourceforge.zbar.ImageScanner
import java.io.IOException

/**
 * 二维码扫描
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/05/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class CodeScanFragment : BaseFragment(), IActivity, SurfaceHolder.Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        hasSurface = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (holder == null) {
            L.e("*** WARNING *** surfaceCreated() gave us a null surface!")
        }
        if (!hasSurface) {
            hasSurface = true

            baseViewHolder.postDelay(300) {
                initCamera(holder)
            }
        }
    }

    override fun getCameraManager(): CameraManager {
        return cameraManager!!
    }

    override fun getHandler(): Handler? {
        return handler
    }

    override fun getHandleActivity(): Activity {
        return requireActivity()
    }

    override fun getViewfinderView(): ViewfinderView {
        return viewfinderView
    }

    override fun setResult(resultCode: Int, data: Intent?) {

    }

    override fun finish() {

    }

    override fun getPackageManager(): PackageManager {
        return packageManager
    }

    override fun startActivity(intent: Intent?) {

    }

    override fun drawViewfinder() {
        viewfinderView.drawViewfinder()
    }

    companion object {

        fun show(
            fragmentManager: FragmentManager?,
            onResultCallback: (String) -> Unit = {}
        ): CodeScanFragment {
            val fragment = CodeScanFragment()
            fragment.onScanResult = onResultCallback

            FragmentHelper.build(fragmentManager)
                .defaultEnterAnim()
                .showFragment(fragment)
                .doIt()
            return fragment
        }

        /**第二层 扫描*/
        private fun scanPictureFun2(scanBitmap: Bitmap): String {
            val source = PlanarYUVLuminanceSource(
                rgb2YUV(scanBitmap),
                scanBitmap.width,
                scanBitmap.height,
                0, 0,
                scanBitmap.width,
                scanBitmap.height,
                false
            )

            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            var result: Result?
            try {
                result = reader.decode(binaryBitmap, HINTS_DECODE)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    result = reader.decode(BinaryBitmap(GlobalHistogramBinarizer(source)), HINTS_DECODE)
                    return result.text ?: ""
                } catch (e: Exception) {
                    return scanPictureFun3(scanBitmap)
                }
            }
            return result.text ?: ""
        }

        /**第三层 扫描方法, 使用 ZBar*/
        private fun scanPictureFun3(scanBitmap: Bitmap): String {
            val barcode = Image(scanBitmap.width, scanBitmap.height, "Y800")
//            Debug.logTimeStart("rgb2YUV")
//            barcode.data = rgb2YUV(scanBitmap) //BmpUtil.generateBitstream(scanBitmap, Bitmap.CompressFormat.JPEG, 100)
//            Debug.logTimeEnd("rgb2YUV")

            val width = scanBitmap.width
            val height = scanBitmap.height
            val pixels = IntArray(width * height)
            scanBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
//            Debug.logTimeStart("rgb2YUV_2")
            barcode.data = DecodeHandlerJni.rgb2yuv(pixels, width, height)
//            Debug.logTimeEnd("rgb2YUV_2")

            val mImageScanner = ImageScanner()
            val result = mImageScanner.scanImage(barcode)
            var resultQRcode: String? = null
            if (result != 0) {
                val symSet = mImageScanner.results
                for (sym in symSet)
                    resultQRcode = sym.data
            }
            return resultQRcode ?: ""
        }

        /**第一层 扫描*/
        fun scanPictureFun1(bitmap: Bitmap?): String {
            var result: Result? = null
            var source: RGBLuminanceSource? = null
            try {
                val width = bitmap!!.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                source = RGBLuminanceSource(width, height, pixels)
                result = MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source)), HINTS_DECODE)
                return result!!.text
            } catch (e: Exception) {
                e.printStackTrace()
                if (source != null) {
                    try {
                        result =
                            MultiFormatReader().decode(BinaryBitmap(GlobalHistogramBinarizer(source)), HINTS_DECODE)
                        return result!!.text
                    } catch (e2: Throwable) {
                        e2.printStackTrace()
                        return scanPictureFun2(bitmap!!)
                    }
                }
                return ""
            }
        }

        /**调用三层扫码方法*/
        fun scanPictureFun(context: Context, bitmapPath: String): String {
            return scanPictureFun1(createBitmap(context, bitmapPath))
        }

        /**
         * 根据图片路径创建一个合适大小的位图
         *
         * @param filePath
         * @return
         */
        fun createBitmap(context: Context, filePath: String): Bitmap {
            val dm = context.resources.displayMetrics
            val displayWidth = dm.widthPixels

            val opts = BitmapFactory.Options()            //实例位图设置
            opts.inJustDecodeBounds = true                                        //表示不全将图片加载到内存,而是读取图片的信息2
            BitmapFactory.decodeFile(filePath, opts)                            //首先读取图片信息,并不加载图片到内存,防止内存泄露
            val imageWidth = opts.outWidth                                        //获取图片的宽
            val imageHeight = opts.outHeight                                    //获取图片的高

            var scale = 1
            if (imageWidth > displayWidth || imageHeight > displayWidth) {
                val scaleX = imageWidth / displayWidth                             //计算图片,宽度的缩放率
                val scaleY = imageHeight / displayWidth                            //计算图片,高度的缩放率

                if ((scaleX > scaleY) and (scaleY >= 1)) {                                //如果图片的宽比高大,则采用宽的比率
                    scale = scaleX
                }

                if ((scaleY > scaleX) and (scaleX >= 1)) {                                //如果图片的高比宽大,则采用高的比率
                    scale = scaleY
                }
            }

            opts.inJustDecodeBounds = false                                    //表示加载图片到内存
            opts.inSampleSize = scale                                            //设置Bitmap的采样率

            return BitmapFactory.decodeFile(filePath, opts)
        }

        fun rgb2YUV(bitmap: Bitmap): ByteArray {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val len = width * height
            val yuv = ByteArray(len * 3 / 2)
            var y: Int
            var u: Int
            var v: Int
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val rgb = pixels[i * width + j] and 0x00FFFFFF

                    val r = rgb and 0xFF
                    val g = rgb shr 8 and 0xFF
                    val b = rgb shr 16 and 0xFF

                    y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                    u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                    v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128

                    y = if (y < 16) 16 else if (y > 255) 255 else y
                    u = if (u < 0) 0 else if (u > 255) 255 else u
                    v = if (v < 0) 0 else if (v > 255) 255 else v

                    yuv[i * width + j] = y.toByte()
                    //                yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                    //                yuv[len + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
                }
            }
            return yuv
        }
    }

    private var hasSurface: Boolean = false
    private var inactivityTimer: InactivityTimer? = null
    private var beepManager: BeepManager? = null
    private var ambientLightManager: AmbientLightManager? = null
    private lateinit var cameraManager: CameraManager
    private var handler: CaptureActivityHandler? = null
    private var lastResult: Result? = null
    private lateinit var surfaceView: SurfaceView
    private lateinit var viewfinderView: ViewfinderView
    private val VIBRATE_DURATION: Long = 50

    override fun getLayoutId(): Int {
        return R.layout.fragment_code_scan_layout
    }

    override fun initBaseView(viewHolder: RBaseViewHolder, arguments: Bundle?, savedInstanceState: Bundle?) {
        super.initBaseView(viewHolder, arguments, savedInstanceState)

        hasSurface = false
        inactivityTimer = InactivityTimer(handleActivity)
        beepManager = BeepManager(handleActivity)
        ambientLightManager = AmbientLightManager(mAttachContext)
        cameraManager = CameraManager(mAttachContext.applicationContext)

        val view = viewHolder.v<RImageCheckView>(R.id.base_light_switch_view)
        view.setOnCheckedChangeListener { buttonView, isChecked -> openFlashlight(isChecked) }

        viewHolder.click(R.id.base_photo_selector_view) { onSelectorPhotoClick() }

        surfaceView = viewHolder.v(R.id.base_preview_view)
        viewfinderView = viewHolder.v(R.id.base_viewfinder_view)
        viewfinderView.laserColor = (SkinHelper.getSkin().themeSubColor)

        viewfinderView.setCameraManager(cameraManager)

        val surfaceHolder = surfaceView.holder
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder)
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this)
        }
    }

    /**
     * 震动一下, 需要权限VIBRATE
     */
    @SuppressLint("MissingPermission")
    protected open fun playVibrate() {
        val vibrator = mAttachContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VIBRATE_DURATION)
    }

    /**
     * 打开闪光灯
     */
    protected fun openFlashlight(open: Boolean) {
        cameraManager!!.setTorch(open)
    }

    /**
     * 打开图片二维码
     */
    protected fun onSelectorPhotoClick() {

    }

    override fun onFragmentShow(bundle: Bundle?) {
        super.onFragmentShow(bundle)

        baseViewHolder.itemView.keepScreenOn = true


        handler = null
        lastResult = null

        beepManager!!.updatePrefs()
        ambientLightManager!!.start(cameraManager)

        inactivityTimer!!.onResume()

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            //initCamera(surfaceView.holder)
            initCamera(surfaceView!!.holder)
        }
    }

    override fun onFragmentHide() {
        super.onFragmentHide()

        baseViewHolder.itemView.keepScreenOn = false

        if (handler != null) {
            handler!!.quitSynchronously()
            handler = null
        }
        inactivityTimer!!.onPause()
        ambientLightManager!!.stop()
        beepManager!!.close()
        cameraManager!!.closeDriver()
        //historyManager = null; // Keep for onActivityResult
    }

    override fun onDestroy() {
        super.onDestroy()
        surfaceView.holder.removeCallback(this)
        inactivityTimer?.shutdown()
    }

    private var decodeFormats: Collection<BarcodeFormat>? = null
    private var decodeHints: Map<DecodeHintType, *>? = null
    private var characterSet: String? = null

    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        fun doIt() {
            if (surfaceHolder == null) {
                throw IllegalStateException("No SurfaceHolder provided")
            }
            if (cameraManager.isOpen) {
                L.w("initCamera() while already open -- late SurfaceView callback?")
                return
            }
            try {
                cameraManager.openDriver(surfaceHolder)
                // Creating the handler starts the preview, which can also throw a RuntimeException.
                if (handler == null) {
                    handler = CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager)
                }
                decodeOrStoreSavedBitmap(null, null)
            } catch (ioe: IOException) {
                L.w("$ioe")
                displayFrameworkBugMessageAndExit()
            } catch (e: RuntimeException) {
                // Barcode Scanner has seen crashes in the wild of this variety:
                // java.?lang.?RuntimeException: Fail to connect to camera service
                L.w("Unexpected error initializing camera :$e")
                displayFrameworkBugMessageAndExit()
            }
        }

        RxPermissions(requireActivity())
            .request(Manifest.permission.CAMERA)
            .subscribe { aBoolean ->
                if (aBoolean!!) {
                    if (surfaceView.background is ColorDrawable &&
                        (surfaceView.background as ColorDrawable).color == 0
                    ) {

                    } else {
                        AnimUtil.startArgb(surfaceView, Color.BLACK, Color.TRANSPARENT, 200)
                    }
                    doIt()
                } else {
                    displayFrameworkBugMessageAndExit()
                }
            }
    }

    private var savedResultToShow: Result? = null

    private fun decodeOrStoreSavedBitmap(bitmap: Bitmap?, result: Result?) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result
        } else {
            if (result != null) {
                savedResultToShow = result
            }
            if (savedResultToShow != null) {
                val message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow)
                handler?.sendMessage(message)
            }
            savedResultToShow = null
        }
    }

    /**
     * 打开摄像头出错
     */
    private fun displayFrameworkBugMessageAndExit() {
        onDisplayError()
    }

    /**
     * 打开失败
     */
    fun onDisplayError() {
        T_.error("请开启摄像头权限.")
        backFragment(false)
    }

    var onScanResult: ((String) -> Unit)? = null

    /**处理扫码返回的结果*/
    override fun handleDecode(data: String?) {
        L.e("call: handleDecode ->$data")
        var result = ""
        if (TextUtils.isEmpty(data)) {
            //T_.ok("unknown")
            result = ""
        } else {
            //playVibrate()
            result = data!!
            //T_.ok(data)
        }
        beepManager?.playBeepSoundAndVibrate()
        onHandleDecode(result)
    }

    open fun onHandleDecode(data: String) {
        backFragment(false)
        onScanResult?.invoke(data)
        //重复扫描请调用此方法
        //scanAgain()
    }

    /**重新扫描*/
    open fun scanAgain() {
        restartPreviewAfterDelay(1000L)
    }

    fun restartPreviewAfterDelay(delayMS: Long) {
        if (handler != null) {
            handler?.sendEmptyMessageDelayed(R.id.restart_preview, delayMS)
        }
    }

    /**
     * 扫描图片
     * */
    fun scanPicture(scanBitmap: Bitmap) {
        RDialog.flow(mAttachContext)
        Rx.base(object : RFunc<String>() {
            override fun onFuncCall(): String {
                return scanPictureFun1(scanBitmap)
            }
        }, object : HttpSubscriber<String>() {
            override fun onSucceed(bean: String) {
                handleDecode(bean)
            }

            override fun onEnd(data: String?, error: Throwable?) {
                super.onEnd(data, error)
                RDialog.hide()
            }
        })
    }
}
