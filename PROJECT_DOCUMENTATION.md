# Reminder Dashboard Application - Technical Documentation
## A Comprehensive Guide for Presentation

---

## PART 1: DATABASE & SINGLETON CLASS

### 1.1 Overview
The Reminder Dashboard uses a **MySQL relational database** connected via a **Singleton pattern** from the frontend, and **Spring Boot JPA** from the backend. This ensures efficient, centralized database access without creating multiple connections.

### 1.2 Database Architecture

#### Tables Structure:
```
┌─────────────┐
│   users     │
├─────────────┤
│ id (PK)     │
│ username    │
│ email       │
│ password    │
│ full_name   │
│ created_at  │
│ updated_at  │
└─────────────┘
       ↓ (One-to-Many)
┌─────────────────┐
│   reminders     │
├─────────────────┤
│ id (PK)         │
│ user_id (FK)    │ ← Links to users.id
│ title           │
│ description     │
│ due_date        │
│ completed       │
│ created_at      │
│ updated_at      │
└─────────────────┘
```

#### Key Relationships:
- **1 User → Many Reminders** (One-to-Many)
- **Foreign Key Constraint**: `user_id` in reminders references `users(id)`
- **ON DELETE CASCADE**: When a user is deleted, their reminders are automatically deleted

### 1.3 Singleton Pattern - Frontend Database Connection

#### What is a Singleton?
A Singleton is a design pattern that **ensures only one instance of a class exists** throughout the application's lifetime. This prevents multiple database connections from being created unnecessarily.

#### Frontend Implementation: UserDAO Class

**Location**: `reminder-frontend/src/main/java/com/reminder/frontend/util/UserDAO.java`

```java
public class UserDAO {
    private static UserDAO instance;  // Single instance
    private Connection connection;
    
    // Private constructor - cannot be instantiated from outside
    private UserDAO() {
        this.connection = getConnection();
    }
    
    // Synchronized method to ensure thread safety
    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }
    
    // Get database connection
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/reminder_db",
            "root",
            ""
        );
    }
}
```

**Why Singleton Here?**
- ✅ Only ONE database connection for the entire desktop application
- ✅ Prevents connection leaks and resource exhaustion
- ✅ Thread-safe - synchronized access across multiple UI threads
- ✅ Centralized connection management

#### Usage in Frontend:
```java
// Always use getInstance() - never new UserDAO()
UserDAO userDAO = UserDAO.getInstance();
User user = userDAO.authenticateUser(username, password);
```

### 1.4 MySQL Connection Details

#### Connection String:
```
jdbc:mysql://localhost:3306/reminder_db
├─ localhost:3306  → MySQL server on local machine, port 3306
└─ reminder_db     → Database name
```

#### Credentials (Frontend):
- **Username**: root
- **Password**: (empty)

#### Connection Pool (Backend - Spring Boot):
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/reminder_db
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection pooling settings
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
```

**HikariCP**: Spring Boot uses HikariCP to manage a **connection pool**
- Maintains 2-10 database connections ready to use
- Reuses connections instead of creating new ones
- Drastically improves performance compared to Singleton pattern

### 1.5 Data Initialization

#### SQL Schema Setup

**File**: `init_users_table.sql`

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATETIME NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Sample test user
INSERT INTO users (username, email, password, full_name) 
VALUES ('testuser', 'test@example.com', 'password123', 'Test User')
ON DUPLICATE KEY UPDATE username = username;
```

#### How to Initialize:
```bash
mysql -u root < init_users_table.sql
# OR in PowerShell:
Get-Content init_users_table.sql | mysql -u root
```

### 1.6 Data Persistence Flow

#### When User Creates a Reminder:

```
Frontend (JavaFX)
      ↓
  ReminderClient (HTTP)
      ↓
   POST /api/reminders?userId=1
      ↓
Backend (Spring Boot)
      ↓
ReminderController receives request
      ↓
Fetches User(id=1) from database
      ↓
Sets reminder.setUser(user)
      ↓
ReminderRepository.save(reminder)
      ↓
Hibernate ORM converts to SQL:
   INSERT INTO reminders (user_id, title, description, due_date, ...)
   VALUES (1, 'Buy milk', '...', '2026-05-18 15:30:00', ...)
      ↓
MySQL Database
```

### 1.7 User Isolation & Data Security

#### How Each User Only Sees Their Reminders:

```java
// Backend Query
@GetMapping
public List<Reminder> getAllReminders(@RequestParam Long userId) {
    return reminderRepository.findByUserIdOrderByDueDateAsc(userId);
    // Only retrieves reminders WHERE user_id = userId
}
```

#### SQL Generated:
```sql
SELECT * FROM reminders 
WHERE user_id = 1 
ORDER BY due_date ASC;
```

**Result**: User 1 sees only their reminders, User 2 sees only their reminders. **Complete isolation!**

---

## PART 2: CODE ARCHITECTURE & STRUCTURE

### 2.1 Project Structure

```
reminder-dashboard/
├── reminder-backend/           # Spring Boot REST API
│   ├── src/main/java/
│   │   └── com/reminder/backend/
│   │       ├── model/
│   │       │   ├── User.java
│   │       │   └── Reminder.java
│   │       ├── repository/
│   │       │   ├── UserRepository.java
│   │       │   └── ReminderRepository.java
│   │       └── controller/
│   │           └── ReminderController.java
│   └── pom.xml
│
├── reminder-frontend/          # JavaFX Desktop App
│   ├── src/main/java/
│   │   └── com/reminder/frontend/
│   │       ├── Main.java
│   │       ├── model/
│   │       │   ├── User.java
│   │       │   └── Reminder.java
│   │       ├── controller/
│   │       │   ├── LoginController.java
│   │       │   └── DashboardController.java
│   │       ├── client/
│   │       │   └── ReminderClient.java
│   │       ├── scheduler/
│   │       │   └── ReminderScheduler.java
│   │       └── util/
│   │           ├── UserDAO.java
│   │           └── LocalDateTimeAdapter.java
│   ├── src/main/resources/
│   │   └── fxml/
│   │       ├── login.fxml
│   │       └── dashboard.fxml
│   └── pom.xml
│
└── pom.xml (parent)
```

### 2.2 Backend Architecture (Spring Boot)

#### Layer Pattern: Model → Repository → Controller

```
Request (HTTP POST)
       ↓
ReminderController
  ├─ Receives @RequestBody Reminder
  ├─ Fetches User from UserRepository
  └─ Passes to ReminderRepository
       ↓
ReminderRepository (JPA)
  ├─ Converts Java object to SQL
  ├─ Executes INSERT/UPDATE/DELETE
  └─ Returns Result
       ↓
MySQL Database
```

#### 2.2.1 Model Layer - JPA Entities

**User.java**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore  // Prevents infinite JSON serialization
    private List<Reminder> reminders;
}
```

**Reminder.java**:
```java
@Entity
@Table(name = "reminders")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Links to User
    
    private String title;
    private LocalDateTime dueDate;
    private boolean completed;
}
```

**Key Annotations**:
- `@Entity`: Maps Java class to database table
- `@ManyToOne`: Many reminders belong to one user
- `@JoinColumn`: Specifies foreign key column name
- `@JsonIgnore`: Prevents circular reference in JSON

#### 2.2.2 Repository Layer - Data Access

```java
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserIdOrderByDueDateAsc(Long userId);
    List<Reminder> findByUserIdAndCompleted(Long userId, boolean completed);
}
```

**How it Works**:
- Spring Data JPA automatically implements these methods
- Method names are **queries**: `findByUserIdOrderByDueDateAsc`
- Generates SQL: `SELECT * FROM reminders WHERE user_id = ? ORDER BY due_date ASC`

#### 2.2.3 Controller Layer - API Endpoints

```java
@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "*")
public class ReminderController {
    
    @GetMapping
    public List<Reminder> getAllReminders(@RequestParam Long userId) {
        return reminderRepository.findByUserIdOrderByDueDateAsc(userId);
    }
    
    @PostMapping
    public ResponseEntity<Reminder> createReminder(
        @RequestBody Reminder reminder,
        @RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        reminder.setUser(user);
        return ResponseEntity.ok(reminderRepository.save(reminder));
    }
}
```

**REST Endpoints**:
| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/api/reminders?userId=1` | Get all reminders for user 1 |
| POST | `/api/reminders?userId=1` | Create reminder for user 1 |
| PUT | `/api/reminders/{id}` | Update reminder by ID |
| DELETE | `/api/reminders/{id}` | Delete reminder by ID |

### 2.3 Frontend Architecture (JavaFX)

#### Layer Pattern: View (FXML) → Controller → Client

```
User clicks button
       ↓
LoginController / DashboardController
       ↓
ReminderClient (HTTP calls)
       ↓
REST API (Backend)
       ↓
Response (JSON)
       ↓
Update UI (TableView, Labels)
```

#### 2.3.1 Entry Point - Main.java

```java
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        BorderPane root = loader.load();
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Reminder Dashboard - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
```

**Flow**:
1. Loads `login.fxml` file
2. Creates Scene with dimensions 800x600
3. Displays login window

#### 2.3.2 View Layer - FXML Files

**login.fxml** - Login & Registration UI:
```xml
<BorderPane>
    <top> <!-- Header with logo/title -->
    <center>
        <StackPane>
            <!-- Login Form (visible by default) -->
            <VBox fx:id="loginForm">
                <TextField fx:id="usernameField"/>
                <PasswordField fx:id="passwordField"/>
                <Button fx:id="loginButton" text="Login"/>
            </VBox>
            
            <!-- Register Form (hidden initially) -->
            <VBox fx:id="registerForm">
                <TextField fx:id="regUsernameField"/>
                <TextField fx:id="regFullNameField"/>
                <Button fx:id="submitRegisterButton" text="Register"/>
            </VBox>
        </StackPane>
    </center>
</BorderPane>
```

**dashboard.fxml** - Task List UI:
```xml
<BorderPane>
    <top>
        <!-- Title "My Reminder Dashboard" + Logout Button -->
    </top>
    <center>
        <!-- TableView with reminders -->
        <TableView fx:id="reminderTable">
            <columns>
                <TableColumn text="Title"/>
                <TableColumn text="Due Date"/>
                <TableColumn text="Status"/>
                <TableColumn text="Actions"/>
            </columns>
        </TableView>
    </center>
    <bottom>
        <!-- Form to create new reminder -->
    </bottom>
</BorderPane>
```

#### 2.3.3 Controller Layer - Business Logic

**LoginController.java**:
```java
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    
    private UserDAO userDAO;
    
    @FXML
    public void initialize() {
        userDAO = UserDAO.getInstance();  // Singleton!
        loginButton.setOnAction(event -> handleLogin());
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Authenticate against Singleton database
        User user = userDAO.authenticateUser(username, password);
        
        if (user != null) {
            // Load dashboard and pass user
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            BorderPane root = loader.load();
            
            DashboardController controller = loader.getController();
            controller.setCurrentUser(user);
            
            // Switch scene
            Stage window = (Stage) loginButton.getScene().getWindow();
            window.setScene(new Scene(root, 1200, 700));
        }
    }
}
```

**DashboardController.java**:
```java
public class DashboardController {
    @FXML private TableView<Reminder> reminderTable;
    @FXML private TextField titleInput;
    @FXML private Button addButton;
    @FXML private Button logoutButton;
    
    private ReminderClient reminderClient;
    private ReminderScheduler scheduler;
    private User currentUser;
    
    @FXML
    public void initialize() {
        reminderClient = new ReminderClient();
        addButton.setOnAction(event -> handleAddReminder());
        logoutButton.setOnAction(event -> handleLogout());
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadReminders();
        scheduler = new ReminderScheduler(reminderClient, user.getId());
        scheduler.start();
    }
    
    private void loadReminders() {
        // Async task - keeps UI responsive
        Task<List<Reminder>> task = new Task<List<Reminder>>() {
            @Override
            protected List<Reminder> call() throws Exception {
                return reminderClient.fetchReminders(currentUser.getId());
            }
        };
        
        task.setOnSucceeded(event -> {
            reminderTable.getItems().setAll(task.getValue());
        });
        
        new Thread(task).start();
    }
    
    private void handleAddReminder() {
        Reminder reminder = new Reminder(titleInput.getText(), "...", LocalDateTime.now());
        
        Task<Reminder> task = new Task<Reminder>() {
            @Override
            protected Reminder call() throws Exception {
                return reminderClient.createReminder(reminder, currentUser.getId());
            }
        };
        
        task.setOnSucceeded(event -> loadReminders());
        new Thread(task).start();
    }
}
```

**Key Concepts**:
- `@FXML`: Injects UI elements from FXML file
- `Task<T>`: Runs long operations on background thread (keeps UI responsive)
- `setOnSucceeded()`: Callback when background task completes

#### 2.3.4 Client Layer - HTTP Communication

**ReminderClient.java**:
```java
public class ReminderClient {
    private static final String BASE_URL = "http://localhost:8080/api/reminders";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    public List<Reminder> fetchReminders(Long userId) throws Exception {
        String url = BASE_URL + "?userId=" + userId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        // JSON → Java object
        return gson.fromJson(response.body(), new TypeToken<List<Reminder>>(){}.getType());
    }
    
    public Reminder createReminder(Reminder reminder, Long userId) throws Exception {
        String url = BASE_URL + "?userId=" + userId;
        String json = gson.toJson(reminder);  // Java object → JSON
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        return gson.fromJson(response.body(), Reminder.class);
    }
}
```

**How HTTP Works**:
1. **Serialization**: Java object → JSON (via Gson)
2. **HTTP Request**: Send JSON to backend
3. **Backend Processing**: Backend deserializes, saves to DB
4. **HTTP Response**: Backend sends back JSON
5. **Deserialization**: JSON → Java object

#### 2.3.5 Scheduler Layer - Background Tasks

**ReminderScheduler.java**:
```java
public class ReminderScheduler {
    private ScheduledExecutorService scheduler;
    private ReminderClient reminderClient;
    private Long userId;
    private static final long CHECK_INTERVAL_SECONDS = 30;
    
    public ReminderScheduler(ReminderClient client, Long userId) {
        this.reminderClient = client;
        this.userId = userId;
        this.scheduler = new ScheduledThreadPoolExecutor(1);
    }
    
    public void start() {
        scheduler.scheduleAtFixedRate(
            this::checkReminders,  // What to run
            0,                      // Initial delay
            CHECK_INTERVAL_SECONDS, // Interval
            TimeUnit.SECONDS
        );
    }
    
    private void checkReminders() {
        try {
            List<Reminder> reminders = reminderClient.fetchReminders(userId);
            LocalDateTime now = LocalDateTime.now();
            
            for (Reminder reminder : reminders) {
                if (!reminder.isCompleted() && isTimeToRemind(reminder)) {
                    showAlert(reminder);
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking reminders: " + e.getMessage());
        }
    }
    
    private void showAlert(Reminder reminder) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reminder Alert");
            alert.setHeaderText("Time for: " + reminder.getTitle());
            alert.showAndWait();
        });
    }
}
```

**How It Works**:
- Runs `checkReminders()` every 30 seconds
- Fetches reminders for current user
- Compares due_date with current time
- Shows alert when reminder is due
- `Platform.runLater()` safely updates UI from background thread

### 2.4 Complete Data Flow Example

#### Scenario: User creates "Buy milk" reminder

```
1. USER ACTION
   └─ Types "Buy milk" and clicks "Add Reminder"

2. FRONTEND - DashboardController.handleAddReminder()
   ├─ Creates Reminder object
   └─ Calls reminderClient.createReminder(reminder, userId=1)

3. FRONTEND - ReminderClient.createReminder()
   ├─ Converts Reminder to JSON:
   │  {
   │    "title": "Buy milk",
   │    "description": "Milk from grocery",
   │    "dueDate": "2026-05-18T15:30:00",
   │    "completed": false
   │  }
   └─ Sends: POST http://localhost:8080/api/reminders?userId=1

4. BACKEND - ReminderController.createReminder()
   ├─ Receives JSON + userId=1
   ├─ Calls userRepository.findById(1)
   │  └─ SQL: SELECT * FROM users WHERE id = 1
   │  └─ Returns: User {id: 1, username: "testuser", ...}
   ├─ Calls reminder.setUser(user)
   └─ Calls reminderRepository.save(reminder)

5. BACKEND - Hibernate ORM
   ├─ Converts Reminder to SQL:
   │  INSERT INTO reminders 
   │  (user_id, title, description, due_date, completed, created_at, updated_at)
   │  VALUES (1, 'Buy milk', 'Milk from grocery', '2026-05-18 15:30:00', false, NOW(), NOW())
   └─ Executes query

6. DATABASE - MySQL
   └─ Stores reminder in reminders table with user_id = 1

7. BACKEND - Response
   ├─ Returns saved Reminder as JSON:
   │  {
   │    "id": 42,
   │    "title": "Buy milk",
   │    "completed": false,
   │    ...
   │  }
   └─ Sends to frontend

8. FRONTEND - Update UI
   ├─ Calls loadReminders() to refresh
   ├─ Backend returns: GET /api/reminders?userId=1
   │  └─ SQL: SELECT * FROM reminders WHERE user_id = 1 ORDER BY due_date
   │  └─ Results now include new "Buy milk" reminder
   └─ Updates TableView - "Buy milk" appears on screen!

9. FRONTEND - Background Scheduler
   ├─ Every 30 seconds, checks reminderClient.fetchReminders(1)
   ├─ Compares each reminder.dueDate with LocalDateTime.now()
   └─ When due time arrives: Shows alert popup
```

### 2.5 Key Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Singleton** | UserDAO (Frontend) | One database connection for entire app |
| **MVC** | Controllers + FXML | Separation of UI and business logic |
| **Repository** | ReminderRepository | Abstract database operations |
| **DAO** | UserDAO | Data Access Object pattern |
| **Client-Server** | ReminderClient + Backend | HTTP-based communication |
| **Task** | Task<T> | Async operations on background thread |
| **Observer** | setOnSucceeded() | Event-driven programming |

### 2.6 Security Considerations

#### Current Implementation:
```
✓ Per-user data isolation (can't see other users' reminders)
✓ Unique usernames and emails
✓ Password stored in database (WARNING: Not encrypted in this demo)

⚠️ Missing in Production:
- Password hashing (use BCrypt)
- JWT/OAuth authentication
- HTTPS instead of HTTP
- Input validation & SQL injection prevention
- CORS restrictions (currently "*")
```

#### Production Recommendations:
```java
// USE: Spring Security + BCrypt
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// USE: JWT Tokens instead of Singleton
@PostMapping("/login")
public ResponseEntity<String> login(@RequestBody LoginRequest req) {
    // Validate credentials
    // Return JWT token
    // Client includes token in Authorization header
}
```

---

## PART 3: DEPLOYMENT & RUNNING

### 3.1 Prerequisites
- Java 17+
- MySQL 8.0+
- Maven (or use mvnw wrapper)

### 3.2 Setup Steps

```bash
# 1. Initialize database
mysql -u root < init_users_table.sql

# 2. Start backend (Terminal 1)
cd reminder-backend
mvnw spring-boot:run

# 3. Start frontend (Terminal 2)
cd reminder-frontend
mvnw javafx:run
```

### 3.3 Test Flow

```
1. Login: testuser / password123
2. Create reminder: "Buy milk" at 3:00 PM tomorrow
3. Create second reminder: "Meeting" at 2:00 PM
4. Logout
5. Relogin as testuser
   ✓ Both reminders still visible
6. Logout, create new user: user2
7. Login as user2
   ✓ No reminders visible (fresh user)
8. Create reminder: "Study"
9. Logout, relogin as testuser
   ✓ Only original reminders visible, not "Study"
```

---

## SUMMARY

### Frontend Stack:
- **JavaFX**: Desktop UI framework
- **FXML**: XML-based UI markup
- **Gson**: JSON serialization
- **HttpClient**: REST communication
- **Task**: Async operations

### Backend Stack:
- **Spring Boot**: REST API framework
- **Spring Data JPA**: ORM abstraction
- **Hibernate**: ORM implementation
- **MySQL**: Relational database

### Key Achievement:
✅ **Fully functional multi-user reminder system** with persistent database storage, real-time background scheduling, and complete user data isolation.
