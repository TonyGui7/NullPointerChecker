package com.example.tonygui.nullpointercheck;


public class TextUtil {
    private BaseClassDemo mDemo;
    String mText;

    public static String TAG = "测试";

    private TextUtil() {
        mDemo = new BaseClassDemo();
        mText = "测试";
    }



    private static TextUtil mInstance;

    private final String DEFAULT_TEXT = "默认字符串";

    public static TextUtil getInstance() {
        if (mInstance == null) {
            mInstance = new TextUtil();
        }


        return mInstance;
    }

    private BaseClassDemo getDemo() {
        return mDemo;
    }

    public String getDemoText() {
         if (getDemo() != null) {
             return "";
         }

         return getDemo().getDemoText(getDemo().getDemoText(""));
    }

    public class BaseClassDemo {
        public String getDemoText(String demo) {
            return "";
        }
    }
}
