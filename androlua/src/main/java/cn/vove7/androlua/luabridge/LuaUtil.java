package cn.vove7.androlua.luabridge;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class LuaUtil {
    private static final byte[] BUFFER = new byte[1024 * 1024];

    //读取asset文件

    public static byte[] readAsset(Context context, String name) throws IOException {
        AssetManager am = context.getAssets();
        InputStream is = am.open(name);
        byte[] ret = readAll(is);
        is.close();
        //am.close();
        return ret;
    }

    public static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[2 ^ 32];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] ret = output.toByteArray();
        output.close();
        return ret;
    }

    public static String getTextFromAsset(Context context, String file) {
        StringBuffer buffer = null;
        try {
            InputStream is = context.getAssets().open(file);
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            buffer = new StringBuffer("");
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
                buffer.append("\n");
            }
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    //复制asset文件到sd卡
    public static void assetsToSD(Context context, String InFileName, String OutFileName) throws IOException {
        InputStream myInput;
        File p = new File(OutFileName).getParentFile();
        if (!p.exists()) {
            p.mkdirs();
        }
        OutputStream myOutput = new FileOutputStream(OutFileName);
        myInput = context.getAssets().open(InFileName);
        byte[] buffer = new byte[8192];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    public static void copyFile(String from, String to) {
        try {
            copyFile(new FileInputStream(from), new FileOutputStream(to));
        } catch (IOException e) {
            Log.d("lua", e.getMessage());
        }
    }

    public static boolean copyFile(InputStream in, OutputStream out) {
        try {
            int byteread;
            byte[] buffer = new byte[1024 * 1024];
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("lua", e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean copyDir(String from, String to) {
        return copyDir(new File(from), new File(to));
    }

    public static boolean copyDir(File from, File to) {
        boolean ret = true;
        File p = to.getParentFile();
        if (!p.exists())
            p.mkdirs();
        if (from.isDirectory()) {
            File[] fs = from.listFiles();
            if (fs != null && fs.length != 0) {
                for (File f : fs)
                    ret = copyDir(f, new File(to, f.getName()));
            } else {
                if (!to.exists())
                    ret = to.mkdirs();
            }
        } else {
            try {
                if (!to.exists())
                    to.createNewFile();
                ret = copyFile(new FileInputStream(from), new FileOutputStream(to));
            } catch (IOException e) {
                Log.e("lua", e.getMessage());
                ret = false;
            }
        }
        return ret;
    }

    public static boolean rmDir(File dir) {
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            for (File f : fs)
                rmDir(f);
        }
        return dir.delete();
    }

    public static void rmDir(File dir, String ext) {
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            for (File f : fs)
                rmDir(f, ext);
            dir.delete();
        }
        if (dir.getName().endsWith(ext))
            dir.delete();
    }

// 计算文件的 MD5 值

    public static byte[] readZip(String zippath, String filepath) throws IOException {
        ZipFile zip = new ZipFile(zippath);
        ZipEntry entey = zip.getEntry(filepath);
        InputStream is = zip.getInputStream(entey);
        return readAll(is);
    }

    public static String getFileMD5(File file) {
        try {
            return getFileMD5(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String getFileMD5(InputStream in) {
        byte buffer[] = new byte[8192];
        int len;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    // 计算文件的 SHA-1 值
    public static String getFileSha1(InputStream in) {
        byte buffer[] = new byte[8192];
        int len;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            BigInteger bigInt = new BigInteger(1, digest.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void unZip(String SourceDir) throws IOException {
        unZip(SourceDir, new File(SourceDir).getParent(), "");
    }

    public static void unZip(String SourceDir, String extDir) throws IOException {
        unZip(SourceDir, extDir, "");
    }

    public static void unZip(String SourceDir, String extDir, String fileExt) throws IOException {
        ZipFile zip = new ZipFile(SourceDir);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!name.startsWith(fileExt))
                continue;
            String path = name;
            if (entry.isDirectory()) {
                File f = new File(extDir + File.separator + path);
                if (!f.exists())
                    f.mkdirs();
            } else {
                String fname = extDir + File.separator + path;
                File temp = new File(fname).getParentFile();
                if (!temp.exists()) {
                    if (!temp.mkdirs()) {
                        throw new RuntimeException("create file " + temp.getName() + " fail");
                    }
                }

                FileOutputStream out = new FileOutputStream(extDir + File.separator + path);
                InputStream in = zip.getInputStream(entry);
                byte[] buf = new byte[2 ^ 16];
                int count = 0;
                while ((count = in.read(buf)) != -1) {
                    out.write(buf, 0, count);
                }
                out.close();
                in.close();
            }
        }
        zip.close();
    }

    public static boolean zip(String sourceFilePath) {
        return zip(sourceFilePath, new File(sourceFilePath).getParent());
    }

    public static boolean zip(String sourceFilePath, String zipFilePath) {
        File f = new File(sourceFilePath);
        return zip(sourceFilePath, f.getParent(), f.getName() + ".zip");
    }

    public static boolean zip(String sourceFilePath, String zipFilePath, String zipFileName) {
        boolean result = false;
        File source = new File(sourceFilePath);
        File zipFile = new File(zipFilePath, zipFileName);
        if (!zipFile.getParentFile().exists()) {
            if (!zipFile.getParentFile().mkdirs()) {
                return result;
            }
        }
        if (zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                return result;
            }
        }

        FileOutputStream dest = null;
        ZipOutputStream out = null;
        try {
            dest = new FileOutputStream(zipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            out = new ZipOutputStream(new BufferedOutputStream(checksum));
            out.setMethod(ZipOutputStream.DEFLATED);
            compress(source, out, "/");
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static void compress(File file, ZipOutputStream out, String mainFileName) {
        if (file.isFile()) {
            FileInputStream fi = null;
            BufferedInputStream origin = null;
            try {
                fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER.length);
                //int index=file.getAbsolutePath().indexOf(mainFileName);
                String entryName = mainFileName + file.getName();
                System.out.println(entryName);
                ZipEntry entry = new ZipEntry(entryName);
                out.putNextEntry(entry);
                //			byte[] data = new byte[BUFFER];
                int count;
                while ((count = origin.read(BUFFER, 0, BUFFER.length)) != -1) {
                    out.write(BUFFER, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (origin != null) {
                    try {
                        origin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (file.isDirectory()) {
            File[] fs = file.listFiles();
            if (fs != null && fs.length > 0) {
                for (File f : fs) {
                    if (f.isFile())
                        compress(f, out, mainFileName);
                    else
                        compress(f, out, mainFileName + f.getName() + "/");

                }
            }
        }
    }

    private static final String[] reasons = new String[]{
            "Success", "Yield error", "Runtime error", "Syntax error",
            "Out of memory", "GC error", "error error"
    };


    public static String errorReason(int error) {
        if (error >= 0 && error < reasons.length) {
            return reasons[error];
        }
        return "Unknown error " + error;
    }

}
