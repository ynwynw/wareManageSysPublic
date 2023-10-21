<template>
	<view class="content">
		<view class="scan">
			<u-search v-model="order.orderNo" :disabled="true" search-icon="scan" :show-action="false" placeholder="请扫描出库单二维码"
				@click="scanCode"></u-search>
			<u-button text="提交" type="primary" @click="confirm" :customStyle="'width:120rpx;height:64rpx;margin-left:10rpx;'"></u-button>
		</view>
		<view class="order">
			<view>单据类型：</view>
			<view class="label">{{order.orderTypeLabel}}</view>
			<view>当前货位：</view>
			<view class="label">{{selectLocation}}</view>
		</view>
		<view class="detail">
			<block v-for="(item, index) in order.detailList" :key="index">
				<view :class="[item.lineNo === selectItem.lineNo ? 'detail-item active' : 'detail-item']" @click="chooseItem(item)">
					<view class="left">{{item.lineNo}}、</view>
					<view class="right">
						<view>物料编码：{{item.matCode}}</view>
						<view>物料名称：{{item.matName}}</view>
						<view>图号：{{item.figNum}}</view>
						<view>
							<span>需领：{{item.quantity}}</span>
							<span class="unit">已领：{{item.receivedQuantity}}</span>
						</view>
						<view>推荐货位：{{item.locationCode ? item.locationCode : ''}}</view>
					</view>
					<view class="tag">
						<u-tag text="待完成" type="error" v-if="item.receivedQuantity === 0"></u-tag>
						<u-tag text="领料中" type="warning" v-if="item.quantity > item.receivedQuantity && item.receivedQuantity > 0"></u-tag>
						<u-tag text="已完成" type="success" v-if="item.quantity === item.receivedQuantity"></u-tag>
					</view>
				</view>
				<u-gap bgColor="#e5e6e7" height="1"></u-gap>
			</block>
		</view>
		
		<u-modal title="选择物料" :show="showModal" showCancelButton @confirm="confirmSelectMatLabel" @cancel="cancelSelectMatLabel">
			<view class="slot-content">
				<view class="content-line">
					<view class="content-line-label">物料编码：</view>
					<view>{{scanMatLabelItem.matCode}}</view>
				</view>
				<view class="content-line">
					<view class="content-line-label">物料名称：</view>
					<view>{{scanMatLabelItem.matName}}</view>
				</view>
				<view class="content-line">
					<view class="content-line-label">图号：</view>
					<view>{{scanMatLabelItem.figNum}}</view>
				</view>
				<view class="content-line">
					<view class="content-line-label">批次：</view>
					<view>{{scanMatLabelItem.batch}}</view>
				</view>
				<view class="content-line">
					<view class="content-line-label">生产日期：</view>
					<view>{{scanMatLabelItem.prodTime}}</view>
				</view>
				<view class="content-line">
					<view class="content-line-label">标签剩余：</view>
					<view>{{scanMatLabelItem.remainingQuantity}}</view>
				</view>
				<view class="content-line">
					<view class="content-line-label">需领数：</view>
					<view>{{scanMatLabelItem.needQuantity}}</view>
				</view>
				<view class="content-line quantity">
					<view class="content-line-label">领取：</view>
					<view>
						<u-number-box :inputWidth="80" slot="right-icon" v-model="scanMatLabelItem.inputQuantity" step="1" :min="0.01" decimalLength="2"/>
					</view>
				</view>
			</view>
		</u-modal>
		
		<u-toast ref="loadinToast"></u-toast>
	</view>
</template>

<script>
export default {
	data() {
		return {
			//订单
			order: {
				orderNo: '',
				orderTypeLabel: '',
				detailList: [],
			},
			//加载
			toastParams: {
				type: 'loading',
				title: '正在加载',
				message: "正在加载",
				iconUrl: 'https://cdn.uviewui.com/uview/demo/toast/loading.png',
				duration: 60000,
			},
			//参数
			params: {},
			
			//当前货位
			selectLocation: '',
			//当前选中项
			selectItem: {},
				
			//选择物料
			showModal: false,
			scanMatLabelItem: {},
			
			//领取集,key:标签ID，value:数量
			receivedMap: { },
		}
	},
	onShow() {
		if (!uni.getStorageSync("vuex_token")) {
			this.$u.route('pages/user/login');
		}
	},
	methods: {
		scanCode() {
			this.$refs.loadinToast.show({ ...this.toastParams });
			this.doScan();
		},
		doScan() {
			uni.scanCode({
				scanType: ["qrCode"],
				success: (res) => {
					let code = res.result;
					if(code.indexOf("ORDER:") === 0){//扫码入库单
						this.order.orderNo = code.replace("ORDER:", "");
						this.loadStockInOrder();
					}else if(code.indexOf("LOCATION") === 0){//扫码货位
						if(!this.order.orderNo){
							uni.showToast({ title: '请先扫描出库单', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						let codeArr = code.match(/:(\S*)/)[1].split(":");
						if(codeArr && codeArr.length === 2){
							if(this.order.warehouseCode !== codeArr[1]){
								uni.showToast({ title: '非目标仓库货位', icon: 'none' })
								this.$refs.loadinToast.hide();
								return;
							}
							this.selectLocation = codeArr[0];
							this.$refs.loadinToast.hide();
						}
					}else if(code.indexOf("LABEL") === 0){//扫码物料标签
						if(!this.order.orderNo){
							uni.showToast({ title: '请先扫描出库单', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						if(!this.selectLocation){
							uni.showToast({ title: '请先扫描货位', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						if(!this.selectItem.detailId){
							uni.showToast({ title: '请先选择行项目', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						let labelId = code.replace("LABEL:", "");
						this.selectMatLabel(labelId);
					}else{
						uni.showToast({ title: '二维码不正确', icon: 'none' })
						this.$refs.loadinToast.hide();
					}
				},
				fail: (res) => {
					console.log('未识别到二维码');
					this.$refs.loadinToast.hide();
				}
			})
		},
		//查询入库单信息
		loadStockInOrder(){
			this.$http({url: '/stock/outOrder/m/'+this.order.orderNo, method: 'GET', data: this.params}).then((res) => {
				if(res.code === 200){
					this.order = res.data;
				}else{
					uni.showToast({ title: res.msg, icon: 'none' })
				}
				this.$refs.loadinToast.hide();
			});
		},
		//选择行
		chooseItem(item){
			this.selectItem = item;
		},
		//选择物料
		selectMatLabel(labelId){
			if(this.selectItem.quantity - this.selectItem.receivedQuantity === 0){
				uni.showToast({ title: '物料已达需求量', icon: 'none' })
				this.$refs.loadinToast.hide();
				return;
			}
			this.scanMatLabelItem = {};
			this.inputQuantity = 1;
			this.$http({url: '/stock/matLabel/'+labelId, method: 'GET', data: this.params}).then((res) => {
				if(res.code === 200){
					this.scanMatLabelItem = res.data;
					if(this.selectItem.matCode !== this.scanMatLabelItem.matCode){
						uni.showToast({ title: '非目标物料', icon: 'none' })
						this.$refs.loadinToast.hide();
						return;
					}
					if(this.selectItem.warehouseCode !== this.scanMatLabelItem.warehouseCode){
						uni.showToast({ title: '非目标仓库物料', icon: 'none' })
						this.$refs.loadinToast.hide();
						return;
					}
					//标签剩余数
					let labelId = this.scanMatLabelItem.labelId;
					if(this.receivedMap.hasOwnProperty(labelId)){
						this.scanMatLabelItem.remainingQuantity -= this.receivedMap[labelId];
					}
					//判断标签是否还能领取
					if(this.scanMatLabelItem.remainingQuantity === 0){
						uni.showToast({ title: '标签已领完', icon: 'none' })
						this.$refs.loadinToast.hide();
						return;
					}
					//需领数
					this.scanMatLabelItem.needQuantity = this.selectItem.quantity - this.selectItem.receivedQuantity;
					//领取数
					if(this.scanMatLabelItem.remainingQuantity >= this.scanMatLabelItem.needQuantity){
						this.scanMatLabelItem.inputQuantity = this.scanMatLabelItem.needQuantity;
					}else{
						this.scanMatLabelItem.inputQuantity = this.scanMatLabelItem.remainingQuantity;
					}
				}else{
					uni.showToast({ title: res.msg, icon: 'none' })
				}
				this.showModal = true;
				this.$refs.loadinToast.hide();
			});
		},
		confirmSelectMatLabel(){
			let matCode = this.selectItem.matCode;
			//记录扫码 标签id-数量
			let labelId = this.scanMatLabelItem.labelId;
			if(this.receivedMap.hasOwnProperty(labelId)){
				this.receivedMap[labelId] += this.inputQuantity;
			}else{
				this.receivedMap[labelId] = this.inputQuantity;
			}
			//修改已领数
			this.selectItem.receivedQuantity += this.inputQuantity;
			this.showModal = false;
		},
		cancelSelectMatLabel(){
			this.showModal = false;
		},
		//提交
		confirm(){
			let that = this;
			uni.showModal({
				title: '提示',
				content: '确认提交出库信息？',
				success: function (res) {
					if (res.confirm) {
						that.$refs.loadinToast.show({ ...that.toastParams });
						let stockOutRequestBody = {
							orderNo: that.order.orderNo,
							receivedMap: that.receivedMap,
						};
						that.$http({url: '/stock/outOrder/submitStockOut', method: 'POST', data: stockOutRequestBody}).then((stockOutRes) => {
							if(stockOutRes.code === 200){
								that.order = [];
								that.selectLocation = '';
								that.selectItem = {};
								that.scanMatLabelItem = {};
								that.receivedMap =  { };
							}
							uni.showToast({ title: stockOutRes.msg, icon: 'none' })
							that.$refs.loadinToast.hide();
						});
					}
				}
			});
		},
	}
}
</script>

<style lang="scss">
.scan{
	padding: 20rpx;
	display: flex;
	align-items: center;
}
.order{
	padding: 0 20rpx 0 20rpx;
	display: flex;
	align-items: flex-end;
	font-size: 30rpx;
	color: #515151;
	.label{
		margin-right: 40rpx;
		width: 165rpx;
		border-bottom: 1px solid #515151;
	}
}
.detail{
	padding: 20rpx;
	.detail-item{
		padding: 10rpx;
		display: flex;
		position: relative;
		border-radius: 1%;
		.right{
			font-size: 26rpx;
			.unit{
				padding-left: 80rpx;
			}
		}
		.tag{
			position: absolute;
			top: 10rpx;
			right: 10rpx;
		}
	}
	.active{
		background-color: #e6e6e6;
	}
}
.slot-content{
	width: 520rpx;
	.content-line{
		display: flex;
		align-items: flex-end;
		.content-line-label{
			display: flex;
			justify-content: flex-end;
			width:180rpx;
		}
	}
	.quantity{
		margin-top: 10rpx;
		align-items: center;
	}
}
</style>