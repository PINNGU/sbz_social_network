import { createApp } from 'vue'
import App from './App.vue'
import router from './router';
import axios from 'axios';


import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap' // Import Bootstrap as a whole module
import './assets/styles.css'


createApp(App).use(router).mount('#app')
