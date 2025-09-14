<template>
  <div class="dashboard-container">
    <h2 class="dashboard-title">Global Posts</h2>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div v-if="posts.length === 0" class="no-posts">No global posts to show.</div>
      <div class="posts-grid">
  <div v-for="postWithReason in posts" :key="postWithReason.post.id" :class="['post-card', { 'for-you': postWithReason.reasons.includes('for_you'), 'suggested': postWithReason.reasons.includes('suggested') }]">
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
            <span class="likes">👍 {{ likesByPostId[postWithReason.post.id]?.length || 0 }}</span>
            <button
              class="btn btn-sm ms-2"
              :class="{
                'btn-outline-primary': !hasLiked(postWithReason.post.id),
                'btn-success liked-btn': hasLiked(postWithReason.post.id)
              }"
              @click="likePost(postWithReason.post.id)"
              :disabled="liking[postWithReason.post.id] || hasLiked(postWithReason.post.id)"
            >
              <span v-if="hasLiked(postWithReason.post.id)"><i class="fa fa-thumbs-up"></i> Liked</span>
              <span v-else>Like</span>
            </button>

            <!-- Report area: show button if reportable, otherwise show 'Reported' label when user has already reported -->
            <div class="report-area">
              <button
                v-if="canReport(postWithReason.post)"
                class="btn btn-sm ms-2 btn-outline-danger report-btn"
                @click="reportPost(postWithReason.post.id)"
                :disabled="reported[postWithReason.post.id]"
                title="Report post"
              >
                <span v-if="reported[postWithReason.post.id]"><i class="fa fa-flag"></i> Reported</span>
                <span v-else><i class="fa fa-flag"></i> Report</span>
              </button>

              <span v-else-if="hasReported(postWithReason.post)" class="reported-label">Reported</span>
            </div>



            <span class="reason-labels right-align">
              <template v-for="reason in postWithReason.reasons" :key="reason">
                <span v-if="reason === 'friend'" class="friend-label">Friend</span>
                <span v-else-if="reason === 'for_you'" class="for-you-label"><span class="star">★</span> For you</span>
                <span v-else-if="reason === 'suggested'" class="suggested-label">Suggested</span>
                <span v-else-if="reason === 'popular'" class="popular-label">Popular</span>
                <span v-else-if="reason === 'popular_hashtag'" class="trending-label">Trending</span>
                <span v-else-if="reason === 'similar_user'" class="similar-user-label">{{ reasonLabel(reason) }}</span>
                <span v-else-if="reason === 'similar_content'" class="similar-content-label">{{ reasonLabel(reason) }}</span>
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
  const likesByPostId = ref<{ [key: number]: number[] }>({});

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
        // Fetch likes for each post
        const likePromises = posts.value.map(async (postWithReason) => {
          const postId = postWithReason.post.id;
          try {
            const res = await axios.get(`/api/posts/${postId}/likes`);
            likesByPostId.value[postId] = res.data;
          } catch (e) {
            likesByPostId.value[postId] = [];
          }
        });
        await Promise.all(likePromises);
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
        // After liking, reload likes for this post only
        const res = await axios.get(`/api/posts/${postId}/likes`);
        likesByPostId.value[postId] = res.data;
      } catch (err) {
        // handle error
      } finally {
        liking.value[postId] = false;
      }
    };

    const reportPost = async (postId: number) => {
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) throw new Error('User not logged in');
        const user = JSON.parse(userStr);
        reported.value[postId] = true;
        await axios.post(`/api/posts/${postId}/report`, null, { params: { userId: user.id } });
        // update local posts array to include this reporter so UI reflects it
        const pwr = posts.value.find(p => p.post.id === postId);
        if (pwr) {
          if (!pwr.post.reports) pwr.post.reports = [];
          if (!pwr.post.reports.includes(user.id)) pwr.post.reports.push(user.id);
        }
      } catch (err: any) {
        reported.value[postId] = false;
        // show error briefly
        error.value = err.response?.data || err.message || 'Failed to report post.';
        setTimeout(() => { error.value = ''; }, 3000);
      }
    };

    const canReport = (post: any) => {
      const userStr = localStorage.getItem('user');
      if (!userStr) return false;
      const user = JSON.parse(userStr);
      if (!post || !post.user) return false;
      if (post.user.id === user.id) return false; // don't allow reporting own posts
      if (post.reports && post.reports.includes(user.id)) return false; // already reported
      return true;
    };

    const hasReported = (post: any) => {
      const userStr = localStorage.getItem('user');
      if (!userStr) return false;
      const user = JSON.parse(userStr);
      return post?.reports && post.reports.includes(user.id);
    };

    const parseReason = (reason: string) => {
      // Format: reason:friends (e.g., 'friend:true', 'for_you:false')
      const [r, f] = reason.split(":");
      return { reason: r, friends: f === 'true' };
    };

    const reasonLabel = (reason: string) => {
      if (reason === 'Because you and others liked similar content' || reason.startsWith('brand_new_user') || reason === 'similar_content') {
        return 'Because you and others liked similar content';
      }
      if (reason === 'similar_user') {
        return 'Because similar users to you liked this post';
      }
      switch (reason) {
        case 'for_you': return 'For you';
        case 'popular': return 'Popular';
        case 'suggested': return 'Suggested';
        case 'all': return 'Other';
        case 'friend': return 'Friend';
        default: return 'Other';
      }
    };

    // Check if the current user has liked the post (using likesByPostId)
    const hasLiked = (postId: number) => {
      const userStr = localStorage.getItem('user');
      if (!userStr) return false;
      const user = JSON.parse(userStr);
      return likesByPostId.value[postId] && likesByPostId.value[postId].includes(user.id);
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
      reportPost,
      canReport,
      hasReported,
      formatDate,
      reasonLabel,
      parseReason,
      hasLiked,
      likesByPostId,
    };
  },
});
</script>

<style scoped>
.similar-user-label {
  color: #d72660;
  font-weight: 600;
  margin-right: 0.5em;
}
.similar-content-label {
  color: #4b145b;
  font-weight: 600;
  margin-right: 0.5em;
}
.dashboard-container {
  max-width: 1400px;
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

.btn.liked-btn {
  background: #7be495;
  color: #1b5e20;
  border: 1px solid #7be495;
  font-weight: 600;
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

.post-card.suggested {
  border-left: 6px solid #7be495;
  background: #eaffea;
}

.suggested-label {
  background: #7be495;
  color: #1b5e20;
  border-radius: 12px;
  padding: 0.2rem 0.7rem;
  margin-right: 0.5em;
  font-weight: 600;
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
  justify-content: flex-start;
}
.reason-labels.right-align {
  margin-left: auto;
  display: flex;
  align-items: center;
}
.likes {
  display: flex;
  align-items: center;
}
.reports {
  display: flex;
  align-items: center;
}
.reported-label {
  background: #f5c6cb;
  color: #721c24;
  padding: 0.25rem 0.6rem;
  border-radius: 12px;
  font-weight: 600;
  margin-left: 0.5rem;
}
</style>
