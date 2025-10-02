package com.example.demo.repo;

import com.example.demo.model.Fruit;
import com.example.demo.model.StockEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockEntryRepo extends JpaRepository<StockEntry, Long> {
  List<StockEntry> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

  @Query("""
         select s from StockEntry s
         where s.date between :start and :end and s.fruit = :fruit
         order by s.date asc
         """)
  List<StockEntry> findSeries(@Param("start") LocalDate start,
                              @Param("end") LocalDate end,
                              @Param("fruit") Fruit fruit);
}
