package com.ruoyi.web.controller.stock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.service.IBaseWarehouseService;
import com.ruoyi.common.bean.AllotDetailPdfData;
import com.ruoyi.common.bean.AllotOrderPdfData;
import com.ruoyi.common.bean.OutDetailPdfData;
import com.ruoyi.common.bean.OutOrderPdfData;
import com.ruoyi.common.bean.request.StockOutRequestBody;
import com.ruoyi.common.bean.typeEnum.AllotProgressEnum;
import com.ruoyi.common.bean.typeEnum.OrderStatusEnum;
import com.ruoyi.common.service.PDFService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.stock.domain.StockAllotDetail;
import com.ruoyi.stock.domain.StockOutDetail;
import com.ruoyi.stock.domain.StockOutOrder;
import com.ruoyi.stock.service.IStockAllotDetailService;
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
import com.ruoyi.stock.domain.StockAllotOrder;
import com.ruoyi.stock.service.IStockAllotOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 调拨单Controller
 *
 * @author ruoyi
 * @date 2022-08-05
 */
@RestController
@RequestMapping("/stock/allotOrder")
public class StockAllotOrderController extends BaseController {
    @Autowired
    private IStockAllotOrderService stockAllotOrderService;
    @Autowired
    private IStockAllotDetailService stockAllotDetailService;
    @Autowired
    private IBaseWarehouseService baseWarehouseService;
    @Autowired
    private ISysDictDataService sysDictDataService;
    @Autowired
    private PDFService pdfService;

    private static final String dictType = "base_mat_unit";

    /**
     * 查询调拨单列表
     */
    @PreAuthorize("@ss.hasPermi('stock:allotOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockAllotOrder stockAllotOrder) {
        startPage();
        List<StockAllotOrder> list = stockAllotOrderService.selectStockAllotOrderList(stockAllotOrder);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockAllotOrder order : list){
                order.setSrcWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getSrcWarehouseCode()));
                order.setDestWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getDestWarehouseCode()));
                order.setAllotStatusLabel(OrderStatusEnum.getLabel(order.getAllotStatus()));
                order.setAllotProgressLabel(AllotProgressEnum.getLabel(order.getAllotProgress()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出调拨单列表
     */
    @PreAuthorize("@ss.hasPermi('stock:allotOrder:export')")
    @Log(title = "调拨单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockAllotOrder stockAllotOrder) {
        List<StockAllotOrder> list = stockAllotOrderService.selectStockAllotOrderList(stockAllotOrder);
        ExcelUtil<StockAllotOrder> util = new ExcelUtil<StockAllotOrder>(StockAllotOrder.class);
        util.exportExcel(response, list, "调拨单数据");
    }

    /**
     * 获取调拨单详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:allotOrder:query')")
    @GetMapping(value = "/{allotId}")
    public AjaxResult getInfo(@PathVariable("allotId") Long allotId) {
        StockAllotOrder order = stockAllotOrderService.selectStockAllotOrderByAllotId(allotId);
        if(order != null){
            order.setSrcWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getSrcWarehouseCode()));
            order.setDestWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getDestWarehouseCode()));
            order.setAllotProgressLabel(AllotProgressEnum.getLabel(order.getAllotProgress()));
            List<StockAllotDetail> detailList = stockAllotDetailService.selectStockAllotDetailListByAllotNo(order.getAllotNo());
            order.setDetailList(detailList);
        }
        return AjaxResult.success(order);
    }

    /**
     * 获取调拨单详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:allotOrder:query')")
    @GetMapping(value = "/m/{allotNo}")
    public AjaxResult getInfo(@PathVariable("allotNo") String allotNo) {
        StockAllotOrder order = stockAllotOrderService.selectStockAllotOrderByAllotNo(allotNo);
        if(order == null){
            return AjaxResult.error("调拨单信息不正确");
        }
        if(AllotProgressEnum.RECEIVE.getValue().equals(order.getAllotProgress())){
            return AjaxResult.error("调拨单已完成");
        }
        order.setSrcWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getSrcWarehouseCode()));
        order.setDestWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getDestWarehouseCode()));
        order.setAllotProgressLabel(AllotProgressEnum.getLabel(order.getAllotProgress()));
        List<StockAllotDetail> detailList = stockAllotDetailService.selectStockAllotDetailListByAllotNo(order.getAllotNo());
        if(CollectionUtils.isNotEmpty(detailList)){
            for(StockAllotDetail detail : detailList){
                detail.setUnitName(sysDictDataService.selectDictLabel(dictType, detail.getUnitCode()));
            }
        }
        order.setDetailList(detailList);
        return AjaxResult.success(order);
    }

    /**
     * 新增调拨单
     */
    @PreAuthorize("@ss.hasPermi('stock:allotOrder:add')")
    @Log(title = "调拨单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockAllotOrder stockAllotOrder) {
        return toAjax(stockAllotOrderService.insertStockAllotOrder(getUsername(), stockAllotOrder));
    }

    /**
     * 修改调拨单
     */
    @PreAuthorize("@ss.hasPermi('stock:allotOrder:edit')")
    @Log(title = "调拨单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockAllotOrder stockAllotOrder) {
        return toAjax(stockAllotOrderService.updateStockAllotOrder(stockAllotOrder));
    }

    /**
     * 删除调拨单
     */
    @PreAuthorize("@ss.hasPermi('stock:allotOrder:remove')")
    @Log(title = "调拨单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{allotIds}")
    public AjaxResult remove(@PathVariable Long[] allotIds) {
        return toAjax(stockAllotOrderService.deleteStockAllotOrderByAllotIds(allotIds));
    }

    /**
     * 打印调拨单
     */
    @GetMapping("printAllotOrder/{allotId}")
    public void printAllotOrder(HttpServletResponse response, @PathVariable Long allotId) throws Exception{
        OutputStream out = response.getOutputStream();
        StockAllotOrder order = stockAllotOrderService.selectStockAllotOrderByAllotId(allotId);
        if(order != null){
            //更新调拨单状态
            order.setAllotStatus(OrderStatusEnum.PRINTED.getValue());
            order.setUpdateBy(getUsername());
            order.setUpdateTime(DateUtils.getNowDate());
            stockAllotOrderService.updateStockAllotOrder(order);
            //调拨单
            AllotOrderPdfData orderPdfData = new AllotOrderPdfData();
            BeanUtils.copyBeanProp(orderPdfData, order);
            orderPdfData.setSrcWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getSrcWarehouseCode()));
            orderPdfData.setDestWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getDestWarehouseCode()));
            //出库单详情
            List<AllotDetailPdfData> detailPdfDataList = new ArrayList<>();
            AllotDetailPdfData detailPdfData = null;
            List<StockAllotDetail> detailList = stockAllotDetailService.selectStockAllotDetailListByAllotNo(order.getAllotNo());
            if(CollectionUtils.isNotEmpty(detailList)){
                for(StockAllotDetail detail : detailList){
                    detailPdfData = new AllotDetailPdfData();
                    BeanUtils.copyBeanProp(detailPdfData, detail);
                    detailPdfData.setUnitName(sysDictDataService.selectDictLabel(dictType, detail.getUnitCode()));
                    detailPdfDataList.add(detailPdfData);
                }
            }
            InputStream in = new FileInputStream(new File(pdfService.printAllotOrder(orderPdfData, detailPdfDataList)));
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
     * 扫码提交调拨单
     */
    @PostMapping("submitAllot")
    public AjaxResult submitAllot(@RequestBody StockAllotOrder stockAllotOrder){
        if(stockAllotOrder == null){
            return error("系统繁忙，请稍后再试！");
        }
        String allotProgress = stockAllotOrder.getAllotProgress();
        if(AllotProgressEnum.CREATED.getValue().equals(allotProgress)){
            return stockAllotOrderService.submitAllotPicking(getUsername(), stockAllotOrder);
        }else if(AllotProgressEnum.PICKING.getValue().equals(allotProgress)){
            return stockAllotOrderService.submitAllotReceive(getUsername(), stockAllotOrder);
        }
        return error("系统繁忙，请稍后再试！");
    }

}
