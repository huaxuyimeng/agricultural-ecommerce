/**
 * 404页面
 * 文件路径: src/views/404.vue
 * 功能描述: 全局404找不到页面提示，当用户访问不存在的路由时展示，包含404大字提示、引导文案、
 *           返回首页按钮和返回上一页按钮，提供友好的用户引导体验
 * 关联文件:
 * - src/router/index.js: 路由配置中作为通配符兜底路由
 */
<template>
  <div>
    <div style="height: 100vh; overflow: hidden; display: flex; align-items: center; justify-content: center; background: #f0f2f5;">
      <div style="text-align: center;">
        <div style="font-size: 100px; font-weight: 800; color: #667eea; margin-bottom: 20px;">404</div>
        <div style="font-size: 24px; color: #333; margin-bottom: 20px;">页面不存在</div>
        <div style="font-size: 16px; color: #666; margin-bottom: 30px;">
          抱歉，您访问的页面不存在或已被移除
        </div>
        <div style="display: flex; gap: 12px; justify-content: center; align-items: center;">
          <el-button 
            type="primary" 
            @click="goHome"
            style="padding: 10px 20px; border-radius: 6px; font-weight: 600;"
          >
            <i class="el-icon-s-home"></i>
            返回首页
          </el-button>
          <el-button 
            @click="goBack"
            style="padding: 10px 20px; border-radius: 6px; font-weight: 600; border-color: #667eea; color: #667eea;"
          >
            <i class="el-icon-arrow-left"></i>
            返回上一页
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "NotFound",  // 改为合法的组件名
  data() {
    return {}
  },
  methods: {
    goHome() {
      const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
      
      if (user.role === 'ADMIN') {
        this.$router.push('/home')
      } else if (user.role === 'MERCHANT') {
        this.$router.push('/front/home')
      } else if (user.role === 'USER') {
        this.$router.push('/front/home')
      } else {
        this.$router.push('/login')
      }
    },
    goBack() {
      this.$router.go(-1)
    }
  },
  mounted() {
    setTimeout(() => {
      this.goHome()
    }, 3000)
  }
}
</script>

<style scoped>
/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

div[style*="text-align: center"] {
  animation: fadeIn 0.5s ease;
}
</style>