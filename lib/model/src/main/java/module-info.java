module org.pcsoft.app.hocal.model {
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    exports org.pcsoft.app.hocal.model.project;
    exports org.pcsoft.app.hocal.model.storage;

    // Jackson reads and writes the POJOs reflectively through their getters and setters.
    opens org.pcsoft.app.hocal.model.project to tools.jackson.databind;
    opens org.pcsoft.app.hocal.model.storage to tools.jackson.databind;
}
