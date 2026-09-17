<template>
  <footer class="site-footer">
    <div class="footer-content">
      <div class="footer-main">
        <!-- 品牌信息 -->
        <div class="footer-brand">
          <div class="brand-logo">
            <i class="el-icon-shopping-bag-2"></i>
            <span class="brand-name">农情汇</span>
          </div>
          <p class="brand-slogan">源自田间，鲜达餐桌</p>
          <p class="brand-desc">专注农产品电商，为您提供新鲜、优质的农产品，让健康生活走进千家万户。</p>
          <div class="social-links">
            <a href="javascript:void(0)" class="social-link" title="微信">
              <i class="el-icon-chat-dot-round"></i>
            </a>
            <a href="javascript:void(0)" class="social-link" title="微博">
              <i class="el-icon-bell"></i>
            </a>
            <a href="javascript:void(0)" class="social-link" title="抖音">
              <i class="el-icon-video-camera"></i>
            </a>
          </div>
        </div>

        <!-- 快速链接 -->
        <div class="footer-links">
          <h4 class="footer-title">快速链接</h4>
          <ul class="link-list">
            <li><a href="/front">首页</a></li>
            <li><a href="/front/products">全部商品</a></li>
            <li><a href="/front/news">资讯中心</a></li>
            <li><a href="/front/user">个人中心</a></li>
          </ul>
        </div>

        <!-- 客户服务 -->
        <div class="footer-links">
          <h4 class="footer-title">客户服务</h4>
          <ul class="link-list">
            <li><a href="javascript:void(0)">购物指南</a></li>
            <li><a href="javascript:void(0)">配送方式</a></li>
            <li><a href="javascript:void(0)">支付方式</a></li>
            <li><a href="javascript:void(0)">售后服务</a></li>
          </ul>
        </div>

        <!-- 联系方式 -->
        <div class="footer-contact">
          <h4 class="footer-title">联系我们</h4>
          <div class="contact-item">
            <i class="el-icon-phone"></i>
            <span>400-888-8888</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-message"></i>
            <span>service@nongqinghui.com</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-location"></i>
            <span>北京市朝阳区农业中关村</span>
          </div>
          <div class="contact-item">
            <i class="el-icon-clock"></i>
            <span>周一至周日 9:00-21:00</span>
          </div>
        </div>
      </div>

      <!-- 底部版权 -->
      <div class="footer-bottom">
        <div class="copyright">
          <p>&copy; 2026 农情汇 Agricultural E-commerce. All rights reserved.</p>
        </div>
        <div class="footer-nav">
          <a href="javascript:void(0)">隐私政策</a>
          <span class="divider">|</span>
          <a href="javascript:void(0)">使用条款</a>
          <span class="divider">|</span>
          <a href="javascript:void(0)">网站地图</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'SiteFooter'
}
</script>

<style scoped>
.site-footer {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: #ecf0f1;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-main {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1.2fr;
  gap: 40px;
  padding: 50px 0 30px;
}

/* 品牌信息 */
.footer-brand {
  display: flex;
  flex-direction: column;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.brand-logo i {
  font-size: 32px;
  color: #4caf50;
}

.brand-name {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 14px;
  color: #4caf50;
  margin: 0 0 12px;
  font-weight: 500;
}

.brand-desc {
  font-size: 13px;
  color: #95a5a6;
  line-height: 1.7;
  margin: 0 0 20px;
}

.social-links {
  display: flex;
  gap: 12px;
}

.social-link {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ecf0f1;
  transition: all 0.3s ease;
}

.social-link:hover {
  background: #4caf50;
  transform: translateY(-3px);
}

.social-link i {
  font-size: 16px;
}

/* 链接区域 */
.footer-links {
  display: flex;
  flex-direction: column;
}

.footer-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #4caf50;
  display: inline-block;
}

.link-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.link-list li a {
  color: #95a5a6;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
  display: inline-block;
}

.link-list li a:hover {
  color: #4caf50;
  padding-left: 5px;
}

/* 联系方式 */
.footer-contact {
  display: flex;
  flex-direction: column;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #95a5a6;
}

.contact-item i {
  font-size: 16px;
  color: #4caf50;
  width: 20px;
  text-align: center;
}

/* 底部版权 */
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.copyright {
  flex: 1;
}

.copyright p {
  margin: 0;
  font-size: 13px;
  color: #7f8c8d;
}

.footer-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-nav a {
  color: #7f8c8d;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.3s ease;
}

.footer-nav a:hover {
  color: #4caf50;
}

.footer-nav .divider {
  color: #7f8c8d;
}

/* 响应式 */
@media (max-width: 992px) {
  .footer-main {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .footer-main {
    grid-template-columns: 1fr;
    gap: 30px;
    padding: 40px 0 20px;
  }

  .footer-brand {
    text-align: center;
    align-items: center;
  }

  .brand-logo {
    justify-content: center;
  }

  .social-links {
    justify-content: center;
  }

  .footer-links,
  .footer-contact {
    text-align: center;
  }

  .footer-title {
    display: block;
    text-align: center;
  }

  .contact-item {
    justify-content: center;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
  }
}
</style>
