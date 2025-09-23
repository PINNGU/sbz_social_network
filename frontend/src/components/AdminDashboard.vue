<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>Admin Dashboard</h1>
      <p class="subtitle">Upravljanje sistemom i detekcija loših korisnika</p>
    </div>

    <div class="dashboard-controls">
      <div class="control-card">
        <h2>Detekcija loših korisnika</h2>
        <div class="control-buttons">
          <button 
            class="btn btn-primary" 
            @click="runDetection"
            :disabled="isDetectionRunning"
          >
            {{ isDetectionRunning ? 'Detekcija u toku...' : 'Pokreni detekciju' }}
          </button>
          <button 
            class="btn btn-secondary" 
            @click="loadSuspiciousUsers"
            :disabled="isLoadingUsers"
          >
            {{ isLoadingUsers ? 'Učitavanje...' : 'Prikaži sumnjive korisnike' }}
          </button>
        </div>
      </div>

      <div class="control-card">
        <h2>Statistike sistema</h2>
        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-number">{{ totalUsers }}</span>
            <span class="stat-label">Ukupno korisnika</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ suspiciousUsers.length }}</span>
            <span class="stat-label">Sumnjivi korisnici</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ activeSuspensions }}</span>
            <span class="stat-label">Aktivne suspenzije</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats.totalPosts }}</span>
            <span class="stat-label">Ukupno postova</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats.totalReports }}</span>
            <span class="stat-label">Ukupno prijava</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Rezultati detekcije -->
    <div v-if="detectionResults" class="detection-results">
      <h2>Rezultati detekcije</h2>
      <div class="alert alert-success">
        <strong>Detekcija završena!</strong><br>
        Vreme: {{ new Date(detectionResults.timestamp).toLocaleString() }}<br>
        Pronađeno {{ detectionResults.suspiciousUsersCount }} sumnjivih korisnika
      </div>
    </div>

    <!-- Lista sumnjivih korisnika -->
    <div v-if="suspiciousUsers.length > 0" class="suspicious-users-section">
      <h2>Sumnjivi korisnici ({{ suspiciousUsers.length }})</h2>
      <div class="users-table-container">
        <table class="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Email</th>
              <th>Ime</th>
              <th>Status suspenzije</th>
              <th>Razlog</th>
              <th>Suspendovan do</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="detection in suspiciousUsers" :key="detection.user.id" class="user-row">
              <td>{{ detection.user.id }}</td>
              <td>{{ detection.user.email }}</td>
              <td>{{ detection.user.name }} {{ detection.user.surname }}</td>
              <td>
                <span 
                  :class="['status-badge', getSuspensionStatusClass(detection.user)]"
                >
                  {{ getSuspensionStatusText(detection.user) }}
                </span>
              </td>
              <td class="reason-cell">
                <span class="reason-text">{{ detection.suspicionReason }}</span>
              </td>
              <td>
                {{ detection.user.suspendedUntil ? 
                   new Date(detection.user.suspendedUntil).toLocaleString() : 
                   'Nije suspendovan' }}
              </td>
              <td>
                <button 
                  class="btn btn-small btn-info" 
                  @click="analyzeUser(detection.user.id)"
                  :disabled="isAnalyzing"
                >
                  Analiziraj
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Prazno stanje -->
    <div v-else-if="hasSearched && suspiciousUsers.length === 0" class="empty-state">
      <h3>Nema sumnjivih korisnika</h3>
      <p>Sistem nije detektovao problematično ponašanje korisnika.</p>
    </div>

    <!-- Loading state -->
    <div v-if="isDetectionRunning || isLoadingUsers" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>{{ isDetectionRunning ? 'Analiziranje korisnika...' : 'Učitavanje podataka...' }}</p>
    </div>

    <!-- Error state -->
    <div v-if="errorMessage" class="alert alert-error">
      <strong>Greška:</strong> {{ errorMessage }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  data() {
    return {
      suspiciousUsers: [],
      detectionResults: null,
      isDetectionRunning: false,
      isLoadingUsers: false,
      isAnalyzing: false,
      hasSearched: false,
      errorMessage: '',
      totalUsers: 0,
      activeSuspensions: 0,
      stats: {
        totalPosts: 0,
        totalReports: 0
      }
    }
  },
  async mounted() {
    await this.loadSuspiciousUsers();
    await this.loadSystemHealth();
  },
  methods: {
    async runDetection() {
      this.isDetectionRunning = true;
      this.errorMessage = '';
      
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('http://localhost:8080/api/admin/detect-bad-users', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        this.detectionResults = result;
        
        // Automatski učitaj ažurirane rezultate
        await this.loadSuspiciousUsers();
        await this.loadSystemHealth();
        
      } catch (error) {
        console.error('Greška pri pokretanju detekcije:', error);
        this.errorMessage = 'Greška pri pokretanju detekcije: ' + error.message;
      } finally {
        this.isDetectionRunning = false;
      }
    },

    async loadSuspiciousUsers() {
      this.isLoadingUsers = true;
      this.errorMessage = '';
      this.hasSearched = true;
      
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('http://localhost:8080/api/admin/suspicious-users', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        
        // Backend sada vraća BadUserDetection objekte direktno
        this.suspiciousUsers = result.suspiciousUsers || [];
        
        this.activeSuspensions = this.suspiciousUsers.filter(
          detection => detection.user.suspendedUntil && 
          new Date(detection.user.suspendedUntil) > new Date()
        ).length;
        
      } catch (error) {
        console.error('Greška pri učitavanju sumnjivih korisnika:', error);
        this.errorMessage = 'Greška pri učitavanju: ' + error.message;
      } finally {
        this.isLoadingUsers = false;
      }
    },

    async loadSystemHealth() {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch('http://localhost:8080/api/admin/system-health', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const healthData = await response.json();
        this.totalUsers = healthData.totalUsers || 0;
        this.stats.totalPosts = healthData.totalPosts || 0;
        this.stats.totalReports = healthData.totalReports || 0;
        
      } catch (error) {
        console.error('Greška pri učitavanju system health:', error);
      }
    },

    async analyzeUser(userId) {
      this.isAnalyzing = true;
      
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`http://localhost:8080/api/admin/analyze-user/${userId}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        alert(`Analiza korisnika:\n${result.isSuspicious ? 'SUMNJIV' : 'REGULARAN'}\nRazlog: ${result.reason || 'Nema problematičnog ponašanja'}`);
        
        // Osvežiti listu
        await this.loadSuspiciousUsers();
        
      } catch (error) {
        console.error('Greška pri analizi korisnika:', error);
        this.errorMessage = 'Greška pri analizi: ' + error.message;
      } finally {
        this.isAnalyzing = false;
      }
    },

    getSuspensionStatusClass(user) {
      if (!user.suspendedUntil) return 'status-normal';
      
      const suspendedUntil = new Date(user.suspendedUntil);
      const now = new Date();
      
      if (suspendedUntil > now) {
        return 'status-suspended';
      } else {
        return 'status-expired';
      }
    },

    getSuspensionStatusText(user) {
      if (!user.suspendedUntil) return 'Aktivan';
      
      const suspendedUntil = new Date(user.suspendedUntil);
      const now = new Date();
      
      if (suspendedUntil > now) {
        const restrictions = [];
        if (!user.canPost) restrictions.push('objave');
        if (!user.canLogin) restrictions.push('login');
        return `Suspendovan (${restrictions.join(', ')})`;
      } else {
        return 'Suspenzija istekla';
      }
    },

    getSuspicionReason(user) {
      // Ovo je privremena logika za demonstraciju
      // U stvarnosti razlog bi trebalo da dolazi iz backend-a
      if (!user.canPost && !user.canLogin) {
        return 'Potpuna suspenzija zbog višestrukih prekršaja';
      } else if (!user.canPost) {
        return 'Blokiranje objavljivanja zbog prijava objava';
      } else if (!user.canLogin) {
        return 'Blokiranje pristupa zbog problematičnog ponašanja';
      }
      return 'Detektovano sumnjivo ponašanje';
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
}

.dashboard-header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 2.5rem;
}

.subtitle {
  color: #6c757d;
  font-size: 1.1rem;
}

.dashboard-controls {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 40px;
}

.control-card {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
}

.control-card h2 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.3rem;
}

.control-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 15px;
  max-width: 800px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  display: block;
  font-size: 2rem;
  font-weight: bold;
  color: #007bff;
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  color: #6c757d;
  margin-top: 5px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-block;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #545b62;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-small {
  padding: 6px 12px;
  font-size: 0.875rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.detection-results, .suspicious-users-section {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.alert {
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.alert-success {
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.alert-error {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.users-table-container {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.users-table th,
.users-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #dee2e6;
}

.users-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.user-row:hover {
  background-color: #f8f9fa;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-normal {
  background-color: #d4edda;
  color: #155724;
}

.status-suspended {
  background-color: #f8d7da;
  color: #721c24;
}

.status-expired {
  background-color: #fff3cd;
  color: #856404;
}

.reason-cell {
  max-width: 200px;
}

.reason-text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  color: white;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .dashboard-controls {
    grid-template-columns: 1fr;
  }
  
  .control-buttons {
    flex-direction: column;
  }
  
  .users-table {
    font-size: 0.875rem;
  }
}
</style>