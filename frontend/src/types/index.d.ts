export interface Post {
  id: number;
  description: string;
  numberOfLikes: number;
  hashtags: string[];
  reports: number[];
  dateOfCreation: string;
  user: any;
  likes: number[];
}

export interface PostWithReason {
  post: Post;
  reasons: string[];
}
