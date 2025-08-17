<template>
    <nav class="navbar navbar-expand-lg navbar-dark" style="background-color: #1877f2;">
      <div class="container-fluid">
        <router-link class="navbar-brand" to="/">Social Network</router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <!-- Links for all users (for now) -->
            <li class="nav-item">
              <router-link class="nav-link" to="/global-posts">Global Posts</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/my-posts">My Posts</router-link>
            </li>
            <li class="nav-item">
                <router-link class="nav-link" to="/create-post">Create Post</router-link>
              </li>
            <!-- Admin-only link (for now, always visible) -->
            <li class="nav-item">
              <router-link class="nav-link" to="/create-place">Create Place</router-link>
            </li>
          </ul>
          <ul class="navbar-nav">
            <li class="nav-item">
              <router-link class="nav-link" to="/login">Login</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/register">Register</router-link>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </template>
  
  <script lang="ts">
  import { defineComponent } from 'vue';
  
  export default defineComponent({
    name: 'NavBar',
  });
  </script>
  
  <style scoped>
  /* No specific styles needed yet, Bootstrap handles most */
  </style>
