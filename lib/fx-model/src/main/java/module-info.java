module org.pcsoft.app.hocal.fx.model {
    requires kotlin.stdlib;

    // The JavaFX property system is the reason this module exists, so consumers see it transitively.
    requires transitive javafx.base;

    // Only javafx.application.Platform is used, to recognize the FX thread; see the architecture rule.
    requires javafx.graphics;

    // The observable wrappers expose the plain model types, so consumers need them as well.
    requires transitive org.pcsoft.app.hocal.model;
    requires transitive org.pcsoft.app.hocal.thumbnail;

    // The property models mirroring the plain model types are the public surface.
    exports org.pcsoft.app.hocal.fx.model.project;

    // The singleton that owns the loaded project and is the only way to change it.
    exports org.pcsoft.app.hocal.fx.model.manager;
}
