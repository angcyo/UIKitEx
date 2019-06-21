package com.neovisionaries.ws.client.angcyo;

import android.text.TextUtils;
import android.util.Log;
import com.neovisionaries.ws.client.*;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：compile 'com.neovisionaries:nv-websocket-client:2.3'  2017-12-12
 * 类的描述：https://github.com/TakahikoKawasaki/nv-websocket-client
 * 创建人员：Robi
 * 创建时间：2017/12/12 14:01
 * 修改人员：Robi
 * 修改时间：2017/12/12 14:01
 * 修改备注：
 * Version: 1.0.0
 *
 * 2019-6-21
 *  compile 'com.neovisionaries:nv-websocket-client:2.8'
 */
public class RWebSocket extends WebSocketAdapter {

    /**
     * 连接中途出现错误
     */
    public final static int CODE_ERROR = -400;
    /**
     * 连接时错误
     */
    public final static int CODE_CONNECT_ERROR = -401;

    /**
     * webSocket关闭
     */
    public final static int WEB_SOCKET_CLOSE = 0x10;
    /**
     * websocket正在连接
     */
    public final static int WEB_SOCKET_CONNECTING = 0x11;
    /**
     * webSocket连接成功
     */
    public final static int WEB_SOCKET_OPEN = 0x12;
    /**
     * 连接超时设置
     */
    public static int TIMEOUT = 10_000;
    public static boolean DEBUG = BuildConfig.DEBUG;
    /**
     * 每隔多久检查一次状态
     */
    public static int CHECK_STATE_TIME = 3_000;
    final String TAG = "RWebSocket";
    private String mWebSocketUrl;
    /**
     * 连接状态标识
     */
    private int webSocketState = WEB_SOCKET_CLOSE;

    /**
     * 是否重连中...当连接成功后, 网络断开, 程序自动连接时, 会标识的变量
     */
    private boolean isReconnect = false;

    /**
     * 定时监测websocket状态  实现重连
     */
    private Subscription observable;
    private WebSocket mWebSocket;
    private List<RWebSocketListener> mListeners = new CopyOnWriteArrayList<>();

    private RWebSocket() {
    }

    public static RWebSocket create() {
        return new RWebSocket();
    }

    public RWebSocket addListener(RWebSocketListener listener) {
        mListeners.add(listener);
        return this;
    }

    public RWebSocket removeListener(RWebSocketListener listener) {
        mListeners.remove(listener);
        return this;
    }

    /**
     * 外部调用, ws://116.7.249.36:8083/mqtt
     */
    public void connect(String wsUrl) {
        isReconnect = false;
        connect(wsUrl, isReconnect);
    }

    /**
     * 内部调用
     */
    private void connect(String wsUrl, boolean isReconnect) {
        if (TextUtils.isEmpty(wsUrl)) {
            return;
        }
        //连接相同的Url (会自动重连, 不需要手动重连)
        if (TextUtils.equals(wsUrl, mWebSocketUrl) && isConnect()) {
            return;
        }
        //释放之前的资源
        if (mWebSocket != null) {
            closeWebSocket();
        }

        this.mWebSocketUrl = wsUrl;

        webSocketState = WEB_SOCKET_CONNECTING;
        //注意  websocket的连接需要在异步线程
        Observable.just(mWebSocketUrl)
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        try {
                            i("开始连接WebSocket:" + mWebSocketUrl);
                            WebSocketFactory mWebSocketFactory = new WebSocketFactory();
                            WebSocket webSocket = mWebSocketFactory.createSocket(mWebSocketUrl, TIMEOUT);
                            webSocket.addListener(RWebSocket.this);
                            webSocket.connect();
                        } catch (Exception e) {
                            e("开始连接出错:" + e.getMessage());
                            webSocketState = WEB_SOCKET_CLOSE;

                            throw new IllegalStateException(e);
                        }
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        checkWebSocketState();
                    }

                    @Override
                    public void onError(Throwable e) {
                        webSocketState = WEB_SOCKET_CLOSE;
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }
                });
    }

    /**
     * webSocket的连接状态检查
     */
    private void checkWebSocketState() {
        i("开始检测webSocket的状态");
        if (observable == null || observable.isUnsubscribed()) {
            observable = Observable.interval(CHECK_STATE_TIME, CHECK_STATE_TIME, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Long>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(Long aLong) {
                            if (mWebSocket != null) {
                                d("WebSocket连接是否成功:" + mWebSocket.isOpen());
                            }
                            if (!TextUtils.isEmpty(mWebSocketUrl) && webSocketState == WEB_SOCKET_CLOSE) {
                                i("检测到webSocket已断开,重连中...");
                                reconnectWebsocket();
                            }
                        }
                    });
        }
    }

    /**
     * 关闭webSocket
     */
    public void closeWebSocket() {
        mWebSocketUrl = "";
        if (observable != null) {
            observable.unsubscribe();
        }
        if (mWebSocket != null) {
            mWebSocket.disconnect();
        }
        webSocketState = WEB_SOCKET_CLOSE;
        i("释放WebSocket资源");
    }

    /**
     * 重新连接
     */
    public void reconnectWebsocket() {
        if (webSocketState == WEB_SOCKET_CONNECTING) {
            i("已经在重连WebSocket");
            return;
        }

        isReconnect = true;
        i("准备重连WebSocket");

        if (mWebSocket != null) {
            try {
                webSocketState = WEB_SOCKET_CONNECTING;
                mWebSocket = mWebSocket.recreate();
                mWebSocket.connect();
            } catch (Exception e) {
                e("重连失败:" + e.getMessage());
                webSocketState = WEB_SOCKET_CLOSE;
                mWebSocket.disconnect();
                mWebSocket = null;
            }
        } else {
            String reconnectUrl = "";
            for (RWebSocketListener listener : mListeners) {
                reconnectUrl = listener.getReconnectUrl();
                if (!TextUtils.isEmpty(reconnectUrl)) {
                    break;
                }
            }
            if (TextUtils.isEmpty(reconnectUrl)) {
                connect(mWebSocketUrl, isReconnect);
            } else {
                connect(reconnectUrl, isReconnect);
            }
        }
    }

    /**
     * WebSocket 是否连接上
     */
    public boolean isConnect() {
        if (mWebSocket != null) {
            return mWebSocket.isOpen();
        }
        return webSocketState == WEB_SOCKET_OPEN;
    }

    private void i(String msg) {
        if (DEBUG) {
            Log.i(TAG, Thread.currentThread().getName() + "#" + msg);
        }
    }

    private void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, Thread.currentThread().getName() + "#" + msg);
        }
    }

    private void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, Thread.currentThread().getName() + "#" + msg);
        }
    }

    //*****************************WebSocketAdapter  start***********************************************************************/

    /**
     * 连接中途失败回调
     *
     * @param websocket
     * @param cause
     * @throws Exception
     */
    @Override
    public void onError(WebSocket websocket, final WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
        e("webSocket连接失败onError：" + cause.getMessage());
        webSocketState = WEB_SOCKET_CLOSE;
        onMain(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mListeners.size(); i++) {
                    RWebSocketListener listener = mListeners.get(i);
                    listener.disConnectWebsocket(CODE_ERROR, cause.getMessage());
                }
            }
        });
    }

    /**
     * 连接中途失败回调
     *
     * @param websocket
     * @param exception
     * @throws Exception
     */
    @Override
    public void onConnectError(WebSocket websocket, final WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        e("webSocket连接失败onConnectError：" + exception.getMessage());
        webSocketState = WEB_SOCKET_CLOSE;
        onMain(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mListeners.size(); i++) {
                    RWebSocketListener listener = mListeners.get(i);
                    listener.disConnectWebsocket(CODE_CONNECT_ERROR, exception.getMessage());
                }
            }
        });
    }

    /**
     * 连接断开  主动关闭webSocket时回调
     *
     * @param websocket
     * @param serverCloseFrame
     * @param clientCloseFrame
     * @param closedByServer
     * @throws Exception
     */
    @Override
    public void onDisconnected(WebSocket websocket, final WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        e("webSocket断开" + mWebSocket);
        webSocketState = WEB_SOCKET_CLOSE;
        //final int closeCode = serverCloseFrame.getCloseCode();
        //final String closeReason = serverCloseFrame.getCloseReason();

        onMain(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mListeners.size(); i++) {
                    RWebSocketListener listener = mListeners.get(i);
                    listener.disConnectWebsocket();
                }
            }
        });
    }

    /**
     * 连接成功
     *
     * @param websocket
     * @param headers
     * @throws Exception
     */
    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        i("webSocket连接成功");
        if (websocket != null) {
            this.mWebSocket = websocket;
            webSocketState = WEB_SOCKET_OPEN;
            onMain(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < mListeners.size(); i++) {
                        RWebSocketListener listener = mListeners.get(i);
                        //listener.connectSuccessWebsocket(mWebSocket);
                        listener.connectSuccessWebsocket(mWebSocket, isReconnect);
                    }
                }
            });
        }
    }

    /**
     * 字节数据  请根据需要去处理
     *
     * @param websocket
     * @param binary
     * @throws Exception
     */
    @Override
    public void onBinaryMessage(final WebSocket websocket, final byte[] binary) throws Exception {
        super.onBinaryMessage(websocket, binary);
        onMain(new Runnable() {
            @Override
            public void run() {
                dealData(websocket, new String(binary, Charset.forName("utf8")));
            }
        });
    }

    /**
     * 获取数据
     *
     * @param websocket
     * @param text
     * @throws Exception
     */
    @Override
    public void onTextMessage(final WebSocket websocket, final String text) throws Exception {
        super.onTextMessage(websocket, text);
        onMain(new Runnable() {
            @Override
            public void run() {
                dealData(websocket, text);
            }
        });
    }

    private void onMain(final Runnable onMain) {
        Observable.just("1")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        onMain.run();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }


    /**
     * 因websocket接收数据的线程是子线程，外围接收者接受数据时也处于子线程，无法进行ui更新，因此通过rx将其放到主线程处理
     *
     * @param data
     */
    private void dealData(WebSocket websocket, String data) {
        for (int i = 0; i < mListeners.size(); i++) {
            RWebSocketListener listener = mListeners.get(i);
            listener.onTextMessage(websocket, data);
        }
    }

    //*****************************WebSocketAdapter  end***********************************************************************/

    public void sendText(String message) {
        if (isConnect()) {
            mWebSocket.sendText(message);
        }
    }
}
