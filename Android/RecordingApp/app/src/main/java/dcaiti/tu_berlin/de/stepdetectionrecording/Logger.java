package dcaiti.tu_berlin.de.stepdetectionrecording;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class to log sensor related data. Because of that this class optimizes the writing process
 * by using the same Writer over all write operations. That means that the user has to use the close
 * operations to be 100% certain that the writer will be correctly flushed and released
 * before object destruction.
 *
 * Created by Joris Clement on 13.11.17.
 */
public class Logger {

    private static final String TAG = Logger.class.getSimpleName();

    private final File logFile;

    private BufferedWriter writer;

    public Logger(File logFile) throws IOException {
        this.logFile = logFile;
        this.writer = new BufferedWriter(new FileWriter(logFile, true));
        Log.d(TAG, "log File: " + logFile.getAbsolutePath());
    }

    public void reInit() throws IOException {
        this.writer = new BufferedWriter(new FileWriter(this.logFile, true));
    }

    public void write(String data) throws IOException {
        this.writer.write(data);
    }

    public void writeln(String data) throws IOException {
        this.writer.write(data);
        this.writer.newLine();
        Log.v(TAG, logFile.getName() + "data written");
    }

    public void close() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }
}
