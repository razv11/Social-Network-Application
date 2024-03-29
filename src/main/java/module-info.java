module ro.ubbcluj.cs.map.socialnetworkgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;


    opens ro.ubbcluj.cs.map.socialnetworkgui to javafx.fxml;
    opens ro.ubbcluj.cs.map.domain;

    exports ro.ubbcluj.cs.map.socialnetworkgui;
}