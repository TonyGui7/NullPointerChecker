package com.example.tonygui.nullpointercheck;

public class TestDemo {
    private final String mDefaultText = "默认字符串";
    public String getDemoText(){
        String str = "字符串";
        Object def = null;
        return "常量字符"+ str;
    }
}
