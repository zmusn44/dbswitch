// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.util;

import com.gitee.dbswitch.admin.common.response.PageResult;
import com.gitee.dbswitch.admin.common.response.PageResult.Pagination;
import com.github.pagehelper.PageHelper;
import java.util.List;
import java.util.function.Supplier;

public class PageUtils {

  public static <E> PageResult<E> getPage(Supplier<List<E>> method, int pageNum, int pageSize) {
    com.github.pagehelper.Page<E> originPage = PageHelper.startPage(pageNum, pageSize);
    PageResult<E> resultPage = new PageResult<>();
    resultPage.setData(method.get());

    Pagination pagination = new Pagination();
    pagination.setTotal((int) originPage.getTotal());
    if (0 == originPage.getPageSize()) {
      pagination.setPage(pageNum);
      pagination.setSize(pageSize);
    } else {
      pagination.setPage(originPage.getPageNum());
      pagination.setSize(originPage.getPageSize());
    }
    resultPage.setPagination(pagination);

    return resultPage;
  }

}
