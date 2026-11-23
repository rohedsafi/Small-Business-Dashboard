module com.business.dashboard.smallbuisness {
    requires javafx.controls;
    requires javafx.fxml;


    requires java.sql;

    opens com.business.dashboard.smallbuisness to javafx.fxml;
    exports com.business.dashboard.smallbuisness;
}