package com.angcyo.uikitex.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

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
                Log.i("angcyo", "onChartGestureStart");
                translateX = endTranslateX;
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                //translateX = endTranslateX;

                Log.i("angcyo", "onChartGestureEnd");
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
                Log.i("angcyo", "x1:" + me1.getX() + " x2:" + me2.getX());
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
                    setDragOffsetX(dX);
                }

                Log.i("angcyo", "dx:" + dX + " dy:" + dY);
            }
        });
    }

    public SingleBarChartRenderer getSingleBarChartRenderer() {
        return (SingleBarChartRenderer) mRenderer;
    }
}
