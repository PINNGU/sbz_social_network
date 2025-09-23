<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useRoute } from 'vue-router';
import NavBar from './components/NavBar.vue';
import AdsSidebar from './components/AdsSidebar.vue';
import { isLoggedIn } from './auth';
// import CreatePost, { type CreatePostModalRef } from './components/CreatePost.vue'; // Import the interface

export default defineComponent({
  name: 'App',
  components: {
    NavBar,
    AdsSidebar,
  },
  setup() {
    const route = useRoute();
    const showAds = computed(() => {
      const name = route.name as string | undefined;
      // don't show on login or register, and only when logged in
      if (!isLoggedIn()) return false;
      if (name === 'Login' || name === 'Register') return false;
      return true;
    });

    return { showAds };
  },
});
</script>

<template>
  <div id="app">
    <NavBar />
    <div class="app-body d-flex">
      <div class="app-main flex-grow-1 p-3">
        <router-view />
      </div>
      <template v-if="showAds">
        <AdsSidebar />
      </template>
    </div>
    <!-- <CreatePost ref="createPostModalRef" /> -->
  </div>
</template>

<style>
/* Global styles here */
</style>
