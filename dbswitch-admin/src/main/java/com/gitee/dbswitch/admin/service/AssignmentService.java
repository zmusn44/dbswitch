// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.service;

import com.gitee.dbswitch.admin.common.converter.ConverterFactory;
import com.gitee.dbswitch.admin.common.excption.DbswitchException;
import com.gitee.dbswitch.admin.common.response.PageResult;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.common.response.ResultCode;
import com.gitee.dbswitch.admin.controller.converter.AssignmentDetailConverter;
import com.gitee.dbswitch.admin.controller.converter.AssignmentInfoConverter;
import com.gitee.dbswitch.admin.dao.AssignmentConfigDAO;
import com.gitee.dbswitch.admin.dao.AssignmentTaskDAO;
import com.gitee.dbswitch.admin.dao.DatabaseConnectionDAO;
import com.gitee.dbswitch.admin.entity.AssignmentConfigEntity;
import com.gitee.dbswitch.admin.entity.AssignmentTaskEntity;
import com.gitee.dbswitch.admin.entity.DatabaseConnectionEntity;
import com.gitee.dbswitch.admin.model.request.AssigmentCreateRequest;
import com.gitee.dbswitch.admin.model.request.AssigmentUpdateRequest;
import com.gitee.dbswitch.admin.model.response.AssignmentDetailResponse;
import com.gitee.dbswitch.admin.model.response.AssignmentInfoResponse;
import com.gitee.dbswitch.admin.type.ScheduleModeEnum;
import com.gitee.dbswitch.admin.util.JsonUtil;
import com.gitee.dbswitch.admin.util.PageUtil;
import com.gitee.dbswitch.data.config.DbswichProperties;
import com.gitee.dbswitch.data.config.DbswichProperties.SourceDataSourceProperties;
import com.gitee.dbswitch.data.config.DbswichProperties.TargetDataSourceProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentService {

  @Resource
  private AssignmentTaskDAO assignmentTaskDAO;

  @Resource
  private AssignmentConfigDAO assignmentConfigDAO;

  @Resource
  private ScheduleService scheduleService;

  @Resource
  private DatabaseConnectionDAO databaseConnectionDAO;

  @Transactional(rollbackFor = Exception.class)
  public AssignmentInfoResponse createAssignment(AssigmentCreateRequest request) {
    AssignmentTaskEntity assignment = request.toAssignmentTask();
    assignmentTaskDAO.insert(assignment);

    AssignmentConfigEntity assignmentConfigEntity = request.toAssignmentConfig(assignment.getId());
    assignmentConfigDAO.insert(assignmentConfigEntity);

    return ConverterFactory.getConverter(AssignmentInfoConverter.class)
        .convert(assignmentTaskDAO.getById(assignment.getId()));
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteAssignment(Long id) {
    assignmentTaskDAO.deleteById(id);
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateAssignment(AssigmentUpdateRequest request) {
    AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(request.getId());
    if (Objects.isNull(assignmentTaskEntity)) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "ID=" + request.getId());
    } else if (assignmentTaskEntity.getPublished()) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_HAS_DEPLOY, "ID=" + request.getId());
    }

    AssignmentTaskEntity newAssignmentTaskEntity = request.toAssignmentTask();
    assignmentTaskDAO.updateById(newAssignmentTaskEntity);

    AssignmentConfigEntity assignmentConfigEntity = request
        .toAssignmentConfig(assignmentTaskEntity.getId());
    assignmentConfigDAO.deleteByAssignmentTaskId(assignmentTaskEntity.getId());
    assignmentConfigDAO.insert(assignmentConfigEntity);
  }

  public PageResult<AssignmentInfoResponse> listAll(String searchText, Integer page, Integer size) {
    Supplier<List<AssignmentInfoResponse>> method = () ->
        ConverterFactory.getConverter(AssignmentInfoConverter.class)
            .convert(assignmentTaskDAO.listAll(searchText));

    return PageUtil.getPage(method, page, size);
  }

  public Result<AssignmentDetailResponse> detailAssignment(Long id) {
    AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
    if (Objects.isNull(assignmentTaskEntity)) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "ID=" + id);
    }

    AssignmentDetailResponse detailResponse = ConverterFactory
        .getConverter(AssignmentDetailConverter.class).convert(assignmentTaskEntity);
    return Result.success(detailResponse);
  }

  @Transactional(rollbackFor = Exception.class)
  public void deployAssignments(List<Long> ids) {
    checkAssignmentAllExist(ids);

    //检查是否有已经发布过
    ids.forEach(id -> {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      if (assignmentTaskEntity.getPublished()) {
        throw new DbswitchException(ResultCode.ERROR_RESOURCE_HAS_DEPLOY, "ID=" + id);
      }
    });

    for (Long id : ids) {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      AssignmentConfigEntity assignmentConfigEntity = assignmentConfigDAO.getByAssignmentTaskId(id);

      DbswichProperties properties = new DbswichProperties();
      SourceDataSourceProperties srcConfig = this.getSourceDataSourceProperties(
          assignmentConfigEntity);
      properties.setSource(Collections.singletonList(srcConfig));
      properties.setTarget(this.getTargetDataSourceProperties(assignmentConfigEntity));

      assignmentTaskEntity.setPublished(Boolean.TRUE);
      assignmentTaskEntity.setContent(JsonUtil.toJsonString(properties));
      assignmentTaskDAO.updateById(assignmentTaskEntity);

      ScheduleModeEnum systemScheduled = ScheduleModeEnum.SYSTEM_SCHEDULED;
      if (assignmentTaskEntity.getScheduleMode() == systemScheduled) {
        scheduleService.scheduleTask(assignmentTaskEntity.getId(), systemScheduled);
      }
    }

  }

  @Transactional(rollbackFor = Exception.class)
  public void runAssignments(List<Long> ids) {
    checkAssignmentAllExist(ids);

    //检查是否有已经都发布了
    List<AssignmentTaskEntity> tasks = new ArrayList<>();
    for (Long id : ids) {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      if (assignmentTaskEntity.getPublished()) {
        tasks.add(assignmentTaskEntity);
      } else {
        throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_DEPLOY, "ID=" + id);
      }
    }

    tasks.forEach(assignmentTask -> {
      scheduleService.scheduleTask(assignmentTask.getId(), ScheduleModeEnum.MANUAL);
    });

  }

  @Transactional(rollbackFor = Exception.class)
  public void retireAssignments(List<Long> ids) {
    checkAssignmentAllExist(ids);
    for (Long id : ids) {
      AssignmentTaskEntity assignmentTaskEntity = assignmentTaskDAO.getById(id);
      if (Objects.nonNull(assignmentTaskEntity.getPublished()) &&
          assignmentTaskEntity.getPublished()) {
        scheduleService.cancelJobByTaskId(id);
        assignmentTaskEntity.setPublished(Boolean.FALSE);
        assignmentTaskEntity.setContent("{}");
        assignmentTaskDAO.updateById(assignmentTaskEntity);
      }

    }
  }

  private void checkAssignmentAllExist(List<Long> ids) {
    for (Long id : ids) {
      if (Objects.isNull(assignmentTaskDAO.getById(id))) {
        throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "ID=" + id);
      }
    }
  }

  private SourceDataSourceProperties getSourceDataSourceProperties(
      AssignmentConfigEntity assignmentConfigEntity) {
    SourceDataSourceProperties sourceDataSourceProperties = new SourceDataSourceProperties();
    DatabaseConnectionEntity sourceDatabaseConnectionEntity = databaseConnectionDAO
        .getById(assignmentConfigEntity.getSourceConnectionId());
    sourceDataSourceProperties.setUrl(sourceDatabaseConnectionEntity.getUrl());
    sourceDataSourceProperties.setDriverClassName(sourceDatabaseConnectionEntity.getDriver());
    sourceDataSourceProperties.setUsername(sourceDatabaseConnectionEntity.getUsername());
    sourceDataSourceProperties.setPassword(sourceDatabaseConnectionEntity.getPassword());

    String sourceSchemas = assignmentConfigEntity.getSourceSchemas();
    List<String> schemas = JsonUtil.toBeanList(sourceSchemas, String.class);
    sourceDataSourceProperties.setSourceSchema(schemas.stream().collect(Collectors.joining(",")));
    sourceDataSourceProperties.setPrefixTable(assignmentConfigEntity.getTablePrefix());
    sourceDataSourceProperties.setFetchSize(10000);
    return sourceDataSourceProperties;
  }

  private TargetDataSourceProperties getTargetDataSourceProperties(
      AssignmentConfigEntity assignmentConfigEntity) {
    TargetDataSourceProperties targetDataSourceProperties = new TargetDataSourceProperties();
    DatabaseConnectionEntity targetDatabaseConnectionEntity = databaseConnectionDAO
        .getById(assignmentConfigEntity.getTargetConnectionId());
    targetDataSourceProperties.setUrl(targetDatabaseConnectionEntity.getUrl());
    targetDataSourceProperties.setDriverClassName(targetDatabaseConnectionEntity.getDriver());
    targetDataSourceProperties.setUsername(targetDatabaseConnectionEntity.getUsername());
    targetDataSourceProperties.setPassword(targetDatabaseConnectionEntity.getPassword());
    targetDataSourceProperties.setTargetSchema(assignmentConfigEntity.getTargetSchema());
    if (assignmentConfigEntity.getTargetDropTable()) {
      targetDataSourceProperties.setTargetDrop(Boolean.TRUE);
      targetDataSourceProperties.setChangeDataSynch(Boolean.FALSE);
    } else {
      targetDataSourceProperties.setTargetDrop(Boolean.FALSE);
      targetDataSourceProperties.setChangeDataSynch(Boolean.TRUE);
    }

    return targetDataSourceProperties;
  }

}
