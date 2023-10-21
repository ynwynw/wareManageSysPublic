package com.ruoyi.web.controller.stock;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.service.IBaseMatClassService;
import com.ruoyi.base.service.IBaseMatGroupService;
import com.ruoyi.base.service.IBaseWarehouseService;
import com.ruoyi.common.bean.request.PutOffRequestBody;
import com.ruoyi.common.bean.request.PutOnRequestBody;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.stock.domain.StockInOrder;
import com.ruoyi.stock.domain.StockRecord;
import com.ruoyi.stock.domain.stats.StockInfoSummary;
import com.ruoyi.system.service.ISysDictDataService;
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
import com.ruoyi.stock.domain.StockInfo;
import com.ruoyi.stock.service.IStockInfoService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 库存信息Controller
 *
 * @author ruoyi
 * @date 2022-07-25
 */
@RestController
@RequestMapping("/stock/info")
public class StockInfoController extends BaseController {
    @Autowired
    private IStockInfoService stockInfoService;
    @Autowired
    private IBaseMatGroupService baseMatGroupService;
    @Autowired
    private IBaseMatClassService baseMatClassService;
    @Autowired
    private IBaseWarehouseService baseWarehouseService;
    @Autowired
    private ISysDictDataService sysDictDataService;

    private static final String dictType = "base_mat_unit";

    /**
     * 查询库存汇总
     */
    @PreAuthorize("@ss.hasPermi('stock:record:list')")
    @GetMapping("/statsList")
    public TableDataInfo statsList(StockInfo stockInfo) {
        startPage();
        List<StockInfo> list = stockInfoService.selectStockInfoStatsList(stockInfo);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockInfo info : list){
                info.setMatGroupName(baseMatGroupService.selectBaseMatGroupNameByGroupCode(info.getMatGroup()));
                info.setMatClassName(baseMatClassService.selectBaseMatClassNameByClassCode(info.getMatClass()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出库存汇总列表
     */
    @PreAuthorize("@ss.hasPermi('stock:info:export')")
    @Log(title = "库存汇总", businessType = BusinessType.EXPORT)
    @PostMapping("/statsExport")
    public void statsExport(HttpServletResponse response, StockInfo stockInfo) {
        List<StockInfo> list = stockInfoService.selectStockInfoStatsList(stockInfo);
        ExcelUtil<StockInfoSummary> util = new ExcelUtil<StockInfoSummary>(StockInfoSummary.class);
        List<StockInfoSummary> summaryList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)){
            StockInfoSummary summary = null;
            for(StockInfo info : list){
                summary = new StockInfoSummary();
                BeanUtils.copyBeanProp(summary, info);
                summaryList.add(summary);
            }
        }
        util.exportExcel(response, summaryList, "库存汇总数据");
    }

    /**
     * 查询库存汇总
     */
    @PreAuthorize("@ss.hasPermi('stock:record:list')")
    @GetMapping("/locationMatList")
    public AjaxResult locationMatList(StockInfo stockInfo) {
        List<StockInfo> list = stockInfoService.selectStockInfoStatsList(stockInfo);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockInfo info : list){
                info.setUnitName(sysDictDataService.selectDictLabel(dictType, info.getUnitCode()));
            }
        }
        return AjaxResult.success(list);
    }

    /**
     * 查询库存信息列表
     */
    @PreAuthorize("@ss.hasPermi('stock:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockInfo stockInfo) {
        startPage();
        List<StockInfo> list = stockInfoService.selectStockInfoList(stockInfo);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockInfo info : list){
                info.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(info.getWarehouseCode()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出库存信息列表
     */
    @PreAuthorize("@ss.hasPermi('stock:info:export')")
    @Log(title = "库存信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockInfo stockInfo) {
        List<StockInfo> list = stockInfoService.selectStockInfoList(stockInfo);
        ExcelUtil<StockInfo> util = new ExcelUtil<StockInfo>(StockInfo.class);
        util.exportExcel(response, list, "库存信息数据");
    }

    /**
     * 获取库存信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:info:query')")
    @GetMapping(value = "/{infoId}")
    public AjaxResult getInfo(@PathVariable("infoId") Long infoId) {
        return AjaxResult.success(stockInfoService.selectStockInfoByInfoId(infoId));
    }

    /**
     * 新增库存信息
     */
    @PreAuthorize("@ss.hasPermi('stock:info:add')")
    @Log(title = "库存信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockInfo stockInfo) {
        return toAjax(stockInfoService.insertStockInfo(stockInfo));
    }

    /**
     * 修改库存信息
     */
    @PreAuthorize("@ss.hasPermi('stock:info:edit')")
    @Log(title = "库存信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockInfo stockInfo) {
        return toAjax(stockInfoService.updateStockInfo(stockInfo));
    }

    /**
     * 删除库存信息
     */
    @PreAuthorize("@ss.hasPermi('stock:info:remove')")
    @Log(title = "库存信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds) {
        return toAjax(stockInfoService.deleteStockInfoByInfoIds(infoIds));
    }

    /**
     * 扫码提交上架
     */
    @PostMapping("submitPutOn")
    public AjaxResult submitPutOn(@RequestBody PutOnRequestBody putOnRequestBody){
        if(putOnRequestBody == null){
            return error("系统繁忙，请稍后再试！");
        }
        return stockInfoService.submitPutOn(getUsername(), putOnRequestBody);
    }

    /**
     * 扫码提交下架
     */
    @PostMapping("submitPutOff")
    public AjaxResult submitPutOff(@RequestBody PutOffRequestBody putOffRequestBody){
        if(putOffRequestBody == null){
            return error("系统繁忙，请稍后再试！");
        }
        return stockInfoService.submitPutOff(getUsername(), putOffRequestBody);
    }

}
