import { createRouter, createWebHistory } from 'vue-router';
import RegistrationForm from '../components/RegistrationForm.vue';
import LoginPage from '../components/LoginPage.vue';
import GlobalPosts from '../components/GlobalPosts.vue';
import MyPosts from '../components/MyPosts.vue';
import CreatePlace from '../components/CreatePlace.vue';
import CreatePost from '../components/CreatePost.vue';

import { isLoggedIn, getRole } from '../auth'; // Import auth functions

const routes = [
  {
    path: '/',
    redirect: '/login' // Redirect to login by default
  },  
  {
    path: '/register',
    name: 'Register',
    component: RegistrationForm,
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
  },
  {
    path: '/global-posts',
    name: 'GlobalPosts',
    component: GlobalPosts,
    meta: { requiresAuth: true }
  },
  {
    path: '/my-posts',
    name: 'MyPosts',
    component: MyPosts,
    meta: { requiresAuth: true }
  },
  {
    path: '/create-post',
    name: 'CreatePost',
    component: CreatePost,
    meta: { requiresAuth: true } // Requires authentication
  }

];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isLoggedIn()) {
    next('/login');
  } else if ((to.name === 'Login' || to.name === 'Register') && isLoggedIn()) {
    next('/global-posts'); // Redirect logged-in users away from login/register
  } else if (to.meta.requiredRole && getRole() !== to.meta.requiredRole) {
    next('/global-posts'); // Redirect to global posts if role doesn't match
  } else {
    next();
  }
});

export default router;
