package com.ruoyi.web.controller.stock;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.domain.BaseMat;
import com.ruoyi.base.service.IBaseMatClassService;
import com.ruoyi.base.service.IBaseMatGroupService;
import com.ruoyi.common.bean.MatLabelPdfData;
import com.ruoyi.common.bean.typeEnum.InOrderTypeEnum;
import com.ruoyi.common.service.PDFService;
import com.ruoyi.common.utils.Arith;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.service.ISysDictDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import com.ruoyi.stock.domain.StockMatLabel;
import com.ruoyi.stock.service.IStockMatLabelService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 物料标签Controller
 *
 * @author ruoyi
 * @date 2022-07-25
 */
@RestController
@RequestMapping("/stock/matLabel")
public class StockMatLabelController extends BaseController {
    @Autowired
    private IStockMatLabelService stockMatLabelService;
    @Autowired
    private IBaseMatGroupService baseMatGroupService;
    @Autowired
    private IBaseMatClassService baseMatClassService;
    @Autowired
    private ISysDictDataService sysDictDataService;
    @Autowired
    private PDFService pdfService;

    private static final String dictType = "base_mat_unit";

    /**
     * 查询物料标签列表
     */
    @PreAuthorize("@ss.hasPermi('stock:matLabel:list')")
    @GetMapping("/list")
    public TableDataInfo list(StockMatLabel stockMatLabel) {
        startPage();
        List<StockMatLabel> list = stockMatLabelService.selectStockMatLabelList(stockMatLabel);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockMatLabel label : list){
                label.setMatGroupName(baseMatGroupService.selectBaseMatGroupNameByGroupCode(label.getMatGroup()));
                label.setMatClassName(baseMatClassService.selectBaseMatClassNameByClassCode(label.getMatClass()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 查询物料标签列表（弹窗）
     */
    @PreAuthorize("@ss.hasPermi('stock:matLabel:list')")
    @GetMapping("/listDialog")
    public TableDataInfo listDialog(StockMatLabel stockMatLabel) {
        startPage();
        List<StockMatLabel> list = stockMatLabelService.selectStockMatLabelListDialog(stockMatLabel);
        if(CollectionUtils.isNotEmpty(list)){
            for(StockMatLabel label : list){
                label.setMatGroupName(baseMatGroupService.selectBaseMatGroupNameByGroupCode(label.getMatGroup()));
                label.setMatClassName(baseMatClassService.selectBaseMatClassNameByClassCode(label.getMatClass()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出物料标签列表
     */
    @PreAuthorize("@ss.hasPermi('stock:matLabel:export')")
    @Log(title = "物料标签", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, StockMatLabel stockMatLabel) {
        List<StockMatLabel> list = stockMatLabelService.selectStockMatLabelList(stockMatLabel);
        ExcelUtil<StockMatLabel> util = new ExcelUtil<StockMatLabel>(StockMatLabel.class);
        util.exportExcel(response, list, "物料标签数据");
    }

    /**
     * 获取物料标签详细信息
     */
    @PreAuthorize("@ss.hasPermi('stock:matLabel:query')")
    @GetMapping(value = "/{labelId}")
    public AjaxResult getInfo(@PathVariable("labelId") Long labelId) {
        StockMatLabel matLabel = stockMatLabelService.selectStockMatLabelByLabelId(labelId);
        matLabel.setRemainingQuantity(matLabel.getUsableQuantity().subtract(matLabel.getReceivedQuantity()));
        matLabel.setUnitName(sysDictDataService.selectDictLabel(dictType, matLabel.getUnitCode()));
        return AjaxResult.success(matLabel);
    }

    /**
     * 新增物料标签
     */
    @PreAuthorize("@ss.hasPermi('stock:matLabel:add')")
    @Log(title = "物料标签", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody StockMatLabel stockMatLabel) {
        stockMatLabel.setCreateBy(getUsername());
        return toAjax(stockMatLabelService.insertStockMatLabel(stockMatLabel));
    }

    /**
     * 修改物料标签
     */
    @PreAuthorize("@ss.hasPermi('stock:matLabel:edit')")
    @Log(title = "物料标签", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody StockMatLabel stockMatLabel) {
        stockMatLabel.setUpdateBy(getUsername());
        return toAjax(stockMatLabelService.updateStockMatLabel(stockMatLabel));
    }

    /**
     * 删除物料标签
     */
    @PreAuthorize("@ss.hasPermi('stock:matLabel:remove')")
    @Log(title = "物料标签", businessType = BusinessType.DELETE)
    @DeleteMapping("/{labelIds}")
    public AjaxResult remove(@PathVariable Long[] labelIds) {
        return toAjax(stockMatLabelService.deleteStockMatLabelByLabelIds(labelIds));
    }

    /**
     * 打印物料标签
     */
    @GetMapping("printLabel/{labelId}")
    public void printLabel(HttpServletResponse response, @PathVariable Long labelId) throws Exception{
        OutputStream out = response.getOutputStream();
        StockMatLabel matLabel = stockMatLabelService.selectStockMatLabelByLabelId(labelId);
        matLabel.setOrderTypeLabel(InOrderTypeEnum.getLabel(matLabel.getOrderType()));
        MatLabelPdfData pdfData = new MatLabelPdfData();
        BeanUtils.copyBeanProp(pdfData, matLabel);
        pdfData.setUnitName(sysDictDataService.selectDictLabel(dictType, pdfData.getUnitCode()));
        InputStream in = new FileInputStream(new File(pdfService.printLabel(pdfData, 1)));
        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        response.setContentType("application/pdf");
        in.close();
    }

    /**
     * 打印物料标签(批量)
     */
    @GetMapping("printLabelBatch/{labelIds}")
    public void printLabelBatch(HttpServletResponse response, @PathVariable Long[] labelIds) {
        List<MatLabelPdfData> pdfDataList = new ArrayList<>();
        OutputStream out = null;
        InputStream in = null;
        try{
            out = response.getOutputStream();
            for(Long labelId : labelIds){
                StockMatLabel matLabel = stockMatLabelService.selectStockMatLabelByLabelId(labelId);
                matLabel.setOrderTypeLabel(InOrderTypeEnum.getLabel(matLabel.getOrderType()));
                MatLabelPdfData pdfData = new MatLabelPdfData();
                BeanUtils.copyBeanProp(pdfData, matLabel);
                pdfData.setUnitName(sysDictDataService.selectDictLabel(dictType, pdfData.getUnitCode()));
                pdfDataList.add(pdfData);
            }
            in = new FileInputStream(new File(pdfService.printLabelBatch(pdfDataList)));
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            response.setContentType("application/pdf");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
