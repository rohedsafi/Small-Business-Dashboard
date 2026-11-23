package com.business.dashboard.smallbuisness;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "business_dashboard_db";
    private static final String DB_URL = BASE_URL + DB_NAME;

    // TODO: Update with your MySQL credentials
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    // H2 fallback (embedded) — used when MySQL is unavailable
    private static final String H2_URL = "jdbc:h2:./data/business_dashboard_db;AUTO_SERVER=TRUE;MODE=MySQL";
    private static boolean useH2 = false;

    public static Connection getConnection() throws SQLException {
        if (!useH2) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        } else {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException ignored) {
            }
            return DriverManager.getConnection(H2_URL, "sa", "");
        }
    }

    public static void initializeDatabase() {
        // Create database
        // Try to create/check MySQL DB first; if it fails we fall back to embedded H2
        try (Connection conn = DriverManager.getConnection(BASE_URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("✓ MySQL Database created/verified: " + DB_NAME);
            // ensure subsequent calls connect to the specific DB
            useH2 = false;

        } catch (SQLException e) {
            System.err.println("✗ WARNING: Cannot connect to MySQL — falling back to embedded H2.");
            System.err.println("  Reason: " + e.getMessage());
            useH2 = true;

            // Ensure H2 data directory exists
            try {
                File dataDir = new File("data");
                if (!dataDir.exists()) dataDir.mkdirs();
                try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
                     Statement stmt = conn.createStatement()) {
                    System.out.println("✓ Using H2 embedded database at ./data/business_dashboard_db");
                }
            } catch (SQLException ex) {
                System.err.println("✗ ERROR: Failed to initialize H2 fallback database");
                ex.printStackTrace();
                return;
            }
        }

        // Create tables (on whatever DB is active)
        createTables();
    }

    private static void createTables() {
        String salesTable = """
            CREATE TABLE IF NOT EXISTS sales (
                id INT AUTO_INCREMENT PRIMARY KEY,
                product_name VARCHAR(150) NOT NULL,
                quantity INT NOT NULL,
                unit_price DECIMAL(10, 2) NOT NULL,
                total_amount DECIMAL(10, 2) NOT NULL,
                sale_date DATE NOT NULL,
                customer_name VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_sale_date (sale_date),
                INDEX idx_product (product_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

        String expensesTable = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INT AUTO_INCREMENT PRIMARY KEY,
                category VARCHAR(100) NOT NULL,
                description TEXT,
                amount DECIMAL(10, 2) NOT NULL,
                expense_date DATE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_expense_date (expense_date),
                INDEX idx_category (category)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(salesTable);
            stmt.execute(expensesTable);
            System.out.println("✓ Tables created successfully");

            // Insert sample data if empty
            insertSampleData();

        } catch (SQLException e) {
            System.err.println("✗ Error creating tables");
            e.printStackTrace();
        }
    }

    private static void insertSampleData() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if data exists
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM sales");
            rs.next();
            if (rs.getInt(1) == 0) {
                // Insert sample sales
                stmt.executeUpdate("""
                    INSERT INTO sales (product_name, quantity, unit_price, total_amount, sale_date, customer_name) VALUES
                    ('Laptop Dell XPS 15', 2, 1200.00, 2400.00, '2024-01-15', 'John Smith'),
                    ('Office Chair', 5, 150.00, 750.00, '2024-01-20', 'ABC Company'),
                    ('Wireless Mouse', 10, 25.00, 250.00, '2024-01-22', 'Tech Solutions'),
                    ('Monitor 27 inch', 3, 300.00, 900.00, '2024-02-01', 'Sarah Johnson'),
                    ('USB Hub', 15, 20.00, 300.00, '2024-02-05', 'Office Depot')
                """);

                // Insert sample expenses
                stmt.executeUpdate("""
                    INSERT INTO expenses (category, description, amount, expense_date) VALUES
                    ('Rent', 'Office rent for January', 1500.00, '2024-01-01'),
                    ('Utilities', 'Electricity bill', 200.00, '2024-01-10'),
                    ('Supplies', 'Office supplies and stationery', 150.00, '2024-01-15'),
                    ('Marketing', 'Facebook ads campaign', 300.00, '2024-01-20'),
                    ('Salaries', 'Employee salaries', 3000.00, '2024-02-01')
                """);

                System.out.println("✓ Sample data inserted");
            }

        } catch (SQLException e) {
            System.err.println("Note: Sample data may already exist");
        }
    }
}