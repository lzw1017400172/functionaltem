/**
 * 
 */
package com.deehow.core.base;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import com.baomidou.mybatisplus.plugins.Page;
import com.deehow.core.util.WebUtil;

/**
 * 控制器基类
 * 
 * @author ShenHuaJie
 * @version 2016年5月20日 下午3:47:58
 */
public abstract class AbstractController<T extends BaseProvider> extends BaseController {
    protected final Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    protected T provider;

    public abstract String getService();

    public Object query(ModelMap modelMap, Map<String, Object> param) {
        Parameter parameter = new Parameter(getService(), "query").setMap(param);
        logger.info("{} execute query start...", parameter.getNo());
        Page<?> list = provider.execute(parameter).getPage();
        logger.info("{} execute query end.", parameter.getNo());
        return setSuccessModelMap(modelMap, list);
    }

    public Object queryList(ModelMap modelMap, Map<String, Object> param) {
        Parameter parameter = new Parameter(getService(), "queryList").setMap(param);
        logger.info("{} execute queryList start...", parameter.getNo());
        List<?> list = provider.execute(parameter).getList();
        logger.info("{} execute queryList end.", parameter.getNo());
        return setSuccessModelMap(modelMap, list);
    }

    public Object get(ModelMap modelMap, BaseModel param) {
        Parameter parameter = new Parameter(getService(), "queryById").setId(param.getId());
        logger.info("{} execute queryById start...", parameter.getNo());
        BaseModel result = provider.execute(parameter).getModel();
        logger.info("{} execute queryById end.", parameter.getNo());
        return setSuccessModelMap(modelMap, result);
    }

    public Object update(ModelMap modelMap, BaseModel param) {
        Long userId = WebUtil.getCurrentUser();
        if (param.getId() == null) {
            param.setCreateBy(userId);
            param.setCreateTime(new Date());
        }
        param.setUpdateBy(userId);
        param.setUpdateTime(new Date());
        Parameter parameter = new Parameter(getService(), "update").setModel(param);
        logger.info("{} execute update start...", parameter.getNo());
        provider.execute(parameter);
        logger.info("{} execute update end.", parameter.getNo());
        return setSuccessModelMap(modelMap);
    }

    public Object delete(ModelMap modelMap, BaseModel param) {
        Parameter parameter = new Parameter(getService(), "delete").setId(param.getId());
        logger.info("{} execute delete start...", parameter.getNo());
        provider.execute(parameter);
        logger.info("{} execute delete end.", parameter.getNo());
        return setSuccessModelMap(modelMap);
    }
}
