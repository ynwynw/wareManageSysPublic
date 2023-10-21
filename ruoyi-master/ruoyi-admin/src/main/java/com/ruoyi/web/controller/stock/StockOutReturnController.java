package com.ruoyi.web.controller.stock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.service.IBaseWarehouseService;
import com.ruoyi.base.service.IBaseWorkshopService;
import com.ruoyi.common.bean.InReturnDetailPdfData;
import com.ruoyi.common.bean.InReturnPdfData;
import com.ruoyi.common.bean.OutReturnDetailPdfData;
import com.ruoyi.common.bean.OutReturnPdfData;
import com.ruoyi.common.bean.typeEnum.OrderStatusEnum;
import com.ruoyi.common.bean.typeEnum.OutOrderReturnTypeEnum;
import com.ruoyi.common.service.PDFService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.stock.domain.*;
import com.ruoyi.stock.service.IStockInReturnDetailService;
import com.ruoyi.stock.service.IStockOutReturnDetailService;
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
import com.ruoyi.stock.service.IStockOutReturnService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 出库单退货Controller
 *
 * @author ruoyi
 * @date 2022-07-25
 */
@RestController
@RequestMapping("/stock/outReturn")
public class StockOutReturnController extends BaseController {
    @Autowired
    private IStockOutReturnService stockOutReturnService;
    @Autowired
    private IStockOutReturnDetailService stockOutReturnDetailService;
    @Autowired
    private IBaseWarehouseService baseWarehouseService;
    @Autowired
    private IBaseWorkshopService baseWorkshopService;
    @Autowired
    private ISysDictDataService sysDictDataService;
    @Autowired
    private PDFService pdfService;

    private static final String dictType = "base_mat_unit";

    /**
     * 查询出库单退货列表
     */
    @PreAuthorize("@ss.hasPermi('stock:outReturn:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockOutReturn stockOutReturn) {
        startPage();
        List<StockOutReturn> list = stockOutReturnService.selectStockOutReturnList(stockOutReturn);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockOutReturn outReturn : list){
                outReturn.setReturnTypeLabel(OutOrderReturnTypeEnum.getLabel(outReturn.getReturnType()));
                outReturn.setReturnStatusLabel(OrderStatusEnum.getLabel(outReturn.getReturnStatus()));
                outReturn.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(outReturn.getWarehouseCode()));
                outReturn.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(outReturn.getWorkshopCode()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出出库单退货列表
     */
    @PreAuthorize("@ss.hasPermi('stock:outReturn:export')")
    @Log(title = "出库单退货", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockOutReturn stockOutReturn) {
        List<StockOutReturn> list = stockOutReturnService.selectStockOutReturnList(stockOutReturn);
        ExcelUtil<StockOutReturn> util = new ExcelUtil<StockOutReturn>(StockOutReturn.class);
        util.exportExcel(response, list, "出库单退货数据");
    }

    /**
     * 获取出库单退货详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:outReturn:query')")
    @GetMapping(value = "/{returnId}")
    public AjaxResult getInfo(@PathVariable("returnId") Long returnId) {
        StockOutReturn outReturn = stockOutReturnService.selectStockOutReturnByReturnId(returnId);
        if(outReturn != null){
            outReturn.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(outReturn.getWarehouseCode()));
            outReturn.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(outReturn.getWorkshopCode()));
            outReturn.setReturnTypeLabel(OutOrderReturnTypeEnum.getLabel(outReturn.getReturnType()));
            List<StockOutReturnDetail> detailList = stockOutReturnDetailService.selectStockOutReturnDetailListByReturnNo(outReturn.getReturnNo());
            outReturn.setDetailList(detailList);
        }
        return AjaxResult.success(outReturn);
    }

    /**
     * 获取出库单退货详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:outReturn:query')")
    @GetMapping(value = "/m/{returnNo}")
    public AjaxResult getInfo(@PathVariable("returnNo") String returnNo) {
        StockOutReturn outReturn = stockOutReturnService.selectStockOutReturnByReturnNo(returnNo);
        if(outReturn == null){
            return AjaxResult.error("出库退货单信息不正确");
        }
        List<StockOutReturnDetail> detailList = stockOutReturnDetailService.selectStockOutReturnDetailListByReturnNo(outReturn.getReturnNo());
        if(CollectionUtils.isEmpty(detailList)){
            return AjaxResult.error("系统繁忙，请稍后再试！");
        }
        //过滤已完成的详情
        detailList = detailList.stream().filter(item -> item.getQuantity().compareTo(item.getReturnQuantity()) == 1).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(detailList)){
            return AjaxResult.error("出库退货单已完成");
        }
        outReturn.setReturnTypeLabel(OutOrderReturnTypeEnum.getLabel(outReturn.getReturnType()));
        outReturn.setDetailList(detailList);
        return AjaxResult.success(outReturn);
    }

    /**
     * 新增出库单退货
     */
    @PreAuthorize("@ss.hasPermi('stock:outReturn:add')")
    @Log(title = "出库单退货", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockOutReturn stockOutReturn) {
        if(stockOutReturn == null || CollectionUtils.isEmpty(stockOutReturn.getDetailList())){
            return error("系统繁忙，请稍后再试！");
        }
        return toAjax(stockOutReturnService.insertStockOutReturn(getUsername(), stockOutReturn));
    }

    /**
     * 修改出库单退货
     */
    @PreAuthorize("@ss.hasPermi('stock:outReturn:edit')")
    @Log(title = "出库单退货", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockOutReturn stockOutReturn) {
        return toAjax(stockOutReturnService.updateStockOutReturn(stockOutReturn));
    }

    /**
     * 删除出库单退货
     */
    @PreAuthorize("@ss.hasPermi('stock:outReturn:remove')")
    @Log(title = "出库单退货", businessType = BusinessType.DELETE)
    @DeleteMapping("/{returnIds}")
    public AjaxResult remove(@PathVariable Long[] returnIds) {
        return toAjax(stockOutReturnService.deleteStockOutReturnByReturnIds(returnIds));
    }

    /**
     * 打印出库退货单
     */
    @GetMapping("printOutOrderReturn/{returnId}")
    public void printInOrder(HttpServletResponse response, @PathVariable Long returnId) throws Exception{
        OutputStream out = response.getOutputStream();
        StockOutReturn outReturn = stockOutReturnService.selectStockOutReturnByReturnId(returnId);
        if(outReturn != null){
            //更新出库退货单状态
            outReturn.setReturnStatus(OrderStatusEnum.PRINTED.getValue());
            outReturn.setUpdateBy(getUsername());
            outReturn.setUpdateTime(DateUtils.getNowDate());
            stockOutReturnService.updateStockOutReturn(outReturn);
            //出库退货单
            OutReturnPdfData returnPdfData = new OutReturnPdfData();
            BeanUtils.copyBeanProp(returnPdfData, outReturn);
            returnPdfData.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(outReturn.getWarehouseCode()));
            returnPdfData.setWorkshopName(baseWorkshopService.selectBaseWorkshopByWorkshopCode(outReturn.getWorkshopCode()));
            //出库退货单详情
            List<OutReturnDetailPdfData> detailPdfDataList = new ArrayList<>();
            OutReturnDetailPdfData detailPdfData = null;
            List<StockOutReturnDetail> detailList = stockOutReturnDetailService.selectStockOutReturnDetailListByReturnNo(outReturn.getReturnNo());
            if(CollectionUtils.isNotEmpty(detailList)){
                for(StockOutReturnDetail detail : detailList){
                    detailPdfData = new OutReturnDetailPdfData();
                    BeanUtils.copyBeanProp(detailPdfData, detail);
                    detailPdfData.setUnitName(sysDictDataService.selectDictLabel(dictType, detail.getUnitCode()));
                    detailPdfDataList.add(detailPdfData);
                }
            }
            InputStream in = new FileInputStream(new File(pdfService.printOutOrderReturn(returnPdfData, detailPdfDataList)));
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
     * 扫码提交出库单退货-退货
     */
    @PostMapping("submitOutReturn")
    public AjaxResult submitOutReturn(@RequestBody StockOutReturn stockOutReturn){
        if(stockOutReturn == null){
            return error("系统繁忙，请稍后再试！");
        }
        return stockOutReturnService.submitOutReturn(getUsername(), stockOutReturn);
    }

}
