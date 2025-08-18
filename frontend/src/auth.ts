import { reactive } from 'vue';

interface UserAuthData {
    id: number;
    email: string;
    role: string;
}

const authStore = reactive<{
    user: UserAuthData | null
}>({ user: null });

// Initialize from localStorage on app load
const storedUser = localStorage.getItem('user');
if (storedUser) {
    authStore.user = JSON.parse(storedUser);
}

export function setAuthData(id: number, email: string, role: string) {
    const userAuthData: UserAuthData = { id, email, role };
    authStore.user = userAuthData;
    localStorage.setItem('user', JSON.stringify(userAuthData));
}

export function getUser() {
    return authStore.user;
}

export function getRole() {
    return authStore.user ? authStore.user.role : null;
}

export function isLoggedIn() {
    return authStore.user !== null;
}

export function logout() {
    authStore.user = null;
    localStorage.removeItem('user');
}
