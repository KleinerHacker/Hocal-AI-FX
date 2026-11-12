module org.pcsoft.app.hocal.model {
    requires kotlin.stdlib;

    exports org.pcsoft.app.hocal.model.project;

    // Jackson reads and writes the POJOs reflectively through their getters and setters.
    opens org.pcsoft.app.hocal.model.project to tools.jackson.databind;
}
