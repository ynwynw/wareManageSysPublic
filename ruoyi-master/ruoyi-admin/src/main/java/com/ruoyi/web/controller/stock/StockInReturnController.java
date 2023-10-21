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
import com.ruoyi.common.bean.InReturnDetailPdfData;
import com.ruoyi.common.bean.InReturnPdfData;
import com.ruoyi.common.bean.request.InReturnRequestBody;
import com.ruoyi.common.bean.typeEnum.InOrderReturnTypeEnum;
import com.ruoyi.common.bean.typeEnum.OrderStatusEnum;
import com.ruoyi.common.service.PDFService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.stock.domain.*;
import com.ruoyi.stock.service.IStockInReturnDetailService;
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
import com.ruoyi.stock.service.IStockInReturnService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 入库单退货Controller
 *
 * @author ruoyi
 * @date 2022-07-25
 */
@RestController
@RequestMapping("/stock/inReturn")
public class StockInReturnController extends BaseController {
    @Autowired
    private IStockInReturnService stockInReturnService;
    @Autowired
    private IStockInReturnDetailService stockInReturnDetailService;
    @Autowired
    private IBaseWarehouseService baseWarehouseService;
    @Autowired
    private ISysDictDataService sysDictDataService;
    @Autowired
    private PDFService pdfService;

    private static final String dictType = "base_mat_unit";

    /**
     * 查询入库单退货列表
     */
    @PreAuthorize("@ss.hasPermi('stock:inReturn:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockInReturn stockInReturn) {
        startPage();
        List<StockInReturn> list = stockInReturnService.selectStockInReturnList(stockInReturn);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockInReturn inReturn : list){
                inReturn.setReturnTypeLabel(InOrderReturnTypeEnum.getLabel(inReturn.getReturnType()));
                inReturn.setReturnStatusLabel(OrderStatusEnum.getLabel(inReturn.getReturnStatus()));
                inReturn.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(inReturn.getWarehouseCode()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出入库单退货列表
     */
    @PreAuthorize("@ss.hasPermi('stock:inReturn:export')")
    @Log(title = "入库单退货", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockInReturn stockInReturn) {
        List<StockInReturn> list = stockInReturnService.selectStockInReturnList(stockInReturn);
        ExcelUtil<StockInReturn> util = new ExcelUtil<StockInReturn>(StockInReturn.class);
        util.exportExcel(response, list, "入库单退货数据");
    }

    /**
     * 获取入库单退货详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:inReturn:query')")
    @GetMapping(value = "/{returnId}")
    public AjaxResult getInfo(@PathVariable("returnId") Long returnId) {
        StockInReturn inReturn = stockInReturnService.selectStockInReturnByReturnId(returnId);
        if(inReturn != null){
            inReturn.setReturnTypeLabel(InOrderReturnTypeEnum.getLabel(inReturn.getReturnType()));
            inReturn.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(inReturn.getWarehouseCode()));
            List<StockInReturnDetail> detailList = stockInReturnDetailService.selectStockInReturnDetailListByReturnNo(inReturn.getReturnNo());
            inReturn.setDetailList(detailList);
        }
        return AjaxResult.success(inReturn);
    }

    /**
     * 获取入库单退货详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:inReturn:query')")
    @GetMapping(value = "/m/{returnNo}")
    public AjaxResult getInfo(@PathVariable("returnNo") String returnNo) {
        StockInReturn inReturn = stockInReturnService .selectStockInReturnByReturnNo(returnNo);
        if(inReturn == null){
            return AjaxResult.error("入库退货单信息不正确");
        }
        List<StockInReturnDetail> detailList = stockInReturnDetailService.selectStockInReturnDetailListByReturnNo(inReturn.getReturnNo());
        if(CollectionUtils.isEmpty(detailList)){
            return AjaxResult.error("系统繁忙，请稍后再试！");
        }
        //过滤已完成的详情
        detailList = detailList.stream().filter(item -> item.getQuantity().compareTo(item.getReturnQuantity()) == 1).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(detailList)){
            return AjaxResult.error("入库退货单已完成");
        }
        for(StockInReturnDetail detail : detailList){
            detail.setUnitName(sysDictDataService.selectDictLabel(dictType, detail.getUnitCode()));
        }
        inReturn.setReturnTypeLabel(InOrderReturnTypeEnum.getLabel(inReturn.getReturnType()));
        inReturn.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(inReturn.getWarehouseCode()));
        inReturn.setDetailList(detailList);
        return AjaxResult.success(inReturn);
    }

    /**
     * 新增入库单退货
     */
    @PreAuthorize("@ss.hasPermi('stock:inReturn:add')")
    @Log(title = "入库单退货", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockInReturn stockInReturn) {
        if(stockInReturn == null || CollectionUtils.isEmpty(stockInReturn.getDetailList())){
            return error("系统繁忙，请稍后再试！");
        }
        return toAjax(stockInReturnService.insertStockInReturn(getUsername(), stockInReturn));
    }

    /**
     * 修改入库单退货
     */
    @PreAuthorize("@ss.hasPermi('stock:inReturn:edit')")
    @Log(title = "入库单退货", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockInReturn stockInReturn) {
        return toAjax(stockInReturnService.updateStockInReturn(stockInReturn));
    }

    /**
     * 删除入库单退货
     */
    @PreAuthorize("@ss.hasPermi('stock:inReturn:remove')")
    @Log(title = "入库单退货", businessType = BusinessType.DELETE)
    @DeleteMapping("/{returnIds}")
    public AjaxResult remove(@PathVariable Long[] returnIds) {
        return toAjax(stockInReturnService.deleteStockInReturnByReturnIds(returnIds));
    }

    /**
     * 打印入库退货单
     */
    @GetMapping("printInOrderReturn/{returnId}")
    public void printInOrder(HttpServletResponse response, @PathVariable Long returnId) throws Exception{
        OutputStream out = response.getOutputStream();
        StockInReturn inReturn = stockInReturnService.selectStockInReturnByReturnId(returnId);
        if(inReturn != null){
            //更新入库退货单状态
            inReturn.setReturnStatus(OrderStatusEnum.PRINTED.getValue());
            inReturn.setUpdateBy(getUsername());
            inReturn.setUpdateTime(DateUtils.getNowDate());
            stockInReturnService.updateStockInReturn(inReturn);
            //入库退货单
            InReturnPdfData returnPdfData = new InReturnPdfData();
            BeanUtils.copyBeanProp(returnPdfData, inReturn);
            returnPdfData.setWarehouseName(baseWarehouseService.selectBaseWarehouseNameByWarehouseCode(inReturn.getWarehouseCode()));
            //入库退货单详情
            List<InReturnDetailPdfData> detailPdfDataList = new ArrayList<>();
            InReturnDetailPdfData detailPdfData = null;
            List<StockInReturnDetail> detailList = stockInReturnDetailService.selectStockInReturnDetailListByReturnNo(inReturn.getReturnNo());
            if(CollectionUtils.isNotEmpty(detailList)){
                for(StockInReturnDetail detail : detailList){
                    detailPdfData = new InReturnDetailPdfData();
                    BeanUtils.copyBeanProp(detailPdfData, detail);
                    detailPdfData.setUnitName(sysDictDataService.selectDictLabel(dictType, detail.getUnitCode()));
                    detailPdfDataList.add(detailPdfData);
                }
            }
            InputStream in = new FileInputStream(new File(pdfService.printInOrderReturn(returnPdfData, detailPdfDataList)));
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
     * 扫码提交入库单退货-退货
     */
    @PostMapping("submitInReturn")
    public AjaxResult submitInReturn(@RequestBody InReturnRequestBody inReturnRequestBody){
        if(inReturnRequestBody == null){
            return error("系统繁忙，请稍后再试！");
        }
        return stockInReturnService.submitInReturn(getUsername(), inReturnRequestBody);
    }

}
