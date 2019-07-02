package exocr.exocrengine;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import com.angcyo.opencv.R;

/**
 * description: 扫描框
 * create by kalu on 2018/11/20 13:28
 */
public final class CaptureView extends View {
    final Paint mPaint = new Paint();
    final DisplayMetrics metrics;
    /**********************************************************************************************/

//    private final DashPathEffect mDashPathEffect = new DashPathEffect(new float[]{20f, 10f}, 0);
    private final int[] laserAlpha = {0, 64, 128, 192, 255, 192, 128, 64};
    private boolean isFront = true;
    private int laserAlphaIndex = 0;

    private RectF drawRect = new RectF();
    private float round = 0;
    private Drawable frontDrawable;

    public CaptureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        metrics = getContext().getResources().getDisplayMetrics();
        round = 5 * metrics.density;

        frontDrawable = ContextCompat.getDrawable(context, R.drawable.bd_ocr_id_card_locator_front);
    }

    public void setFront(boolean front) {
        isFront = front;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int canvasHeight = canvas.getHeight();
        final int canvasWidth = canvas.getWidth();
        // Log.e("kalu1", "canvasHeight = " + canvasHeight + ", canvasWidth = " + canvasWidth);

        mPaint.clearShadowLayer();
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setFakeBoldText(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor("#cccccc"));
        final float stroke = 2 * metrics.density;
        mPaint.setStrokeWidth(stroke);
//        mPaint.setPathEffect(mDashPathEffect);

        if (canvasWidth < canvasHeight) {

            final float layerWidth = canvasWidth * 0.83f;
            final float layerHeight = layerWidth / 1.6f;
            final float layerLeft = (canvasWidth - layerWidth) / 2;
            final float layerTop = (canvasHeight - layerHeight) / 2;
            final float layerRight = layerLeft + layerWidth;
            final float layerBottom = layerTop + layerHeight;
            // step1
            canvas.drawRect(layerLeft, layerTop, layerRight, layerBottom, mPaint);
            // step2
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("#66666666"));
            canvas.drawRect(0, 0, layerLeft - stroke / 2, canvasHeight, mPaint);
            canvas.drawRect(layerRight + stroke / 2, 0, canvasWidth, canvasHeight, mPaint);
            // step3
            canvas.drawRect(layerLeft - stroke / 2, 0, layerRight + stroke / 2, layerTop - stroke / 2, mPaint);
            canvas.drawRect(layerLeft - stroke / 2, layerBottom + stroke / 2, layerRight + stroke / 2, canvasHeight, mPaint);
            // step4
            mPaint.setColor(Color.RED);
            mPaint.setAlpha(laserAlpha[laserAlphaIndex]);
//            mPaint.setPathEffect(mDashPathEffect);
            laserAlphaIndex = (laserAlphaIndex + 1) % laserAlpha.length;

            float temp = canvasWidth / 7;
            final int left = (int) (temp * 3 + stroke / 2);
            final int top = (int) (canvasHeight / 2 - stroke / 2);
            final int right = (int) (temp * 4 - stroke / 2);
            final int bottom = (int) (canvasHeight / 2 + stroke / 2);
//            LinearGradient backGradient = new LinearGradient(left, right, top, bottom, new int[]{0x66FF0000, 0xFFFF0000, 0x66FF0000}, null, Shader.TileMode.MIRROR);
//            mPaint.setShader(backGradient);
            canvas.drawRect(left, top, right, bottom, mPaint);
            postInvalidateDelayed(50, left, top, right, bottom);

            if (isFront) {
                drawFace(canvas, mPaint, layerWidth, layerHeight, layerLeft, layerTop);
            } else {
                drawEmblem(canvas, mPaint, layerWidth, layerHeight, layerLeft, layerTop);
            }

            drawBackground(canvas, mPaint, canvasWidth, canvasHeight, layerLeft, layerTop, layerRight, layerBottom);
        } else {

            final float layerHeight = canvasHeight * 0.83f;
            final float layerWidth = layerHeight * 1.6f;
            final float layerLeft = (canvasWidth - layerWidth) / 2;
            final float layerTop = (canvasHeight - layerHeight) / 2;
            final float layerRight = layerLeft + layerWidth;
            final float layerBottom = layerTop + layerHeight;
            // step1
            drawRect.set(layerLeft, layerTop, layerRight, layerBottom);
            canvas.drawRoundRect(drawRect, round, round, mPaint);
            // step2
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("#66666666"));
            canvas.drawRect(0, 0, layerLeft - stroke / 2, canvasHeight, mPaint);
            canvas.drawRect(layerRight + stroke / 2, 0, canvasWidth, canvasHeight, mPaint);
            // step3
            canvas.drawRect(layerLeft - stroke / 2, 0, layerRight + stroke / 2, layerTop - stroke / 2, mPaint);
            canvas.drawRect(layerLeft - stroke / 2, layerBottom + stroke / 2, layerRight + stroke / 2, canvasHeight, mPaint);
            // step4
            mPaint.setColor(Color.RED);
            mPaint.setAlpha(laserAlpha[laserAlphaIndex]);
//            mPaint.setPathEffect(mDashPathEffect);
            laserAlphaIndex = (laserAlphaIndex + 1) % laserAlpha.length;

            float temp = canvasWidth / 7;
            final int left = (int) (temp * 3 + stroke / 2);
            final int top = (int) (canvasHeight / 2 - stroke / 2);
            final int right = (int) (temp * 4 - stroke / 2);
            final int bottom = (int) (canvasHeight / 2 + stroke / 2);
            //            LinearGradient backGradient = new LinearGradient(left, right, top, bottom, new int[]{0x66FF0000, 0xFFFF0000, 0x66FF0000}, null, Shader.TileMode.MIRROR);
//            mPaint.setShader(backGradient);
            drawRect.set(left, top, right, bottom);

            canvas.drawRoundRect(drawRect, round, round, mPaint);
            postInvalidateDelayed(50, left, top, right, bottom);

            if (isFront) {
                drawFace(canvas, mPaint, layerWidth, layerHeight, layerLeft, layerTop);
            } else {
                drawEmblem(canvas, mPaint, layerWidth, layerHeight, layerLeft, layerTop);
            }

            drawBackground(canvas, mPaint, canvasWidth, canvasHeight, layerLeft, layerTop, layerRight, layerBottom);
        }
    }

    private final void drawFace(final Canvas canvas, final Paint paint, final float layerWidth, final float layerHeight, final float layerLeft, final float layerTop) {

        final float faceWidth = layerWidth * 0.3f;
        final float faceHeight = layerHeight * 0.61f;
        final float faceLeft = layerLeft + layerWidth * 0.93f - faceWidth;
        final float faceTop = layerTop + layerHeight * 0.15f;
        final float faceRight = layerLeft + layerWidth * 0.93f;
        final float faceBottom = faceTop + faceHeight;
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(Color.WHITE);
//        canvas.drawRect(faceLeft, faceTop, faceRight, faceBottom, paint);

        frontDrawable.setBounds((int) faceLeft, (int) faceTop, (int) faceRight, (int) faceBottom);
        frontDrawable.draw(canvas);
    }

    private final void drawEmblem(final Canvas canvas, final Paint paint, final float layerWidth, final float layerHeight, final float layerLeft, final float layerTop) {

        final float emblemWidth = layerWidth * 0.19f;
        final float emblemHeight = layerWidth * 0.21f;
        final float emblemTop = layerHeight * 0.08f + layerTop;
        final float emblemLeft = layerHeight * 0.08f + layerLeft;
        final float emblemRight = emblemLeft + emblemWidth;
        final float emblemBottom = emblemTop + emblemHeight;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        canvas.drawRect(emblemLeft, emblemTop, emblemRight, emblemBottom, paint);
    }

    private final void drawBackground(final Canvas canvas, final Paint paint, final float width, final float height, final float layerLeft, final float layerTop, final float layerRight, final float layerBottom) {

        final float strokeWidth = paint.getStrokeWidth();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.parseColor("#242424"));

        final float backgroundTop = layerTop - strokeWidth;
        final float backgroundLeft = layerLeft - strokeWidth;
        final float backgroundRight = layerRight + strokeWidth;
        final float backgroundBottom = layerBottom + strokeWidth;
        canvas.drawRect(backgroundRight, 0, width, height, paint);
        canvas.drawRect(0, 0, backgroundLeft, height, paint);
        canvas.drawRect(backgroundLeft, 0, backgroundRight, backgroundTop, paint);
        canvas.drawRect(backgroundLeft, backgroundBottom, backgroundRight, height, paint);
    }

    /**********************************************************************************************/

    @Override
    public void setBackground(Drawable background) {
    }

    @Override
    public void setBackgroundColor(int color) {
    }

    @Override
    public void setBackgroundResource(int resid) {
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
    }

    @Override
    public void setBackgroundTintList(ColorStateList tint) {
    }

    @Override
    public void setBackgroundTintMode(PorterDuff.Mode tintMode) {
    }

    /**********************************************************************************************/
}
    
