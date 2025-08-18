<template>
  <div class="create-post">
    <h2>Create Post</h2>
    <form @submit.prevent="submitPost">
      <div>
        <label for="description">Description:</label>
        <textarea
          id="description"
          v-model="description"
          placeholder="What's on your mind?"
          rows="4"
          required
        ></textarea>
      </div>
      <div>
        <label for="hashtags">Hashtags (comma-separated):</label>
        <input
          id="hashtags"
          v-model="hashtags"
          type="text"
          placeholder="#example, #post, #social"
        />
      </div>
      <button type="submit" :disabled="loading">
        {{ loading ? 'Creating...' : 'Create Post' }}
      </button>
      <div v-if="error" class="error">{{ error }}</div>
      <div v-if="success" class="success">Post created successfully!</div>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

// Set axios base URL to use Vite proxy
axios.defaults.baseURL = '/';

export default defineComponent({
  name: 'CreatePost',
  setup() {
    const description = ref('');
    const hashtags = ref('');
    const loading = ref(false);
    const error = ref('');
    const success = ref(false);
    const router = useRouter();

    const submitPost = async () => {
      loading.value = true;
      error.value = '';
      success.value = false;
      
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) {
          error.value = 'You must be logged in to create a post.';
          loading.value = false;
          return;
        }
        const user = JSON.parse(userStr);
        const response = await axios.post(
          '/api/posts/create',
          {
            description: description.value,
            hashtags: hashtags.value.split(',').map((tag: string) => tag.trim()).filter((tag: string) => tag.length > 0),
            userId: user.id,
          }
        );
        success.value = true;
        description.value = '';
        hashtags.value = '';
        setTimeout(() => router.push('/global-posts'), 2000);
      } catch (err: any) {
        console.error('Create post error:', err);
        error.value = err.response?.data?.message || err.response?.data || 'Failed to create post.';
      } finally {
        loading.value = false;
      }
    };

    return {
      description,
      hashtags,
      loading,
      error,
      success,
      submitPost,
    };
  },
});
</script>

<style scoped>
.create-post {
  max-width: 400px;
  margin: 2rem auto;
  padding: 2rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  background: #fff;
}
.create-post label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}
.create-post textarea,
.create-post input {
  width: 100%;
  margin-bottom: 1rem;
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #ccc;
  box-sizing: border-box;
}
.create-post button {
  padding: 0.5rem 1rem;
  border: none;
  background: #007bff;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
}
.create-post button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.create-post .error {
  color: #d32f2f;
  margin-top: 1rem;
  padding: 0.5rem;
  background: #ffebee;
  border-radius: 4px;
}
.create-post .success {
  color: #388e3c;
  margin-top: 1rem;
  padding: 0.5rem;
  background: #e8f5e8;
  border-radius: 4px;
}
</style>