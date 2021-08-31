// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.controller.converter;

import com.gitee.dbswitch.admin.common.converter.AbstractConverter;
import com.gitee.dbswitch.admin.dao.AssignmentConfigDAO;
import com.gitee.dbswitch.admin.dao.DatabaseConnectionDAO;
import com.gitee.dbswitch.admin.entity.AssignmentConfigEntity;
import com.gitee.dbswitch.admin.entity.AssignmentTaskEntity;
import com.gitee.dbswitch.admin.entity.DatabaseConnectionEntity;
import com.gitee.dbswitch.admin.model.response.AssignmentDetailResponse;
import com.gitee.dbswitch.admin.util.JsonUtil;
import com.gitee.dbswitch.admin.util.SpringUtil;
import java.util.Objects;

public class AssignmentDetailConverter extends
    AbstractConverter<AssignmentTaskEntity, AssignmentDetailResponse> {

  @Override
  public AssignmentDetailResponse convert(AssignmentTaskEntity assignmentTaskEntity) {
    if (Objects.isNull(assignmentTaskEntity)) {
      return null;
    }

    DatabaseConnectionDAO databaseConnectionDAO = SpringUtil.getBean(DatabaseConnectionDAO.class);
    AssignmentConfigDAO assignmentConfigDAO = SpringUtil.getBean(AssignmentConfigDAO.class);
    AssignmentConfigEntity taskConfig = assignmentConfigDAO.getByAssignmentTaskId(
        assignmentTaskEntity.getId());
    DatabaseConnectionEntity srcConn = databaseConnectionDAO.getById(taskConfig.getSourceConnectionId());
    DatabaseConnectionEntity dstConn = databaseConnectionDAO.getById(taskConfig.getTargetConnectionId());

    AssignmentDetailResponse.Configuration config = new AssignmentDetailResponse.Configuration();
    config.setSourceConnectionId(srcConn.getId());
    config.setSourceConnectionName(srcConn.getName());
    config.setSourceSchemas(JsonUtil.toBeanList(taskConfig.getSourceSchemas(), String.class));
    config.setTargetConnectionId(dstConn.getId());
    config.setTargetConnectionName(dstConn.getName());
    config.setTargetSchema(taskConfig.getTargetSchema());
    config.setTablePrefix(taskConfig.getTablePrefix());

    AssignmentDetailResponse detailResponse = new AssignmentDetailResponse();
    detailResponse.setId(assignmentTaskEntity.getId());
    detailResponse.setName(assignmentTaskEntity.getName());
    detailResponse.setDescription(assignmentTaskEntity.getDescription());
    detailResponse.setScheduleMode(assignmentTaskEntity.getScheduleMode());
    detailResponse.setCronExpression(assignmentTaskEntity.getCronExpression());
    detailResponse.setCreateTime(assignmentTaskEntity.getCreateTime());
    detailResponse.setUpdateTime(assignmentTaskEntity.getUpdateTime());
    detailResponse.setConfiguration(config);

    return detailResponse;
  }

}
