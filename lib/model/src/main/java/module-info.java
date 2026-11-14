module org.pcsoft.app.hocal.model {
    requires kotlin.stdlib;
    requires org.pcsoft.app.hocal.crypto;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    // The types a user interface sees: the plain objects, the name rules and the errors.
    exports org.pcsoft.app.hocal.model.project;
    exports org.pcsoft.app.hocal.model.storage;
    exports org.pcsoft.app.hocal.model.tree;

    // Reading and writing the file, the media store, the media history and the tree operations are reached
    // only through the project manager of hocal-fx-model; no other module may call them.
    exports org.pcsoft.app.hocal.model.storage.internal to org.pcsoft.app.hocal.fx.model;
    exports org.pcsoft.app.hocal.model.tree.internal to org.pcsoft.app.hocal.fx.model;

    // Jackson reads and writes the POJOs reflectively through their getters and setters.
    opens org.pcsoft.app.hocal.model.project to tools.jackson.databind;
    opens org.pcsoft.app.hocal.model.storage.internal to tools.jackson.databind;
}
