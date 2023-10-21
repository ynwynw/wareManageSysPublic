package com.ruoyi.web.controller.stats;

import com.ruoyi.base.service.IBaseMatService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.stock.domain.StockInOrder;
import com.ruoyi.stock.domain.stats.StockInStats;
import com.ruoyi.stock.domain.stats.StockOutStats;
import com.ruoyi.stock.domain.stats.StockRecordStats;
import com.ruoyi.stock.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计Controller
 *
 * @author ruoyi
 * @date 2022-07-24
 */
@RestController
@RequestMapping("/stats")
public class StatsController extends BaseController {
    @Autowired
    private IBaseMatService baseMatService;
    @Autowired
    private IStockInOrderService stockInOrderService;
    @Autowired
    private IStockInReturnService stockInReturnService;
    @Autowired
    private IStockOutOrderService stockOutOrderService;
    @Autowired
    private IStockOutReturnService stockOutReturnService;
    @Autowired
    private IStockRecordService stockRecordService;

    /**
     * 首页头部统计
     */
    @GetMapping("/indexUpper")
    public AjaxResult indexUpper() {
        Map<String, Long> statsMap = new HashMap<>();
        //物料数
        int matTotal = baseMatService.selectBaseMatTotal();
        statsMap.put("matTotal", Long.parseLong(String.valueOf(matTotal)));
        Date nowDate = DateUtils.getNowDate();
        //入库单数
        Map<String, Long> inOrderStats = stockInOrderService.selectStockInOrderTotal(nowDate);
        statsMap.putAll(inOrderStats);
        //入库退货单数
        Map<String, Long> inReturnStats = stockInReturnService.selectStockInReturnTotal(nowDate);
        statsMap.putAll(inReturnStats);
        //出库单数
        Map<String, Long> outOrderStats = stockOutOrderService.selectStockOutOrderTotal(nowDate);
        statsMap.putAll(outOrderStats);
        //出库退货单数
        Map<String, Long> outReturnStats = stockOutReturnService.selectStockOutReturnTotal(nowDate);
        statsMap.putAll(outReturnStats);
        return AjaxResult.success(statsMap);
    }

    /**
     * 首页中部统计
     */
    @GetMapping("/indexMiddle")
    public AjaxResult indexMiddle() {
        Map<String, Long[]> statsMap = new HashMap<>();
        Date[] weekDay = DateUtils.getThisWeekDay();
        Long[] purchaseArr = new Long[7];
        Long[] productionArr = new Long[7];
        Long[] commonArr = new Long[7];
        Map<String, Long> inOrderStats = null;
        Map<String, Long> outOrderStats = null;
        int i = 0;
        for(Date d : weekDay){
            inOrderStats = stockInOrderService.selectStockInOrderTotal(d);
            purchaseArr[i] = inOrderStats.get("purchase");
            outOrderStats = stockOutOrderService.selectStockOutOrderTotal(d);
            productionArr[i] = outOrderStats.get("production");
            commonArr[i] = outOrderStats.get("common");
            i++;
        }
        statsMap.put("purchaseArr", purchaseArr);
        statsMap.put("productionArr", productionArr);
        statsMap.put("commonArr", commonArr);
        return AjaxResult.success(statsMap);
    }

    /**
     * 首页下部统计
     */
    @GetMapping("/indexLower")
    public AjaxResult indexLower() {
        Map<String, Long[]> statsMap = new HashMap<>();
        Date[] weekDay = DateUtils.getThisWeekDay();
        Long[] purchaseReturnArr = new Long[7];
        Long[] productionReturnArr = new Long[7];
        Long[] commonReturnArr = new Long[7];
        Map<String, Long> inReturnStats = null;
        Map<String, Long> outReturnStats = null;
        int i = 0;
        for(Date d : weekDay){
            inReturnStats = stockInReturnService.selectStockInReturnTotal(d);
            purchaseReturnArr[i] = inReturnStats.get("purchaseReturn");
            outReturnStats = stockOutReturnService.selectStockOutReturnTotal(d);
            productionReturnArr[i] = outReturnStats.get("productionReturn");
            commonReturnArr[i] = outReturnStats.get("commonReturn");
            i++;
        }
        statsMap.put("purchaseReturnArr", purchaseReturnArr);
        statsMap.put("productionReturnArr", productionReturnArr);
        statsMap.put("commonReturnArr", commonReturnArr);
        return AjaxResult.success(statsMap);
    }

    /**
     * 查询入库统计列表
     */
    @GetMapping("/stockIn")
    public TableDataInfo stockIn(String matCode, String matName) {
        startPage();
        List<StockInStats> list = stockInOrderService.selectStockInStatsList(matCode, matName);
        return getDataTable(list);
    }

    /**
     * 查询出库统计列表
     */
    @GetMapping("/stockOut")
    public TableDataInfo stockOut(String matCode, String matName) {
        startPage();
        List<StockOutStats> list = stockOutOrderService.selectStockOutStatsList(matCode, matName);
        return getDataTable(list);
    }

    /**
     * 查询库存操作统计列表
     */
    @GetMapping("/stockRecord")
    public TableDataInfo stockRecord(StockRecordStats recordStats) {
        startPage();
        List<StockRecordStats> list = stockRecordService.statsStockRecord(recordStats);
        return getDataTable(list);
    }

    /**
     * 导出入库单统计
     */
    @PreAuthorize("@ss.hasPermi('stock:inOrder:export')")
    @Log(title = "入库单统计", businessType = BusinessType.EXPORT)
    @PostMapping("/statsInOrderExport")
    public void statsInOrderExport(HttpServletResponse response, String matCode, String matName) {
        List<StockInStats> list = stockInOrderService.selectStockInStatsList(matCode, matName);
        ExcelUtil<StockInStats> util = new ExcelUtil<StockInStats>(StockInStats.class);
        util.exportExcel(response, list, "入库单统计数据");
    }

    /**
     * 导出出库单统计
     */
    @PreAuthorize("@ss.hasPermi('stock:outOrder:export')")
    @Log(title = "出库单统计", businessType = BusinessType.EXPORT)
    @PostMapping("/statsOutOrderExport")
    public void statsOutOrderExport(HttpServletResponse response, String matCode, String matName) {
        List<StockOutStats> list = stockOutOrderService.selectStockOutStatsList(matCode, matName);
        ExcelUtil<StockOutStats> util = new ExcelUtil<StockOutStats>(StockOutStats.class);
        util.exportExcel(response, list, "出库单统计数据");
    }

    /**
     * 导出库存操作统计
     */
    @PreAuthorize("@ss.hasPermi('stock:record:export')")
    @Log(title = "库存操作统计", businessType = BusinessType.EXPORT)
    @PostMapping("/statsRecordExport")
    public void statsRecordExport(HttpServletResponse response, StockRecordStats recordStats) {
        List<StockRecordStats> list = stockRecordService.statsStockRecord(recordStats);
        ExcelUtil<StockRecordStats> util = new ExcelUtil<StockRecordStats>(StockRecordStats.class);
        util.exportExcel(response, list, "出库单统计数据");
    }

}
