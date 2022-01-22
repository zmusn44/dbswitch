<template>
  <div>
    <el-card>
      <div align="right"
           style="margin:10px 5px;"
           width="65%">
        <el-button type="primary"
                   icon="el-icon-document-add"
                   @click="handleCreate">添加</el-button>
      </div>
      <el-table :data="tableData"
                size="small"
                border>
        <el-table-column prop="id"
                         label="编号"
                         min-width="8%"></el-table-column>
        <el-table-column prop="name"
                         label="名称"
                         min-width="30%"></el-table-column>
        <el-table-column prop="scheduleMode"
                         label="调度"
                         :formatter="stringFormatSchedule"
                         min-width="8%"></el-table-column>
        <el-table-column prop="isPublished"
                         label="已发布"
                         :formatter="boolFormatPublish"
                         :show-overflow-tooltip="true"
                         min-width="8%"></el-table-column>
        <el-table-column prop="createTime"
                         label="时间"
                         min-width="15%"></el-table-column>
        <el-table-column label="操作"
                         min-width="40%">
          <template slot-scope="scope">
            <el-button size="small"
                       type="success"
                       v-if="scope.row.isPublished===false"
                       @click="handlePublish(scope.$index, scope.row)"><i class="el-icon-timer el-icon--right"></i>发布</el-button>
            <el-button size="small"
                       type="warning"
                       v-if="scope.row.isPublished===true"
                       @click="handleRetireTask(scope.$index, scope.row)"><i class="el-icon-delete-location el-icon--right"></i>下线</el-button>
            <el-button size="small"
                       type="danger"
                       v-if="scope.row.isPublished===true"
                       @click="handleRunTask(scope.$index, scope.row)"><i class="el-icon-video-play el-icon--right"></i>执行</el-button>
            <el-dropdown size="small"
                         split-button
                         type="primary">
              更多
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native.prevent="handleUpdate(scope.$index, scope.row)">修改</el-dropdown-item>
                <el-dropdown-item @click.native.prevent="handleDelete(scope.$index, scope.row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <div class="page"
           align="right">
        <el-pagination :current-page="currentPage"
                       :page-sizes="[5, 10, 20, 40]"
                       :page-size="pageSize"
                       layout="total, sizes, prev, pager, next, jumper"
                       :total="totalCount"></el-pagination>
      </div>

      <el-dialog title="添加任务"
                 :visible.sync="createFormVisible"
                 :showClose="false"
                 :before-close="handleClose">
        <el-form :model="createform"
                 size="mini"
                 status-icon
                 :rules="rules"
                 ref="createform">
          <el-form-item label="名称"
                        label-width="120px"
                        :required=true
                        prop="name"
                        style="width:85%">
            <el-input v-model="createform.name"
                      auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="描述"
                        label-width="120px"
                        prop="description"
                        style="width:85%">
            <el-input v-model="createform.description"
                      type="textarea"
                      :rows="3"
                      auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="调度方式"
                        label-width="120px"
                        :required=true
                        prop="scheduleMode"
                        style="width:85%">
            <el-select v-model="createform.scheduleMode">
              <el-option label="手动调度"
                         value="MANUAL"></el-option>
              <el-option label="系统调度"
                         value="SYSTEM_SCHEDULED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="Cron表达式"
                        label-width="120px"
                        style="width:85%"
                        v-if="createform.scheduleMode=='SYSTEM_SCHEDULED'">
            <el-col :span="8">
              <el-popover v-model="cronPopover">
                <vueCron @change="changeCreateCronExpression"
                         @close="cronPopover=false"
                         i18n="cn" />
                <el-input slot="reference"
                          disabled="true"
                          v-model="createform.cronExpression"
                          placeholder="定时策略"
                          @click="cronPopover=true" />
              </el-popover>
            </el-col>
          </el-form-item>
          <el-form-item label="来源端数据源"
                        label-width="120px"
                        :required=true
                        prop="sourceConnectionId"
                        style="width:85%">
            <el-select v-model="createform.sourceConnectionId"
                       @change="selectChangedSourceConnection"
                       placeholder="请选择">
              <el-option v-for="(item,index) in connectionNameList"
                         :key="index"
                         :label="item.name"
                         :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="源端模式名"
                        label-width="120px"
                        :required=true
                        prop="sourceSchema"
                        style="width:65%">
            <el-select v-model="createform.sourceSchema"
                       @change="selectCreateChangedSourceSchema"
                       placeholder="请选择">
              <el-option v-for="(item,index) in sourceConnectionSchemas"
                         :key="index"
                         :label="item"
                         :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="配置方式"
                        label-width="120px"
                        :required=true
                        prop="includeOrExclude"
                        style="width:65%">
            <el-select placeholder="请选择配置方式"
                       v-model="createform.includeOrExclude">
              <el-option label="包含表"
                         value="INCLUDE"></el-option>
              <el-option label="排除表"
                         value="EXCLUDE"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="表名配置"
                        label-width="120px"
                        :required=false
                        prop="sourceTables"
                        style="width:65%">
            <el-select placeholder="请选择表名"
                       multiple
                       v-model="createform.sourceTables">
              <el-option v-for="(item,index) in sourceSchemaTables"
                         :key="index"
                         :label="item"
                         :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="目的端数据源"
                        label-width="120px"
                        :required=true
                        prop="targetConnectionId"
                        style="width:85%">
            <el-select v-model="createform.targetConnectionId"
                       @change="selectChangedTargetConnection"
                       placeholder="请选择">
              <el-option v-for="(item,index) in connectionNameList"
                         :key="index"
                         :label="item.name"
                         :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="目的端模式名"
                        label-width="120px"
                        :required=true
                        prop="targetSchema"
                        style="width:65%">
            <el-select v-model="createform.targetSchema"
                       placeholder="请选择">
              <el-option v-for="(item,index) in targetConnectionSchemas"
                         :key="index"
                         :label="item"
                         :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="目的端表名前缀"
                        label-width="120px"
                        style="width:85%">
            <el-input v-model="createform.tablePrefix"
                      auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="数据批次大小"
                        label-width="120px"
                        :required=true
                        prop="batchSize"
                        style="width:85%">
            <el-select v-model="createform.batchSize">
              <el-option label="1000"
                         value="1000"></el-option>
              <el-option label="5000"
                         value="5000"></el-option>
              <el-option label="10000"
                         value="10000"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer"
             class="dialog-footer">
          <el-button @click="createFormVisible = false">取 消</el-button>
          <el-button type="primary"
                     @click="handleCreateSave">确 定</el-button>
        </div>
      </el-dialog>

      <el-dialog title="修改任务"
                 :visible.sync="updateFormVisible"
                 :showClose="false"
                 :before-close="handleClose">
        <el-form :model="updateform"
                 size="mini"
                 status-icon
                 :rules="rules"
                 ref="updateform">
          <el-form-item label="名称"
                        label-width="120px"
                        :required=true
                        prop="name"
                        style="width:85%">
            <el-input v-model="updateform.name"
                      auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="描述"
                        label-width="120px"
                        style="width:85%">
            <el-input v-model="updateform.description"
                      type="textarea"
                      :rows="3"
                      auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="调度方式"
                        label-width="120px"
                        :required=true
                        prop="scheduleMode"
                        style="width:85%">
            <el-select v-model="updateform.scheduleMode">
              <el-option label="手动调度"
                         value="MANUAL"></el-option>
              <el-option label="系统调度"
                         value="SYSTEM_SCHEDULED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="Cron表达式"
                        label-width="120px"
                        style="width:85%"
                        v-if="updateform.scheduleMode=='SYSTEM_SCHEDULED'">
            <el-col :span="8">
              <el-popover v-model="cronPopover">
                <vueCron @change="changeUpdateCronExpression"
                         @close="cronPopover=false"
                         i18n="cn" />
                <el-input slot="reference"
                          disabled="true"
                          v-model="updateform.cronExpression"
                          placeholder="定时策略"
                          @click="cronPopover=true" />
              </el-popover>
            </el-col>
          </el-form-item>
          <el-form-item label="来源端数据源"
                        label-width="120px"
                        :required=true
                        prop="sourceConnectionId"
                        style="width:85%">
            <el-select v-model="updateform.sourceConnectionId"
                       @change="selectChangedSourceConnection"
                       placeholder="请选择">
              <el-option v-for="(item,index) in connectionNameList"
                         :key="index"
                         :label="item.name"
                         :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="源端模式名"
                        label-width="120px"
                        :required=true
                        prop="sourceSchema"
                        style="width:65%">
            <el-select v-model="updateform.sourceSchema"
                       @change="selectUpdateChangedSourceSchema"
                       placeholder="请选择">
              <el-option v-for="(item,index) in sourceConnectionSchemas"
                         :key="index"
                         :label="item"
                         :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="配置方式"
                        label-width="120px"
                        :required=true
                        prop="includeOrExclude"
                        style="width:65%">
            <el-select placeholder="请选择配置方式"
                       v-model="updateform.includeOrExclude">
              <el-option label="包含表"
                         value="INCLUDE"></el-option>
              <el-option label="排除表"
                         value="EXCLUDE"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="表名配置"
                        label-width="120px"
                        :required=true
                        prop="sourceTables"
                        style="width:65%">
            <el-select placeholder="请选择表名"
                       multiple
                       v-model="updateform.sourceTables">
              <el-option v-for="(item,index) in sourceSchemaTables"
                         :key="index"
                         :label="item"
                         :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="目的端数据源"
                        label-width="120px"
                        :required=true
                        prop="targetConnectionId"
                        style="width:85%">
            <el-select v-model="updateform.targetConnectionId"
                       @change="selectChangedTargetConnection"
                       placeholder="请选择">
              <el-option v-for="(item,index) in connectionNameList"
                         :key="index"
                         :label="item.name"
                         :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="目的端模式名"
                        label-width="120px"
                        :required=true
                        prop="targetSchema"
                        style="width:65%">
            <el-select v-model="updateform.targetSchema"
                       placeholder="请选择">
              <el-option v-for="(item,index) in targetConnectionSchemas"
                         :key="index"
                         :label="item"
                         :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="目的端表名前缀"
                        label-width="120px"
                        style="width:85%">
            <el-input v-model="updateform.tablePrefix"
                      auto-complete="off"></el-input>
          </el-form-item>
          <el-form-item label="数据批次大小"
                        label-width="120px"
                        :required=true
                        prop="batchSize"
                        style="width:85%">
            <el-select v-model="updateform.batchSize">
              <el-option label="1000"
                         value="1000"></el-option>
              <el-option label="5000"
                         value="5000"></el-option>
              <el-option label="10000"
                         value="10000"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer"
             class="dialog-footer">
          <el-button @click="updateFormVisible = false">取 消</el-button>
          <el-button type="primary"
                     @click="handleUpdateSave">确 定</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>

export default {
  data () {
    return {
      loading: true,
      currentPage: 1,
      pageSize: 10,
      totalCount: 2,
      connectionNameList: [],
      tableData: [],
      createform: {
        name: "",
        description: "",
        scheduleMode: "MANUAL",
        cronExpression: "",
        sourceConnectionId: 0,
        sourceSchema: "",
        includeOrExclude: "",
        sourceTables: [],
        tablePrefix: "",
        targetConnectionId: 0,
        targetDropTable: true,
        targetSchema: "",
        batchSize: "10000"
      },
      updateform: {
        id: 0,
        name: "",
        description: "",
        scheduleMode: "MANUAL",
        cronExpression: "",
        sourceConnectionId: 0,
        sourceSchema: "",
        includeOrExclude: "",
        sourceTables: [],
        tablePrefix: "",
        targetConnectionId: 0,
        targetDropTable: true,
        targetSchema: "",
        batchSize: "10000"
      },
      rules: {
        name: [
          {
            required: true,
            message: "任务名称不能为空",
            trigger: "blur"
          }
        ],
        scheduleMode: [
          {
            required: true,
            type: 'string',
            message: "调度方式必须选择",
            trigger: "change"
          }
        ],
        sourceConnectionId: [
          {
            required: true,
            type: 'integer',
            message: "必选选择一个来源端",
            trigger: "change"
          }
        ],
        sourceSchema: [
          {
            required: true,
            type: 'string',
            message: "必选选择一个Schema名",
            trigger: "change"
          }
        ],
        includeOrExclude: [
          {
            required: true,
            type: 'string',
            message: "配置方式必须选择",
            trigger: "change"
          }
        ],
        sourceTables: [
          {
            required: false,
            type: 'array',
            message: "必选选择一个Table名",
            trigger: "change"
          }
        ],
        targetConnectionId: [
          {
            required: true,
            type: 'integer',
            message: "必选选择一个目的端",
            trigger: "change"
          }
        ],
        targetSchema: [
          {
            required: true,
            type: 'string',
            message: "必选选择一个Schema名",
            trigger: "change"
          }
        ],
        batchSize: [
          {
            required: true,
            type: 'string',
            message: "必选选择一个批大小",
            trigger: "change"
          }
        ]
      },
      jobTaskName: "",
      jobTableData: [
        {
          jobId: 0,
          scheduleTime: "",
          startTime: "",
          finishTime: "",
          duration: "",
          jobStatus: "",
          scheduleMode: "",
          errorLog: ""
        }
      ],
      jobHistoryTableData: [
        {
          jobId: 0,
          scheduleTime: "",
          startTime: "",
          finishTime: "",
          duration: "",
          jobStatus: "",
          scheduleMode: "",
          errorLog: ""
        }
      ],
      jobHistoryDrawer: false,
      jobTableVisible: false,
      createFormVisible: false,
      updateFormVisible: false,
      cronPopover: false,

      sourceConnectionSchemas: [],
      sourceSchemaTables: [],
      targetConnectionSchemas: []
    };
  },
  methods: {
    loadData: function () {
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/assignment/list/" + this.currentPage + "/" + this.pageSize
      }).then(
        res => {
          if (0 === res.data.code) {
            this.currentPage = res.data.pagination.page;
            this.pageSize = res.data.pagination.size;
            this.totalCount = res.data.pagination.total;
            this.tableData = res.data.data;
          } else {
            alert("加载任务列表失败:" + res.data.errmsg);
          }
          this.totalCount = this.tableData.length;
        },
        function () {
          console.log("failed");
        }
      );
    },
    loadConnections: function () {
      this.connectionNameList = [];
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/connection/list/name"
      }).then(
        res => {
          if (0 === res.data.code) {
            this.connectionNameList = res.data.data;
          } else {
            alert("加载任务列表失败:" + res.data.errmsg);
          }
        },
        function () {
          console.log("failed");
        }
      );
    },
    boolFormatPublish (row, column) {
      if (row.isPublished === true) {
        return "是";
      } else {
        return "否";
      }
    },
    stringFormatSchedule (row, column) {
      if (row.scheduleMode == "MANUAL") {
        return "手动";
      } else {
        return "系统";
      }
    },
    handleClose: function () { },
    handleCreate: function () {
      this.createFormVisible = true;
      this.createform = {};
    },
    handleDelete: function (index, row) {
      this.$confirm(
        "此操作将此任务ID=" + row.id + "删除么, 是否继续?",
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        this.$http.delete(
          "/dbswitch/admin/api/v1/assignment/delete/" + row.id
        ).then(res => {
          if (0 === res.data.code) {
            this.loadData();
          } else {
            alert("删除任务失败:" + res.data.message);
          }
        });
      });
    },
    handleUpdate: function (index, row) {
      this.$http.get(
        "/dbswitch/admin/api/v1/assignment/detail/id/" + row.id
      ).then(res => {
        if (0 === res.data.code) {
          let detail = res.data.data;
          this.updateform = {
            id: detail.id,
            name: detail.name,
            description: detail.description,
            scheduleMode: detail.scheduleMode,
            cronExpression: detail.cronExpression,
            sourceConnectionId: detail.configuration.sourceConnectionId,
            sourceSchema: detail.configuration.sourceSchema,
            includeOrExclude: detail.configuration.includeOrExclude,
            sourceTables: detail.configuration.sourceTables,
            tablePrefix: detail.configuration.tablePrefix,
            targetConnectionId: detail.configuration.targetConnectionId,
            targetDropTable: detail.configuration.targetDropTable,
            targetSchema: detail.configuration.targetSchema,
            batchSize: detail.configuration.batchSize
          }
          this.selectChangedSourceConnection(this.updateform.sourceConnectionId)
          this.selectUpdateChangedSourceSchema(this.updateform.sourceSchema)
          this.selectChangedTargetConnection(this.updateform.targetConnectionId)
          this.updateFormVisible = true;
        } else {
          alert("查询任务失败," + res.data.message);
        }
      });

    },
    handleCreateSave: function () {
      this.$refs['createform'].validate(valid => {
        if (valid) {
          this.$http({
            method: "POST",
            headers: {
              'Content-Type': 'application/json'
            },
            url: "/dbswitch/admin/api/v1/assignment/create",
            data: JSON.stringify({
              name: this.createform.name,
              description: this.createform.description,
              scheduleMode: this.createform.scheduleMode,
              cronExpression: this.createform.cronExpression,
              config: {
                sourceConnectionId: this.createform.sourceConnectionId,
                sourceSchema: this.createform.sourceSchema,
                includeOrExclude: this.createform.includeOrExclude,
                sourceTables: this.createform.sourceTables,
                targetConnectionId: this.createform.targetConnectionId,
                targetSchema: this.createform.targetSchema,
                tablePrefix: this.createform.tablePrefix,
                targetDropTable: true,
                batchSize: this.createform.batchSize
              }
            })
          }).then(res => {
            if (0 === res.data.code) {
              this.createFormVisible = false;
              this.$message("添加任务成功");
              this.createform = {};
              this.loadData();
            } else {
              alert("添加任务失败:" + res.data.message);
            }
          });
        } else {
          alert("请检查输入");
        }
      });
    },
    handleUpdateSave: function () {
      this.$refs['updateform'].validate(valid => {
        if (valid) {
          this.$http({
            method: "POST",
            headers: {
              'Content-Type': 'application/json'
            },
            url: "/dbswitch/admin/api/v1/assignment/update",
            data: JSON.stringify({
              id: this.updateform.id,
              name: this.updateform.name,
              description: this.updateform.description,
              scheduleMode: this.updateform.scheduleMode,
              cronExpression: this.updateform.cronExpression,
              config: {
                sourceConnectionId: this.updateform.sourceConnectionId,
                sourceSchema: this.updateform.sourceSchema,
                includeOrExclude: this.updateform.includeOrExclude,
                sourceTables: this.updateform.sourceTables,
                targetConnectionId: this.updateform.targetConnectionId,
                targetSchema: this.updateform.targetSchema,
                tablePrefix: this.updateform.tablePrefix,
                targetDropTable: true,
                batchSize: this.updateform.batchSize
              }
            })
          }).then(res => {
            if (0 === res.data.code) {
              this.updateFormVisible = false;
              this.$message("修改任务成功");
              this.updateformform = {};
              this.loadData();
            } else {
              alert("修改任务失败," + res.data.message);
            }
          });
        } else {
          alert("请检查输入");
        }
      });
    },
    handlePublish: function (index, row) {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/deploy?ids=" + row.id,
      }).then(res => {
        if (0 === res.data.code) {
          this.$message("任务发布成功");
          this.loadData();
        } else {
          alert("任务发布失败," + res.data.message);
        }
      });
    },
    handleRunTask: function (index, row) {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/run",
        data: JSON.stringify([row.id])
      }).then(res => {
        if (0 === res.data.code) {
          this.$message("手动启动执行任务成功");
          this.loadData();
        } else {
          alert("手动启动执行任务失败," + res.data.message);
        }
      });
    },
    handleRetireTask: function (index, row) {
      this.$http({
        method: "POST",
        headers: {
          'Content-Type': 'application/json'
        },
        url: "/dbswitch/admin/api/v1/assignment/retire?ids=" + row.id,
      }).then(res => {
        if (0 === res.data.code) {
          this.$message("下线任务成功");
          this.loadData();
        } else {
          alert("下线任务失败," + res.data.message);
        }
      });
    },
    changeCreateCronExpression: function (value) {
      this.createform.cronExpression = value;
    },
    changeUpdateCronExpression: function (value) {
      this.updateform.cronExpression = value;
    },
    selectChangedSourceConnection: function (value) {
      this.sourceConnectionSchemas = [];
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/schemas/get/" + value
      ).then(res => {
        if (0 === res.data.code) {
          this.sourceConnectionSchemas = res.data.data;
        } else {
          this.$message.error("查询来源端数据库的Schema失败," + res.data.message);
        }
      });
    },
    selectCreateChangedSourceSchema: function (value) {
      this.sourceSchemaTables = [];
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/tables/get/" + this.createform.sourceConnectionId + "?schema=" + value
      ).then(res => {
        if (0 === res.data.code) {
          this.sourceSchemaTables = res.data.data;
        } else {
          this.$message.error("查询来源端数据库在制定Schema下的表列表失败," + res.data.message);
        }
      });
    },
    selectUpdateChangedSourceSchema: function (value) {
      this.sourceSchemaTables = [];
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/tables/get/" + this.updateform.sourceConnectionId + "?schema=" + value
      ).then(res => {
        if (0 === res.data.code) {
          this.sourceSchemaTables = res.data.data;
        } else {
          this.$message.error("查询来源端数据库在制定Schema下的表列表失败," + res.data.message);
        }
      });
    },
    selectChangedTargetConnection: function (value) {
      this.targetConnectionSchemas = [];
      this.$http.get(
        "/dbswitch/admin/api/v1/connection/schemas/get/" + value
      ).then(res => {
        if (0 === res.data.code) {
          this.targetConnectionSchemas = res.data.data;
        } else {
          this.$message.error("查询目的端数据库的Schema失败," + res.data.message);
        }
      });
    }
  },
  created () {
    this.loadConnections();
    this.loadData();
  }
};
</script>

<style scoped>
.el-card,
.el-message {
  width: 100%;
  height: 100%;
  overflow: auto;
}

.el-table {
  width: 100%;
  height: 100%;
}

.demo-table-expand {
  font-size: 0;
}

.demo-table-expand label {
  width: 90px;
  color: #99a9bf;
}

.demo-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 50%;
}
</style>
