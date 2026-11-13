module org.pcsoft.app.hocal.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires org.slf4j;
    requires org.apache.commons.lang3;

    requires org.controlsfx.controls;
    requires de.saxsys.mvvmfx;

    requires org.pcsoft.app.hocal.fx.model;

    opens org.pcsoft.app.hocal.app to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.hocal.app.ui.window to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.hocal.app.ui.component to javafx.fxml, de.saxsys.mvvmfx;
    opens org.pcsoft.app.hocal.app.ui.dialog to javafx.fxml, de.saxsys.mvvmfx;

    exports org.pcsoft.app.hocal.app;
    exports org.pcsoft.app.hocal.app.ui;
    exports org.pcsoft.app.hocal.app.ui.window;
    exports org.pcsoft.app.hocal.app.ui.component;
    exports org.pcsoft.app.hocal.app.ui.dialog;
    exports org.pcsoft.app.hocal.app.ui.preview;
    exports org.pcsoft.app.hocal.app.ui.prompt;
}
