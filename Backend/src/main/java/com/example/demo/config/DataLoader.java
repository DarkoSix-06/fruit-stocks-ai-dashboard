package com.example.demo.config;

import com.example.demo.model.Fruit;
import com.example.demo.model.StockEntry;
import com.example.demo.repo.StockEntryRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Random;

@Configuration
public class DataLoader {

  @Bean
  CommandLineRunner seed(StockEntryRepo repo){
    return args -> {
      Random r = new Random(42);                   // deterministic demo data
      LocalDate start = LocalDate.now().minusDays(90);
      for (int i = 0; i <= 90; i++) {              // ~91 days
        LocalDate d = start.plusDays(i);
        for (Fruit f : Fruit.values()) {
          int base = 100;
          int drift = r.nextInt(21) - 10;          // -10..+10
          int weekly = (i % 7) - 3;                // small weekly wave
          int qty = Math.max(0, base + drift + weekly);
          repo.save(StockEntry.builder()
              .date(d).fruit(f).quantity(qty).build());
        }
      }
    };
  }
}
