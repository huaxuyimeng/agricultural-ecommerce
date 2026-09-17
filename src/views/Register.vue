/**
 * 注册页面
 * 文件路径: src/views/Register.vue
 * 功能描述: 用户注册入口页面，支持账号、密码、确认密码表单验证（密码一致性校验），
 *           注册成功后自动跳转到登录页面，品牌Logo展示、底部版权信息、已有账号去登录链接
 * 关联文件:
 * - src/api/index.js: 提供用户注册接口
 * - src/views/Login.vue: 登录页面（注册后跳转登录）
 */
<template>
  <div class="container">
    <div style="background-color: white; height: 70px;">
      <div style="margin: 0 auto; width: 50%; height: 100%; display: flex; align-items: center">
        <img src="@/assets/imgs/logo.png" alt="" style="width: 50px">
        <span style="font-weight: bold; font-size: 24px; margin-left: 5px">"绿源农鲜"农产品销售平台</span>
      </div>
    </div>

    <div style="margin: 0 auto; width: 50%; height: calc(100vh - 140px); display: flex; grid-gap: 20px; align-items: center">
      <div style="flex: 1">
        <img style="width: 100%;" src="@/assets/imgs/logo.png" alt="">
      </div>

      <div style="flex: 1">
        <div style="width: 350px; padding: 50px 30px; background-color: white; border-radius: 5px;">
          <div style="text-align: center; font-size: 20px; margin-bottom: 30px; color: #333">欢 迎 注 册</div>
          <el-form :model="form" :rules="rules" ref="formRef">
            <el-form-item prop="username">
              <el-input size="medium" prefix-icon="el-icon-user" placeholder="请输入账号" v-model="form.username"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input size="medium" autocomplete="new-password" prefix-icon="el-icon-lock" placeholder="请输入密码" show-password  v-model="form.password"></el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input size="medium" autocomplete="new-password" prefix-icon="el-icon-lock" placeholder="请确认密码" show-password  v-model="form.confirmPassword"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button size="medium" style="width: 100%; background-color: #4caf50; border-color: #4caf50; color: white" @click="register">注 册</el-button>
            </el-form-item>
            <div style="display: flex; align-items: center">
              <div style="flex: 1"></div>
              <div style="flex: 1; text-align: right">
                已有账号？请 <a href="/login">登录</a>
              </div>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <div style="height: 50px; line-height: 70px; color: #666; text-align: center">
      ©2024-2025 "绿源农鲜"农产品销售平台
    </div>

  </div>
</template>

<script>
import { register } from '@/api'

export default {
  name: "Register",
  data() {
    // 验证码校验
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('请确认密码'))
      } else if (confirmPass !== this.form.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: { role: 'USER', username: '', password: '', confirmPassword: '' },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        confirmPassword: [
          { validator: validatePassword, trigger: 'blur' },
        ]
      }
    }
  },
  created() {

  },
  methods: {
    async register() {
      this.$refs['formRef'].validate(async (valid) => {
        if (!valid) return

        try {
          const userSettings = JSON.parse(localStorage.getItem('xm-settings-user') || '{}')
          const defaultAvatar = userSettings.defaultAvatar || ''

          await register({
            username: this.form.username,
            password: this.form.password,
            name: this.form.username,
            role: 'USER',
            avatar: defaultAvatar
          })

          this.$message.success('注册成功，请登录')
          this.$router.push('/login')
        } catch (error) {
          console.error('注册失败:', error)
          this.$message.error(error.message || '注册失败，请稍后重试')
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  height: 100vh;
  overflow: hidden;
  background-color: #f6f6f6;
}
a {
  color: #2a60c9;
}
</style>