# MY FIRST VUE

> 因为项目需要使用Django和VUE。学习了一下VUE框架的使用，包括配置环境等等环节。

## 配置环境

使用的是IDEA作为编辑工具，找了一个教程，根据步骤一步一步来就可以完成。

- Windows使用IDEA创建VUE项目https://blog.csdn.net/weixin_42343424/article/details/86001889 
- 学习VUE.js官方教程 https://cn.vuejs.org/v2/guide/instance.html

## HelloWorld

> 跟第一个HelloWorld相关的只有两个文件。\src\main.js 和 \index.html

```
//main.js
import Vue from 'vue'
import router from './router'

Vue.config.productionTip = false

/* eslint-disable no-new */
var app = new Vue({
  el: '#app',
  router,
  data: {
    message: 'Hello Vue!'
  }
})
```

```
//main.html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>sun_first_vue</title>
  </head>
  <body>
    <div id="app">
      {{ message }}
    </div>
    <!-- built files will be auto injected -->
  </body>
</html>
```

- 然后点击run就可以直接执行啦。

## 条件语句 if （控制内容的显示与隐藏）

```
//main.js
var app = new Vue({
  el: '#app',
  router,
  data: {
    message: 'Hello Vue!'，
    seen：false
  }
})
```

```
//main.html
<div id="app">
  <p v-if="seen">
    {{ message }}
  </p>
</div>
```

## 循环语句 for（绑定数据，可以插入更新和删除元素）

```
//main.js
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
```

```
//main.html
  <div id="appfor">
      <ol>
        <li v-for="todo in todos">
          {{ todo.text }}
        </li>
      </ol>
    </div>
```

## 其他

- v-on 事件监听

  ```
  <!-- 完整语法 -->
  <a v-on:click="doSomething">...</a>
  <!-- 缩写 -->
  <a @click="doSomething">...</a>
  ```

- v-model 表单输入和应用状态之间的双向绑定

- v-bind 将待办事项传到循环输出的每个组件中

  ```
  <!-- 完整语法 -->
  <a v-bind:href="url">...</a>
  <!-- 缩写 -->
  <a :href="url">...</a>
  ```

- v-once 一次性插值，数据改变时，插值处的内容不会更新

- v-html 输出真正的html

  ```
  <p>Using mustaches: {{ rawHtml }}</p>
  <p>Using v-html directive: <span v-html="rawHtml"></span></p>
  ```

- 计算属性computed：

  ```
  <div id="example">
    <p>Original message: "{{ message }}"</p>
    <p>Computed reversed message: "{{ reversedMessage }}"</p>
  </div>
  ```

  ```
  var vm = new Vue({
    el: '#example',
    data: {
      message: 'Hello'
    },
    computed: {
      // 计算属性的 getter
      reversedMessage: function () {
        // `this` 指向 vm 实例
        return this.message.split('').reverse().join('')
      }
    }
  })
  ```

  