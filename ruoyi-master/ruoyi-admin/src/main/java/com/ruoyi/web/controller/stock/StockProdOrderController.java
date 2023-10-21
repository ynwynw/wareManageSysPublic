package com.ruoyi.web.controller.stock;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.service.IBaseWorkshopService;
import com.ruoyi.common.bean.typeEnum.ProdOrderStatusEnum;
import com.ruoyi.common.utils.OrderNoUtil;
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
import com.ruoyi.stock.domain.StockProdOrder;
import com.ruoyi.stock.service.IStockProdOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 生产订单Controller
 *
 * @author ruoyi
 * @date 2022-07-25
 */
@RestController
@RequestMapping("/stock/prodOrder")
public class StockProdOrderController extends BaseController {
    @Autowired
    private IStockProdOrderService stockProdOrderService;
    @Autowired
    private IBaseWorkshopService baseWorkshopService;

    /**
     * 查询生产订单列表
     */
    @PreAuthorize("@ss.hasPermi('stock:prodOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockProdOrder stockProdOrder) {
        startPage();
        List<StockProdOrder> list = stockProdOrderService.selectStockProdOrderList(stockProdOrder);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockProdOrder prodOrder : list){
                prodOrder.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(prodOrder.getWorkshopCode()));
                prodOrder.setOrderStatusLabel(ProdOrderStatusEnum.getLabel(prodOrder.getOrderStatus()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出生产订单列表
     */
    @PreAuthorize("@ss.hasPermi('stock:prodOrder:export')")
    @Log(title = "生产订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockProdOrder stockProdOrder) {
        List<StockProdOrder> list = stockProdOrderService.selectStockProdOrderList(stockProdOrder);
        ExcelUtil<StockProdOrder> util = new ExcelUtil<StockProdOrder>(StockProdOrder.class);
        util.exportExcel(response, list, "生产订单数据");
    }

    /**
     * 获取生产订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:prodOrder:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId) {
        return AjaxResult.success(stockProdOrderService.selectStockProdOrderByOrderId(orderId));
    }

    /**
     * 新增生产订单
     */
    @PreAuthorize("@ss.hasPermi('stock:prodOrder:add')")
    @Log(title = "生产订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockProdOrder stockProdOrder) {
        stockProdOrder.setOrderNo(OrderNoUtil.generateUniqueKey(OrderNoUtil.PROD_PREFIX));
        stockProdOrder.setOrderStatus(ProdOrderStatusEnum.ONGOING.getValue());
        stockProdOrder.setCreateBy(getUsername());
        return toAjax(stockProdOrderService.insertStockProdOrder(stockProdOrder));
    }

    /**
     * 修改生产订单
     */
    @PreAuthorize("@ss.hasPermi('stock:prodOrder:edit')")
    @Log(title = "生产订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockProdOrder stockProdOrder) {
        return toAjax(stockProdOrderService.updateStockProdOrder(stockProdOrder));
    }

    /**
     * 删除生产订单
     */
    @PreAuthorize("@ss.hasPermi('stock:prodOrder:remove')")
    @Log(title = "生产订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds) {
        return toAjax(stockProdOrderService.deleteStockProdOrderByOrderIds(orderIds));
    }
}
