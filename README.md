# 🔥 Fruit Stocks AI Dashboard 🔥

A full-stack demo that tracks **Apple**, **Orange**, and **Banana** stocks over time and generates **AI-powered insights** for any selected date range.

- **Frontend:** React + Vite (JSX), Recharts, `react-markdown` for tidy summaries  
- **Backend:** Spring Boot 3, H2 in-memory DB (seeded mock data), Java `HttpClient` calling **Gemini 2.0**  
- **UI:** KPI cards + Line chart + AI Summary (Markdown). No clutter charts.  
- **AI Output:** Chart-aware summary (first/last, change %, min/max dates, averages, volatility score) with **emoji trend tags** (🔥 👍 😐 ⚠️ 🚨).  
  *(No Greek symbols; no Δ or σ in the prompt.)*

---
## 🧱 DEMO 
![WhatsApp Image 2025-10-02 at 01 22 06_f12223b9](https://github.com/user-attachments/assets/59437fe3-88b2-4db3-a54c-9c8024e134b7)

---

## ✨ Features

- Date range picker → refreshes **KPI cards** and **line chart**
- One-click **Summarize** → backend `/summarize` returns an executive summary (Markdown)
- H2 DB auto-seeds ~90 days of demo data on startup
- Defensive backend (no 500s on AI errors), CORS enabled

---

## 🧱 Tech Stack

**Frontend**
- React + Vite (JavaScript, JSX)
- Recharts
- `react-markdown` + `remark-gfm`
- `lucide-react` icons

**Backend**
- Spring Boot 3 (Web, JPA)
- H2 Database (in-memory)
- Java 17+
- `java.net.http.HttpClient` (no OkHttp)
- Gemini **2.0** (`gemini-2.0-flash` default)

---

## 📁 Project Structure

├─ backend/
│ ├─ src/main/java/com/example/demo/...
│ │ ├─ web/StocksController.java
│ │ ├─ service/TimeSeriesService.java
│ │ ├─ service/KpiService.java
│ │ ├─ ai/GeminiClient.java
│ │ ├─ model/{Fruit,StockEntry}.java
│ │ └─ repo/StockEntryRepo.java
│ ├─ src/main/resources/application.yml
│ └─ pom.xml
└─ frontend/
├─ src/App.jsx
├─ src/api.js
├─ index.html
└─ package.json


---

## 🚀 Quick Start

### Requirements
- **Java 17+**
- **Node 18+**
- A **Gemini API key** (optional; you’ll still get a fallback summary if missing)

### 1) Backend (Spring Boot)
cd backend
$env:GEMINI_API_KEY="YOUR_GEMINI_KEY"


# Build & run with Maven Wrapper
# Windows:
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
# macOS/Linux:
./mvnw clean compile
./mvnw spring-boot:run

### 2) Frontend (React + Vite)
cd frontend
npm i

# Create .env with the API base
echo VITE_API_BASE=http://localhost:8081/api/v1 > .env
npm run dev

##🔌 Configuration
backend/src/main/resources/application.yml
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
    apiKey: ${GEMINI_API_KEY:}    # env var; blank falls back to local message
    model: gemini-2.0-flash       # or gemini-2.0-pro

##🧠 AI Summary (How it Works)

Backend computes per-fruit stats from the selected range:

first, last, change % (signed), min/max with dates, avg, volatility score

The prompt asks Gemini to write:

Headline, Key Highlights, KPIs (plain “change %”), Pattern, Action, Risk/Watch

Trend strength is tagged with emojis in the context lines:

🔥 (≥ +10%), 👍 (≥ +3%), 😐 (−3% to +3%), ⚠️ (≥ −10%), 🚨 (≤ −10%)

If the API call fails or key is missing, a friendly fallback string is returned (no HTTP 500).

##🧪 Demo Data

Seeded on startup (last ~90 days)

Baseline around 100/day per fruit

Small weekly ripple + random drift to make trends obvious

Table: STOCK_ENTRY (id, date, fruit, quantity)

-------
📜 License
MIT License — feel free to use, modify, and build awesome stuff.

👨‍⚕️ Author
Darko Six_Nimesh Tharaka
AI Engineer | Data Science | Exploring AI, Machine Learning, and Big Data
📧 Gmail-bandaranayakanimesh@gmail.com
🌐 Portfolio-
🔗 LinkedIn-www.linkedin.com/in/nimesh-bandaranayake-0a2912304

