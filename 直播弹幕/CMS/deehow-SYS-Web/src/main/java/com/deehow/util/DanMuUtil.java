package com.deehow.util;

import com.deehow.core.util.CacheUtil;
import com.deehow.core.websocket.WebSocketHander;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;

/**
 * Created by LZW on 2018/11/3.
 */
public class DanMuUtil {

    public static final String REDIS_LIST_KEY = "com.lzw.danMuListKey.viewChannel:";

    private static final Logger logger = Logger.getLogger(DanMuUtil.class);

    private static int MAX_ERR_TIMES = 10;

    private static int EXCEPTION_SLEEP_SECONDS = 3;

    /**
     * 系统启动就将所有频道放入 websocket 中
     */
    public static void setViewChannel() {
        // ......查询频道列表
        WebSocketHander.setMoreViewChannel(new String[]{"1111","2222","3333","4444"});
    }


    /** 如果收到消息就推给websocket就不是异步的了，保证性能，使用队列保证异步进行 */
    /** 使用redis队列先进先出来生产和消费弹幕。使产生弹幕和消费弹幕相分离，异步进行 */
    /** redis队列的生产模式，左侧保持添加 */
    /** redis队列的消费模式，右侧保持弹出 */
    /** 直播弹幕的特性，需要一直排队消费弹幕 */
    /** redis队列的消费方式，启动一个线程，一直消费队列 */
    public static void monitorAndConsumeDM() {
        Runnable danMuRunnable = new Runnable(){
            @Override
            public void run() {
                int errorTimes = 0;
                boolean runFlag = true;
                Long channel = 2222l;
                while(runFlag){
                    try {
                        Object ss = rightPop(channel);
                        if(ss != null && !"".equals(ss)){
                            WebSocketHander.sendMessageToViewChannel(channel.toString(),new TextMessage(ss.toString()));
                        }
                        errorTimes = 0;
                    }catch (Exception e){
                        e.printStackTrace();
                        errorTimes++;
                        if(errorTimes > MAX_ERR_TIMES){
                            logger.warn("消费者异常次数超过阈值,关闭线程,请查看redis服务是否关闭或异常");
                            runFlag = false;
                            break;
                        }
                        try {
                            Thread.sleep(EXCEPTION_SLEEP_SECONDS*1000);
                        } catch (InterruptedException e1) {
                            logger.warn("消费者异常，睡眠被打断",e1);
                        }
                        logger.warn("消费者异常",e);
                    }
                }
            }
        };
        new Thread(danMuRunnable).start();
    }





    /**
     * 生产弹幕
     * @param viewChannel   频道
     * @param msg   消息
     */
    public static void leftPush(Long viewChannel,String msg){
        CacheUtil.getRedisHelper().lleftPush(REDIS_LIST_KEY + viewChannel,msg);
    }

    /**
     * 消费弹幕，若数量=0 处于阻塞，则一直等待
     * @param viewChannel
     * @return
     */
    public static Object rightPop(Long viewChannel){
        return CacheUtil.getRedisHelper().lrightPop(REDIS_LIST_KEY + viewChannel);
    }


}
