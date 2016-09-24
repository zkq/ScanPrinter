package com.example.zkq.scanprinter.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.api.SerialPort;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.zkq.scanprinter.R;
import com.example.zkq.scanprinter.util.ImageUtil;
import com.smartdevicesdk.io.ScanGpio;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    EditText editText;
    Bitmap bitmap;

    public static SerialPort serialPort;
    public Handler handler;
    public static long pressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.iv);
        button = (Button) findViewById(R.id.btnPrintPic);
        editText = (EditText) findViewById(R.id.editText);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String barCode = editText.getText().toString().trim();

                if(barCode.equals("")){
                    imageView.setImageBitmap(null);
                    return;
                }

                if(barCode.getBytes().length > barCode.length()){
                    imageView.setImageBitmap(null);
                    showToast("不能创建条形码");
                    return;
                }

                button.setEnabled(false);
                bitmap = ImageUtil.createBitmap(MainActivity.this, barCode, 384, 80);
                imageView.setImageBitmap(bitmap);
                button.setEnabled(true);

                if(bitmap == null)
                {
                    showToast("创建条形码失败");
                    return;
                }

                handler.sendEmptyMessage(0xfe);
            }
        });

        serialPort = new SerialPort(this);
        boolean b = serialPort.openPort();

        if(!b)
        {
            showToast("初始化扫描仪失败");
        }

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what)
                {
                    case 0xff:
                        String str = msg.getData().getString("barstr");
                        editText.setText(str);
                        break;
                    case 0xfe:
                        serialPort.switchFunc(SerialPort.PORT.comPrinter);
                        serialPort.Write(ImageUtil.getImageBytes(bitmap));
                        serialPort.Write("\n");
                        serialPort.Write("\n");
                        serialPort.Write("\n");
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void showToast(String str)
    {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onResume() {
        super.onResume();
        ScanGpio.openPower();
        serialPort.startRead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ScanGpio.closePower();
        serialPort.stopRead();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialPort.closePort();
        serialPort = null;
    }
}
