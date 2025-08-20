package me.lyrica.utils.system;

import me.lyrica.Lyrica;
import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class NativeMusicInfo {
    static {
        try {
            String configDir = Lyrica.MOD_NAME;
            String nativeDir = configDir + File.separator + "native";
            String dllPath = nativeDir + File.separator + "Astera.dll";

            File nativeFolder = new File(nativeDir);
            if (!nativeFolder.exists()) {
                nativeFolder.mkdirs();
            }

            File dllFile = new File(dllPath);
            if (!dllFile.exists()) {
                // my github :OOO
                String dllUrl = "https://github.com/0x12F/songdetector/releases/download/dll/SongDetector.dll";
                try (InputStream in = new URL(dllUrl).openStream()) {
                    Files.copy(in, dllFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            System.load(dllFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static native String getCurrentSong();
}