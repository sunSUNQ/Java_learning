// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
// import App from './App'
import router from './router'

Vue.config.productionTip = false

/* eslint-disable no-new */
var app = new Vue({
  el: '#app',
  router,
  // components: { App },
  // template: '<App/>',
  data: {
    message: 'Hello Vue!',
    seen: false
  }
})

// app.message = 'change the message'

var appfor = new Vue({
  el: '#appfor',
  data: {
    todos: [
      { text: 'one' },
      { text: 'two' },
      { text: 'three' }
    ]
  }
})


