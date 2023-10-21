<template>
	<view class="login-bg">
		<image class="img-a" src="@/static/img/login/bg1.png"></image>
		<view class="t-login">
			<view class="t-b">登 录</view>
			<form class="cl">
				<view class="t-a">
					<image src="@/static/img/login/user.png"></image>
					<input type="text" placeholder="请输入账号" maxlength="20" v-model="params.username" />
				</view>
				<view class="t-a">
					<image src="@/static/img/login/pwd.png"></image>
					<input type="password" placeholder="请输入密码" maxlength="20" v-model="params.password" />
				</view>
				<button class="t-btn" @tap="login()">账号登录</button>
			</form>
		</view>
		<view class="cardBox">
			<view>
				内部系统，请联系管理员.
			</view>
		</view>
		<image class="img-b" src="@/static/img/login/bg2.png"></image>
		
	</view>
</template>
<script>
export default {
	data() {
		return {
			//参数
			params:{
				username: '',
				password: '',
			},
		};
	},
	onLoad() {
		
	},
	methods: {
		//登录
		login() {
			this.$http({url: '/mLogin', method: 'POST', data: this.params}).then((res) => {
				if(res.code === 200){
					this.setVuexAttr(res);
				}else{
					uni.showToast({ title: res.msg, icon: 'none' })
				}
			});
		},
		setVuexAttr(res){
			uni.setStorageSync('vuex_token', res.token);
			//跳转
			this.$nextTick(function(){
				this.$u.route({ type: 'switchTab', url: 'pages/home/index' });
			});
		},
	}
};
</script>
<style lang="scss" scoped>
.img-a {
	width: 100%;
}
.img-b {
	width: 100%;
	height: 45px;
	bottom: 0;
	position: absolute;
}
.login-bg {
	height: 100vh;
	background: linear-gradient(to bottom, #3776ff, #85cafe);
}

.t-login {
	width: 580rpx;
	padding: 55rpx;
	margin: 0 auto;
	font-size: 28rpx;
	background-color: #ffffff;
	border-radius: 20rpx;
	position: relative;
	margin-top: -200rpx;
	box-shadow: 0 5px 7px 0 rgba(0, 0, 0, 0.15);
	z-index: 9;
}
.t-login button {
	font-size: 28rpx;
	height: 70rpx;
	line-height: 70rpx;
	border-radius: 50rpx;
}

.t-login input {
	padding: 0 20rpx 0 120rpx;
	height: 90rpx;
	line-height: 90rpx;
	margin-bottom: 20rpx;
	background: #f6f6f6;
	border: 1px solid #f6f6f6;
	font-size: 28rpx;
	border-radius: 50rpx;
}

.t-login .t-a {
	position: relative;
}

.t-btn {
	background: linear-gradient(to right, #78c5ff, #61a8fe);
	color: #fff;
	margin-top: 50rpx;
}

.btn-wx-auth {
	margin-top: 30rpx;
	color: #fff;
	background: linear-gradient(to right, #48ba6c, #41a863);
}

.t-login .t-a image {
	width: 40rpx;
	height: 40rpx;
	position: absolute;
	left: 40rpx;
	top: 28rpx;
	padding-right: 20rpx;
}

.t-login .t-b {
	text-align: left;
	font-size: 46rpx;
	color: #66bdff;
	font-weight: bold;
	margin: 0 0 50rpx 20rpx;
}

.t-login .t-d {
	text-align: center;
	color: #999;
	margin: 80rpx 0;
}

.t-login .t-c {
	text-align: right;
	color: #c0c0c0;
	margin: -20rpx 30rpx 40rpx 0;
}

.t-login .t-f {
	text-align: center;
	margin: 200rpx 0 0 0;
	color: #666;
}

.t-login .t-f text {
	margin-left: 20rpx;
	color: #aaaaaa;
	font-size: 27rpx;
}

.t-login .uni-input-placeholder {
	color: #aeaeae;
}

.cl {
	zoom: 1;
}

.cl:after {
	clear: both;
	display: block;
	visibility: hidden;
	height: 0;
	content: '\20';
}
.cardBox {
	-webkit-box-orient: horizontal;
	-webkit-box-direction: normal;
	-webkit-flex-direction: row;
	flex-direction: row;
	-webkit-box-align: center;
	-webkit-align-items: center;
	align-items: center;
	// padding: 5px;
	background: #ffffff;
	opacity: 0.9;
	-webkit-border-radius: 20rpx;
	border-radius: 0 0 20rpx 20rpx;
	height: 70rpx;
	width: 500rpx;
	margin: 0 auto;
	position: relative;
	text-align: center;
	line-height: 70rpx;
	color: #aaa;
	font-size: 28rpx;
}
.cardBox .txt {
	color: #4bb7fe;
}
</style>
