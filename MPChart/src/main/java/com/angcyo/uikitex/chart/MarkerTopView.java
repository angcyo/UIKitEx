package com.angcyo.uikitex.chart;

import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.NonNull;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * 会在图表的最顶部显示, 也就是 Y轴 不受点的坐标控制, 固定在顶部. x 轴居中
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/06
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class MarkerTopView extends MarkerView {

    ViewPortHandler viewPortHandler;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public MarkerTopView(Context context, int layoutResource, ViewPortHandler viewPortHandler) {
        super(context, layoutResource);

        this.viewPortHandler = viewPortHandler;
    }

    @Override
    public void refreshContent(Entry entry, Highlight highlight) {
        initLayout(this, entry, highlight);
        super.refreshContent(entry, highlight);
    }

    public void initLayout(@NonNull MarkerTopView markerTopView, @NonNull Entry entry, @NonNull Highlight highlight) {

    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        MPPointF offset = super.getOffsetForDrawingAtPoint(posX, posY);
        offset.y = -posY;
        offset.x = -getMeasuredWidth() / 2;
        return offset;
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        super.draw(canvas, posX, posY);
    }
}
