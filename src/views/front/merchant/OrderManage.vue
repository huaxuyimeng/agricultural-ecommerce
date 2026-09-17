/**
 * 商户订单管理页面
 * 文件路径: src/views/front/merchant/OrderManage.vue
 * 功能描述: 商户订单管理页面，用于管理商户的订单信息
 * 关联文件:
 * - src/views/front/merchant/Dashboard.vue: 商家中心页面
 * - src/views/front/merchant/MyProducts.vue: 我的商品页面
 * - src/views/front/MyOrders.vue: 用户订单页面
 */
<template>
  <MyOrders />
</template>

<script>
import MyOrders from '../MyOrders.vue'

export default {
  name: 'OrderManage',
  components: {
    MyOrders
  }
}
</script>
