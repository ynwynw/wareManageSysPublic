package com.ruoyi.web.controller.stock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.bean.InDetailPdfData;
import com.ruoyi.common.bean.InOrderPdfData;
import com.ruoyi.common.bean.MatLabelPdfData;
import com.ruoyi.common.bean.typeEnum.InOrderCheckStatusEnum;
import com.ruoyi.common.bean.typeEnum.InOrderTypeEnum;
import com.ruoyi.common.bean.typeEnum.OrderStatusEnum;
import com.ruoyi.common.service.PDFService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.stock.domain.StockInDetail;
import com.ruoyi.stock.domain.StockMatLabel;
import com.ruoyi.stock.service.IStockInDetailService;
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
import com.ruoyi.stock.domain.StockInOrder;
import com.ruoyi.stock.service.IStockInOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 入库单Controller
 *
 * @author ruoyi
 * @date 2022-07-25
 */
@RestController
@RequestMapping("/stock/inOrder")
public class StockInOrderController extends BaseController {
    @Autowired
    private IStockInOrderService stockInOrderService;
    @Autowired
    private IStockInDetailService stockInDetailService;
    @Autowired
    private ISysDictDataService sysDictDataService;
    @Autowired
    private PDFService pdfService;

    private static final String dictType = "base_mat_unit";

    /**
     * 查询入库单列表
     */
    @PreAuthorize("@ss.hasPermi('stock:inOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockInOrder stockInOrder) {
        startPage();
        List<StockInOrder> list = stockInOrderService.selectStockInOrderList(stockInOrder);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockInOrder order : list){
                order.setOrderTypeLabel(InOrderTypeEnum.getLabel(order.getOrderType()));
                order.setOrderStatusLabel(OrderStatusEnum.getLabel(order.getOrderStatus()));
                order.setCheckStatusLabel(InOrderCheckStatusEnum.getLabel(order.getCheckStatus()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出入库单列表
     */
    @PreAuthorize("@ss.hasPermi('stock:inOrder:export')")
    @Log(title = "入库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockInOrder stockInOrder) {
        List<StockInOrder> list = stockInOrderService.selectStockInOrderList(stockInOrder);
        ExcelUtil<StockInOrder> util = new ExcelUtil<StockInOrder>(StockInOrder.class);
        util.exportExcel(response, list, "入库单数据");
    }

    /**
     * 获取入库单详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:inOrder:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId) {
        StockInOrder order = stockInOrderService.selectStockInOrderByOrderId(orderId);
        if(order != null){
            order.setOrderTypeLabel(InOrderTypeEnum.getLabel(order.getOrderType()));
            order.setDetailList(stockInDetailService.selectStockInDetailListByOrderNo(order.getOrderNo()));
        }
        return AjaxResult.success(order);
    }

    /**
     * 获取入库单详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:inOrder:query')")
    @GetMapping(value = "/m/{orderNo}")
    public AjaxResult getInfo(@PathVariable("orderNo") String orderNo) {
        StockInOrder order = stockInOrderService.selectStockInOrderByOrderNo(orderNo);
        if(order == null){
            return AjaxResult.error("入库单信息不正确");
        }
        if(InOrderCheckStatusEnum.UN_CHECKOUT.getValue().equals(order.getCheckStatus())){
            return AjaxResult.error("入库单未质检");
        }
        List<StockInDetail> detailList = stockInDetailService.selectStockInDetailListByOrderNo(order.getOrderNo());
        if(CollectionUtils.isEmpty(detailList)){
            return AjaxResult.error("系统繁忙，请稍后再试！");
        }
        //过滤已完成的详情
        detailList = detailList.stream().filter(item -> item.getQualifiedQuantity().compareTo(item.getStockInQuantity()) == 1).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(detailList)){
            return AjaxResult.error("入库单已完成");
        }
        for(StockInDetail detail : detailList){
            detail.setUnitName(sysDictDataService.selectDictLabel(dictType, detail.getUnitCode()));
        }
        order.setOrderTypeLabel(InOrderTypeEnum.getLabel(order.getOrderType()));
        order.setDetailList(detailList);
        return AjaxResult.success(order);
    }

    /**
     * 新增入库单
     */
    @PreAuthorize("@ss.hasPermi('stock:inOrder:add')")
    @Log(title = "入库单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockInOrder stockInOrder) {
        if(stockInOrder == null || CollectionUtils.isEmpty(stockInOrder.getDetailList())){
            return error("系统繁忙，请稍后再试！");
        }
        return toAjax(stockInOrderService.insertStockInOrder(getUsername(), stockInOrder));
    }

    /**
     * 修改入库单
     */
    @PreAuthorize("@ss.hasPermi('stock:inOrder:edit')")
    @Log(title = "入库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockInOrder stockInOrder) {
        return toAjax(stockInOrderService.updateStockInOrder(stockInOrder));
    }

    /**
     * 删除入库单
     */
    @PreAuthorize("@ss.hasPermi('stock:inOrder:remove')")
    @Log(title = "入库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds) {
        return toAjax(stockInOrderService.deleteStockInOrderByOrderIds(orderIds));
    }

    /**
     * 打印入库单
     */
    @GetMapping("printInOrder/{orderId}")
    public void printInOrder(HttpServletResponse response, @PathVariable Long orderId) throws Exception{
        OutputStream out = response.getOutputStream();
        StockInOrder order = stockInOrderService.selectStockInOrderByOrderId(orderId);
        if(order != null){
            //更新入库单状态
            order.setOrderStatus(OrderStatusEnum.PRINTED.getValue());
            order.setUpdateBy(getUsername());
            order.setUpdateTime(DateUtils.getNowDate());
            stockInOrderService.updateStockInOrder(order);
            //入库单
            InOrderPdfData orderPdfData = new InOrderPdfData();
            BeanUtils.copyBeanProp(orderPdfData, order);
            //入库单详情
            List<InDetailPdfData> detailPdfDataList = new ArrayList<>();
            InDetailPdfData detailPdfData = null;
            List<StockInDetail> detailList = stockInDetailService.selectStockInDetailListByOrderNo(order.getOrderNo());
            if(CollectionUtils.isNotEmpty(detailList)){
                for(StockInDetail detail : detailList){
                    detailPdfData = new InDetailPdfData();
                    BeanUtils.copyBeanProp(detailPdfData, detail);
                    detailPdfData.setUnitName(sysDictDataService.selectDictLabel(dictType, detail.getUnitCode()));
                    detailPdfDataList.add(detailPdfData);
                }
            }
            InputStream in = new FileInputStream(new File(pdfService.printInOrder(orderPdfData, detailPdfDataList)));
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            response.setContentType("application/pdf");
            in.close();
        }
    }

    /**
     * 入库单质检
     */
    @Log(title = "入库单", businessType = BusinessType.UPDATE)
    @PutMapping("check")
    public AjaxResult check(@RequestBody List<StockInDetail> detailList) {
        if(CollectionUtils.isEmpty(detailList)){
            return error("系统繁忙，请稍后再试！");
        }
        return toAjax(stockInOrderService.inOrderCheck(getUsername(), detailList));
    }

    /**
     * 扫码提交入库单-入库
     */
    @PostMapping("submitStockIn")
    public AjaxResult submitStockIn(@RequestBody StockInOrder stockInOrder){
        if(stockInOrder == null){
            return error("系统繁忙，请稍后再试！");
        }
        return stockInOrderService.submitStockIn(getUsername(), stockInOrder);
    }

}
