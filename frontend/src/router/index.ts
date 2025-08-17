import { createRouter, createWebHistory } from 'vue-router';
import RegistrationForm from '../components/RegistrationForm.vue';
import LoginPage from '../components/LoginPage.vue';
import GlobalPosts from '../components/GlobalPosts.vue';
import MyPosts from '../components/MyPosts.vue';
import CreatePost from '../components/CreatePost.vue';
import CreatePlace from '../components/CreatePlace.vue';

const routes = [
  {
    path: '/',
    redirect: '/register'
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
  },
  {
    path: '/my-posts',
    name: 'MyPosts',
    component: MyPosts,
  },
  {
    path: '/create-post',
    name: 'CreatePost',
    component: CreatePost,
  },
  {
    path: '/create-place',
    name: 'CreatePlace',
    component: CreatePlace,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
