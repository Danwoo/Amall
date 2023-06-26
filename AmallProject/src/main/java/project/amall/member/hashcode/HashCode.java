package project.amall.member.hashcode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCode {
    public static String convert(String plain) {
        String cipher = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plain.getBytes());
            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            cipher = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cipher = null;
        }
        return cipher;
    }
}
