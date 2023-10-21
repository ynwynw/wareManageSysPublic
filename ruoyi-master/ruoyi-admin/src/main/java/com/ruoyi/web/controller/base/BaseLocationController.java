package com.ruoyi.web.controller.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.service.IBaseWarehouseService;
import com.ruoyi.common.bean.LocationPdfData;
import com.ruoyi.common.bean.OutDetailPdfData;
import com.ruoyi.common.bean.OutOrderPdfData;
import com.ruoyi.common.service.PDFService;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.stock.domain.StockOutDetail;
import com.ruoyi.stock.domain.StockOutOrder;
import org.apache.commons.collections4.CollectionUtils;
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
import com.ruoyi.base.domain.BaseLocation;
import com.ruoyi.base.service.IBaseLocationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 货位Controller
 *
 * @author ruoyi
 * @date 2022-07-24
 */
@RestController
@RequestMapping("/base/location")
public class BaseLocationController extends BaseController {
    @Autowired
    private IBaseLocationService baseLocationService;
    @Autowired
    private IBaseWarehouseService baseWarehouseService;
    @Autowired
    private PDFService pdfService;

    /**
     * 查询货位列表
     */
    @PreAuthorize("@ss.hasPermi('base:location:list')")
    @GetMapping("/list")
    public TableDataInfo list(BaseLocation baseLocation) {
        startPage();
        List<BaseLocation> list = baseLocationService.selectBaseLocationList(baseLocation);
        if(CollectionUtils.isNotEmpty(list)){
            for(BaseLocation location : list){
                location.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(location.getWarehouseCode()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 查询货位列表
     */
    //@PreAuthorize("@ss.hasPermi('base:location:list')")
    @GetMapping("/listAll")
    public List<BaseLocation> listAll(BaseLocation baseLocation) {
        startPage();
        List<BaseLocation> list = baseLocationService.selectBaseLocationList(baseLocation);
        if(CollectionUtils.isNotEmpty(list)){
            for(BaseLocation location : list){
                location.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(location.getWarehouseCode()));
            }
        }
        return list;
    }

    /**
     * 导出货位列表
     */
    @PreAuthorize("@ss.hasPermi('base:location:export')")
    @Log(title = "货位", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BaseLocation baseLocation) {
        List<BaseLocation> list = baseLocationService.selectBaseLocationList(baseLocation);
        ExcelUtil<BaseLocation> util = new ExcelUtil<BaseLocation>(BaseLocation.class);
        util.exportExcel(response, list, "货位数据");
    }

    /**
     * 获取货位详细信息
     */
    @PreAuthorize("@ss.hasPermi('base:location:query')")
    @GetMapping(value = "/{locationId}")
    public AjaxResult getInfo(@PathVariable("locationId") Long locationId) {
        return AjaxResult.success(baseLocationService.selectBaseLocationByLocationId(locationId));
    }

    /**
     * 新增货位
     */
    @PreAuthorize("@ss.hasPermi('base:location:add')")
    @Log(title = "货位", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BaseLocation baseLocation) {
        baseLocation.setCreateBy(getUsername());
        return toAjax(baseLocationService.insertBaseLocation(baseLocation));
    }

    /**
     * 修改货位
     */
    @PreAuthorize("@ss.hasPermi('base:location:edit')")
    @Log(title = "货位", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BaseLocation baseLocation) {
        baseLocation.setUpdateBy(getUsername());
        return toAjax(baseLocationService.updateBaseLocation(baseLocation));
    }

    /**
     * 删除货位
     */
    @PreAuthorize("@ss.hasPermi('base:location:remove')")
    @Log(title = "货位", businessType = BusinessType.DELETE)
    @DeleteMapping("/{locationIds}")
    public AjaxResult remove(@PathVariable Long[] locationIds) {
        return toAjax(baseLocationService.deleteBaseLocationByLocationIds(locationIds));
    }

    /**
     * 打印货位
     */
    @GetMapping("printLocation/{locationId}")
    public void printLocation(HttpServletResponse response, @PathVariable Long locationId) throws Exception{
        OutputStream out = response.getOutputStream();
        BaseLocation location = baseLocationService.selectBaseLocationByLocationId(locationId);
        if(location != null){
            LocationPdfData locationPdfData = new LocationPdfData();
            BeanUtils.copyBeanProp(locationPdfData, location);
            InputStream in = new FileInputStream(new File(pdfService.printLocation(locationPdfData)));
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
