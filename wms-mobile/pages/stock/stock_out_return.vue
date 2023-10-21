<template>
	<view class="content">
		<view class="scan">
			<u-search v-model="order.returnNo" :disabled="true" search-icon="scan" :show-action="false" placeholder="请扫描出库退货单二维码"
				@click="scanCode"></u-search>
			<u-button text="提交" type="primary" @click="confirm" :customStyle="'width:120rpx;height:64rpx;margin-left:10rpx;'"></u-button>
		</view>
		<view class="order">
			<view>单据类型：</view>
			<view class="label">{{order.returnTypeLabel}}</view>
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
							<span>需退：{{item.quantity}}</span>
							<span class="unit">已退：{{item.returnQuantity}}</span>
						</view>
						<view>所在货位：{{item.locationCode ? item.locationCode : ''}}</view>
					</view>
					<view class="tag">
						<u-tag text="待完成" type="warning" v-if="item.quantity > item.returnQuantity"></u-tag>
						<u-tag text="已完成" type="success" v-else></u-tag>
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
			//订单
			order: {
				returnNo: '',
				returnTypeLabel: '',
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
			
			//当前货位
			checkLocation: '',
			//当前选中项
			selectItem: {},
			//参数
			params: {},
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
					if(code.indexOf("ORDER:") === 0){//扫码出库退货单
						this.order.returnNo = code.replace("ORDER:", "");
						this.loadStockOutReturnOrder();
					}else if(code.indexOf("LOCATION") === 0){//扫码货位
						if(!this.order.returnNo){
							uni.showToast({ title: '请先扫描出库退货单', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						if(!this.selectItem.detailId){
							uni.showToast({ title: '请先选择行项目', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						let codeArr = code.match(/:(\S*)/)[1].split(":");
						if(codeArr && codeArr.length === 2){
							if(this.selectItem.warehouseCode !== codeArr[1]){
								uni.showToast({ title: '非目标仓库', icon: 'none' })
								this.$refs.loadinToast.hide();
								return;
							}
							if(this.selectItem.locationCode !== codeArr[0]){
								uni.showToast({ title: '非目标货位', icon: 'none' })
								this.$refs.loadinToast.hide();
								return;
							}
							this.selectItem.returnQuantity = this.selectItem.quantity;
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
		//查询出库退货单信息
		loadStockOutReturnOrder(){
			this.$http({url: '/stock/outReturn/m/'+this.order.returnNo, method: 'GET', data: this.params}).then((res) => {
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
		//提交
		confirm(){
			let that = this;
			uni.showModal({
				title: '提示',
				content: '确认提交退货信息？',
				success: function (res) {
					if (res.confirm) {
						that.$refs.loadinToast.show({ ...that.toastParams });
						that.$http({url: '/stock/outReturn/submitOutReturn', method: 'POST', data: that.order}).then((stockOutReturnRes) => {
							if(stockOutReturnRes.code === 200){
								that.order = [];
							}
							uni.showToast({ title: stockOutReturnRes.msg, icon: 'none' })
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
		font-size: 28rpx;
		margin-right: 40rpx;
		width: 200rpx;
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
