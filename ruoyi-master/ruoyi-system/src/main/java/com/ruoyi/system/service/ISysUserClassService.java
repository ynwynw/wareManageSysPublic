package com.ruoyi.system.service;

import java.util.List;

import com.ruoyi.system.domain.SysUserClass;

/**
 * 用户管理的物料分类Service接口
 *
 * @author ruoyi
 * @date 2022-08-11
 */
public interface ISysUserClassService {

    /**
     * 查询用户管理的物料分类列表
     *
     * @param sysUserClass 用户管理的物料分类
     * @return 用户管理的物料分类集合
     */
    public List<SysUserClass> selectSysUserClassList(SysUserClass sysUserClass);

    /**
     * 查询用户管理的物料分类列表
     */
    public List<SysUserClass> selectSysUserClassListByUserName(String userName);

    /**
     * 修改用户管理的物料分类
     */
    public int changeActive(SysUserClass sysUserClass);

}
