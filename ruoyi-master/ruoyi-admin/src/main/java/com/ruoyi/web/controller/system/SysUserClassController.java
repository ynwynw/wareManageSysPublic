package com.ruoyi.web.controller.system;

import java.util.List;

import com.ruoyi.base.service.IBaseMatClassService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysUserClass;
import com.ruoyi.system.service.ISysUserClassService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 用户管理的物料分类Controller
 *
 * @author ruoyi
 * @date 2022-08-11
 */
@RestController
@RequestMapping("/system/class")
public class SysUserClassController extends BaseController {
    @Autowired
    private ISysUserClassService sysUserClassService;

    /**
     * 查询用户管理的物料分类列表
     */
    @PreAuthorize("@ss.hasPermi('system:class:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUserClass sysUserClass) {
        startPage();
        List<SysUserClass> list = sysUserClassService.selectSysUserClassList(sysUserClass);
        if(CollectionUtils.isNotEmpty(list)){
            for(SysUserClass userClass : list){
                userClass.setActiveStatus(StringUtils.isNotEmpty(userClass.getUserName()) ? "1" : "0");
            }
        }
        return getDataTable(list);
    }

    /**
     * 修改用户管理的物料分类
     */
    @PreAuthorize("@ss.hasPermi('system:class:edit')")
    @Log(title = "用户管理的物料分类", businessType = BusinessType.UPDATE)
    @PutMapping("changeActive")
    public AjaxResult changeActive(@RequestBody SysUserClass sysUserClass) {
        return toAjax(sysUserClassService.changeActive(sysUserClass));
    }

}
