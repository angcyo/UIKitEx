package com.angcyo.uikitex.chart;

import android.graphics.Canvas;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/04
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class SingleXAxisRenderer extends XAxisRenderer {
    SingleBarChartRenderer singleBarChartRenderer;

    public SingleXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans, SingleBarChartRenderer singleBarChartRenderer) {
        super(viewPortHandler, xAxis, trans);
        this.singleBarChartRenderer = singleBarChartRenderer;
    }

    @Override
    public void renderAxisLabels(Canvas c) {
        super.renderAxisLabels(c);
    }

    @Override
    protected void drawLabels(Canvas c, float pos, MPPointF anchor) {
        //super.drawLabels(c, pos, anchor);
        List<IBarDataSet> dataSets = singleBarChartRenderer.singleBarChart.getBarData().getDataSets();
        if (dataSets != null && dataSets.size() > 0) {
            IBarDataSet dataSet = dataSets.get(0);

            final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();

            for (int i = 0; i < dataSet.getEntryCount(); i++) {
                BarEntry entry = dataSet.getEntryForIndex(i);
                float enterCenterX = singleBarChartRenderer.getEnterCenterX(entry);

                if (!mViewPortHandler.isInBoundsLeft(enterCenterX) || !mViewPortHandler.isInBoundsRight(enterCenterX))
                    continue;

                String label = mXAxis.getValueFormatter().getFormattedValue(i, mXAxis);

                drawLabel(c, label, enterCenterX, pos, anchor, labelRotationAngleDegrees);
            }
        }
    }

    @Override
    protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
        super.drawLabel(c, formattedLabel, x, y, anchor, angleDegrees);
    }

}
