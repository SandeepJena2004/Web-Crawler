🌐 WEB CRAWLER

A multi-threaded Java Web Crawler with a Spring Boot backend and React frontend.
This project enables configurable crawling parameters, respects domain restrictions, and displays real-time crawl progress and results in a modern web interface.

─────────────────────────────
🚀 Features

• Concurrent crawling with customizable thread count
• Domain-restricted link extraction to stay within the target website
• Configurable parameters: start URL, maximum pages, and crawl depth
• Real-time progress updates displayed dynamically on the UI
• Robust HTML parsing with Jsoup
• Spring Boot REST API backend returning JSON crawl data
• Interactive React frontend for starting crawls and visualizing results

─────────────────────────────
🧱 Tech Stack

Backend:
Java 17 
Spring Boot 3
Jsoup (HTML parsing)
Maven (build & dependency management)

Frontend:
React (functional components + hooks)
Node.js & npm

─────────────────────────────
🛠️ Getting Started

✅ Prerequisites

Make sure you have the following installed:

JDK 17

Maven 3.x

Node.js (with npm)

─────────────────────────────
📦 Installation

Clone the repository

Build the backend
mvn clean install


Setup the frontend
cd client
npm install


─────────────────────────────
▶️ Running the Application

🧩 Backend (Spring Boot)

mvn spring-boot:run


The API will be available at:
http://localhost:8080

💻 Frontend (React)

cd client
npm start


Open your browser at:
http://localhost:3000

─────────────────────────────
🧭 Usage

Enter a Start URL.

Configure:

Maximum pages

Thread count

Maximum crawl depth

Click “Start Crawl” to begin.

View real-time crawl progress and list of crawled URLs.

Crawling stops automatically when limits are reached.
