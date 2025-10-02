package com.example.demo.web;

import com.example.demo.ai.GeminiClient;
import com.example.demo.dto.KpiResponse;
import com.example.demo.dto.SummarizeRequest;
import com.example.demo.dto.SummarizeResponse;
import com.example.demo.dto.TimeseriesPoint;
import com.example.demo.service.KpiService;
import com.example.demo.service.TimeSeriesService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class StocksController {

    private final TimeSeriesService ts;
    private final KpiService kpis;
    private final GeminiClient gemini;

    public StocksController(TimeSeriesService ts, KpiService kpis, GeminiClient gemini) {
        this.ts = ts;
        this.kpis = kpis;
        this.gemini = gemini;
    }

    // ---------- Types & helpers (moved out of method so Java is happy) ----------

    // Simple stats holder (class-level, not a local record)
    private static final class Stats {
        final int first, last, delta;
        final double pct;
        final int min, max;
        final String minDate, maxDate;
        final double avg, std;

        Stats(int first, int last, int delta, double pct,
                int min, String minDate, int max, String maxDate,
                double avg, double std) {
            this.first = first;
            this.last = last;
            this.delta = delta;
            this.pct = pct;
            this.min = min;
            this.minDate = minDate;
            this.max = max;
            this.maxDate = maxDate;
            this.avg = avg;
            this.std = std;
        }
    }

    private Stats computeStats(List<TimeseriesPoint> all, String fruitName) {
        var points = all.stream()
                .filter(p -> p.fruit().name().equals(fruitName))
                .sorted(Comparator.comparing(TimeseriesPoint::date))
                .toList();

        if (points.isEmpty()) {
            return new Stats(0, 0, 0, 0, 0, "-", 0, "-", 0, 0);
        }

        int first = points.get(0).quantity();
        int last = points.get(points.size() - 1).quantity();
        int delta = last - first;
        double pct = first == 0 ? 0 : (delta * 100.0 / first);

        var minP = points.stream().min(Comparator.comparingInt(TimeseriesPoint::quantity)).orElse(points.get(0));
        var maxP = points.stream().max(Comparator.comparingInt(TimeseriesPoint::quantity)).orElse(points.get(0));

        double avg = points.stream().mapToInt(TimeseriesPoint::quantity).average().orElse(0);
        double var = points.stream().mapToDouble(p -> {
            double d = p.quantity() - avg;
            return d * d;
        }).average().orElse(0);
        double std = Math.sqrt(var);

        return new Stats(first, last, delta, pct,
                minP.quantity(), minP.date().toString(),
                maxP.quantity(), maxP.date().toString(),
                avg, std);
    }

    // Emoji based on percent change (no Greek symbols)
    private String emoji(double pct) {
        if (pct >= 10)
            return "🔥";
        if (pct >= 3)
            return "👍";
        if (pct > -3)
            return "😐";
        if (pct > -10)
            return "⚠️";
        return "🚨";
    }

    private String pct1(double v) {
        return String.format("%+.1f%%", v);
    }

    private String trendLine(String fruit, Stats s) {
        return "%s change %s (%s), first=%d, last=%d, min=%d on %s, max=%d on %s, avg=%.1f, volatility score=%.1f"
                .formatted(fruit, pct1(s.pct), emoji(s.pct),
                        s.first, s.last, s.min, s.minDate, s.max, s.maxDate, s.avg, s.std);
    }

    // ---------------------------------------------------------------------------

    // --- Time series for chart/table
    @GetMapping("/stocks")
    public List<TimeseriesPoint> stocks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ts.getSeries(startDate, endDate);
    }

    // --- KPI totals for cards
    @GetMapping("/kpis")
    public KpiResponse kpi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        var t = kpis.totals(startDate, endDate);
        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        return new KpiResponse(t.apple(), t.orange(), t.banana(), t.grand(), days);
    }

    // --- AI summary (defensive, chart-aware, emoji trend tags)
    @PostMapping("/summarize")
    public SummarizeResponse summarize(@RequestBody SummarizeRequest req) {
        try {
            // Guard: invalid date order / nulls
            if (req.startDate() == null || req.endDate() == null || req.endDate().isBefore(req.startDate())) {
                return new SummarizeResponse("error",
                        "Invalid date range. Ensure startDate <= endDate and both are YYYY-MM-DD.");
            }

            var totals = kpis.totals(req.startDate(), req.endDate());
            var all = ts.getSeries(req.startDate(), req.endDate());

            // Guard: no data in range
            if (all == null || all.isEmpty()) {
                String msg = "No data found for " + req.startDate() + " to " + req.endDate()
                        + ". Try a range within the last 90 seeded days.";
                String text = """
                        **Headline**
                        • %s
                        • Action: choose a wider date range that overlaps the seeded demo data.
                        """.formatted(msg);
                return new SummarizeResponse("ok", text);
            }

            // Compute stats per fruit
            var A = computeStats(all, "APPLE");
            var O = computeStats(all, "ORANGE");
            var B = computeStats(all, "BANANA");

            // Build prompt (no σ/Δ, uses emojis)
            String prompt = """
                    You are a supply & inventory analyst. Write an impressive, manager-ready summary of fruit stocks.

                    Date range: %s to %s  (days=%d)
                    Totals (sum over range):
                      • Apple:  %d
                      • Orange: %d
                      • Banana: %d
                      • Grand:  %d

                    Per-fruit trend stats (from line chart, emojis show direction/strength):
                      %s
                      %s
                      %s

                    Write the output in this exact structure (Markdown bullets allowed, no code fences):
                    **Headline (1 sentence)**
                    • Key Highlights (3–5 bullets: mention rises/dips, which fruit led, stability/volatility, anomalies, and peak/dip dates)
                    • KPIs (compact): Apple change, Orange change, Banana change, Highest peak day/value, Lowest dip day/value
                    • Insight on Seasonality/Pattern (1–2 bullets; infer weekly waves/volatility)
                    • Action (1 line, specific operational recommendation)
                    • Risk/Watch (1 line: what to monitor next period)

                    Keep it concise but insightful. Avoid saying “insufficient information”.
                    """
                    .formatted(
                            req.startDate(), req.endDate(),
                            (int) (req.endDate().toEpochDay() - req.startDate().toEpochDay()) + 1,
                            totals.apple(), totals.orange(), totals.banana(), totals.grand(),
                            trendLine("APPLE", A),
                            trendLine("ORANGE", O),
                            trendLine("BANANA", B));

            String text = gemini.summarize(prompt); // client is defensive; no 500s
            return new SummarizeResponse("gemini", text);

        } catch (Exception e) {
            return new SummarizeResponse("error",
                    "Summarize failed: " + e.getClass().getSimpleName() + " – " + String.valueOf(e.getMessage()));
        }
    }
}
