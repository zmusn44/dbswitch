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
import com.gitee.dbswitch.admin.common.response.PageResult;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.common.response.ResultCode;
import com.gitee.dbswitch.admin.controller.converter.DbConnectionDetailConverter;
import com.gitee.dbswitch.admin.dao.DatabaseConnectionDAO;
import com.gitee.dbswitch.admin.entity.DatabaseConnectionEntity;
import com.gitee.dbswitch.admin.model.request.DbConnectionCreateRequest;
import com.gitee.dbswitch.admin.model.request.DbConnectionSearchRequest;
import com.gitee.dbswitch.admin.model.request.DbConnectionUpdateRequest;
import com.gitee.dbswitch.admin.model.response.DatabaseTypeDetailResponse;
import com.gitee.dbswitch.admin.model.response.DbConnectionDetailResponse;
import com.gitee.dbswitch.admin.model.response.DbConnectionNameResponse;
import com.gitee.dbswitch.admin.type.SupportDbTypeEnum;
import com.gitee.dbswitch.admin.util.JDBCURL;
import com.gitee.dbswitch.admin.util.PageUtil;
import com.gitee.dbswitch.common.constant.DatabaseTypeEnum;
import com.gitee.dbswitch.core.service.IMetaDataService;
import com.gitee.dbswitch.core.service.impl.MigrationMetaDataServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DbConnectionService {

  @Resource
  private DatabaseConnectionDAO databaseConnectionDAO;

  public List<DatabaseTypeDetailResponse> listTypeAll() {
    List<DatabaseTypeDetailResponse> lists = new ArrayList<>();
    for (SupportDbTypeEnum type : SupportDbTypeEnum.values()) {
      DatabaseTypeDetailResponse detail = new DatabaseTypeDetailResponse();
      detail.setId(type.getId());
      detail.setType(type.getName().toUpperCase());
      detail.setDriver(type.getDriver());
      detail.setTemplate(StringUtils.join(type.getUrl(), ","));

      lists.add(detail);
    }

    return lists;
  }

  public PageResult<DbConnectionDetailResponse> listAll(DbConnectionSearchRequest request,
      Integer page,
      Integer size) {
    Supplier<List<DbConnectionDetailResponse>> method = () -> {
      List<DatabaseConnectionEntity> databaseConnectionEntities = databaseConnectionDAO
          .listAll(request.getSearchText());
      return ConverterFactory.getConverter(DbConnectionDetailConverter.class)
          .convert(databaseConnectionEntities);
    };

    return PageUtil.getPage(method, page, size);
  }

  public DbConnectionDetailResponse getDetailById(Long id) {
    return ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(databaseConnectionDAO.getById(id));
  }

  public Result test(Long id) {
    DatabaseConnectionEntity dbConn = databaseConnectionDAO.getById(id);
    if (Objects.isNull(dbConn)) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + id);
    }

    SupportDbTypeEnum supportDbType = SupportDbTypeEnum
        .valueOf(dbConn.getType().getName().toUpperCase());
    for (String pattern : supportDbType.getUrl()) {
      final Matcher matcher = JDBCURL.getPattern(pattern).matcher(dbConn.getUrl());
      if (!matcher.matches()) {
        if (1 == supportDbType.getUrl().length) {
          return Result.failed(ResultCode.ERROR_INVALID_JDBC_URL, dbConn.getUrl());
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
        return Result.failed(ResultCode.ERROR_CANNOT_CONNECT_REMOTE, dbConn.getUrl());
      }

      DatabaseTypeEnum prd = DatabaseTypeEnum.valueOf(dbConn.getType().getName().toUpperCase());
      IMetaDataService metaDataService = new MigrationMetaDataServiceImpl();
      metaDataService.setDatabaseConnection(prd);
      metaDataService.testQuerySQL(
          dbConn.getUrl(),
          dbConn.getUsername(),
          dbConn.getPassword(),
          dbConn.getType().getSql()
      );
    }
    return Result.success();
  }

  public Result<List<String>> getSchemas(Long id) {
    DatabaseConnectionEntity dbConn = databaseConnectionDAO.getById(id);
    if (Objects.isNull(dbConn)) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + id);
    }

    DatabaseTypeEnum dbType = DatabaseTypeEnum.valueOf(dbConn.getType().getName().toUpperCase());
    IMetaDataService metaDataService = new MigrationMetaDataServiceImpl();
    metaDataService.setDatabaseConnection(dbType);
    List<String> schemas = metaDataService
        .querySchemaList(dbConn.getUrl(), dbConn.getUsername(), dbConn.getPassword());
    return Result.success(schemas);
  }

  public Result<DbConnectionDetailResponse> addDatabaseConnection(
      DbConnectionCreateRequest request) {
    if (Objects.isNull(request.getName())) {
      return Result.failed(ResultCode.ERROR_INVALID_ARGUMENT, "name is null");
    }

    if (Objects.nonNull(databaseConnectionDAO.getByName(request.getName()))) {
      return Result.failed(ResultCode.ERROR_RESOURCE_ALREADY_EXISTS, "name=" + request.getName());
    }

    DatabaseConnectionEntity conn = request.toDatabaseConnection();
    databaseConnectionDAO.insert(conn);

    return Result.success(ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(databaseConnectionDAO.getById(conn.getId())));
  }

  public Result<DbConnectionDetailResponse> updateDatabaseConnection(
      DbConnectionUpdateRequest request) {
    if (Objects.isNull(request.getId()) || Objects
        .isNull(databaseConnectionDAO.getById(request.getId()))) {
      return Result.failed(ResultCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + request.getId());
    }

    DatabaseConnectionEntity conn = request.toDatabaseConnection();
    databaseConnectionDAO.updateById(conn);

    return Result.success(ConverterFactory.getConverter(DbConnectionDetailConverter.class)
        .convert(databaseConnectionDAO.getById(conn.getId())));
  }

  public void deleteDatabaseConnection(Long id) {
    databaseConnectionDAO.deleteById(id);
  }

  public Result<DbConnectionNameResponse> getNameList() {
    List<DatabaseConnectionEntity> lists = databaseConnectionDAO.listAll(null);
    List<DbConnectionNameResponse> ret = lists.parallelStream()
        .map(c -> new DbConnectionNameResponse(c.getId(), c.getName()))
        .collect(Collectors.toList());
    return Result.success(ret);
  }

}
