<template>
  <div class="friend-requests-container position-relative">
    <!-- Friends Button -->
    <button
      class="btn btn-outline-light position-relative"
      @click="toggleDropdown"
      @blur="handleBlur"
    >
      <i class="fas fa-user-friends"></i>
      <!-- Notification badge -->
      <span 
        v-if="pendingRequests.length > 0" 
        class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
      >
        {{ pendingRequests.length }}
      </span>
    </button>
    
    <!-- Dropdown -->
    <div 
      v-if="showDropdown"
      class="friend-requests-dropdown position-absolute"
      @mousedown.prevent
      @click="handleDropdownClick"
    >
      <div class="dropdown-header">
        <h6 class="mb-0">Friend Requests</h6>
      </div>
      
      <div v-if="loadingRequests" class="dropdown-item text-center">
        <span class="spinner-border spinner-border-sm me-2"></span>
        Loading requests...
      </div>
      
      <div v-else-if="friendRequests.length === 0" class="dropdown-item text-muted text-center">
        No friend requests
      </div>
      
      <div 
        v-else
        v-for="request in friendRequests" 
        :key="request.id"
        class="friend-request-card"
      >
        <div class="request-content">
          <div class="request-info">
            <div class="request-text">
              <span v-if="request.status === 'PENDING'">
                <strong>{{ getUserName(request.senderId) }}</strong> has sent you a friend request
              </span>
              <span v-else-if="request.status === 'ACCEPTED'">
                Friend Request accepted: <strong>{{ getUserName(request.senderId) }}</strong>
              </span>
              <span v-else-if="request.status === 'REJECTED'">
                Friend Request declined: <strong>{{ getUserName(request.senderId) }}</strong>
              </span>
            </div>
            <div class="request-timestamp">
              {{ formatTimestamp(request.timestamp) }}
            </div>
          </div>
          
          <!-- Action buttons for pending requests -->
          <div v-if="request.status === 'PENDING'" class="request-actions">
            <button
              class="btn btn-sm btn-success me-1"
              @click="handleAccept(request.id)"
              :disabled="actionLoading[request.id]"
            >
              <span v-if="actionLoading[request.id]" class="spinner-border spinner-border-sm me-1"></span>
              Accept
            </button>
            <button
              class="btn btn-sm btn-danger"
              @click="handleReject(request.id)"
              :disabled="actionLoading[request.id]"
            >
              <span v-if="actionLoading[request.id]" class="spinner-border spinner-border-sm me-1"></span>
              Reject
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { getUser } from '../auth';

interface FriendRequest {
  id: number;
  senderId: number;
  receiverId: number;
  status: string;
  timestamp: string;
}

interface User {
  id: number;
  name: string;
  surname: string;
  email: string;
}

export default defineComponent({
  name: 'FriendRequestsDropdown',
  setup() {
    const showDropdown = ref(false);
    const friendRequests = ref<FriendRequest[]>([]);
    const users = ref<User[]>([]);
    const loadingRequests = ref(false);
    const actionLoading = ref<{ [key: number]: boolean }>({});

    const currentUser = computed(() => getUser());

    const pendingRequests = computed(() => 
      friendRequests.value.filter(request => request.status === 'PENDING')
    );

    const toggleDropdown = () => {
      showDropdown.value = !showDropdown.value;
      if (showDropdown.value) {
        loadFriendRequests();
      }
    };

    const handleBlur = () => {
      setTimeout(() => {
        showDropdown.value = false;
      }, 150);
    };

    const handleDropdownClick = (event: Event) => {
      event.stopPropagation();
    };

    const loadFriendRequests = async () => {
      if (!currentUser.value) return;

      loadingRequests.value = true;
      try {
        const response = await axios.get('/api/friendRequest/all', {
          params: {
            userId: currentUser.value.id
          }
        });
        
        friendRequests.value = response.data;
        
        // Load user details for senders
        await loadUsers();
      } catch (error) {
        console.error('Error loading friend requests:', error);
      } finally {
        loadingRequests.value = false;
      }
    };

    const loadUsers = async () => {
      try {
        const response = await axios.get('/api/users/all', {
          params: {
            userId: currentUser.value?.id
          }
        });
        users.value = response.data;
      } catch (error) {
        console.error('Error loading users:', error);
      }
    };

    const getUserName = (userId: number) => {
      const user = users.value.find(u => u.id === userId);
      return user ? `${user.name} ${user.surname}` : 'Unknown User';
    };

    const formatTimestamp = (timestamp: string) => {
      const date = new Date(timestamp);
      const now = new Date();
      const diffInMs = now.getTime() - date.getTime();
      const diffInHours = diffInMs / (1000 * 60 * 60);
      const diffInDays = diffInMs / (1000 * 60 * 60 * 24);

      if (diffInHours < 1) {
        const minutes = Math.floor(diffInMs / (1000 * 60));
        return `${minutes}m`;
      } else if (diffInHours < 24) {
        return `${Math.floor(diffInHours)}h`;
      } else if (diffInDays < 7) {
        return `${Math.floor(diffInDays)}d`;
      } else {
        return date.toLocaleDateString('en-GB', { 
          day: '2-digit', 
          month: '2-digit' 
        });
      }
    };

    const handleAccept = async (requestId: number) => {
      actionLoading.value[requestId] = true;
      try {
        await axios.put('/api/friendRequest/accept', requestId, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        // Update local state
        const request = friendRequests.value.find(r => r.id === requestId);
        if (request) {
          request.status = 'ACCEPTED';
          request.timestamp = new Date().toISOString();
        }
      } catch (error) {
        console.error('Error accepting friend request:', error);
      } finally {
        actionLoading.value[requestId] = false;
      }
    };

    const handleReject = async (requestId: number) => {
      actionLoading.value[requestId] = true;
      try {
        await axios.put('/api/friendRequest/reject', requestId, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        // Update local state
        const request = friendRequests.value.find(r => r.id === requestId);
        if (request) {
          request.status = 'REJECTED';
          request.timestamp = new Date().toISOString();
        }
      } catch (error) {
        console.error('Error rejecting friend request:', error);
      } finally {
        actionLoading.value[requestId] = false;
      }
    };

    onMounted(() => {
      if (currentUser.value) {
        loadFriendRequests();
      }
    });

    return {
      showDropdown,
      friendRequests,
      pendingRequests,
      loadingRequests,
      actionLoading,
      toggleDropdown,
      handleBlur,
      handleDropdownClick,
      getUserName,
      formatTimestamp,
      handleAccept,
      handleReject
    };
  }
});
</script>

<style scoped>
.friend-requests-container {
  display: inline-block;
}

.friend-requests-dropdown {
  top: calc(100% + 5px);
  right: 0;
  z-index: 1050;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  min-width: 350px;
  max-height: 400px;
  overflow-y: auto;
}

.dropdown-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.friend-request-card {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.friend-request-card:hover {
  background-color: #f8f9fa;
}

.friend-request-card:last-child {
  border-bottom: none;
}

.request-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.request-info {
  flex: 1;
}

.request-text {
  font-size: 0.9rem;
  margin-bottom: 4px;
}

.request-timestamp {
  font-size: 0.75rem;
  color: #6c757d;
}

.request-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.btn-sm {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
}

.dropdown-item {
  padding: 12px 16px;
}

@media (max-width: 768px) {
  .friend-requests-dropdown {
    min-width: 300px;
    right: -50px;
  }
}
</style>