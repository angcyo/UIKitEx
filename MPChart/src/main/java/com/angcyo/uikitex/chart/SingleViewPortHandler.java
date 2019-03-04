package com.angcyo.uikitex.chart;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/04
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class SingleViewPortHandler extends ViewPortHandler {
    SingleBarChartRenderer singleBarChartRenderer;
    SingleBarChart singleBarChart;

    public SingleViewPortHandler() {

    }

    public void setSingleBarChartRenderer(SingleBarChartRenderer singleBarChartRenderer) {
        this.singleBarChartRenderer = singleBarChartRenderer;
    }

    public void setSingleBarChart(SingleBarChart singleBarChart) {
        this.singleBarChart = singleBarChart;
    }

    @Override
    public boolean isFullyZoomedOut() {
        return super.isFullyZoomedOut();
    }

    @Override
    public boolean hasNoDragOffset() {
        boolean needDrag = false;

        if (singleBarChart != null) {
            BarData barData = singleBarChart.getBarData();
            if (barData != null) {
                List<IBarDataSet> dataSets = barData.getDataSets();
                if (!dataSets.isEmpty()) {
                    IBarDataSet dataSet = dataSets.get(0);

                    if (dataSet.getEntryCount() > 0) {
                        BarEntry lastEntry = dataSet.getEntryForIndex(dataSet.getEntryCount() - 1);

                        float lastX = singleBarChartRenderer.getEnterCenterX(lastEntry);
                        if (lastX + singleBarChartRenderer.getBarWidth() / 2 >= contentRight()) {
                            needDrag = true;
                        }
                    }
                }
            }
        }

        return super.hasNoDragOffset() && !needDrag;
    }

    public float mTransOffsetX = 0f;

    @Override
    public void setDragOffsetX(float offset) {
        super.setDragOffsetX(offset);
        mTransOffsetX = Utils.convertDpToPixel(offset);
    }
}
