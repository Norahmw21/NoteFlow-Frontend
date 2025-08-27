# 📘 Note Flow  

## Introduction  
**Note Flow** is a desktop productivity application built for students and lifelong learners. It combines **note-taking**, **task management**, **GPA calculation**, and **AI-powered learning assistance** in one place.  

With a clean interface and smart features, it helps you stay organized, improve focus, and boost academic performance.  

**To Check Backend Code visit this repo → **[Backend](https://github.com/Norahmw21/NoteFlow-Backend)

Project demo → **[Video](https://www.canva.com/design/DAGxK9Peo_E/NWPbT8BwOSOVT2sMx37KWQ/watch?utm_content=DAGxK9Peo_E&utm_campaign=designshare&utm_medium=link2&utm_source=uniquelinks&utlId=h3287cf69b3)**

---

## 📑 Table of Contents  
- [✨ Features](#-features)  
- [🛠 Tech Stack](#-tech-stack)  
- [🏗 Architecture](#-architecture)  
- [⚙️ Installation](#️-installation)  
- [▶️ Usage](#️-usage)  
- [🔧 Configuration](#-configuration)  
- [📂 Examples](#-examples)  
- [👥 Contributors](#-contributors)  
- [📜 License](#-license)  

---

## ✨ Features  
- **🔐 User Authentication**  
  - Sign up and log in to access personalized content.  

- **📝 Notes Management**  
  - Create folders to organize notes.  
  - Add **text notes** and **drawing notes**.  

- **⭐ Favorites**  
  - Save frequently accessed notes for quick reference.  

- **📋 To-Do List**  
  - Add tasks with start/end dates.  
  - Rearrange tasks by urgency.  

- **📊 GPA Calculator**  
  - Enter grades and subjects.  
  - Supports **4.0** and **5.0** GPA scales.  

- **🤖 AI Chatbot Assistant**  
  - Powered by **Ollama**, with **Llama 3** and **Mistral** models.  
  - Provides course-related help and productivity tips.  

- **👤 User Profile**  
  - Edit and manage personal information.  

---

## 🛠 Tech Stack  
- **Backend** → [Spring Boot](https://spring.io/projects/spring-boot)  
- **Frontend** → [JavaFX](https://openjfx.io/)  
- **Database** → [PostgreSQL](https://www.postgresql.org/)  
- **AI Integration** → [Ollama](https://ollama.ai/) with Llama 3 & Mistral  

---

### ✅ Prerequisites  
- [Java 17+](https://adoptium.net/)  
- [Maven](https://maven.apache.org/)  
- [PostgreSQL](https://www.postgresql.org/download/)  
- [Ollama](https://ollama.ai/) installed & running  

### 📥 Steps  
1. **Clone the repository**  
   ```bash
   git clone https://github.com/yourusername/noteflow.git
   cd noteflow
   ```
2. **Configure Database**  
   - Create a PostgreSQL database: `noteflow_db`  
   - Add a user: `noteflow_user` with password  
   - Update credentials in `src/main/resources/application.properties`  

3. **Run Backend (Spring Boot)**  
   ```bash
   mvn spring-boot:run
   ```

4. **Run Frontend (JavaFX)**  
   - Import into your IDE (e.g., IntelliJ, Eclipse).  
   - Run the JavaFX `Main` class.  

5. **Setup AI Models (Optional, for chatbot)**  
   ```bash
   ollama pull llama3
   ollama pull mistral
   ```

---

## ▶️ Usage  
Once launched, you can:  
- **Login/Sign up** → Access your account.  
- **Navigate via Nav Bar**:  
  - `My Notes` → Create and organize notes.  
  - `Favorites` → Access frequently used notes.  
  - `To-Do List` → Manage tasks & deadlines.  
  - `GPA Calculator` → Track academic performance.  
  - `AI Chatbot` → Get study help & productivity tips.  
  - `Profile` → Update personal info.  

---

## 🔧 Configuration  

In `application.properties`:  
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/noteflow_db
spring.datasource.username=noteflow_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

For AI chatbot:  
```bash
ollama pull llama3
ollama pull mistral
```
---

## 📂 Examples  
- Create a folder **Math** and add both a **text note** and a **drawing note**.  
- Add a task like **"Finish Assignment"**, set a deadline, and reorder tasks.  
- Enter grades and calculate GPA on a **5.0 scale**.  
- Ask the chatbot: *"Summarize Newton’s laws"* or *"Suggest a study plan for exams"*.  

---


## 👥 Contributors  
- [Norah Alwabel](https://github.com/Norahmw21) -[Raghad Alhelal](https://github.com/Raghadlh) - [Rouba Alharbi](https://github.com/Rubabdran) - [Noura Altuwaim](https://github.com/tunourah)  

---

## 📜 License  
This project is licensed under the **MIT License**.  
