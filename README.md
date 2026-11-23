# Small Business Dashboard

A desktop dashboard for tracking sales, expenses, profit, and recent business activity.

The application is built with Java and JavaFX and stores records in MySQL when it is available. It automatically falls back to an embedded H2 database, so it can still be used when MySQL is not running.

## Features

- View total sales, total expenses, net profit, and transaction counts
- Display monthly sales, expenses, and profit charts
- View expenses grouped by category in a pie chart
- Filter dashboard data by a date range
- Add, edit, delete, and search sales
- Add, edit, delete, and search expenses
- Automatically calculate sale totals from quantity and unit price
- Export dashboard summaries, sales, and expenses to timestamped CSV files
- Use sample sales and expense records when a new database is empty

## Technologies

- Java 17
- JavaFX 21
- Maven
- FXML and CSS
- MySQL 8 with JDBC
- H2 embedded database fallback

## Project Structure

```text
Small-Buisness-Dashboard/
├── data/                         # Embedded H2 database files
├── src/main/java/
│   ├── module-info.java
│   └── com/business/dashboard/smallbuisness/
│       ├── Main.java             # Application entry point
│       ├── DashboardController   # Dashboard UI and charts
│       ├── SalesController       # Sales management
│       ├── ExpensesController    # Expense management
│       ├── *DAO.java             # Database operations
│       ├── DatabaseConnection    # MySQL/H2 initialization
│       ├── ExportUtil            # CSV export support
│       └── ValidationUtil        # Form validation
└── src/main/resources/
    └── com/business/dashboard/smallbuisness/
        ├── dashboard.fxml
        ├── Sale.fxml
        ├── Expense.fxml
        └── style.css
```

## Prerequisites

- JDK 17 or newer
- Maven 3.8 or newer, or the included Maven wrapper
- MySQL 8.x (optional)
- JavaFX-compatible desktop environment

## Database Configuration

The application first tries to connect to MySQL using:

- Host: `localhost`
- Port: `3306`
- Database: `business_dashboard_db`
- Default username: `root`
- Default password: `root`

Update the constants in `DatabaseConnection.java` if your MySQL credentials are different:

```java
private static final String USERNAME = "root";
private static final String PASSWORD = "root";
```

Do not commit real production credentials to source control.

The application automatically creates the `business_dashboard_db` database and `sales` and `expenses` tables when needed. If MySQL is unavailable, it uses an embedded H2 database stored in `data/business_dashboard_db`.

When the database tables are empty, the application inserts a small set of sample records.

## Setup and Run

### Windows

From the project directory, compile the project:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Start the JavaFX application:

```powershell
.\mvnw.cmd javafx:run
```

### macOS or Linux

```bash
./mvnw -q -DskipTests compile
./mvnw javafx:run
```

## CSV Exports

The app creates CSV reports in the project directory:

- `Sales_Report_yyyyMMdd_HHmmss.csv`
- `Expenses_Report_yyyyMMdd_HHmmss.csv`
- `Dashboard_Summary_yyyyMMdd_HHmmss.csv`

## Usage

1. Launch the application.
2. Review totals and charts on the dashboard.
3. Use the date pickers to filter sales and expenses.
4. Select **Manage Sales** or **Manage Expenses** to open the transaction screens.
5. Search, add, update, or delete records and export the current data to CSV.
