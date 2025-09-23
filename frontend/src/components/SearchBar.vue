<template>
  <div class="search-container position-relative">
    <div class="input-group">
      <input
        type="text"
        class="form-control search-input"
        placeholder="Search users..."
        v-model="searchQuery"
        @input="handleSearch"
        @focus="showDropdown = true"
        @blur="handleBlur"
      />
      <span class="input-group-text search-icon">
        <i class="fas fa-search"></i>
      </span>
    </div>
    
    <!-- Dropdown Results -->
    <div 
      v-if="showDropdown && (filteredUsers.length > 0 || (searchQuery.trim() && filteredUsers.length === 0))"
      class="search-dropdown position-absolute w-100"
      @mousedown.prevent
      @click="handleDropdownClick"
    >
      <div v-if="loadingUsers" class="dropdown-item text-center">
        <span class="spinner-border spinner-border-sm me-2"></span>
        Loading users...
      </div>
      
      <div v-else-if="filteredUsers.length === 0 && searchQuery.trim()" class="dropdown-item text-muted">
        No users found
      </div>
      
      <div 
        v-else
        v-for="user in filteredUsers" 
        :key="user.id" 
        class="dropdown-item d-flex justify-content-between align-items-center"
      >
        <div class="user-info">
          <strong>{{ user.name }} {{ user.surname }}</strong>
          <small class="text-muted d-block">{{ user.email }}</small>
        </div>
        
        <button
          :class="[
            'btn btn-sm',
            getButtonClass(user.id)
          ]"
          @click="handleFriendAction(user.id)"
          :disabled="acceptedFriends.has(user.id) || friendRequestLoading[user.id]"
        >
          <span v-if="friendRequestLoading[user.id]" class="spinner-border spinner-border-sm me-1"></span>
          {{ getButtonText(user.id) }}
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { getUser } from '../auth';

interface User {
  id: number;
  name: string;
  surname: string;
  email: string;
}

interface FriendRequest {
  id: number;
  senderId: number;
  receiverId: number;
  status: string;
}

export default defineComponent({
  name: 'SearchBar',
  setup() {
    const searchQuery = ref('');
    const allUsers = ref<User[]>([]);
    const showDropdown = ref(false);
    const loadingUsers = ref(false);
    const loadingFriendRequests = ref(false);
    const friendRequestLoading = ref<{ [key: number]: boolean }>({});
    const sentRequests = ref<Set<number>>(new Set());
    const acceptedFriends = ref<Set<number>>(new Set());
    const friendRequests = ref<FriendRequest[]>([]);

    const currentUser = computed(() => getUser());

    // Filter users based on search query
    const filteredUsers = computed(() => {
      if (!searchQuery.value.trim()) {
        return [];
      }

      const query = searchQuery.value.toLowerCase().trim();
      return allUsers.value
        .filter(user => {
          if (user.id === currentUser.value?.id) return false; 
          
          const fullName = `${user.name} ${user.surname}`.toLowerCase();
          const firstName = user.name.toLowerCase();
          const lastName = user.surname.toLowerCase();
          
          if (firstName.startsWith(query) || lastName.startsWith(query) || fullName.startsWith(query)) {
            return true;
          }
          
          const queryWords = query.split(' ').filter(word => word.length > 0);
          if (queryWords.length > 1) {
            return firstName.startsWith(queryWords[0]) && lastName.startsWith(queryWords[1]);
          }
          
          return false;
        })
        .slice(0, 10); 
    });

    const loadAllUsers = async () => {
      if (!currentUser.value) return;

      loadingUsers.value = true;
      try {
        const response = await axios.get('/api/users/all', {
          params: {
            userId: currentUser.value.id
          }
        });
        allUsers.value = response.data;
      } catch (error) {
        console.error('Error loading users:', error);
        allUsers.value = [];
      } finally {
        loadingUsers.value = false;
      }
    };

    // Load all friend requests sent by current user
    const loadFriendRequests = async () => {
      if (!currentUser.value) return;

      loadingFriendRequests.value = true;
      try {
        // Load sent requests
        const sentResponse = await axios.get('/api/friendRequest/allSent', {
          params: {
            userId: currentUser.value.id
          }
        });
        
        // Load received requests
        const receivedResponse = await axios.get('/api/friendRequest/all', {
          params: {
            userId: currentUser.value.id
          }
        });
        
        // Combine both arrays
        friendRequests.value = [...sentResponse.data, ...receivedResponse.data];
        
        // Update sentRequests set based on loaded friend requests
        const newSentRequests = new Set<number>();
        const newAcceptedFriends = new Set<number>();
        
        friendRequests.value.forEach(request => {
          // Filter by status - only pending requests should show as "Request Sent"
          if (request.senderId === currentUser.value?.id && request.status === 'PENDING') {
            newSentRequests.add(request.receiverId);
          }
          // Check for accepted friend requests (BOTH DIRECTIONS)
          if (request.status === 'ACCEPTED') {
            if (request.senderId === currentUser.value?.id) {
              newAcceptedFriends.add(request.receiverId);
            } else if (request.receiverId === currentUser.value?.id) {
              newAcceptedFriends.add(request.senderId);
            }
          }
        });
        
        sentRequests.value = newSentRequests;
        acceptedFriends.value = newAcceptedFriends;
        
      } catch (error) {
        console.error('Error loading friend requests:', error);
      } finally {
        loadingFriendRequests.value = false;
      }
    };

    const handleSearch = () => {
      // No need for debouncing since we're filtering locally
      showDropdown.value = true;
    };

    const handleBlur = () => {
      // Delay hiding dropdown to allow clicking on results
      setTimeout(() => {
        showDropdown.value = false;
      }, 150);
    };

    const handleDropdownClick = (event: Event) => {
      // Prevent dropdown from closing when clicking inside it
      event.stopPropagation();
    };

    const getButtonClass = (userId: number) => {
      if (acceptedFriends.value.has(userId)) {
        return 'btn-success';
      }
      if (sentRequests.value.has(userId)) {
        return 'btn-warning';
      }
      return 'btn-primary';
    };

    const getButtonText = (userId: number) => {
      if (acceptedFriends.value.has(userId)) {
        return 'Friends';
      }
      if (sentRequests.value.has(userId)) {
        return 'Request Sent';
      }
      return 'Add Friend';
    };

    const handleFriendAction = async (userId: number) => {
      if (!currentUser.value) return;

      // Don't allow actions on existing friends
      if (acceptedFriends.value.has(userId)) {
        return;
      }

      friendRequestLoading.value[userId] = true;

      try {
        if (sentRequests.value.has(userId)) {
          // Cancel friend request
          await axios.delete('/api/friendRequest/delete', {
            params: {
              userId: currentUser.value.id,
              friendId: userId
            }
          });
          
          // Remove from local state
          sentRequests.value.delete(userId);
          
          // Remove from friendRequests array
          friendRequests.value = friendRequests.value.filter(
            request => !(request.senderId === currentUser.value?.id && request.receiverId === userId)
          );
          
        } else {
          // Send friend request
          await axios.post('/api/friendRequest/send', {
            senderId: currentUser.value.id,
            receiverId: userId
          });
          
          // Add to local state
          sentRequests.value.add(userId);
          
          // Add to friendRequests array (create temporary object since we don't get response with ID)
          friendRequests.value.push({
            id: Date.now(), // Temporary ID
            senderId: currentUser.value.id,
            receiverId: userId,
            status: 'PENDING'
          });
        }
      } catch (error) {
        console.error('Friend request error:', error);
        // You could add a toast notification here
      } finally {
        friendRequestLoading.value[userId] = false;
      }
    };

    // Load users and friend requests when component mounts
    onMounted(async () => {
      if (currentUser.value) {
        await Promise.all([
          loadAllUsers(),
          loadFriendRequests()
        ]);
      }
    });

    return {
      searchQuery,
      filteredUsers,
      showDropdown,
      loadingUsers,
      loadingFriendRequests,
      friendRequestLoading,
      sentRequests,
      acceptedFriends,
      handleSearch,
      handleBlur,
      handleDropdownClick,
      getButtonClass,
      getButtonText,
      handleFriendAction,
    };
  },
});
</script>

<style scoped>
.search-container {
  width: 300px;
  max-width: 100%;
}

.search-input {
  border-radius: 20px 0 0 20px;
  border-right: none;
}

.search-icon {
  border-radius: 0 20px 20px 0;
  background-color: white;
  border-left: none;
}

.search-dropdown {
  top: 100%;
  z-index: 1050;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  max-height: 400px;
  overflow-y: auto;
}

.dropdown-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: default;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
}

.dropdown-item:last-child {
  border-bottom: none;
}

.user-info {
  flex: 1;
}

.btn-sm {
  font-size: 0.8rem;
  padding: 0.25rem 0.75rem;
}

@media (max-width: 768px) {
  .search-container {
    width: 200px;
  }
}
</style>
