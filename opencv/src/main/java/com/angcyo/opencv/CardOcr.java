package com.angcyo.opencv;

import android.content.Context;
import com.angcyo.lib.L;
import exocr.exocrengine.EXOCREngine;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class CardOcr {
    public static void init(Context context) {
        boolean b = EXOCREngine.InitDict(context);
        if (!b) {
            L.i("ocr engine 初始化失败.");
        }
    }
}
