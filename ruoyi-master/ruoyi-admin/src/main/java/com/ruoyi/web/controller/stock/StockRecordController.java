package com.ruoyi.web.controller.stock;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.service.IBaseMatClassService;
import com.ruoyi.base.service.IBaseMatGroupService;
import com.ruoyi.base.service.IBaseWarehouseService;
import com.ruoyi.base.service.IBaseWorkshopService;
import com.ruoyi.common.bean.typeEnum.StockRecordTypeEnum;
import com.ruoyi.stock.service.IStockInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.stock.domain.StockRecord;
import com.ruoyi.stock.service.IStockRecordService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 库存流水Controller
 *
 * @author ruoyi
 * @date 2022-07-25
 */
@RestController
@RequestMapping("/stock/record")
public class StockRecordController extends BaseController {
    @Autowired
    private IStockRecordService stockRecordService;
    @Autowired
    private IBaseMatGroupService baseMatGroupService;
    @Autowired
    private IBaseMatClassService baseMatClassService;
    @Autowired
    private IBaseWarehouseService baseWarehouseService;
    @Autowired
    private IBaseWorkshopService baseWorkshopService;

    /**
     * 查询库存流水列表
     */
    @PreAuthorize("@ss.hasPermi('stock:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockRecord stockRecord) {
        startPage();
        List<StockRecord> list = stockRecordService.selectStockRecordList(stockRecord);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockRecord record : list){
                record.setRecordTypeLabel(StockRecordTypeEnum.getLabel(record.getRecordType()));
                record.setMatGroupName(baseMatGroupService.selectBaseMatGroupNameByGroupCode(record.getMatGroup()));
                record.setMatClassName(baseMatClassService.selectBaseMatClassNameByClassCode(record.getMatClass()));
                record.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(record.getWarehouseCode()));
                record.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(record.getWorkshopCode()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 查询库存流水列表（退货）
     */
    @GetMapping("/returnList/{orderNo}")
    public AjaxResult returnList(@PathVariable("orderNo")String orderNo) {
        List<StockRecord> list = stockRecordService.selectStockRecordListByOrderNo(orderNo);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockRecord record : list){
                record.setRecordTypeLabel(StockRecordTypeEnum.getLabel(record.getRecordType()));
                record.setMatGroupName(baseMatGroupService.selectBaseMatGroupNameByGroupCode(record.getMatGroup()));
                record.setMatClassName(baseMatClassService.selectBaseMatClassNameByClassCode(record.getMatClass()));
                record.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(record.getWarehouseCode()));
            }
        }
        return AjaxResult.success(list);
    }

    /**
     * 导出库存流水列表
     */
    @PreAuthorize("@ss.hasPermi('stock:record:export')")
    @Log(title = "库存流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockRecord stockRecord) {
        List<StockRecord> list = stockRecordService.selectStockRecordList(stockRecord);
        ExcelUtil<StockRecord> util = new ExcelUtil<StockRecord>(StockRecord.class);
        util.exportExcel(response, list, "库存流水数据");
    }

    /**
     * 获取库存流水详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:record:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId) {
        return AjaxResult.success(stockRecordService.selectStockRecordByRecordId(recordId));
    }

    /**
     * 新增库存流水
     */
    @PreAuthorize("@ss.hasPermi('stock:record:add')")
    @Log(title = "库存流水", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockRecord stockRecord) {
        return toAjax(stockRecordService.insertStockRecord(stockRecord));
    }

    /**
     * 修改库存流水
     */
    @PreAuthorize("@ss.hasPermi('stock:record:edit')")
    @Log(title = "库存流水", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockRecord stockRecord) {
        return toAjax(stockRecordService.updateStockRecord(stockRecord));
    }

    /**
     * 删除库存流水
     */
    @PreAuthorize("@ss.hasPermi('stock:record:remove')")
    @Log(title = "库存流水", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds) {
        return toAjax(stockRecordService.deleteStockRecordByRecordIds(recordIds));
    }
}
