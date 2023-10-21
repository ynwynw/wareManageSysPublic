<template>
	<view class="content">
		<view class="header">
			<u-avatar :src="user.avatar ? BASE_URL + user.avatar : defaultAvatar" size="50"></u-avatar>
			<view class="user-info">
				<view class="">{{user.nickName}}</view>
				<view class="">部门：{{user.dept ? user.dept.deptName : ''}}</view>
			</view>
		</view>
		<u-gap bgColor="#d3d3d3" height="1"></u-gap>
		<view class="action">
			<u-button type="error" :customStyle="'width: 80%'" @click="logout">退出登录</u-button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				//当前用户
				user: { },
				defaultAvatar: require('@/static/img/user/default.png'),
				//参数
				params: { },
				
				BASE_URL: this.$config.BASE_URL,
			}
		},
		onShow() {
			if (!uni.getStorageSync("vuex_token")) {
				this.$u.route('pages/user/login');
			}
			this.loadUserInfo();
		},
		methods: {
			//加载用户信息
			loadUserInfo(){
				this.$http({url: '/getInfo', method: 'GET', data: this.params}).then((res) => {
					if(res.code === 200){
						this.user = res.user;
					}else{
						uni.showToast({ title: res.msg, icon: 'none' })
					}
				});
			},
			//退出
			logout(){
				let that = this;
				uni.showModal({
					title: '提示',
					content: '确认退出登录？',
					success: function (res) {
						if (res.confirm) {
							that.$http({url: '/logout', method: 'POST', data: that.params}).then((res) => {
								if(res.code === 200){
									uni.removeStorageSync('vuex_token');
									//跳转
									that.$nextTick(function(){
										that.$u.route('pages/user/login');
									});
								}else{
									uni.showToast({ title: res.msg, icon: 'none' })
								}
							});
						}
					}
				});
			},
		}
	}
</script>

<style lang="scss">
.content{
	padding: 20rpx;
}
.header{
	display: flex;
	align-items: center;
	padding-bottom: 40rpx;
	.user-info{
		margin-left: 40rpx;
	}
}
.action{
	display: flex;
	justify-content: center;
	padding-top: 100rpx;
	.logout{
		color: red;
	}
}
</style>
