package com.business.dashboard.smallbuisness;

import javafx.collections.ObservableList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExportUtil {

    public static File exportSalesToCSV(ObservableList<Sale> sales) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File("Sales_Report_" + timestamp + ".csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("ID,Product Name,Quantity,Unit Price,Total Amount,Sale Date,Customer");

            for (Sale sale : sales) {
                writer.printf("%d,\"%s\",%d,%.2f,%.2f,%s,\"%s\"%n",
                        sale.getId(),
                        escape(sale.getProductName()),
                        sale.getQuantity(),
                        sale.getUnitPrice(),
                        sale.getTotalAmount(),
                        sale.getSaleDate(),
                        escape(sale.getCustomerName())
                );
            }

            double total = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
            writer.println();
            writer.printf("TOTAL SALES:,,,,,%.2f%n", total);
        }

        return file;
    }

    public static File exportExpensesToCSV(ObservableList<Expense> expenses) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File("Expenses_Report_" + timestamp + ".csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("ID,Category,Description,Amount,Expense Date");

            for (Expense expense : expenses) {
                writer.printf("%d,\"%s\",\"%s\",%.2f,%s%n",
                        expense.getId(),
                        escape(expense.getCategory()),
                        escape(expense.getDescription()),
                        expense.getAmount(),
                        expense.getExpenseDate()
                );
            }

            double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
            writer.println();
            writer.printf("TOTAL EXPENSES:,,,,%.2f%n", total);
        }

        return file;
    }

    public static File exportDashboardSummary(ObservableList<Sale> sales,
                                              ObservableList<Expense> expenses) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File("Dashboard_Summary_" + timestamp + ".csv");

        double totalSales = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double profit = totalSales - totalExpenses;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("BUSINESS DASHBOARD SUMMARY REPORT");
            writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("FINANCIAL OVERVIEW");
            writer.println("==================");
            writer.printf("Total Sales:,$%.2f%n", totalSales);
            writer.printf("Total Expenses:,$%.2f%n", totalExpenses);
            writer.printf("Net Profit:,$%.2f%n", profit);
            writer.println();
            writer.println("TRANSACTION SUMMARY");
            writer.println("===================");
            writer.printf("Total Sales Transactions:,%d%n", sales.size());
            writer.printf("Total Expense Transactions:,%d%n", expenses.size());
        }

        return file;
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
