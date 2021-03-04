package com.openosrs.injector;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ExtractInjectedJar {

    public static void main(String[] args)
    {
        String source = "./build/libs/injected-client.jar";
        String destination = "./tmp/injected/";
        try {
            ZipFile zipFile = new ZipFile(source);
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }
}
