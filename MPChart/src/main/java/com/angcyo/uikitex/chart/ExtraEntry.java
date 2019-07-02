package com.angcyo.uikitex.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import androidx.annotation.NonNull;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/04
 */
public class ExtraEntry extends Entry {
    public ExtraEntry() {
    }

    public ExtraEntry(float x, float y) {
        super(x, y);
    }

    public ExtraEntry(float x, float y, Object data) {
        super(x, y, data);
    }

    public ExtraEntry(float x, float y, Drawable icon) {
        super(x, y, icon);
    }

    public ExtraEntry(float x, float y, Drawable icon, Object data) {
        super(x, y, icon, data);
    }

    public ExtraEntry(Parcel in) {
        super(in);
    }

    boolean drawExtra = false;

    float circleRadius = -1;
    /**
     * 在原来的基础上 offset
     */
    float circleRadiusOffset = Utils.convertDpToPixel(2f);

    int circleColor = -1;

    public ExtraEntry(float x, float y, float circleRadiusOffset, int circleColor) {
        super(x, y);
        drawExtra = true;
        this.circleRadiusOffset = circleRadiusOffset;
        this.circleColor = circleColor;
    }

    public ExtraEntry(float x, float y, int circleColor, float circleRadius) {
        super(x, y);
        drawExtra = true;
        this.circleRadius = circleRadius;
        this.circleColor = circleColor;
    }

    public ExtraEntry(float x, float y, int circleColor) {
        super(x, y);
        drawExtra = true;
        this.circleColor = circleColor;
    }

    /**
     * 自定义绘制
     */
    public void drawExtra(@NonNull Canvas canvas,
                          @NonNull Paint paint,
                          @NonNull ILineDataSet lineDataSet,
                          float x, float y) {

        if (drawExtra) {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if (circleColor != -1) {
                paint.setColor(circleColor);
            }

            float drawRadius;

            if (circleRadius != -1) {
                drawRadius = circleRadius;
            } else {
                drawRadius = lineDataSet.getCircleRadius() + circleRadiusOffset;
            }

            canvas.drawCircle(x, y, drawRadius, paint);
        }
    }
}
