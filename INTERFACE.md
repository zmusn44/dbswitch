# dbswitch-admin-public-api接口调用说明文档

## 目录

<!-- TOC -->

- [dbswitch-admin-publicapi接口调用说明文档](#dbswitch-admin-public-api接口调用说明文档)
    - [目录](#目录)
    - [一、功能描述](#一功能描述)
    - [二、数据库结构转换的兼容性问题](#二数据库结构转换的兼容性问题)
    - [三、错误异常返回](#三错误异常返回)
    - [四、异构库表结构转换部分接口](#四异构库表结构转换部分接口)
        - [1、获取数据库中所有的模式(model/schema)](#1获取数据库中所有的模式modelschema)
        - [2、获取数据库中指定模式下的所有表](#2获取数据库中指定模式下的所有表)
        - [3、获取业务数据库中指定表的元信息](#3获取业务数据库中指定表的元信息)
        - [4、获取业务数据库中指定SQL的元信息](#4获取业务数据库中指定sql的元信息)
        - [5、转换业务数据库中指定表为建表SQL语句](#5转换业务数据库中指定表为建表sql语句)
        - [6、测试指定数据库中sql有效性](#6测试指定数据库中sql有效性)
    - [五、表结构拼接生成部分接口](#五表结构拼接生成部分接口)
        - [1、创建表拼接生成SQL语句](#1创建表拼接生成sql语句)
        - [2、修改表拼接生成SQL语句](#2修改表拼接生成sql语句)
        - [3、删除表拼接生成SQL语句](#3删除表拼接生成sql语句)
        - [4、清空表拼接生成SQL语句](#4清空表拼接生成sql语句)
    - [附录一、支持的几类数据库数据类型明细表](#附录一支持的几类数据库数据类型明细表)

<!-- /TOC -->

## 一、功能描述

- 支持标准SQL语法DML/DDL(部分)格式与MySQL/MariaDB/Oralce/PostgreSQL/SqlServer/Greenplum/DB2数据库语法的转换；
- 通过给定的数据库连接信息获取相关的元信息数据(模式列表信息、表或视图信息、字段列信息、主键信息等)；
- 异构数据库建根据表结构分析对应数据库的建表语句等；
- 基于函数式的DDL建表/改表/删表/清表的SQL拼接生成；
- 允许使用?占位符进行DML类的SQL进行语法转换；
- 异构数据向PostgreSQL/Greenplum的表结构与数据的迁移；

## 二、数据库结构转换的兼容性问题

 - oracle中 VARCHAR2(4000) 类型可以作为主键，在MySQL中varchar做主键最大长度为255 ;
 - 在MySQL(某些版本)数据库中text、blob等类型不允许做主键 ;
 - 在Greenplum中分布式键不允许修改；
 - 在MySQL数据库中varchar类型的总长度不应大于65535
 - 在MySQL数据库中varchar类型做主键，5.6版本varchar(>255)不能做主键，但在5.7版本中varchar(>1024)不能做主键
 - 整理中....

## 三、错误异常返回

所有接口均存在异常情况，定义的异常返回格式如下：

```
{
  "code": -1,
  "message": "Invalid JSON format：expect ':' at 0, name source:"
}
```

| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |

## 四、异构库表结构转换部分接口

### 1、获取数据库中所有的模式(model/schema)
 
 **URI:** http://host:port/dbswitch/admin/api/v1/database/models_list
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql,mariadb,db2 |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|

**Request Example:**

```
{
    "type":"oracle",
    "host":"10.17.7.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tang",
    "dbname":"orcl",
    "charset":"utf-8"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的模式列表 |

**Response Example:**

```
{
    "data":[
        "SYS",
        "ODI",
        "TEST"
    ],
    "code":0,
    "message":"ok"
}
```

### 2、获取数据库中指定模式下的所有表
 **URI:** http://host:port/dbswitch/admin/api/v1/database/tables_list
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql,mariadb,db2 |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| model | string | 模式名 | Schema名称 |
| charset | string | 字符集 | 数据库的字符集|

**Request Example:**

```
{
    "type":"oracle",
    "host":"10.17.7.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tang",
    "dbname":"orcl",
    "model":"YI_BO",
    "charset":"utf-8"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的数据列表 |
| table_name | string | 表名称 | 表或视图的英文名称 |
| table_type | string | 表类型 | 当表为物理表时标记为table;当表为视图表时标记为view |
| remarks    | string | 中文描述 | 源库里的表注释描述,可能为null |

**Response Example:**

```
{
    "data":[
        {
            "table_type":"table",
            "table_name":"test_world",
            "remarks":"测试表"
        },
        {
            "table_type":"view",
            "table_name":"v_test",
            "remarks":"视图表"
        }
    ],
    "code":0,
    "message":"ok"
}
```

### 3、获取业务数据库中指定表的元信息
 
 **URI:** http://host:port/dbswitch/admin/api/v1/database/table_info
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
 | 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql,mariadb,db2 |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| model | string | 模式名 | Schema名称 |
| charset | string | 字符集 | 数据库的字符集|
| src_table | string | 源表名称 | 查询的源业务库表名的实际名称|
 
**Request Example:**

```
{
    "type":"oracle",
    "host":"10.17.7.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tang",
    "dbname":"orcl",
    "model":"YI_BO",
    "charset":"utf-8",
    "src_table":"C_SEX"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| primary_key | list | 表的主键列 | 表的主键字段列表 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：true-是；false-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | 浮点数的精度 | 浮点数的精度 |
| scale | integer | 浮点数的位数 | 浮点数的位数 |
| class_type | string | 内部存储类型 | 内部存储类型 |
| auto_increment | bool | 是否为自增类型 | 取值：true-是；false-否, 说明：该字段在MySQL/SqlServer/PostgreSQL/mariadb/db2有效，在Oracle无效 |
| remarks    | string | 字段注释 | 源库里的字段的comment描述,可能为null |
| metadata | Object | 表元信息 | 表元信息对象 |
| table_name | string | 表名称 | 表或视图的英文名称 |
| table_type | string | 表类型 | 当表为物理表时标记为table;当表为视图表时标记为view |
| remarks | string | 表注释 | metadata下的remarks字段，取值：null、空字符串、普通字符串|

 **Response Example:**
 
```
{
  "code": 0,
  "message": "success",
  "data": {
    "metadata": {
      "table_name": "C_SEX",
      "remarks": "性别测试表",
      "table_type": "TABLE"
    },
    "columns": [
      {
        "class_type": "java.math.BigDecimal",
        "nullable": false,
        "precision": 11,
        "name": "id",
        "display_size": 12,
        "scale": 0,
        "auto_increment": true,
        "type": "NUMBER",
        "remarks": "编号"
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "name",
        "display_size": 255,
        "scale": 0,
        "auto_increment": false,
        "type": "NVARCHAR2",
        "remarks": "名称"
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "value",
        "display_size": 255,
        "scale": 0,
        "auto_increment": false,
        "type": "NVARCHAR2",
        "remarks": "取值"
      }
    ],
    "primary_key": [
      "id"
    ]
  }
}
```

### 4、获取业务数据库中指定SQL的元信息
 
 **URI:** http://host:port/dbswitch/admin/api/v1/database/sql_info
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
 | 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql,mariadb,db2 |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| model | string | 模式名 | Schema名称 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|
| querysql | string | SQL语句 | SELECT查询的SQL语句|
 
**Request Example:**

```
{
    "type":"oracle",
    "host":"10.17.7.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tang",
    "dbname":"orcl",
    "charset":"utf-8",
    "querysql":"select * from YI_BO.C_SEX"
}
```
 
 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：true-是；false-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | 浮点数的精度 | 浮点数的精度 |
| scale | integer | 浮点数的位数 | 浮点数的位数 |
| class_type | string | 内部存储类型 | 内部存储类型 |
| auto_increment | bool | 是否为自增类型 | 取值：true-是；false-否, 说明：该字段在MySQL/SqlServer/PostgreSQL/mariadb/db2有效，在Oracle无效 |
| remarks    | string | 中文描述 | 源库里的字段的comment描述,可能为null |

 **Response Example:**
 
```
{
  "code": 0,
  "message": "success",
  "data": {
    "columns": [
      {
        "class_type": "java.math.BigDecimal",
        "nullable": false,
        "precision": 11,
        "name": "id",
        "display_size": 12,
        "scale": 0,
        "auto_increment": false,
        "type": "NUMBER",
        "remarks": null
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "name",
        "display_size": 255,
        "scale": 0,
        "auto_increment": false,
        "type": "NVARCHAR2",
        "remarks": null
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "value",
        "display_size": 255,
        "scale": 0,
        "auto_increment": false,
        "type": "NVARCHAR2",
        "remarks": null
      }
    ]
  }
}
```

### 5、转换业务数据库中指定表为建表SQL语句
 
 **URI:** http://host:port/dbswitch/admin/api/v1/database/table_sql
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
 | 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 源数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql,mariadb,db2 |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| charset | string | 字符集 | 数据库的字符集|
| dbname | string | 库名 | 连接的数据库名称 |
| src_model | string | 来源库模式名 | 来源库Schema名称 |
| src_table | string | 来源库源表名称 | 来源库业务库表名的实际名称|
| target | string | 目的数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| dest_model | string | 目的库模式名 | 目的库Schema名称 |
| dest_table | string | 目的库表名称 | 目的库建表的名称|
 
**Request Example:**

```
{
    "type":"oracle",
    "host":"10.17.7.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tang",
    "dbname":"orcl",
    "charset":"utf-8",
    "src_model":"YI_BO",
    "src_table":"C_SEX",
    "target":"mysql",
    "dest_model":"test",
    "dest_table":"test"
}
```
 
 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| create_sql | string | 建表的SQL语句 | 指定数据库语法的建表SQL语句 |
| primary_key | list | 表的主键列 | 表的主键字段列表 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：true-是；false-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | 浮点数的精度 | 浮点数的精度 |
| scale | integer | 浮点数的位数 | 浮点数的位数 |
| class_type | string | 内部存储类型 | 内部存储类型 |
| remarks    | string | 字段注释 | 源库里的字段的comment描述,可能为null |
| metadata | Object | 表元信息 | 表元信息对象 |
| table_name | string | 表名称 | 表或视图的英文名称 |
| table_type | string | 表类型 | 当表为物理表时标记为table;当表为视图表时标记为view |
| remarks | string | 表注释 | metadata下的remarks字段，取值：null、空字符串、普通字符串|

 **Response Example:**
 
```
{
  "code": 0,
  "data": {
    "metadata": {
      "table_name": "C_SEX",
      "remarks": "性别测试表",
      "table_type": "TABLE"
    },
    "columns": [
      {
        "class_type": "java.math.BigDecimal",
        "nullable": false,
        "precision": 11,
        "name": "id",
        "display_size": 12,
        "scale": 0,
        "type": "NUMBER",
        "remarks": "编号"
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "name",
        "display_size": 255,
        "scale": 0,
        "type": "NVARCHAR2",
        "remarks": "名称"
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "value",
        "display_size": 255,
        "scale": 0,
        "type": "NVARCHAR2",
        "remarks": "取值"
      }
    ],
    "create_sql": "CREATE TABLE `test`.`test` (\n\t`id` BIGINT NOT NULL,\n\t`name` VARCHAR(255),\n\t`value` VARCHAR(255),\n\tPRIMARY KEY (`id`)\n)",
    "primary_key": [
      "id"
    ]
  },
  "message": "success"
}
```

### 6、测试指定数据库中sql有效性
 **URI:** http://host:port/dbswitch/admin/api/v1/database/sql_test
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql,mariadb,db2 |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|
| querysql | string | SQL语句 | 待验证的合法SQL|

**Request Example:**

```
{
    "type":"oracle",
    "host":"10.17.7.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tang",
    "dbname":"orcl",
    "querysql":"select * from YI_BO.CJB",
    "charset":"utf-8"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |

**Response Example:**

```
{
    "code":0,            
    "message":"ok"           
}
```

## 五、表结构拼接生成部分接口

### 1、创建表拼接生成SQL语句
 
 **URI:** http://host:port/dbswitch/admin/api/v1/generator/create_table
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------| :------ | :------ | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| schema_name | string | 模式名称 | 模式(model/Schema)名称 |
| table_name | string | 表名称 | 表名称 |
| column_list | list | 列信息 | 数组类型 |
| field_name | string | 字段英文名称 | 登录的帐号名 |
| comment | string | 字段注释 | 登录的密码 |
| field_type | string | 数据类型 | 不同数据库的数据类型存在差异,支持的数据类型请见后面的附录一 |
| length_or_precision | integer | 显示长度 | 显示长度 |
| scale | integer | 存储精度 | 对于浮点型数据与length_or_precision联合确定存储精度|
| nullable | integer | 是否可为空 | 1-为是；0-为否|
| primary_key | integer | 是否为主键 | 1-为是；0-为否|
| auto_increment | integer | 是否为自增 | 1-为是；0-为否|
| default_value | string | 默认值 | 当nullable为0时配置的默认值，对于时间字段默认值问题见下表 |

对于时间字段设置当前时间的**default_value**取值的说明:

| 数据库 | 当前时间的设置方法 |
| :------:| :------: |
| MySQL数据库| CURRENT_TIMESTAMP |
| Oracle数据库| SYSDATE |
| PostgreSQL数据库| (now()) |
| Greenplum数据库| (now()) |

**Request Example:**

```
{
    "type":"mysql",
    "schema_name":"tang",
    "table_name":"test_table",
    "column_list":[
        {
            "field_name":"col1",
            "comment":"列1",
            "field_type":"int",
            "length_or_precision":11,
            "scale":0,
            "nullable":0,
            "primary_key":1,
            "auto_increment":1,
            "default_value":null
        },
        {
            "field_name":"col2",
            "comment":"列2",
            "field_type":"char",
            "length_or_precision":25,
            "scale":0,
            "nullable":0,
            "primary_key":0,
            "auto_increment":0,
            "default_value":"test"
        }
    ]
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 返回的SQL语句 | 返回的SQL语句 |

**Response Example:**

```
{
  "code": 0,
  "data": {
    "sql": " CREATE TABLE `tang`.`test_table` (\n  `col1` INT (11)  NOT NULL AUTO_INCREMENT COMMENT '列1'\n,`col2` CHAR (25)  DEFAULT 'test' COMMENT '列2'\n, PRIMARY KEY (`col1`)\n )\n"
  },
  "message": "success"
}
```

 **Supported Notice**
 
 - 建表参数中的字段注释comment当前只对于MySQL数据库有效，对于Oracle/Greenplum数据库无效；

### 2、修改表拼接生成SQL语句
 
 **URI:** http://host:port/dbswitch/admin/api/v1/generator/alter_table
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------| :------ | :------ | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| schema_name | string | 模式名称 | 模式(model/schema)名称 |
| table_name | string | 表名称 | 表名称 |
| operator | string | 操作类型 | 取值范围请见下表《operator字段的取值说明》 |
| column_list | list | 列信息 | 数组类型 |
| field_name | string | 字段英文名称 | 字段英文名称 |
| comment | string | 字段注释 | 字段注释 |
| field_type | string | 数据类型 | 不同数据库的数据类型存在差异,,支持的数据类型请见后面的附录一 |
| length_or_precision | integer | 显示长度 | 显示长度 |
| scale | integer | 存储精度 | 对于浮点型数据与length_or_precision联合确定存储精度|
| nullable | integer | 是否可为空 | 1-为是；0-为否|
| default_value | string | 默认值 | 当nullable为0时配置的默认值 |

 **operator字段的取值说明**
 
| 取值 | 操作 | 描述 | 特殊说明 |
| :------:| :------: | :------: | :------ |
| add | 添加列 | 向数据库表中增加一列或多列 | 对于Oracle、MySQL两类数据库来说支持一次增加多列，对于PostgreSQL、Greenplum类数据库每次只能增加一列 |
| modify | 修改列 | 修改数据库表中的一列 | 每次只能修改一列的信息，包括列的类型、是否为空、默认值等 |
| drop | 删除列 | 删除数据库表中的一列 | 每次只能删除一列 |

**Request Example:**

```
{
    "type":"mysql",
    "schema_name":"tang",
    "table_name":"test_table",
    "operator":"add",
    "column_list":[
        {
            "field_name":"col1",
            "comment":"列1",
            "field_type":"int",
            "length_or_precision":11,
            "scale":0,
            "nullable":1,
            "default_value":null
        }
    ]
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 返回的SQL语句 | 返回的SQL语句 |

**Response Example:**

```
{
  "code": 0,
  "data": {
    "sql": " CREATE TABLE `tang`.`test_table` (\n  `col1` INT (11)  NOT NULL COMMENT '列1'\n,`col2` CHAR (25)  DEFAULT 'test' COMMENT '列2'\n, PRIMARY KEY (`col1`)\n )\n"
  },
  "message": "success"
}
```

**Supported Notice:**

 - 该接口不支持主键相关修改，Greenplum不支持主键更换修改，主键为分布式键；
 - 该接口不支持rename操作修改字段名；

### 3、删除表拼接生成SQL语句
 
 **URI:** http://host:port/dbswitch/admin/api/v1/generator/drop_table
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| schema_name | string | 模式名称 | 模式(model/Schema)名称 |
| table_name | string | 表名称 | 表名称 |

**Request Example:**

```
{
    "type":"mysql",
    "schema_name":"tang",
    "table_name":"test_table"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 返回的SQL语句 | 返回的SQL语句 |

**Response Example:**

```
{
  "code": 0,
  "data": {
    "sql": "DROP TABLE `public`.`test_table`"
  },
  "message": "success"
}
```

### 4、清空表拼接生成SQL语句
 
 **URI:** http://host:port/dbswitch/admin/api/v1/generator/truncate_table
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| schema_name | string | 模式名称 | 模式(model/Schema)名称 |
| table_name | string | 表名称 | 表名称 |

**Request Example:**

```
{
    "type":"mysql",
    "schema_name":"tang",
    "table_name":"test_table"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| code | integer | 错误码 | 0为成功，其他为失败 |
| message | string | 错误信息 | 当code=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 返回的SQL语句 | 返回的SQL语句 |

**Response Example:**

```
{
  "code": 0,
  "data": {
    "sql": "TRUNCATE TABLE `public`.`test_table`"
  },
  "message": "success"
}
```

## 附录一、支持的几类数据库数据类型明细表

| 数据库 | 类型分类 | 数据类型 | 定义示例 |
| :------:| :------: | :------: | :------ |
| MySQL | 数字 | TINYINT | TINYINT(2) |
| MySQL | 数字 | SMALLINT | SMALLINT(2) |
| MySQL | 数字 | MEDIUMINT | MEDIUMINT(2) |
| MySQL | 数字 | INTEGER | INTEGER(2) |
| MySQL | 数字 | INT | INT(2) |
| MySQL | 数字 | BIGINT | BIGINT(2) |
| MySQL | 数字 | FLOAT | FLOAT(2) |
| MySQL | 数字 | DOUBLE | DOUBLE(2) |
| MySQL | 数字 | DECIMAL | DECIMAL(6,2) |
| MySQL | 时间 | DATE | DATE |
| MySQL | 时间 | TIME | TIME |
| MySQL | 时间 | YEAR | YEAR |
| MySQL | 时间 | DATETIME | DATETIME |
| MySQL | 时间 | TIMESTAMP | TIMESTAMP |
| MySQL | 文本 | CHAR | CHAR(2) |
| MySQL | 文本 | VARCHAR | VARCHAR(2) |
| MySQL | 文本 | TINYBLOB | TINYBLOB(2) |
| MySQL | 文本 | TINYTEXT | TINYTEXT |
| MySQL | 文本 | TEXT | TEXT |
| MySQL | 文本 | MEDIUMTEXT | MEDIUMTEXT |
| MySQL | 文本 | LONGTEXT | LONGTEXT |
| MySQL | 二进制 | BLOB | BLOB |
| MySQL | 二进制 | MEDIUMBLOB | MEDIUMBLOB |
| MySQL | 二进制 | LONGBLOB | LONGBLOB |
| Oracle | 数字 | NUMBER | NUMBER(38,0)、NUMBER(38,2) |
| Oracle | 时间 | DATE | DATE |
| Oracle | 时间 | TIMESTAMP | TIMESTAMP |
| Oracle | 文本 | CHAR | CHAR(2) |
| Oracle | 文本 | NCHAR | NCHAR(2) |
| Oracle | 文本 | VARCHAR | VARCHAR(2) |
| Oracle | 文本 | VARCHAR2 | VARCHAR2(2) |
| Oracle | 文本 | LONG | LONG |
| Oracle | 文本 | CLOB | CLOB |
| Oracle | 二进制 | BLOB | BLOB |
| Greenplum/PostgreSQL | 布尔 | BOOL | BOOL |
| Greenplum/PostgreSQL | 数字 | SMALLINT | SMALLINT(2) |
| Greenplum/PostgreSQL | 数字 | INT2 | INT2 |
| Greenplum/PostgreSQL | 数字 | INTEGER | INTEGER |
| Greenplum/PostgreSQL | 数字 | INT4 | INT4 |
| Greenplum/PostgreSQL | 数字 | BIGINT | BIGINT |
| Greenplum/PostgreSQL | 数字 | INT8 | INT8 |
| Greenplum/PostgreSQL | 数字 | DECIMAL | DECIMAL(8,2) |
| Greenplum/PostgreSQL | 数字 | NUMERIC | NUMERIC(8,2) |
| Greenplum/PostgreSQL | 数字 | REAL | REAL(8,2) |
| Greenplum/PostgreSQL | 数字 | DOUBLE | DOUBLE |
| Greenplum/PostgreSQL | (伪)数字 | SERIAL | SERIAL |
| Greenplum/PostgreSQL | (伪)数字 | BIGSERIAL | BIGSERIAL |
| Greenplum/PostgreSQL | 时间 | DATE | DATE |
| Greenplum/PostgreSQL | 时间 | TIME | TIME |
| Greenplum/PostgreSQL | 时间 | TIMESTAMP | TIMESTAMP |
| Greenplum/PostgreSQL | 文本 | CHAR | CHAR(2) |
| Greenplum/PostgreSQL | 文本 | VARCHAR | VARCHAR(128) |
| Greenplum/PostgreSQL | 文本 | TEXT | TEXT |
| Greenplum/PostgreSQL | 二进制 | BYTEA | BYTEA |
