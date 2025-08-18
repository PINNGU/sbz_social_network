<template>
    <nav class="navbar navbar-expand-lg navbar-dark" style="background-color: #1877f2;">
      <div class="container-fluid">
        <router-link class="navbar-brand" to="/">Social Network</router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">
            <li class="nav-item" v-if="isLoggedIn">
              <router-link class="nav-link" to="/global-posts">Global Posts</router-link>
            </li>
            <li class="nav-item" v-if="isLoggedIn && isRegular">
              <router-link class="nav-link" to="/my-posts">My Posts</router-link>
            </li>
            <li class="nav-item" v-if="isLoggedIn && isRegular">
                <router-link class="nav-link" to="/create-post">Create Post</router-link>
              </li>
            <li class="nav-item" v-if="isLoggedIn && isAdmin">
              <router-link class="nav-link" to="/create-place">Create Place</router-link>
            </li>
          </ul>
          <ul class="navbar-nav">
            <li class="nav-item" v-if="!isLoggedIn">
              <router-link class="nav-link" to="/login">Login</router-link>
            </li>
            <li class="nav-item" v-if="!isLoggedIn">
              <router-link class="nav-link" to="/register">Register</router-link>
            </li>
            <li class="nav-item" v-if="isLoggedIn">
              <a class="nav-link" @click.prevent="handleLogout" href="#">Logout</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  </template>
  
  <script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { isLoggedIn, getRole, logout } from '../auth';
  
  export default defineComponent({
    name: 'NavBar',
    // emits: ['open-create-post-modal'], // Declare the emitted event
    setup(props) {
      const router = useRouter();
  
      const handleLogout = () => {
        logout();
        router.push('/login');
      };

      // const openCreatePostModal = () => {
      //   emit('open-create-post-modal'); // Emit the event
      //   console.log('Attempting to open create post modal...'); // For debugging
      // };
  
      const isAdmin = computed(() => {
        return getRole() === 'ADMIN';
      });

      const isRegular = computed(() => {
        return getRole() === 'REGULAR';
      });

      return {
        isLoggedIn: computed(() => isLoggedIn()),
        isAdmin,
        isRegular,
        handleLogout,
        // openCreatePostModal,
      };
    },
  });
  </script>
  
  <style scoped>
  /* No specific styles needed yet, Bootstrap handles most */
  </style>
