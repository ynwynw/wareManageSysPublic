package com.ruoyi.web.controller.stock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.domain.BaseMatBom;
import com.ruoyi.base.service.IBaseMatBomService;
import com.ruoyi.base.service.IBaseMatService;
import com.ruoyi.base.service.IBaseWarehouseService;
import com.ruoyi.base.service.IBaseWorkshopService;
import com.ruoyi.common.bean.*;
import com.ruoyi.common.bean.request.StockOutRequestBody;
import com.ruoyi.common.bean.typeEnum.OrderStatusEnum;
import com.ruoyi.common.bean.typeEnum.OutOrderTypeEnum;
import com.ruoyi.common.service.PDFService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.stock.domain.StockInfo;
import com.ruoyi.stock.domain.StockOutDetail;
import com.ruoyi.stock.domain.StockOutReturnDetail;
import com.ruoyi.stock.service.IStockInfoService;
import com.ruoyi.stock.service.IStockOutDetailService;
import com.ruoyi.system.domain.SysUserClass;
import com.ruoyi.system.service.ISysDictDataService;
import com.ruoyi.system.service.ISysUserClassService;
import com.ruoyi.web.utils.MatBomTreeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.ruoyi.stock.domain.StockOutOrder;
import com.ruoyi.stock.service.IStockOutOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 出库单Controller
 *
 * @author ruoyi
 * @date 2022-07-25
 */
@RestController
@RequestMapping("/stock/outOrder")
public class StockOutOrderController extends BaseController {
    @Autowired
    private IStockOutOrderService stockOutOrderService;
    @Autowired
    private IStockOutDetailService stockOutDetailService;
    @Autowired
    private ISysDictDataService sysDictDataService;
    @Autowired
    private IBaseMatBomService baseMatBomService;
    @Autowired
    private IBaseMatService baseMatService;
    @Autowired
    private IBaseWorkshopService baseWorkshopService;
    @Autowired
    private IBaseWarehouseService baseWarehouseService;
    @Autowired
    private IStockInfoService stockInfoService;
    @Autowired
    private ISysUserClassService sysUserClassService;
    @Autowired
    private PDFService pdfService;

    private static final String dictType = "base_mat_unit";

    /**
     * 查询出库单列表
     */
    @PreAuthorize("@ss.hasPermi('stock:outOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockOutOrder stockOutOrder) {
        startPage();
        List<StockOutOrder> list = stockOutOrderService.selectStockOutOrderList(stockOutOrder);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockOutOrder order : list){
                order.setOrderTypeLabel(OutOrderTypeEnum.getLabel(order.getOrderType()));
                order.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getWarehouseCode()));
                order.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(order.getWorkshopCode()));
                order.setOrderStatusLabel(OrderStatusEnum.getLabel(order.getOrderStatus()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出出库单列表
     */
    @PreAuthorize("@ss.hasPermi('stock:outOrder:export')")
    @Log(title = "出库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockOutOrder stockOutOrder) {
        List<StockOutOrder> list = stockOutOrderService.selectStockOutOrderList(stockOutOrder);
        ExcelUtil<StockOutOrder> util = new ExcelUtil<StockOutOrder>(StockOutOrder.class);
        util.exportExcel(response, list, "出库单数据");
    }

    /**
     * 获取出库单详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:outOrder:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId) {
        StockOutOrder order = stockOutOrderService.selectStockOutOrderByOrderId(orderId);
        if(order != null){
            String warehouseCode = order.getWarehouseCode();
            order.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(warehouseCode));
            order.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(order.getWorkshopCode()));
            List<StockOutDetail> detailList = stockOutDetailService.selectStockOutDetailListByOrderNo(order.getOrderNo());
            if(CollectionUtils.isNotEmpty(detailList)){
                //设置推荐货位
                for(StockOutDetail detail : detailList){
                    String recommendLocation = stockInfoService.selectRecommendLocation(detail.getMatCode(), warehouseCode);
                    if(StringUtils.isEmpty(recommendLocation)){
                        recommendLocation = "暂无库存";
                    }
                    detail.setLocationCode(recommendLocation);
                    stockOutDetailService.updateStockOutDetail(detail);
                }
            }
            order.setDetailList(detailList);
        }
        return AjaxResult.success(order);
    }

    /**
     * 获取出库单详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:outOrder:query')")
    @GetMapping(value = "/m/{orderNo}")
    public AjaxResult getInfo(@PathVariable("orderNo") String orderNo) {
        StockOutOrder order = stockOutOrderService.selectStockOutOrderByOrderNo(orderNo);
        if(order == null){
            return AjaxResult.error("出库单信息不正确");
        }
        List<StockOutDetail> detailList = stockOutDetailService.selectStockOutDetailListByOrderNo(order.getOrderNo());
        if(CollectionUtils.isEmpty(detailList)){
            return AjaxResult.error("系统繁忙，请稍后再试！");
        }
        //过滤已完成的详情
        detailList = detailList.stream().filter(item -> item.getQuantity().compareTo(item.getReceivedQuantity()) == 1).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(detailList)){
            return AjaxResult.error("出库单已完成");
        }
        order.setOrderTypeLabel(OutOrderTypeEnum.getLabel(order.getOrderType()));
        order.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getWarehouseCode()));
        order.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(order.getWorkshopCode()));
        order.setDetailList(detailList);
        return AjaxResult.success(order);
    }

    /**
     * 新增出库单
     */
    @PreAuthorize("@ss.hasPermi('stock:outOrder:add')")
    @Log(title = "出库单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockOutOrder stockOutOrder) {
        if(stockOutOrder == null || CollectionUtils.isEmpty(stockOutOrder.getDetailList())){
            return error("系统繁忙，请稍后再试！");
        }
        return toAjax(stockOutOrderService.insertStockOutOrder(getUsername(), stockOutOrder));
    }

    /**
     * 修改出库单
     */
    @PreAuthorize("@ss.hasPermi('stock:outOrder:edit')")
    @Log(title = "出库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockOutOrder stockOutOrder) {
        return toAjax(stockOutOrderService.updateStockOutOrder(stockOutOrder));
    }

    /**
     * 删除出库单
     */
    @PreAuthorize("@ss.hasPermi('stock:outOrder:remove')")
    @Log(title = "出库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds) {
        return toAjax(stockOutOrderService.deleteStockOutOrderByOrderIds(orderIds));
    }

    /**
     * 查询出库单所需物料列表
     */
    @GetMapping("/matBomList")
    public List<StockOutDetail> matBomList(StockOutOrder stockOutOrder) {
        List<BaseMatBom> baseMatBomList = baseMatBomService.selectBaseMatBomByFatherMatCode(stockOutOrder.getMatCode());
        List<StockOutDetail> stockOutDetailList = MatBomTreeUtil.getStockOutDetailList(
                baseMatBomList, baseMatBomService, baseMatService, sysDictDataService);
        if(CollectionUtils.isNotEmpty(stockOutDetailList)){
            //获取库管员管理的分类
            List<SysUserClass> userClassList = sysUserClassService.selectSysUserClassListByUserName(stockOutOrder.getWarehouseKeeper());
            List<String> classList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(userClassList)){
                classList = userClassList.stream().map(SysUserClass::getClassCode).distinct().collect(Collectors.toList());
            }
            //去除非库管员管理物料
            Iterator<StockOutDetail> iterator = stockOutDetailList.iterator();
            StockOutDetail detail = null;
            while (iterator.hasNext()) {
                detail = iterator.next();
                if(!classList.contains(detail.getMatClass())){
                    iterator.remove();
                    continue;
                }
            }
        }
        return stockOutDetailList;
    }

    /**
     * 打印出库单
     */
    @GetMapping("printOutOrder/{orderId}")
    public void printOutOrder(HttpServletResponse response, @PathVariable Long orderId) throws Exception{
        OutputStream out = response.getOutputStream();
        StockOutOrder order = stockOutOrderService.selectStockOutOrderByOrderId(orderId);
        if(order != null){
            //更新出库单状态
            order.setOrderStatus(OrderStatusEnum.PRINTED.getValue());
            order.setUpdateBy(getUsername());
            order.setUpdateTime(DateUtils.getNowDate());
            stockOutOrderService.updateStockOutOrder(order);
            //出库单
            OutOrderPdfData orderPdfData = new OutOrderPdfData();
            BeanUtils.copyBeanProp(orderPdfData, order);
            orderPdfData.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(order.getWarehouseCode()));
            orderPdfData.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(order.getWorkshopCode()));
            //出库单详情
            List<OutDetailPdfData> detailPdfDataList = new ArrayList<>();
            OutDetailPdfData detailPdfData = null;
            List<StockOutDetail> detailList = stockOutDetailService.selectStockOutDetailListByOrderNo(order.getOrderNo());
            if(CollectionUtils.isNotEmpty(detailList)){
                for(StockOutDetail detail : detailList){
                    detailPdfData = new OutDetailPdfData();
                    BeanUtils.copyBeanProp(detailPdfData, detail);
                    detailPdfData.setUnitName(sysDictDataService.selectDictLabel(dictType, detail.getUnitCode()));
                    detailPdfDataList.add(detailPdfData);
                }
            }
            InputStream in = new FileInputStream(new File(pdfService.printOutOrder(orderPdfData, detailPdfDataList)));
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
     * 扫码提交出库单-出库
     */
    @PostMapping("submitStockOut")
    public AjaxResult submitStockOut(@RequestBody StockOutRequestBody stockOutRequestBody){
        if(stockOutRequestBody == null){
            return error("系统繁忙，请稍后再试！");
        }
        return stockOutOrderService.submitStockOut(getUsername(), stockOutRequestBody);
    }

}
