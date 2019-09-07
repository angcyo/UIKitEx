package com.angcyo.tesstwo;


import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.angcyo.http.Http;
import com.angcyo.http.Json;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.utils.RUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.jvm.functions.Function1;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class NetOcr {
    public static final String ID_PATTERN = "(([0-9]{17}[0-9|x|X])|([0-9]{14}[0-9|x|X]))";
    public static String BAIDU_API_KEY = "xzgNq9VmyXjo3eR7dMRsw9pm";
    public static String BAIDU_SECRET_KEY = "U88h4T074C6gUecEmFP0mSN8unOqG9N6";
    public static String BAIDU_TOKEN = null;

    //aliyun
    public static String ALI_APPCODE = "79998588d4334b8781696e5382cd931d";

    OkHttpClient mOkHttpClient;
    /**
     * 是否请求中
     */
    AtomicBoolean requestIng = new AtomicBoolean(false);

    Tesstwo.OnResultCallback onResultCallback;

    public NetOcr() {
        initClient();
        getBaiduToken();
    }

    private void initClient() {
        mOkHttpClient = Http.defaultOkHttpClient("OCR").build();
    }

    public void resetBaiduApi(String apiKey, String secretKey) {
        BAIDU_API_KEY = apiKey;
        BAIDU_SECRET_KEY = secretKey;
        BAIDU_TOKEN = null;
        getBaiduToken();
    }

    /**
     * 获取百度API 访问token
     */
    private void getBaiduToken() {
        if (BAIDU_TOKEN == null) {
            final Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" +
                            BAIDU_API_KEY + "&client_secret=" + BAIDU_SECRET_KEY)
                    .post(RequestBody.create(MediaType.parse("application/json"), ""))
                    .build();

            requestIng.set(true);

            //获取Token
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    requestIng.set(false);

                    try {
                        String body = response.body().string();
                        JSONObject jsonObject = new JSONObject(body);
                        BAIDU_TOKEN = jsonObject.getString("access_token");
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
     * 阿里云 COR
     * https://duguang.aliyun.com/experience?spm=5176.12127985.1248150.2.68984f58sh2SJK&type=doc&midtype=fulldoc&subtype=ecommerce
     */
    public void ocrAliyun(byte[] bytes, final Function1<String, Void> onResult) {
        String api = "https://ocrapi-advanced.taobao.com/ocrservice/advanced";

        try {
            String jsonBody = Json.json()
                    .add("img", Base64.encodeToString(bytes, Base64.NO_WRAP))
                    .add("rotate", true)
                    .get();

            final Request request = new Request.Builder()
                    .url(api + "?access_token=" + BAIDU_TOKEN)
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                    .header("Authorization", "APPCODE " + ALI_APPCODE)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .build();

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requestIng.set(false);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    requestIng.set(false);

                    try {
                        String body = response.body().string();
                        L.i("阿里云OCR结果:" + body);

                        if (onResult != null) {
                            onResult.invoke(body);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 讯飞OCR
     * <p>
     * https://www.xfyun.cn/services/wordRecg
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
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requestIng.set(false);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
     * <p>
     * https://ai.baidu.com/tech/ocr/general
     */
    public void ocrBaidu(byte[] bytes) {
        //身份证识别
        ocrBaidu("https://aip.baidubce.com/rest/2.0/ocr/v1/idcard", bytes, "&id_card_side=front", new Function1<String, Void>() {
            @Override
            public Void invoke(String body) {
                try {
                    JSONObject jsonObject = new JSONObject(body);

                    JSONObject wordsResult = jsonObject.getJSONObject("words_result");
                    Iterator<String> keys = wordsResult.keys();

                    String words = "";
                    while (keys.hasNext()) {
                        String next = keys.next();
                        if (next != null && next.contains("号")) {
                            words = wordsResult.getJSONObject(next).getString("words");
                            break;
                        }
                    }
                    //L.i("识别结果:"+ words);

                    if (onResultCallback != null && !TextUtils.isEmpty(words)) {
                        onResultCallback.onResult(null, null, words);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public void ocrBaiduBasic(byte[] bytes, final Function1<String, Void> onResult) {
        //通用文字识别

        //https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic  //普通
        //https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic //高精度

        ocrBaidu("https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic",
                bytes,
                "&language_type=CHN_ENG&detect_direction=true",
                new Function1<String, Void>() {
                    @Override
                    public Void invoke(String body) {
                        if (onResult != null) {
                            onResult.invoke(body);
                        }
                        return null;
                    }
                });
    }

    public void ocrBaidu(String api, byte[] bytes, String formBody, final Function1<String, Void> onResult) {
        if (bytes != null && !requestIng.get() && !TextUtils.isEmpty(BAIDU_TOKEN)) {

            String body;
            try {
                body = "image=" +
                        URLEncoder.encode(Base64.encodeToString(bytes, Base64.NO_WRAP), "UTF8") +
                        formBody;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                body = "";
            }

            final Request request = new Request.Builder()
                    .url(api + "?access_token=" + BAIDU_TOKEN)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), body))
                    .build();

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, IOException e) {
                    requestIng.set(false);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    requestIng.set(false);

                    try {
                        String body = response.body().string();
                        L.i("百度OCR结果:" + body);

                        if (onResult != null) {
                            onResult.invoke(body);
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
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (e instanceof UnknownHostException) {

                    } else {
                        requestIng.set(false);
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
