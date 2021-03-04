package com.openosrs.injector;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ExtractVanillaJar {

    public static void main(String[] args)
    {
        String source = "./tmp/vanilla.jar";
        String destination = "./tmp/vanilla/";
        try {
            ZipFile zipFile = new ZipFile(source);
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
}
