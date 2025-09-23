<template>
  <aside class="ads-sidebar">
    <h5>Preporučena mesta</h5>
    <div v-if="ads.length === 0">Nema preporuka</div>
    <div v-for="ad in ads" :key="ad.place.id" class="ad-card">
      <div class="ad-title">{{ ad.place.name }} ({{ ad.place.town }})</div>
      <div class="ad-reason">Razlog: {{ ad.reason }}</div>
      <div class="ad-score">Score: {{ ad.score.toFixed(1) }}</div>
    </div>
  </aside>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { getUser } from '../auth';

export default defineComponent({
  name: 'AdsSidebar',
  setup() {
    const ads = ref<Array<any>>([]);

    const fetchAds = async () => {
      const user = getUser();
      if (!user) return;
      try {
        const resp = await fetch(`/api/ads/recommended?userId=${user.id}`);
        if (resp.ok) {
          ads.value = await resp.json();
        }
      } catch (e) {
        console.error('Failed to fetch ads', e);
      }
    };

    onMounted(() => { fetchAds(); });

    return { ads };
  }
});
</script>

<style scoped>
.ads-sidebar {
  width: 300px;
  padding: 12px;
  border-left: 1px solid #ddd;
  background: #fafafa;
}
.ad-card { margin-bottom: 12px; padding: 8px; border: 1px solid #eee; background: white; }
.ad-title { font-weight: 600; }
.ad-reason { font-size: 0.9em; color: #555; }
.ad-score { font-size: 0.8em; color: #888; }
</style>
