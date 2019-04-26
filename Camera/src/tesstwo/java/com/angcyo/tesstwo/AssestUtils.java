package com.angcyo.tesstwo;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 资源工具类
 */
class AssestUtils {

    String path;// = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tessdata/";
    private Context mContext;

    public AssestUtils(Context mContext, String path) {
        super();
        this.mContext = mContext;
        this.path = path + File.separator + "tessdata/";
    }

    public void init() {

        isFolderExists(path);
        try {
            String[] list = mContext.getAssets().list("dic");
            for (int i = 0; i < list.length; i++) {
                assetsDataToSD(path + list[i], list[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFolderExists(String strFolder) {
        File file = new File(strFolder);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    public void assetsDataToSD(String path, String fileName) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            return;
        }
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(path);
        myInput = mContext.getAssets().open("dic/" + fileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

}
