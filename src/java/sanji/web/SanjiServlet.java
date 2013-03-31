/**
 * @(#)SanjiServlet.java, 2012-10-22. 
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 */
package sanji.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.DispatcherServlet;

import sanji.base.Conf;
import sanji.flow.Featurer;
import sanji.flow.Modeler;
import sanji.utils.Logger;

/**
 * @author leo
 */
@SuppressWarnings("serial")
public final class SanjiServlet extends DispatcherServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 灏嗘爣鍑嗛敊璇噸瀹氬悜鍒版爣鍑嗚緭鍑�        System.setErr(System.out);
        Conf.init(config.getServletContext().getRealPath("WEB-INF"));
        Logger.info("{SanjiServlet.init} set app home:" + Conf.APP_HOME);
        super.init(config);
        Featurer.init();
        Modeler.init();
    }
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        super.service(request, response);
    }
}
