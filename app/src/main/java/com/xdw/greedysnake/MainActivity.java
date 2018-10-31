package com.xdw.greedysnake;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {
    Button button;
    EditText edit1,edit2;
    CheckBox checkbox;
    ProgressBar bar;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.login_button);
        edit1=findViewById(R.id.input1);
        edit2=findViewById(R.id.input2);
        checkbox=findViewById(R.id.remember_button);
        bar=findViewById(R.id.progress);
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember=pref.getBoolean("rem",false);     //获取代表是否保存密码的变量值，这里初值设为false

        if(isRemember) {
            //如果记住密码，则将账号和密码自动填充到文本框中
            String account=pref.getString("account","");
            String password=pref.getString("password","");
            edit1.setText(account);
            edit2.setText(password);
            checkbox.setChecked(true);
        }
        button.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        new Thread(new Runnable(){      //开启线程运行进度条，减少主线程的压力，这里不用子线程也影响不大
            @Override
            public void run() {
                for (int i=0;i<25;i++) {
                    int progress=bar.getProgress();
                    progress=progress + 10;
                    bar.setProgress(progress);
                }
            }
        }).start();

        String account=edit1.getText().toString();
        String password=edit2.getText().toString();
        if(account.equals("liusiyi") && password.equals("123456")) {
            editor=pref.edit();    //这个方法用于向SharedPreferences文件中写数据
            if(checkbox.isChecked()) {
                editor.putBoolean("rem",true);
                editor.putString("account",account);
                editor.putString("password",password);
            }
            else {
                editor.clear();
            }
            editor.commit();    //这个方法必须要有，不然数据不会被保存。生效后，就可以从该文件中读取数据。
            Intent intent=new Intent(MainActivity.this,SecondActivity.class);
            startActivity(intent);
        }
        else{    //如果用户名或密码不正确，这里会弹出一个提示框
            Toast.makeText(MainActivity.this,"账号或用户名错误",Toast.LENGTH_SHORT).show();
        }
    }
}
