package com.business.dashboard.smallbuisness;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

public class ExpensesController {

    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtAmount;
    @FXML private DatePicker dpExpenseDate;

    @FXML private TextField txtSearch;
    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, Integer> colId;
    @FXML private TableColumn<Expense, String> colCategory;
    @FXML private TableColumn<Expense, String> colDescription;
    @FXML private TableColumn<Expense, Double> colAmount;
    @FXML private TableColumn<Expense, LocalDate> colDate;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    private ObservableList<Expense> expensesList;
    private Expense selectedExpense;

    @FXML
    public void initialize() {
        setupTable();
        setupCategories();
        setupListeners();
        loadExpenses();

        dpExpenseDate.setValue(LocalDate.now());
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));

        // Format amount column
        colAmount.setCellFactory(tc -> new TableCell<Expense, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("$%.2f", amount));
            }
        });

        // Row selection
        expensesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedExpense = newSelection;
            boolean hasSelection = newSelection != null;
            btnUpdate.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);

            if (hasSelection) {
                populateForm(newSelection);
            }
        });
    }

    private void setupCategories() {
        cmbCategory.getItems().addAll(
                "Rent", "Utilities", "Salaries", "Marketing", "Supplies",
                "Insurance", "Transportation", "Equipment", "Maintenance", "Other"
        );
        cmbCategory.setValue("Other");
    }

    private void setupListeners() {
        txtSearch.textProperty().addListener((obs, old, newVal) -> handleSearch());
    }

    private void loadExpenses() {
        try {
            expensesList = ExpenseDAO.getAll();
            expensesTable.setItems(expensesList);
        } catch (SQLException e) {
            showError("Database Error", "Failed to load expenses: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        String error = validateInput();
        if (error != null) {
            showWarning("Validation Error", error);
            return;
        }

        try {
            Expense expense = createExpenseFromForm();
            Expense createdExpense = ExpenseDAO.create(expense);
            expensesList.add(0, createdExpense);
            clearForm();
            showInfo("Success", "Expense added successfully!");

        } catch (SQLException e) {
            showError("Database Error", "Failed to add expense: " + e.getMessage());
        } catch (Exception e) {
            showError("Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedExpense == null) {
            showWarning("No Selection", "Please select an expense to update");
            return;
        }

        String error = validateInput();
        if (error != null) {
            showWarning("Validation Error", error);
            return;
        }

        try {
            updateExpenseFromForm(selectedExpense);
            ExpenseDAO.update(selectedExpense);
            expensesTable.refresh();
            clearForm();
            showInfo("Success", "Expense updated successfully!");

        } catch (SQLException e) {
            showError("Database Error", "Failed to update expense: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedExpense == null) {
            showWarning("No Selection", "Please select an expense to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Expense");
        confirm.setContentText("Are you sure you want to delete this expense?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                ExpenseDAO.delete(selectedExpense.getId());
                expensesList.remove(selectedExpense);
                clearForm();
                showInfo("Success", "Expense deleted successfully!");

            } catch (SQLException e) {
                showError("Database Error", "Failed to delete expense: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText();

        if (keyword == null || keyword.trim().isEmpty()) {
            loadExpenses();
            return;
        }

        try {
            expensesList = ExpenseDAO.search(keyword);
            expensesTable.setItems(expensesList);
        } catch (SQLException e) {
            showError("Search Error", "Failed to search: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            File file = ExportUtil.exportExpensesToCSV(expensesList);
            showInfo("Export Successful", "Expenses exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export Failed", "Could not export expenses: " + e.getMessage());
        }
    }

    private String validateInput() {
        String error;

        if (cmbCategory.getValue() == null) {
            return "Category is required";
        }

        error = ValidationUtil.validatePositiveNumber(txtAmount.getText(), "Amount");
        if (error != null) return error;

        if (dpExpenseDate.getValue() == null) {
            return "Expense date is required";
        }

        return null;
    }

    private Expense createExpenseFromForm() {
        return new Expense(
                cmbCategory.getValue(),
                txtDescription.getText().trim(),
                Double.parseDouble(txtAmount.getText()),
                dpExpenseDate.getValue()
        );
    }

    private void updateExpenseFromForm(Expense expense) {
        expense.setCategory(cmbCategory.getValue());
        expense.setDescription(txtDescription.getText().trim());
        expense.setAmount(Double.parseDouble(txtAmount.getText()));
        expense.setExpenseDate(dpExpenseDate.getValue());
    }

    private void populateForm(Expense expense) {
        cmbCategory.setValue(expense.getCategory());
        txtDescription.setText(expense.getDescription());
        txtAmount.setText(String.format("%.2f", expense.getAmount()));
        dpExpenseDate.setValue(expense.getExpenseDate());
    }

    private void clearForm() {
        cmbCategory.setValue("Other");
        txtDescription.clear();
        txtAmount.clear();
        dpExpenseDate.setValue(LocalDate.now());
        expensesTable.getSelectionModel().clearSelection();
        selectedExpense = null;
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
