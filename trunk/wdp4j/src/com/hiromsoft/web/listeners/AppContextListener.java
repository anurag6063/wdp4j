
package com.hiromsoft.web.listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hiromsoft.utils.Global;

public class AppContextListener implements ServletContextListener {

    public AppContextListener() {
        super();
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext servletContext=arg0.getServletContext();
        String root = servletContext.getRealPath("/");
        Global.BASEPATH=root;
        //PropertyConfigurator.configure(root+"WEB-INF\\log4j.properties");
        //System.getProperty("xzsp.root")
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    }

}
