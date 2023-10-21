<template>
	<view class="content">
		<view class="login_area">
			<!-- <view class="logo">
				<u-image shape="square" width="200" height="200" :src="logoImg" ></u-image>
			</view> -->
			
			<view class="tips">
				<u-checkbox-group v-model="agreeValue" placement="column" @change="checkboxChange" activeColor="#f5a300">
					<u-checkbox :shape="'circle'" :key="1" :name="'agree'"></u-checkbox>
				</u-checkbox-group>
				<view class="words">阅读并同意</view>
				<view class="rule">《个人协议及个人信息处理规则》</view>
			</view>
			<u-button icon="weixin-circle-fill" text="微信用户一键登录" size="large" :customStyle="'height: 80rpx'" type="success" 
				@click="doLogin"></u-button>
		</view>
	</view>
</template>

<script>
export default {
	components: {  },
	data() {
		return {
			agreeValue: ['agree'],
			
			// logoImg: require('@/static/img/user/logo.png'),
			
			wechatStatus: '',
			
			sourcePage: '',
			
			//参数
			params: {},
		}
	},
	onShow() {
		//登录检测
		let flag = this.checkLogin();
		if(flag){
			this.$u.route({ type: 'switchTab', url: 'pages/home/index' });
		}else{
			this.checkWxAuth();
		}
	},
	onLoad(options) {
		this.sourcePage = options.sourcePage;
	},
	methods: {
		checkboxChange(e){
			console.log(e)
		},
		//检测登录
		checkLogin(){
			let vuex_token = uni.getStorageSync("vuex_token");
			if(!vuex_token){
				return false;
			}else{
				return true;
			}
		},
		//微信检测授权
		checkWxAuth(){
			let that = this;
			wx.login({
				success(res){
					let jsCode = res.code;
					that.$http({url: '/wechat/auth/checkAuth', method: 'GET', data: {jsCode: jsCode}}).then((authRes) => {
						if(authRes.code === 200){
							that.wechatStatus = 'auth';
						}else if(authRes.code === 4011){
							that.wechatStatus = 'unauth';
						}
					});
				}
			});
		},
		//进行登录
		doLogin(){
			if(this.wechatStatus === 'auth'){
				this.wxLogin();
			}else{
				this.wxRegisterAndLogin();
			}
		},
		//进行授权、登记用户信息
		wxRegisterAndLogin(){
			let that = this;
			wx.getUserProfile({
				desc: '获取你的昵称、头像、地区及性别',
				success(profileRes){
					wx.login({
						success(loginRes){
							let jsCode = loginRes.code;
							let userInfo = JSON.stringify(profileRes.userInfo);
							that.$http({url: '/wechat/auth/register', method: 'POST', data: {jsCode: jsCode, userInfo: userInfo}}).then((regRes) => {
								if(regRes.code === 200){
									that.setVuexAttr(regRes);
								}else{
									uni.showToast({ title: regRes.msg, icon: 'none' })
								}
							});
						}
					});
				}
			})
		},
		//微信登录
		wxLogin(){
			let that = this;
			wx.login({
				success(loginRes){
					let jsCode = loginRes.code;
					that.$http({url: '/wechat/auth/wxLogin', method: 'POST', data: {jsCode: jsCode}}).then((logRes) => {
						if(logRes.code === 200){
							that.setVuexAttr(logRes);
						}else{
							uni.showToast({ title: logRes.msg, icon: 'none' })
						}
					});
				}
			});
		},
		setVuexAttr(res){
			//用户基本信息
			let user = {
				id: res.data.id,
				username: res.data.username, 
				nickName: res.data.nickName, 
				avatar: res.data.avatar,
				userType: res.data.userType,
			};
			uni.setStorageSync('vuex_user', JSON.stringify(user));
			uni.setStorageSync('vuex_token', res.data.Authorization);
			if(this.sourcePage === 'home'){
				this.$u.route({ type: 'switchTab', url: 'pages/home/index' });
			}else if(this.sourcePage === 'member'){
				this.$u.route({ type: 'switchTab', url: 'pages/member/index' });
			}else{
				this.$u.route({ type: 'switchTab', url: 'pages/member/index' });
			}
		},
	},
}
</script>

<style lang="scss">
.content{
	height: calc(100vh);
	padding: 180rpx 50rpx 0 50rpx;
	display: flex;
	justify-content: center;
	.login_area{
		.logo{
			display: flex;
			align-items: center;
			justify-content: center;
			padding-bottom: 40rpx;
		}
		.tips{
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 30rpx;
			font-size: 26rpx;
			.words{
				color: #8a8a8a;
			}
			.rule{
				color: #f5a300;
			}
		}
		button{
			width: 650rpx;
		}
	}
}

</style>