// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.mapper;

import com.gitee.dbswitch.admin.entity.AssignmentConfigEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface AssignmentConfigMapper extends Mapper<AssignmentConfigEntity> {

  @Select("select * from DBSWITCH_ASSIGNMENT_CONFIG where assignment_id=#{assignmentId} LIMIT 1 ")
  AssignmentConfigEntity getByAssignmentId(Long assignmentId);

}
