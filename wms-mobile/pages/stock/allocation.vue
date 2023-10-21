<template>
	<view class="content">
		<view class="scan">
			<u-search v-model="order.allotNo" :disabled="true" search-icon="scan" :show-action="false" placeholder="请扫描调拨单二维码"
				@click="scanCode"></u-search>
			<u-button text="提交" type="primary" @click="confirm" :customStyle="'width:120rpx;height:64rpx;margin-left:10rpx;'"></u-button>
		</view>
		<view class="order">
			<view class="tips">发起仓库：</view>
			<view class="label">{{order.srcWarehouseName}}</view>
			<view class="tips">目标仓库：</view>
			<view class="label">{{order.destWarehouseName}}</view>
		</view>
		<view class="detail">
			<block v-for="(item, index) in matLabelList" :key="index">
				<view v-if="order.allotProgress === 'created'" :class="[item.labelId === selectItem.labelId ? 'detail-item active' : 'detail-item']" @click="chooseItem(item)">
					<view class="left">{{index + 1}}、</view>
					<view class="right">
						<view>物料编码：{{item.matCode}}</view>
						<view>物料名称：{{item.matName}}</view>
						<view>图号：{{item.figNum}}</view>
						<view>所在货位：{{item.locationCode}}</view>
						<view>
							<span>数量：{{item.quantity}}</span>
							<span class="unit">单位：{{item.unitName}}</span>
						</view>
					</view>
				</view>
				<view v-if="order.allotProgress === 'picking'" :class="[item.labelId === selectItem.labelId ? 'detail-item active' : 'detail-item']" @click="chooseItem(item)">
					<view class="left">{{index + 1}}、</view>
					<view class="right">
						<view>物料编码：{{item.matCode}}</view>
						<view>物料名称：{{item.matName}}</view>
						<view>图号：{{item.figNum}}</view>
						<view>
							<span>发起数量：{{item.quantity}}</span>
							<span class="unit">单位：{{item.unitName}}</span>
						</view>
						<view>
							<span>接收数量：{{item.signQuantity}}</span>
							<span class="unit">接收货位：{{item.destLocationCode ? item.destLocationCode : ''}}</span>
						</view>
						<view class="tag">
							<u-tag text="待完成" type="error" v-if="!item.destLocationCode"></u-tag>
							<u-tag text="已完成" type="success" v-else></u-tag>
						</view>
					</view>
				</view>
				<u-gap bgColor="#e5e6e7" height="1"></u-gap>
			</block>
		</view>
		
		<u-toast ref="loadinToast"></u-toast>
	</view>
</template>

<script>
export default {
	data() {
		return {
			//调拨单
			order:{
				allotNo:'',
				srcWarehouseName: '',
				destWarehouseName: '',
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
			params: { },
			//当前货位
			selectLocation: '',
			//当前选中项
			selectItem: {},
			
			matLabelList: [],
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
					if(code.indexOf("ORDER:") === 0){//扫码调拨单
						this.order.allotNo = code.replace("ORDER:", "");
						this.loadAllotOrder();
					}else if(code.indexOf("LABEL") === 0){//扫码物料标签
						if(!this.order.allotNo){
							uni.showToast({ title: '请先扫描调拨单', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						let labelId = code.replace("LABEL:", "");
						this.loadMatLabel(labelId);
					}else if(code.indexOf("LOCATION") === 0){//扫码货位
						if(!this.order.allotNo){
							uni.showToast({ title: '请先扫描调拨单', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						if(this.order.allotProgress !== 'picking'){
							uni.showToast({ title: '请发起仓库完成拣货', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						let codeArr = code.match(/:(\S*)/)[1].split(":");
						if(codeArr && codeArr.length === 2){
							this.selectItem.destLocationCode = codeArr[0];
							this.selectItem.signQuantity = this.selectItem.quantity;
							this.$refs.loadinToast.hide();
						}
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
		//查询调拨单信息
		loadAllotOrder(){
			this.$http({url: '/stock/allotOrder/m/' + this.order.allotNo, method: 'GET', data: this.params}).then((res) => {
				if(res.code === 200){
					this.order = res.data;
					this.matLabelList = [];
					if(this.order.allotProgress === 'picking'){
						let detailList = res.data.detailList;
						detailList && detailList.length > 0 && detailList.forEach(item => {
							this.matLabelList.unshift(item);
						});
					}
				}else{
					uni.showToast({ title: res.msg, icon: 'none' })
				}
				this.$refs.loadinToast.hide();
			});
		},
		//查询物料标签信息
		loadMatLabel(labelId){
			this.$http({url: '/stock/matLabel/' + labelId, method: 'GET', data: this.params}).then((res) => {
				if(res.code === 200){
					let matLabel = res.data;
					matLabel.quantity = matLabel.usableQuantity - matLabel.receivedQuantity;
					matLabel.srcWarehouseCode = matLabel.warehouseCode;
					matLabel.srcLocationCode = matLabel.locationCode;
					this.matLabelList.unshift(matLabel);
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
		//提交
		confirm(){
			let that = this;
			uni.showModal({
				title: '提示',
				content: '确认提交调拨信息？',
				success: function (res) {
					if (res.confirm) {
						that.$refs.loadinToast.show({ ...that.toastParams });
						that.order.detailList = that.matLabelList;
						that.$http({url: '/stock/allotOrder/submitAllot', method: 'POST', data: that.order}).then((allotRes) => {
							if(allotRes.code === 200){
								that.order = [];
								that.selectLocation = '';
								that.selectItem = {};
								that.matLabelList = [];
							}
							uni.showToast({ title: allotRes.msg, icon: 'none' })
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
	.tips{
		font-size: 28rpx;
	}
	.label{
		font-size: 28rpx;
		margin-right: 40rpx;
		width: 180rpx;
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
</style>
