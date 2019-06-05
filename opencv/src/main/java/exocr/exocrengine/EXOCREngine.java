package exocr.exocrengine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import com.angcyo.opencv.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * description: jni
 * create by kalu on 2018/11/20 10:24
 */
public final class EXOCREngine {

    static final String LIB_FILE_NAME_LIB = "zocr0.lib";

    private static final byte[] info = new byte[4096];
    private static final int[] rects = new int[32];

    static {
        System.loadLibrary("exocrenginec");
    }

    private EXOCREngine() {
    }

    public static final void clearDict() {

        int code = EXOCREngine.nativeDone();
        Log.e("kalu", "clearDict ==> code = " + code);
    }

    private static final boolean checkSign(final Context context) {
        int code = EXOCREngine.nativeCheckSignature(context);
        Log.e("kalu", "checkSign ==> code = " + code);
        return code == 1;
    }

    private static final boolean checkDict(final String path) {

        final byte[] bytes = path.getBytes();
        final int code = EXOCREngine.nativeInit(bytes);
        Log.e("kalu", "checkDict ==> code = " + code);
        return code >= 0;
    }

    private static final boolean checkFile(final Context context, final String pathname) {

        final File file = new File(pathname);
        if (!file.exists() || file.isDirectory()) {

            file.delete();

            try {
                file.createNewFile();

                int byteread;
                final byte[] buffer = new byte[1024];

                //final InputStream is = context.getAssets().open(fileName);
                final InputStream is = context.getResources().openRawResource(R.raw.zocr0);
                final OutputStream fs = new FileOutputStream(file);// to为要写入sdcard中的文件名称

                while ((byteread = is.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                is.close();
                fs.close();
                return true;
            } catch (Exception e) {
                Log.e("kalu", "checkFile ==> message = " + e.getMessage(), e);
                return false;
            }
        } else {
            return true;
        }
    }

    public static final boolean InitDict(final Context activity) {

        final String path = activity.getCacheDir().getAbsolutePath();
        final String pathname = path + "/" + LIB_FILE_NAME_LIB;

        // step1: 检测字典是否存在
        boolean okFile = checkFile(activity, pathname);
        if (!okFile) {

            clearDict();
            return false;
        }

        // step2: 检测字典是否正确
        boolean okDict = checkDict(path);
        if (!okDict) {

            clearDict();

            return false;
        }

        return true;
    }

    /**********************************************************************************************/

    /**
     * 解析相机数据
     *
     * @param image  帧数据
     * @param width  相机预览分辨率，宽
     * @param height 相机预览分辨率，高
     * @return
     */
    public static EXOCRModel decodeByte(final byte[] image, final int width, final int height) {

        if (null == image) {
            return null;
        }

        final int code = nativeRecoIDCardRawdat(image, width, height, width, 1, info, info.length);
        if (code < 0) {
            return null;
        } else {

            final Bitmap bitmap = nativeGetIDCardStdImg(image, width, height, info, info.length, rects);
            if (null == bitmap) {
                return null;
            } else {
                final EXOCRModel decode = EXOCRModel.decode(info, code);
                decode.bitmapToBase64(bitmap);
                // Log.e("jsjs", decode.toString());

                if (null != bitmap) {
                    bitmap.recycle();
                }

                return decode;
            }
        }
    }

    /**
     * 解析本地图片
     *
     * @param bitmap
     * @return
     */
    public static EXOCRModel decodeBitmap(Bitmap bitmap) {

        final int code = nativeRecoIDCardBitmap(bitmap, info, info.length);
        if (code < 0) {
            final EXOCRModel decode = EXOCRModel.decode(info, code);
            return decode;
        } else {
            return null;
        }
    }

    /**
     * 解析本地图片
     *
     * @param is
     * @return
     */
    public static EXOCRModel decodeBitmap(final InputStream is) {

        if (null == is) {
            return null;
        }

        final Bitmap bitmap = BitmapFactory.decodeStream(is);
        if (null == bitmap) {
            return null;
        }

        final int code = nativeRecoIDCardBitmap(bitmap, info, info.length);

        if (code < 0) {
            final EXOCRModel decode = EXOCRModel.decode(info, code);
            return decode;
        } else {
            return null;
        }
    }

    /**
     * 解析本地图片
     *
     * @param path
     * @return
     */
    public static EXOCRModel decodeBitmap(final String path) {

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        final Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (null == bitmap) {
            return null;
        }

        final int code = nativeRecoIDCardBitmap(bitmap, info, info.length);

        if (code < 0) {
            final EXOCRModel decode = EXOCRModel.decode(info, code);
            return decode;
        } else {
            return null;
        }
    }

    /**
     * 解析本地图片
     *
     * @param resources
     * @param resid
     * @return
     */
    public static EXOCRModel decodeBitmap(final Resources resources, final int resid) {

        final Bitmap bitmap = BitmapFactory.decodeResource(resources, resid);
        if (null == bitmap) {
            return null;
        }

        final int code = nativeRecoIDCardBitmap(bitmap, info, info.length);

        if (code < 0) {
            final EXOCRModel decode = EXOCRModel.decode(info, code);
            return decode;
        } else {
            return null;
        }
    }

    /**********************************************************************************************/

    private static native int nativeGetVersion(byte[] exversion);

    private static native int nativeInit(byte[] dbpath);

    private static native int nativeDone();

    private static native int nativeCheckSignature(Context context);

    private static native int nativeRecoIDCardBitmap(Bitmap bitmap, byte[] bresult, int maxsize);

    private static native Bitmap nativeRecoIDCardStillImage(Bitmap bitmap, int tryhard, int bwantimg, byte[] bresult, int maxsize, int[] rets);

    private static native Bitmap nativeRecoIDCardStillImageV2(Bitmap bitmap, int tryhard, int bwantimg, byte[] bresult, int maxsize, int[] rects, int[] rets);

    private static native int nativeRecoIDCardRawdat(byte[] imgdata, int width, int height, int pitch, int imgfmt, byte[] bresult, int maxsize);

    private static native Bitmap nativeGetIDCardStdImg(byte[] NV21, int width, int height, byte[] bresult, int maxsize, int[] rects);

    private static native int nativeRecoVECardBitmap(Bitmap bitmap, byte[] bresult, int maxsize);

    private static native Bitmap nativeRecoVECardStillImage(Bitmap bitmap, int tryhard, int bwantimg, byte[] bresult, int maxsize, int[] rets);

    private static native Bitmap nativeRecoVECardStillImageV2(Bitmap bitmap, int tryhard, int bwantimg, byte[] bresult, int maxsize, int[] rects, int[] rets);

    private static native int nativeRecoVECardRawdat(byte[] imgdata, int width, int height, int pitch, int imgfmt, byte[] bresult, int maxsize);

    private static native Bitmap nativeGetVECardStdImg(byte[] NV21, int width, int height, byte[] bresult, int maxsize, int[] rects);

    private static native Bitmap nativeRecoVE2CardNV21(byte[] imgnv21, int width, int height, int bwantimg, byte[] bresult, int maxsize, int[] rects, int[] rets);

    private static native Bitmap nativeRecoVE2CardStillImage(Bitmap bitmap, int tryhard, int bwantimg, byte[] bresult, int maxsize, int[] rects, int[] rets);

    private static native int nativeRecoScanLineRawdata(byte[] imgdata, int width, int height, int imgfmt, int lft, int rgt, int top, int btm, int nRecoType, byte[] bresult, int maxsize);

    private static native Bitmap nativeRecoDRCardNV21(byte[] imgnv21, int width, int height, int bwantimg, byte[] bresult, int maxsize, int[] rects, int[] rets);

    private static native Bitmap nativeRecoDRCardStillImage(Bitmap bitmap, int tryhard, int bwantimg, byte[] bresult, int maxsize, int[] rects, int[] rets);
}