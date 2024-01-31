module com.example.spendwise {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.pdfbox;
    requires jasperreports;
    requires java.desktop;


    opens com.example.spendwise to javafx.fxml;
    exports com.example.spendwise;
}