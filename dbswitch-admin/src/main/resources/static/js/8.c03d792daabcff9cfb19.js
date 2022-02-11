webpackJsonp([8],{D0I9:function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var o=a("mvHQ"),r=a.n(o),n={data:function(){return{loading:!0,currentPage:1,pageSize:10,totalCount:2,connectionNameList:[],tableData:[],createform:{name:"",description:"",scheduleMode:"MANUAL",cronExpression:"",sourceConnectionId:0,sourceSchema:"",includeOrExclude:"",sourceTables:[],tablePrefix:"",targetConnectionId:0,targetDropTable:!0,targetSchema:"",batchSize:5e3},updateform:{id:0,name:"",description:"",scheduleMode:"MANUAL",cronExpression:"",sourceConnectionId:0,sourceSchema:"",includeOrExclude:"",sourceTables:[],tablePrefix:"",targetConnectionId:0,targetDropTable:!0,targetSchema:"",batchSize:5e3},rules:{name:[{required:!0,message:"任务名称不能为空",trigger:"blur"}],scheduleMode:[{required:!0,type:"string",message:"调度方式必须选择",trigger:"change"}],sourceConnectionId:[{required:!0,type:"integer",message:"必选选择一个来源端",trigger:"change"}],sourceSchema:[{required:!0,type:"string",message:"必选选择一个Schema名",trigger:"change"}],includeOrExclude:[{required:!0,type:"string",message:"配置方式必须选择",trigger:"change"}],sourceTables:[{required:!1,type:"array",message:"必选选择一个Table名",trigger:"change"}],targetConnectionId:[{required:!0,type:"integer",message:"必选选择一个目的端",trigger:"change"}],targetSchema:[{required:!0,type:"string",message:"必选选择一个Schema名",trigger:"change"}],batchSize:[{required:!0,type:"integer",message:"必选选择一个批大小",trigger:"change"}]},jobTaskName:"",jobTableData:[{jobId:0,scheduleTime:"",startTime:"",finishTime:"",duration:"",jobStatus:"",scheduleMode:"",errorLog:""}],jobHistoryTableData:[{jobId:0,scheduleTime:"",startTime:"",finishTime:"",duration:"",jobStatus:"",scheduleMode:"",errorLog:""}],jobHistoryDrawer:!1,jobTableVisible:!1,createFormVisible:!1,updateFormVisible:!1,cronPopover:!1,sourceConnectionSchemas:[],sourceSchemaTables:[],targetConnectionSchemas:[]}},methods:{loadData:function(){var e=this;this.$http({method:"GET",url:"/dbswitch/admin/api/v1/assignment/list/"+this.currentPage+"/"+this.pageSize}).then(function(t){0===t.data.code?(e.currentPage=t.data.pagination.page,e.pageSize=t.data.pagination.size,e.totalCount=t.data.pagination.total,e.tableData=t.data.data):alert("加载任务列表失败:"+t.data.errmsg),e.totalCount=e.tableData.length},function(){console.log("failed")})},loadConnections:function(){var e=this;this.connectionNameList=[],this.$http({method:"GET",url:"/dbswitch/admin/api/v1/connection/list/name"}).then(function(t){0===t.data.code?e.connectionNameList=t.data.data:alert("加载任务列表失败:"+t.data.errmsg)},function(){console.log("failed")})},boolFormatPublish:function(e,t){return!0===e.isPublished?"是":"否"},stringFormatSchedule:function(e,t){return"MANUAL"==e.scheduleMode?"手动":"系统"},handleClose:function(){},handleCreate:function(){this.createFormVisible=!0,this.createform={}},handleDelete:function(e,t){var a=this;this.$confirm("此操作将此任务ID="+t.id+"删除么, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(function(){a.$http.delete("/dbswitch/admin/api/v1/assignment/delete/"+t.id).then(function(e){0===e.data.code?a.loadData():alert("删除任务失败:"+e.data.message)})})},handleUpdate:function(e,t){var a=this;this.$http.get("/dbswitch/admin/api/v1/assignment/detail/id/"+t.id).then(function(e){if(0===e.data.code){var t=e.data.data;a.updateform={id:t.id,name:t.name,description:t.description,scheduleMode:t.scheduleMode,cronExpression:t.cronExpression,sourceConnectionId:t.configuration.sourceConnectionId,sourceSchema:t.configuration.sourceSchema,includeOrExclude:t.configuration.includeOrExclude,sourceTables:t.configuration.sourceTables,tablePrefix:t.configuration.tablePrefix,targetConnectionId:t.configuration.targetConnectionId,targetDropTable:t.configuration.targetDropTable,targetSchema:t.configuration.targetSchema,batchSize:t.configuration.batchSize},a.selectChangedSourceConnection(a.updateform.sourceConnectionId),a.selectUpdateChangedSourceSchema(a.updateform.sourceSchema),a.selectChangedTargetConnection(a.updateform.targetConnectionId),a.updateFormVisible=!0}else alert("查询任务失败,"+e.data.message)})},handleCreateSave:function(){var e=this;this.$refs.createform.validate(function(t){t?e.$http({method:"POST",headers:{"Content-Type":"application/json"},url:"/dbswitch/admin/api/v1/assignment/create",data:r()({name:e.createform.name,description:e.createform.description,scheduleMode:e.createform.scheduleMode,cronExpression:e.createform.cronExpression,config:{sourceConnectionId:e.createform.sourceConnectionId,sourceSchema:e.createform.sourceSchema,includeOrExclude:e.createform.includeOrExclude,sourceTables:e.createform.sourceTables,targetConnectionId:e.createform.targetConnectionId,targetSchema:e.createform.targetSchema,tablePrefix:e.createform.tablePrefix,targetDropTable:!0,batchSize:e.createform.batchSize}})}).then(function(t){0===t.data.code?(e.createFormVisible=!1,e.$message("添加任务成功"),e.createform={},e.loadData()):alert("添加任务失败:"+t.data.message)}):alert("请检查输入")})},handleUpdateSave:function(){var e=this;this.$refs.updateform.validate(function(t){t?e.$http({method:"POST",headers:{"Content-Type":"application/json"},url:"/dbswitch/admin/api/v1/assignment/update",data:r()({id:e.updateform.id,name:e.updateform.name,description:e.updateform.description,scheduleMode:e.updateform.scheduleMode,cronExpression:e.updateform.cronExpression,config:{sourceConnectionId:e.updateform.sourceConnectionId,sourceSchema:e.updateform.sourceSchema,includeOrExclude:e.updateform.includeOrExclude,sourceTables:e.updateform.sourceTables,targetConnectionId:e.updateform.targetConnectionId,targetSchema:e.updateform.targetSchema,tablePrefix:e.updateform.tablePrefix,targetDropTable:!0,batchSize:e.updateform.batchSize}})}).then(function(t){0===t.data.code?(e.updateFormVisible=!1,e.$message("修改任务成功"),e.updateformform={},e.loadData()):alert("修改任务失败,"+t.data.message)}):alert("请检查输入")})},handlePublish:function(e,t){var a=this;this.$http({method:"POST",headers:{"Content-Type":"application/json"},url:"/dbswitch/admin/api/v1/assignment/deploy?ids="+t.id}).then(function(e){0===e.data.code?(a.$message("任务发布成功"),a.loadData()):alert("任务发布失败,"+e.data.message)})},handleRunTask:function(e,t){var a=this;this.$http({method:"POST",headers:{"Content-Type":"application/json"},url:"/dbswitch/admin/api/v1/assignment/run",data:r()([t.id])}).then(function(e){0===e.data.code?(a.$message("手动启动执行任务成功"),a.loadData()):alert("手动启动执行任务失败,"+e.data.message)})},handleRetireTask:function(e,t){var a=this;this.$http({method:"POST",headers:{"Content-Type":"application/json"},url:"/dbswitch/admin/api/v1/assignment/retire?ids="+t.id}).then(function(e){0===e.data.code?(a.$message("下线任务成功"),a.loadData()):alert("下线任务失败,"+e.data.message)})},changeCreateCronExpression:function(e){this.createform.cronExpression=e},changeUpdateCronExpression:function(e){this.updateform.cronExpression=e},selectChangedSourceConnection:function(e){var t=this;this.sourceConnectionSchemas=[],this.$http.get("/dbswitch/admin/api/v1/connection/schemas/get/"+e).then(function(e){0===e.data.code?t.sourceConnectionSchemas=e.data.data:t.$message.error("查询来源端数据库的Schema失败,"+e.data.message)})},selectCreateChangedSourceSchema:function(e){var t=this;this.sourceSchemaTables=[],this.$http.get("/dbswitch/admin/api/v1/connection/tables/get/"+this.createform.sourceConnectionId+"?schema="+e).then(function(e){0===e.data.code?t.sourceSchemaTables=e.data.data:t.$message.error("查询来源端数据库在制定Schema下的表列表失败,"+e.data.message)})},selectUpdateChangedSourceSchema:function(e){var t=this;this.sourceSchemaTables=[],this.$http.get("/dbswitch/admin/api/v1/connection/tables/get/"+this.updateform.sourceConnectionId+"?schema="+e).then(function(e){0===e.data.code?t.sourceSchemaTables=e.data.data:t.$message.error("查询来源端数据库在制定Schema下的表列表失败,"+e.data.message)})},selectChangedTargetConnection:function(e){var t=this;this.targetConnectionSchemas=[],this.$http.get("/dbswitch/admin/api/v1/connection/schemas/get/"+e).then(function(e){0===e.data.code?t.targetConnectionSchemas=e.data.data:t.$message.error("查询目的端数据库的Schema失败,"+e.data.message)})}},created:function(){this.loadConnections(),this.loadData()}},l={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",[a("el-card",[a("div",{staticStyle:{margin:"10px 5px"},attrs:{align:"right",width:"65%"}},[a("el-button",{attrs:{type:"primary",icon:"el-icon-document-add"},on:{click:e.handleCreate}},[e._v("添加")])],1),e._v(" "),a("el-table",{attrs:{data:e.tableData,size:"small",border:""}},[a("el-table-column",{attrs:{prop:"id",label:"编号","min-width":"8%"}}),e._v(" "),a("el-table-column",{attrs:{prop:"name",label:"名称","show-overflow-tooltip":"","min-width":"30%"}}),e._v(" "),a("el-table-column",{attrs:{prop:"scheduleMode",label:"调度",formatter:e.stringFormatSchedule,"min-width":"8%"}}),e._v(" "),a("el-table-column",{attrs:{prop:"isPublished",label:"已发布",formatter:e.boolFormatPublish,"show-overflow-tooltip":!0,"min-width":"8%"}}),e._v(" "),a("el-table-column",{attrs:{prop:"createTime",label:"时间","min-width":"15%"}}),e._v(" "),a("el-table-column",{attrs:{label:"操作","min-width":"40%"},scopedSlots:e._u([{key:"default",fn:function(t){return[!1===t.row.isPublished?a("el-button",{attrs:{size:"small",type:"success"},on:{click:function(a){return e.handlePublish(t.$index,t.row)}}},[a("i",{staticClass:"el-icon-timer el-icon--right"}),e._v("发布")]):e._e(),e._v(" "),!0===t.row.isPublished?a("el-button",{attrs:{size:"small",type:"warning"},on:{click:function(a){return e.handleRetireTask(t.$index,t.row)}}},[a("i",{staticClass:"el-icon-delete-location el-icon--right"}),e._v("下线")]):e._e(),e._v(" "),!0===t.row.isPublished?a("el-button",{attrs:{size:"small",type:"danger"},on:{click:function(a){return e.handleRunTask(t.$index,t.row)}}},[a("i",{staticClass:"el-icon-video-play el-icon--right"}),e._v("执行")]):e._e(),e._v(" "),a("el-dropdown",{attrs:{size:"small","split-button":"",type:"primary"}},[e._v("\n            更多\n            "),a("el-dropdown-menu",{attrs:{slot:"dropdown"},slot:"dropdown"},[a("el-dropdown-item",{nativeOn:{click:function(a){return a.preventDefault(),e.handleUpdate(t.$index,t.row)}}},[e._v("修改")]),e._v(" "),a("el-dropdown-item",{nativeOn:{click:function(a){return a.preventDefault(),e.handleDelete(t.$index,t.row)}}},[e._v("删除")])],1)],1)]}}])})],1),e._v(" "),a("div",{staticClass:"page",attrs:{align:"right"}},[a("el-pagination",{attrs:{"current-page":e.currentPage,"page-sizes":[5,10,20,40],"page-size":e.pageSize,layout:"total, sizes, prev, pager, next, jumper",total:e.totalCount}})],1),e._v(" "),a("el-dialog",{attrs:{title:"添加任务",visible:e.createFormVisible,showClose:!1,"before-close":e.handleClose},on:{"update:visible":function(t){e.createFormVisible=t}}},[a("el-form",{ref:"createform",attrs:{model:e.createform,size:"mini","status-icon":"",rules:e.rules}},[a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"名称","label-width":"120px",required:!0,prop:"name"}},[a("el-input",{attrs:{"auto-complete":"off"},model:{value:e.createform.name,callback:function(t){e.$set(e.createform,"name",t)},expression:"createform.name"}})],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"描述","label-width":"120px",prop:"description"}},[a("el-input",{attrs:{type:"textarea",rows:3,"auto-complete":"off"},model:{value:e.createform.description,callback:function(t){e.$set(e.createform,"description",t)},expression:"createform.description"}})],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"调度方式","label-width":"120px",required:!0,prop:"scheduleMode"}},[a("el-select",{model:{value:e.createform.scheduleMode,callback:function(t){e.$set(e.createform,"scheduleMode",t)},expression:"createform.scheduleMode"}},[a("el-option",{attrs:{label:"手动调度",value:"MANUAL"}}),e._v(" "),a("el-option",{attrs:{label:"系统调度",value:"SYSTEM_SCHEDULED"}})],1)],1),e._v(" "),"SYSTEM_SCHEDULED"==e.createform.scheduleMode?a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"Cron表达式","label-width":"120px"}},[a("el-col",{attrs:{span:8}},[a("el-popover",{model:{value:e.cronPopover,callback:function(t){e.cronPopover=t},expression:"cronPopover"}},[a("vueCron",{attrs:{i18n:"cn"},on:{change:e.changeCreateCronExpression,close:function(t){e.cronPopover=!1}}}),e._v(" "),a("el-input",{attrs:{slot:"reference",disabled:"true",placeholder:"定时策略"},on:{click:function(t){e.cronPopover=!0}},slot:"reference",model:{value:e.createform.cronExpression,callback:function(t){e.$set(e.createform,"cronExpression",t)},expression:"createform.cronExpression"}})],1)],1)],1):e._e(),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"来源端数据源","label-width":"120px",required:!0,prop:"sourceConnectionId"}},[a("el-select",{attrs:{placeholder:"请选择"},on:{change:e.selectChangedSourceConnection},model:{value:e.createform.sourceConnectionId,callback:function(t){e.$set(e.createform,"sourceConnectionId",t)},expression:"createform.sourceConnectionId"}},e._l(e.connectionNameList,function(e,t){return a("el-option",{key:t,attrs:{label:e.name,value:e.id}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"65%"},attrs:{label:"源端模式名","label-width":"120px",required:!0,prop:"sourceSchema"}},[a("el-select",{attrs:{placeholder:"请选择"},on:{change:e.selectCreateChangedSourceSchema},model:{value:e.createform.sourceSchema,callback:function(t){e.$set(e.createform,"sourceSchema",t)},expression:"createform.sourceSchema"}},e._l(e.sourceConnectionSchemas,function(e,t){return a("el-option",{key:t,attrs:{label:e,value:e}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"65%"},attrs:{label:"配置方式","label-width":"120px",required:!0,prop:"includeOrExclude"}},[a("el-select",{attrs:{placeholder:"请选择配置方式"},model:{value:e.createform.includeOrExclude,callback:function(t){e.$set(e.createform,"includeOrExclude",t)},expression:"createform.includeOrExclude"}},[a("el-option",{attrs:{label:"包含表",value:"INCLUDE"}}),e._v(" "),a("el-option",{attrs:{label:"排除表",value:"EXCLUDE"}})],1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"表名配置","label-width":"120px",required:!1,prop:"sourceTables"}},[a("el-tooltip",{attrs:{placement:"top"}},[a("div",{attrs:{slot:"content"},slot:"content"},[e._v("\n              当为包含表时，选择所要精确包含的表名，如果不选则代表选择所有；当为排除表时，选择索要精确排除的表名。\n            ")]),e._v(" "),a("i",{staticClass:"el-icon-question"})]),e._v(" "),a("el-select",{attrs:{placeholder:"请选择表名",multiple:""},model:{value:e.createform.sourceTables,callback:function(t){e.$set(e.createform,"sourceTables",t)},expression:"createform.sourceTables"}},e._l(e.sourceSchemaTables,function(e,t){return a("el-option",{key:t,attrs:{label:e,value:e}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"目的端数据源","label-width":"120px",required:!0,prop:"targetConnectionId"}},[a("el-select",{attrs:{placeholder:"请选择"},on:{change:e.selectChangedTargetConnection},model:{value:e.createform.targetConnectionId,callback:function(t){e.$set(e.createform,"targetConnectionId",t)},expression:"createform.targetConnectionId"}},e._l(e.connectionNameList,function(e,t){return a("el-option",{key:t,attrs:{label:e.name,value:e.id}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"65%"},attrs:{label:"目的端模式名","label-width":"120px",required:!0,prop:"targetSchema"}},[a("el-select",{attrs:{placeholder:"请选择"},model:{value:e.createform.targetSchema,callback:function(t){e.$set(e.createform,"targetSchema",t)},expression:"createform.targetSchema"}},e._l(e.targetConnectionSchemas,function(e,t){return a("el-option",{key:t,attrs:{label:e,value:e}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"目的端表名前缀","label-width":"120px"}},[a("el-input",{attrs:{"auto-complete":"off"},model:{value:e.createform.tablePrefix,callback:function(t){e.$set(e.createform,"tablePrefix",t)},expression:"createform.tablePrefix"}})],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"数据批次大小","label-width":"120px",required:!0,prop:"batchSize"}},[a("el-select",{model:{value:e.createform.batchSize,callback:function(t){e.$set(e.createform,"batchSize",t)},expression:"createform.batchSize"}},[a("el-option",{attrs:{label:"1000",value:1e3}}),e._v(" "),a("el-option",{attrs:{label:"5000",value:5e3}}),e._v(" "),a("el-option",{attrs:{label:"10000",value:1e4}})],1)],1)],1),e._v(" "),a("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[a("el-button",{on:{click:function(t){e.createFormVisible=!1}}},[e._v("取 消")]),e._v(" "),a("el-button",{attrs:{type:"primary"},on:{click:e.handleCreateSave}},[e._v("确 定")])],1)],1),e._v(" "),a("el-dialog",{attrs:{title:"修改任务",visible:e.updateFormVisible,showClose:!1,"before-close":e.handleClose},on:{"update:visible":function(t){e.updateFormVisible=t}}},[a("el-form",{ref:"updateform",attrs:{model:e.updateform,size:"mini","status-icon":"",rules:e.rules}},[a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"名称","label-width":"120px",required:!0,prop:"name"}},[a("el-input",{attrs:{"auto-complete":"off"},model:{value:e.updateform.name,callback:function(t){e.$set(e.updateform,"name",t)},expression:"updateform.name"}})],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"描述","label-width":"120px"}},[a("el-input",{attrs:{type:"textarea",rows:3,"auto-complete":"off"},model:{value:e.updateform.description,callback:function(t){e.$set(e.updateform,"description",t)},expression:"updateform.description"}})],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"调度方式","label-width":"120px",required:!0,prop:"scheduleMode"}},[a("el-select",{model:{value:e.updateform.scheduleMode,callback:function(t){e.$set(e.updateform,"scheduleMode",t)},expression:"updateform.scheduleMode"}},[a("el-option",{attrs:{label:"手动调度",value:"MANUAL"}}),e._v(" "),a("el-option",{attrs:{label:"系统调度",value:"SYSTEM_SCHEDULED"}})],1)],1),e._v(" "),"SYSTEM_SCHEDULED"==e.updateform.scheduleMode?a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"Cron表达式","label-width":"120px"}},[a("el-col",{attrs:{span:8}},[a("el-popover",{model:{value:e.cronPopover,callback:function(t){e.cronPopover=t},expression:"cronPopover"}},[a("vueCron",{attrs:{i18n:"cn"},on:{change:e.changeUpdateCronExpression,close:function(t){e.cronPopover=!1}}}),e._v(" "),a("el-input",{attrs:{slot:"reference",disabled:"true",placeholder:"定时策略"},on:{click:function(t){e.cronPopover=!0}},slot:"reference",model:{value:e.updateform.cronExpression,callback:function(t){e.$set(e.updateform,"cronExpression",t)},expression:"updateform.cronExpression"}})],1)],1)],1):e._e(),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"来源端数据源","label-width":"120px",required:!0,prop:"sourceConnectionId"}},[a("el-select",{attrs:{placeholder:"请选择"},on:{change:e.selectChangedSourceConnection},model:{value:e.updateform.sourceConnectionId,callback:function(t){e.$set(e.updateform,"sourceConnectionId",t)},expression:"updateform.sourceConnectionId"}},e._l(e.connectionNameList,function(e,t){return a("el-option",{key:t,attrs:{label:e.name,value:e.id}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"65%"},attrs:{label:"源端模式名","label-width":"120px",required:!0,prop:"sourceSchema"}},[a("el-select",{attrs:{placeholder:"请选择"},on:{change:e.selectUpdateChangedSourceSchema},model:{value:e.updateform.sourceSchema,callback:function(t){e.$set(e.updateform,"sourceSchema",t)},expression:"updateform.sourceSchema"}},e._l(e.sourceConnectionSchemas,function(e,t){return a("el-option",{key:t,attrs:{label:e,value:e}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"65%"},attrs:{label:"配置方式","label-width":"120px",required:!0,prop:"includeOrExclude"}},[a("el-select",{attrs:{placeholder:"请选择配置方式"},model:{value:e.updateform.includeOrExclude,callback:function(t){e.$set(e.updateform,"includeOrExclude",t)},expression:"updateform.includeOrExclude"}},[a("el-option",{attrs:{label:"包含表",value:"INCLUDE"}}),e._v(" "),a("el-option",{attrs:{label:"排除表",value:"EXCLUDE"}})],1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"表名配置","label-width":"120px",required:!1,prop:"sourceTables"}},[a("el-tooltip",{attrs:{placement:"top"}},[a("div",{attrs:{slot:"content"},slot:"content"},[e._v("\n              当为包含表时，选择所要精确包含的表名，如果不选则代表选择所有；当为排除表时，选择索要精确排除的表名。\n            ")]),e._v(" "),a("i",{staticClass:"el-icon-question"})]),e._v(" "),a("el-select",{attrs:{placeholder:"请选择表名",multiple:""},model:{value:e.updateform.sourceTables,callback:function(t){e.$set(e.updateform,"sourceTables",t)},expression:"updateform.sourceTables"}},e._l(e.sourceSchemaTables,function(e,t){return a("el-option",{key:t,attrs:{label:e,value:e}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"目的端数据源","label-width":"120px",required:!0,prop:"targetConnectionId"}},[a("el-select",{attrs:{placeholder:"请选择"},on:{change:e.selectChangedTargetConnection},model:{value:e.updateform.targetConnectionId,callback:function(t){e.$set(e.updateform,"targetConnectionId",t)},expression:"updateform.targetConnectionId"}},e._l(e.connectionNameList,function(e,t){return a("el-option",{key:t,attrs:{label:e.name,value:e.id}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"65%"},attrs:{label:"目的端模式名","label-width":"120px",required:!0,prop:"targetSchema"}},[a("el-select",{attrs:{placeholder:"请选择"},model:{value:e.updateform.targetSchema,callback:function(t){e.$set(e.updateform,"targetSchema",t)},expression:"updateform.targetSchema"}},e._l(e.targetConnectionSchemas,function(e,t){return a("el-option",{key:t,attrs:{label:e,value:e}})}),1)],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"目的端表名前缀","label-width":"120px"}},[a("el-input",{attrs:{"auto-complete":"off"},model:{value:e.updateform.tablePrefix,callback:function(t){e.$set(e.updateform,"tablePrefix",t)},expression:"updateform.tablePrefix"}})],1),e._v(" "),a("el-form-item",{staticStyle:{width:"85%"},attrs:{label:"数据批次大小","label-width":"120px",required:!0,prop:"batchSize"}},[a("el-select",{model:{value:e.updateform.batchSize,callback:function(t){e.$set(e.updateform,"batchSize",t)},expression:"updateform.batchSize"}},[a("el-option",{attrs:{label:"1000",value:1e3}}),e._v(" "),a("el-option",{attrs:{label:"5000",value:5e3}}),e._v(" "),a("el-option",{attrs:{label:"10000",value:1e4}})],1)],1)],1),e._v(" "),a("div",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[a("el-button",{on:{click:function(t){e.updateFormVisible=!1}}},[e._v("取 消")]),e._v(" "),a("el-button",{attrs:{type:"primary"},on:{click:e.handleUpdateSave}},[e._v("确 定")])],1)],1)],1)],1)},staticRenderFns:[]};var i=a("VU/8")(n,l,!1,function(e){a("arLB")},"data-v-641874cc",null);t.default=i.exports},arLB:function(e,t){}});
//# sourceMappingURL=8.c03d792daabcff9cfb19.js.map