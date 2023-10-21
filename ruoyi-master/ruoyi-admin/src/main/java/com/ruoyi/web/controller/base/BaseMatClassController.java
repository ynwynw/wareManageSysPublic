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
import com.ruoyi.base.domain.BaseMatClass;
import com.ruoyi.base.service.IBaseMatClassService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 物料分类Controller
 *
 * @author ruoyi
 * @date 2022-07-23
 */
@RestController
@RequestMapping("/base/class")
public class BaseMatClassController extends BaseController {
    @Autowired
    private IBaseMatClassService baseMatClassService;

    /**
     * 查询物料分类列表
     */
    @PreAuthorize("@ss.hasPermi('base:class:list')")
    @GetMapping("/list")
    public TableDataInfo list(BaseMatClass baseMatClass) {
        startPage();
        List<BaseMatClass> list = baseMatClassService.selectBaseMatClassList(baseMatClass);
        return getDataTable(list);
    }

    /**
     * 查询物料分类列表(所有)
     */
    @PreAuthorize("@ss.hasPermi('base:class:list')")
    @GetMapping("/listAll")
    public List<BaseMatClass> listAll(BaseMatClass baseMatClass) {
        List<BaseMatClass> list = baseMatClassService.selectBaseMatClassList(baseMatClass);
        return list;
    }

    /**
     * 导出物料分类列表
     */
    @PreAuthorize("@ss.hasPermi('base:class:export')")
    @Log(title = "物料分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BaseMatClass baseMatClass) {
        List<BaseMatClass> list = baseMatClassService.selectBaseMatClassList(baseMatClass);
        ExcelUtil<BaseMatClass> util = new ExcelUtil<BaseMatClass>(BaseMatClass.class);
        util.exportExcel(response, list, "物料分类数据");
    }

    /**
     * 获取物料分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('base:class:query')")
    @GetMapping(value = "/{classId}")
    public AjaxResult getInfo(@PathVariable("classId") Long classId) {
        return AjaxResult.success(baseMatClassService.selectBaseMatClassByClassId(classId));
    }

    /**
     * 新增物料分类
     */
    @PreAuthorize("@ss.hasPermi('base:class:add')")
    @Log(title = "物料分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BaseMatClass baseMatClass) {
        baseMatClass.setCreateBy(getUsername());
        return toAjax(baseMatClassService.insertBaseMatClass(baseMatClass));
    }

    /**
     * 修改物料分类
     */
    @PreAuthorize("@ss.hasPermi('base:class:edit')")
    @Log(title = "物料分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BaseMatClass baseMatClass) {
        baseMatClass.setUpdateBy(getUsername());
        return toAjax(baseMatClassService.updateBaseMatClass(baseMatClass));
    }

    /**
     * 删除物料分类
     */
    @PreAuthorize("@ss.hasPermi('base:class:remove')")
    @Log(title = "物料分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{classIds}")
    public AjaxResult remove(@PathVariable Long[] classIds) {
        return toAjax(baseMatClassService.deleteBaseMatClassByClassIds(classIds));
    }
}
