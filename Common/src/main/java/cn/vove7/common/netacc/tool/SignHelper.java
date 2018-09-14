package cn.vove7.common.netacc.tool;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

import cn.vove7.vtp.log.Vog;

/**
 * Created by IntelliJ IDEA.
 * User: Vove
 * Date: 2018/7/11
 * Time: 14:56
 */
public class SignHelper {
    private static final String SECRET_KEY = "vove777";

    /**
     * 给param签名
     *
     * @param params TreeMap 默认 通过key排序
     */
    public static void signParam(TreeMap<String, String> params) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> p : params.entrySet()) {
            if (p.getValue() == null) {
                String e = "参数错误 - s" + p.getKey();
                builder.append(p.getKey()).append("null");
                //return null;
            }
            if (!p.getKey().equalsIgnoreCase("sign"))
                builder.append(p.getKey()).append(p.getValue());
        }
        String r = builder.toString();

        Vog.INSTANCE.v(new Object(), "参数连接 -> " + r);

        params.put("sign", MD5(r + SECRET_KEY));
    }


    public static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
