<template>
  <div class="dashboard-container">
    <h2 class="dashboard-title">My Posts</h2>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div v-if="posts.length === 0" class="no-posts">You haven't created any posts yet.</div>
      <div class="posts-grid">
        <div v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-header">
            <span class="post-date">{{ formatDate(post.dateOfCreation) }}</span>
          </div>
          <div class="post-body">
            <p class="post-description">{{ post.description }}</p>
            <div v-if="post.hashtags && post.hashtags.length" class="hashtags">
              <span v-for="tag in post.hashtags" :key="tag" class="hashtag">{{ tag }}</span>
            </div>
          </div>
          <div class="post-footer">
            <span class="likes">👍 {{ post.numberOfLikes }}</span>
            <span class="reports" v-if="post.reports && post.reports.length">🚩 {{ post.reports.length }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
  
<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';

interface Post {
  id: number;
  description: string;
  hashtags: string[];
  numberOfLikes: number;
  reports: number[];
  dateOfCreation: string;
  user: { id: number };
}

export default defineComponent({
  name: 'MyPosts',
  setup() {
    const posts = ref<Post[]>([]);
    const loading = ref(true);
    const error = ref('');

    const fetchPosts = async () => {
      loading.value = true;
      error.value = '';
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) {
          error.value = 'You must be logged in to view your posts.';
          loading.value = false;
          return;
        }
        const user = JSON.parse(userStr);
        const response = await axios.get(`/api/posts?userId=${user.id}`);
        posts.value = response.data.filter((post: Post) => post.user && post.user.id === user.id);
      } catch (err: any) {
        error.value = err.response?.data?.message || err.response?.data || 'Failed to fetch posts.';
      } finally {
        loading.value = false;
      }
    };

    const formatDate = (dateStr: string) => {
      const date = new Date(dateStr);
      return date.toLocaleString();
    };

    onMounted(fetchPosts);

    return {
      posts,
      loading,
      error,
      formatDate,
    };
  },
});
</script>
  
<style scoped>
.dashboard-container {
  max-width: 900px;
  margin: 2rem auto;
  padding: 2rem;
  background: #f0f2f5;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.dashboard-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  color: #1877f2;
  text-align: center;
}
.loading, .error, .no-posts {
  text-align: center;
  margin: 2rem 0;
  font-size: 1.2rem;
}
.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}
.post-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s;
}
.post-card:hover {
  box-shadow: 0 4px 16px rgba(24,119,242,0.12);
}
.post-header {
  display: flex;
  justify-content: flex-end;
  font-size: 0.9rem;
  color: #65676b;
  margin-bottom: 0.5rem;
}
.post-body {
  flex: 1;
}
.post-description {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}
.hashtags {
  margin-top: 0.5rem;
}
.hashtag {
  background: #e7f3ff;
  color: #1877f2;
  border-radius: 4px;
  padding: 0.2rem 0.5rem;
  margin-right: 0.3rem;
  font-size: 0.95rem;
}
.post-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  font-size: 0.95rem;
  color: #65676b;
  margin-top: 0.5rem;
}
.likes {
  display: flex;
  align-items: center;
}
.reports {
  display: flex;
  align-items: center;
}
</style>
