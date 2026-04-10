module example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens example.demo to javafx.fxml;
    exports example.demo;
}