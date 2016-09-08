/**
 * 连接PC的Activity
 */
package connectpc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.lingdong20.MainActivity;
import com.lingdong20.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import filestrans.Files_Trans_Activity;
import service.ClipBoardActivity;

/**
 * Created by 周博文 on 2016/8/23.
 */
public class Connect_PC  extends Activity {

    public static String DuiFangde_IP2 = MainActivity.IP_DuiFangde;
    public static String DuiFangde_IP3 = MainActivity.IP_DuiFangde;

    private Socket socket;

    //发送文件到PC
    private Button btn_connect_pc_sendfiletopc;
    //界面的九宫格
    private Button btn_connect_pc_ESC;
    private Button btn_connect_pc_UP;
    private Button btn_connect_pc_ALT_TAB;
    private Button btn_connect_pc_LEFT;
    private Button btn_connect_pc_ENTER;
    private Button btn_connect_pc_RIGHT;
    private Button btn_connect_pc_WIN;
    private Button btn_connect_pc_DOWN;
    private Button btn_connect_pc_ScreenCut;
    //关机的按钮
    private Button btn_connect_pc_shutdownpc;
    //关闭当前窗口
    private Button btn_connect_pc_shutdownpresentscreen;
    //文字剪贴
    private Button btn_connect_pc_textcutpaste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_pc_main);

        //发送文件到PC
        btn_connect_pc_sendfiletopc = (Button) findViewById(R.id.btn_connect_pc_sendfiletopc);

        //界面的九宫格
        btn_connect_pc_ESC = (Button) findViewById(R.id.btn_connect_pc_ESC);
        btn_connect_pc_UP = (Button) findViewById(R.id.btn_connect_pc_UP);
        btn_connect_pc_ALT_TAB = (Button) findViewById(R.id.btn_connect_pc_ALT_TAB);
        btn_connect_pc_LEFT = (Button) findViewById(R.id.btn_connect_pc_LEFT);
        btn_connect_pc_ENTER = (Button) findViewById(R.id.btn_connect_pc_ENTER);
        btn_connect_pc_RIGHT = (Button) findViewById(R.id.btn_connect_pc_RIGHT);
        btn_connect_pc_WIN = (Button) findViewById(R.id.btn_connect_pc_WIN);
        btn_connect_pc_DOWN = (Button) findViewById(R.id.btn_connect_pc_DOWN);
        btn_connect_pc_ScreenCut = (Button) findViewById(R.id.btn_connect_pc_ScreenCut);

        //关机的按钮
        btn_connect_pc_shutdownpc = (Button) findViewById(R.id.btn_connect_pc_shutdownpc);
        //关闭当前窗口
        btn_connect_pc_shutdownpresentscreen = (Button) findViewById(R.id.btn_connect_pc_shutdownpresentscreen);
        //文字剪贴按钮
        btn_connect_pc_textcutpaste = (Button) findViewById(R.id.btn_connect_pc_textcutpaste);

        /**设置九宫格按钮的监听事件*/
        btn_connect_pc_ESC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01esc");
            }
        });

        btn_connect_pc_UP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01up");
            }
        });

        btn_connect_pc_ALT_TAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01alttab");
            }
        });

        btn_connect_pc_LEFT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01left");
            }
        });

        btn_connect_pc_ENTER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01enter");
            }
        });

        btn_connect_pc_RIGHT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01right");
            }
        });


        btn_connect_pc_WIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01win");
            }
        });

        btn_connect_pc_DOWN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01down");
            }
        });

        btn_connect_pc_ScreenCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01screencut");
            }
        });

        //关闭电脑的监听事件
        btn_connect_pc_shutdownpc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               //弹出对话框，进行文件提取码的输入，然后下载文件
                AlertDialog.Builder builder = new AlertDialog.Builder(Connect_PC.this);
                builder.setTitle("重要提醒！！！");
                //通过LayoutInflater来加载一个xml的布局文件作为一个View对象
                final View Dialogview = LayoutInflater.from(Connect_PC.this).inflate(R.layout.connect_pc_shutdownpc_dialog, null);
                //设置我们自己定义的布局文件作为弹出框的Content
                builder.setView(Dialogview);
                //设置点击下载后的事件
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Client(DuiFangde_IP2,1234,"01shutdownpc");
                    }
                });

                //设置点击取消后的事件
                builder.setNegativeButton("取消", null);
                builder.show();
            }
        });

        //关闭电脑的当前窗口
        btn_connect_pc_shutdownpresentscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Client(DuiFangde_IP2,1234,"01shutdownpresentscreen");
            }
        });


        //发送文件到电脑的按钮的监听事件
        btn_connect_pc_sendfiletopc.setOnClickListener(new View.OnClickListener() {

            //
            @Override
            public void onClick(View view) {
                //把IP传进去
                MainActivity.IP_DuiFangde = DuiFangde_IP3;
                //跳转到文件发送界面
                Intent intent_filetrans = new Intent(getApplicationContext(), Files_Trans_Activity.class);
                startActivity(intent_filetrans);

            }
        });


        //文字剪贴按钮，跳转到一个新的activity
       btn_connect_pc_textcutpaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到文字剪贴界面
                Intent intent_textcutpaste = new Intent(getApplicationContext(), ClipBoardActivity.class);
                startActivity(intent_textcutpaste);
            }
        });


    }


    //发送指令到pc,操作电脑
    /**
     * \
     * @param ipAddress
     * @param port
     * @param zhiling
     */
    public void Client(final String ipAddress, final int port,final String zhiling)
    {
        socket = null;
        new Thread(new Runnable() {
            public void run() {
                DataOutputStream dos = null;
                try
                {

                    socket = new Socket();
                    socket.connect(new InetSocketAddress(ipAddress, port));
                    dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(zhiling);// 传送的指令
                    dos.flush();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (dos != null)
                        try {
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    if (socket != null)
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }

        }).start();

    }

}