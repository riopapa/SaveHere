package com.urrecliner.savehere;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import static com.urrecliner.savehere.Vars.utils;

@SuppressLint("AppCompatCustomView")
public class CustomTextOutLineView extends TextView {
    // 클래스명 다르게 생성시 붙여넣으면 이곳을 클래스명에 맞게 수정
    private boolean stroke = false;
    private float strokeWidth = 0.0f;
    private int strokeColor;

    public CustomTextOutLineView(Context context, AttributeSet attrs, int defStyle) {
// 클래스명 다르게 생성시 붙여넣으면 이곳을 클래스명에 맞게 수정
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    public CustomTextOutLineView(Context context, AttributeSet attrs) {
// 클래스명 다르게 생성시 붙여넣으면 이곳을 클래스명에 맞게 수정
        super(context, attrs);
        initView(context, attrs);
    }

    public CustomTextOutLineView(Context context) {
// 클래스명 다르게 생성시 붙여넣으면 이곳을 클래스명에 맞게 수정
        super(context);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextOutLineView);
        stroke = a.getBoolean(R.styleable.CustomTextOutLineView_textStroke, true);
        strokeWidth = a.getFloat(R.styleable.CustomTextOutLineView_textStrokeWidth, 4f);
        strokeColor = a.getColor(R.styleable.CustomTextOutLineView_textStrokeColor, 0x0);
        Log.w("init","stroke="+stroke+" width="+strokeWidth+", color="+strokeColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (stroke) {
            ColorStateList states = getTextColors();
            float strokeSize = getTextSize() / 8;
            getPaint().setStyle(Style.STROKE);
            getPaint().setStrokeWidth(strokeSize);
            setTextColor(Color.YELLOW);
            super.onDraw(canvas);
            getPaint().setStyle(Style.FILL);
            setTextColor(states);
        }
        super.onDraw(canvas);
    }
}