package com.angcyo.opencv

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.angcyo.uiview.less.base.BaseAppCompatActivity
import com.angcyo.uiview.less.base.helper.ActivityHelper
import com.angcyo.uiview.less.base.helper.FragmentHelper
import exocr.exocrengine.EXOCRModel
import exocr.exocrengine.OcrSurfaceView

open class CardOcrScanActivity : BaseAppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 220
        const val KEY_DATA = "KEY_DATA"

        fun show(activity: Activity) {
            ActivityHelper.build(activity)
                .defaultEnterAnim()
                .setClass(CardOcrScanActivity::class.java)
                .doIt(REQUEST_CODE)
        }

        fun getResult(requestCode: Int, resultCode: Int, data: Intent?): EXOCRModel? {
            return if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                data?.getSerializableExtra(KEY_DATA) as? EXOCRModel
            } else {
                null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame)

        FragmentHelper.restoreShow(
            baseContext,
            supportFragmentManager,
            R.id.root_layout,
            CardOcrScanFragment::class.java
        ).firstOrNull().apply {

            (this as? CardOcrScanFragment)?.onResultCallback =
                OcrSurfaceView.OnOcrChangeListener { exocrModel ->
                    exocrModel?.let {
                        setResult(RESULT_OK, Intent().apply {
                            putExtra(KEY_DATA, it)
                        })
                    }
                    finish()
                }
        }
    }


    override fun checkBackPressed(): Boolean {
        return true
    }
}
