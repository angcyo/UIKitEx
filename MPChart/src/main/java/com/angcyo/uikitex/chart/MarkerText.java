package com.angcyo.uikitex.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class MarkerText implements IMarker {

    private MPPointF mOffset = new MPPointF();
    private Paint paint;

    /**
     * 需要绘制文本, null 则使用 y值
     */
    private String drawText = null;

    private float textSize = Utils.convertDpToPixel(10);

    private int textColor = Color.rgb(140, 234, 255);

    public MarkerText() {

    }

    public MarkerText(float textSize, int textColor) {
        this.textSize = textSize;
        this.textColor = textColor;
    }

    public void setOffset(MPPointF offset) {
        mOffset = offset;

        if (mOffset == null) {
            mOffset = new MPPointF();
        }
    }

    public void setOffset(float offsetX, float offsetY) {
        mOffset.x = offsetX;
        mOffset.y = offsetY;
    }

    private void ensurePaint() {
        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
    }

    public void setDrawText(String drawText) {
        this.drawText = drawText;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    @Override
    public MPPointF getOffset() {
        return mOffset;
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        return mOffset;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        e.getData();
        if (drawText == null) {
            drawText = String.valueOf(e.getY());
        }
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        ensurePaint();

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and draw
        canvas.translate(offset.x, offset.y);

        float textWidth = paint.measureText(drawText);
        canvas.drawText(drawText, posX - textWidth / 2, posY - paint.descent(), paint);

        canvas.restoreToCount(saveId);
    }
}
