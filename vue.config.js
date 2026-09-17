const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  publicPath: '/',
  transpileDependencies: true,
  devServer: {
    port: 8080,
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        ws: true
      },
      '/imgs': {
        target: 'http://localhost:9090',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://localhost:9090',
        changeOrigin: true
      }
    }
  },
  chainWebpack: config => {
    config.plugin('html')
      .tap(args => {
        args[0].title = "管理系统";
        return args;
      })
  }
})
