package com.ruoyi.system.mapper;

import java.util.List;

import com.ruoyi.system.domain.SysUserClass;
import org.apache.ibatis.annotations.Param;

/**
 * 用户管理的物料分类Mapper接口
 *
 * @author ruoyi
 * @date 2022-08-11
 */
public interface SysUserClassMapper {
    /**
     * 查询用户管理的物料分类
     *
     * @param userName 用户管理的物料分类主键
     * @return 用户管理的物料分类
     */
    public SysUserClass selectSysUserClassByUserName(@Param("userName") String userName, @Param("classCode") String classCode);

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
    public List<SysUserClass> selectSysUserClassListByUserName(@Param("userName") String userName);

    /**
     * 新增用户管理的物料分类
     *
     * @param sysUserClass 用户管理的物料分类
     * @return 结果
     */
    public int insertSysUserClass(SysUserClass sysUserClass);


    /**
     * 删除用户管理的物料分类
     *
     * @param userName 用户管理的物料分类主键
     * @return 结果
     */
    public int deleteSysUserClassByUserName(@Param("userName") String userName, @Param("classCode") String classCode);

}
