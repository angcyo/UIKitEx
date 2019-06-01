package com.angcyo.camera.play;

import android.graphics.Matrix;


public class ScaleVideo {

    private Size mViewSize;
    private Size mVideoSize;

    public ScaleVideo(Size viewSize, Size videoSize) {
        mViewSize = viewSize;
        mVideoSize = videoSize;
    }

    public Matrix getScaleMatrix(ScalableType scalableType) {
        switch (scalableType) {
            case NONE:
                return getNoScale();
            case FIT_XY:
                return fitXY();
            case FIT_CENTER:
                return fitCenter();
            case FIT_START:
                return fitStart();
            case FIT_END:
                return fitEnd();
            case CENTER_CROP:
                return getCropScale(PivotPoint.CENTER);

            default:
                return null;
        }
    }

    private Matrix getMatrix(float sx, float sy, float px, float py) {
        Matrix matrix = new Matrix();
        matrix.setScale(sx, sy, px, py);
        return matrix;
    }

    private Matrix getMatrix(float sx, float sy, PivotPoint pivotPoint) {
        switch (pivotPoint) {
            case LEFT_TOP:
                return getMatrix(sx, sy, 0, 0);
            case CENTER:
                return getMatrix(sx, sy, mViewSize.getWidth() / 2f, mViewSize.getHeight() / 2f);
            case RIGHT_BOTTOM:
                return getMatrix(sx, sy, mViewSize.getWidth(), mViewSize.getHeight());

            default:
                throw new IllegalArgumentException("Illegal PivotPoint");
        }
    }

    private Matrix getNoScale() {
        float sx = mVideoSize.getWidth() / (float) mViewSize.getWidth();
        float sy = mVideoSize.getHeight() / (float) mViewSize.getHeight();
        return getMatrix(sx, sy, PivotPoint.LEFT_TOP);
    }

    private Matrix getFitScale(PivotPoint pivotPoint) {
        float sx = (float) mViewSize.getWidth() / mVideoSize.getWidth();
        float sy = (float) mViewSize.getHeight() / mVideoSize.getHeight();
        float minScale = Math.min(sx, sy);
        sx = minScale / sx;
        sy = minScale / sy;
        return getMatrix(sx, sy, pivotPoint);
    }


    private Matrix getCropScale(PivotPoint pivotPoint) {
        float sx = (float) mViewSize.getWidth() / mVideoSize.getWidth();
        float sy = (float) mViewSize.getHeight() / mVideoSize.getHeight();
        float maxScale = Math.max(sx, sy);
        sx = maxScale / sx;
        sy = maxScale / sy;
        return getMatrix(sx, sy, pivotPoint);
    }


    private Matrix fitXY() {
        return getMatrix(1, 1, PivotPoint.LEFT_TOP);
    }

    private Matrix fitStart() {
        return getFitScale(PivotPoint.LEFT_TOP);
    }

    private Matrix fitCenter() {
        return getFitScale(PivotPoint.CENTER);
    }

    private Matrix fitEnd() {
        return getFitScale(PivotPoint.RIGHT_BOTTOM);
    }

}
