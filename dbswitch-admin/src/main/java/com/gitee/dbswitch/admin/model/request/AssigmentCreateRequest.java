// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.model.request;

import com.gitee.dbswitch.admin.common.excption.DbswitchException;
import com.gitee.dbswitch.admin.common.response.ResultCode;
import com.gitee.dbswitch.admin.entity.AssignmentConfigEntity;
import com.gitee.dbswitch.admin.entity.AssignmentTaskEntity;
import com.gitee.dbswitch.admin.type.ScheduleModeEnum;
import com.gitee.dbswitch.admin.service.ScheduleService;
import com.gitee.dbswitch.admin.util.JsonUtil;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AssigmentCreateRequest {

  private String name;
  private String description;
  private ScheduleModeEnum scheduleMode;
  private String cronExpression;
  private AssigmentCreateConfig config;

  @NoArgsConstructor
  @Data
  public static class AssigmentCreateConfig {

    private Long sourceConnectionId;
    private List<String> sourceSchemas;
    private Long targetConnectionId;
    private String targetSchema;
    private String tablePrefix;
    private Boolean targetDropTable;
  }

  public AssignmentTaskEntity toAssignmentTask() {
    AssignmentTaskEntity assignment = new AssignmentTaskEntity();
    assignment.setId(null);
    assignment.setName(this.getName());
    assignment.setDescription(this.getDescription());
    assignment.setScheduleMode(this.getScheduleMode());
    if (ScheduleModeEnum.SYSTEM_SCHEDULED == this.getScheduleMode()) {
      if (!ScheduleService.checkCronExpressionValid(this.getCronExpression())) {
        throw new DbswitchException(ResultCode.ERROR_INVALID_ARGUMENT, this.getCronExpression());
      }

      assignment.setCronExpression(this.getCronExpression());
    }

    return assignment;
  }


  public AssignmentConfigEntity toAssignmentConfig(Long assignmentId) {
    AssignmentConfigEntity assignmentConfigEntity = new AssignmentConfigEntity();
    assignmentConfigEntity.setAssignmentId(assignmentId);
    assignmentConfigEntity.setSourceConnectionId(this.getConfig().getSourceConnectionId());
    assignmentConfigEntity
        .setSourceSchemas(JsonUtil.toJsonString(this.getConfig().getSourceSchemas()));
    assignmentConfigEntity.setTargetConnectionId(this.getConfig().getTargetConnectionId());
    assignmentConfigEntity.setTargetSchema(this.getConfig().getTargetSchema());
    assignmentConfigEntity.setTablePrefix(this.getConfig().getTablePrefix());
    assignmentConfigEntity.setTargetDropTable(this.getConfig().getTargetDropTable());
    assignmentConfigEntity.setFirstFlag(Boolean.TRUE);

    return assignmentConfigEntity;
  }

}
