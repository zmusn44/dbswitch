webpackJsonp([9],{"0eSS":function(e,t,a){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var l={data:function(){return{loading:!0,lists:[],currentPage:1,pageSize:10,totalCount:0}},methods:{loadData:function(){var e=this;this.$http.get("/dbswitch/admin/api/v1/syslog/list/2/"+this.currentPage+"/"+this.pageSize).then(function(t){e.loading=!1,0===t.data.code?(e.currentPage=t.data.pagination.page,e.pageSize=t.data.pagination.size,e.totalCount=t.data.pagination.total,e.lists=t.data.data):alert("加载数据失败:"+t.data.message)},function(t){e.$message({showClose:!0,message:"数据加载错误",type:"error"})})},boolFormat:function(e,t){return!0===e.failed?"是":"否"},handleSizeChange:function(e){this.loading=!0,this.pageSize=e,this.loadData()},handleCurrentChange:function(e){this.loading=!0,this.currentPage=e,this.loadData()},handleDetail:function(e,t){this.$message({showClose:!0,message:"查看日志详情"+e+" "+t,type:"info"})}},created:function(){this.loadData()}},o={render:function(){var e=this,t=e.$createElement,a=e._self._c||t;return a("div",{staticStyle:{"margin-top":"15px"}},[a("el-table",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],staticStyle:{width:"100%"},attrs:{"header-cell-style":{background:"#eef1f6",color:"#606266"},"element-loading-text":"拼命加载中","element-loading-spinner":"el-icon-loading","element-loading-background":"rgba(0, 0, 0, 0.8)",data:e.lists,stripe:"",size:"small",border:""}},[a("el-table-column",{attrs:{type:"expand"},scopedSlots:e._u([{key:"default",fn:function(t){return[a("el-form",{staticClass:"demo-table-expand",attrs:{"label-position":"left",inline:""}},[a("el-form-item",{attrs:{label:"日志编号:"}},[a("span",[e._v(e._s(t.row.id))])]),e._v(" "),a("el-form-item",{attrs:{label:"日志时间:"}},[a("span",[e._v(e._s(t.row.createTime))])]),e._v(" "),a("el-form-item",{attrs:{label:"操作用户:"}},[a("span",[e._v(e._s(t.row.username))])]),e._v(" "),a("el-form-item",{attrs:{label:"请求IP地址:"}},[a("span",[e._v(e._s(t.row.ipAddress))])]),e._v(" "),a("el-form-item",{attrs:{label:"操作模块:"}},[a("span",[e._v(e._s(t.row.moduleName))])]),e._v(" "),a("el-form-item",{attrs:{label:"操作描述:"}},[a("span",[e._v(e._s(t.row.content))])]),e._v(" "),a("el-form-item",{attrs:{label:"处理耗时(ms):"}},[a("span",[e._v(e._s(t.row.elapseSeconds))])]),e._v(" "),a("el-form-item",{attrs:{label:"请求路径:"}},[a("span",[e._v(e._s(t.row.urlPath))])]),e._v(" "),a("el-form-item",{attrs:{label:"异常状态:"}},[a("span",[e._v(e._s(t.row.failed))])]),e._v(" "),a("el-form-item",{attrs:{label:""}},[a("span")]),e._v(" "),a("el-form-item",{attrs:{label:"异常日志:"}},[a("el-input",{staticStyle:{"font-size":"12px",width:"700px"},attrs:{type:"textarea",autosize:{minRows:2,maxRows:5}},model:{value:t.row.exception,callback:function(a){e.$set(t.row,"exception",a)},expression:"props.row.exception"}})],1)],1)]}}])}),e._v(" "),a("el-table-column",{attrs:{prop:"createTime",label:"日志时间","min-width":"15%","show-overflow-tooltip":!0}}),e._v(" "),a("el-table-column",{attrs:{prop:"username",label:"操作用户","min-width":"10%","show-overflow-tooltip":!0}}),e._v(" "),a("el-table-column",{attrs:{prop:"ipAddress",label:"请求IP","min-width":"10%","show-overflow-tooltip":!0}}),e._v(" "),a("el-table-column",{attrs:{prop:"moduleName",label:"操作类型","min-width":"10%","show-overflow-tooltip":!0}}),e._v(" "),a("el-table-column",{attrs:{prop:"elapseSeconds",label:"耗时(ms)","min-width":"10%","show-overflow-tooltip":!0}}),e._v(" "),a("el-table-column",{attrs:{prop:"urlPath",label:"请求路径","min-width":"20%","show-overflow-tooltip":!0}}),e._v(" "),a("el-table-column",{attrs:{prop:"failed",label:"异常",formatter:e.boolFormat,"min-width":"10%","show-overflow-tooltip":!0}})],1),e._v(" "),a("div",{staticClass:"page",attrs:{align:"right"}},[a("el-pagination",{attrs:{"current-page":e.currentPage,"page-sizes":[5,10,20,40],"page-size":e.pageSize,layout:"total, sizes, prev, pager, next, jumper",total:e.totalCount},on:{"size-change":e.handleSizeChange,"current-change":e.handleCurrentChange}})],1)],1)},staticRenderFns:[]};var n=a("VU/8")(l,o,!1,function(e){a("3GnV")},"data-v-aee4e92e",null);t.default=n.exports},"3GnV":function(e,t){}});
//# sourceMappingURL=9.5a8e8f0e586b6f27113c.js.map