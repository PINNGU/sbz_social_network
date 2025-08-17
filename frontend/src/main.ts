import { createApp } from 'vue'
import App from './App.vue'
import router from './router'; // Import the router

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap'
import './assets/styles.css' // Import global styles

createApp(App).use(router).mount('#app') // Use the router
