package com.business.dashboard.smallbuisness;



import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;

public class SaleDAO {

    public static Sale create(Sale sale) throws SQLException {
        String sql = """
            INSERT INTO sales (product_name, quantity, unit_price, total_amount, 
                              sale_date, customer_name) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, sale.getProductName());
            pstmt.setInt(2, sale.getQuantity());
            pstmt.setDouble(3, sale.getUnitPrice());
            pstmt.setDouble(4, sale.getTotalAmount());
            pstmt.setDate(5, Date.valueOf(sale.getSaleDate()));
            pstmt.setString(6, sale.getCustomerName());

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                sale.setId(rs.getInt(1));
            }
            return sale;
        }
    }

    public static ObservableList<Sale> getAll() throws SQLException {
        ObservableList<Sale> sales = FXCollections.observableArrayList();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sales.add(extractFromResultSet(rs));
            }
        }
        return sales;
    }

    public static boolean update(Sale sale) throws SQLException {
        String sql = """
            UPDATE sales SET product_name = ?, quantity = ?, unit_price = ?, 
                           total_amount = ?, sale_date = ?, customer_name = ? 
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sale.getProductName());
            pstmt.setInt(2, sale.getQuantity());
            pstmt.setDouble(3, sale.getUnitPrice());
            pstmt.setDouble(4, sale.getTotalAmount());
            pstmt.setDate(5, Date.valueOf(sale.getSaleDate()));
            pstmt.setString(6, sale.getCustomerName());
            pstmt.setInt(7, sale.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM sales WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static ObservableList<Sale> search(String keyword) throws SQLException {
        ObservableList<Sale> sales = FXCollections.observableArrayList();
        String sql = """
            SELECT * FROM sales 
            WHERE product_name LIKE ? OR customer_name LIKE ? 
            ORDER BY sale_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sales.add(extractFromResultSet(rs));
            }
        }
        return sales;
    }

    public static ObservableList<Sale> getByDateRange(LocalDate start, LocalDate end) throws SQLException {
        ObservableList<Sale> sales = FXCollections.observableArrayList();
        String sql = "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sales.add(extractFromResultSet(rs));
            }
        }
        return sales;
    }

    private static Sale extractFromResultSet(ResultSet rs) throws SQLException {
        return new Sale(
                rs.getInt("id"),
                rs.getString("product_name"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price"),
                rs.getDouble("total_amount"),
                rs.getDate("sale_date").toLocalDate(),
                rs.getString("customer_name")
        );
    }
}