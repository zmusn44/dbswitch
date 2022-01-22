// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.controller.privateapi;

import com.gitee.dbswitch.admin.common.annotation.OperateLog;
import com.gitee.dbswitch.admin.common.response.PageResult;
import com.gitee.dbswitch.admin.common.response.Result;
import com.gitee.dbswitch.admin.config.SwaggerConfig;
import com.gitee.dbswitch.admin.model.request.DbConnectionCreateRequest;
import com.gitee.dbswitch.admin.model.request.DbConnectionSearchRequest;
import com.gitee.dbswitch.admin.model.request.DbConnectionUpdateRequest;
import com.gitee.dbswitch.admin.model.response.DbConnectionDetailResponse;
import com.gitee.dbswitch.admin.model.response.DbConnectionNameResponse;
import com.gitee.dbswitch.admin.service.DbConnectionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"连接管理接口"})
@RestController
@RequestMapping(value = SwaggerConfig.API_V1 + "/connection")
public class DbConnectionController {

  @Resource
  private DbConnectionService databaseConnectionService;

  @ApiOperation(value = "数据库类型")
  @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result types() {
    return Result.success(databaseConnectionService.listTypeAll());
  }

  @ApiOperation(value = "连接列表")
  @GetMapping(value = "/list/{page}/{size}", produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResult<DbConnectionDetailResponse> listAll(DbConnectionSearchRequest request,
      @PathVariable("page") Integer page, @PathVariable("size") Integer size) {
    return databaseConnectionService.listAll(request, page, size);
  }

  @ApiOperation(value = "连接详情")
  @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result<DbConnectionDetailResponse> getDetail(@PathVariable("id") Long id) {
    return Result.success(databaseConnectionService.getDetailById(id));
  }

  @ApiOperation(value = "测试连接")
  @GetMapping(value = "/test/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result test(@PathVariable("id") Long id) {
    return databaseConnectionService.test(id);
  }

  @ApiOperation(value = "查询连接的Schema列表")
  @GetMapping(value = "/schemas/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result getSchemas(@PathVariable("id") Long id) {
    return databaseConnectionService.getSchemas(id);
  }

  @ApiOperation(value = "查询连接在制定Schema下的所有表列表")
  @GetMapping(value = "/tables/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result getSchemaTables(@PathVariable("id") Long id,
      @RequestParam("schema") String schema) {
    return databaseConnectionService.getSchemaTables(id, schema);
  }

  @OperateLog(name = "数据库连接", description = "'添加的数据库连接标题为：'+#request.name")
  @ApiOperation(value = "添加连接")
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result<DbConnectionDetailResponse> create(@RequestBody DbConnectionCreateRequest request) {
    return databaseConnectionService.addDatabaseConnection(request);
  }

  @OperateLog(name = "数据库连接", description = "'修改的数据库连接ID为：'+#request.id")
  @ApiOperation(value = "修改连接")
  @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result update(@RequestBody DbConnectionUpdateRequest request) {
    return databaseConnectionService.updateDatabaseConnection(request);
  }

  @OperateLog(name = "数据库连接", description = "'删除的数据库连接ID为：'+#id")
  @ApiOperation(value = "删除连接")
  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result delete(@PathVariable("id") Long id) {
    databaseConnectionService.deleteDatabaseConnection(id);
    return Result.success();
  }

  @ApiOperation(value = "连接名称")
  @GetMapping(value = "/list/name", produces = MediaType.APPLICATION_JSON_VALUE)
  public Result<DbConnectionNameResponse> getNameList() {
    return databaseConnectionService.getNameList();
  }

}
