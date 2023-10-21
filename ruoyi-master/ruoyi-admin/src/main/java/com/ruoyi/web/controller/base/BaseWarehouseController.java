package com.ruoyi.web.controller.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.domain.BaseLocation;
import com.ruoyi.common.bean.LocationPdfData;
import com.ruoyi.common.bean.WarehousePdfData;
import com.ruoyi.common.service.PDFService;
import com.ruoyi.common.utils.bean.BeanUtils;
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
import com.ruoyi.base.domain.BaseWarehouse;
import com.ruoyi.base.service.IBaseWarehouseService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 仓库Controller
 *
 * @author ruoyi
 * @date 2022-07-24
 */
@RestController
@RequestMapping("/base/warehouse")
public class BaseWarehouseController extends BaseController {
    @Autowired
    private IBaseWarehouseService baseWarehouseService;
    @Autowired
    private PDFService pdfService;

    /**
     * 查询仓库列表
     */
    @PreAuthorize("@ss.hasPermi('base:warehouse:list')")
    @GetMapping("/list")
    public TableDataInfo list(BaseWarehouse baseWarehouse) {
        startPage();
        List<BaseWarehouse> list = baseWarehouseService.selectBaseWarehouseList(baseWarehouse);
        return getDataTable(list);
    }

    /**
     * 查询仓库列表（所有）
     */
    @PreAuthorize("@ss.hasPermi('base:warehouse:list')")
    @GetMapping("/listAll")
    public List<BaseWarehouse> listAll(BaseWarehouse baseWarehouse) {
        List<BaseWarehouse> list = baseWarehouseService.selectBaseWarehouseList(baseWarehouse);
        return list;
    }

    /**
     * 导出仓库列表
     */
    @PreAuthorize("@ss.hasPermi('base:warehouse:export')")
    @Log(title = "仓库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BaseWarehouse baseWarehouse) {
        List<BaseWarehouse> list = baseWarehouseService.selectBaseWarehouseList(baseWarehouse);
        ExcelUtil<BaseWarehouse> util = new ExcelUtil<BaseWarehouse>(BaseWarehouse.class);
        util.exportExcel(response, list, "仓库数据");
    }

    /**
     * 获取仓库详细信息
     */
    @PreAuthorize("@ss.hasPermi('base:warehouse:query')")
    @GetMapping(value = "/{warehouseId}")
    public AjaxResult getInfo(@PathVariable("warehouseId") Long warehouseId) {
        return AjaxResult.success(baseWarehouseService.selectBaseWarehouseByWarehouseId(warehouseId));
    }

    /**
     * 新增仓库
     */
    @PreAuthorize("@ss.hasPermi('base:warehouse:add')")
    @Log(title = "仓库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BaseWarehouse baseWarehouse) {
        return toAjax(baseWarehouseService.insertBaseWarehouse(baseWarehouse));
    }

    /**
     * 修改仓库
     */
    @PreAuthorize("@ss.hasPermi('base:warehouse:edit')")
    @Log(title = "仓库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BaseWarehouse baseWarehouse) {
        return toAjax(baseWarehouseService.updateBaseWarehouse(baseWarehouse));
    }

    /**
     * 删除仓库
     */
    @PreAuthorize("@ss.hasPermi('base:warehouse:remove')")
    @Log(title = "仓库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{warehouseIds}")
    public AjaxResult remove(@PathVariable Long[] warehouseIds) {
        return toAjax(baseWarehouseService.deleteBaseWarehouseByWarehouseIds(warehouseIds));
    }

    /**
     * 打印仓库
     */
    @GetMapping("printWarehouse/{warehouseId}")
    public void printLocation(HttpServletResponse response, @PathVariable Long warehouseId) throws Exception{
        OutputStream out = response.getOutputStream();
        BaseWarehouse warehouse = baseWarehouseService.selectBaseWarehouseByWarehouseId(warehouseId);
        if(warehouse != null){
            WarehousePdfData warehousePdfData = new WarehousePdfData();
            BeanUtils.copyBeanProp(warehousePdfData, warehouse);
            InputStream in = new FileInputStream(new File(pdfService.printWarehouse(warehousePdfData)));
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            response.setContentType("application/pdf");
            in.close();
        }
    }

}
