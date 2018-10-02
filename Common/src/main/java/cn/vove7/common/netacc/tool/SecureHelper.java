package cn.vove7.common.netacc.tool;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import cn.vove7.common.app.GlobalLog;
import cn.vove7.vtp.log.Vog;

/**
 * Created by IntelliJ IDEA.
 * User: Vove
 * Date: 2018/7/11
 * Time: 14:56
 */
public class SecureHelper {
    private static final String SECRET_KEY = "vove777";

    public static String signData(Object body, Long uId, Long time) {

        //uid{data}time
        String content = String.valueOf(uId == null ? "" : uId) + (body == null ? "" : body)
                + String.valueOf(time);
        //Vog.INSTANCE.d("", "signData --->\n" + content);

        return MD5(content + SECRET_KEY);
    }

    public static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (Exception e) {
            GlobalLog.INSTANCE.err(e);
            throw new RuntimeException(e);
        }
    }

    public static String MD5(String... ss) {
        StringBuilder bu = new StringBuilder();
        for (String s : ss) {
            bu.append(s);
        }
        return MD5(bu.toString());
    }

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private static String toHex(byte[] bytes) {
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            ret.append(HEX_DIGITS[(aByte >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[aByte & 0x0f]);
        }
        return ret.toString();
    }

}
