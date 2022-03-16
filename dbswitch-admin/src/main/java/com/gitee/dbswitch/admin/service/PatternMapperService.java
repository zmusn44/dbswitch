package com.gitee.dbswitch.admin.service;

import com.gitee.dbswitch.admin.common.excption.DbswitchException;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.common.response.ResultCode;
import com.gitee.dbswitch.admin.entity.DatabaseConnectionEntity;
import com.gitee.dbswitch.admin.model.request.PreviewColumnNameMapperRequest;
import com.gitee.dbswitch.admin.model.request.PreviewTableNameMapperRequest;
import com.gitee.dbswitch.admin.model.response.PreviewNameMapperResponse;
import com.gitee.dbswitch.common.util.PatterNameUtils;
import com.gitee.dbswitch.core.model.ColumnDescription;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataByJdbcService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PatternMapperService {

  @Resource
  private DbConnectionService connectionService;

  public Result<List<PreviewNameMapperResponse>> previewTableNamesMapper(
      PreviewTableNameMapperRequest request) {
    boolean include = true;
    if (null != request.getIsInclude() && !request.getIsInclude()) {
      include = false;
    }
    List<PreviewNameMapperResponse> result = new ArrayList<>();
    if (CollectionUtils.isEmpty(request.getTableNames())) {
      for (TableDescription td : getAllTableNames(request)) {
        result.add(PreviewNameMapperResponse.builder()
            .originalName(td.getTableName())
            .targetName(PatterNameUtils.getFinalName(td.getTableName(), request.getNameMapper()))
            .build());
      }
    } else {
      if (include) {
        for (String name : request.getTableNames()) {
          if (StringUtils.isNotBlank(name)) {
            result.add(PreviewNameMapperResponse.builder()
                .originalName(name)
                .targetName(PatterNameUtils.getFinalName(name, request.getNameMapper()))
                .build());
          }
        }
      } else {
        for (TableDescription td : getAllTableNames(request)) {
          if (!request.getTableNames().contains(td.getTableName())) {
            result.add(PreviewNameMapperResponse.builder()
                .originalName(td.getTableName())
                .targetName(
                    PatterNameUtils.getFinalName(td.getTableName(), request.getNameMapper()))
                .build());
          }
        }
      }
    }
    return Result.success(result);
  }

  public Result<List<PreviewNameMapperResponse>> previewColumnNamesMapper(
      PreviewColumnNameMapperRequest request) {
    if (null == request.getId() || StringUtils.isBlank(request.getSchemaName())
        || StringUtils.isBlank(request.getTableName())) {
      throw new DbswitchException(ResultCode.ERROR_INVALID_ARGUMENT,
          "id or schemaName or tableName");
    }

    List<PreviewNameMapperResponse> result = new ArrayList<>();
    Long connectionId = request.getId();
    DatabaseConnectionEntity dbConn = connectionService.getDatabaseConnectionById(connectionId);
    if (null == dbConn) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + request.getId());
    }
    IMetaDataByJdbcService service = connectionService.getMetaDataCoreService(dbConn);
    List<ColumnDescription> tables = service.queryTableColumnMeta(
        dbConn.getUrl(), dbConn.getUsername(), dbConn.getPassword(), request.getSchemaName(),
        request.getTableName());
    for (ColumnDescription cd : tables) {
      result.add(PreviewNameMapperResponse.builder()
          .originalName(cd.getFieldName())
          .targetName(PatterNameUtils.getFinalName(cd.getFieldName(), request.getNameMapper()))
          .build());
    }

    return Result.success(result);
  }

  private List<TableDescription> getAllTableNames(PreviewTableNameMapperRequest request) {
    if (null == request.getId() || StringUtils.isBlank(request.getSchemaName())) {
      throw new DbswitchException(ResultCode.ERROR_INVALID_ARGUMENT, "id or schemaName");
    }

    Long connectionId = request.getId();
    DatabaseConnectionEntity dbConn = connectionService.getDatabaseConnectionById(connectionId);
    if (null == dbConn) {
      throw new DbswitchException(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + request.getId());
    }
    IMetaDataByJdbcService service = connectionService.getMetaDataCoreService(dbConn);
    return service.queryTableList(
        dbConn.getUrl(),
        dbConn.getUsername(),
        dbConn.getPassword(),
        request.getSchemaName()
    ).stream()
        .filter(td -> !td.isViewTable())
        .collect(Collectors.toList());
  }

}
