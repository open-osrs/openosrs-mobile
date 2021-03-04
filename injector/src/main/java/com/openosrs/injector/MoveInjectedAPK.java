package com.openosrs.injector;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MoveInjectedAPK {
    static List<File> filesToZip = new ArrayList<>();
    public static void main(String[] args)
    {
        File source = new File("./osrs/dist/osrs.apk");
        File destination = new File("../client/lib/osrs.apk");
        try
        {
            try (
                    InputStream in = new BufferedInputStream(
                            new FileInputStream(source));
                    OutputStream out = new BufferedOutputStream(
                            new FileOutputStream(destination))) {

                byte[] buffer = new byte[1024];
                int lengthRead;
                while ((lengthRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, lengthRead);
                    out.flush();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
