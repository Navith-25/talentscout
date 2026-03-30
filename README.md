# 🚀 TalentScout AI - Intelligent Candidate Prioritization System

TalentScout AI is a modern, data-driven recruitment backend built to streamline and optimize the hiring pipeline. By leveraging advanced data structures and intelligent scoring algorithms, it automatically evaluates, ranks, and schedules candidates, ensuring that recruitment teams focus on the best talent without ignoring potential outliers.

## ✨ Key Features

* **🧠 Intelligent Scoring Engine:** Automatically parses candidate profiles and calculates a weighted score based on skills, experience, and educational background.
* **⚡ Priority-Based Ranking (Max-Heap):** Utilizes a custom `CandidateMaxHeap` data structure to ensure that the highest-scoring candidates are always surfaced first, guaranteeing $O(1)$ time complexity for retrieving the top candidate.
* **⚖️ Anti-Starvation Scheduler:** Implements a specialized background service (`AntiStarvationScheduler`) to periodically boost the priority of older, unreviewed applications, ensuring fair processing and preventing lower-scored candidates from remaining in the queue indefinitely.
* **🙈 Blind Hiring Mode:** Promotes Diversity, Equity, and Inclusion (DEI) by generating `BlindCandidateDTO` profiles. This strips Personally Identifiable Information (PII) such as names and contact details during the initial screening phase to eliminate unconscious bias.
* **📄 Automated CV Parsing:** Extracts key metrics and keywords from candidate submissions using the `CVParserService` to feed directly into the scoring engine.

## 🛠️ Architecture & Tech Stack

**Backend**
* **Framework:** Java / Spring Boot 3.x
* **Core Algorithms:** Custom Max-Heap implementation, Priority Scheduling
* **Build Tool:** Maven

**Frontend**
* **Technologies:** HTML5, CSS3, Vanilla JavaScript (Seamlessly integrates with the Spring Boot REST API)

## 🚦 Getting Started

### Prerequisites
* Java Development Kit (JDK) 17 or higher
* Maven (or use the included `mvnw` wrapper)

### Installation & Execution

1. **Clone the repository:**
   ```bash
   git clone <your-repository-url>
   cd talentscout
