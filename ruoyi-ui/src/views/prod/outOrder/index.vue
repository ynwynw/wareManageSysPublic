<template>
  <div class="app-container">
    <el-form
      :model="queryParams"
      ref="queryForm"
      size="small"
      :inline="true"
      v-show="showSearch"
      label-width="68px"
    >
      <el-form-item label="单据号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入单据号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单据状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="请选择">
          <el-option
            v-for="item in orderStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="生产订单" prop="prodOrderNo">
        <el-input
          v-model="queryParams.prodOrderNo"
          placeholder="请输入生产订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仓库" prop="warehouseCode">
        <el-select v-model="queryParams.warehouseCode" placeholder="请选择仓库">
          <el-option
            v-for="item in warehouseList"
            :key="item.warehouseCode"
            :label="item.warehouseName"
            :value="item.warehouseCode"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="车间" prop="workshopCode">
        <el-select v-model="queryParams.workshopCode" placeholder="请选择车间">
          <el-option
            v-for="item in workshopList"
            :key="item.workshopCode"
            :label="item.workshopName"
            :value="item.workshopCode"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="物料编码" prop="matCode">
        <el-input
          v-model="queryParams.matCode"
          placeholder="请输入物料编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物料名称" prop="matName">
        <el-input
          v-model="queryParams.matName"
          placeholder="请输入物料名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="库管员" prop="warehouseKeeper">
        <el-input
          v-model="queryParams.warehouseKeeper"
          placeholder="请输入库管员"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          icon="el-icon-search"
          size="mini"
          @click="handleQuery"
          >搜索</el-button
        >
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
          >重置</el-button
        >
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['stock:outOrder:add']"
          >新增</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['stock:outOrder:remove']"
          >删除</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['stock:outOrder:export']"
          >导出</el-button
        >
      </el-col>
      <right-toolbar
        :showSearch.sync="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>

    <el-table
      v-loading="loading"
      :data="outOrderList"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        label="单据号"
        fixed
        align="center"
        prop="orderNo"
        width="180"
      />
      <el-table-column
        label="单据类型"
        fixed
        align="center"
        prop="orderTypeLabel"
        width="120"
      />
      <el-table-column
        label="生产订单号"
        align="center"
        prop="prodOrderNo"
        width="180"
      />
      <el-table-column
        label="物料编码"
        align="center"
        prop="matCode"
        width="120"
      />
      <el-table-column
        label="物料名称"
        align="center"
        prop="matName"
        width="180"
      />
      <el-table-column
        label="仓库"
        align="center"
        prop="warehouseName"
        width="120"
      />
      <el-table-column
        label="库管员"
        align="center"
        prop="warehouseKeeper"
        width="120"
      />
      <el-table-column
        label="车间"
        align="center"
        prop="workshopName"
        width="120"
      />
      <el-table-column
        label="单据状态"
        align="center"
        prop="orderStatusLabel"
        width="120"
      />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
      >
        <template slot-scope="scope">
          <span>{{
            $moment(scope.row.createTime).format("YYYY-MM-DD HH:mm")
          }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        fixed="right"
        align="center"
        class-name="small-padding fixed-width"
        width="120"
      >
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleDetail(scope.row)"
            v-hasPermi="['stock:outOrder:edit']"
            >详情</el-button
          >
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['stock:outOrder:remove']"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加出库单对话框 -->
    <el-dialog
      :title="title"
      :visible.sync="open"
      width="1200px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="7">
            <el-form-item label="生产订单" prop="prodOrderNo">
              <el-input
                readonly
                v-model="form.prodOrderNo"
                placeholder="请选择生产订单"
              >
                <el-button
                  slot="append"
                  icon="el-icon-search"
                  @click="openSelectProdOrderDialog"
                ></el-button>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="仓库" prop="warehouseCode">
              <el-select v-model="form.warehouseCode" placeholder="请选择仓库">
                <el-option
                  v-for="item in warehouseList"
                  :key="item.warehouseCode"
                  :label="item.warehouseName"
                  :value="item.warehouseCode"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="库管员" prop="warehouseKeeper">
              <el-input
                readonly
                placeholder="请选择库管员"
                v-model="form.warehouseKeeper"
              >
                <el-button
                  slot="append"
                  icon="el-icon-search"
                  @click="openSelectKeeperDialog"
                ></el-button>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-button
              type="success"
              style="margin-left: 20px"
              @click="confirmSelectInfo"
              >确认选择</el-button
            >
          </el-col>
        </el-row>
      </el-form>
      <el-table :data="outOrderDetailList" style="width: 100%">
        <el-table-column label="行号" align="center" type="index" />
        <el-table-column label="物料编码" align="center" prop="matCode" />
        <el-table-column label="物料名称" align="center" prop="matName" />
        <el-table-column label="图号" align="center" prop="figNum" />
        <el-table-column label="数量" align="center" prop="quantity" />
        <el-table-column label="单位" align="center" prop="unitCode">
          <template slot-scope="scope">
            <dict-tag
              :options="dict.type.base_mat_unit"
              :value="scope.row.unitCode"
            />
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">创 建</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="'选择生产订单'"
      :visible.sync="selectProdOrderOpen"
      width="1200px"
      append-to-body
      :close-on-click-modal="false"
    >
      <selectProdOrder
        @confirmSelect="confirmSelectProdOrder"
      ></selectProdOrder>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelSelectProdOrder">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="'选择库管员'"
      :visible.sync="selectKeeperOpen"
      width="1200px"
      append-to-body
      :close-on-click-modal="false"
    >
      <selectWarehouseKeeper
        @confirmSelect="confirmSelectKeeper"
      ></selectWarehouseKeeper>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelSelectKeeper">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 出库单详情对话框 -->
    <el-dialog
      class="detail-dialog"
      :title="'出库单详情'"
      :visible.sync="outOrderDetailOpen"
      width="1200px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="form" :model="form" label-width="120px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="出库单号：">
              <span>{{ form.orderNo }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓库：">
              <span>{{ form.warehouseName }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库管员：">
              <span>{{ form.warehouseKeeper }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="生产订单号：">
              <span>{{ form.prodOrderNo }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="物料编码：">
              <span>{{ form.matCode }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="物料名称：">
              <span>{{ form.matName }}</span>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="生产数量：">
              <span>{{ form.quantity }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="领料人：">
              <span>{{ form.createBy }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="领料时间：">
              <span>{{
                $moment(form.createTime).format("YYYY-MM-DD HH:mm")
              }}</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-table
        class="detail-table"
        :data="outOrderDetailList"
        style="width: 100%"
      >
        <el-table-column label="行号" align="center" prop="lineNo" />
        <el-table-column label="物料编码" align="center" prop="matCode" />
        <el-table-column label="物料名称" align="center" prop="matName" />
        <el-table-column label="图号" align="center" prop="figNum" />
        <el-table-column label="数量" align="center" prop="quantity" />
        <el-table-column label="已领" align="center" prop="receivedQuantity" />
        <el-table-column label="单位" align="center" prop="unitCode">
          <template slot-scope="scope">
            <dict-tag
              :options="dict.type.base_mat_unit"
              :value="scope.row.unitCode"
            />
          </template>
        </el-table-column>
        <el-table-column label="推荐货位" align="center" prop="locationCode" />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button
          type="primary"
          icon="el-icon-printer"
          @click="confirmPrintOutOrder"
          >打 印</el-button
        >
        <el-button @click="cancelOrderDetail">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listOutOrder,
  getOutOrder,
  delOutOrder,
  addOutOrder,
  listOutOrderMatBomList,
  printOutOrder,
} from "@/api/stock/outOrder";
import { listAllWorkshop } from "@/api/base/workshop";
import { listAllWarehouse } from "@/api/base/warehouse";

import selectProdOrder from "../../components/select-prod-order/index";
import selectWarehouseKeeper from "../../components/select-warehouse-keeper/index";

export default {
  name: "OutOrder",
  dicts: ["base_mat_unit"],
  components: { selectProdOrder, selectWarehouseKeeper },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      orderNos: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 出库单表格数据
      outOrderList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        orderType: "production",
        prodOrderNo: null,
        warehouseCode: null,
        workshopCode: null,
        orderReason: null,
        matCode: null,
        matName: null,
        quantity: null,
        orderStatus: null,
        warehouseKeeper: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        prodOrderNo: [
          { required: true, message: "请选择生产订单", trigger: "blur" },
        ],
        warehouseCode: [
          { required: true, message: "请选择仓库", trigger: "blur" },
        ],
        warehouseKeeper: [
          { required: true, message: "请选择库管员", trigger: "blur" },
        ],
      },

      // 日期范围
      dateRange: [],

      //单据状态
      orderStatusOptions: [
        { value: "created", label: "已创建" },
        { value: "printed", label: "已打印" },
        { value: "received", label: "已领料" },
      ],

      //选择仓库、车间
      warehouseList: [],
      workshopList: [],

      //选择生产订单
      selectProdOrderOpen: false,

      //选择库管员
      selectKeeperOpen: false,

      //出库单详情
      outOrderDetailOpen: false,
      outOrderDetailList: [],
    };
  },
  created() {
    this.getList();
    this.getWarehouseList();
    this.getWorkshopList();
  },
  methods: {
    /** 查询出库单列表 */
    getList() {
      this.loading = true;
      listOutOrder(this.addDateRange(this.queryParams, this.dateRange)).then(
        (response) => {
          this.outOrderList = response.rows;
          this.total = response.total;
          this.loading = false;
        }
      );
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
      this.outOrderDetailList = [];
    },
    // 表单重置
    reset() {
      this.form = {
        orderId: null,
        orderNo: null,
        orderType: null,
        prodOrderNo: null,
        warehouseCode: null,
        workshopCode: null,
        orderReason: null,
        matCode: null,
        matName: null,
        quantity: null,
        orderStatus: "0",
        warehouseKeeper: null,
        delFlag: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map((item) => item.orderId);
      this.orderNos = selection.map((item) => item.orderNo);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加出库单";
    },
    /** 详情 */
    handleDetail(row) {
      this.reset();
      getOutOrder(row.orderId).then((response) => {
        this.form = response.data;
        this.outOrderDetailList = response.data.detailList;
        this.outOrderDetailOpen = true;
      });
    },
    confirmPrintOutOrder() {
      printOutOrder(this.form.orderId).then((response) => {
        const binaryData = [];
        binaryData.push(response);
        let pdfUrl = window.URL.createObjectURL(
          new Blob(binaryData, { type: "application/pdf" })
        );
        window.open(pdfUrl);
      });
    },
    cancelOrderDetail() {
      this.outOrderDetailOpen = false;
    },
    /** 提交按钮 */
    submitForm() {
      let that = this;
      if (!that.outOrderDetailList || that.outOrderDetailList.length === 0) {
        that.$modal.msgError("请选择物料");
        return;
      }
      that.$refs["form"].validate((valid) => {
        if (valid) {
          that.$modal.confirm("是否确认创建出库单？").then(function () {
            that.form.detailList = that.outOrderDetailList;
            that.form.orderType = "production";
            addOutOrder(that.form).then((response) => {
              that.$modal.msgSuccess("新增成功");
              that.open = false;
              that.getList();
              that.reset();
              that.outOrderDetailList = [];
            });
          });
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const orderIds = row.orderId || this.ids;
      const delOrderNos = row.orderNo || this.orderNos;
      this.$modal
        .confirm('是否确认删除出库单号为 "' + delOrderNos + '" 的数据项？')
        .then(function () {
          return delOutOrder(orderIds);
        })
        .then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        })
        .catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download(
        "stock/outOrder/export",
        {
          ...this.queryParams,
        },
        `outOrder_${new Date().getTime()}.xlsx`
      );
    },
    //查询仓库、车间
    getWarehouseList() {
      listAllWarehouse().then((response) => {
        this.warehouseList = response;
      });
    },
    getWorkshopList() {
      listAllWorkshop().then((response) => {
        this.workshopList = response;
      });
    },
    //选择生产订单
    openSelectProdOrderDialog() {
      this.selectProdOrderOpen = true;
    },
    confirmSelectProdOrder(item) {
      this.form.prodOrderNo = item.orderNo;
      this.form.workshopCode = item.workshopCode;
      this.form.matCode = item.matCode;
      this.form.matName = item.matName;
      this.form.quantity = item.quantity;
      this.selectProdOrderOpen = false;
    },
    cancelSelectProdOrder() {
      this.selectProdOrderOpen = false;
    },
    //选择库管员
    openSelectKeeperDialog() {
      this.selectKeeperOpen = true;
    },
    confirmSelectKeeper(item) {
      this.form.warehouseKeeper = item.userName;
      this.selectKeeperOpen = false;
    },
    cancelSelectKeeper() {
      this.selectKeeperOpen = false;
    },
    //确认选择基础信息
    confirmSelectInfo() {
      listOutOrderMatBomList(this.form).then((response) => {
        this.outOrderDetailList = response;
      });
    },
  },
};
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.detail-dialog {
  .el-form-item {
    margin-bottom: 0px;
  }
  .detail-table {
    margin-top: 20px;
  }
}
</style>
