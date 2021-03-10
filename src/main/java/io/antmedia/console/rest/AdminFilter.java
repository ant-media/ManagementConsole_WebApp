package io.antmedia.console.rest;

import io.antmedia.console.datastore.DataStoreFactory;
import io.antmedia.console.datastore.IDataStore;
import io.antmedia.rest.model.User;
import io.antmedia.rest.model.UserType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

public class AdminFilter implements Filter {
    private IDataStore dataStore;
    private DataStoreFactory dataStoreFactory;

    @Context
    private ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {


    }
    public DataStoreFactory getDataStoreFactory(ServletContext servletContext) {
        if(dataStoreFactory == null)
        {
            WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            dataStoreFactory = (DataStoreFactory) ctxt.getBean("dataStoreFactory");
        }
        return dataStoreFactory;
    }

    public IDataStore getDataStore(ServletContext servletContext) {
        if (dataStore == null) {
            dataStore = getDataStoreFactory(servletContext).getDataStore();
        }
        return dataStore;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String path = ((HttpServletRequest) request).getRequestURI();
        ServletContext servletContext = ((HttpServletRequest)request).getSession().getServletContext();
        //logger.info("servletContext = " + servletContext);
        if (RestService.isAuthenticated(((HttpServletRequest)request).getSession()))
        {
            //logger.info("getDataStore = " + getDataStore(servletContext));
            //logger.info("User mail = " + ((HttpServletRequest)request).getSession().getAttribute("user.email").toString())
            User currentUser = getDataStore(servletContext).getUser(((HttpServletRequest)request).getSession().getAttribute("user.email").toString());
            if(currentUser.getUserType().equals(UserType.ADMIN)) {
                chain.doFilter(request, response);
            }
        }
        else {
            HttpServletResponse resp = (HttpServletResponse) response;
            // resp.reset();
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

    }

    @Override
    public void destroy() {


    }

}

