package com.angcyo.camera

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.angcyo.uiview.less.base.activity.BasePermissionActivity
import com.angcyo.uiview.less.base.helper.ActivityHelper
import com.angcyo.uiview.less.base.helper.FragmentHelper
import com.luck.picture.lib.config.PictureConfig
import java.io.File

open class RecordVideoActivity : BasePermissionActivity() {

    companion object {
        const val REQUEST_CODE = 200
        const val KEY_DATA_PATH = "KEY_DATA_PATH"
        const val KEY_DATA_TYPE = "KEY_DATA_TYPE"
        const val KEY_MAX_TIME = "KEY_MAX_TIME"
        const val KEY_MIN_TIME = "KEY_MIN_TIME"

        var recordVideoCallback: RecordVideoCallback? = null

        fun show(activity: Activity, maxRecordTime: Int = 15, minRecordTime: Int = 3) {
            ActivityHelper.build(activity)
                .defaultEnterAnim()
                .setClass(RecordVideoActivity::class.java)
                .setBundle(Bundle().apply {
                    putInt(KEY_MAX_TIME, maxRecordTime)
                    putInt(KEY_MIN_TIME, minRecordTime)
                })
                .doIt(REQUEST_CODE)
        }

        fun getResultPath(requestCode: Int, resultCode: Int, data: Intent?): String? {
            return if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                data?.getStringExtra(KEY_DATA_PATH)
            } else {
                null
            }
        }

        /**@see [PictureConfig.TYPE_VIDEO]]*/
        fun getResultType(requestCode: Int, resultCode: Int, data: Intent?): Int {
            return if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                data?.getIntExtra(KEY_DATA_TYPE, PictureConfig.TYPE_IMAGE)
                    ?: PictureConfig.TYPE_IMAGE
            } else {
                PictureConfig.TYPE_IMAGE
            }
        }
    }

    override fun getActivityLayoutId(): Int {
        return R.layout.activity_record_video
    }

    override fun onDestroy() {
        super.onDestroy()
        recordVideoCallback = null
    }

    override fun toMain() {
        FragmentHelper.restoreShow(
            baseContext,
            supportFragmentManager,
            R.id.root_layout,
            RecordVideoFragment::class.java
        ).firstOrNull().apply {

            (this as? RecordVideoFragment)?.callback = object : RecordVideoCallback() {
                init {
                    val bundle = ActivityHelper.getBundle(intent)
                    minRecordTime = bundle.getInt(KEY_MIN_TIME, 3)
                    maxRecordTime = bundle.getInt(KEY_MAX_TIME, 10)
                }

                override fun onTakePhotoBefore(photo: Bitmap, width: Int, height: Int): Bitmap {
                    return recordVideoCallback?.onTakePhotoBefore(photo, width, height)
                        ?: super.onTakePhotoBefore(photo, width, height)
                }

                override fun onTakePhotoAfter(photo: Bitmap, width: Int, height: Int): Bitmap {
                    return recordVideoCallback?.onTakePhotoAfter(photo, width, height)
                        ?: super.onTakePhotoAfter(photo, width, height)
                }

                override fun onTakePhoto(bitmap: Bitmap, outputFile: File) {
                    super.onTakePhoto(bitmap, outputFile)
                    result(outputFile.absolutePath, PictureConfig.TYPE_IMAGE)
                }

                override fun onTakeVideo(videoPath: String) {
                    super.onTakeVideo(videoPath)
                    result(videoPath, PictureConfig.TYPE_VIDEO)
                }

                private fun result(path: String, type: Int) {
                    ActivityHelper
                        .build(this@RecordVideoActivity)
                        .setResult(RESULT_OK, Intent().apply {
                            putExtra(KEY_DATA_PATH, path)
                            putExtra(KEY_DATA_TYPE, type)
                        })
                        .defaultExitAnim()
                        .finish()
                }
            }
        }
    }

    override fun checkBackPressed(): Boolean {
        return true
    }
}
