package com.business.dashboard.smallbuisness;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;

public class ExpenseDAO {

    public static Expense create(Expense expense) throws SQLException {
        String sql = """
            INSERT INTO expenses (category, description, amount, expense_date) 
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, expense.getCategory());
            pstmt.setString(2, expense.getDescription());
            pstmt.setDouble(3, expense.getAmount());
            pstmt.setDate(4, Date.valueOf(expense.getExpenseDate()));

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                expense.setId(rs.getInt(1));
            }
            return expense;
        }
    }

    public static ObservableList<Expense> getAll() throws SQLException {
        ObservableList<Expense> expenses = FXCollections.observableArrayList();
        String sql = "SELECT * FROM expenses ORDER BY expense_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                expenses.add(extractFromResultSet(rs));
            }
        }
        return expenses;
    }

    public static boolean update(Expense expense) throws SQLException {
        String sql = """
            UPDATE expenses SET category = ?, description = ?, amount = ?, 
                              expense_date = ? 
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expense.getCategory());
            pstmt.setString(2, expense.getDescription());
            pstmt.setDouble(3, expense.getAmount());
            pstmt.setDate(4, Date.valueOf(expense.getExpenseDate()));
            pstmt.setInt(5, expense.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static ObservableList<Expense> search(String keyword) throws SQLException {
        ObservableList<Expense> expenses = FXCollections.observableArrayList();
        String sql = """
            SELECT * FROM expenses 
            WHERE category LIKE ? OR description LIKE ? 
            ORDER BY expense_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                expenses.add(extractFromResultSet(rs));
            }
        }
        return expenses;
    }

    public static ObservableList<Expense> getByDateRange(LocalDate start, LocalDate end) throws SQLException {
        ObservableList<Expense> expenses = FXCollections.observableArrayList();
        String sql = "SELECT * FROM expenses WHERE expense_date BETWEEN ? AND ? ORDER BY expense_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                expenses.add(extractFromResultSet(rs));
            }
        }
        return expenses;
    }

    private static Expense extractFromResultSet(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getInt("id"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("amount"),
                rs.getDate("expense_date").toLocalDate()
        );
    }
}
