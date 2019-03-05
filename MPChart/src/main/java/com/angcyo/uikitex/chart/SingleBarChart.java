package com.angcyo.uikitex.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.List;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/04
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class SingleBarChart extends BarChart {
    public SingleBarChart(Context context) {
        super(context);
    }

    public SingleBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SingleBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    protected float translateX = 0f;
    protected float endTranslateX = 0f;

    @Override
    protected void init() {
        mViewPortHandler = new SingleViewPortHandler();

        super.init();

        mDoubleTapToZoomEnabled = false;
        mDrawBorders = false;
        //setDrawBarShadow(false);
        //mXAxis.setDrawGridLines(false);
        //setScaleXEnabled(false);
        //setScaleYEnabled(false);

        mRenderer = new SingleBarChartRenderer(this, mAnimator, mViewPortHandler);
        ((SingleViewPortHandler) mViewPortHandler).setSingleBarChartRenderer((SingleBarChartRenderer) mRenderer);
        ((SingleViewPortHandler) mViewPortHandler).setSingleBarChart(this);

        mXAxisRenderer = new SingleXAxisRenderer(mViewPortHandler, mXAxis, mLeftAxisTransformer, (SingleBarChartRenderer) mRenderer);

        setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                //Log.i("angcyo", "onChartGestureStart");
                translateX = endTranslateX;
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                //translateX = endTranslateX;
                //Log.i("angcyo", "onChartGestureEnd");
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                //Log.i("angcyo", "x1:" + me1.getX() + " x2:" + me2.getX());
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                endTranslateX = translateX + dX;
                if (endTranslateX > 0) {
                    endTranslateX = 0;
                } else {
                    float maxX = getSingleBarChartRenderer().getMaxX() - getViewPortHandler().contentWidth();
                    if (endTranslateX < -maxX) {
                        endTranslateX = -maxX;
                    }
                }
            }
        });
    }

    @Override
    public Highlight getHighlightByTouchPoint(float x, float y) {
        if (getBarData() != null) {
            List<IBarDataSet> dataSets = getBarData().getDataSets();
            if (!dataSets.isEmpty()) {
                IBarDataSet dataSet = dataSets.get(0);

                if (dataSet.getEntryCount() > 0) {

                    float barWidth = getSingleBarChartRenderer().getBarWidth();
                    float startBarSpace = getSingleBarChartRenderer().startBarSpace;
                    float barSpace = getSingleBarChartRenderer().barSpace;

                    float left = 0;
                    float right = 0;
                    for (int i = 0; i < dataSet.getEntryCount(); i++) {
                        BarEntry entry = dataSet.getEntryForIndex(i);

                        left = mViewPortHandler.contentLeft() +
                                startBarSpace +
                                barWidth * i +
                                barSpace * (Math.max(i, 0));

                        right = left + barWidth;

                        RectF rectF = getSingleBarChartRenderer().calcXY(entry.getX(), entry.getY());

                        if (x - endTranslateX >= left && x - endTranslateX <= right &&
                                y >= rectF.top && y <= rectF.bottom) {
                            Highlight highlight = new Highlight(entry.getX(), entry.getY(), 0);
                            //y坐标有问题, 不能用
                            highlight.setDraw(left + barWidth / 2, entry.getY());
                            highlight.setDataIndex(i);
                            return highlight;
                        }
                    }
                }
            }
        }
        return super.getHighlightByTouchPoint(x, y);
    }

    @Override
    protected void drawMarkers(Canvas canvas) {
        try {
            super.drawMarkers(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void setData(BarData data) {
        super.setData(data);
        highlightValues(null);
    }

    public SingleBarChartRenderer getSingleBarChartRenderer() {
        return (SingleBarChartRenderer) mRenderer;
    }

    public void resetTranslate() {
        endTranslateX = 0;
        translateX = 0;
    }
}
