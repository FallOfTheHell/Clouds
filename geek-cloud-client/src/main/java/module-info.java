module com.example.geekcloudclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.codec;
    requires com.geekbrains.cloud.common;


    opens com.example.geekcloudclient to javafx.fxml;
    exports com.example.geekcloudclient;
}