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

    private int maxFileIndex = 10;

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
        bw.flush();
    }

    private String getNewFile() throws IOException {
        File[] files = new File(logDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(id);
            }
        });

        if (files != null && files.length > 0) {
            String first = files[0].getCanonicalPath();
            int startIndex = first.indexOf("_", first.lastIndexOf("/")) + 1;
            int endIndex = first.lastIndexOf("_log");

            String minDate = first.substring(startIndex, endIndex);
            File minFile = files[0];
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String  current = file.getCanonicalPath();
                String currDate = current.substring(startIndex, endIndex);

                if (minDate.compareTo(currDate) > 0) {
                    minDate = currDate;
                    minFile = file;
                }
            }

            String index = files.length + "";
            if (minFile != null && files.length >= maxFileIndex) {
                System.out.println("delete file: " + minFile.getCanonicalPath());
                minFile.delete();

                String minPath = minFile.getCanonicalPath();
                index = minPath.substring(minPath.lastIndexOf("_") + 1);
            }

            String date = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
            return logDir + "/" + id + "_" + date + "_log_" + index;
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
