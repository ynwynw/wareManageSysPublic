package com.ruoyi.web.controller.base;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.domain.BaseMatGroup;
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
import com.ruoyi.base.domain.BaseWorkshop;
import com.ruoyi.base.service.IBaseWorkshopService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 车间Controller
 *
 * @author ruoyi
 * @date 2022-07-28
 */
@RestController
@RequestMapping("/base/workshop")
public class BaseWorkshopController extends BaseController {
    @Autowired
    private IBaseWorkshopService baseWorkshopService;

    /**
     * 查询车间列表
     */
    @PreAuthorize("@ss.hasPermi('base:workshop:list')")
    @GetMapping("/list")
    public TableDataInfo list(BaseWorkshop baseWorkshop) {
        startPage();
        List<BaseWorkshop> list = baseWorkshopService.selectBaseWorkshopList(baseWorkshop);
        return getDataTable(list);
    }

    /**
     * 查询车间列表（所有）
     */
    @PreAuthorize("@ss.hasPermi('base:workshop:list')")
    @GetMapping("/listAll")
    public List<BaseWorkshop> listAll(BaseWorkshop baseWorkshop) {
        List<BaseWorkshop> list = baseWorkshopService.selectBaseWorkshopList(baseWorkshop);
        return list;
    }

    /**
     * 导出车间列表
     */
    @PreAuthorize("@ss.hasPermi('base:workshop:export')")
    @Log(title = "车间", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BaseWorkshop baseWorkshop) {
        List<BaseWorkshop> list = baseWorkshopService.selectBaseWorkshopList(baseWorkshop);
        ExcelUtil<BaseWorkshop> util = new ExcelUtil<BaseWorkshop>(BaseWorkshop.class);
        util.exportExcel(response, list, "车间数据");
    }

    /**
     * 获取车间详细信息
     */
    @PreAuthorize("@ss.hasPermi('base:workshop:query')")
    @GetMapping(value = "/{workshopId}")
    public AjaxResult getInfo(@PathVariable("workshopId") Long workshopId) {
        return AjaxResult.success(baseWorkshopService.selectBaseWorkshopByWorkshopId(workshopId));
    }

    /**
     * 新增车间
     */
    @PreAuthorize("@ss.hasPermi('base:workshop:add')")
    @Log(title = "车间", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BaseWorkshop baseWorkshop) {
        return toAjax(baseWorkshopService.insertBaseWorkshop(baseWorkshop));
    }

    /**
     * 修改车间
     */
    @PreAuthorize("@ss.hasPermi('base:workshop:edit')")
    @Log(title = "车间", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BaseWorkshop baseWorkshop) {
        return toAjax(baseWorkshopService.updateBaseWorkshop(baseWorkshop));
    }

    /**
     * 删除车间
     */
    @PreAuthorize("@ss.hasPermi('base:workshop:remove')")
    @Log(title = "车间", businessType = BusinessType.DELETE)
    @DeleteMapping("/{workshopIds}")
    public AjaxResult remove(@PathVariable Long[] workshopIds) {
        return toAjax(baseWorkshopService.deleteBaseWorkshopByWorkshopIds(workshopIds));
    }
}
