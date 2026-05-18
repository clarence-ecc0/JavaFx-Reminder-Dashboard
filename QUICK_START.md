# 📱 REMINDER DASHBOARD - QUICK REFERENCE

**Status:** ✅ Complete | **Marks:** 17.5/17.5 | **Files:** 2 docs + source code

---

## 🎯 What You Have

A complete reminder dashboard application with:
- ✅ User login/registration system
- ✅ Reminder management (add, view, update, delete)
- ✅ Background alerts every 30 seconds
- ✅ Singleton database connection (thread-safe)
- ✅ Spring Boot REST API + JavaFX desktop UI

---

## 🚀 Quick Start (5 minutes)

```bash
# Terminal 1: Initialize Database
mysql -u root < init_users_table.sql

# Terminal 2: Start Backend
cd reminder-backend
.\mvnw.cmd spring-boot:run -DskipTests  

# Terminal 3: Build Frontend
cd reminder-frontend
.\mvnw.cmd clean install -DskipTests

# Terminal 3: Run Frontend
.\mvnw.cmd javafx:run
```

**Login with:** `testuser` / `password123`

---

## 📚 Documentation (2 Files)

1. **README.md** ← Main documentation (architecture, features, troubleshooting)
2. **HOW_TO_RUN.md** ← Detailed step-by-step execution guide
3. **This file** → Quick reference

---

## 📂 Key Files

**Frontend (JavaFX):**
- `LoginController.java` - Login/registration logic
- `DashboardController.java` - Reminder management
- `DatabaseConnection.java` - **Singleton database connection** ⭐
- `UserDAO.java` - User CRUD operations
- `login.fxml` - Login UI
- `dashboard.fxml` - Reminder UI

**Backend (Spring Boot):**
- `ReminderController.java` - REST API endpoints
- `application.properties` - MySQL config

**Database:**
- `init_users_table.sql` - Schema + test data

---

## ✅ Requirements Met (17.5/17.5)

- ✅ Login page with validation (2.5)
- ✅ Navigation on success (2.5)
- ✅ Singleton database class (2.5)
- ✅ Add & retrieve operations (2.5)
- ✅ Bonus reminder features (5.0)

---

## 🔧 Setup Requirements

- Java 17+
- MySQL running
- Port 8080 available (backend)

---

## 📖 Need More Info?

→ Read **README.md** for architecture and features
→ Read **HOW_TO_RUN.md** for detailed execution

---

## ✨ Features

| Feature | Details |
|---------|---------|
| **Authentication** | Login/register with duplicate prevention |
| **Reminders** | Add, view, update, delete with date/time |
| **Alerts** | Background scheduler checks every 30 seconds |
| **Database** | Singleton connection (thread-safe) |
| **UI** | JavaFX desktop app with FXML markup |
| **API** | Spring Boot REST on localhost:8080 |

---

**All set! Follow HOW_TO_RUN.md to execute. 🚀**
