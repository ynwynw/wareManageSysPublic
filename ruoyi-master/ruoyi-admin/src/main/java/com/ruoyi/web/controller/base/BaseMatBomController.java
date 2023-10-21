package com.ruoyi.web.controller.base;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.ruoyi.base.domain.BaseMat;
import com.ruoyi.base.service.IBaseMatService;
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
import com.ruoyi.base.domain.BaseMatBom;
import com.ruoyi.base.service.IBaseMatBomService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 物料BOMController
 *
 * @author ruoyi
 * @date 2022-07-23
 */
@RestController
@RequestMapping("/base/bom")
public class BaseMatBomController extends BaseController {
    @Autowired
    private IBaseMatBomService baseMatBomService;
    @Autowired
    private IBaseMatService baseMatService;

    /**
     * 查询物料BOM列表
     */
    @PreAuthorize("@ss.hasPermi('base:bom:list')")
    @GetMapping("/list")
    public TableDataInfo list(String matCode, String matName) {
        startPage();
        List<BaseMat> list = baseMatService.selectBomList(matCode, matName);
        return getDataTable(list);
    }

    /**
     * 查询物料BOM详情列表
     */
    @PreAuthorize("@ss.hasPermi('base:bom:list')")
    @GetMapping("/detailList")
    public TableDataInfo detailList(BaseMatBom baseMatBom) {
        startPage();
        List<BaseMatBom> list = baseMatBomService.selectBaseMatBomList(baseMatBom);
        if(CollectionUtils.isNotEmpty(list)){
            BaseMat mat = null;
            for(BaseMatBom bom : list){
                mat = baseMatService.selectBaseMatByMatCode(bom.getChildMatCode());
                bom.setChildMatName(mat != null ? mat.getMatName() : "");
                bom.setFigNum(mat != null ? mat.getFigNum() : "");
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出物料BOM列表
     */
    @PreAuthorize("@ss.hasPermi('base:bom:export')")
    @Log(title = "物料BOM", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BaseMatBom baseMatBom) {
        List<BaseMatBom> list = baseMatBomService.selectBaseMatBomList(baseMatBom);
        ExcelUtil<BaseMatBom> util = new ExcelUtil<BaseMatBom>(BaseMatBom.class);
        util.exportExcel(response, list, "物料BOM数据");
    }

    /**
     * 获取物料BOM详细信息
     */
    @PreAuthorize("@ss.hasPermi('base:bom:query')")
    @GetMapping(value = "/{bomId}")
    public AjaxResult getInfo(@PathVariable("bomId") Long bomId) {
        return AjaxResult.success(baseMatBomService.selectBaseMatBomByBomId(bomId));
    }

    /**
     * 新增物料BOM
     */
    @PreAuthorize("@ss.hasPermi('base:bom:add')")
    @Log(title = "物料BOM", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BaseMatBom baseMatBom) {
        baseMatBom.setCreateBy(getUsername());
        return toAjax(baseMatBomService.insertBaseMatBom(baseMatBom));
    }

    /**
     * 修改物料BOM
     */
    @PreAuthorize("@ss.hasPermi('base:bom:edit')")
    @Log(title = "物料BOM", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BaseMatBom baseMatBom) {
        baseMatBom.setUpdateBy(getUsername());
        return toAjax(baseMatBomService.updateBaseMatBom(baseMatBom));
    }

    /**
     * 删除物料BOM
     */
    @PreAuthorize("@ss.hasPermi('base:bom:remove')")
    @Log(title = "物料BOM", businessType = BusinessType.DELETE)
    @DeleteMapping("/{matCodes}")
    public AjaxResult remove(@PathVariable String[] matCodes) {
        return toAjax(baseMatBomService.deleteBaseMatBomByMatCodes(matCodes));
    }

    //模板下载
    @PostMapping("downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelUtil<BaseMatBom> util = new ExcelUtil<BaseMatBom>(BaseMatBom.class);
        util.importTemplateExcel(response, "物料数据");
    }

    //导入数据
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception {
        ExcelUtil<BaseMatBom> util = new ExcelUtil<BaseMatBom>(BaseMatBom.class);
        List<BaseMatBom> matBomList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = baseMatBomService.importMatBom(matBomList, operName);
        return AjaxResult.success(message);
    }

}
