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

export interface Place {
  id: number;
  name: string;
  country?: string;
  town?: string;
  description?: string;
  hashtag?: string;
  type?: string;
}

export interface PlaceRating {
  id?: number;
  placeId: number;
  userId: number;
  rating: number;
  comment?: string;
  createdAt?: string;
  hashtag?: string;
}
