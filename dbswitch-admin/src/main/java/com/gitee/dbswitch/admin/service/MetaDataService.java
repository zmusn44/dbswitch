package com.gitee.dbswitch.admin.service;

import com.gitee.dbswitch.admin.common.excption.DbswitchException;
import com.gitee.dbswitch.admin.common.response.PageResult;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.common.response.ResultCode;
import com.gitee.dbswitch.admin.entity.DatabaseConnectionEntity;
import com.gitee.dbswitch.admin.model.response.MetadataColumnDetailResponse;
import com.gitee.dbswitch.admin.model.response.MetadataSchemaDetailResponse;
import com.gitee.dbswitch.admin.model.response.MetadataTableDetailResponse;
import com.gitee.dbswitch.admin.model.response.MetadataTableInfoResponse;
import com.gitee.dbswitch.admin.model.response.SchemaTableDataResponse;
import com.gitee.dbswitch.admin.type.SupportDbTypeEnum;
import com.gitee.dbswitch.admin.util.JDBCURL;
import com.gitee.dbswitch.admin.util.PageUtils;
import com.gitee.dbswitch.common.type.DatabaseTypeEnum;
import com.gitee.dbswitch.core.model.SchemaTableData;
import com.gitee.dbswitch.core.model.SchemaTableMeta;
import com.gitee.dbswitch.core.model.TableDescription;
import com.gitee.dbswitch.core.service.IMetaDataService;
import com.gitee.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class MetaDataService {

  @Resource
  private DbConnectionService connectionService;

  public PageResult<MetadataSchemaDetailResponse> allSchemas(Long id, Integer page, Integer size) {
    DatabaseConnectionEntity dbConn = connectionService.getDatabaseConnectionById(id);
    IMetaDataService metaDataService = getMetaDataCoreService(dbConn);
    List<String> schemas = metaDataService.querySchemaList(
        dbConn.getUrl(), dbConn.getUsername(), dbConn.getPassword());
    List<MetadataSchemaDetailResponse> responses = schemas.stream()
        .map(s -> new MetadataSchemaDetailResponse(dbConn.getName(), s))
        .collect(Collectors.toList());
    return PageUtils.getPage(responses, page, size);
  }

  public PageResult<MetadataTableInfoResponse> allTables(Long id, String schema, Integer page,
      Integer size) {
    DatabaseConnectionEntity dbConn = connectionService.getDatabaseConnectionById(id);
    IMetaDataService metaDataService = getMetaDataCoreService(dbConn);
    List<TableDescription> tables = metaDataService.queryTableList(
        dbConn.getUrl(), dbConn.getUsername(), dbConn.getPassword(), schema);
    List<MetadataTableInfoResponse> responses = tables.stream()
        .map(one -> MetadataTableInfoResponse.builder()
            .tableName(one.getTableName())
            .schemaName(one.getSchemaName())
            .remarks(one.getRemarks())
            .type(one.getTableType())
            .build()
        ).collect(Collectors.toList());
    return PageUtils.getPage(responses, page, size);
  }

  public Result<MetadataTableDetailResponse> tableDetail(Long id, String schema, String table) {
    DatabaseConnectionEntity dbConn = connectionService.getDatabaseConnectionById(id);
    IMetaDataService metaDataService = getMetaDataCoreService(dbConn);
    SchemaTableMeta tableMeta = metaDataService.queryTableMeta(
        dbConn.getUrl(),
        dbConn.getUsername(),
        dbConn.getPassword(),
        schema,
        table);

    List<String> pks = tableMeta.getPrimaryKeys();
    List<MetadataColumnDetailResponse> columnDetailResponses = tableMeta.getColumns().stream()
        .map(one -> MetadataColumnDetailResponse.builder()
            .fieldName(one.getFieldName())
            .typeName(one.getFieldTypeName())
            .typeClassName(one.getFiledTypeClassName())
            .fieldType(String.valueOf(one.getFieldType()))
            .displaySize(String.valueOf(one.getDisplaySize()))
            .precisionSize(String.valueOf(one.getPrecisionSize()))
            .scaleSize(String.valueOf(one.getScaleSize()))
            .isPrimaryKey(
                toStr(
                    CollectionUtils.isNotEmpty(pks)
                        && pks.contains(one.getFieldName())))
            .isAutoIncrement(toStr(one.isAutoIncrement()))
            .isNullable(toStr(one.isNullable()))
            .remarks(one.getRemarks())
            .build()
        ).collect(Collectors.toList());

    return Result.success(MetadataTableDetailResponse.builder()
        .tableName(tableMeta.getTableName())
        .schemaName(tableMeta.getSchemaName())
        .remarks(tableMeta.getRemarks())
        .type(tableMeta.getTableType())
        .createSql(tableMeta.getCreateSql())
        .primaryKeys(tableMeta.getPrimaryKeys())
        .columns(columnDetailResponses)
        .build());
  }

  public Result<SchemaTableDataResponse> tableData(Long id, String schema, String table) {
    DatabaseConnectionEntity dbConn = connectionService.getDatabaseConnectionById(id);
    IMetaDataService metaDataService = getMetaDataCoreService(dbConn);
    SchemaTableData data = metaDataService.queryTableData(
        dbConn.getUrl(), dbConn.getUsername(), dbConn.getPassword(),
        schema, table, 10);
    return Result.success(SchemaTableDataResponse.builder()
        .schemaName(data.getSchemaName())
        .tableName(data.getTableName())
        .columns(data.getColumns())
        .rows(convertRows(data.getColumns(), data.getRows()))
        .build()
    );
  }

  private List<Map<String, Object>> convertRows(List<String> columns, List<List<Object>> rows) {
    if (null == rows || rows.isEmpty()) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> result = new ArrayList<>(rows.size());
    for (List<Object> row : rows) {
      Map<String, Object> map = new HashMap<>();
      for (int i = 0; i < row.size(); ++i) {
        map.put(columns.get(i), row.get(i));
      }
      result.add(map);
    }
    return result;
  }

  private IMetaDataService getMetaDataCoreService(DatabaseConnectionEntity dbConn) {
    String typeName = dbConn.getType().getName().toUpperCase();
    SupportDbTypeEnum supportDbType = SupportDbTypeEnum.valueOf(typeName);
    for (String pattern : supportDbType.getUrl()) {
      final Matcher matcher = JDBCURL.getPattern(pattern).matcher(dbConn.getUrl());
      if (!matcher.matches()) {
        if (1 == supportDbType.getUrl().length) {
          throw new DbswitchException(ResultCode.ERROR_CANNOT_CONNECT_REMOTE, dbConn.getName());
        } else {
          continue;
        }
      }

      String host = matcher.group("host");
      String port = matcher.group("port");
      if (null == port) {
        port = String.valueOf(supportDbType.getPort());
      }

      if (!JDBCURL.reachable(host, port)) {
        throw new DbswitchException(ResultCode.ERROR_CANNOT_CONNECT_REMOTE, dbConn.getName());
      }
    }
    DatabaseTypeEnum prd = DatabaseTypeEnum.valueOf(dbConn.getType().getName().toUpperCase());
    IMetaDataService metaDataService = new MigrationMetaDataServiceImpl();
    metaDataService.setDatabaseConnection(prd);
    return metaDataService;
  }

  private String toStr(Boolean value) {
    if (null == value) {
      return "未知";
    }
    if (value) {
      return "是";
    }

    return "否";
  }

}
