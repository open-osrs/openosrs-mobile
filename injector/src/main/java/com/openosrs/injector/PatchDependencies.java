package com.openosrs.injector;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PatchDependencies {

    static File vAndroid = new File("./tmp/vanilla/android/");
    static File iAndroid = new File("./tmp/injected/android/");

    static File vAndroidx = new File("./tmp/vanilla/androidx/");
    static File iAndroidx = new File("./tmp/injected/androidx/");

    static File vDagger = new File("./tmp/vanilla/dagger/");
    static File iDagger = new File("./tmp/injected/dagger/");

    static File vJavax = new File("./tmp/vanilla/javax/");
    static File iJavax = new File("./tmp/injected/javax/");

    static File vNet = new File("./tmp/vanilla/net/");
    static File iNet = new File("./tmp/injected/net/");

    static File vComAndroid = new File("./tmp/vanilla/com/android/");
    static File iComAndroid = new File("./tmp/injected/com/android/");

    static File vComAppsflyer = new File("./tmp/vanilla/com/appsflyer/");
    static File iComAppsflyer = new File("./tmp/injected/com/appsflyer/");

    static File vComGoogle = new File("./tmp/vanilla/com/google/");
    static File iComGoogle = new File("./tmp/injected/com/google/");

    static File vComJagexAndroid = new File("./tmp/vanilla/com/jagex/android/");
    static File iComJagexAndroid = new File("./tmp/injected/com/jagex/android/");

    static File vComJagexJagex3 = new File("./tmp/vanilla/com/jagex/jagex3/");
    static File iComJagexJagex3 = new File("./tmp/injected/com/jagex/jagex3/");

    static File vComJagexMobilesdk = new File("./tmp/vanilla/com/jagex/mobilesdk/");
    static File iComJagexMobilesdk = new File("./tmp/injected/com/jagex/mobilesdk/");

    public static void main(String[] args)
    {
        replaceFiles(vAndroid, iAndroid);
        replaceFiles(vAndroidx, iAndroidx);
        replaceFiles(vDagger, iDagger);
        replaceFiles(vJavax, iJavax);
        replaceFiles(vNet, iNet);
        replaceFiles(vComAndroid, iComAndroid);
        replaceFiles(vComAppsflyer, iComAppsflyer);
        replaceFiles(vComGoogle, iComGoogle);
        replaceFiles(vComJagexAndroid, iComJagexAndroid);
        replaceFiles(vComJagexJagex3, iComJagexJagex3);
        replaceFiles(vComJagexMobilesdk, iComJagexMobilesdk);
    }

    public static void replaceFiles(File sourceDir,File destDir)
    {
        if (deleteDirectory(destDir)) {
            try {
                copyDirectory(sourceDir, destDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private static void copyDirectory(File sourceDirectory, File destinationDirectory) throws IOException {
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        for (String f : sourceDirectory.list()) {
            copyDirectoryCompatibityMode(new File(sourceDirectory, f), new File(destinationDirectory, f));
        }
    }

    public static void copyDirectoryCompatibityMode(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            copyDirectory(source, destination);
        } else {
            copyFile(source, destination);
        }
    }

    private static void copyFile(File sourceFile, File destinationFile)
            throws IOException {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }
}
