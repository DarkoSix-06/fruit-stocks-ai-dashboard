# 🔥 Fruit Stocks AI Dashboard 🍎🍊🍌

A full-stack demo that tracks **Apple**, **Orange**, and **Banana** stocks over time and generates **AI summaries** for any selected date range.

- **Frontend:** React + Vite, Recharts, `react-markdown`
- **Backend:** Spring Boot 3, H2 in memory, JPA, Java `HttpClient` to Gemini 2.0
- **UI:** KPI cards, one clean line chart, and a Markdown summary
- **AI Output:** first and last values, change percent, min and max with dates, averages, a simple volatility score, and trend tags 🔥 👍 😐 ⚠️ 🚨  
  (Prompt avoids Greek symbols. No Δ or σ.)

---

## 📸 Demo

![Dashboard screenshot](https://github.com/user-attachments/assets/59437fe3-88b2-4db3-a54c-9c8024e134b7 "KPI cards, line chart, and AI summary")

---

## ✨ Features

- 🎯 Date range picker that updates KPI cards and the chart  
- 🧠 One-click **Summarize** that returns an executive Markdown brief  
- 🌱 H2 auto-seeds about 90 days of mock data on startup  
- 🛡️ Defensive backend. On AI error or missing key, returns a friendly fallback with HTTP 200  
- 🔗 Local CORS enabled for the Vite dev server

---

## 🧰 Stack

**Frontend**
- React + Vite (JavaScript, JSX)
- Recharts
- `react-markdown` with `remark-gfm`
- `lucide-react`

**Backend**
- Spring Boot 3 (Web, JPA)
- H2 in-memory database
- Java 17+
- `java.net.http.HttpClient`
- Gemini 2.0 (`gemini-2.0-flash` default)

---

## 🏗️ Architecture

- Client calls `GET /stocks` for time series and `GET /kpis` for computed stats  
- Client calls `POST /summarize` with the selected range and the stats payload  
- Spring services compute per-fruit measures and build a compact prompt  
- Gemini returns Markdown. On error, the service returns a local fallback

High-level flow:

    React UI → /stocks, /kpis → Spring services → H2
    React UI → /summarize     → Spring service  → Gemini 2.0
                                           ↘ returns fallback on AI error

---

## 🗂️ Project Structure

    ├─ backend/
    │  ├─ src/main/java/com/example/demo/
    │  │  ├─ web/StocksController.java
    │  │  ├─ service/TimeSeriesService.java
    │  │  ├─ service/KpiService.java
    │  │  ├─ ai/GeminiClient.java
    │  │  ├─ model/Fruit.java
    │  │  ├─ model/StockEntry.java
    │  │  └─ repo/StockEntryRepo.java
    │  ├─ src/main/resources/application.yml
    │  └─ pom.xml
    └─ frontend/
       ├─ src/App.jsx
       ├─ src/api.js
       ├─ index.html
       └─ package.json

---

## ⚙️ Quick Start

### Requirements
- Java 17 or newer  
- Node 18 or newer  
- Gemini API key optional. The app returns a fallback summary when the key is missing.

### 1) Backend

Windows PowerShell:

    cd backend
    $env:GEMINI_API_KEY="YOUR_GEMINI_KEY"
    .\mvnw.cmd clean compile
    .\mvnw.cmd spring-boot:run

macOS or Linux:

    cd backend
    export GEMINI_API_KEY="YOUR_GEMINI_KEY"
    ./mvnw clean compile
    ./mvnw spring-boot:run

### 2) Frontend

    cd frontend
    npm i
    echo VITE_API_BASE=http://localhost:8081/api/v1 > .env
    npm run dev

Open the Vite dev server URL printed in the terminal.

---

## 🧾 Configuration

Backend file `backend/src/main/resources/application.yml`:

    server:
      port: 8081

    spring:
      datasource:
        url: jdbc:h2:mem:stocks;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
        driverClassName: org.h2.Driver
        username: sa
        password: ""
      jpa:
        hibernate:
          ddl-auto: create
      h2:
        console:
          enabled: true
          path: /h2

    app:
      gemini:
        apiKey: ${GEMINI_API_KEY:}  # blank uses local fallback
        model: gemini-2.0-flash     # or gemini-2.0-pro

Frontend `.env`:

    VITE_API_BASE=http://localhost:8081/api/v1

---

## 🧠 AI Summary Prompt (Template)

    Write a short, chart-aware summary for fruit inventory over {from} to {to}.
    Use Markdown. Keep the tone factual and concise.
    Do not use Greek symbols. Do not write Δ or σ.

    For each fruit provide:
    - first and last values
    - signed change percent
    - min and max with dates
    - average
    - a simple volatility score label: low, medium, or high

    Add sections: Headline, Key Highlights, KPIs, Pattern, Action, Risk/Watch.
    Tag each fruit with an emoji by trend strength:
    🔥 ≥ +10%, 👍 ≥ +3%, 😐 −3% to +3%, ⚠️ ≤ −10%, 🚨 ≤ −15%.

    Context:
    {compact JSON with stats per fruit}

---
## 🔐 Security and CORS

- Local CORS allows the Vite dev origin  
- Do not send personal data to the AI endpoint  
- For production, restrict allowed origins, add input validation, and rate-limit `/summarize`

---

## 🐞 Troubleshooting

- Frontend cannot reach backend: check `VITE_API_BASE`, confirm backend on port 8081  
- H2 console not loading: visit `/h2`, use the JDBC URL above  
- AI summary shows fallback: set `GEMINI_API_KEY`, verify model access  
- Empty chart: confirm seeding ran and date range covers seeded days

---

## 🚀 Production Tips

- Replace H2 with PostgreSQL or MySQL  
- Add index on `(date, fruit)`  
- Cache KPI responses for common ranges  
- Move seeding to a scheduler or ETL  
- Serve the built React app behind a reverse proxy  
- Add rate limits and structured logging

---

## 📜 License

MIT License

---

## 👤 Author

**Darko Six_Nimesh Tharaka**  
AI Engineer | Data Science | Exploring AI, Machine Learning, and Big Data

📧 bandaranayakanimesh@gmail.com  
🔗 https://www.linkedin.com/in/nimesh-bandaranayake-0a2912304
