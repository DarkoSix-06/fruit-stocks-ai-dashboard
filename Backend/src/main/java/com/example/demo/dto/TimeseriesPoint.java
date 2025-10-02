package com.example.demo.dto;

import com.example.demo.model.Fruit;
import java.time.LocalDate;

public record TimeseriesPoint(LocalDate date, Fruit fruit, int quantity) {}
