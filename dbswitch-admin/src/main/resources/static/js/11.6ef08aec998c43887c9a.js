webpackJsonp([11],{"Mc/G":function(t,e){},"mKp/":function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var s={data:function(){return{loading:!0,currentPage:1,pageSize:10,totalCount:0,currentTaskAssignmentPage:1,currentTaskAssignmentPageSize:10,pageTaskAssignments:[],pageTaskAssignmentsTotalCount:0,taskId:"请选择一个任务安排",jobTableData:[],jobScheduleTime:"",isActive:-1,array:[],dialogShowLogVisible:!1,logContent:"",jobId:0,baseId:0,status:0,timer:null}},methods:{loadPageTaskAssignments:function(){var t=this;this.$http({method:"GET",url:"/dbswitch/admin/api/v1/assignment/list/"+this.currentTaskAssignmentPage+"/"+this.currentTaskAssignmentPageSize}).then(function(e){0===e.data.code?(t.pageTaskAssignments=e.data.data,t.pageTaskAssignmentsTotalCount=e.data.pagination.total):e.data.message&&alert("初始化任务安排信息失败:"+e.data.message)})},handleLoadPageTaskAssignments:function(t){this.currentTaskAssignmentPage=t,this.loadPageTaskAssignments()},handleLoadPageTaskAssignmentsSizeChange:function(t){this.currentTaskAssignmentPageSize=t,this.loadPageTaskAssignments()},handleClose:function(){},handleSizeChange:function(t){this.loading=!0,this.pageSize=t,this.loadJobsData()},handleCurrentChange:function(t){this.loading=!0,this.currentPage=t,this.loadJobsData()},loadJobsData:function(){var t=this;this.$http.get("/dbswitch/admin/api/v1/ops/jobs/list/"+this.currentPage+"/"+this.pageSize+"?id="+this.taskId).then(function(e){0===e.data.code?(t.currentPage=e.data.pagination.page,t.pageSize=e.data.pagination.size,t.totalCount=e.data.pagination.total,t.jobTableData=e.data.data):e.data.message&&alert("查询JOB执行历史纪录失败,"+e.data.message)})},handleChooseClick:function(t,e){this.isActive=e,this.taskId=t,this.loadJobsData()},handleCancelJob:function(t){var e=this;this.$http.get("/dbswitch/admin/api/v1/ops/job/cancel?id="+t).then(function(t){if(0===t.data.code){var a=document.getElementById("butten_cancel_id");a.value="已取消",a.disabled=!0,e.$message("停止JOB成功"),e.loadJobsData()}else t.data.message&&alert("JOB停止失败,"+t.data.message)})},handleShowJobLogs:function(t){var e=this;this.dialogShowLogVisible=!0,this.jobId=t,this.$http.get("/dbswitch/admin/api/v1/ops/job/logs/tail?id="+t+"&size=500").then(function(t){if(0===t.data.code){var a=t.data.data.logs;e.status=t.data.data.status,e.baseId=t.data.data.maxId,e.logContent=a.join(""),e.scrollMaxheight(),1===t.data.data.status&&(e.timer=setInterval(function(){e.timerRefreshLogs()},1e3))}else t.data.message&&alert("加载JOB执行日志失败,"+t.data.message)})},timerRefreshLogs:function(){var t=this;this.$http.get("/dbswitch/admin/api/v1/ops/job/logs/next?id="+this.jobId+"&baseId="+this.baseId).then(function(e){if(0===e.data.code){var a=e.data.data.logs;t.logContent=t.logContent+a.join(""),t.baseId=e.data.data.maxId,t.status=e.data.data.status,t.scrollMaxheight(),1!==e.data.data.status&&(clearInterval(t.timer),t.timer=null)}})},scrollMaxheight:function(){this.$nextTick(function(){setTimeout(function(){var t=document.getElementById("log_textarea_id");t.scrollTop=t.scrollHeight},13)})},handleCloseLogDialog:function(){this.dialogShowLogVisible=!1,clearInterval(this.timer),this.timer=null}},created:function(){this.loadPageTaskAssignments()},beforeDestroy:function(){this.timer&&clearInterval(this.timer)}},n={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("el-card",[a("div",{staticClass:"container"},[a("el-card",{staticClass:"box-card"},[a("div",{staticClass:"clearfix",attrs:{slot:"header"},slot:"header"},[a("span",[t._v("任务安排列表")])]),t._v(" "),a("div",{staticClass:"navsBox"},[a("ul",t._l(t.pageTaskAssignments,function(e,s){return a("li",{key:s,class:{active:s==t.isActive},on:{click:function(a){return t.handleChooseClick(e.id,s)}}},[t._v("["+t._s(e.id)+"]"+t._s(e.name))])}),0),t._v(" "),a("el-pagination",{attrs:{small:"",layout:"sizes, prev, pager, next","current-page":t.currentTaskAssignmentPage,"page-sizes":[10,15,20],"page-size":t.currentTaskAssignmentPageSize,total:t.pageTaskAssignmentsTotalCount},on:{"current-change":t.handleLoadPageTaskAssignments,"size-change":t.handleLoadPageTaskAssignmentsSizeChange}})],1)]),t._v(" "),a("div",{staticClass:"contentBox"},[a("el-table",{attrs:{"header-cell-style":{background:"#eef1f6",color:"#606266"},data:t.jobTableData,size:"small",border:""}},[a("template",{slot:"empty"},[a("span",[t._v("记录为空，或者单击左侧任务列表记录来查看作业调度记录")])]),t._v(" "),a("el-table-column",{attrs:{type:"expand"},scopedSlots:t._u([{key:"default",fn:function(e){return[a("el-form",{staticClass:"demo-table-expand",attrs:{"label-position":"left",inline:""}},[a("el-form-item",{attrs:{label:"执行日志:"}},[a("el-button",{attrs:{size:"small",type:"danger"},on:{click:function(a){return t.handleShowJobLogs(e.row.jobId)}}},[t._v("\n                    查看\n                  ")])],1)],1)]}}])}),t._v(" "),a("el-table-column",{attrs:{property:"jobId",label:"ID",width:"60"}}),t._v(" "),a("el-table-column",{attrs:{property:"assignmentId",label:"任务ID",width:"80"}}),t._v(" "),a("el-table-column",{attrs:{property:"scheduleMode",label:"调度方式",width:"80"}}),t._v(" "),a("el-table-column",{attrs:{property:"startTime",label:"开始时间",width:"160"}}),t._v(" "),a("el-table-column",{attrs:{property:"finishTime",label:"结束时间",width:"160"}}),t._v(" "),a("el-table-column",{attrs:{property:"duration",label:"持续时长(s)",width:"100"}}),t._v(" "),a("el-table-column",{attrs:{property:"jobStatus",label:"执行状态",width:"100"}})],2),t._v(" "),a("div",{staticClass:"page",attrs:{align:"right"}},[a("el-pagination",{attrs:{"current-page":t.currentPage,"page-sizes":[5,10,20,40],"page-size":t.pageSize,layout:"total, sizes, prev, pager, next, jumper",total:t.totalCount},on:{"size-change":t.handleSizeChange,"current-change":t.handleCurrentChange}})],1)],1),t._v(" "),a("el-dialog",{attrs:{title:"日志详情",visible:t.dialogShowLogVisible,showClose:!1,"before-close":t.handleClose},on:{"update:visible":function(e){t.dialogShowLogVisible=e}}},[0===t.status?a("el-alert",{attrs:{title:"执行状态：未执行",type:"info",center:"","show-icon":""}}):t._e(),t._v(" "),1===t.status?a("el-alert",{attrs:{title:"执行状态：执行中",type:"success",center:"","show-icon":""}}):t._e(),t._v(" "),2===t.status?a("el-alert",{attrs:{title:"执行状态：执行失败",type:"error",center:"","show-icon":""}}):t._e(),t._v(" "),3===t.status?a("el-alert",{attrs:{title:"执行状态：执行成功",type:"success",center:"","show-icon":""}}):t._e(),t._v(" "),4===t.status?a("el-alert",{attrs:{title:"执行状态：手动取消",type:"warning",center:"","show-icon":""}}):t._e(),t._v(" "),a("el-input",{staticClass:"log_textarea_style",attrs:{type:"textarea",id:"log_textarea_id",rows:20},model:{value:t.logContent,callback:function(e){t.logContent=e},expression:"logContent"}}),t._v(" "),a("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},["1"==t.status?a("el-button",{attrs:{size:"small",id:"butten_cancel_id",type:"danger"},on:{click:function(e){return t.handleCancelJob(t.jobId)}}},[t._v("\n            终 止\n          ")]):t._e(),t._v(" "),a("el-button",{attrs:{size:"small",type:"success"},on:{click:t.handleCloseLogDialog}},[t._v("关 闭")])],1)],1)],1)])],1)},staticRenderFns:[]};var i=a("VU/8")(s,n,!1,function(t){a("Mc/G")},"data-v-5f5cf1a6",null);e.default=i.exports}});
//# sourceMappingURL=11.6ef08aec998c43887c9a.js.map