package com.angcyo.rcode.scan

import android.app.Activity
import android.content.Intent
import com.angcyo.rcode.CodeScanFragment
import com.angcyo.uiview.less.base.activity.BasePermissionActivity
import com.angcyo.uiview.less.base.helper.ActivityHelper
import com.angcyo.uiview.less.base.helper.FragmentHelper
import com.angcyo.uiview.less.kotlin.get

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/03
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class ScanActivity : BasePermissionActivity() {

    companion object {
        /**最大值 16bit 65535*/
        const val REQUEST_CODE = 0xFFF1
        const val KEY_DATA = "KEY_DATA"

        /**启动界面*/
        fun show(activity: Activity) {
            ActivityHelper.build(activity)
                .defaultEnterAnim()
                .setClass(ScanActivity::class.java)
                .doIt(REQUEST_CODE)
        }

        /**获取扫码结果*/
        fun getResult(requestCode: Int, resultCode: Int, data: Intent?): String? {
            return if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                data?.getStringExtra(KEY_DATA)
            } else {
                null
            }
        }
    }

    override fun toMain() {
        val fragment = CodeScanFragment()
        fragment.onScanResult = this::onScanResult

        get(supportFragmentManager)
            .parentLayoutId(getFragmentParentLayoutId())
            .noAnim()
            .showFragment(fragment)
            .doIt()
    }

    override fun checkBackPressed(): Boolean {
        return false
    }

    /**扫码结果, 重写覆盖逻辑
     * 返回true, 默认会关闭界面
     * */
    open fun onScanResult(result: String): Boolean {
        ActivityHelper.build(this)
            .setResult(RESULT_OK, Intent().apply {
                putExtra(KEY_DATA, result)
            })
            .defaultExitAnim()
            .finish(false)
        return false
    }

    /**重新扫描*/
    open fun scanAgain() {
        FragmentHelper.find(supportFragmentManager, CodeScanFragment::class.java).firstOrNull()
            ?.let {
                (it as? CodeScanFragment)?.scanAgain()
            }
    }
}
