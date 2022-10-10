module com.example.geekcloudclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.codec;
    requires com.geekbrains.cloud.common;
    requires java.sql;
    requires mysql.connector.java;


    opens com.example.geekcloudclient to javafx.fxml;
    exports com.example.geekcloudclient;
}