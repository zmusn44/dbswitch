// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.weishao.dbswitch.webapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {

	/*
	 * This spring security configuration does the following
	 * 
	 * 1. Restrict access to the Shutdown endpoint to the ACTUATOR_ADMIN role. 2.
	 * Allow access to all other actuator endpoints. 3. Allow access to static
	 * resources. 4. Allow access to the home page (/). 5. All other requests need
	 * to be authenticated. 5. Enable http basic authentication to make the
	 * configuration complete. You are free to use any other form of authentication.
	 */

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().anyRequest().permitAll();
		//http.authorizeRequests().requestMatchers(EndpointRequest.to(ShutdownEndpoint.class)).hasRole("ACTUATOR_ADMIN")
		//		.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
		//		.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll().antMatchers("/")
		//		.permitAll().antMatchers("/**").authenticated().and().httpBasic();
	}
}