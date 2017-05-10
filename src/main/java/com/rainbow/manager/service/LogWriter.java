package com.rainbow.manager.service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xuming on 2017/5/9.
 */
public class LogWriter implements Closeable {

    private String logDir = null;
    private String id = null;

    private FileOutputStream fos = null;
    private BufferedWriter bw = null;

    private int maxFileIndex = 20;

    public LogWriter(String logDir, String id) {
        this.logDir = logDir;
        this.id = id;
    }

    public void write(String line) throws IOException {
        if (fos == null && bw == null) {
            fos = new FileOutputStream(getNewFile());
            bw = new BufferedWriter(new OutputStreamWriter(fos));
        }

        bw.write(line + System.lineSeparator());
    }

    private String getNewFile() throws IOException {
        File[] files = new File(logDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(id);
            }
        });

        if (files != null && files.length > 0) {
            int maxIndex = Integer.MIN_VALUE;
            int minIndex = Integer.MAX_VALUE;
            File minIndexFile = null;
            for (File file : files) {
                int index = file.getCanonicalPath().lastIndexOf("_");
                int fileIndex = Integer.parseInt(file.getCanonicalPath().substring(index + 1));
                if (fileIndex > maxIndex) {
                    maxIndex = fileIndex;
                }

                if (fileIndex < minIndex) {
                    minIndex = fileIndex;
                    minIndexFile = file;
                }
            }

            if (maxIndex >= maxFileIndex - 1) {
                minIndexFile.delete();
            }

            String date = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

            return logDir + "/" + id + "_" + date + "_log_" + (maxIndex + 1) % maxFileIndex;
        } else {
            String date = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
            return logDir + "/" + id + "_" + date + "_log_0";
        }
    }

    @Override
    public void close() throws IOException {
        if (bw != null) {
            bw.close();
            bw = null;
        }

        if (fos != null) {
            fos.close();
            fos = null;
        }
    }
}
