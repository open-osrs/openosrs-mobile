package com.openosrs.injector;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PackPatchedJar {
    static List<File> filesToZip = new ArrayList<>();
    public static void main(String[] args)
    {
        String source = "./tmp/injected/";
        String destination = "./tmp/injected-patched.jar";
        File zip = new File("./tmp/injected-patched.jar");
        if (zip.exists())
            zip.delete();
        try {
            ZipFile zipFile = new ZipFile(destination);

            for (File f : new File(source).listFiles())
            {
                if (f.isDirectory())
                    zipFile.addFolder(f);
                else
                    zipFile.addFile(f);
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
}
