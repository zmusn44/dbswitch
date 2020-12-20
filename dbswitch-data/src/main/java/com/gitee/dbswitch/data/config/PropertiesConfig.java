// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Data : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.data.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 注册所有映射属性类 { }中用逗号分隔即可注册多个属性类
 * 
 * @author tang
 *
 */
@Configuration
@EnableConfigurationProperties({ DbswichProperties.class })
public class PropertiesConfig {
}
