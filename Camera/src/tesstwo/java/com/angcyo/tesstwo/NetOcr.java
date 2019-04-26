package com.angcyo.tesstwo;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.angcyo.http.Http;
import com.angcyo.http.Json;
import com.angcyo.uiview.less.utils.RUtils;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class NetOcr {
    static final String ID_PATTERN = "(([0-9]{17}[0-9|x|X])|([0-9]{14}[0-9|x|X]))";
    static final String API_KEY = "xzgNq9VmyXjo3eR7dMRsw9pm";
    static final String SECRET_KEY = "U88h4T074C6gUecEmFP0mSN8unOqG9N6";
    static String token = null;
    OkHttpClient mOkHttpClient;
    /**
     * 是否请求中
     */
    AtomicBoolean requestIng = new AtomicBoolean(false);

    Tesstwo.OnResultCallback onResultCallback;

    public NetOcr() {
        initClient();
        getToken();
    }

    private void initClient() {
        mOkHttpClient = Http.defaultOkHttpClick("OCR").build();
    }

    private void getToken() {
        if (token == null) {
            final Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" +
                            API_KEY + "&client_secret=" + SECRET_KEY)
                    .post(RequestBody.create(MediaType.parse("application/json"), ""))
                    .build();

            requestIng.set(true);

            //获取Token
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    requestIng.set(false);

                    try {
                        String body = response.body().string();
                        JSONObject jsonObject = new JSONObject(body);
                        token = jsonObject.getString("access_token");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public boolean isRequestIng() {
        return requestIng.get();
    }

    public void uploadAndRecognize(byte[] bytes) {
        if (bytes != null && !requestIng.get()) {
            ocrBaidu(bytes);
            //ocrXf(bytes);
        }
    }

    /**
     * 讯飞OCR
     */
    public void ocrXf(byte[] bytes) {
        if (bytes != null && !requestIng.get()) {
            final String key = "4cecb9a95cf543f53107d98065f297ce";
            final String curTime = String.valueOf(System.currentTimeMillis() / 1000);
            String body = null;
            try {
                body = "image=" + URLEncoder.encode(Base64.encodeToString(bytes, Base64.NO_WRAP), "UTF8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String param = Json.json()
                    .add("engine_type", "idcard")
                    .get();

            param = Base64.encodeToString(param.getBytes(), Base64.NO_WRAP);

            final Request request = new Request.Builder()
                    .url("https://webapi.xfyun.cn/v1/service/v1/ocr/idcard")
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), body))
                    .addHeader("X-Appid", "5cc2c404")
                    .addHeader("X-CurTime", curTime)
                    .addHeader("X-Param", param)
                    .addHeader("X-CheckSum", RUtils.md5(key + curTime + param))
                    .build();

            requestIng.set(true);

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requestIng.set(false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    requestIng.set(false);

                    try {
                        String body = response.body().string();

                        JSONObject jsonObject = new JSONObject(body);
                        String words = jsonObject.getJSONObject("data").getString("id_number");

                        if (onResultCallback != null) {
                            onResultCallback.onResult(null, null, words);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * 百度ocr
     */
    public void ocrBaidu(byte[] bytes) {
        if (bytes != null && !requestIng.get() && !TextUtils.isEmpty(token)) {

            String body = null;
            try {
                body = "image=" + URLEncoder.encode(Base64.encodeToString(bytes, Base64.NO_WRAP), "UTF8") +
                        "&id_card_side=front";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            final Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/rest/2.0/ocr/v1/idcard?access_token=" + token)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), body))
                    .build();

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requestIng.set(false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    requestIng.set(false);

                    try {
                        String body = response.body().string();
                        JSONObject jsonObject = new JSONObject(body);
                        String words = jsonObject.getJSONObject("words_result").getJSONObject("公民身份证号码").getString("words");
                        //L.i("识别结果:"+ words);

                        if (onResultCallback != null) {
                            onResultCallback.onResult(null, null, words);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 其他厂家的OCR
     */
    public void ocrOther(byte[] bytes) {
        if (bytes != null && !requestIng.get()) {
            //构造上传请求，类似web表单
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"callbackurl\""), RequestBody.create(null, "/idcard/"))
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"action\""), RequestBody.create(null, "idcard"))
                    .addPart(Headers.of("Content-Disposition", "form-data; name=\"img\"; filename=\"idcardFront_user.jpg\""), RequestBody.create(MediaType.parse("image/jpeg"), bytes))
                    .build();

            //进行包装，使其支持进度回调
            final Request request = new Request.Builder()
                    .header("Host", "ocr.ccyunmai.com:8080")
                    .header("Origin", "http://ocr.ccyunmai.com:8080")
                    .header("Referer", "http://ocr.ccyunmai.com:8080/idcard/")
                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2398.0 Safari/537.36")
                    .url("http://ocr.ccyunmai.com:8080/UploadImage.action")
                    .post(requestBody)
                    .build();
            requestIng.set(true);

            //开始请求
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (e instanceof UnknownHostException) {

                    } else {
                        requestIng.set(false);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    requestIng.set(false);

                    try {
                        final String result = response.body().string();
                        Matcher matcher = Pattern.compile(ID_PATTERN).matcher(result);

                        while (matcher.find()) {
                            Log.i("识别结果1:", matcher.group(0));
                            Log.i("识别结果2:", matcher.group());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    Document parse = Jsoup.parse(result);
//                    final Elements select = parse.select("div#ocrresult");
//                    Log.e(TAG, "onResponse: " + select.text());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            tv_result.setText(getHtmlMsg(select.text(), "公民身份号码:", "签发机关"));
//                        }
//                    });
                }
            });
        }
    }
}
