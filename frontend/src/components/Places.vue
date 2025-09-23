<template>
  <div class="dashboard-container">
    <h2 class="dashboard-title">Places</h2>
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div v-if="places.length === 0" class="no-posts">No places yet.</div>
      <div class="posts-grid">
        <div v-for="place in places" :key="place.id" :class="['post-card', 'place-card']">
          <div class="post-header">
            <div>
              <div class="post-user">{{ place.name }}</div>
              <div class="post-meta"><small>{{ place.town || '-' }}, {{ place.country || '-' }}</small></div>
              <div class="place-type"><small>{{ place.type || '-' }}</small></div>
            </div>
            <div class="post-date">{{ place.hashtag || '-' }}</div>
          </div>

          <div class="post-body">
            <p class="post-description">{{ place.description || 'No description' }}</p>
          </div>

          <div class="post-footer">
            <div class="likes">
              <span class="avg-rating">⭐ {{ avgRating(place.id) }}</span>
            </div>

            <div class="footer-actions">
              <button v-if="!hasUserRated(place.id)" class="btn btn-sm ms-2 btn-outline-primary" @click="toggleRateForm(place.id)">Rate</button>
              <button v-else class="btn btn-sm ms-2 btn-outline-secondary" @click="toggleRateForm(place.id, true)">Edit Rating</button>
            </div>
          </div>


          <div v-if="showRateFormFor === place.id">
            <div class="modal-backdrop" @click="toggleRateForm(null)"></div>
            <div class="modal-box">
              <h3>Rate {{ place.name }}</h3>
              <label>Rating (1-5)</label>
              <select class="modal-input" v-model.number="ratingForm.rating">
                <option v-for="n in 5" :key="n" :value="n">{{ n }}</option>
              </select>
              <label>Comment (required)</label>
              <textarea class="modal-input" v-model="ratingForm.comment" rows="3" placeholder="Write a quick review..."></textarea>
              <label>Hashtag (required)</label>
              <input class="modal-input" v-model="ratingForm.hashtag" placeholder="#tag" />
              <div class="rate-form-actions">
                <button class="btn btn-sm btn-success" @click="submitRating(place.id)" :disabled="!ratingForm.comment || !ratingForm.hashtag">Submit</button>
                <button class="btn btn-sm btn-secondary" @click="toggleRateForm(null)">Cancel</button>
              </div>
              <div class="existing-ratings" v-if="placeRatings[place.id] && placeRatings[place.id].length">
                <h4>Reviews</h4>
                <ul>
                  <li v-for="r in placeRatings[place.id]" :key="r.id">{{ r.rating }} ★ - {{ r.comment || 'no comment' }}</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';
import type { Place, PlaceRating } from '../types';

export default defineComponent({
  name: 'Places',
  setup() {
    const places = ref<Place[]>([]);
    const loading = ref(true);
    const error = ref('');
    const showRateFormFor = ref<number | null>(null);
    const editing = ref(false);
    const editingRatingId = ref<number | null>(null);
    const ratingForm = ref<{ rating: number; comment: string; hashtag?: string }>({ rating: 5, comment: '', hashtag: '' });
    const currentUser = ref<{ id: number } | null>(null);
    const userRating = ref<{ [key: number]: PlaceRating | null }>({});
    const placeRatings = ref<{ [key: number]: PlaceRating[] }>({});

    const fetchPlaces = async () => {
      loading.value = true;
      try {
        const res = await axios.get('/api/places/all');
        places.value = res.data;

        await Promise.all(places.value.map(async (p) => {
          try {
            const r = await axios.get(`/api/placeRating/all?placeId=${p.id}`);
            placeRatings.value[p.id] = r.data;

            const userStr = localStorage.getItem('user');
            const userId = userStr ? JSON.parse(userStr).id : null;
            if (userId) {
              const my = (r.data || []).find((x: PlaceRating) => x.userId === userId) || null;
              userRating.value[p.id] = my;
            } else {
              userRating.value[p.id] = null;
            }
          } catch (e) {
            placeRatings.value[p.id] = [];
            userRating.value[p.id] = null;
          }
        }));
      } catch (e: any) {
        error.value = e.response?.data || 'Failed to fetch places';
      } finally {
        loading.value = false;
      }
    };

    const toggleRateForm = (placeId: number | null, edit = false) => {
      showRateFormFor.value = showRateFormFor.value === placeId ? null : placeId;
      if (placeId !== null) 
      {
        const myRating = userRating.value[placeId] || null;
        if (edit && myRating) {
          editing.value = true;
          editingRatingId.value = myRating.id || null;
          ratingForm.value = { rating: myRating.rating || 5, comment: myRating.comment || '', hashtag: myRating.hashtag || '' };
        } else {
          editing.value = false;
          editingRatingId.value = null;
          ratingForm.value = { rating: 5, comment: '', hashtag: '' };
        }
      } else {
        editing.value = false;
        editingRatingId.value = null;
      }
    };

    const submitRating = async (placeId: number) => {
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) throw new Error('User not logged in');
        const user = JSON.parse(userStr);
        const payload: PlaceRating = {
          placeId,
          userId: user.id,
          rating: ratingForm.value.rating,
          comment: ratingForm.value.comment,
          hashtag: ratingForm.value.hashtag,
        } as PlaceRating;
        if (editing.value) {
          // update existing
          await axios.put('/api/placeRating/update', payload);
        } else {
          await axios.post('/api/placeRating/create', payload);
        }
        // refresh ratings for this place
        const r = await axios.get(`/api/placeRating/all?placeId=${placeId}`);
        placeRatings.value[placeId] = r.data;
        // update userRating map
        const userStr2 = localStorage.getItem('user');
        const userId2 = userStr2 ? JSON.parse(userStr2).id : null;
        if (userId2) {
          const my = (r.data || []).find((x: PlaceRating) => x.userId === userId2) || null;
          userRating.value[placeId] = my;
        }
        toggleRateForm(null);
      } catch (err: any) {
        error.value = err.response?.data || err.message || 'Failed to submit rating.';
        setTimeout(() => { error.value = ''; }, 3000);
      }
    };

    const hasUserRated = (placeId: number) => {
      return !!userRating.value[placeId];
    };

    const avgRating = (placeId: number) => {
      const ratings = placeRatings.value[placeId] || [];
      if (!ratings.length) return '-';
      const sum = ratings.reduce((s, r) => s + (r.rating || 0), 0);
      return (sum / ratings.length).toFixed(1);
    };

    onMounted(fetchPlaces);

    return {
      places,
      loading,
      error,
      showRateFormFor,
      ratingForm,
      placeRatings,
      toggleRateForm,
      submitRating,
      avgRating,
      hasUserRated,
      userRating,
      editing,
      editingRatingId,
    };
  }
});
</script>

<style scoped>
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
.posts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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
  min-height: 250px;
  overflow: hidden;
}
.post-card:hover {
  box-shadow: 0 4px 16px rgba(24,119,242,0.12);
}
.post-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
  color: #65676b;
  margin-bottom: 0.6rem;
}
.post-user {
  font-weight: 700;
  color: #1877f2;
  font-size: 1.05rem;
}
.post-meta { color: #90949c; }
.place-type { 
  color: #50575b; 
  font-weight: 600; 
  margin-top: 0.25rem; 
  word-wrap: break-word;
  overflow-wrap: break-word;
}
.post-body { flex: 1; }
.post-description { font-size: 1rem; margin-bottom: 0.5rem; color:#333; }
.post-footer {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  font-size: 0.95rem;
  color: #65676b;
  margin-top: 0.6rem;
  justify-content: flex-start;
}
.avg-rating { font-weight: 700; color: #d48806; }
.hashtag-label { 
  background: #e7f3ff; 
  color: #1877f2; 
  border-radius: 8px; 
  padding: 0.2rem 0.5rem; 
  font-size: 0.85rem;
  word-break: break-word;
}
.type-label { 
  background: #f3f6f7; 
  color: #50575b; 
  border-radius: 8px; 
  padding: 0.2rem 0.5rem; 
  font-weight: 600; 
  font-size: 0.85rem;
  word-break: break-word;
}
.rate-form { margin-top: 1rem; display:flex; flex-direction:column; gap:0.5rem; }
.rate-form input, .rate-form select { padding: 0.4rem; border-radius:6px; border:1px solid #ddd; }
.rate-form-actions { display:flex; gap:0.5rem; }
.existing-ratings ul { padding-left: 1rem; margin-top:0.5rem; }

.modal-backdrop {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.45);
  z-index: 1000;
}
.modal-box {
  position: fixed;
  left: 50%; top: 50%; transform: translate(-50%, -50%);
  background: white;
  padding: 1.2rem;
  border-radius: 8px;
  width: 420px;
  z-index: 1001;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
}
.modal-box h3 { margin-top: 0; }
.modal-box textarea { width: 100%; resize: vertical; margin-bottom: 0.75rem; padding: 0.6rem; border-radius:6px; border:1px solid #e1e4e8; }
.modal-box input { width: 100%; margin-bottom: 0.75rem; padding: 0.5rem; border-radius:6px; border:1px solid #e1e4e8; }
.modal-input { width: 100%; padding: 0.5rem; margin-bottom: 0.6rem; border-radius:6px; border:1px solid #e6e9ec; }
.modal-box label { display:block; margin-top: 0.6rem; margin-bottom: 0.25rem; font-weight:600; }
.btn[disabled], button:disabled { opacity: 0.55; cursor: not-allowed; }

</style>
