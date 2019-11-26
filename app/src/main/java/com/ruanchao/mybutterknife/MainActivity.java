package com.ruanchao.mybutterknife;

import android.app.Activity;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ruanchao.annotation_api.MyButterKnife;
import com.ruanchao.annotations.BindView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tv_text)
    TextView mTextView;

    @BindView(R.id.btn_click)
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnife.bind(this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("大家好");
            }
        });
    }

}
