<script lang="ts">
import { defineComponent } from 'vue';
import NavBar from './components/NavBar.vue';
// import CreatePost, { type CreatePostModalRef } from './components/CreatePost.vue'; // Import the interface

export default defineComponent({
  name: 'App',
  components: {
    NavBar,
    // CreatePost, // Register CreatePost component
  },
  // setup() {
  //   const createPostModalRef = ref<CreatePostModalRef | null>(null); // Use the interface for typing

  //   const handleOpenCreatePostModal = () => {
  //     if (createPostModalRef.value) {
  //       createPostModalRef.value.showModal();
  //     }
  //   };

  //   return {
  //     createPostModalRef,
  //     handleOpenCreatePostModal,
  //   };
  // },
});
</script>

<template>
  <div id="app">
    <NavBar />
    <router-view />
    <!-- <CreatePost ref="createPostModalRef" /> -->
  </div>
</template>

<style>
/* Global styles here */
</style>
