package io.kazak

import org.fusesource.leveldbjni.JniDBFactory
import org.iq80.leveldb.Options
import java.io.File

public class LevelDBWrapper {


    fun foo(key: ByteArray) {
        val options = Options();
        options.createIfMissing(true);
        val factory = JniDBFactory.factory;
        val db = factory.open(File("example"), options);
        try {
            // Use the db in here....
            val iterator = db.iterator();
        } finally {
            // Make sure you close the db to shutdown the
            // database and avoid resource leaks.
            db.close();
        }
    }

}
