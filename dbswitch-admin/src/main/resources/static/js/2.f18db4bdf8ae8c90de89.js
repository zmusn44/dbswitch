webpackJsonp([2,4],{"4er+":function(t,e,n){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var a={name:"asideBarItem",props:{router:{type:Object}},components:{},data:function(){return{}},computed:{},watch:{},methods:{hasChildrenAndShow:function(t){return!t.hidden&&t.hasOwnProperty("children")},saveActivePath:function(t){this.$emit("setActivePath",t)}},created:function(){},mounted:function(){}},i={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"asideBarItem-container"},[t.hasChildrenAndShow(t.router)?n("el-submenu",{attrs:{index:t.router.path}},[n("template",{slot:"title"},[n("i",{class:t.router.icon}),t._v(" "),n("span",{attrs:{slot:"title"},slot:"title"},[t._v(t._s(t.router.name))])]),t._v(" "),t._l(t.router.children,function(t,e){return n("asideBarItem",{key:t.path,attrs:{router:t}})})],2):n("el-menu-item",{key:t.router.path,attrs:{index:t.router.path},on:{click:function(e){return t.saveActivePath(t.router.path)}}},[n("i",{class:t.router.icon}),t._v(" "),n("span",[t._v(t._s(t.router.name))])])],1)},staticRenderFns:[]};var s={name:"asideBar",components:{asideBarItem:n("VU/8")(a,i,!1,function(t){n("m+0z")},"data-v-0f2a2f60",null).exports},data:function(){return{collapsed:!1,initActivePath:"/dashboard"}},computed:{routers:function(){return this.$router.options.routes[0].children}},watch:{},methods:{showBarItem:function(t){return!t.hidden},handleOpen:function(t,e){},handleClose:function(t,e){},updateCollapse:function(t){this.collapsed=t},setActivePath:function(t){this.initActivePath=t,window.sessionStorage.setItem("activePath",t)},getActivePath:function(){return window.sessionStorage.getItem("activePath")}},created:function(){this.initActivePath=this.getActivePath()},mounted:function(){}},r={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"aside-container"},[n("el-row",{staticClass:"tac"},[n("el-col",{attrs:{span:24}},[n("el-menu",{attrs:{router:!0,"unique-opened":"","background-color":"#001529","text-color":"rgb(191, 203, 217)","active-text-color":"#ffffff",collapse:t.collapsed,"default-active":t.initActivePath},on:{open:t.handleOpen,close:t.handleClose}},t._l(t.routers,function(e){return t.showBarItem(e)?n("asideBarItem",{key:e.path,attrs:{router:e},on:{setActivePath:t.setActivePath}}):t._e()}),1)],1)],1)],1)},staticRenderFns:[]};var c={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("el-breadcrumb",{staticClass:"app-breadcrumb",attrs:{"separator-class":"el-icon-arrow-right"}},[n("transition-group",t._l(t.levelList,function(e,a){return e.name?n("el-breadcrumb-item",{key:e.path},["noredirect"===e.redirect||a==t.levelList.length-1?n("span",{staticClass:"no-redirect"},[t._v(t._s(e.name))]):n("router-link",{attrs:{to:e.redirect||e.path}},[t._v(t._s(e.name))])],1):t._e()}),1)],1)},staticRenderFns:[]};var o={data:function(){return{username:"",nickname:""}},created:function(){this.username=window.sessionStorage.getItem("username"),this.nickname=window.sessionStorage.getItem("realname")},methods:{hadleLogout:function(){window.sessionStorage.clear(),this.$http({method:"GET",url:"/dbswitch/admin/api/v1/authentication/logout"}),this.$router.push("/login")}},destroyed:function(){window.sessionStorage.setItem("activePath","/")}},l={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"user-dropdown-wrap"},[a("el-dropdown",[a("div",{staticClass:"user-dropdown-photo"},[a("span",{staticClass:"user-dropdown-text"},[t._v("\n        "+t._s(t.nickname)+"("+t._s(t.username)+")\n        "),a("i",{staticClass:"el-icon-caret-bottom"})]),t._v(" "),a("img",{attrs:{src:n("BQ5I"),alt:"user"}})]),t._v(" "),a("el-dropdown-menu",{attrs:{solt:"dropdown"}},[a("el-dropdown-item",[a("router-link",{attrs:{to:"/user/personal"}},[a("i",{staticClass:"el-icon-s-custom"}),t._v("个人信息\n        ")])],1),t._v(" "),a("el-dropdown-item",{attrs:{divided:""}},[a("a",{on:{click:function(e){return t.hadleLogout()}}},[a("i",{staticClass:"el-icon-switch-button"}),t._v("推出登录\n        ")])])],1)],1)],1)},staticRenderFns:[]};var d={render:function(){var t=this.$createElement,e=this._self._c||t;return e("div",{staticClass:"viewer-container"},[e("router-view")],1)},staticRenderFns:[]};var u={name:"home",components:{asideBar:n("VU/8")(s,r,!1,function(t){n("GWYl")},"data-v-1dde93fa",null).exports,breadcrumb:n("VU/8")({name:"breadcrumb",data:function(){return{levelList:[]}},created:function(){this.getBreadcrumb()},watch:{$route:function(){this.getBreadcrumb()}},methods:{getBreadcrumb:function(){var t=this.$route.matched.filter(function(t){return t.name});this.levelList=t}}},c,!1,function(t){n("Yuwj")},"data-v-a2157ea2",null).exports,userDropdown:n("VU/8")(o,l,!1,function(t){n("NuUy")},"data-v-03213484",null).exports,viewMain:n("VU/8")({name:"viewer",components:{},data:function(){return{}},computed:{},watch:{},methods:{},created:function(){},mounted:function(){}},d,!1,function(t){n("mWqL")},"data-v-7b74460c",null).exports},data:function(){return{title:"DBSwitch",isCollapse:null}},computed:{},watch:{},methods:{handleToggleCollapse:function(){var t=!this.isCollapse;this.isCollapse=t,this.$refs.asideBar.updateCollapse(t)}},created:function(){},mounted:function(){}},v={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("el-container",{staticClass:"index-container"},[a("el-aside",{attrs:{width:t.isCollapse?"64px":"250px"}},[a("div",{staticClass:"title"},[a("img",{attrs:{src:n("7Otq")}}),t._v(" "),t.isCollapse?t._e():a("span",{staticClass:"title-text"},[t._v(t._s(t.title))])]),t._v(" "),a("asideBar",{ref:"asideBar"})],1),t._v(" "),a("el-main",[a("el-header",[a("div",{staticClass:"collapse",on:{click:t.handleToggleCollapse}},[t.isCollapse?a("i",{staticClass:"el-icon-s-unfold"}):a("i",{staticClass:"el-icon-s-fold"})]),t._v(" "),a("breadcrumb"),t._v(" "),a("userDropdown")],1),t._v(" "),a("viewMain")],1)],1)},staticRenderFns:[]};var h=n("VU/8")(u,v,!1,function(t){n("R5n3")},"data-v-6bc2f4ea",null);e.default=h.exports},"7Otq":function(t,e,n){t.exports=n.p+"static/img/logo.0513b2b.png"},BQ5I:function(t,e){t.exports="data:image/jpeg;base64,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"},GWYl:function(t,e){},NuUy:function(t,e){},R5n3:function(t,e){},T83f:function(t,e){},Yuwj:function(t,e){},"m+0z":function(t,e){},m25N:function(t,e,n){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var a={components:{layout:n("4er+").default},data:function(){return{activeName:"first"}},methods:{handleClick:function(t,e){}}},i={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[n("h3",[t._v("关于dbswitch工具")]),t._v(" "),n("el-tabs",{on:{"tab-click":function(e){return t.handleClick()}},model:{value:t.activeName,callback:function(e){t.activeName=e},expression:"activeName"}},[n("el-tab-pane",{attrs:{label:"系统功能",name:"first"}},[n("div",{staticClass:"content_tag"},[n("el-tag",{attrs:{type:"danger"}},[t._v("迁移")]),t._v(" "),n("el-tag",{attrs:{type:"success"}},[t._v("同步")])],1),t._v(" "),n("div",{staticClass:"content_card"},[n("el-card",{staticClass:"box-card"},[n("div",{staticClass:"text item"},[n("p",[t._v("\n                一句话，dbswitch工具提供源端数据库向目的端数据的"),n("B",[t._v("迁移同步")]),t._v("功能，包括全量和增量方式。迁移包括：\n              ")],1),t._v(" "),n("ul",[n("li",[t._v("项目托管")]),t._v(" "),n("p",[t._v("\n                  Gitee地址："),n("a",{attrs:{href:"https://gitee.com/inrgihc/dbswitch"}},[t._v("https://gitee.com/inrgihc/dbswitch")])]),t._v(" "),n("li",[t._v("结构迁移")]),t._v(" "),n("p",[t._v("\n                  字段类型、主键信息、建表语句等的转换，并生成建表SQL语句。\n                ")]),t._v(" "),n("p",[t._v("\n                  支持基于正则表达式转换的表名与字段名映射转换。\n                ")]),t._v(" "),n("li",[t._v("数据迁移")]),t._v(" "),n("p",[t._v("\n                  基于JDBC的分批次读取源端数据库数据，并基于insert/copy方式将数据分批写入目的数据库。\n                ")]),t._v(" "),n("p",[t._v("\n                  支持有主键表的 增量变更同步 （变化数据计算Change Data Calculate）功能。\n                ")])])])])],1)]),t._v(" "),n("el-tab-pane",{attrs:{label:"异构数据库",name:"second"}},[n("div",{staticClass:"content_tag"},[n("el-tag",{attrs:{type:"success"}},[t._v("JDBC")]),t._v(" "),n("el-tag",{attrs:{type:"danger"}},[t._v("dbswitch")])],1),t._v(" "),n("div",{staticClass:"content_card"},[n("el-card",{staticClass:"box-card"},[n("div",{staticClass:"text item"},[n("p",[t._v("\n                dbswitch提供异构关系数据库间的数据迁移同步，支持绝大多数关系型数据库，包括：\n              ")]),t._v(" "),n("ul",[n("li",[t._v("甲骨文的Oracle\n                ")]),t._v(" "),n("li",[t._v("微软的Microsoft SQLServer\n                ")]),t._v(" "),n("li",[t._v("MySQL\n                ")]),t._v(" "),n("li",[t._v("MariaDB\n                ")]),t._v(" "),n("li",[t._v("PostgreSQL\n                ")]),t._v(" "),n("li",[t._v("Greenplum\n                ")]),t._v(" "),n("li",[t._v("DB2\n                ")]),t._v(" "),n("li",[t._v("达梦数据库DM\n                ")]),t._v(" "),n("li",[t._v("人大金仓数据库Kingbase8\n                ")]),t._v(" "),n("li",[t._v("翰高数据库HighGo\n                ")]),t._v(" "),n("li",[t._v("Apache Hive(只支持为源端)\n                ")]),t._v(" "),n("li",[t._v("SQLite3\n                ")])])])])],1)]),t._v(" "),n("el-tab-pane",{attrs:{label:"开源技术栈",name:"third"}},[n("div",{staticClass:"content_tag"},[n("el-tag",{attrs:{type:"success"}},[t._v("SpringBoot")]),t._v(" "),n("el-tag",{attrs:{type:"danger"}},[t._v("Quartz")]),t._v(" "),n("el-tag",{attrs:{type:"success"}},[t._v("Vue/ElementUI")])],1),t._v(" "),n("div",{staticClass:"content_card"},[n("el-card",{staticClass:"box-card"},[n("div",{staticClass:"text item"},[n("p",[t._v("\n                dbswitch基于Springboot脚手架进行的后端模块开发，模块组成结构如下：\n              "),n("pre",[t._v("\t\t\t\t└── dbswitch\n\t\t\t\t\t├── dbswitch-common    // dbswitch通用定义模块\n\t\t\t\t\t├── dbswitch-pgwriter  // PostgreSQL的二进制写入封装模块\n\t\t\t\t\t├── dbswitch-dbwriter  // 数据库的通用批量Insert封装模块\n\t\t\t\t\t├── dbswitch-core      // 数据库元数据抽取与建表结构语句转换模块\n\t\t\t\t\t├── dbswitch-sql       // 基于calcite的DML语句转换与DDL拼接模块\n\t\t\t\t\t├── dbswitch-dbcommon  // 数据库操作通用封装模块\n\t\t\t\t\t├── dbswitch-dbchange  // 基于全量比对计算变更（变化量）数据模块\n\t\t\t\t\t├── dbswitch-dbsynch   // 将dbchange模块计算的变更数据同步入库模块\n\t\t\t\t\t├── dbswitch-data      // 工具入口模块，读取配置文件中的参数执行异构迁移同步\n\t\t\t\t\t├── dbswitch-admin     // 在以上模块的基础上，采用Quartz提供可视化调度\n\t\t\t\t\t├── dbswitch-admin-ui  // 基于Vue+ElementUI的前端交互页面\n\t\t\t\t\t├── package-tool       // 基于maven-assembly-plugin插件的项目打包模块\n                ")])]),t._v(" "),n("ul",[n("li",[t._v("SpringBoot/Mybatis")]),t._v(" "),n("p",[t._v("\n                  dbwitch基于SpringBoot作为项目的基础框架，利用JdbcTemplate提供常规的动态SQL读写操作，实现异构数据库数据的导出与导入功能。\n                ")]),t._v(" "),n("p",[t._v("\n                  dbwitch-admin模块为用户交互提供了服务接口，基于Mybatis提供配置数据的持久化。\n                ")]),t._v(" "),n("li",[t._v("Quartz")]),t._v(" "),n("p",[t._v("\n                  Quartz是一个开源的作业调度框架，它完全由Java写成。dbswitch-admin基于Quartz提供了支持集群模式迁移同步任务调度功能。\n                ")]),t._v(" "),n("li",[t._v("Vue/ElementUI")]),t._v(" "),n("p",[t._v("\n                  Vue是一套用于构建用户界面的渐进式JavaScript框架。 Element是饿了么团队基于MVVM框架Vue开源出来的一套前端基于Vue 2.0的桌面端组件库。\n                ")]),t._v(" "),n("p",[t._v("\n                  dbswitch-admin-ui模块基于Vue和ElementUI提供可视化的操作WEB界面。\n                ")])])])])],1)])],1)],1)},staticRenderFns:[]};var s=n("VU/8")(a,i,!1,function(t){n("T83f")},"data-v-95acf64e",null);e.default=s.exports},mWqL:function(t,e){}});
//# sourceMappingURL=2.f18db4bdf8ae8c90de89.js.map