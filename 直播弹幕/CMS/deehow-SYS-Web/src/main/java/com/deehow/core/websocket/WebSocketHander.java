package com.deehow.core.websocket;

import org.apache.log4j.Logger;
import org.springframework.web.socket.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHander implements WebSocketHandler {

    private static final Logger logger = Logger.getLogger(WebSocketHander.class);

    /**	让每个视频频道对应一个session，多个客户端与同一个频道session保持长链接通信 */
    private static final ConcurrentHashMap<String,Map<String,WebSocketSession>> viewChannel = new ConcurrentHashMap<String,Map<String,WebSocketSession>>();

    // 初次链接成功执行
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        Map<String,Object> map = webSocketSession.getAttributes();
		Object ob = map.get(HandshakeInterceptor.WSTYPE_DANMU_CHANNEL);
        if(ob == null){
            logger.error("没有找到对应频道。");
            return;
        }
		String key = map.get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME).toString();
        Map<String,WebSocketSession> channel = viewChannel.get(ob.toString());
        if(channel == null){
            logger.error("没有找到对应频道。");
            return;
        }
        channel.put(key,webSocketSession);
		logger.debug("链接成功......，成功连接频道："+ob);
    }

    // 接受消息处理消息
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage)
            throws Exception {

        String messge = webSocketMessage.getPayload().toString();
        if("ping".equals(messge)){//若果只是ping心跳，只返回给对应一个客户端pong即可
            webSocketMessage = new TextMessage("pong");
            webSocketSession.sendMessage(webSocketMessage);
        } else {
            /** 若是弹幕信息，找到频道，全部推送 */
            /** 先通过session找到对应频道 */
            Map<String,Object> map = webSocketSession.getAttributes();
            Object ob = map.get(HandshakeInterceptor.WSTYPE_DANMU_CHANNEL);
            if(ob == null){
                logger.error("没有找到对应频道。");
                return;
            }
            sendMessageToViewChannel(ob.toString(),new TextMessage(messge));
        }
    }

    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
		if (webSocketSession.isOpen()) {
			webSocketSession.close();
		}
		logger.debug("链接出错，关闭链接......");
		Map<String,Object> map = webSocketSession.getAttributes();
        Object ob = map.get(HandshakeInterceptor.WSTYPE_DANMU_CHANNEL);
        if(ob == null){
            logger.error("没有找到对应频道。");
            return;
        }
        Map<String,WebSocketSession> channel = viewChannel.get(ob.toString());
        if(channel == null){
            logger.error("没有找到对应频道。");
            return;
        }
        String key = map.get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME).toString();
        channel.remove(key);
    }

    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
    	logger.debug("链接关闭......" + closeStatus.toString());
		Map<String,Object> map = webSocketSession.getAttributes();
        Object ob = map.get(HandshakeInterceptor.WSTYPE_DANMU_CHANNEL);
        if(ob == null){
            logger.error("没有找到对应频道。");
            return;
        }
        Map<String,WebSocketSession> channel = viewChannel.get(ob.toString());
        if(channel == null){
            logger.error("没有找到对应频道。");
            return;
        }
        String key = map.get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME).toString();
        channel.remove(key);
    }

    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 创建频道
     * @param channel
     */
    public static void setViewChannel(String channel){
        if(channel != null && channel.length() > 0){
            if(viewChannel.get(channel) == null){
                viewChannel.put(channel,new ConcurrentHashMap<String,WebSocketSession>());
            }
        }
	}

    /**
     * 创建more频道
     * @param channels
     */
    public static void setMoreViewChannel(String[] channels){
        if(channels != null && channels.length > 0){
            for(String channel:channels){
                setViewChannel(channel);
            }
        }
    }


    /**
     * 给某个频道所有发送消息
     */
    public static void sendMessageToViewChannel(String channel, TextMessage message) {
        Map<String,WebSocketSession> channelSession = viewChannel.get(channel);
    	if(null != channelSession){
            for(Map.Entry<String, WebSocketSession> entry:channelSession.entrySet()){
                WebSocketSession user = entry.getValue();
                if(null != user){
                    try {
                        if (user.isOpen()) {
                            user.sendMessage(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    	}
    }

}
