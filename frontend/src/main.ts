import { createApp } from 'vue'
import App from './App.vue'
import router from './router';
import axios from 'axios';
import { getToken } from './auth';

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap'
import './assets/styles.css'

// Axios interceptor to add JWT token to requests
axios.interceptors.request.use(
    config => {
        const token = getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

createApp(App).use(router).mount('#app')
