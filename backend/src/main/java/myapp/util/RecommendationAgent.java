package myapp.util;

import myapp.model.Post;
import myapp.model.User;
import java.util.*;

public class RecommendationAgent {
    private static List<User> allUsers = new ArrayList<>();
    private static List<Post> allPosts = new ArrayList<>();

    public static void setAllUsers(List<User> users) {
        allUsers = users;
    }
    public static void setAllPosts(List<Post> posts) {
        allPosts = posts;
    }
    public static List<User> getAllUsers() {
        return allUsers;
    }
    public static List<Post> getAllPosts() {
        return allPosts;
    }
    public static Set<Long> getUserLikes(User user) {
        Set<Long> liked = new HashSet<>();
        for (Post p : allPosts) {
            if (p.getLikes().contains(user.getId())) {
                liked.add(p.getId());
            }
        }
        return liked;
    }
    public static double pearsonSimilarity(User u1, User u2) {
        Set<Long> u1Likes = getUserLikes(u1);
        Set<Long> u2Likes = getUserLikes(u2);
        Set<Long> allPostIds = new HashSet<>(u1Likes);
        allPostIds.addAll(u2Likes);
        int n = allPostIds.size();
        if (n == 0) return 0.0;
        double sum1 = 0, sum2 = 0, sum1Sq = 0, sum2Sq = 0, pSum = 0;
        for (Long postId : allPostIds) {
            int x = u1Likes.contains(postId) ? 1 : 0;
            int y = u2Likes.contains(postId) ? 1 : 0;
            sum1 += x;
            sum2 += y;
            sum1Sq += x * x;
            sum2Sq += y * y;
            pSum += x * y;
        }
        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - sum1 * sum1 / n) * (sum2Sq - sum2 * sum2 / n));
        if (den == 0) return 0.0;
        return num / den;
    }
    // Exclude currentUserId from both sets before calculating overlap
    public static double postSimilarity(Long postA, Long postB, Long currentUserId) {
        Set<Long> likesA = getPostLikes(postA);
        Set<Long> likesB = getPostLikes(postB);
        if (currentUserId != null) {
            likesA.remove(currentUserId);
            likesB.remove(currentUserId);
        }
        if (likesA.isEmpty() || likesB.isEmpty()) return 0.0;
        Set<Long> intersection = new HashSet<>(likesA);
        intersection.retainAll(likesB);
        return (double) intersection.size() / Math.max(likesA.size(), likesB.size());
    }
    private static Set<Long> getPostLikes(Long postId) {
        for (Post p : allPosts) {
            if (p.getId().equals(postId)) {
                return new HashSet<>(p.getLikes());
            }
        }
        return Collections.emptySet();
    }
}
