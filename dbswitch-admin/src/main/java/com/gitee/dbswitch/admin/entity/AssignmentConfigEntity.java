// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.entity;

import com.gitee.dbswitch.admin.handler.ListTypeHandler;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.ColumnType;
import tk.mybatis.mapper.annotation.KeySql;

@NoArgsConstructor
@Data
@Entity
@Table(name = "DBSWITCH_ASSIGNMENT_CONFIG")
public class AssignmentConfigEntity {

  @Id
  @KeySql(useGeneratedKeys = true)
  @Column(name = "id", insertable = false, updatable = false)
  private Long id;

  @Column(name = "assignment_id")
  private Long assignmentId;

  @Column(name = "source_connection_id")
  private Long sourceConnectionId;

  @Column(name = "source_schema")
  private String sourceSchema;

  @Column(name = "source_tables")
  @ColumnType(typeHandler = ListTypeHandler.class)
  private List<String> sourceTables;

  @Column(name = "excluded")
  private Boolean excluded;

  @Column(name = "target_connection_id")
  private Long targetConnectionId;

  @Column(name = "target_schema")
  private String targetSchema;

  @Column(name = "table_prefix")
  private String tablePrefix;

  @Column(name = "target_drop_table")
  private Boolean targetDropTable;

  @Column(name = "batch_size")
  private Integer batchSize;

  @Column(name = "first_flag")
  private Boolean firstFlag;

  @Column(name = "create_time", insertable = false, updatable = false)
  private Timestamp createTime;
}
