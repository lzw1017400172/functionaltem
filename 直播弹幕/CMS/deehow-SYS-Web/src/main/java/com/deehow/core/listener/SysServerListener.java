package com.deehow.core.listener;

import com.deehow.util.DanMuUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SysServerListener implements ServletContextListener {
	protected final Logger logger = Logger.getLogger(this.getClass());
	public void contextDestroyed(ServletContextEvent contextEvent) {
	}

	public void contextInitialized(ServletContextEvent contextEvent) {
		logger.info("=================================");
		logger.info("系统"+contextEvent.getServletContext().getServletContextName()+"启动完成!!!");
		logger.info("=================================");

        DanMuUtil.setViewChannel();
        DanMuUtil.monitorAndConsumeDM();


	}
}