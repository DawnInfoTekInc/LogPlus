/**************************************************************************
 * Licensed Material - Dawn InfoTek Inc.                                  *
 * Copyright (c) Dawn InfoTek Inc. 1999, 2005, 2018 - All rights reserved.*
 * (<http://www.dawninfotek.com>)                                         *
 *                                                                        *
 **************************************************************************/
package com.dawninfotek.logx.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dawninfotek.logx.util.LogXFieldsInjector;

/**
 * This is the Major Filter for LogX, for using the LogX, every application
 * should has to configure this filter in the Web Application.
 * 
 * @author Ryan Wang, John Li
 *
 */
//@WebFilter(filterName = "LogXFilter",urlPatterns = {"/*"}) 
// default filter mapping, could be overridden by web.xml
public class LogXFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) {		
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		boolean logxMatching = LogXFieldsInjector.preProcessHttp((HttpServletRequest)request, (HttpServletResponse)response);

		try {
			chain.doFilter(request, response);
		}finally {
			if(logxMatching) {
				LogXFieldsInjector.postProcessHttp((HttpServletRequest)request, (HttpServletResponse)response);
			}
		}

	}

	@Override
	public void destroy() {
	}
	
}