package com.example.tonygui.nullpointercheck;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mText;
    private TestDemo mDemo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = findViewById(R.id.mytext);
        mText.setText(TextUtil.getInstance().getDemoText());
        mDemo = new TestDemo();
       String demo = mDemo.getDemoText();
    }
}
