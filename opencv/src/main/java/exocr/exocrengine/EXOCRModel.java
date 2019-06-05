package exocr.exocrengine;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * description: 数据字典
 * create by kalu on 2018/11/19 10:04
 */
public final class EXOCRModel implements Serializable {

    public int type = 0; // 正反面
    public String cardnum; // 身份证号码
    public String name; // 姓名
    public String sex; //性别
    public String address; // 住址
    public String nation; //民族
    public String birth; // 出生
    public String office; // 签发单位
    public String validdate; // 有效日期

    public String base64bitmap;

    public Rect rtIDNum;
    public Rect rtName;
    public Rect rtSex;
    public Rect rtNation;
    public Rect rtAddress;
    public Rect rtFace;
    public Rect rtOffice;
    public Rect rtValid;

    public EXOCRModel() {
    }

    public static final EXOCRModel decode(byte[] bResultBuf, int reslen) {
        byte code;
        int i, j, rdcount;
        String content = null;

        EXOCRModel idcard = new EXOCRModel();

        rdcount = 0;
        idcard.type = bResultBuf[rdcount++];
        while (rdcount < reslen) {
            code = bResultBuf[rdcount++];
            i = 0;
            j = rdcount;
            while (rdcount < reslen) {
                i++;
                rdcount++;
                if (bResultBuf[rdcount] == 0x20) break;
            }
            try {
                content = new String(bResultBuf, j, i, "GBK");
            } catch (UnsupportedEncodingException e) {
                Log.e("kalu", e.getMessage(), e);
            }

            if (code == 0x21) {
                idcard.cardnum = content;
                String year = idcard.cardnum.substring(6, 10);
                String month = idcard.cardnum.substring(10, 12);
                String day = idcard.cardnum.substring(12, 14);
                idcard.birth = year + "-" + month + "-" + day;
            } else if (code == 0x22) {
                idcard.name = content;
            } else if (code == 0x23) {
                idcard.sex = content;
            } else if (code == 0x24) {
                idcard.nation = content;
            } else if (code == 0x25) {
                idcard.address = content;
            } else if (code == 0x26) {
                idcard.office = content;
            } else if (code == 0x27) {
                idcard.validdate = content;
            }
            rdcount++;
        }
        //is it correct, check it!
        if (idcard.type == 1 && (idcard.cardnum == null || idcard.name == null || idcard.nation == null || idcard.sex == null || idcard.address == null) ||
                idcard.type == 2 && (idcard.office == null || idcard.validdate == null) ||
                idcard.type == 0) {
            return null;
        } else {
            if (idcard.type == 1 && (idcard.cardnum.length() != 18 || idcard.name.length() < 2 || idcard.address.length() < 10)) {
                return null;
            }
        }
        return idcard;
    }

    //rects存放各个块的矩形，4个一组，这么做是为了将JNI的接口简单化
    // [0, 1, 2, 3]  idnum			issue
    // [4, 5, 6, 7]	 name			validate
    // [8, 9, 10,11] sex
    // [12,13,14,15] nation
    // [16,17,18,19] address
    // [20,21,22,23] face
    public void setRects(int[] rects) {
        if (type == 1) {
            rtIDNum = new Rect(rects[0], rects[1], rects[2], rects[3]);
            rtName = new Rect(rects[4], rects[5], rects[6], rects[7]);
            rtSex = new Rect(rects[8], rects[9], rects[10], rects[11]);
            rtNation = new Rect(rects[12], rects[13], rects[14], rects[15]);
            rtAddress = new Rect(rects[16], rects[17], rects[18], rects[19]);
            rtFace = new Rect(rects[20], rects[21], rects[22], rects[23]);
        } else if (type == 2) {
            rtOffice = new Rect(rects[0], rects[1], rects[2], rects[3]);
            rtValid = new Rect(rects[4], rects[5], rects[6], rects[7]);
        } else {
            return;
        }
    }

    public void bitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            baos.flush();
            baos.close();
            byte[] byteArray = baos.toByteArray();
            base64bitmap = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            Log.e("kalu", e.getMessage(), e);
        } finally {

            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                Log.e("kalu", e.getMessage(), e);
            }
        }
    }

    public final boolean isOk(final boolean front) {

        if (front) {
            return type == 1;
        } else {
            return type == 2;
        }
    }

    @Override
    public String toString() {
        String text = "\n";
        if (type == 1) {
            text += "\nname:" + name;
            text += "\nnumber:" + cardnum;
            text += "\nsex:" + sex;
            text += "\nnation:" + nation;
            text += "\nbirth:" + birth;
            text += "\naddress:" + address;
            //text += "\nbase64bitmap:" + base64bitmap;
        } else if (type == 2) {
            text += "\noffice:" + office;
            text += "\nValDate:" + validdate;
            //text += "\nbase64bitmap:" + base64bitmap;
        }
        return text;
    }

    /**********************************************************************************************/

    /**
     * 是否解析成功
     *
     * @return
     */
    public final boolean isDecodeSucc() {
        return type == 1 || type == 2;
    }

    /**
     * 是否正面, 人像
     *
     * @return
     */
    public final boolean isDecodeFront() {
        return type == 1;
    }
}
