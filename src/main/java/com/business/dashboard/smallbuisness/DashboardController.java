package com.business.dashboard.smallbuisness;





import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label lblTotalSales;
    @FXML private Label lblTotalExpenses;
    @FXML private Label lblNetProfit;
    @FXML private Label lblSalesCount;
    @FXML private Label lblExpensesCount;

    @FXML private LineChart<String, Number> salesTrendChart;
    @FXML private LineChart<String, Number> expensesTrendChart;
    @FXML private BarChart<String, Number> profitChart;
    @FXML private PieChart categoryPieChart;

    @FXML private TableView<Sale> recentSalesTable;
    @FXML private TableColumn<Sale, Integer> colSaleId;
    @FXML private TableColumn<Sale, String> colSaleProduct;
    @FXML private TableColumn<Sale, Double> colSaleAmount;
    @FXML private TableColumn<Sale, LocalDate> colSaleDate;

    @FXML private TableView<Expense> recentExpensesTable;
    @FXML private TableColumn<Expense, Integer> colExpenseId;
    @FXML private TableColumn<Expense, String> colExpenseCategory;
    @FXML private TableColumn<Expense, Double> colExpenseAmount;
    @FXML private TableColumn<Expense, LocalDate> colExpenseDate;

    @FXML private DatePicker dateFromFilter;
    @FXML private DatePicker dateToFilter;

    private ObservableList<Sale> allSales;
    private ObservableList<Expense> allExpenses;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
        updateDashboard();

        // DEBUG: try loading Sale.fxml at startup to capture loading errors early
        try {
            var url = getClass().getResource("/com/business/dashboard/smallbuisness/Sale.fxml");
            if (url == null) System.out.println("DEBUG: Sale.fxml resource not found");
            else {
                new javafx.fxml.FXMLLoader(url).load();
                System.out.println("DEBUG: Sale.fxml loaded successfully");
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Exception while loading Sale.fxml");
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        // Sales table columns
        colSaleId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colSaleProduct.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        colSaleAmount.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotalAmount()).asObject());
        colSaleDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSaleDate()));

        // Expenses table columns
        colExpenseId.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colExpenseCategory.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
        colExpenseAmount.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        colExpenseDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getExpenseDate()));

        // Format amount columns
        colSaleAmount.setCellFactory(tc -> new TableCell<Sale, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        colExpenseAmount.setCellFactory(tc -> new TableCell<Expense, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
    }

    private void loadData() {
        try {
            allSales = SaleDAO.getAll();
            allExpenses = ExpenseDAO.getAll();

            // Show only recent 10 transactions
            recentSalesTable.setItems(FXCollections.observableArrayList(
                    allSales.stream().limit(10).collect(Collectors.toList())
            ));
            recentExpensesTable.setItems(FXCollections.observableArrayList(
                    allExpenses.stream().limit(10).collect(Collectors.toList())
            ));

        } catch (SQLException e) {
            showError("Database Error", "Failed to load data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateDashboard() {
        // Calculate totals
        double totalSales = allSales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double totalExpenses = allExpenses.stream().mapToDouble(Expense::getAmount).sum();
        double netProfit = totalSales - totalExpenses;

        // Update summary labels
        lblTotalSales.setText(String.format("$%.2f", totalSales));
        lblTotalExpenses.setText(String.format("$%.2f", totalExpenses));
        lblNetProfit.setText(String.format("$%.2f", netProfit));
        lblSalesCount.setText(String.valueOf(allSales.size()));
        lblExpensesCount.setText(String.valueOf(allExpenses.size()));

        // Update charts
        updateSalesTrendChart();
        updateExpensesTrendChart();
        updateProfitChart();
        updateCategoryPieChart();
    }

    private void updateSalesTrendChart() {
        salesTrendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");

        Map<String, Double> monthlyData = allSales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));

        monthlyData.forEach((month, amount) ->
                series.getData().add(new XYChart.Data<>(month, amount))
        );

        salesTrendChart.getData().add(series);
    }

    private void updateExpensesTrendChart() {
        expensesTrendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        Map<String, Double> monthlyData = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getExpenseDate().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        monthlyData.forEach((month, amount) ->
                series.getData().add(new XYChart.Data<>(month, amount))
        );

        expensesTrendChart.getData().add(series);
    }

    private void updateProfitChart() {
        profitChart.getData().clear();

        Map<String, Double> monthlySales = allSales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));

        Map<String, Double> monthlyExpenses = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getExpenseDate().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        Collectors.summingDouble(Expense::getAmount)
                ));

        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(monthlySales.keySet());
        allMonths.addAll(monthlyExpenses.keySet());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Profit");

        allMonths.forEach(month -> {
            double sales = monthlySales.getOrDefault(month, 0.0);
            double expenses = monthlyExpenses.getOrDefault(month, 0.0);
            series.getData().add(new XYChart.Data<>(month, sales - expenses));
        });

        profitChart.getData().add(series);
    }

    private void updateCategoryPieChart() {
        categoryPieChart.getData().clear();

        Map<String, Double> categoryData = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        categoryData.forEach((category, amount) ->
                categoryPieChart.getData().add(new PieChart.Data(category + " ($" + String.format("%.2f", amount) + ")", amount))
        );
    }

    @FXML
    private void handleFilterByDate() {
        LocalDate from = dateFromFilter.getValue();
        LocalDate to = dateToFilter.getValue();

        if (from == null || to == null) {
            showWarning("Filter Error", "Please select both start and end dates");
            return;
        }

        if (from.isAfter(to)) {
            showWarning("Filter Error", "Start date must be before end date");
            return;
        }

        try {
            allSales = SaleDAO.getByDateRange(from, to);
            allExpenses = ExpenseDAO.getByDateRange(from, to);

            recentSalesTable.setItems(FXCollections.observableArrayList(
                    allSales.stream().limit(10).collect(Collectors.toList())
            ));
            recentExpensesTable.setItems(FXCollections.observableArrayList(
                    allExpenses.stream().limit(10).collect(Collectors.toList())
            ));

            updateDashboard();

        } catch (SQLException e) {
            showError("Database Error", "Failed to filter data: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearFilter() {
        dateFromFilter.setValue(null);
        dateToFilter.setValue(null);
        loadData();
        updateDashboard();
    }

    @FXML
    private void handleOpenSales() {
        openWindow("/com/business/dashboard/smallbuisness/Sale.fxml", "Sales Management");
    }

    @FXML
    private void handleOpenExpenses() {
        openWindow("/com/business/dashboard/smallbuisness/Expense.fxml", "Expenses Management");
    }

    @FXML
    private void handleRefresh() {
        loadData();
        updateDashboard();
        showInfo("Refreshed", "Dashboard data has been refreshed");
    }

    @FXML
    private void handleExportSummary() {
        try {
            File file = ExportUtil.exportDashboardSummary(allSales, allExpenses);
            showInfo("Export Successful", "Dashboard summary exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export Failed", "Could not export summary: " + e.getMessage());
        }
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) {
                showError("Resource Not Found", "FXML not found on classpath: " + fxmlPath);
                System.err.println("Resource missing: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh data after window closes
            loadData();
            updateDashboard();

        } catch (Exception e) {
            showError("Error", "Failed to open window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}