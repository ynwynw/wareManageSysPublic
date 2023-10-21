package com.ruoyi.web.controller.base;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.base.domain.BaseMatGroup;
import com.ruoyi.base.service.IBaseMatGroupService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 物料组Controller
 *
 * @author ruoyi
 * @date 2022-07-23
 */
@RestController
@RequestMapping("/base/group")
public class BaseMatGroupController extends BaseController {
    @Autowired
    private IBaseMatGroupService baseMatGroupService;

    /**
     * 查询物料组列表
     */
    @PreAuthorize("@ss.hasPermi('base:group:list')")
    @GetMapping("/list")
    public TableDataInfo list(BaseMatGroup baseMatGroup) {
        startPage();
        List<BaseMatGroup> list = baseMatGroupService.selectBaseMatGroupList(baseMatGroup);
        return getDataTable(list);
    }

    /**
     * 查询物料组列表（所有）
     */
    @PreAuthorize("@ss.hasPermi('base:group:list')")
    @GetMapping("/listAll")
    public List<BaseMatGroup> listAll(BaseMatGroup baseMatGroup) {
        List<BaseMatGroup> list = baseMatGroupService.selectBaseMatGroupList(baseMatGroup);
        return list;
    }

    /**
     * 导出物料组列表
     */
    @PreAuthorize("@ss.hasPermi('base:group:export')")
    @Log(title = "物料组", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BaseMatGroup baseMatGroup) {
        List<BaseMatGroup> list = baseMatGroupService.selectBaseMatGroupList(baseMatGroup);
        ExcelUtil<BaseMatGroup> util = new ExcelUtil<BaseMatGroup>(BaseMatGroup.class);
        util.exportExcel(response, list, "物料组数据");
    }

    /**
     * 获取物料组详细信息
     */
    @PreAuthorize("@ss.hasPermi('base:group:query')")
    @GetMapping(value = "/{groupId}")
    public AjaxResult getInfo(@PathVariable("groupId") Long groupId) {
        return AjaxResult.success(baseMatGroupService.selectBaseMatGroupByGroupId(groupId));
    }

    /**
     * 新增物料组
     */
    @PreAuthorize("@ss.hasPermi('base:group:add')")
    @Log(title = "物料组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BaseMatGroup baseMatGroup) {
        baseMatGroup.setCreateBy(getUsername());
        return toAjax(baseMatGroupService.insertBaseMatGroup(baseMatGroup));
    }

    /**
     * 修改物料组
     */
    @PreAuthorize("@ss.hasPermi('base:group:edit')")
    @Log(title = "物料组", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BaseMatGroup baseMatGroup) {
        baseMatGroup.setUpdateBy(getUsername());
        return toAjax(baseMatGroupService.updateBaseMatGroup(baseMatGroup));
    }

    /**
     * 删除物料组
     */
    @PreAuthorize("@ss.hasPermi('base:group:remove')")
    @Log(title = "物料组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{groupIds}")
    public AjaxResult remove(@PathVariable Long[] groupIds) {
        return toAjax(baseMatGroupService.deleteBaseMatGroupByGroupIds(groupIds));
    }
}
