/**
 * 发布公告页面
 * 文件路径: src/views/manager/PublishNotice.vue
 * 功能描述: 创建并发布新的系统公告通知，支持设置公告标题、内容、类型、发布范围，
 *           草稿保存和立即发布，发布成功后跳转回公告管理页面，支持返回上页
 * 关联文件:
 * - src/api/index.js: 提供公告创建接口
 * - src/views/manager/Notice.vue: 公告管理列表页面
 */
<template>
  <div class="publish-notice-page">
    <div class="page-header">
      <div class="header-left">
        <el-button type="text" @click="$router.back()" class="back-btn">
          <i class="el-icon-arrow-left"></i> 返回
        </el-button>
        <h2 class="page-title">
          <i class="el-icon-s-promotion"></i>
          发布公告
        </h2>
        <p class="page-subtitle">创建并发布新的系统公告</p>
      </div>
      <div class="header-right">
        <el-button @click="handleReset">
          <i class="el-icon-refresh"></i> 重置
        </el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          <i class="el-icon-circle-check"></i> 发布公告
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="publish-form">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit clearable prefix-icon="el-icon-edit" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="公告分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择公告分类" style="width: 100%;" clearable>
                <el-option label="政策" value="policy" />
                <el-option label="科技" value="technology" />
                <el-option label="活动" value="activity" />
                <el-option label="公告" value="notice" />
                <el-option label="新闻" value="news" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" maxlength="50" clearable prefix-icon="el-icon-user" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="公告内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入公告内容" maxlength="5000" show-word-limit resize="vertical" />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：农业,科技,政策" clearable prefix-icon="el-icon-price-tag" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { createNews } from '@/api'

export default {
  name: 'PublishNotice',
  data() {
    return {
      submitting: false,
      form: {
        title: '',
        category: '',
        author: '',
        content: '',
        tags: ''
      },
      rules: {
        title: [
          { required: true, message: '请输入公告标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        category: [{ required: true, message: '请选择公告分类', trigger: 'change' }],
        author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
        content: [
          { required: true, message: '请输入公告内容', trigger: 'blur' },
          { min: 5, max: 5000, message: '长度在 5 到 5000 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleReset() {
      this.$refs.formRef.resetFields()
      this.$message.success('表单已重置')
    },
    async handleSubmit() {
      try {
        const valid = await this.$refs.formRef.validate()
        if (!valid) return

        this.submitting = true
        await createNews(this.form)
        this.$message.success('公告发布成功')
        this.$router.push('/notice')
      } catch (error) {
        if (error !== false) {
          this.$message.error('发布失败，请重试')
        }
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.publish-notice-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
  color: #606266;
  padding: 0;
}

.back-btn:hover {
  color: #409eff;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title i {
  color: #409eff;
}

.page-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 4px 0 0 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

.form-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.publish-form {
  padding: 10px 0;
}
</style>
