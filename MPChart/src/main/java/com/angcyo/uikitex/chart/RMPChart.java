package com.angcyo.uikitex.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/02/28
 * Copyright (c) 2019 Shenzhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class RMPChart {
    protected Chart chart;

    protected RMPChart(Chart chart) {
        this.chart = chart;
        defaultStyle();
    }

    public static RMPChart get(@NonNull Chart chart) {
        return new RMPChart(chart);
    }

    /**
     * 默认样式
     */
    protected void defaultStyle() {
        setNoDataStyle("数据加载中");
        setDescriptionEnable(false);
        setXAxisPosition(XAxis.XAxisPosition.BOTTOM);
        if (chart instanceof BarLineChartBase) {
            //关闭右边的轴线
            ((BarLineChartBase) chart).getAxisRight().setEnabled(false);
        }
        setLegendEnable(false);
        setLegendOrientation(Legend.LegendOrientation.VERTICAL);
        setLegendHAlign(Legend.LegendHorizontalAlignment.RIGHT);
        setLegendVAlign(Legend.LegendVerticalAlignment.CENTER);

        setDrawPieDrawCenterText(false);
        setPieSliceSpace(2f);
    }

    public RMPChart defaultSingleBarChartStyle() {
        this
                .setTouchEnabled(true)
                .setHighlightPerTapEnabled(true)
                .setHighlightPerDragEnabled(false)
                .setDrawBarShadow(false)
                .setDrawXAxisGridLines(false)
                .setScaleEnabled(false)
                .setDoubleTapToZoomEnabled(false)
                .setDrawBorders(false)
                .setBarWidth(Utils.convertDpToPixel(20f));
        return this;
    }

    //<editor-fold desc="NoData 和 description 样式">

    public RMPChart setNoDataStyle(String text, int color) {
        chart.setNoDataText(text);
        chart.setNoDataTextColor(color);
        return this;
    }

    public RMPChart setNoDataStyle(String text) {
        //默认颜色
        setNoDataStyle(text, Color.rgb(247, 189, 51));
        return this;
    }

    public RMPChart setPaintStyle(Paint paint, int which) {
        chart.setPaint(paint, which);
        return this;
    }

    /**
     * 用来绘制 noData 的画笔
     */
    public RMPChart setInfoPaintStyle(Paint paint) {
        setPaintStyle(paint, Chart.PAINT_INFO);
        return this;
    }

    /**
     * 用来绘制 description 的画笔
     */
    public RMPChart setDescriptionPaintStyle(Paint paint) {
        setPaintStyle(paint, Chart.PAINT_DESCRIPTION);
        return this;
    }

    public RMPChart setDescriptionEnable(boolean enable) {
        chart.getDescription().setEnabled(enable);
        return this;
    }

    /**
     * @see Description#setPosition(float, float)
     */
    public RMPChart setDescriptionPosition(float x, float y) {
        chart.getDescription().setPosition(x, y);
        return this;
    }

    public RMPChart setDescriptionText(String text) {
        chart.getDescription().setText(text);
        return this;
    }

    public RMPChart setDescriptionTextColor(int color) {
        chart.getDescription().setTextColor(color);
        return this;
    }

    /**
     * @param size the text size, in DP
     */
    public RMPChart setDescriptionTextSize(float size) {
        chart.getDescription().setTextSize(size);
        return this;
    }

    public RMPChart setDescriptionTextAlign(Paint.Align align) {
        chart.getDescription().setTextAlign(align);
        return this;
    }

    /**
     * @param xOffset dp default:5f
     * @param yOffset dp default:5f
     */
    public RMPChart setDescriptionOffset(float xOffset, float yOffset) {
        Description description = chart.getDescription();
        description.setXOffset(xOffset);
        description.setYOffset(yOffset);

        MPPointF position = description.getPosition();
        if (position != null) {
            position.x += xOffset;
            position.y += yOffset;
        }

        return this;
    }

    /**
     * * @param size the text size, in DP
     */
    public RMPChart setDescriptionStyle(String text, int color, float size, Paint.Align align) {
        setDescriptionText(text);
        setDescriptionTextColor(color);
        setDescriptionTextSize(size);
        setDescriptionTextAlign(align);
        return this;
    }

    /**
     * 将 描述字符文本移动至左上角显示, 可以用来显示Y轴的单位
     */
    public RMPChart setDescriptionToTopLeft() {
        chart.post(new Runnable() {
            @Override
            public void run() {
                Description description = chart.getDescription();
                ViewPortHandler viewPortHandler = chart.getViewPortHandler();
                setDescriptionTextAlign(Paint.Align.RIGHT);
                setDescriptionPosition(viewPortHandler.contentLeft() + description.getXOffset(),
                        viewPortHandler.contentTop() + description.getYOffset());
                chart.invalidate();
            }
        });
        return this;
    }

    //</editor-fold desc="NoData 和 description 样式">

    //<editor-fold desc="横轴 样式方法">

    public XAxis getXAxis() {
        if (chart instanceof PieChart) {
            //饼状图 不支持 横轴
            return null;
        } else {
            return chart.getXAxis();
        }
    }

    /**
     * 横轴 label 旋转角度
     */
    public RMPChart setXAxisLabelAngle(float angle) {
        XAxis xAxis = getXAxis();
        if (xAxis != null) {
            xAxis.setLabelRotationAngle(angle);
        }
        return this;
    }

    public RMPChart setXAxisPosition(XAxis.XAxisPosition pos) {
        AxisUtil.setAxisPosition(getXAxis(), pos, YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        return this;
    }

    public RMPChart setDrawXAxisLine(boolean draw) {
        AxisUtil.setDrawAxisLine(getXAxis(), draw);
        return this;
    }

    public RMPChart setDrawXAxisGridLines(boolean draw) {
        AxisUtil.setDrawAxisGridLines(getXAxis(), draw);
        return this;
    }

    public RMPChart setDrawXAxisLabels(boolean draw) {
        AxisUtil.setDrawAxisLabels(getXAxis(), draw);
        return this;
    }

    /**
     * 网格线的颜色 (横轴的网格线是垂直横轴的)
     */
    public RMPChart setDrawXAxisGridColor(int color) {
        AxisUtil.setDrawAxisGridColor(getXAxis(), color);
        return this;
    }

    public RMPChart setDrawXAxisTextColor(int color) {
        AxisUtil.setDrawAxisTextColor(getXAxis(), color);
        return this;
    }

    /**
     * 最边上的轴线颜色
     */
    public RMPChart setDrawXAxisLineColor(int color) {
        AxisUtil.setDrawAxisLineColor(getXAxis(), color);
        return this;
    }

    /**
     * @param size dp
     */
    public RMPChart setXAxisLineWidth(float size) {
        AxisUtil.setAxisLineWidth(getXAxis(), size);
        return this;
    }

    /**
     * @param size dp
     */
    public RMPChart setXAxisgGridWidth(float size) {
        AxisUtil.setAxisGridWidth(getXAxis(), size);
        return this;
    }

    /**
     * 网格线的效果(横轴的网格线是垂直横轴的)
     */
    public RMPChart setXAxisGridDashedLine(float lineLength, float spaceLength, float phase) {
        AxisUtil.setAxisGridDashedLine(getXAxis(), lineLength, spaceLength, phase);
        return this;
    }

    /**
     * 轴线的效果, 似乎没有效果, 可能是chart的bug
     */
    public RMPChart setXAxisLineDashedLine(float lineLength, float spaceLength, float phase) {
        AxisUtil.setAxisLineDashedLine(getXAxis(), lineLength, spaceLength, phase);
        return this;
    }

    public RMPChart setXAxisValueFormatter(IAxisValueFormatter formatter) {
        AxisUtil.setAxisValueFormatter(getXAxis(), formatter);
        return this;
    }

    public RMPChart setXAxisLabelCount(int count, boolean force) {
        AxisUtil.setAxisLabelCount(getXAxis(), count, force);
        return this;
    }

    public RMPChart addXAxisLimitLine(LimitLine limitLine) {
        AxisUtil.addAxisLimitLine(getXAxis(), limitLine);
        return this;
    }

    public RMPChart setXAxisEnable(boolean enable) {
        AxisUtil.setAxisEnable(getXAxis(), enable);
        return this;
    }

    //</editor-fold desc="横轴 样式方法">

    //<editor-fold desc="纵轴 样式方法">

    /**
     * 纵轴有2条, 左边和右边.
     */
    public YAxis getLeftYAxis() {
        if (chart instanceof BarLineChartBase) {
            return ((BarLineChartBase) chart).getAxisLeft();
        } else {
            return null;
        }
    }

    public RMPChart setYAxisLeftPosition(XAxis.XAxisPosition pos) {
        AxisUtil.setAxisPosition(getLeftYAxis(), pos, YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        return this;
    }

    public RMPChart setDrawYAxisLeftLine(boolean draw) {
        AxisUtil.setDrawAxisLine(getLeftYAxis(), draw);
        return this;
    }

    public RMPChart setDrawYAxisLeftGridLines(boolean draw) {
        AxisUtil.setDrawAxisGridLines(getLeftYAxis(), draw);
        return this;
    }

    public RMPChart setDrawYAxisLeftLabels(boolean draw) {
        AxisUtil.setDrawAxisLabels(getLeftYAxis(), draw);
        return this;
    }

    /**
     * 网格线的颜色 (横轴的网格线是垂直横轴的)
     */
    public RMPChart setDrawYAxisLeftGridColor(int color) {
        AxisUtil.setDrawAxisGridColor(getLeftYAxis(), color);
        return this;
    }

    public RMPChart setDrawYAxisLeftTextColor(int color) {
        AxisUtil.setDrawAxisTextColor(getLeftYAxis(), color);
        return this;
    }

    /**
     * 最边上的轴线颜色
     */
    public RMPChart setDrawYAxisLeftLineColor(int color) {
        AxisUtil.setDrawAxisLineColor(getLeftYAxis(), color);
        return this;
    }

    /**
     * @param size dp
     */
    public RMPChart setYAxisLeftLineWidth(float size) {
        AxisUtil.setAxisLineWidth(getLeftYAxis(), size);
        return this;
    }

    /**
     * @param size dp
     */
    public RMPChart setYAxisLeftgGridWidth(float size) {
        AxisUtil.setAxisGridWidth(getLeftYAxis(), size);
        return this;
    }

    /**
     * 网格线的效果(横轴的网格线是垂直横轴的)
     */
    public RMPChart setYAxisLeftGridDashedLine(float lineLength, float spaceLength, float phase) {
        AxisUtil.setAxisGridDashedLine(getLeftYAxis(), lineLength, spaceLength, phase);
        return this;
    }

    /**
     * 轴线的效果, 似乎没有效果, 可能是chart的bug
     */
    public RMPChart setYAxisLeftLineDashedLine(float lineLength, float spaceLength, float phase) {
        AxisUtil.setAxisLineDashedLine(getLeftYAxis(), lineLength, spaceLength, phase);
        return this;
    }

    public RMPChart setYAxisLeftValueFormatter(IAxisValueFormatter formatter) {
        AxisUtil.setAxisValueFormatter(getLeftYAxis(), formatter);
        return this;
    }

    public RMPChart setYAxisLeftLabelCount(int count, boolean force) {
        AxisUtil.setAxisLabelCount(getLeftYAxis(), count, force);
        return this;
    }

    public RMPChart addYAxisLeftLimitLine(LimitLine limitLine) {
        AxisUtil.addAxisLimitLine(getLeftYAxis(), limitLine);
        return this;
    }

    public RMPChart setYAxisLeftEnable(boolean enable) {
        AxisUtil.setAxisEnable(getLeftYAxis(), enable);
        return this;
    }

    //</editor-fold desc="纵轴 样式方法">

    //<editor-fold desc="样式方法">

    /**
     * 可以关闭所有手势事件
     */
    public RMPChart setTouchEnabled(boolean enable) {
        chart.setTouchEnabled(enable);
        return this;
    }

    //</editor-fold desc="样式方法">

    //<editor-fold desc="图例 相关方法">

    public RMPChart setLegendEnable(boolean enable) {
        chart.getLegend().setEnabled(enable);
        return this;
    }

    /**
     * 图例横向对齐方向
     */
    public RMPChart setLegendHAlign(Legend.LegendHorizontalAlignment value) {
        chart.getLegend().setHorizontalAlignment(value);
        return this;
    }

    public RMPChart setLegendVAlign(Legend.LegendVerticalAlignment value) {
        chart.getLegend().setVerticalAlignment(value);
        return this;
    }

    /**
     * 图例方向
     */
    public RMPChart setLegendOrientation(Legend.LegendOrientation value) {
        chart.getLegend().setOrientation(value);
        return this;
    }

    /**
     * 方向为 横向时, 多个 图例之间的间隙
     */
    public RMPChart setLegendXEntrySpace(float value) {
        chart.getLegend().setXEntrySpace(value);
        return this;
    }

    public RMPChart setLegendYEntrySpace(float value) {
        chart.getLegend().setYEntrySpace(value);
        return this;
    }

    /**
     * 偏移距离
     */
    public RMPChart setLegendXOffset(float value) {
        chart.getLegend().setXOffset(value);
        return this;
    }

    public RMPChart setLegendYOffset(float value) {
        chart.getLegend().setYOffset(value);
        return this;
    }

    /**
     * 图例形状
     */
    public RMPChart setLegendForm(Legend.LegendForm value) {
        chart.getLegend().setForm(value);
        return this;
    }

    public RMPChart setScaleEnabled(boolean value) {
        if (chart instanceof BarLineChartBase) {
            ((BarLineChartBase) chart).setScaleEnabled(value);
        }
        return this;
    }

    public RMPChart setScaleXEnabled(boolean value) {
        if (chart instanceof BarLineChartBase) {
            ((BarLineChartBase) chart).setScaleXEnabled(value);
        }
        return this;
    }

    public RMPChart setScaleYEnabled(boolean value) {
        if (chart instanceof BarLineChartBase) {
            ((BarLineChartBase) chart).setScaleYEnabled(value);
        }
        return this;
    }

    public RMPChart setDoubleTapToZoomEnabled(boolean value) {
        if (chart instanceof BarLineChartBase) {
            ((BarLineChartBase) chart).setDoubleTapToZoomEnabled(value);
        }
        return this;
    }

    public RMPChart setDrawBorders(boolean value) {
        if (chart instanceof BarLineChartBase) {
            ((BarLineChartBase) chart).setDrawBorders(value);
        }
        return this;
    }

    public RMPChart setHighlightFullBarEnabled(boolean value) {
        if (chart instanceof BarLineChartBase) {
            ((BarChart) chart).setHighlightFullBarEnabled(value);
        }
        return this;
    }

    /**
     * 点击是否高亮
     *
     * @see com.github.mikephil.charting.listener.PieRadarChartTouchListener#onSingleTapUp(MotionEvent)
     * @see com.github.mikephil.charting.listener.BarLineChartTouchListener#onSingleTapUp(MotionEvent)
     */
    public RMPChart setHighlightPerTapEnabled(boolean value) {
        chart.setHighlightPerTapEnabled(value);
        return this;
    }

    /**
     * 点击是否高亮
     *
     * @see com.github.mikephil.charting.listener.BarLineChartTouchListener#onTouch(View, MotionEvent)
     */
    public RMPChart setHighlightPerDragEnabled(boolean value) {
        if (chart instanceof BarLineChartBase) {
            ((BarLineChartBase) chart).setHighlightPerDragEnabled(value);
        }
        return this;
    }

    /**
     * @see Chart#drawMarkers(Canvas)
     */
    public RMPChart setDrawMarkers(boolean value) {
        chart.setDrawMarkers(value);
        return this;
    }

    //</editor-fold desc="图例 相关方法">

    //<editor-fold desc="LineChart 相关方法">

    protected List<ILineDataSet> lineDataSets;
    /**
     * 只用来临时存储变量的值
     */
    protected LineDataSet tempLineDataSet = new LineDataSet(null, null);

    public RMPChart addLineEntry(float x, float y) {
        return addLineEntry(new Entry(x, y));
    }

    public RMPChart addLineEntry(float x, float y, Object data) {
        return addLineEntry(new Entry(x, y, data));
    }

    public RMPChart addLineEntry(float x, float y, Drawable icon) {
        return addLineEntry(new Entry(x, y, icon));
    }

    public RMPChart addLineEntry(float x, float y, Drawable icon, Object data) {
        return addLineEntry(new Entry(x, y, icon, data));
    }

    public RMPChart addLineEntry(Entry entry) {
        ensureLineDataSet();
        entries.add(entry);
        return this;
    }

    protected void ensureLineDataSet() {
        if (lineDataSets == null) {
            lineDataSets = new ArrayList<>();
        }
        if (entries == null) {
            entries = new ArrayList<>();
        }
    }

    public RMPChart setLineDrawCircles(boolean draw) {
        tempLineDataSet.setDrawCircles(draw);
        return this;
    }

    public RMPChart setLineDrawIcons(boolean draw) {
        tempLineDataSet.setDrawIcons(draw);
        return this;
    }

    /**
     * 关闭 hole 绘制, Circle也绘制不出来, 也许是 chart的bug
     */
    public RMPChart setLineDrawCircleHole(boolean draw) {
        tempLineDataSet.setDrawCircleHole(draw);
        return this;
    }

    public RMPChart setLineDrawFilled(boolean draw) {
        tempLineDataSet.setDrawFilled(draw);
        return this;
    }

    public RMPChart setDrawLineValues(boolean draw) {
        tempLineDataSet.setDrawValues(draw);
        return this;
    }

    /**
     * @param radius dp
     */
    public RMPChart setLineCircleRadius(float radius) {
        tempLineDataSet.setCircleRadius(radius);
        return this;
    }

    /**
     * @param radius dp
     */
    public RMPChart setLineCircleHoleRadius(float radius) {
        tempLineDataSet.setCircleHoleRadius(radius);
        return this;
    }

    public RMPChart setLineFillColor(int color) {
        tempLineDataSet.setFillColor(color);
        return this;
    }

    /**
     * 点击之后, 高亮线的颜色
     */
    public RMPChart setLineHighLightColor(int color) {
        tempLineDataSet.setHighLightColor(color);
        return this;
    }

    /**
     * 点击之后, 是否绘制横向的高亮线
     */
    public RMPChart setDrawLineHorizontalHighlightIndicator(boolean value) {
        tempLineDataSet.setDrawHorizontalHighlightIndicator(value);
        return this;
    }

    public RMPChart setDrawLineVerticalHighlightIndicator(boolean value) {
        tempLineDataSet.setDrawVerticalHighlightIndicator(value);
        return this;
    }

    /**
     * 同时控制 横竖 高亮线
     */
    public RMPChart setDrawLineHighlightIndicators(boolean value) {
        tempLineDataSet.setDrawHighlightIndicators(value);
        return this;
    }

    /**
     * surface (0-255), default: 85
     */
    public RMPChart setLineFillAlpha(int color) {
        tempLineDataSet.setFillAlpha(color);
        return this;
    }

    public RMPChart setLineCircleHoleColor(int color) {
        tempLineDataSet.setCircleHoleColor(color);
        return this;
    }

    public RMPChart setLineCircleColors(List<Integer> colors) {
        tempLineDataSet.setCircleColors(colors);
        return this;
    }

    public RMPChart setLineCircleColor(int color) {
        tempLineDataSet.setCircleColor(color);
        return this;
    }

    /**
     * LINEAR,
     * STEPPED,
     * CUBIC_BEZIER,
     * HORIZONTAL_BEZIER
     */
    public RMPChart setLineMode(LineDataSet.Mode mode) {
        tempLineDataSet.setMode(mode);
        return this;
    }

    protected void configLineDataSet(LineDataSet lineDataSet) {
        try {
            Method copy = LineDataSet.class.getDeclaredMethod("copy", LineDataSet.class);
            copy.setAccessible(true);
            copy.invoke(tempLineDataSet, lineDataSet);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (color != null) {
            lineDataSet.setColor(color);
        }
        if (colors != null) {
            lineDataSet.setColors(colors);
        }
    }

    //</editor-fold desc="LineChart 相关方法">

    //<editor-fold desc="PieChart 相关方法">

    protected List<PieEntry> pieEntries;

    /**
     * 只用来保存值
     */
    protected PieDataSet tempPieDataSet = new PieDataSet(null, null);

    public RMPChart setDrawPieHoleEnable(boolean enable) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setDrawHoleEnabled(enable);
        }
        return this;
    }

    public RMPChart setDrawPieHoleColor(int value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setHoleColor(value);
        }
        return this;
    }

    /**
     * 0-100 的值
     *
     * @param value Radius * (Value / 100f)    default 50%
     */
    public RMPChart setDrawPieHoleRadius(float value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setHoleRadius(value);
        }
        return this;
    }

    /**
     * 透明圆的颜色
     */
    public RMPChart setDrawPieTransparentCircleColor(int value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setTransparentCircleColor(value);
        }
        return this;
    }

    /**
     * 透明圆的半径
     */
    public RMPChart setDrawPieTransparentCircleRadius(float value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setTransparentCircleRadius(value);
        }
        return this;
    }

    public RMPChart setDrawPieTransparentCircleAlpha(int value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setTransparentCircleAlpha(value);
        }
        return this;
    }

    /**
     * 绘制 entry 的 label
     */
    public RMPChart setDrawPieEntryLabels(boolean value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setDrawEntryLabels(value);
        }
        return this;
    }

    /**
     * 设置 label 的颜色
     */
    public RMPChart setDrawPieEntryLabelColor(int value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setEntryLabelColor(value);
        }
        return this;
    }

    /**
     * @param value dp
     */
    public RMPChart setDrawPieEntryLabelTextSize(float value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setEntryLabelTextSize(value);
        }
        return this;
    }

    /**
     * 设置薄片之间的间隙
     *
     * @param value dp
     */
    public RMPChart setPieSliceSpace(float value) {
        tempPieDataSet.setSliceSpace(value);
        return this;
    }

    public RMPChart setDrawPieIcons(boolean value) {
        tempPieDataSet.setDrawIcons(value);
        return this;
    }

    /**
     * 绘制 饼状图 上的 值
     */
    public RMPChart setDrawPieValues(boolean value) {
        tempPieDataSet.setDrawValues(value);
        return this;
    }

    /**
     * 绘制 饼状图 上的 值颜色
     */
    public RMPChart setDrawPieValueTextColor(int value) {
        tempPieDataSet.setValueTextColor(value);
        return this;
    }

    /**
     * 绘制 饼状图 上的 值文本大小
     *
     * @param value dp
     */
    public RMPChart setDrawPieValueTextSize(float value) {
        tempPieDataSet.setValueTextSize(value);
        return this;
    }

    public RMPChart setDrawPieValueLineColor(int value) {
        tempPieDataSet.setValueLineColor(value);
        return this;
    }

    public RMPChart setDrawPieValueLineWidth(float value) {
        tempPieDataSet.setValueLineWidth(value);
        return this;
    }

    public RMPChart setDrawPieDrawCenterText(boolean enable) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setDrawCenterText(enable);
        }
        return this;
    }

    /**
     * 支持 span
     */
    public RMPChart setDrawPieCenterText(CharSequence text) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setCenterText(text);
        }
        return this;
    }

    public RMPChart setDrawPieCenterTextSize(float value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setCenterTextSize(value);
        }
        return this;
    }

    public RMPChart setDrawPieCenterTextSizePixels(float value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setCenterTextSizePixels(value);
        }
        return this;
    }

    public RMPChart setDrawPieCenterTextColor(int value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setCenterTextColor(value);
        }
        return this;
    }

    public RMPChart setDrawPieCenterTextOffset(float x, float y) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setCenterTextOffset(x, y);
        }
        return this;
    }

    public RMPChart setDrawPieCenterTextRadiusPercent(float value) {
        if (chart instanceof PieChart) {
            ((PieChart) chart).setCenterTextRadiusPercent(value);
        }
        return this;
    }

    public RMPChart addPieEntry(float value) {
        return addPieEntry(new PieEntry(value));
    }

    public RMPChart addPieEntry(float value, Object data) {
        return addPieEntry(new PieEntry(value, data));
    }

    public RMPChart addPieEntry(float value, Drawable icon) {
        return addPieEntry(new PieEntry(value, icon));
    }

    public RMPChart addPieEntry(float value, Drawable icon, Object data) {
        return addPieEntry(new PieEntry(value, icon, data));
    }

    public RMPChart addPieEntry(float value, String label) {
        return addPieEntry(new PieEntry(value, label));
    }

    public RMPChart addPieEntry(float value, String label, Object data) {
        return addPieEntry(new PieEntry(value, label, data));
    }

    public RMPChart addPieEntry(float value, String label, Drawable icon) {
        return addPieEntry(new PieEntry(value, label, icon));
    }

    public RMPChart addPieEntry(float value, String label, Drawable icon, Object data) {
        return addPieEntry(new PieEntry(value, label, icon, data));
    }

    public RMPChart addPieEntry(PieEntry entry) {
        if (pieEntries == null) {
            pieEntries = new ArrayList<>();
        }
        pieEntries.add(entry);
        return this;
    }

    protected void configPieDataSet(PieDataSet pieDataSet) {
        try {
            Method copy = PieDataSet.class.getDeclaredMethod("copy", PieDataSet.class);
            copy.setAccessible(true);
            copy.invoke(tempPieDataSet, pieDataSet);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (color != null) {
            pieDataSet.setColor(color);
        }
        if (colors != null) {
            pieDataSet.setColors(colors);
        }

        pieDataSet.setSliceSpace(tempPieDataSet.getSliceSpace());
        pieDataSet.setSelectionShift(tempPieDataSet.getSelectionShift());
    }

    //</editor-fold desc="PieChart 相关方法">

    //<editor-fold desc="BarChart 相关方法">

    protected List<IBarDataSet> barDataSets;
    /**
     * 只用来临时存储变量的值
     */
    protected BarDataSet tempBarDataSet = new BarDataSet(new ArrayList<BarEntry>(), null);

    protected Float barWidth = null;

    protected Float groupFromX = null;
    protected float groupGroupSpace;
    protected float groupBarSpace;

    /**
     * 在 横轴 中的相对比例
     * Default 0.85f
     */
    public RMPChart setBarWidth(float value) {
        barWidth = value;
        return this;
    }

    /**
     * 柱子值不够时, 是否绘制阴影背景
     */
    public RMPChart setDrawBarShadow(boolean value) {
        if (chart instanceof BarChart) {
            ((BarChart) chart).setDrawBarShadow(value);
        }
        return this;
    }

    /**
     * 值绘制在bar最大值的上面, 还是下面
     */
    public RMPChart setDrawBarValueAboveBar(boolean value) {
        if (chart instanceof BarChart) {
            ((BarChart) chart).setDrawValueAboveBar(value);
        }
        return this;
    }

    /**
     * 阴影背景颜色
     */
    public RMPChart setDrawBarShadowColor(int value) {
        tempBarDataSet.setBarShadowColor(value);
        return this;
    }

    public RMPChart setDrawBarValues(boolean value) {
        tempBarDataSet.setDrawValues(value);
        return this;
    }

    public RMPChart setDrawBarValueTextColor(int value) {
        tempBarDataSet.setValueTextColor(value);
        return this;
    }

    /**
     * @param value dp
     */
    public RMPChart setDrawBarValueTextSize(float value) {
        tempBarDataSet.setValueTextSize(value);
        return this;
    }

    public RMPChart setDrawBarBorderWidth(float value) {
        tempBarDataSet.setBarBorderWidth(value);
        return this;
    }

    public RMPChart addBarEntry(float x, float y) {
        return addBarEntry(new BarEntry(x, y));
    }

    public RMPChart addBarEntry(float x, float y, Object data) {
        return addBarEntry(new BarEntry(x, y, data));
    }


    public RMPChart addBarEntry(float x, float y, Drawable icon) {
        return addBarEntry(new BarEntry(x, y, icon));
    }


    public RMPChart addBarEntry(float x, float y, Drawable icon, Object data) {
        return addBarEntry(new BarEntry(x, y, icon, data));
    }


    public RMPChart addBarEntry(float x, float[] vals) {
        return addBarEntry(new BarEntry(x, vals));
    }


    public RMPChart addBarEntry(float x, float[] vals, Object data) {
        return addBarEntry(new BarEntry(x, vals, data));
    }

    public RMPChart addBarEntry(float x, float[] vals, Drawable icon) {
        return addBarEntry(new BarEntry(x, vals, icon));
    }

    public RMPChart addBarEntry(float x, float[] vals, Drawable icon, Object data) {
        return addBarEntry(new BarEntry(x, vals, icon, data));
    }

    public RMPChart addBarEntry(BarEntry entry) {
        ensureBarDataSet();
        entries.add(entry);
        return this;
    }

    public RMPChart groupBars(float fromX, float groupSpace, float barSpace) {
        groupFromX = fromX;
        groupGroupSpace = groupSpace;
        groupBarSpace = barSpace;
        return this;
    }

    protected void ensureBarDataSet() {
        if (barDataSets == null) {
            barDataSets = new ArrayList<>();
        }
        if (entries == null) {
            entries = new ArrayList<>();
        }
    }


    protected void configBarDataSet(BarDataSet barDataSet) {
        try {
            Method copy = BarDataSet.class.getDeclaredMethod("copy", BarDataSet.class);
            copy.setAccessible(true);
            copy.invoke(tempBarDataSet, barDataSet);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (color != null) {
            barDataSet.setColor(color);
        }
        if (colors != null) {
            barDataSet.setColors(colors);
        }
    }

    //</editor-fold desc="BarChart 相关方法">


    //<editor-fold desc="公共方法">

    protected List<BaseEntry> entries;
    protected String label = null;
    /**
     * LineChart里面表示线的颜色, 多根线, 对应多个颜色
     */
    protected Integer color = null;
    protected List<Integer> colors = null;

    protected IValueFormatter valueFormatter;

    /**
     * 设置 Label
     */
    public RMPChart setLabel(String label) {
        this.label = label;
        return this;
    }

    public RMPChart setColor(Integer color) {
        this.color = color;
        return this;
    }

    public RMPChart setColors(List<Integer> colors) {
        this.colors = colors;
        return this;
    }

    public RMPChart setValueFormatter(IValueFormatter formatter) {
        this.valueFormatter = formatter;
        return this;
    }

    /**
     * 图表 偏移
     */
    public RMPChart setChartExtraOffsets(float left, float top, float right, float bottom) {
        chart.setExtraOffsets(left, top, right, bottom);
        return this;
    }

    /**
     * 添加新的一组数据
     */
    public RMPChart addNew() {
        if (entries == null) {
            return this;
        }
        if (lineDataSets != null) {
            List<Entry> list = new ArrayList<>();
            for (BaseEntry entry : entries) {
                list.add((Entry) entry);
            }

            LineDataSet lineDataSet = new LineDataSet(list, label);
            configLineDataSet(lineDataSet);
            lineDataSets.add(lineDataSet);
        } else if (barDataSets != null) {
            List<BarEntry> list = new ArrayList<>();
            for (BaseEntry entry : entries) {
                list.add((BarEntry) entry);
            }

            BarDataSet barDataSet = new BarDataSet(list, label);
            configBarDataSet(barDataSet);
            barDataSets.add(barDataSet);
        }
        reset();
        return this;
    }

    protected void reset() {
        label = null;
        entries = null;
        color = null;
        colors = null;
    }

    /**
     * 设置数据
     */
    public RMPChart doIt() {
        if (chart instanceof LineChart) {
            if (lineDataSets != null) {
                if (entries != null) {
                    addNew();
                }
                LineData lineData = new LineData(lineDataSets);
                configCharData(lineData);
                chart.setData(lineData);
            }
        } else if (chart instanceof PieChart) {
            if (pieEntries != null) {
                PieDataSet pieDataSet = new PieDataSet(pieEntries, label);
                configPieDataSet(pieDataSet);

                PieData pieData = new PieData(pieDataSet);
                configCharData(pieData);
                chart.setData(pieData);
            }
        } else if (chart instanceof BarChart) {
            if (barDataSets != null) {
                if (entries != null) {
                    addNew();
                }
                BarData barData = new BarData(barDataSets);
                if (barWidth != null) {
                    barData.setBarWidth(barWidth);
                }
                configCharData(barData);
                if (groupFromX != null) {
                    barData.groupBars(groupFromX, groupGroupSpace, groupBarSpace);
                }
                chart.setData(barData);
            }
        }
        return this;
    }

    protected void configCharData(ChartData chartData) {
        if (chartData != null) {
            if (valueFormatter != null) {
                chartData.setValueFormatter(valueFormatter);
            }
        }
    }
    //</editor-fold desc="公共方法">

    /**
     * 轴线操作类
     */
    static class AxisUtil {

        public static void setAxisEnable(AxisBase axis, boolean enable) {
            if (axis != null) {
                axis.setEnabled(enable);
            }
        }

        public static void setAxisPosition(AxisBase axis,
                                           XAxis.XAxisPosition posX,
                                           YAxis.YAxisLabelPosition posY) {
            if (axis instanceof XAxis) {
                ((XAxis) axis).setPosition(posX);
            } else if (axis instanceof YAxis) {
                ((YAxis) axis).setPosition(posY);
            }
        }

        public static void setDrawAxisLine(AxisBase axis, boolean draw) {
            if (axis != null) {
                axis.setDrawAxisLine(draw);
            }
        }

        public static void setDrawAxisGridLines(AxisBase axis, boolean draw) {
            if (axis != null) {
                axis.setDrawGridLines(draw);
            }

        }

        public static void setDrawAxisLabels(AxisBase axis, boolean draw) {
            if (axis != null) {
                axis.setDrawLabels(draw);
            }
        }

        public static void setDrawAxisGridColor(AxisBase axis, int color) {
            if (axis != null) {
                axis.setGridColor(color);
            }
        }

        public static void setDrawAxisTextColor(AxisBase axis, int color) {
            if (axis != null) {
                axis.setTextColor(color);
            }
        }

        /**
         * 最边上的轴线颜色
         */
        public static void setDrawAxisLineColor(AxisBase axis, int color) {
            if (axis != null) {
                axis.setAxisLineColor(color);
            }
        }

        /**
         * @param size dp
         */
        public static void setAxisLineWidth(AxisBase axis, float size) {
            if (axis != null) {
                axis.setAxisLineWidth(size);
            }
        }

        /**
         * @param size dp
         */
        public static void setAxisGridWidth(AxisBase axis, float size) {
            if (axis != null) {
                axis.setGridLineWidth(size);
            }
        }

        /**
         * 网格线的效果(横轴的网格线是垂直横轴的)
         */
        public static void setAxisGridDashedLine(AxisBase axis, float lineLength, float spaceLength, float phase) {
            if (axis != null) {
                axis.enableGridDashedLine(lineLength, spaceLength, phase);
            }
        }

        /**
         * 轴线的效果, 似乎没有效果, 可能是chart的bug
         */
        public static void setAxisLineDashedLine(AxisBase axis, float lineLength, float spaceLength, float phase) {
            if (axis != null) {
                axis.enableAxisLineDashedLine(lineLength, spaceLength, phase);
            }
        }

        public static void setAxisValueFormatter(AxisBase axis, IAxisValueFormatter formatter) {
            if (axis != null) {
                axis.setValueFormatter(formatter);
            }
        }

        public static void setAxisLabelCount(AxisBase axis, int count, boolean force) {
            if (axis != null) {
                axis.setLabelCount(count, force);
            }
        }

        public static void addAxisLimitLine(AxisBase axis, LimitLine limitLine) {
            if (axis != null) {
                axis.addLimitLine(limitLine);
            }
        }
    }
}
