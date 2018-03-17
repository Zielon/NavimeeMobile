package org.pl.android.drively.util;

public class ChatUtils {
    public static String getRoomId(String a, String b) {
        return a.compareTo(b) > 0 ? b + "|" + a : a + "|" + b;
    }
}
