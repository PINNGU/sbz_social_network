package myapp.payload;

import myapp.model.Place;

public class AdRecommendation {
    private Place place;
    private double score;
    private String reason;

    public AdRecommendation() {}

    public AdRecommendation(Place place, double score, String reason) {
        this.place = place;
        this.score = score;
        this.reason = reason;
    }

    public Place getPlace() { return place; }
    public void setPlace(Place place) { this.place = place; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
