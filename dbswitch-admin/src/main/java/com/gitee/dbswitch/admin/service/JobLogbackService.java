package com.gitee.dbswitch.admin.service;

import com.gitee.dbswitch.admin.common.response.PageResult;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.dao.AssignmentJobDAO;
import com.gitee.dbswitch.admin.dao.JobLogbackDAO;
import com.gitee.dbswitch.admin.entity.AssignmentJobEntity;
import com.gitee.dbswitch.admin.entity.JobLogbackEntity;
import com.gitee.dbswitch.admin.model.response.TaskJobLogbackResponse;
import com.gitee.dbswitch.admin.type.JobStatusEnum;
import com.gitee.dbswitch.admin.util.PageUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class JobLogbackService {

  @Resource
  private AssignmentJobDAO assignmentJobDAO;
  @Resource
  private JobLogbackDAO jobLogbackDAO;

  public Result<TaskJobLogbackResponse> tailLog(Long jobId, Integer size) {
    TaskJobLogbackResponse response = new TaskJobLogbackResponse();
    AssignmentJobEntity jobEntity = assignmentJobDAO.getById(jobId);
    if (Objects.isNull(jobEntity)) {
      return Result.success(response);
    }

    Supplier<List<JobLogbackEntity>> method = () -> jobLogbackDAO.getTailByUuid(jobId.toString());
    PageResult<JobLogbackEntity> page = PageUtils.getPage(method, 1, Optional.of(size).orElse(100));
    response.setStatus(jobEntity.getStatus());
    if (!CollectionUtils.isEmpty(page.getData())) {
      response.setMaxId(page.getData().stream().mapToLong(JobLogbackEntity::getId).max().getAsLong());
      response.setLogs(page.getData().stream().map(JobLogbackEntity::getContent).collect(Collectors.toList()));
    } else {
      if (JobStatusEnum.FAIL.getValue() == jobEntity.getStatus()) {
        response.setLogs(Arrays.asList(jobEntity.getErrorLog()));
      }
    }

    return Result.success(response);
  }

  public Result<TaskJobLogbackResponse> nextLog(Long jobId, Long baseId) {
    TaskJobLogbackResponse response = new TaskJobLogbackResponse();
    AssignmentJobEntity jobEntity = assignmentJobDAO.getById(jobId);
    if (Objects.isNull(jobEntity)) {
      return Result.success(response);
    }

    baseId = Optional.ofNullable(baseId).orElse(0L);
    List<JobLogbackEntity> page = jobLogbackDAO.getNextByUuid(jobId.toString(), baseId);
    response.setStatus(jobEntity.getStatus());
    if (!CollectionUtils.isEmpty(page)) {
      response.setMaxId(page.stream().mapToLong(JobLogbackEntity::getId).max().getAsLong());
      response.setLogs(page.stream().map(JobLogbackEntity::getContent).collect(Collectors.toList()));
    }
    if (response.getMaxId() <= baseId) {
      response.setMaxId(baseId);
    }

    return Result.success(response);
  }

}
