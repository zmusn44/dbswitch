// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Datae : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.webapi.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.alibaba.fastjson.JSON;

/**
 * WEB请求日志截面类
 * 
 * @author tang
 *
 */
@Aspect
@Component
public class WebLogAspect {

	private static final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

	@Pointcut("execution(* com.gitee.dbswitch.webapi.controller..*.*(..))")
	public void webLog() {
	}

	@Around("webLog()")
	public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		String className = proceedingJoinPoint.getSignature().getDeclaringTypeName();
		boolean flag = "com.gitee.dbswitch.webapi.controller.ExceptionController".equals(className);
		if (!flag) {
			HashMap<String, String> msg = new HashMap<>();
			msg.put("REQ_URL :", request.getRequestURL().toString());
			msg.put("METHOD  ", request.getMethod());
			msg.put("REMOTE_IP ", request.getRemoteAddr());
			msg.put("CLASS_METHOD ", className + "." + proceedingJoinPoint.getSignature().getName());
			msg.put("BODY_CONTENT : ", Arrays.toString(proceedingJoinPoint.getArgs()));
			logger.info("[Request] {}", JSON.toJSONString(msg));
		}

		long startTime = System.currentTimeMillis();
		Object result = proceedingJoinPoint.proceed();

		if (!flag) {
			logger.info("[Response] : {} , Elipse {} ms", JSON.toJSONString(result), System.currentTimeMillis() - startTime);
		}

		return result;
	}

}
