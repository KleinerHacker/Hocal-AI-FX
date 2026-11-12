module org.pcsoft.app.hocal.fx.model {
    requires kotlin.stdlib;

    // The JavaFX property system is the reason this module exists, so consumers see it transitively.
    requires transitive javafx.base;

    // The observable wrappers expose the plain model types, so consumers need them as well.
    requires transitive org.pcsoft.app.hocal.model;

    // The property models mirroring the plain model types are the public surface.
    exports org.pcsoft.app.hocal.fx.model.project;

    // The singleton that owns the loaded project and is the only way to change it.
    exports org.pcsoft.app.hocal.fx.model.manager;
}
