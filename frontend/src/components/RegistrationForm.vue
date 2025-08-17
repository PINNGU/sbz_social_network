<template>
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card">
            <div class="card-header text-white" style="background-color: #1877f2;">Register</div>
            <div class="card-body">
              <form @submit.prevent="handleSubmit">
                <div class="mb-3">
                  <label for="name" class="form-label">Name</label>
                  <input type="text" class="form-control" :class="{'is-invalid': errors.name}" id="name" v-model="user.name">
                  <div class="invalid-feedback">{{ errors.name }}</div>
                </div>
                <div class="mb-3">
                  <label for="surname" class="form-label">Surname</label>
                  <input type="text" class="form-control" :class="{'is-invalid': errors.surname}" id="surname" v-model="user.surname">
                  <div class="invalid-feedback">{{ errors.surname }}</div>
                </div>
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
                <div class="mb-3">
                  <label for="address" class="form-label">Address</label>
                  <input type="text" class="form-control" :class="{'is-invalid': errors.address}" id="address" v-model="user.address">
                  <div class="invalid-feedback">{{ errors.address }}</div>
                </div>
                <div v-if="registrationError" class="alert alert-danger">{{ registrationError }}</div>
                <button type="submit" class="btn text-white" style="background-color: #1877f2;">Register</button>
              </form>
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
  
  export default defineComponent({
    name: 'RegistrationForm',
    setup() {
      const user = ref({
        name: '',
        surname: '',
        email: '',
        password: '',
        address: '',
      });
      const errors = ref<{[key: string]: string}>({});
      const registrationError = ref<string | null>(null);
      const router = useRouter(); // Initialize router
  
      const validateForm = () => {
        errors.value = {};
        let isValid = true;
  
        if (!user.value.name) {
          errors.value.name = 'Name is required';
          isValid = false;
        }
        if (!user.value.surname) {
          errors.value.surname = 'Surname is required';
          isValid = false;
        }
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
        if (!user.value.address) {
          errors.value.address = 'Address is required';
          isValid = false;
        }
        return isValid;
      };
  
      const handleSubmit = async () => {
        registrationError.value = null;
        if (validateForm()) {
          try {
            const response = await axios.post('/api/register', user.value);
            if (response.status === 201) { // HttpStatus.CREATED
              console.log('Registration successful!', response.data);
              router.push('/login'); // Redirect to login page
            } else {
              // This block might not be reached if Axios throws on non-2xx status
              registrationError.value = 'Registration failed with status: ' + response.status;
            }
          } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
              registrationError.value = error.response.data || 'Registration failed.';
            } else {
              registrationError.value = 'An error occurred during registration.';
            }
            console.error('Registration error:', error);
          }
        }
      };
  
      return {
        user,
        errors,
        registrationError,
        handleSubmit,
      };
    },
  });
  </script>
  
  <style scoped>
  /* Add any component-specific styles here */
  </style>
