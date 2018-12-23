package ru.ulmc.crawler.client.tools;

public class KeywordUtil {

    public static String wrapToRegexGroup(String keywords) {
        String[] split = keywords.split(" ");
        if (split.length == 1) {
            return split[0];
        } else {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < split.length; i++) {
                sb.append(split[i])
                        .append("|");
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
