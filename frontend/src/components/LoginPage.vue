<template>
    <div class="container mt-5">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card">
            <div class="card-header text-white" style="background-color: #1877f2;">Login</div>
            <div class="card-body">
              <p>This is the login page. (Not yet implemented)</p>
              <router-link to="/register" class="btn btn-link">Register</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script lang="ts">
  import { defineComponent } from 'vue';
  
  export default defineComponent({
    name: 'LoginPage',
  });
  </script>
  
  <style scoped>
  /* Add any component-specific styles here */
  </style>
