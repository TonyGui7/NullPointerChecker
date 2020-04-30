package com.example.tonygui.nullpointercheck;


public class MainTest {
    /**
     * field
     */
    private TestObjectB mDemo;
    String mText;

    /**
     * static Field
     */
    public static String TAG = "测试";
    private static MainTest mInstance;


    /**
     * constructor
     */
    private MainTest() {
        mDemo = new TestObjectB(TAG);
        mText = "测试";
    }


    /**
     * static Method
     */
    public static MainTest getInstance() {
        if (mInstance == null) {
            mInstance = new MainTest();
        }


        return mInstance;
    }

    /**
     * special Method
     */
    private TestObjectB getObjectB() {
        new TestObjectB("ceshi").getText(new TestObjectA("haha", new TestObjectB("demo")));
        return mDemo;
    }

    /**
     * virtual Method
     */
    public String getText() {
        if (!(getObjectB() instanceof TestObjectB)) {
            getObjectB().equals(null);
            return "";
        }

        return getObjectB().getText("");
    }


    /***************************
     *    Test Case            *
     ***************************/


    /**
     * test invoke case
     */
    public void testInvoke() {
        if (getObjectB() == null) {
            return;
        }
        //invoke special
        getObjectB().getText(new TestObjectA());

        //static field
        getObjectB().getText(TAG);

        //field
        getObjectB().getText(mText);

        //invoke special, field
        getObjectB().getText(mText, new TestObjectB(TAG));

        //invoke static without args, ldc
        getObjectB().getText("ceshi", TestObjectB.getInstance());

        //invoke static with args, field
        getObjectB().getText(mText, TestObjectB.getInstance(TAG, new TestObjectA()));
    }

    /**
     * test array case
     */
    public void testArray() {
        if (getObjectB() == null) {
            return;
        }
        getObjectB().getNumber("ce", new TestObjectA[11][13][14][15], new TestObjectA());
        //new array 1 dim
        getObjectB().getNumber(new TestObjectA[7]);

        //new array 2 dim
        getObjectB().getNumber(new TestObjectA[3][9]);

        //new array 4 dim
        getObjectB().getNumber(new TestObjectA[11][13][14][15]);

        //new array with invoke
        getObjectB().getNumber(new TestObjectA[5][getObjectB().getInteger()]);

        //new array with static invoke without args
        getObjectB().getNumber(new TestObjectA[5][TestObjectB.getStaticInteger()]);

        //new array with static invoke with one args
        getObjectB().getNumber(new TestObjectA[5][TestObjectB.getStaticInteger(getText())]);

        //new array with static invoke with two args
        getObjectB().getNumber(new TestObjectA[5][TestObjectB.getStaticInteger(getText(), getObjectB())]);

    }

    /**
     * test switch opcode case
     */
    public void testSwitch() {
        int result = 0;
        switch (getObjectB().getNumber()) {
            case 1:
                result = 1;
                break;
            case 7:
                result = 2;
                break;
            case 14:
                result = 3;
                break;
            default:
                result = 4;
                break;
        }

        String text = "test";
        switch (text) {
            case "test":
                text = "test";
                break;
            case "text":
                text = "text";
                break;
            case "txt":
                text = "txt";
                break;
            default:
                text = "default";
                break;
        }
    }
}
