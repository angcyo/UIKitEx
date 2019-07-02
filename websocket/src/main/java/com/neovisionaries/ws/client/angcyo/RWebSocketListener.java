package com.neovisionaries.ws.client.angcyo;

import androidx.annotation.NonNull;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/12/12 14:18
 * 修改人员：Robi
 * 修改时间：2017/12/12 14:18
 * 修改备注：
 * Version: 1.0.0
 */
public class RWebSocketListener {


    /**
     * 当遇到错误, 导致断开时, 回调
     */
    public void disConnectWebsocket(int code, String message) {

    }

    /**
     * WebSocket 断开后回调
     */
    public void disConnectWebsocket() {

    }

    /**
     * WebSocket 连接成功
     */
    @Deprecated
    public void connectSuccessWebsocket(@NonNull WebSocket webSocket) {

    }

    /**
     * WebSocket 连接成功
     *
     * @param isReconnect 是否是重连
     */
    public void connectSuccessWebsocket(@NonNull WebSocket webSocket, boolean isReconnect) {
        connectSuccessWebsocket(webSocket);
    }

    /**
     * 主线程回调
     */
    public void onTextMessage(@NonNull WebSocket websocket, String data) {

    }

    /**
     * 重连时, 可以通过此方法使用新的Url, 返回空表示不使用
     */
    public String getReconnectUrl() {
        return "";
    }
}
