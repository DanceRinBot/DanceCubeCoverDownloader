//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.Downloader;

import java.io.File;
import java.io.IOException;

public abstract class Config {
    public static String linuxRootPath;
    public static String windowsRootPath;
    private static final boolean windowsMark = (new File("./WINDOWS_MARK")).exists();
    public static String configPath;

    public Config() {
    }

    public static boolean itIsAReeeeaaaalWindowsMark() {
        return windowsMark;
    }

    static {
        try {
            linuxRootPath = (new File("..")).getCanonicalPath();
            windowsRootPath = (new File(".")).getCanonicalPath();
            if (itIsAReeeeaaaalWindowsMark()) {
                configPath = windowsRootPath + "/DcConfig/";
            } else {
                configPath = linuxRootPath + "/DcConfig/";
            }

            (new File(configPath)).mkdirs();
        } catch (IOException var1) {
            var1.printStackTrace();
        }

    }
}
