package com.example.demo.service;

import com.example.demo.dto.TimeseriesPoint;
import com.example.demo.model.Fruit;
import com.example.demo.repo.StockEntryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSeriesService {
  private final StockEntryRepo repo;

  public List<TimeseriesPoint> getSeries(LocalDate start, LocalDate end){
    List<TimeseriesPoint> out = new ArrayList<>();
    for (Fruit f : Fruit.values()){
      repo.findSeries(start, end, f)
          .forEach(se -> out.add(new TimeseriesPoint(se.getDate(), f, se.getQuantity())));
    }
    return out;
  }
}
