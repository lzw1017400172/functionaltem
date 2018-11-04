package com.deehow.web;

import com.deehow.core.base.BaseController;
import com.deehow.util.DanMuUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created by LZW on 2018/3/4.
 */
@Controller
public class HelloController extends BaseController {

    private static final Logger logger = Logger.getLogger(HelloController.class);

    /**
     * 此请求保证浏览器第一次请求获取到sesison
     * 如果没有获取到session就直接连接 websocket 会报 session空指针。
     * @return
     */
    @GetMapping("/home/page")
    public String homePage(){
        return "index";
    }




    /* 目前就只消费了一个频道 2222l */
//    @PostMapping("/send/barrage")
//    public Object sendBarrage(ModelMap modelMap,String message){
//        if(StringUtils.isNotBlank(message)){
//            /** 插入redis队列数据 */
//            DanMuUtil.leftPush(2222l,message);
//        }
//        return setSuccessModelMap(modelMap);
//    }



}
