# Reminder Dashboard Application

A full-stack reminder management system built with **Spring Boot** (backend) and **JavaFX** (desktop frontend), featuring real-time reminder alerts and MySQL database persistence.

## Project Structure

```
.
├── reminder-backend/          # Spring Boot REST API
│   ├── src/main/java/
│   │   └── com/reminder/backend/
│   │       ├── controller/    # REST endpoints
│   │       ├── model/         # JPA entities
│   │       ├── repository/    # Data access layer
│   │       └── ReminderBackendApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── pom.xml
│   ├── mvnw                   # Maven wrapper (Unix/Mac)
│   └── mvnw.cmd               # Maven wrapper (Windows)
│
├── reminder-frontend/         # JavaFX desktop app
│   ├── src/main/java/
│   │   └── com/reminder/frontend/
│   │       ├── controller/    # UI controllers
│   │       ├── model/         # Data models
│   │       ├── client/        # HTTP client
│   │       ├── scheduler/     # Background reminder checker
│   │       ├── util/          # Utilities (JSON adapters)
│   │       └── Main.java
│   ├── src/main/resources/
│   │   └── fxml/              # UI layout files
│   ├── pom.xml
│   ├── mvnw                   # Maven wrapper (Unix/Mac)
│   └── mvnw.cmd               # Maven wrapper (Windows)
│
└── README.md
```

## Prerequisites

Before running the application, ensure you have:

1. **Java 17+** installed
   - Verify: `java -version`

2. **MySQL Server** running
   - Verify: `mysql --version`
   - Default: localhost:3306, user: `root`, no password
   - Create database: `CREATE DATABASE reminder_db;` (optional—Hibernate will create it)

3. **Git** (optional, for cloning)

> **Note:** Maven is NOT required—both projects include Maven Wrapper (`mvnw.cmd` for Windows, `mvnw` for Unix/Mac)

## Quick Start

### Step 1: Start the Backend (Spring Boot API)

Navigate to the backend directory:

```bash
cd reminder-backend
```

**On Windows:**
```cmd
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run 


.\mvnw.cmd clean install -DskipTests  
.\mvnw.cmd spring-boot:run -DskipTests  
```


✅ Backend should be running on **http://localhost:8080**

Verify it's working:
```bash
curl http://localhost:8080/api/reminders
# Should return: []
```

### Step 2: Start the Frontend (JavaFX Desktop App)

In a **new terminal**, navigate to the frontend directory:

```bash
cd reminder-frontend
```

**On Windows:**
```cmd
.\mvnw.cmd clean install
.\mvnw.cmd javafx:run   
```

```

✅ JavaFX window should open with the Reminder Dashboard

## Features

### Dashboard UI
- **Reminders Table**: Displays all reminders with ID, title, description, due date, and status
- **Add Reminder Form**:
  - Title (required)
  - Description (optional)
  - Due Date (required, date picker)
  - Time Picker (hours: 0-23, minutes: 0-59 in 5-minute increments)
  - Add button
- **Action Buttons**:
  - **Toggle**: Mark reminder as complete/incomplete
  - **Delete**: Remove reminder from database

### Background Scheduler
- Runs every 30 seconds
- Checks for due reminders
- Shows **Alert popup** when a reminder is due
- Displays reminder title in the alert

### Database Persistence
- All reminders are saved to MySQL
- Data persists across app restarts
- Auto-create tables on first run

## Usage

### Adding a Reminder
1. Enter reminder **title** (e.g., "Team Meeting")
2. Enter **description** (optional, e.g., "Discuss Q2 roadmap")
3. Select **due date** using the date picker
4. Set **time** using hour and minute spinners
5. Click **"Add Reminder"** button
6. Confirmation alert appears when created

### Marking Complete
1. Click the **Toggle** button next to any reminder
2. Status changes in database
3. List automatically refreshes

### Deleting a Reminder
1. Click the **Delete** button next to any reminder
2. Confirm in the dialog
3. Reminder is removed from database and list

### Testing Alerts
1. Add a reminder with current date/time + 1 minute
2. Wait up to 30 seconds
3. Alert popup should appear when due time is reached

## API Endpoints (Backend)

All endpoints return JSON and are accessible at `http://localhost:8080/api/reminders`:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reminders` | Get all reminders |
| GET | `/api/reminders/{id}` | Get reminder by ID |
| POST | `/api/reminders` | Create new reminder |
| PUT | `/api/reminders/{id}` | Update reminder |
| DELETE | `/api/reminders/{id}` | Delete reminder |

### Example: Create Reminder via cURL
```bash
curl -X POST http://localhost:8080/api/reminders \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Doctor Appointment",
    "description": "Annual checkup",
    "dueDate": "2026-05-20T14:30:00",
    "completed": false
  }'
```

## Database Schema

### Reminders Table
```sql
CREATE TABLE reminders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  due_date DATETIME NOT NULL,
  completed BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

> This table is **automatically created** by Hibernate on first run.

## Troubleshooting

### Issue: Backend won't start
**Error:** `Connection refused` or `No suitable driver`

**Solution:**
1. Verify MySQL is running: `mysql -u root -p`
2. Ensure database exists: `CREATE DATABASE reminder_db;`
3. Check `application.properties` has correct credentials
4. Try restarting MySQL service

---

### Issue: Frontend won't start - "Spinner is not a valid type"
**Error:** `javafx.fxml.LoadException: Spinner is not a valid type`

**Solution:**
- Ensure `dashboard.fxml` has the import: `<?import javafx.scene.control.Spinner?>`
- Run `mvnw.cmd clean install` before `mvnw.cmd javafx:run`

---

### Issue: "Could not connect to backend" in frontend
**Error:** Alert says connection failed

**Solution:**
1. Verify backend is running on port 8080: `curl http://localhost:8080/api/reminders`
2. Check firewall isn't blocking port 8080
3. Restart backend: `mvnw.cmd spring-boot:run`

---

### Issue: Maven command not found
**Error:** `'mvnw' is not recognized` (Windows) or `command not found` (Mac/Linux)

**Solution:**
- On Windows: Use `mvnw.cmd` instead of `mvnw`
- Ensure you're in the correct project directory (`reminder-backend` or `reminder-frontend`)

---

### Issue: Port 8080 already in use
**Error:** `Address already in use`

**Solution:**
1. Find process using port 8080:
   - Windows: `netstat -ano | findstr :8080`
   - Mac/Linux: `lsof -i :8080`
2. Kill the process (get PID from above)
   - Windows: `taskkill /PID <PID> /F`
   - Mac/Linux: `kill -9 <PID>`

---

### Issue: MySQL connection with wrong encoding
**Error:** `Encoding warning: Using platform encoding`

**Solution:**
This is just a warning. To fix, add to backend's `application.properties`:
```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

## Development Notes

### Frontend Technologies
- **Framework**: JavaFX 21
- **JSON Parsing**: Gson (with custom LocalDateTime adapter)
- **HTTP Client**: Java's built-in `HttpURLConnection`
- **Threading**: `javafx.concurrent.Task` for background operations
- **UI Layout**: FXML + CSS styling

### Backend Technologies
- **Framework**: Spring Boot 3.3.0
- **Database ORM**: Hibernate/JPA
- **Database Driver**: MySQL Connector/J
- **Build Tool**: Maven

### Key Design Patterns
- **MVC**: Model-View-Controller in frontend (FXML + Controller)
- **Repository Pattern**: Data access abstraction in backend
- **Async Tasks**: UI remains responsive during network calls
- **Background Scheduler**: `ScheduledExecutorService` for polling reminders

## Future Enhancements

- [ ] Edit existing reminders
- [ ] Search/filter reminders by title
- [ ] Visual indicators for overdue reminders (red highlighting)
- [ ] Sound notifications in addition to popups
- [ ] Recurring reminders (daily, weekly, monthly)
- [ ] User authentication and multiple accounts
- [ ] Web UI dashboard (React/Vue)
- [ ] Email notifications

## License

MIT License - Free to use and modify

## Support

For issues or questions:
1. Check the **Troubleshooting** section above
2. Verify all prerequisites are installed
3. Ensure both backend and frontend are running
4. Check application logs for error details

---

**Happy reminder-taking! 📝**
