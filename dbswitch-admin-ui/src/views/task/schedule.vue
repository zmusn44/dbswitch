<template>
  <div>
    <el-card>
      <div class="container">

        <div class="navsBox">
          <ul>
            <li v-for="(item,index) in allTaskAssignments"
                :key="index"
                @click="handleChooseClick(item.id,index)"
                :class="{active:index==isActive}">[{{item.id}}]{{item.name}}</li>
          </ul>
        </div>
        <div class="contentBox">
          <el-table :data="jobTableData"
                    size="small"
                    border>
            <el-table-column type="expand">
              <template slot-scope="props">
                <el-form label-position="left"
                         inline
                         class="demo-table-expand">
                  <el-form-item label="JOB编号:">
                    <span>{{ props.row.jobId }}</span>
                  </el-form-item>
                  <el-form-item label="调度方式:">
                    <span>{{ props.row.scheduleMode }}</span>
                  </el-form-item>
                  <el-form-item label="开始时间:">
                    <span>{{ props.row.startTime }}</span>
                  </el-form-item>
                  <el-form-item label="结束时间:">
                    <span>{{ props.row.finishTime }}</span>
                  </el-form-item>
                  <el-form-item label="执行状态:">
                    <span>{{ props.row.jobStatus }}</span>
                  </el-form-item>
                  <el-form-item label="">
                    <span></span>
                  </el-form-item>
                  <el-form-item label="异常日志:">
                    <el-input type="textarea"
                              style="font-size:12px;width: 700px"
                              :autosize="{ minRows: 2, maxRows: 5}"
                              v-model="props.row.errorLog">
                    </el-input>
                  </el-form-item>
                </el-form>
              </template>
            </el-table-column>
            <el-table-column property="jobId"
                             label="ID"
                             width="60"></el-table-column>
            <el-table-column property="assignmentId"
                             label="任务ID"
                             width="80"></el-table-column>
            <el-table-column property="scheduleMode"
                             label="调度方式"
                             width="80"></el-table-column>
            <el-table-column property="startTime"
                             label="开始时间"
                             width="160"></el-table-column>
            <el-table-column property="finishTime"
                             label="结束时间"
                             width="160"></el-table-column>
            <el-table-column property="duration"
                             label="持续时长(s)"
                             width="100"></el-table-column>
            <el-table-column property="jobStatus"
                             label="执行状态"
                             width="100"></el-table-column>
          </el-table>
          <div class="page"
               align="right">
            <el-pagination @size-change="handleSizeChange"
                           @current-change="handleCurrentChange"
                           :current-page="currentPage"
                           :page-sizes="[5, 10, 20, 40]"
                           :page-size="pageSize"
                           layout="total, sizes, prev, pager, next, jumper"
                           :total="totalCount"></el-pagination>
          </div>
        </div>
      </div>
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
      totalCount: 0,
      allTaskAssignments: [],
      taskId: '请选择一个任务安排',
      jobTableData: [],
      jobScheduleTime: '',
      isActive: -1,
      array: [],
    };
  },
  methods: {
    loadAllTaskAssignments: function () {
      this.$http({
        method: "GET",
        url: "/dbswitch/admin/api/v1/assignment/list/1/10000"
      }).then(res => {
        if (0 === res.data.code) {
          this.allTaskAssignments = res.data.data;
        } else {
          alert("初始化任务安排信息失败:" + res.data.errmsg);
        }
      }
      );
    },
    handleClose: function () { },
    handleSizeChange: function (pageSize) {
      this.loading = true;
      this.pageSize = pageSize;
      this.loadJobsData();
    },
    handleCurrentChange: function (currentPage) {
      this.loading = true;
      this.currentPage = currentPage;
      this.loadJobsData();
    },
    loadJobsData: function () {
      this.$http.get(
        "/dbswitch/admin/api/v1/ops/jobs/list/" + this.currentPage + "/" + this.pageSize + "?id=" + this.taskId
      ).then(res => {
        if (0 === res.data.code) {
          this.currentPage = res.data.pagination.page;
          this.pageSize = res.data.pagination.size;
          this.totalCount = res.data.pagination.total;
          this.jobTableData = res.data.data;
        } else {
          alert("查询JOB执行历史纪录失败," + res.data.message);
        }
      });
    },
    handleChooseClick: function (taskId, index) {
      this.isActive = index;
      this.taskId = taskId;
      this.loadJobsData();
    },
  },
  created () {
    this.loadAllTaskAssignments();
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
  border-collapse: collapse;
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

.filter {
  margin: 10px;
}

.container {
  display: flex;
  height: 600px;
}

.container .navsBox {
  background: #f2f2f2;
}

.container .navsBox ul {
  margin: 0;
  padding-left: 10px;
}

.container .navsBox ul li {
  list-style: none;
  cursor: pointer; /*鼠标悬停变小手*/
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
  width: 250px;
}

.container .navsBox .active {
  background: #bcbcbe6e;
  color: rgb(46, 28, 88);
}

.container .contentBox {
  padding: 10px;
}
</style>
