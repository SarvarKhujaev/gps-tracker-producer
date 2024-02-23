package com.ssd.mvd.netty;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FileUtil {

    public static Path createFile(String name, Integer trackerPort) {
        Path file = null;
        try {
            DateFormat df1 = new SimpleDateFormat("yyyy-MM");
            String dirName = df1.format(new Date());
            String dir = (trackerPort == Server.portForTeltonika) ? "teltonika" : "meitrack";
            Path theDir = Paths.get(dir + "/" + dirName);
            if (!Files.isDirectory(theDir)) {
                System.out.println("creating directory: " + theDir);
                Files.createDirectories(theDir, getFileAttributes());
                System.out.println("DIR created");
            }
            DateFormat df2 = new SimpleDateFormat("dd");
            String childDirName = df2.format(new Date());
            Path childDir = Paths.get(dir + "/" + dirName + "/" + childDirName);
            if (!Files.isDirectory(childDir)) {
                System.out.println("creating child directory: " + childDirName);
                Files.createFile(childDir, getFileAttributes());
                System.out.println("Child DIR created");
            }
            String fileName = childDir + "/" + trackerPort + "_" + name + ".log";
            file = Files.createFile(Paths.get(fileName), getFileAttributes());
            System.out.println(fileName + " created");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return file;
    }

    public static void writeToFile(String fileName, Integer trackerPort, String msg) {
        try {
            Path fileInException = FileUtil.createFile(fileName, trackerPort);
            String message = getDateTime() + "  " + msg;
            Files.write(fileInException, message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileAttribute<Set<PosixFilePermission>> getFileAttributes() {
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OTHERS_WRITE);
        permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        return PosixFilePermissions.asFileAttribute(permissions);
    }

    private static String getDateTime() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        return df.format(new Date());
    }

}
