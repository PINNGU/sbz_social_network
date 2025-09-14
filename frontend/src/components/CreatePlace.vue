<template>
  <div class="create-post">
    <h2>Create Place</h2>
    <form @submit.prevent="submitPlace">
      <div>
        <label for="name">Name:</label>
        <input id="name" v-model="name" type="text" required />
      </div>

      <div>
        <label for="country">Country:</label>
        <input id="country" v-model="country" type="text" required />
      </div>

      <div>
        <label for="town">Town:</label>
        <input id="town" v-model="town" type="text" required />
      </div>

      <div>
        <label for="type">Type:</label>
        <input id="type" v-model="type" type="text" required placeholder="e.g. Restaurant, Park, Museum" />
      </div>

      <div>
        <label for="description">Description:</label>
        <textarea id="description" v-model="description" rows="4" required></textarea>
      </div>

      <div>
        <label for="hashtag">Hashtag (comma-separated):</label>
        <input id="hashtag" v-model="hashtag" type="text" placeholder="#tag1, #tag2" required />
      </div>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Creating...' : 'Create Place' }}
      </button>

      <div v-if="error" class="error">{{ error }}</div>
      <div v-if="success" class="success">Place created successfully! Redirecting...</div>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

axios.defaults.baseURL = '/';

export default defineComponent({
  name: 'CreatePlace',
  setup() {
    const name = ref('');
    const country = ref('');
    const town = ref('');
  const type = ref('');
    const description = ref('');
    const hashtag = ref('');
    const loading = ref(false);
    const error = ref('');
    const success = ref(false);
    const router = useRouter();

    const submitPlace = async () => {
      loading.value = true;
      error.value = '';
      success.value = false;
      try {
        const payload = {
          name: name.value,
          country: country.value,
          town: town.value,
          type: type.value,
          description: description.value,
          hashtag: hashtag.value
        };
        await axios.post('/api/places/create', payload);
        success.value = true;
        // reset fields
  name.value = '';
  country.value = '';
  town.value = '';
  type.value = '';
  description.value = '';
  hashtag.value = '';
        setTimeout(() => router.push('/places'), 800);
      } catch (err: any) {
        console.error('Create place error:', err);
        error.value = err.response?.data?.message || err.response?.data || 'Failed to create place.';
      } finally {
        loading.value = false;
      }
    };

    return { name, country, town, type, description, hashtag, loading, error, success, submitPlace };
  },
});
</script>

<style scoped>
.create-post {
  max-width: 600px;
  margin: 2rem auto;
  padding: 1.5rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  background: #fff;
}
.create-post h2 {
  text-align: center;
  color: #1877f2;
  margin-bottom: 1rem;
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
  border: 1px solid #ddd;
  box-sizing: border-box;
}
.create-post button {
  padding: 0.5rem 1rem;
  border: none;
  background: #1877f2;
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
