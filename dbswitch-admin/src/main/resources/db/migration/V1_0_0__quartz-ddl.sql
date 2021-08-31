-- IN YOUR QUARTZ PROPERTIES FILE,
-- YOU'LL NEED TO SET ORG.QUARTZ.JOBSTORE.DRIVERDELEGATECLASS = ORG.QUARTZ.IMPL.JDBCJOBSTORE.STDJDBCDELEGATE
-- 你需要在你的QUARTZ.PROPERTIES文件中设置ORG.QUARTZ.JOBSTORE.DRIVERDELEGATECLASS = ORG.QUARTZ.IMPL.JDBCJOBSTORE.STDJDBCDELEGATE
-- STDJDBCDELEGATE说明支持集群，所有的任务信息都会保存到数据库中，可以控制事物，还有就是如果应用服务器关闭或者重启，任务信息都不会丢失，并且可以恢复因服务器关闭或者重启而导致执行失败的任务
-- THIS IS THE SCRIPT FROM QUARTZ TO CREATE THE TABLES IN A MYSQL DATABASE, MODIFIED TO USE INNODB INSTEAD OF MYISAM

-- 存储每一个已配置的JOB的详细信息
CREATE TABLE IF NOT EXISTS DBSWITCH_JOB_DETAILS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME VARCHAR(250) NOT NULL,
    IS_DURABLE VARCHAR(1) NOT NULL,
    IS_NONCONCURRENT VARCHAR(1) NOT NULL,
    IS_UPDATE_DATA VARCHAR(1) NOT NULL,
    REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
) ENGINE=INNODB;

-- 存储已配置的TRIGGER的信息
CREATE TABLE IF NOT EXISTS DBSWITCH_TRIGGERS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT(13) NULL,
    PREV_FIRE_TIME BIGINT(13) NULL,
    PRIORITY INTEGER NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT(13) NOT NULL,
    END_TIME BIGINT(13) NULL,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT(2) NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
        REFERENCES DBSWITCH_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP)
) ENGINE=INNODB;

-- 存储已配置的SIMPLE TRIGGER的信息
CREATE TABLE IF NOT EXISTS DBSWITCH_SIMPLE_TRIGGERS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT(7) NOT NULL,
    REPEAT_INTERVAL BIGINT(12) NOT NULL,
    TIMES_TRIGGERED BIGINT(10) NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ENGINE=INNODB;

-- 存储CRON TRIGGER，包括CRON表达式和时区信息
CREATE TABLE IF NOT EXISTS DBSWITCH_CRON_TRIGGERS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ENGINE=INNODB;

-- 存储简单的Trigger，包括重复次数，间隔，以及已触的次数
CREATE TABLE IF NOT EXISTS DBSWITCH_SIMPROP_TRIGGERS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR(1) NULL,
    BOOL_PROP_2 VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
)ENGINE=INNODB;

-- TRIGGER作为BLOB类型存储(用于QUARTZ用户用JDBC创建他们自己定制的TRIGGER类型，JOBSTORE并不知道如何存储实例的时候)
CREATE TABLE IF NOT EXISTS DBSWITCH_BLOB_TRIGGERS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    INDEX (SCHED_NAME,TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
)ENGINE=INNODB;

-- 以BLOB类型存储QUARTZ的CALENDAR日历信息,QUARTZ可配置一个日历来指定一个时间范围
CREATE TABLE IF NOT EXISTS DBSWITCH_CALENDARS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME VARCHAR(200) NOT NULL,
    CALENDAR BLOB NOT NULL,
    PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
)ENGINE=INNODB;

-- 存储已暂停的TRIGGER组的信息
CREATE TABLE IF NOT EXISTS DBSWITCH_PAUSED_TRIGGER_GRPS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
) ENGINE=INNODB;

-- 存储与已触发的TRIGGER相关的状态信息，以及相联JOB的执行信息
CREATE TABLE IF NOT EXISTS DBSWITCH_FIRED_TRIGGERS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT(13) NOT NULL,
    SCHED_TIME BIGINT(13) NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_NONCONCURRENT VARCHAR(1) NULL,
    REQUESTS_RECOVERY VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,ENTRY_ID)
) ENGINE=INNODB;

-- 存储少量的有关 SCHEDULER的状态信息，和别的 SCHEDULER 实例(假如是用于一个集群中)
CREATE TABLE IF NOT EXISTS DBSWITCH_SCHEDULER_STATE
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT(13) NOT NULL,
    CHECKIN_INTERVAL BIGINT(13) NOT NULL,
    PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
) ENGINE=INNODB;

-- 存储程序的非观锁的信息(假如使用了悲观锁)
CREATE TABLE IF NOT EXISTS DBSWITCH_LOCKS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME VARCHAR(40) NOT NULL,
    PRIMARY KEY (SCHED_NAME,LOCK_NAME)
) ENGINE=INNODB;

-- 创建索引
CREATE INDEX IDX_DBSWITCH_J_REQ_RECOVERY ON DBSWITCH_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_DBSWITCH_J_GRP ON DBSWITCH_JOB_DETAILS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_DBSWITCH_T_J ON DBSWITCH_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_DBSWITCH_T_JG ON DBSWITCH_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_DBSWITCH_T_C ON DBSWITCH_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX IDX_DBSWITCH_T_G ON DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_DBSWITCH_T_STATE ON DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX IDX_DBSWITCH_T_N_STATE ON DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_DBSWITCH_T_N_G_STATE ON DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_DBSWITCH_T_NEXT_FIRE_TIME ON DBSWITCH_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX IDX_DBSWITCH_T_NFT_ST ON DBSWITCH_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_DBSWITCH_T_NFT_MISFIRE ON DBSWITCH_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE INDEX IDX_DBSWITCH_T_NFT_ST_MISFIRE ON DBSWITCH_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX IDX_DBSWITCH_T_NFT_ST_MISFIRE_GRP ON DBSWITCH_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_DBSWITCH_FT_TRIG_INST_NAME ON DBSWITCH_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX IDX_DBSWITCH_FT_INST_JOB_REQ_RCVRY ON DBSWITCH_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_DBSWITCH_FT_J_G ON DBSWITCH_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_DBSWITCH_FT_JG ON DBSWITCH_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_DBSWITCH_FT_T_G ON DBSWITCH_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_DBSWITCH_FT_TG ON DBSWITCH_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);

COMMIT;