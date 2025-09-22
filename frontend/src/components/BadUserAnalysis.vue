<template>
  <div class="bad-user-analysis">
    <div class="analysis-header">
      <h2>Detaljna analiza korisnika</h2>
      <button class="btn btn-secondary" @click="$emit('close')">
        Zatvori
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Analiziranje korisnika...</p>
    </div>

    <div v-else-if="analysis" class="analysis-content">
      <!-- Osnovne informacije o korisniku -->
      <div class="user-info-card">
        <h3>Informacije o korisniku</h3>
        <div class="user-details">
          <div class="detail-item">
            <strong>ID:</strong> {{ analysis.userId }}
          </div>
          <div class="detail-item">
            <strong>Email:</strong> {{ analysis.userEmail }}
          </div>
          <div class="detail-item">
            <strong>Status:</strong> 
            <span :class="['status-badge', analysis.isSuspicious ? 'suspicious' : 'normal']">
              {{ analysis.isSuspicious ? 'SUMNJIV' : 'REGULARAN' }}
            </span>
          </div>
          <div v-if="analysis.reason" class="detail-item">
            <strong>Razlog sumnje:</strong> {{ analysis.reason }}
          </div>
        </div>
      </div>

      <!-- Trenutno stanje suspenzije -->
      <div v-if="analysis.currentSuspension" class="suspension-card">
        <h3>Trenutno stanje suspenzije</h3>
        <div class="suspension-details">
          <div class="detail-item">
            <strong>Suspendovan:</strong> 
            <span :class="['status-indicator', analysis.currentSuspension.isSuspended ? 'suspended' : 'active']">
              {{ analysis.currentSuspension.isSuspended ? 'DA' : 'NE' }}
            </span>
          </div>
          <div v-if="analysis.currentSuspension.suspendedUntil" class="detail-item">
            <strong>Suspendovan do:</strong> 
            {{ new Date(analysis.currentSuspension.suspendedUntil).toLocaleString() }}
          </div>
          <div class="detail-item">
            <strong>Može objavljivati:</strong> 
            <span :class="['permission-badge', analysis.currentSuspension.canPost ? 'allowed' : 'denied']">
              {{ analysis.currentSuspension.canPost ? 'DA' : 'NE' }}
            </span>
          </div>
          <div class="detail-item">
            <strong>Može se ulogovati:</strong> 
            <span :class="['permission-badge', analysis.currentSuspension.canLogin ? 'allowed' : 'denied']">
              {{ analysis.currentSuspension.canLogin ? 'DA' : 'NE' }}
            </span>
          </div>
          <div v-if="analysis.currentSuspension.suspensionReason" class="detail-item">
            <strong>Razlog suspenzije:</strong> {{ analysis.currentSuspension.suspensionReason }}
          </div>
        </div>
      </div>

      <!-- Analiza aktivnosti -->
      <div v-if="analysis.analysis && analysis.analysis.activities" class="activities-card">
        <h3>Analiza aktivnosti</h3>
        <div class="activities-summary">
          <div class="activity-stat">
            <span class="stat-number">{{ getActivityCount('POST_REPORTED') }}</span>
            <span class="stat-label">Prijavljene objave</span>
          </div>
          <div class="activity-stat">
            <span class="stat-number">{{ getActivityCount('USER_BLOCKED') }}</span>
            <span class="stat-label">Puta blokiran</span>
          </div>
          <div class="activity-stat">
            <span class="stat-number">{{ getActivityCount('EXCESSIVE_POSTING') }}</span>
            <span class="stat-label">Prekomerne objave</span>
          </div>
          <div class="activity-stat">
            <span class="stat-number">{{ getActivityCount('SPAM_ACTIVITY') }}</span>
            <span class="stat-label">Spam aktivnosti</span>
          </div>
        </div>

        <!-- Detalji aktivnosti -->
        <div class="activities-timeline">
          <h4>Hronologija aktivnosti (poslednja 24h)</h4>
          <div v-if="recentActivities.length > 0" class="timeline">
            <div 
              v-for="(activity, index) in recentActivities" 
              :key="index" 
              class="timeline-item"
              :class="getActivityClass(activity.activityType)"
            >
              <div class="timeline-time">
                {{ formatTime(activity.timestamp) }}
              </div>
              <div class="timeline-content">
                <strong>{{ getActivityTypeName(activity.activityType) }}</strong>
                <p>{{ activity.details }}</p>
              </div>
            </div>
          </div>
          <div v-else class="no-activities">
            Nema aktivnosti u poslednja 24 sata
          </div>
        </div>
      </div>

      <!-- Preporučene akcije -->
      <div v-if="analysis.isSuspicious" class="recommendations-card">
        <h3>Preporučene akcije</h3>
        <div class="recommendations">
          <div class="recommendation-item warning">
            <i class="icon">⚠️</i>
            <div>
              <strong>Praćenje korisnika</strong>
              <p>Nastaviti praćenje aktivnosti ovog korisnika</p>
            </div>
          </div>
          <div v-if="!analysis.currentSuspension.isSuspended" class="recommendation-item action">
            <i class="icon">🔒</i>
            <div>
              <strong>Razmotriti suspenziju</strong>
              <p>Na osnovu detektovanih obrazaca ponašanja</p>
            </div>
          </div>
          <div class="recommendation-item info">
            <i class="icon">📊</i>
            <div>
              <strong>Periodična analiza</strong>
              <p>Sistem će automatski analizirati korisnika svakih 15 minuta</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="error" class="error-state">
      <div class="alert alert-error">
        <strong>Greška:</strong> {{ error }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BadUserAnalysis',
  props: {
    userId: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      analysis: null,
      loading: false,
      error: null
    }
  },
  computed: {
    recentActivities() {
      if (!this.analysis || !this.analysis.analysis || !this.analysis.analysis.activities) {
        return [];
      }
      
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      
      return this.analysis.analysis.activities
        .filter(activity => new Date(activity.timestamp) > yesterday)
        .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
        .slice(0, 10); // Poslednja 10 aktivnosti
    }
  },
  async mounted() {
    await this.loadAnalysis();
  },
  methods: {
    async loadAnalysis() {
      this.loading = true;
      this.error = null;
      
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`http://localhost:8080/api/admin/analyze-user/${this.userId}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        this.analysis = await response.json();
      } catch (error) {
        console.error('Greška pri učitavanju analize:', error);
        this.error = 'Greška pri učitavanju analize: ' + error.message;
      } finally {
        this.loading = false;
      }
    },

    getActivityCount(activityType) {
      if (!this.analysis || !this.analysis.analysis || !this.analysis.analysis.activities) {
        return 0;
      }
      
      return this.analysis.analysis.activities.filter(
        activity => activity.activityType === activityType
      ).length;
    },

    getActivityClass(activityType) {
      switch (activityType) {
        case 'POST_REPORTED':
          return 'activity-report';
        case 'USER_BLOCKED':
          return 'activity-block';
        case 'EXCESSIVE_POSTING':
          return 'activity-excessive';
        case 'SPAM_ACTIVITY':
          return 'activity-spam';
        default:
          return 'activity-normal';
      }
    },

    getActivityTypeName(activityType) {
      switch (activityType) {
        case 'POST_REPORTED':
          return 'Objava prijavljena';
        case 'USER_BLOCKED':
          return 'Korisnik blokiran';
        case 'EXCESSIVE_POSTING':
          return 'Prekomerno objavljivanje';
        case 'SPAM_ACTIVITY':
          return 'Spam aktivnost';
        default:
          return activityType;
      }
    },

    formatTime(timestamp) {
      return new Date(timestamp).toLocaleString();
    }
  }
}
</script>

<style scoped>
.bad-user-analysis {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  max-width: 900px;
  margin: 0 auto;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e9ecef;
}

.analysis-header h2 {
  color: #2c3e50;
  margin: 0;
}

.user-info-card,
.suspension-card,
.activities-card,
.recommendations-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  border-left: 4px solid #007bff;
}

.user-info-card h3,
.suspension-card h3,
.activities-card h3,
.recommendations-card h3 {
  color: #2c3e50;
  margin-bottom: 15px;
  font-size: 1.2rem;
}

.user-details,
.suspension-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-item strong {
  min-width: 120px;
  color: #495057;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
}

.status-badge.suspicious {
  background-color: #dc3545;
  color: white;
}

.status-badge.normal {
  background-color: #28a745;
  color: white;
}

.status-indicator {
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.status-indicator.suspended {
  background-color: #f8d7da;
  color: #721c24;
}

.status-indicator.active {
  background-color: #d4edda;
  color: #155724;
}

.permission-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.permission-badge.allowed {
  background-color: #d4edda;
  color: #155724;
}

.permission-badge.denied {
  background-color: #f8d7da;
  color: #721c24;
}

.activities-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  margin-bottom: 25px;
}

.activity-stat {
  text-align: center;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-number {
  display: block;
  font-size: 1.8rem;
  font-weight: bold;
  color: #007bff;
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  color: #6c757d;
  margin-top: 5px;
}

.activities-timeline h4 {
  color: #495057;
  margin-bottom: 15px;
}

.timeline {
  max-height: 400px;
  overflow-y: auto;
}

.timeline-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  margin-bottom: 10px;
  background: white;
  border-radius: 8px;
  border-left: 4px solid #dee2e6;
}

.timeline-item.activity-report {
  border-left-color: #ffc107;
}

.timeline-item.activity-block {
  border-left-color: #dc3545;
}

.timeline-item.activity-excessive {
  border-left-color: #fd7e14;
}

.timeline-item.activity-spam {
  border-left-color: #e83e8c;
}

.timeline-time {
  min-width: 140px;
  font-size: 0.875rem;
  color: #6c757d;
  font-weight: 500;
}

.timeline-content {
  flex: 1;
}

.timeline-content strong {
  color: #2c3e50;
}

.timeline-content p {
  margin: 5px 0 0 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.recommendations {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recommendation-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px;
  border-radius: 8px;
  background: white;
}

.recommendation-item.warning {
  border-left: 4px solid #ffc107;
}

.recommendation-item.action {
  border-left: 4px solid #dc3545;
}

.recommendation-item.info {
  border-left: 4px solid #17a2b8;
}

.recommendation-item .icon {
  font-size: 1.5rem;
  min-width: 30px;
}

.recommendation-item strong {
  color: #2c3e50;
  display: block;
  margin-bottom: 5px;
}

.recommendation-item p {
  margin: 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.no-activities {
  text-align: center;
  padding: 30px;
  color: #6c757d;
  font-style: italic;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-state {
  padding: 20px;
}

.alert {
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.alert-error {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}
</style>