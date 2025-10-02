package com.example.demo.dto;

import java.time.LocalDate;

public record SummarizeRequest(LocalDate startDate, LocalDate endDate) {}
