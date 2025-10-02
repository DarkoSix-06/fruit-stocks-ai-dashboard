package com.example.demo.service;

import com.example.demo.model.StockEntry;
import com.example.demo.repo.StockEntryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KpiService {
  private final StockEntryRepo repo;

  public record Totals(int apple, int orange, int banana, int grand) {}

  public Totals totals(LocalDate start, LocalDate end){
    List<StockEntry> all = repo.findByDateBetweenOrderByDateAsc(start, end);
    int a=0,o=0,b=0;
    for (StockEntry s : all){
      switch (s.getFruit()){
        case APPLE -> a += s.getQuantity();
        case ORANGE -> o += s.getQuantity();
        case BANANA -> b += s.getQuantity();
      }
    }
    return new Totals(a,o,b,a+o+b);
  }
}
