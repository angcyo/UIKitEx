package com.angcyo.uikitex.chart;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.RectF;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.Range;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
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
public class SingleBarChartRenderer extends BarChartRenderer {
    public SingleBarChart singleBarChart;

    //bar 之间的间隙
    public float barSpace = Utils.convertDpToPixel(10f);

    //第一个bar 与边缘的距离
    public float startBarSpace = Utils.convertDpToPixel(20f);

    public SingleBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
        singleBarChart = (SingleBarChart) chart;
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        //super.drawDataSet(c, dataSet, index);

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        // initialize the buffer
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(mChart.getBarData().getBarWidth());

        buffer.feed(dataSet);

        trans.pointValuesToPixel(buffer.buffer);

        final boolean isSingleColor = dataSet.getColors().size() == 1;

        if (isSingleColor) {
            mRenderPaint.setColor(dataSet.getColor());
        }

        float barWidth = getBarWidth();

        for (int j = 0; j < buffer.size(); j += 4) {

            BarEntry entry = dataSet.getEntryForIndex(j / 4);

            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                mRenderPaint.setColor(dataSet.getColor(j / 4));
            }

            if (dataSet.getGradientColor() != null) {
                GradientColor gradientColor = dataSet.getGradientColor();
                mRenderPaint.setShader(
                        new LinearGradient(
                                buffer.buffer[j],
                                buffer.buffer[j + 3],
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                gradientColor.getStartColor(),
                                gradientColor.getEndColor(),
                                android.graphics.Shader.TileMode.MIRROR));
            }

            if (dataSet.getGradientColors() != null) {
                mRenderPaint.setShader(
                        new LinearGradient(
                                buffer.buffer[j],
                                buffer.buffer[j + 3],
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                dataSet.getGradientColor(j / 4).getStartColor(),
                                dataSet.getGradientColor(j / 4).getEndColor(),
                                android.graphics.Shader.TileMode.MIRROR));
            }

            float enterCenterX = getEntryCenterX(entry);

            buffer.buffer[j] = enterCenterX - barWidth / 2;
            buffer.buffer[j + 2] = enterCenterX + barWidth / 2;

            if (isInChartBounds(entry)) {
                c.drawRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], mRenderPaint);
            }
        }
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        //super.drawHighlighted(c, indices);

        BarData barData = mChart.getBarData();

        for (Highlight high : indices) {

            if (high.getDataIndex() == -1) {
                continue;
            }

            float highDrawX = high.getDrawX();

            IBarDataSet set = barData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            BarEntry e = set.getEntryForIndex(high.getDataIndex());

            Transformer trans = mChart.getTransformer(set.getAxisDependency());

            mHighlightPaint.setColor(set.getHighLightColor());
            mHighlightPaint.setAlpha(set.getHighLightAlpha());

            boolean isStack = (high.getStackIndex() >= 0 && e.isStacked()) ? true : false;

            final float y1;
            final float y2;

            if (isStack) {

                if (mChart.isHighlightFullBarEnabled()) {

                    y1 = e.getPositiveSum();
                    y2 = -e.getNegativeSum();

                } else {

                    Range range = e.getRanges()[high.getStackIndex()];

                    y1 = range.from;
                    y2 = range.to;
                }

            } else {
                y1 = e.getY();
                y2 = 0.f;
            }

            prepareBarHighlight(e.getX(), y1, y2, barData.getBarWidth() / 2f, trans);

            setHighlightDrawPos(high, mBarRect);
            float offsetX = singleBarChart.endTranslateX;

            high.setDraw(highDrawX + offsetX, mBarRect.top);
            mBarRect.set(highDrawX - getBarWidth() / 2 + offsetX, mBarRect.top,
                    highDrawX + getBarWidth() / 2 + offsetX, mBarRect.bottom);

            c.drawRect(mBarRect, mHighlightPaint);
        }
    }

    //获取entry 对应需要绘制的中心点x坐标
    public float getEntryCenterX(Entry entry) {
        float index = entry.getX();
        float barWidth = getBarWidth();

        float offsetX = singleBarChart.endTranslateX;

        float x =
                mViewPortHandler.contentLeft() +
                        offsetX +
                        startBarSpace +
                        barWidth / 2 +
                        barWidth * index +
                        barSpace * (Math.max(index, 0));
        return x;
    }

    /**
     * 最后一个entry的 中心点 x坐标
     */
    public float getLastEntryCenterX() {
        float result = -1;
        BarData barData = singleBarChart.getBarData();
        if (barData != null) {
            List<IBarDataSet> dataSets = barData.getDataSets();
            if (!dataSets.isEmpty()) {
                IBarDataSet dataSet = dataSets.get(0);

                if (dataSet.getEntryCount() > 0) {

                    int lastIndex = dataSet.getEntryCount() - 1;

                    float barWidth = getBarWidth();
                    result = mViewPortHandler.contentLeft() +
                            startBarSpace +
                            barWidth / 2 +
                            barWidth * lastIndex +
                            barSpace * (Math.max(lastIndex, 0));
                }
            }
        }
        return result;
    }

    /**
     * x轴 最大的值
     */
    public float getMaxX() {
        return getLastEntryCenterX() + getBarWidth() / 2;
    }

    public boolean isInChartBounds(Entry entry) {
        float enterCenterX = getEntryCenterX(entry);
        float barWidth = getBarWidth();

        float offsetX = singleBarChart.endTranslateX;


        if (enterCenterX - barWidth / 2 >= mViewPortHandler.contentLeft() + offsetX &&
                enterCenterX + barWidth / 2 <= mViewPortHandler.contentRight()) {
            return true;
        }
        return false;
    }

    public float getBarWidth() {
        BarData barData = mChart.getBarData();
        float barWidth = barData.getBarWidth();
        return barWidth;
    }

    /**
     * 通过entry 的 x y值, 拿到对应 绘制 坐标系的 x y 值(左上角起点)
     */
    public RectF calcXY(float x, float y) {
        BarData barData = mChart.getBarData();
        if (barData != null && barData.getDataSets().size() > 0) {
            IBarDataSet set = barData.getDataSetByIndex(0);
            Transformer trans = mChart.getTransformer(set.getAxisDependency());

            float barWidth = getBarWidth();

            float left = x - barWidth / 2;
            float right = x + barWidth / 2;
            float top = y;
            float bottom = 0;

            mBarRect.set(left, top, right, bottom);

            trans.rectToPixelPhase(mBarRect, mAnimator.getPhaseY());
        }
        return mBarRect;
    }
}
