package com.angcyo.uikitex.chart;

import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/04
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class SingleViewPortHandler extends ViewPortHandler {
    SingleBarChartRenderer singleBarChartRenderer;

    public SingleViewPortHandler() {

    }

    public void setSingleBarChartRenderer(SingleBarChartRenderer singleBarChartRenderer) {
        this.singleBarChartRenderer = singleBarChartRenderer;
    }

    @Override
    public boolean isFullyZoomedOut() {
        return super.isFullyZoomedOut();
    }

    @Override
    public boolean hasNoDragOffset() {
        return false;//super.hasNoDragOffset() || false;
    }

    public float mTransOffsetX = 0f;

    @Override
    public void setDragOffsetX(float offset) {
        super.setDragOffsetX(offset);
        mTransOffsetX = Utils.convertDpToPixel(offset);
    }
}
