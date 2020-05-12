package com.mp.android.apps.livevblank;

public class Constants {
    public static final String[] TEMPLATES = new String[]{"default_1", "default_2", "default_3",
            "default_4", "default_5", "default_6", "default_7", "default_8", "default_9"};
    private static String CURRENT_TEMPLATE = TEMPLATES[0];

    public static final String[] CN_UPPER_NUMBER = {"零", "壹", "贰", "叁", "肆",
            "伍", "陆", "柒", "捌", "玖"};

    public static String getCurrentTemplate() {
        return CURRENT_TEMPLATE;
    }

    public static void setCurrentTemplate(String currentTemplate) {
        CURRENT_TEMPLATE = currentTemplate;
    }
}
