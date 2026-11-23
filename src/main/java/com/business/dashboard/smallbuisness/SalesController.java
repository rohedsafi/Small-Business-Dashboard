package com.business.dashboard.smallbuisness;




import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

public class SalesController {

    @FXML private TextField txtProductName;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtUnitPrice;
    @FXML private TextField txtCustomerName;
    @FXML private DatePicker dpSaleDate;
    @FXML private Label lblTotalAmount;

    @FXML private TextField txtSearch;
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> colId;
    @FXML private TableColumn<Sale, String> colProduct;
    @FXML private TableColumn<Sale, Integer> colQuantity;
    @FXML private TableColumn<Sale, Double> colUnitPrice;
    @FXML private TableColumn<Sale, Double> colTotalAmount;
    @FXML private TableColumn<Sale, LocalDate> colDate;
    @FXML private TableColumn<Sale, String> colCustomer;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private ObservableList<Sale> salesList;
    private Sale selectedSale;

    @FXML
    public void initialize() {
        setupTable();
        setupListeners();
        loadSales();
        clearForm();

        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        // Format currency columns
        colUnitPrice.setCellFactory(tc -> new TableCell<Sale, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
            }
        });

        colTotalAmount.setCellFactory(tc -> new TableCell<Sale, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("$%.2f", amount));
            }
        });

        // Row selection
        salesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedSale = newSelection;
            boolean hasSelection = newSelection != null;
            btnUpdate.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);

            if (hasSelection) {
                populateForm(newSelection);
            }
        });
    }

    private void setupListeners() {
        // Auto-calculate total
        txtQuantity.textProperty().addListener((obs, old, newVal) -> calculateTotal());
        txtUnitPrice.textProperty().addListener((obs, old, newVal) -> calculateTotal());

        // Search functionality
        txtSearch.textProperty().addListener((obs, old, newVal) -> handleSearch());
    }

    private void calculateTotal() {
        try {
            int quantity = Integer.parseInt(txtQuantity.getText());
            double unitPrice = Double.parseDouble(txtUnitPrice.getText());
            double total = quantity * unitPrice;
            lblTotalAmount.setText(String.format("$%.2f", total));
        } catch (NumberFormatException e) {
            lblTotalAmount.setText("$0.00");
        }
    }

    private void loadSales() {
        try {
            salesList = SaleDAO.getAll();
            salesTable.setItems(salesList);
        } catch (SQLException e) {
            showError("Database Error", "Failed to load sales: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        // Validate input
        String error = validateInput();
        if (error != null) {
            showWarning("Validation Error", error);
            return;
        }

        try {
            Sale sale = createSaleFromForm();
            Sale createdSale = SaleDAO.create(sale);
            salesList.add(0, createdSale);
            clearForm();
            showInfo("Success", "Sale added successfully!");

        } catch (SQLException e) {
            showError("Database Error", "Failed to add sale: " + e.getMessage());
        } catch (Exception e) {
            showError("Error", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedSale == null) {
            showWarning("No Selection", "Please select a sale to update");
            return;
        }

        String error = validateInput();
        if (error != null) {
            showWarning("Validation Error", error);
            return;
        }

        try {
            updateSaleFromForm(selectedSale);
            SaleDAO.update(selectedSale);
            salesTable.refresh();
            clearForm();
            showInfo("Success", "Sale updated successfully!");

        } catch (SQLException e) {
            showError("Database Error", "Failed to update sale: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedSale == null) {
            showWarning("No Selection", "Please select a sale to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Sale");
        confirm.setContentText("Are you sure you want to delete this sale?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                SaleDAO.delete(selectedSale.getId());
                salesList.remove(selectedSale);
                clearForm();
                showInfo("Success", "Sale deleted successfully!");

            } catch (SQLException e) {
                showError("Database Error", "Failed to delete sale: " + e.getMessage());
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
            loadSales();
            return;
        }

        try {
            salesList = SaleDAO.search(keyword);
            salesTable.setItems(salesList);
        } catch (SQLException e) {
            showError("Search Error", "Failed to search: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            File file = ExportUtil.exportSalesToCSV(salesList);
            showInfo("Export Successful", "Sales exported to:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Export Failed", "Could not export sales: " + e.getMessage());
        }
    }

    private String validateInput() {
        String error;

        error = ValidationUtil.validateRequired(txtProductName.getText(), "Product name");
        if (error != null) return error;

        error = ValidationUtil.validatePositiveInteger(txtQuantity.getText(), "Quantity");
        if (error != null) return error;

        error = ValidationUtil.validatePositiveNumber(txtUnitPrice.getText(), "Unit price");
        if (error != null) return error;

        if (dpSaleDate.getValue() == null) {
            return "Sale date is required";
        }

        return null;
    }

    private Sale createSaleFromForm() {
        return new Sale(
                txtProductName.getText().trim(),
                Integer.parseInt(txtQuantity.getText()),
                Double.parseDouble(txtUnitPrice.getText()),
                dpSaleDate.getValue(),
                txtCustomerName.getText().trim()
        );
    }

    private void updateSaleFromForm(Sale sale) {
        sale.setProductName(txtProductName.getText().trim());
        sale.setQuantity(Integer.parseInt(txtQuantity.getText()));
        sale.setUnitPrice(Double.parseDouble(txtUnitPrice.getText()));
        sale.setSaleDate(dpSaleDate.getValue());
        sale.setCustomerName(txtCustomerName.getText().trim());
    }

    private void populateForm(Sale sale) {
        txtProductName.setText(sale.getProductName());
        txtQuantity.setText(String.valueOf(sale.getQuantity()));
        txtUnitPrice.setText(String.format("%.2f", sale.getUnitPrice()));
        txtCustomerName.setText(sale.getCustomerName());
        dpSaleDate.setValue(sale.getSaleDate());
    }

    private void clearForm() {
        txtProductName.clear();
        txtQuantity.clear();
        txtUnitPrice.clear();
        txtCustomerName.clear();
        dpSaleDate.setValue(LocalDate.now());
        lblTotalAmount.setText("$0.00");
        salesTable.getSelectionModel().clearSelection();
        selectedSale = null;
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
