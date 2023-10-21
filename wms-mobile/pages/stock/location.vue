<template>
	<view class="content">
		<view class="scan">
			<u-search v-model="params.locationCode" :disabled="true" search-icon="scan" :show-action="false" placeholder="请扫描货位二维码"
				@click="scanCode"></u-search>
		</view>
		<view class="detail">
			<block v-for="(item, index) in matList" :key="index">
				<view :class="[index === selectIndex ? 'detail-item active' : 'detail-item']" @click="chooseItem(index)">
					<view class="left">{{index + 1}}、</view>
					<view class="right">
						<view>物料编码：{{item.matCode}}</view>
						<view>物料名称：{{item.matName}}</view>
						<view>图号：{{item.figNum}}</view>
						<view>
							<span>数量：{{item.statsQuantity}}</span>
							<span class="unit">单位：{{item.unitName}}</span>
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
			//参数
			params: { 
				//货位
				locationCode: '',
			},
			matList: [],
			//选中项
			selectIndex: '-1',
			
			//加载
			toastParams: {
				type: 'loading',
				title: '正在加载',
				message: "正在加载",
				iconUrl: 'https://cdn.uviewui.com/uview/demo/toast/loading.png',
				duration: 60000,
			},
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
					//扫码货位
					if(code.indexOf("LOCATION") === 0){
						let codeArr = code.match(/:(\S*)/)[1].split(":");
						if(codeArr && codeArr.length === 2){
							this.params.locationCode = codeArr[0];
							this.loadStockInOrder();
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
		//查询入库单信息
		loadStockInOrder(){
			this.$http({url: '/stock/info/locationMatList', method: 'GET', data: this.params}).then((res) => {
				if(res.code === 200){
					this.matList = res.data;
				}else{
					uni.showToast({ title: res.msg, icon: 'none' })
				}
				this.$refs.loadinToast.hide();
			});
		},
		//选择行
		chooseItem(index){
			this.selectIndex = index;
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
