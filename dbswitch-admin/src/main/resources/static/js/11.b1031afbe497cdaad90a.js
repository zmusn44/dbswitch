webpackJsonp([11],{"4H5z":function(t,a){},"mKp/":function(t,a,e){"use strict";Object.defineProperty(a,"__esModule",{value:!0});var s={data:function(){return{loading:!0,currentPage:1,pageSize:10,totalCount:0,allTaskAssignments:[],taskId:"请选择一个任务安排",jobTableData:[],jobScheduleTime:"",isActive:-1,array:[]}},methods:{loadAllTaskAssignments:function(){var t=this;this.$http({method:"GET",url:"/dbswitch/admin/api/v1/assignment/list/1/10000"}).then(function(a){0===a.data.code?t.allTaskAssignments=a.data.data:alert("初始化任务安排信息失败:"+a.data.errmsg)})},handleClose:function(){},handleSizeChange:function(t){this.loading=!0,this.pageSize=t,this.loadJobsData()},handleCurrentChange:function(t){this.loading=!0,this.currentPage=t,this.loadJobsData()},loadJobsData:function(){var t=this;this.$http.get("/dbswitch/admin/api/v1/ops/jobs/list/"+this.currentPage+"/"+this.pageSize+"?id="+this.taskId).then(function(a){0===a.data.code?(t.currentPage=a.data.pagination.page,t.pageSize=a.data.pagination.size,t.totalCount=a.data.pagination.total,t.jobTableData=a.data.data):alert("查询JOB执行历史纪录失败,"+a.data.message)})},handleChooseClick:function(t,a){this.isActive=a,this.taskId=t,this.loadJobsData()},handleCancelJob:function(t){var a=this;this.$http.get("/dbswitch/admin/api/v1/ops/job/cancel?id="+t).then(function(t){0===t.data.code?(a.$message("停止JOB成功"),a.loadJobsData()):alert("JOB停止失败,"+t.data.message)})}},created:function(){this.loadAllTaskAssignments()}},n={render:function(){var t=this,a=t.$createElement,e=t._self._c||a;return e("div",[e("el-card",[e("div",{staticClass:"container"},[e("div",{staticClass:"navsBox"},[e("ul",t._l(t.allTaskAssignments,function(a,s){return e("li",{key:s,class:{active:s==t.isActive},on:{click:function(e){return t.handleChooseClick(a.id,s)}}},[t._v("["+t._s(a.id)+"]"+t._s(a.name))])}),0)]),t._v(" "),e("div",{staticClass:"contentBox"},[e("el-table",{attrs:{data:t.jobTableData,size:"small",border:""}},[e("el-table-column",{attrs:{type:"expand"},scopedSlots:t._u([{key:"default",fn:function(a){return[e("el-form",{staticClass:"demo-table-expand",attrs:{"label-position":"left",inline:""}},[e("el-form-item",{attrs:{label:"JOB编号:"}},[e("span",[t._v(t._s(a.row.jobId))])]),t._v(" "),e("el-form-item",{attrs:{label:"调度方式:"}},[e("span",[t._v(t._s(a.row.scheduleMode))])]),t._v(" "),e("el-form-item",{attrs:{label:"开始时间:"}},[e("span",[t._v(t._s(a.row.startTime))])]),t._v(" "),e("el-form-item",{attrs:{label:"结束时间:"}},[e("span",[t._v(t._s(a.row.finishTime))])]),t._v(" "),e("el-form-item",{attrs:{label:"执行状态:"}},[e("span",[t._v(t._s(a.row.jobStatus))])]),t._v(" "),e("el-form-item",{attrs:{label:"操作:"}},["1"==a.row.status?e("el-button",{attrs:{size:"small",type:"danger"},on:{click:function(e){return t.handleCancelJob(a.row.jobId)}}},[t._v("\n                    停止\n                  ")]):t._e()],1),t._v(" "),e("el-form-item",{attrs:{label:"异常日志:"}},[e("el-input",{staticStyle:{"font-size":"12px",width:"700px"},attrs:{type:"textarea",autosize:{minRows:2,maxRows:5}},model:{value:a.row.errorLog,callback:function(e){t.$set(a.row,"errorLog",e)},expression:"props.row.errorLog"}})],1)],1)]}}])}),t._v(" "),e("el-table-column",{attrs:{property:"jobId",label:"ID",width:"60"}}),t._v(" "),e("el-table-column",{attrs:{property:"assignmentId",label:"任务ID",width:"80"}}),t._v(" "),e("el-table-column",{attrs:{property:"scheduleMode",label:"调度方式",width:"80"}}),t._v(" "),e("el-table-column",{attrs:{property:"startTime",label:"开始时间",width:"160"}}),t._v(" "),e("el-table-column",{attrs:{property:"finishTime",label:"结束时间",width:"160"}}),t._v(" "),e("el-table-column",{attrs:{property:"duration",label:"持续时长(s)",width:"100"}}),t._v(" "),e("el-table-column",{attrs:{property:"jobStatus",label:"执行状态",width:"100"}})],1),t._v(" "),e("div",{staticClass:"page",attrs:{align:"right"}},[e("el-pagination",{attrs:{"current-page":t.currentPage,"page-sizes":[5,10,20,40],"page-size":t.pageSize,layout:"total, sizes, prev, pager, next, jumper",total:t.totalCount},on:{"size-change":t.handleSizeChange,"current-change":t.handleCurrentChange}})],1)],1)])])],1)},staticRenderFns:[]};var l=e("VU/8")(s,n,!1,function(t){e("4H5z")},"data-v-439c3c72",null);a.default=l.exports}});
//# sourceMappingURL=11.b1031afbe497cdaad90a.js.map