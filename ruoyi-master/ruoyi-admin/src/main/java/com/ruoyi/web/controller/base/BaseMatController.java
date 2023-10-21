package com.ruoyi.web.controller.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.base.service.IBaseMatClassService;
import com.ruoyi.base.service.IBaseMatGroupService;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.ClassPathResource;
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
import com.ruoyi.base.domain.BaseMat;
import com.ruoyi.base.service.IBaseMatService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 物料主数据Controller
 *
 * @author ruoyi
 * @date 2022-07-23
 */
@RestController
@RequestMapping("/base/mat")
public class BaseMatController extends BaseController {
    @Autowired
    private IBaseMatService baseMatService;
    @Autowired
    private IBaseMatGroupService baseMatGroupService;
    @Autowired
    private IBaseMatClassService baseMatClassService;

    /**
     * 查询物料主数据列表
     */
    @PreAuthorize("@ss.hasPermi('base:mat:list')")
    @GetMapping("/list")
    public TableDataInfo list(BaseMat baseMat) {
        startPage();
        List<BaseMat> list = baseMatService.selectBaseMatList(baseMat);
        if(CollectionUtils.isNotEmpty(list)){
            for(BaseMat mat : list){
                mat.setMatGroupName(baseMatGroupService.selectBaseMatGroupNameByGroupCode(mat.getMatGroup()));
                mat.setMatClassName(baseMatClassService.selectBaseMatClassNameByClassCode(mat.getMatClass()));
            }
        }
        return getDataTable(list);
    }

    /**
     * 导出物料主数据列表
     */
    @PreAuthorize("@ss.hasPermi('base:mat:export')")
    @Log(title = "物料主数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BaseMat baseMat) {
        List<BaseMat> list = baseMatService.selectBaseMatList(baseMat);
        ExcelUtil<BaseMat> util = new ExcelUtil<BaseMat>(BaseMat.class);
        util.exportExcel(response, list, "物料主数据数据");
    }

    /**
     * 获取物料主数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('base:mat:query')")
    @GetMapping(value = "/{matId}")
    public AjaxResult getInfo(@PathVariable("matId") Long matId) {
        return AjaxResult.success(baseMatService.selectBaseMatByMatId(matId));
    }

    /**
     * 新增物料主数据
     */
    @PreAuthorize("@ss.hasPermi('base:mat:add')")
    @Log(title = "物料主数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BaseMat baseMat) {
        baseMat.setCreateBy(getUsername());
        return toAjax(baseMatService.insertBaseMat(baseMat));
    }

    /**
     * 修改物料主数据
     */
    @PreAuthorize("@ss.hasPermi('base:mat:edit')")
    @Log(title = "物料主数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BaseMat baseMat) {
        baseMat.setUpdateBy(getUsername());
        return toAjax(baseMatService.updateBaseMat(baseMat));
    }

    /**
     * 删除物料主数据
     */
    @PreAuthorize("@ss.hasPermi('base:mat:remove')")
    @Log(title = "物料主数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{matIds}")
    public AjaxResult remove(@PathVariable Long[] matIds) {
        return toAjax(baseMatService.deleteBaseMatByMatIds(matIds));
    }

    //模板下载
    @PostMapping("downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        ExcelUtil<BaseMat> util = new ExcelUtil<BaseMat>(BaseMat.class);
        util.importTemplateExcel(response, "物料数据");
    }

    //导入数据
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
        ExcelUtil<BaseMat> util = new ExcelUtil<BaseMat>(BaseMat.class);
        List<BaseMat> matList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = baseMatService.importMat(matList, updateSupport, operName);
        return AjaxResult.success(message);
    }

}
