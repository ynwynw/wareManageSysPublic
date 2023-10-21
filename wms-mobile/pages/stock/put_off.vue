<template>
	<view class="content">
		<view class="scan">
			<u-search v-model="selectLocation" :disabled="true" search-icon="scan" :show-action="false" placeholder="请扫描货位二维码"
				@click="scanCode"></u-search>
			<u-button text="提交" type="primary" @click="confirm" :customStyle="'width:120rpx;height:64rpx;margin-left:10rpx;'"></u-button>
		</view>
		<view class="detail">
			<block v-for="(item, index) in matLabelList" :key="index">
				<view :class="[item.labelId === selectItem.labelId ? 'detail-item active' : 'detail-item']" @click="chooseItem(item)">
					<view class="left">{{index + 1}}、</view>
					<view class="right">
						<view>物料编码：{{item.matCode}}</view>
						<view>物料名称：{{item.matName}}</view>
						<view>图号：{{item.figNum}}</view>
						<view>批次：{{item.batch}}</view>
						<view>
							<span>数量：{{item.remainingQuantity}}</span>
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
			
			//仓库、货位
			selectWarehouseCode: '',
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
					//扫码货位
					if(code.indexOf("LOCATION") === 0){
						let codeArr = code.match(/:(\S*)/)[1].split(":");
						if(codeArr && codeArr.length === 2){
							this.selectLocation = codeArr[0];
							this.selectWarehouseCode = codeArr[1];
							this.$refs.loadinToast.hide();
						}
					}else if(code.indexOf("LABEL") === 0){
						if(!this.selectLocation){
							uni.showToast({ title: '请先扫描货位', icon: 'none' })
							this.$refs.loadinToast.hide();
							return;
						}
						let labelId = code.replace("LABEL:", "");
						this.loadMatLabel(labelId);
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
		//查询物料标签信息
		loadMatLabel(labelId){
			this.$http({url: '/stock/matLabel/' + labelId, method: 'GET', data: this.params}).then((res) => {
				if(res.code === 200){
					if(res.data.locationCode){
						uni.showToast({ title: '该物料未上架', icon: 'none' })
						this.$refs.loadinToast.hide();
						return;
					}
					this.matLabelList.unshift(res.data);
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
		//提交下架
		confirm(){
			let that = this;
			uni.showModal({
				title: '提示',
				content: '确认提交下架信息？',
				success: function (res) {
					if (res.confirm) {
						that.$refs.loadinToast.show({ ...that.toastParams });
						let putOffRequestBody = {
							warehouseCode: that.selectWarehouseCode,
							locationCode: that.selectLocation,
							matLabelList: that.matLabelList,
						};
						that.$http({url: '/stock/info/submitPutOff', method: 'POST', data: putOffRequestBody}).then((putOffRes) => {
							if(putOffRes.code === 200){
								that.selectWarehouseCode = '';
								that.selectLocation = '';
								that.selectItem = {};
								that.matLabelList = [];
							}
							uni.showToast({ title: putOffRes.msg, icon: 'none' })
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
