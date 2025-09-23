package myapp.service;

import myapp.payload.BadUserDetection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledBadUserDetectionService {

    @Autowired
    private BadUserDetectionService badUserDetectionService;

    @Scheduled(fixedRate = 900000) // 15 minuta = 900000 ms
    public void performScheduledBadUserDetection() 
    {
        try 
        {
            System.out.println("=== POKRETANJE PERIODICNE DETEKCIJE LOSIH KORISNIKA ===");
            System.out.println("Vreme: " + LocalDateTime.now());
            
            List<BadUserDetection> suspiciousUsers = badUserDetectionService.detectBadUsers();
            
            System.out.println("Detektovano " + suspiciousUsers.size() + " sumnjivih korisnika:");
            for (BadUserDetection detection : suspiciousUsers) 
            {
                if (detection.isSuspicious()) {
                    System.out.println("- " + detection.getUser().getEmail() + 
                                     " - Razlog: " + detection.getSuspicionReason());
                }
            }
            
            System.out.println("=== PERIODICNA DETEKCIJA ZAVRSENA ===\n");
            
        } 
        catch (Exception e) 
        {
            System.err.println("Greska tokom periodične detekcije loših korisnika: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 2 * * *") // Svaki dan u 02:00
    public void performDailyDeepAnalysis() {
        try {
            System.out.println("=== POKRETANJE DNEVNE DUBOKE ANALIZE ===");
            System.out.println("Vreme: " + LocalDateTime.now());
            
            List<BadUserDetection> suspiciousUsers = badUserDetectionService.detectBadUsers();
            
            int suspendedCount = 0;
            for (BadUserDetection detection : suspiciousUsers) 
            {
                if (detection.isSuspicious()) 
                {
                    suspendedCount++;
                }
            }
            
            System.out.println("Dnevna analiza zavrsena:");
            System.out.println("- Ukupno analiziranih korisnika: " + suspiciousUsers.size());
            System.out.println("- Broj suspendovanih: " + suspendedCount);
            System.out.println("=== DNEVNA ANALIZA ZAVRSENA ===\n");
            
        } 
        catch (Exception e) 
        {
            System.err.println("Greska tokom dnevne analize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 3600000) // 1 sat = 3600000 ms
    public void cleanupExpiredSuspensions() 
    {
        try 
        {
            System.out.println("Pokretanje ciscenja isteklih suspenzija...");
            
            int clearedCount = badUserDetectionService.clearExpiredSuspensions();
            
            if (clearedCount > 0) 
            {
                System.out.println("Ocisceno " + clearedCount + " isteklih suspenzija");
            }
            
        } catch (Exception e) {
            System.err.println("Greska tokom ciscenja suspenzija: " + e.getMessage());
        }
    }
}