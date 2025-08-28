<template>
  <div class="friends-container">
    <div class="friends-content">
      <h2 class="friends-title">My Friends</h2>
      
      <div v-if="loading" class="loading text-center">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-2">Loading friends...</p>
      </div>
      
      <div v-else-if="error" class="error text-center text-danger">
        <i class="fas fa-exclamation-triangle"></i>
        <p>{{ error }}</p>
      </div>
      
      <div v-else-if="friends.length === 0" class="no-friends text-center text-muted">
        <i class="fas fa-user-friends fa-3x mb-3"></i>
        <p>You don't have any friends yet.</p>
        <p>Start connecting with people!</p>
      </div>
      
      <div v-else class="friends-list">
        <div 
          v-for="friend in friends" 
          :key="friend.id"
          class="friend-card"
        >
          <div class="friend-info">
            <div class="friend-avatar">
              <i class="fas fa-user-circle fa-2x"></i>
            </div>
            <div class="friend-details">
              <h5 class="friend-name">{{ friend.name }} {{ friend.surname }}</h5>
              <p class="friend-email text-muted">{{ friend.email }}</p>
            </div>
          </div>
          
          <div class="friend-actions">
            <!-- Block/Unblock Button -->
            <button
              :class="[
                'btn btn-sm me-2',
                isBlocked(friend.id) ? 'btn-warning' : 'btn-outline-warning'
              ]"
              @click="toggleBlock(friend.id)"
              :disabled="actionLoading[friend.id]"
              :title="isBlocked(friend.id) ? 'Unblock friend' : 'Block friend'"
            >
              <span v-if="actionLoading[friend.id]" class="spinner-border spinner-border-sm me-1"></span>
              <i :class="isBlocked(friend.id) ? 'fas fa-unlock' : 'fas fa-ban'"></i>
            </button>
            
            <!-- Remove Friend Button -->
            <button
              class="btn btn-sm btn-outline-danger"
              @click="removeFriend(friend.id)"
              :disabled="actionLoading[friend.id] || isBlocked(friend.id)"
              :title="isBlocked(friend.id) ? 'Cannot remove blocked friend' : 'Remove friend'"
            >
              <span v-if="actionLoading[friend.id]" class="spinner-border spinner-border-sm me-1"></span>
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';
import { getUser } from '../auth';

interface Friend {
  id: number;
  name: string;
  surname: string;
  email: string;
}

export default defineComponent({
  name: 'Friends',
  setup() {
    const friends = ref<Friend[]>([]);
    const blockedFriends = ref<Set<number>>(new Set());
    const loading = ref(false);
    const error = ref('');
    const actionLoading = ref<{ [key: number]: boolean }>({});

    const currentUser = getUser();

    const loadFriends = async () => {
      if (!currentUser) return;

      loading.value = true;
      error.value = '';
      
      try {
        const response = await axios.get('/api/friends/all', {
          params: {
            userId: currentUser.id
          }
        });
        
        friends.value = response.data;
        
        // Load blocked friends status
        await loadBlockedFriends();
        
      } catch (err: any) {
        console.error('Error loading friends:', err);
        error.value = 'Failed to load friends. Please try again.';
      } finally {
        loading.value = false;
      }
    };

    const loadBlockedFriends = async () => {
      if (!currentUser) return;
      
      try {
        const response = await axios.get('/api/friends/blocked', {
          params: { userId: currentUser.id }
        });
        blockedFriends.value = new Set(response.data.map((f: Friend) => f.id));
      } catch (error) {
        console.error('Error loading blocked friends:', error);
      }
    };

    const isBlocked = (friendId: number) => {
      return blockedFriends.value.has(friendId);
    };

    const toggleBlock = async (friendId: number) => {
      if (!currentUser) return;

      actionLoading.value[friendId] = true;

      try {
        const isCurrentlyBlocked = isBlocked(friendId);
        
        if (isCurrentlyBlocked) {
          // Unblock friend
          await axios.delete('/api/block/unblock', {
            data: {
              userId: currentUser.id,
              blockedUserId: friendId
            }
          });
          blockedFriends.value.delete(friendId);
        } else {
          // Block friend
          await axios.post('/api/block/block', {
            userId: currentUser.id,
            blockedUserId: friendId
          });
          blockedFriends.value.add(friendId);
        }
        
      } catch (error) {
        console.error('Error toggling block status:', error);
        alert('Failed to update block status. Please try again.');
      } finally {
        actionLoading.value[friendId] = false;
      }
    };

    const removeFriend = async (friendId: number) => {
      if (!currentUser) return;

      // Check if friend is blocked
      if (isBlocked(friendId)) {
        alert('Cannot remove a blocked friend. Please unblock them first.');
        return;
      }

      // Confirm removal
      if (!confirm('Are you sure you want to remove this friend?')) {
        return;
      }

      actionLoading.value[friendId] = true;

      try {
        await axios.put('/api/friends/remove', {
          senderId: currentUser.id,
          receiverId: friendId
        });
        
        // Remove from local state
        friends.value = friends.value.filter(friend => friend.id !== friendId);
        blockedFriends.value.delete(friendId);
        
      } catch (error) {
        console.error('Error removing friend:', error);
        alert('Failed to remove friend. Please try again.');
      } finally {
        actionLoading.value[friendId] = false;
      }
    };

    onMounted(() => {
      loadFriends();
    });

    return {
      friends,
      loading,
      error,
      actionLoading,
      isBlocked,
      toggleBlock,
      removeFriend
    };
  }
});
</script>

<style scoped>
.friends-container {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 2rem 0;
}

.friends-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1rem;
}

.friends-title {
  font-size: 2.5rem;
  font-weight: bold;
  color: #1877f2;
  text-align: center;
  margin-bottom: 2rem;
}

.loading, .error, .no-friends {
  padding: 3rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.friends-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.friend-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: box-shadow 0.2s, transform 0.2s;
}

.friend-card:hover {
  box-shadow: 0 4px 16px rgba(24,119,242,0.12);
  transform: translateY(-2px);
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.friend-avatar {
  color: #1877f2;
}

.friend-details {
  flex: 1;
}

.friend-name {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #1c1e21;
}

.friend-email {
  margin: 0;
  font-size: 0.9rem;
}

.friend-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  border-radius: 6px;
  min-width: 44px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-outline-warning:hover {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #212529;
}

.btn-warning {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #212529;
}

.btn-warning:hover {
  background-color: #ffb300;
  border-color: #ffb300;
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  border-color: #dc3545;
  color: white;
}

/* Responsive design */
@media (max-width: 768px) {
  .friend-card {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .friend-info {
    flex-direction: column;
    text-align: center;
  }
  
  .friend-actions {
    justify-content: center;
  }
  
  .friends-title {
    font-size: 2rem;
  }
}

@media (max-width: 480px) {
  .friends-content {
    padding: 0 0.5rem;
  }
  
  .friend-card {
    padding: 1rem;
  }
  
  .friends-title {
    font-size: 1.75rem;
  }
}
</style>