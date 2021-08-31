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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PageUtil {

  public static <E> PageResult<E> getPage(Supplier<List<E>> method, int pageNum, int pageSize) {
    com.github.pagehelper.Page<E> originPage = PageHelper.startPage(pageNum, pageSize);
    List<E> result = method.get();

    PageResult<E> resultPage = new PageResult<>();
    resultPage.setData(result);

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

  public static <E> PageResult<E> getPage(List<E> pagingList, int pageNum, int pageSize) {
    Pagination pagination = new Pagination();
    PageResult<E> page = new PageResult<>();
    if (pageNum <= 0 || pageSize <= 0) {
      pagination.setTotal(pagingList.size());
      pagination.setPage(0);
      pagination.setSize(0);
      page.setPagination(pagination);
      page.setData(pagingList);
    } else {
      pagination.setTotal(pagingList.size());
      pagination.setPage(pageNum);
      pagination.setSize(pageSize);

      page.setPagination(pagination);
      page.setData(subList(pagingList, pageNum, pageSize));
    }

    return page;
  }

  public static <E> PageResult<E> pacPage(List<E> pagingList, int pageNum, int pageSize, int total) {
    Pagination pagination = new Pagination();
    PageResult<E> page = new PageResult<>();
    pagination.setTotal(total);
    pagination.setPage(pageNum);
    pagination.setSize(pageSize);
    page.setPagination(pagination);
    page.setData(pagingList);
    return page;
  }

  private static <E> List<E> subList(List<E> list, int pageNum, int pageSize) {
    int size = list.size();
    List<E> result = new ArrayList<>();
    int idx = (pageNum - 1) * pageSize;
    int end = idx + pageSize;
    while (idx < size && idx < end) {
      result.add(list.get(idx));
      idx++;
    }

    return result;
  }

}
