<template>
	<view class="content">
		<view class="banner">
			<u-swiper :list="bannerList" keyName="bannerUrl" :imgDomain="imgDomain" indicator indicatorMode="line" circular></u-swiper>
		</view>
		<view class="menu">
			<uni-section title="仓库服务" type="line"></uni-section>
			<u-grid :col="4">
				<u-grid-item :customStyle="'padding: 0 0 0 0'" v-for="(item, index) in menuList" :key="index"  @click="clickService(item)">
					<u--image :src="item.icon" width="40" height="40"></u--image>
					<text class="grid-text">{{item.name}}</text>
				</u-grid-item>
			</u-grid>
		</view>
		<u-gap bgColor="#f3f4f6" height="5"></u-gap>
		<view class="info">
			<uni-section title="今日单据" type="line"></uni-section>
			<view class="info-area">
				<view class="info-item">
					<view class="title">采购入库单</view>
					<view class="num">{{statsInfo.purchase}}</view>
				</view>
				<view class="info-item">
					<view class="title">生产出库单</view>
					<view class="num">{{statsInfo.production}}</view>
				</view>
				<view class="info-item">
					<view class="title">通用出库单</view>
					<view class="num">{{statsInfo.common}}</view>
				</view>
			</view>
			<view class="info-area">
				<view class="info-item">
					<view class="title">采购退货单</view>
					<view class="num return">{{statsInfo.purchaseReturn}}</view>
				</view>
				<view class="info-item">
					<view class="title">生产退货单</view>
					<view class="num return">{{statsInfo.productionReturn}}</view>
				</view>
				<view class="info-item">
					<view class="title">通用退货单</view>
					<view class="num return">{{statsInfo.commonReturn}}</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	components: {  },
	data() {
		return {
			//图片域名
			imgDomain: this.$config.IMG_URL,
			//banner
			bannerList: [
				{bannerUrl: 'group1/M00/00/01/CgAEB2L0nEeAGofOAANCFp9KtjY342.jpg'},
			],
			//菜单
			menuList:[
				{ name: '入库单', icon: require('@/static/img/grid/stock-in.png'), url: 'pages/stock/stock_in' },
				{ name: '出库单', icon: require('@/static/img/grid/stock-out.png'), url: 'pages/stock/stock_out' },
				{ name: '上架', icon: require('@/static/img/grid/put-on.png'), url: 'pages/stock/put_on' },
				{ name: '下架', icon: require('@/static/img/grid/put-off.png'), url: 'pages/stock/put_off' },
				{ name: '入库退货', icon: require('@/static/img/grid/stock-in-return.png'), url: 'pages/stock/stock_in_return' },
				{ name: '出库退货', icon: require('@/static/img/grid/stock-out-return.png'), url: 'pages/stock/stock_out_return' },
				{ name: '货位库存', icon: require('@/static/img/grid/location-stock.png'), url: 'pages/stock/location' },
				{ name: '调拨单', icon: require('@/static/img/grid/allocation.png'), url: 'pages/stock/allocation' },
			],
			
			//数据
			statsInfo: {
				purchase: 0,
				purchaseReturn: 0,
				production: 0,
				productionReturn: 0,
				common: 0,
				commonReturn: 0,
			},
		}
	},
	onShow() {
		if (!uni.getStorageSync("vuex_token")) {
			this.$u.route('pages/user/login');
		}
	},
	onLoad() {
		this.loadData();
	},
	methods: {
		//加载数据
		async loadData(){
			this.$http({url: '/stats/indexUpper', method: 'GET', data: this.params}).then((res) => {
				if(res.code === 200){
					this.statsInfo = res.data;
				}else{
					uni.showToast({ title: res.msg, icon: 'none' })
				}
			});
		},
		//点击服务
		clickService(item){
			this.$u.route({ params: this.params, url: item.url });
		},
	},
}
</script>

<style lang="scss">
.banner{
	width: 100%;
	height: auto;
	box-sizing: border-box;
	padding: 10rpx 30rpx;
	background-color: white;
}
.menu{
	.grid-text {
		font-size: 14px;
		color: #909399;
		padding: 10rpx 0 20rpx 0rpx;
		/* #ifndef APP-PLUS */
		box-sizing: border-box;
		/* #endif */
	}
}
.info{
	.info-area{
		display: flex;
		justify-content: center;
		margin-bottom: 30rpx;
		.info-item{
			margin: 0px 15rpx 0 15rpx;
			padding: 15rpx 40rpx 15rpx 40rpx;
			border-radius: 12rpx;
			box-shadow: 0 0 20rpx 2rpx rgba(0, 0, 0, 0.1);
			.title{
				font-size: 26rpx;
			}
			.num{
				display: flex;
				justify-content: center;
				color: #3c9cff;
				margin-top: 10rpx;
			}
			.return{
				color: #f56c6c;
			}
		}
	}
}
</style>

