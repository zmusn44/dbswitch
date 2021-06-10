# 异构数据库数据与结构同步工具

## 一、工具介绍

### 1、功能描述

一句话，dbswitch工具提供源端数据库向目的端数据的迁移同步功能，包括全量和增量方式。迁移包括：

- **结构迁移**

字段类型、主键信息、建表语句等的转换，并生成建表SQL语句。

- **数据迁移**。

基于JDBC的分批次读取源端数据库数据，并基于insert/copy方式将数据分批次写入目的数据库。

支持有主键表的 **增量变更同步** （变化数据计算Change Data Calculate）功能(千万级以上数据量慎用)

### 2、功能设计

 ![function](images/function.PNG)
 
### 3、详细功能

- 源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo向目的端为Greenplum/PostgreSQL/HighGo的迁移(**支持绝大多数常规类型字段**)
 
- 源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo向目的端为Oralce的迁移(**支持绝大多数常规类型字段**)

- 源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo向目的端为DM的迁移(**支持绝大多数常规类型字段...**)

- 源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo向目的端为SQLServer的迁移(**字段类型兼容测试中...**)

- 源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo向目的端为MySQL/MariaDB的迁移(**字段类型兼容测试中...**)

- 源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo向目的端为DB2的迁移(**字段类型兼容测试中...**)

- 源端oracle/SqlServer/MySQL/MariaDB/PostgreSQL/DB2/DM/Kingbase8/HighGo向目的端为Kingbase8的迁移(**支持绝大多数常规类型字段...**)

### 4、结构设计
  
- 模块结构设计

  ![structure](images/stucture.PNG)
  
- 模块结构功能 

```
└── dbswitch
    ├── dbswitch-common    // dbswitch通用定义模块
    ├── dbswitch-pgwriter  // PostgreSQL的二进制写入封装模块
    ├── dbswitch-dbwriter  // 数据库的通用批量Insert封装模块
    ├── dbswitch-core      // 数据库元数据抽取与建表结构语句转换模块
    ├── dbswitch-sql       // 基于calcite的DML语句转换与DDL拼接模块
    ├── dbswitch-dbcommon  // 数据库操作通用封装模块
    ├── dbswitch-dbchange  // 基于全量比对计算变更（变化量）数据模块
    ├── dbswitch-dbsynch   // 将dbchange模块计算的变更数据同步入库模块
    ├── dbswitch-data      // 工具入口模块，读取配置文件中的参数执行异构迁移同步
    ├── dbswitch-webapi    // dbswitch-core与dbswitch-sql的RESTful接口模块
    ├── package-tool       // 基于maven-assembly-plugin插件的项目打包模块
```
 
## 二、编译配置

本工具纯Java语言开发，代码中的依赖全部来自于开源项目。

### 1、编译打包

- 环境要求:

  **JDK**:>=1.8
 
  **maven**:>=3.6
 
- 编译命令:

**(1) windows下：**

```
 双击build.cmd脚本文件即可编译打包
```

**(2) Linux下：**

```
git clone https://gitee.com/inrgihc/dbswitch.git
cd dbswitch/
sh ./build.sh
```

**(3) Docker下:**

```
git clone https://gitee.com/inrgihc/dbswitch.git
cd dbswitch/
sh ./docker-maven-build.sh
```

**特别注意：** 在Java9及以上版本默认情况下不允许应用程序查看来自JDK的所有类，但在dbswitch中利用反射计算对象的字节大小，所以需要在JVM启动时(bin/datasync.sh脚本)需要增加如下参数：
```
--add-opens java.base/jdk.internal.loader=ALL-UNNAMED
```

### 2、安装部署

当编译打包命令执行完成后，会在dbswitch/target/目录下生成dbswitch-relase-x.x.x.tar.gz的打包文件，将文件拷贝到已安装JRE的部署机器上解压即可。

### 3、配置文件

配置文件信息请见部署包中的：conf/config.properties（注：也同时支持使用conf/config.yml配置文件名的YAML格式）

| 配置参数 | 配置说明 | 示例 | 备注 |
| :------| :------ | :------ | :------ |
| dbswitch.source[i].url | 来源端JDBC连接的URL | jdbc:oracle:thin:@10.17.1.158:1521:ORCL | 可为：oracle/mysql/mariadb/sqlserver/postgresql/db2/dm/kingbase8/highgo |
| dbswitch.source[i].driver-class-name | 来源端数据库的驱动类名称 | oracle.jdbc.driver.OracleDriver | 对应数据库的驱动类 |
| dbswitch.source[i].username | 来源端连接帐号名 | tangyibo | 无 |
| dbswitch.source[i].password | 来源端连接帐号密码 | tangyibo | 无 |
| dbswitch.source[i].fetch-size | 来源端数据库查询时的fetch_size设置 | 10000 | 需要大于100有效 |
| dbswitch.source[i].source-schema | 来源端的schema名称 | dbo,test | 多个之间用英文逗号分隔 |
| dbswitch.source[i].prefix-table | 创建对应目的表的前缀 | TA_ | 不能含有特殊字符，可以为空; 建议最长为8个字符，以下划线结尾 |
| dbswitch.source[i].source-includes | 来源端schema下的表中需要包含的表名称 | users1,orgs1 | 支持多个表（多个之间用英文逗号分隔）；支持支持正则表达式(不能含有逗号) |
| dbswitch.source[i].source-excludes | 来源端schema下的表中需要过滤的表名称 | users,orgs | 不包含的表名称，多个之间用英文逗号分隔 |
| dbswitch.target.url | 目的端JDBC连接的URL | jdbc:postgresql://10.17.1.90:5432/study | 可为：oracle/sqlserver/postgresql/greenplum,mysql/mariadb/db2/dm/kingbase8/highgo也支持，但字段类型兼容性问题比较多 |
| dbswitch.target.driver-class-name |目的端 数据库的驱动类名称 | org.postgresql.Driver | 对应数据库的驱动类 |
| dbswitch.target.username | 目的端连接帐号名 | study | 无 |
| dbswitch.target.password | 目的端连接帐号密码 | 123456 | 无 |
| dbswitch.target.target-schema | 目的端的schema名称 | public | 目的端的schema名称只能有且只有一个 |
| dbswitch.target.target-drop | 是否执行先drop表然后create表命令,当target.datasource-target.drop=true时有效 | true | 可选值为：true、false |
| dbswitch.target.create-table-auto-increment | 是否执启用支持create表时主键自增 | true | 可选值为：true、false |
| dbswitch.target.writer-engine-insert | 是否使用insert写入数据 | false | 可选值为：true为insert写入、false为copy写入，只针对目的端数据库为PostgreSQL/Greenplum的有效 |
| dbswitch.target.change-data-synch | 是否启用增量变更同步，dbswitch.target.target-drop为false时且表有主键情况下有效,千万级以上数据量建议设为false | false | 可选值为：true、false |


 **注意:**
 
- （1）支持源端为多个数据源类型，如果```dbswitch.source[i]```为数组类型，i为编号，从0开始的整数； 
 
- （2）如果```dbswitch.source[i].source-includes```不为空，则按照包含表的方式来执行； 

- （3）如果```dbswitch.source[i].source-includes```为空，则按照```dbswitch.source[i].source-excludes```排除表的方式来执行。 

- （4）如果```dbswitch.target.target-drop=false```，```dbswitch.target.change-data-synch=true```；时会对有主键表启用增量变更方式同步 

- （5）同时也支持配置文件名为```conf/config.yml```的YML格式，配置文件样例如下：

```
dbswitch:
  source:
	# source database connection information
	## support MySQL/MariaDB/DB2/DM/Kingbase8/Oracle/SQLServer/PostgreSQL/Greenplum
	## support mutiple source database connection
    - url: jdbc:oracle:thin:@172.17.2.10:1521:ORCL
      driver-class-name: 'oracle.jdbc.driver.OracleDriver'
      username: 'system'
      password: '123456'
      # source database configuration parameters
      ## fetch size for query source database
      fetch-size: 10000
      ## schema name for query source database
      source-schema: 'TANG'
      ## prefix of table name for target name
      prefix-table: 'TA_'
      ## table name include from table lists
      source-includes: ''
      ## table name exclude from table lists
      source-excludes: ''

  target:
    # target database connection information
    ## Best support for Oracle/PostgreSQL/Greenplum/DM/Kingbase8
    url: jdbc:postgresql://172.17.2.10:5432/test
    driver-class-name: org.postgresql.Driver
    username: tang
    password: 123456
    # target database configuration parameters
    ## schema name for create/insert table data
    target-schema: public
    ## whether drop-create table when target table exist
    target-drop: true
    ## whether create table support auto increment for primary key field
    create-table-auto-increment: false
    ## whether use insert engine to write data for target database
    ## Only usefull for PostgreSQL/Greenplum database
    writer-engine-insert: false
    ## whether use change data synchronize to target database table
    change-data-synch: true
```

- （6）各个数据库的JDBC驱动连接示例如下：

**mysql/mariadb的驱动配置样例**

```
jdbc连接地址：jdbc:mysql://172.17.2.10:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true
jdbc驱动名称： com.mysql.cj.jdbc.Driver
```

与:

```
jdbc连接地址：jdbc:mariadb://172.17.2.10:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&nullCatalogMeansCurrent=true
jdbc驱动名称： org.mariadb.jdbc.Driver
```

**oracle的驱动配置样例**

```
jdbc连接地址：jdbc:oracle:thin:@172.17.2.10:1521:ORCL
jdbc驱动名称：oracle.jdbc.driver.OracleDriver
```

**SqlServer(>=2005)的驱动配置样例**

```
jdbc连接地址：jdbc:sqlserver://172.17.2.10:1433;DatabaseName=hqtest
jdbc驱动名称：com.microsoft.sqlserver.jdbc.SQLServerDriver
```

**PostgreSQL/Greenplum的驱动配置样例**

```
jdbc连接地址：jdbc:postgresql://172.17.2.10:5432/study
jdbc驱动名称：org.postgresql.Driver
```

**DB2的驱动配置样例**

```
jdbc连接地址：jdbc:db2://172.17.2.10:50000/testdb:driverType=4;fullyMaterializeLobData=true;fullyMaterializeInputStreams=true;progressiveStreaming=2;progresssiveLocators=2;
jdbc驱动名称：com.ibm.db2.jcc.DB2Driver
```

**达梦DM的驱动配置样例**

```
jdbc连接地址：jdbc:dm://172.17.2.10:5236
jdbc驱动名称：dm.jdbc.driver.DmDriver
```

**人大金仓Kingbase8的驱动配置样例**

```
jdbc连接地址：jdbc:kingbase8://172.17.2.10:54321/MYTEST
jdbc驱动名称：com.kingbase8.Driver
```

**翰高HighGo数据库(可按PostgreSQL使用)**
 
```
jdbc连接地址：jdbc:postgresql://172.17.2.10:5866/highgo
jdbc驱动名称：org.postgresql.Driver
```

### 4、启动方法

- linux系统下：

```
cd dbswitch-release-X.X.X/
bin/datasync.sh
```

- windows系统下：

```
切换到dbswitch-release-X.X.X/bin/目录下，双击datasync.cmd脚本文件即可启动
```

## 三、特别说明

- 1、对于向目的库为PostgreSQL/Greenplum的数据离线同步默认采用copy方式写入数据，说明如下：
  
  **（a）** 如若使用copy方式写入，配置文件中需配置为postgresql的jdbcurl和驱动类（不能为greenplum的驱动包），
  
  **（b）** 如若使用insert方式写入，需要在config.properties配置文件中设置如下参数为true:

```
dbswitch.target.writer-engine-insert=true
```
 
- 2、dbswitch离线同步工具提供各种数据库间表结构转换RESTful类型的API接口，服务启动方式如下：
 
 ```
cd dbswitch-release-X.X.X/
bin/startup.sh
```

提供swagger在线接口文档：htttp://127.0.0.1:9088/swagger-ui.html

- 3、dbswitch离线同步工具支持的数据类型包括：整型、时间、文本、二进制等常用数据类型;

- 4、Oracle的表虽然设置了主键，如果**主键约束实际为DISABLED状态**，那在进行结构转换时会按照没有此主键处理。

- 5、关于增量变更同步方式的使用说明

> 步骤A：先通过设置dbswitch.target.target-drop=true，dbswitch.target.change-data-synch=false；启动程序进行表结构和数据的全量同步;

> 步骤B：然后设置dbswitch.target.target-drop=false，dbswitch.target.change-data-synch=true；再启动程序对（有主键表）数据进行增量变更同步。

> 注：如果待同步的两端表结构已经一致或源端字段是目的端字段的子集，也可直接用步骤B配置进行变更同步

## 四、文档博客

（1）https://blog.csdn.net/inrgihc/article/details/103739629

（2）https://blog.csdn.net/inrgihc/article/details/104642238

（3）https://blog.csdn.net/inrgihc/article/details/103932231

（4）https://blog.csdn.net/inrgihc/article/details/103738656

## 五、问题反馈

如果您看到或使用了本工具，或您觉得本工具对您有价值，请为此项目**点个赞**，多谢！如果您在使用时遇到了bug，欢迎在issue中反馈。也可扫描下方二维码入群讨论：（加好友请注明："程序交流"）

![structure](images/weixin.PNG)


