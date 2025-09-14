package myapp.controller;

import myapp.payload.AdRecommendation;
import myapp.service.AdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdsController {

    @Autowired
    private AdsService adsService;

    @GetMapping("/recommended")
    public ResponseEntity<List<AdRecommendation>> getRecommended(@RequestParam(name = "userId", required = false) Long userId) {
        List<AdRecommendation> ads = adsService.recommendForUser(userId);
        return ResponseEntity.ok(ads);
    }
}
