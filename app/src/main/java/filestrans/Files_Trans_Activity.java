/**
 * 文件传输
 *
 * */

package filestrans;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lingdong20.MainActivity;
import com.lingdong20.R;

import org.json.JSONException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import offlinefiles.Offline_Files_Choose_Activity;

public class Files_Trans_Activity extends Activity {

    private long mExitTime;
    private TextView tvMsg;
    private EditText txtIP, txtPort, txtEt;
    private Button btnSend;
    private Handler handler3;
    private ServerSocket server;
    private Socket_Manager socket_Manager;
    private ProgressDialog progressDialog;
    private ProgressDialog recevieProgressDialog;
    private boolean out_recieve = true;
    private String File_Name = null;

    //判断文件是否发送成功
    public boolean fileTransTrueOrFalse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files_trans_main);
        FilesTransActivityContent.mContent = Files_Trans_Activity.this;
        out_recieve = true;
        tvMsg = (TextView) findViewById(R.id.tvMsg);
        txtIP = (EditText) findViewById(R.id.txtIP);

        //将对方的IP地址直接写入txtIP这个EditText控件
        txtIP.setText(MainActivity.IP_DuiFangde);

        txtPort = (EditText) findViewById(R.id.txtPort);
        //txtEt = (EditText) findViewById(R.id.et);
        btnSend = (Button) findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Offline_Files_Choose_Activity.class);// 启动文件管理
                startActivityForResult(intent, 0);
            }
        });
        //打开进度条
        handler3 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    //跳出进度条
                    case 0:
                        showRecieveProgressDialog();
                        break;
                    case 1:
                        tvMsg.setText(msg.obj.toString());
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    //发送结束后返回3，进度条显示完成，去除误差
                    case 3:
                        recevieProgressDialog.setProgress(100);
                        break;
                }
            }

        };
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                // 绑定端口
                int port = 9999;
                while (port > 9000) {
                    try {
                        server = new ServerSocket(port);// 初始化server
                        break;
                    } catch (Exception e) {
                        port--;
                    }
                }

                if (server != null) { // 如果server不空
                    socket_Manager = new Socket_Manager(server);// 初始化socketManager
                    Message.obtain(handler3, 1, "本机IP地址：" + GetIpAddress()/* + " 监听端口:" + port */).sendToTarget();// 不知道这句干嘛
                    while (true) { // 接收文件，死循环
//							if (!out_recieve){
//								out_recieve=true;
//								break;
//							}
                        socket_Manager.ReceiveFile();// 定义一个字符串response

                        // Message.obtain(handler3, 0, response).sendToTarget();
                    }
                } else {
                    Message.obtain(handler3, 1, "未能绑定端口").sendToTarget();
                }
            }
        });
        listener.start();
    }





    @Override
    public void finish() {

        //退出这个页面时，清空IP_DuiFangde的值
        MainActivity.IP_DuiFangde = null;

        super.finish();


    }







    //存储传输文件的名字以及后缀名
    public static String Trans_File_Name = "";
    public static String Trans_File_Type = "";
    public static int Trans_File_Size;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 选择了文件发送

        if (resultCode == RESULT_OK) {

            showProgressDialog();// 显示进度条

            final String fileName = data.getStringExtra("FileName");
            final String path = data.getStringExtra("FilePath");

            Trans_File_Name = fileName;
            if ((fileName.indexOf(".")) > 0) {
                Trans_File_Type = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
            }

            //Trans_File_Size =

            System.out.println("00000000000000000000000000000000000000000000000000000000000000000000" + Trans_File_Name + Trans_File_Type + Trans_File_Size);

            //final String ipAddress = data.getStringExtra("IP_DuiFangde");

            //txtIP.setText(MainActivity.IP_DuiFangde);

            final String ipAddress = txtIP.getText().toString();
            final int port = Integer.parseInt(txtPort.getText().toString());

            String response = socket_Manager.SendFile(fileName, path, ipAddress, port);
            System.out.println("00000000000000000000000000000000000000000000000000000000000000000000显示的IP为：" + MainActivity.IP_DuiFangde);


            //String response = socket_Manager.SendFile(fileName, path, MainActivity.IP_DuiFangde, port);

        }
    }

//	@Override
//	protected void onDestroy() {
//		out_recieve = false;
//		super.onDestroy();
//
//		finish();
//		// System.exit(0);//注释掉。。。。
//	}

    // 点击两次退出传送界面
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if ((System.currentTimeMillis() - mExitTime) > 2000) {
//				Object mHelperUtils;
//				Toast.makeText(this, "再按一次退出传送界面", Toast.LENGTH_SHORT).show();
//				mExitTime = System.currentTimeMillis();
//
//			} else {
//				//android.os.Process.killProcess(android.os.Process.myPid()); /**杀死这个应用的全部进程*/
////				onDestroy();
//			}
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}

    public String GetIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int i = wifiInfo.getIpAddress();
        String a = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
        String b = "0.0.0.0";
        if (a.equals(b)) {
            a = "192.168.43.1";// 当手机当作WIFI热点的时候，自身IP地址为192.168.43.1
        }
        return a;
    }

    /**
     * 进度条对话框
     * 显示发送当前进度
     */
    protected void showProgressDialog() {

        progressDialog = new ProgressDialog(FilesTransActivityContent.mContent);
        progressDialog.setTitle("正在发送...");
        // 设置进度条样式
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置进度条最大值
        progressDialog.setMax(100);
        // 完成按钮
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "完成", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dialog 里的 点击事件
                //Toast.makeText(getApplicationContext(), "发送完成", 0).show();
                progressDialog.dismiss();

            }
        });
        // 取消按钮
        // progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new
        // DialogInterface.OnClickListener() {
        //
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        // // dialog 里的 点击事件
        // progressDialog.dismiss();
        // }
        // });
        //
        if (!FilesTransActivityContent.mContent.isFinishing()) {
            progressDialog.show();
        }


    }

    //显示接收进度
    protected void showRecieveProgressDialog() {

        recevieProgressDialog = new ProgressDialog(FilesTransActivityContent.mContent);
        recevieProgressDialog.setTitle("正在接收   "+File_Name+"...");
        // 设置进度条样式
        recevieProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置进度条最大值
        recevieProgressDialog.setMax(100);
        // 完成按钮
        recevieProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "完成", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dialog 里的 点击事件
                //Toast.makeText(getApplicationContext(), "接受完成", 0).show();
                recevieProgressDialog.dismiss();
            }
        });
        if (!FilesTransActivityContent.mContent.isFinishing()) {
            recevieProgressDialog.show();
        }

    }

    private class Socket_Manager {
        private ServerSocket server;
        private int currentProcess;
        private int pgs;
        private int length;
        private double sumL;
        private byte[] sendBytes;
        private Socket socket;
        private DataOutputStream dos;
        private FileInputStream fis;
        private boolean bool;

        public Socket_Manager(ServerSocket server) {
            this.server = server;
        }


        // 接收文件
        public void ReceiveFile() {

            try {
                // 接收文件名
                Socket socket = server.accept();
                //接受到的文件存放在SD卡LingDong目录下面
                String pathdir = Environment.getExternalStorageDirectory().getPath() + "/LingDong";
                byte[] inputByte = null;
                long length = 0;
                DataInputStream dis = null;
                FileOutputStream fos = null;
                String filePath;
                long L;

                try {


                    dis = new DataInputStream(socket.getInputStream());
                    File f = new File(pathdir);
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    File_Name = dis.readUTF();
                    filePath = pathdir + "/" + File_Name;

                    fos = new FileOutputStream(new File(filePath));
                    inputByte = new byte[1024];
                    L = f.length();
                    System.out.println("文件路径：" + filePath);
                    // System.out.println(dis.readLong());
                    double rfl = 0;
                    L = dis.readLong();
                    System.out.println("文件长度" + L + "kB");
                    System.out.println("开始接收数据...");
                    //弹出进度条信号
                    handler3.sendEmptyMessage(0);

                    while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
                        rfl += length;
                        fos.write(inputByte, 0, (int) length);
                        pgs = (int) (rfl * 100 / 1024.0 / L);
                        //实时更新进度条
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                recevieProgressDialog.setProgress(pgs);

                            }
                        });


                        System.out.println("rfl:" + rfl);
                        System.out.println("psg:" + pgs);
                        fos.flush();

                    }
                    fos.close();
                    dis.close();
                    socket.close();
                    System.out.println("完成接收：" + filePath);
                    //接受完成信号
                    handler3.sendEmptyMessage(3);
                    pgs = 0;
                    // return "完成接收：" + filePath;


                } catch (Exception e) {
                    e.printStackTrace();
                }
                // return "完成接收：" + dis.readUTF();
            } catch (Exception e) {
                e.printStackTrace();
                // return "接收错误";
            }

        }

        public String SendFile(String fileName, final String path, final String ipAddress, final int port) {

            length = 0;
            sumL = 0;
            sendBytes = null;
            socket = null;
            dos = null;
            fis = null;
            bool = false;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        File file = new File(path); // 要传输的文件路径
                        long l = file.length();
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(ipAddress, port));
                        dos = new DataOutputStream(socket.getOutputStream());
                        fis = new FileInputStream(path);
                        sendBytes = new byte[1024];
                        dos.writeUTF(file.getName());// 传递文件名
                        dos.flush();
                        dos.writeLong((long) file.length() / 1024 + 1);
                        dos.flush();

                        while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                            sumL += length;
                            currentProcess = (int) ((sumL / l) * 100);
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    progressDialog.setProgress(currentProcess);

                                }
                            });
                            System.out.println("currentProcess" + currentProcess);
                            System.out.println("已传输：" + ((sumL / l) * 100) + "%");
                            dos.write(sendBytes, 0, length);
                            dos.flush();
                        }
                        //记录文件的大小
                        Trans_File_Size = (int) sumL;
                        //更改判断文件发送成功与否的标志位
                        fileTransTrueOrFalse = true;

                        /*************************************文件发送成功后，向数据库里面记录一条数据******************************/
                        if (fileTransTrueOrFalse) {
                            MainActivity.add_User_Using_Files_Trans_Android();
                        }

                        /*************************文件发送完成后，将数据库中的文件发送信息的数据发送到服务端，并清空数据表****************************/
                        try {
                            //要首先判断这个数据表是不是为空，即当没有进行文件传输的时候，这个表应该是空的，如果这个时候仍然执行，那么应用就会闪退
                            Cursor cursor = MainActivity.dbWriter.query(MainActivity.lingdongdb.TABLE_User_Using_Files_Trans_Android, null, null, null, null, null, null);
                            cursor.getCount();
                            if (cursor.getCount() > 0) {
                                MainActivity.get_User_Using_Files_Trans_Android();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // 虽然数据类型不同，但JAVA会自动转换成相同数据类型后在做比较
                        if (sumL == l) {
                            bool = true;
                        }

                    } catch (Exception e) {
                        System.out.println("客户端文件传输异常");
                        bool = false;
                        e.printStackTrace();
                    } finally {
                        if (dos != null)
                            try {
                                dos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        if (fis != null)
                            try {
                                fis.close();
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
            System.out.println(bool ? "成功" : "失败");
            return fileName + " 发送完成";


        }


    }

}
