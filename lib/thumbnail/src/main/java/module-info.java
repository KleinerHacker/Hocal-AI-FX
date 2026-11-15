/**
 * The thumbnail caching of Hocal AI: caches in memory and in a ZIP file, the conversion of images and the encryption
 * of the file. From the toolkit it uses only the package javafx.scene.image.
 */
module org.pcsoft.app.hocal.thumbnail {
    requires kotlin.stdlib;
    requires transitive javafx.graphics;
    requires org.pcsoft.app.hocal.crypto;

    exports org.pcsoft.app.hocal.thumbnail;
}
