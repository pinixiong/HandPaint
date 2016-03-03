package com.rj.handpaint.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rj.handpaint.R;
import com.rj.handpaint.view.PaintView;

public class MainActivity extends Activity {
    private PaintView paintView;
    private Button btn_1;
    private Boolean ispen = true;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);
        paintView = (PaintView) findViewById(R.id.paint_layout);
        btn_1 = (Button) findViewById(R.id.btn_1);
        dialog();
    }

    protected void dialog() {
        dialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info).setTitle("提示")
                .setMessage("确认保存吗?")
                .setPositiveButton("确定", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (paintView.saveBitmap()) {
                            Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton("取消", null).create();

    }

    public void onClick_Event(View view) {
        switch (view.getId()) {
            case R.id.btn_0:
                if (paintView.ago()) {
                    Toast.makeText(MainActivity.this, "上一次痕迹", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "痕迹为空", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_1:
                paintView.selectPaintStyle();
                if (ispen) {
                    btn_1.setText("铅笔");
                    this.ispen = false;
                } else {
                    btn_1.setText("橡皮");
                    this.ispen = true;
                }

                break;
            case R.id.btn_2:
                paintView.removeAllPaint();
                break;
            case R.id.btn_3:
                paintView.undo();
                break;
            case R.id.btn_4:
                paintView.redo();
                break;
            case R.id.btn_5:
                dialog.show();
                break;
            default:
                break;
        }
    }

}


