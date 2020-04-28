package com.example.tonygui.nullpointercheck;

public class TestObjectB {
    public TestObjectB(String text) {

    }

    /**
     * static Field
     */
    public static int Test_Number = 5;
    public static TestObjectB Instance = new TestObjectB("");

    /**
     * virtual Method
     */
    public int getInteger() {
        return 2;
    }

    public String getText(String demo) {
        return "";
    }

    public String getText(String demo, TestObjectB classDemo) {
        return "";
    }

    public String getText(TestObjectA demo) {
        return "";
    }

    public int getNumber(TestObjectA[] demos) {
        return demos.length;
    }

    public int getNumber(TestObjectA[][] demos) {
        return demos.length;
    }

    public int getNumber(TestObjectA[][][][] demos) {
        return demos.length;
    }

    public int getNumber(String text, TestObjectA[][][][] demos, TestObjectA demo) {
        return demos.length;
    }


    /**
     * static Method
     */
    public static int getStaticInteger() {
        return 5;
    }

    public static int getStaticInteger(String input) {
        return 5;
    }

    public static int getStaticInteger(String input, TestObjectB classDemo) {
        return 5;
    }

    public static TestObjectB getInstance() {
        return Instance;
    }

    public static TestObjectB getInstance(String text, TestObjectA demo) {
        return Instance;
    }
}
