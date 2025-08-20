<template>
  <div class="dashboard-container">
    <h2 class="dashboard-title">Global Posts</h2>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div v-if="posts.length === 0" class="no-posts">No global posts to show.</div>
      <div class="posts-grid">
  <div v-for="postWithReason in posts" :key="postWithReason.post.id" :class="['post-card', { 'for-you': postWithReason.reasons.includes('for_you') }]">
          <div class="post-header">
            <span class="post-date">{{ formatDate(postWithReason.post.dateOfCreation) }}</span>
            <span class="post-user">by {{ postWithReason.post.user.name }} {{ postWithReason.post.user.surname }}</span>
          </div>
          <div class="post-body">
            <p class="post-description">{{ postWithReason.post.description }}</p>
            <div v-if="postWithReason.post.hashtags && postWithReason.post.hashtags.length" class="hashtags">
              <span v-for="tag in postWithReason.post.hashtags" :key="tag" class="hashtag">{{ tag }}</span>
            </div>
          </div>
          <div class="post-footer">
            <span class="likes">👍 {{ postWithReason.post.numberOfLikes }}</span>
            <button class="btn btn-sm btn-outline-primary ms-2" @click="likePost(postWithReason.post.id)" :disabled="liking[postWithReason.post.id]">Like</button>
            <span class="reason-labels">
              <template v-for="reason in postWithReason.reasons" :key="reason">
                <span v-if="reason === 'friend'" class="friend-label">Friend</span>
                <span v-else-if="reason === 'for_you'" class="for-you-label"><span class="star">★</span> For you</span>
                <span v-else-if="reason === 'popular'" class="popular-label">Popular</span>
                <span v-else-if="reason === 'popular_hashtag'" class="trending-label">Trending</span>
                <span v-else class="other-label">{{ reasonLabel(reason) }}</span>
              </template>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">

import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';
import type { PostWithReason } from '../types';

export default defineComponent({
  name: 'GlobalPosts',
  setup() {
  const posts = ref<PostWithReason[]>([]);
    const loading = ref(true);
    const error = ref('');
    const liking = ref<{ [key: number]: boolean }>({});
    const reported = ref<{ [key: number]: boolean }>({});

    const fetchPosts = async () => {
      loading.value = true;
      error.value = '';
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) {
          error.value = 'You must be logged in to view global posts.';
          loading.value = false;
          return;
        }
        const user = JSON.parse(userStr);
        const response = await axios.get(`/api/posts/global?userId=${user.id}`);
        posts.value = response.data;
      } catch (err: any) {
        error.value = err.response?.data?.message || err.response?.data || 'Failed to fetch global posts.';
      } finally {
        loading.value = false;
      }
    };

    const likePost = async (postId: number) => {
      liking.value[postId] = true;
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) throw new Error('User not logged in');
        const user = JSON.parse(userStr);
        await axios.post(`/api/posts/${postId}/like`, null, { params: { userId: user.id } });
        const postWithReason = posts.value.find(pwr => pwr.post.id === postId);
        if (postWithReason) postWithReason.post.numberOfLikes++;
      } catch (err) {
        // handle error
      } finally {
        liking.value[postId] = false;
      }
    };

    const parseReason = (reason: string) => {
      // Format: reason:friends (e.g., 'friend:true', 'for_you:false')
      const [r, f] = reason.split(":");
      return { reason: r, friends: f === 'true' };
    };

    const reasonLabel = (reason: string) => {
      switch (reason) {
        case 'for_you': return 'For you';
        case 'popular': return 'Popular';
        case 'suggested': return 'Suggested';
        case 'all': return 'Other';
        case 'friend': return 'Friend';
        default: return 'Other';
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
      liking,
      reported,
      likePost,
      formatDate,
      reasonLabel,
      parseReason,
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
  border-left: 6px solid transparent;
}
.post-card:hover {
  box-shadow: 0 4px 16px rgba(24,119,242,0.12);
}
.post-card.for-you {
  border-left: 6px solid #FFD700;
  background: #fffbe6;
}
.post-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: #65676b;
  margin-bottom: 0.5rem;
}
.post-user {
  font-weight: bold;
  color: #1877f2;
}

.reason-label {
  margin-left: auto;
  font-weight: 500;
  padding: 0.2rem 0.7rem;
  border-radius: 12px;
  background: #e7f3ff;
  color: #1877f2;
}
.reason-label.for-you-label {
  background: #FFD700;
  color: #333;
}
.star {
  color: white;
  font-size: 1.1em;
  margin-right: 0.2em;
}
.friend-label {
  background: #4caf50;
  color: #fff;
  border-radius: 12px;
  padding: 0.2rem 0.7rem;
  margin-right: 0.5em;
  font-weight: 600;
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
  align-items: center;
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
