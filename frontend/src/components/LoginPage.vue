<template>
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card">
            <div class="card-header text-white" style="background-color: #1877f2;">Login</div>
            <div class="card-body">
              <form @submit.prevent="handleLogin">
                <div class="mb-3">
                  <label for="email" class="form-label">Email address</label>
                  <input type="email" class="form-control" :class="{'is-invalid': errors.email}" id="email" v-model="user.email">
                  <div class="invalid-feedback">{{ errors.email }}</div>
                </div>
                <div class="mb-3">
                  <label for="password" class="form-label">Password</label>
                  <input type="password" class="form-control" :class="{'is-invalid': errors.password}" id="password" v-model="user.password">
                  <div class="invalid-feedback">{{ errors.password }}</div>
                </div>
                <div v-if="loginError" class="alert alert-danger">{{ loginError }}</div>
                <button type="submit" class="btn text-white" style="background-color: #1877f2;">Login</button>
              </form>
              <router-link to="/register" class="btn btn-link">Register</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script lang="ts">
  import { defineComponent, ref } from 'vue';
  import axios from 'axios';
  import { useRouter } from 'vue-router';
  import { setAuthData } from '../auth'; // Import setAuthData
  
  export default defineComponent({
    name: 'LoginPage',
    setup() {
      const user = ref({
        email: '',
        password: '',
      });
      const errors = ref<{[key: string]: string}>({});
      const loginError = ref<string | null>(null);
      const router = useRouter();

      const validateForm = () => {
        errors.value = {};
        let isValid = true;

        if (!user.value.email) {
          errors.value.email = 'Email is required';
          isValid = false;
        } else if (!/^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/.test(user.value.email)) {
          errors.value.email = 'Invalid email format';
          isValid = false;
        }

        if (!user.value.password) {
          errors.value.password = 'Password is required';
          isValid = false;
        }

        return isValid;
      };

      const handleLogin = async () => {
        loginError.value = null;
        if (validateForm()) {
          try {
            const response = await axios.post('/api/auth/login', user.value);
            if (response.status === 200) {
              console.log('Login successful!', response.data);
              // Store JWT token and user info (e.g., in localStorage or Vuex store)
              setAuthData( response.data.id, response.data.email, response.data.role);
              router.push('/global-posts'); // Redirect to a protected page
            } else {
              loginError.value = 'Login failed with status: ' + response.status;
            }
          } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
              loginError.value = error.response.data.message || 'Login failed.';
            } else {
              loginError.value = 'An error occurred during login.';
            }
            console.error('Login error:', error);
          }
        }
      };

      return {
        user,
        errors,
        loginError,
        handleLogin,
      };
    },
  });
  </script>
  
  <style scoped>
  /* Add any component-specific styles here */
  </style>
