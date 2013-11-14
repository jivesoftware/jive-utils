package com.jivesoftware.os.server.http.jetty.jersey.server.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class NewRelicRequestFilter implements Filter {

    public NewRelicRequestFilter() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            request.setAttribute("com.newrelic.agent.TRANSACTION_NAME", ((HttpServletRequest) request).getPathInfo());
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
