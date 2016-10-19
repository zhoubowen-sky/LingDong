/**
 * 这是APP的MainActivity，当然，前面还有启动界面
 */

package com.lingdong20;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import connect.Constant;
import connect.WifiAdmin;
import connect.WifiApAdmin;
import connectpc.Connect_PC;
import database.LingDongDB_Upload;
import feedback.FeedBack;
import filesmanage.Files_Manage_Activity;
import filestrans.Files_Trans_Activity;
import offlinefiles.HttpThread_DownLoad;
import offlinefiles.HttpThread_UpLoad;
import offlinefiles.Offline_Files_Choose_Activity;
import database.LingDongDB;

    public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public boolean UdpReceiveOut = true;//8秒后跳出udp接收线程
    /**LingDongRootFolder此程序自己的文件目录*/
    String LingDongRootFolder = "/sdcard/LingDong/";
    /**发送离线文件的按钮*/
    private Button btnSend_offlinefiles;
    /**弹出对话框下载离线文件的按钮**/
    private Button btnDown_offlinefiles;
    /**点两次返回按键退出程序的时间*/
    private long mExitTime;
    /**显示离线文件传输的日志提醒的Textview，默认情况下文本为空*/
    public static TextView offline_trans_log;
    /**在MainActivity声明两个Fab按钮，类FloatingActionButton是引入自开源库library*/
    private com.getbase.floatingactionbutton.FloatingActionButton fab_CreateConnection;
    private com.getbase.floatingactionbutton.FloatingActionButton fab_ScanToJoin;
    private static String LOG_TAG = "WifiBroadcastActivity";
    private boolean wifiFlag = true;//扫描wifi的子线程的标志位，如果已经连接上正确的wifi热点，线程将结束
    private String address;
    private WifiAdmin wifiAdmin;
    private ArrayList<String> arraylist = new ArrayList<String>();
    private ArrayAdapter adapter;
    private boolean update_wifi_flag = true;
    String ip;
    private ListView listView;
    public static final int DEFAULT_PORT = 43708;
    private static final int MAX_DATA_PACKET_LENGTH = 40;
    private byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];
    public boolean run = false;//判断是否接收到TCP返回，若接收到则不再继续接受
    public boolean show = false;//判断是否是由于超时而退出线程，若是则显示dialog
    private static boolean tcpout = false;
    private boolean a = false;
    private com.getbase.floatingactionbutton.FloatingActionsMenu multiple_actions;
    //开启wifi ... ...
    private WifiManager wifiManager = null;
    /**********************************************************************************************/
    private ImageView iv_scanning;
    private android.support.v4.widget.DrawerLayout rl_root;
    /*********************UdpReceive线程**********************/

    Socket socket = null;
    static DatagramSocket udpSocket = null;
    static DatagramPacket udpPacket = null;
    private boolean udpout = false;
    /*******************************************************/
    //用以存储传送到文件发送界面的IP，即接收方的IP
    public static String IP_DuiFangde;

    /*********************************LingDongDB**************************************/
    public static LingDongDB lingdongdb;  //声明本app的数据库
    public static SQLiteDatabase dbWriter;
    public static String Device_ID = "";

    /********************************************************************************/

    /**
     * 声明Handler用来接收并处理子线程的消息并更新UI界面
     */
    public static Handler handler = new Handler() {
        private int process = 0;//用作离线上传下载的进度条，但是还没加

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://更新上传进度，用以上传进度条
                    process += 1;
                    offline_trans_log.setText("正在上传..." + process + "%");//在主线程中更新UI界面
                    break;
                case 1://更新下载进度，用以下载进度条
                    process += 1;
                    offline_trans_log.setText("正在下载..." + process + "%");//在主线程中更新UI界面
                    break;
                case 2://提示下载完成
                    offline_trans_log.setText("编号" + HttpThread_DownLoad.DownLoadNumber + "的文件下载完成，存储路径为SD卡下LingDong目录！");//在主线程中更新UI界面
                    break;
                case 3://提示上传完成并返回文件提取码
                    offline_trans_log.setText(HttpThread_UpLoad.result);//在主线程中更新UI界面
                    break;
                case 4://输入文件提取码后，PHP服务器返回的字符串信息  /**没有输入文件提取码！*/
                    offline_trans_log.setText("没有输入文件提取码！");//在主线程中更新UI界面
                    break;
                case 7://输入文件提取码后，PHP服务器返回的字符串信息  /**此文件提取码无效！*/
                    offline_trans_log.setText("此文件提取码无效！");//在主线程中更新UI界面
                    break;
                case 5://选择上传的文件后，显示正在上传字样
                    offline_trans_log.setText("正在上传... ...请稍等");//在主线程中更新UI界面
                    break;
                case 6://选择上传的文件后，显示正在下载字样
                    offline_trans_log.setText("正在下载... ...请稍等");//在主线程中更新UI界面
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * handler用于子线程更新
     */
    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    fab_CreateConnection.setEnabled(true);
                    fab_ScanToJoin.setEnabled(true);
                    offline_trans_log.setText("并发送请求到此设备" + "UDP接受已关闭");
                    break;
                case 2:
                    UdpReceiveOut = false;
                    if (!udpout) {
                        Log.i("tag", "00000000000000000000000000000000000000000000000000000000000");
                        if (setWifiApEnabled(true)) {
                            //热点开启成功
                            Log.i("tag", "111111111111111111111111111111111111111111111111111");
                            Toast.makeText(getApplicationContext(), "WIFI热点开启成功,热点名称为:" + Constant.HOST_SPOT_SSID + ",密码为：" + Constant.HOST_SPOT_PASS_WORD, Toast.LENGTH_LONG).show();
                            //这里可以设置为当用户连接到自己开的热点后，就跳转到文件发送界面，并将连接到自己热点设备的IP传过去
                            //getConnectDeviceIP()返回的值前面自带IP加一个回车 字样，如IP 192.168.0.111 所以需要截取一下才可以
                            Log.i("tag", "2222222222222222222222222222222222222222");
                            fab_CreateConnection.setEnabled(true);
                            fab_ScanToJoin.setEnabled(true);
                            startNew startNew = new startNew();
                            startNew.start();

                        } else {
                            //热点开启失败
                            Toast.makeText(getApplicationContext(), "WIFI热点开启失败", Toast.LENGTH_LONG).show();
                            offline_trans_log.setText("WIFI热点开启失败");
                        }
                    }
                    break;
                case 3:
                    if (!udpout) {
                        Toast.makeText(MainActivity.this, "局域网内未搜索到设备，将自动启用热点模式分享文件", Toast.LENGTH_SHORT).show();
                        offline_trans_log.setText("局域网内未搜索到设备，将自动启用热点模式分享文件");
                    }
                    break;
                default:
                    offline_trans_log.setText("查找到可用设备，其IP为：" + msg.obj + "\n");

                    if (MainActivity.isIp((String) (msg.obj))) {
                        Toast.makeText(MainActivity.this, "这是一个标准IP，地址为：" + msg.obj, Toast.LENGTH_LONG).show();
                        IP_DuiFangde = (String) msg.obj;
                        //跳转到文件发送界面
                        Log.i("TAG", "111111111111111111111111111111111111111");
                        Intent intent_filetrans = new Intent(MainActivity.this, Files_Trans_Activity.class);
                        startActivity(intent_filetrans);
                    } else {
                        Toast.makeText(MainActivity.this, "这不是一个标准IP，内容为：" + msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    };


    /**
     * Android6.0 获取更改系统设置的权限，app用了其他的方式，这段代码没有用到，删除也可以的
     */
    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有WRITE_SETTINGS权限
            if (!Settings.System.canWrite(this)) {
                // 申请WRITE_SETTINGS权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }
        }
    }


    /**获取连接到手机热点设备的IP*/
    StringBuilder resultList;
    ArrayList<String> connectedIP;

    public String getConnectDeviceIP() {

        try {
            connectedIP = getConnectIp();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        resultList = new StringBuilder();
        for (String ip : connectedIP) {
            resultList.append(ip);
            resultList.append("\n");
            try {
                connectedIP = getConnectIp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String textString = resultList.toString();
        return textString;

    }

    //从系统/proc/net/arp文件中读取出已连接的设备的信息
    //获取连接设备的IP
    private ArrayList<String> getConnectIp() throws Exception {
        ArrayList<String> connectIpList = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4) {
                String ip = splitted[0];
                connectIpList.add(ip);
            }
        }
        return connectIpList;
    }


    /** wifi热点开关的方法*/
    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = Constant.HOST_SPOT_SSID;
            //配置热点的密码
            apConfig.preSharedKey = Constant.HOST_SPOT_PASS_WORD;

            /***配置热点的其他信息  加密方式**/
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            //用WPA密码方式保护
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }


    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    if (!a) {
                        Toast.makeText(MainActivity.this, "局域网内没有搜索到可用设备，正在热点模式下搜索设备", Toast.LENGTH_LONG).show();
                        tcpout = true;
                        //更新并显示WIFI列表，此时还需要判断WIFI是否打开，可以直接写在UpdateWifiList()里面
                        // UpdateWifiList(0);
                    }
                    a = false;
                    break;
                case 8:
                    fab_ScanToJoin.setEnabled(true);
                    fab_CreateConnection.setEnabled(true);
                    break;
                case 9:
                    UpdateWifiList(1);
                    break;
                case 10:
                    update_wifi_flag=false;
                    break;
                default:
                    tcpout = true;
                    offline_trans_log.append("查找到可用设备，其IP为：" + msg.obj + "\n");

                    System.out.println("00000000000000000000000000000000000000000000000000000000000000000000000" + msg.obj);

                    if (isIp((String) (msg.obj))) {
                        Toast.makeText(MainActivity.this, "这是一个IP，地址为：" + (msg.obj), Toast.LENGTH_LONG).show();

                        IP_DuiFangde = (String) (msg.obj);
                        //跳转到文件发送界面
                        Log.i("TAG", "2222222222222222222222222222222");
                        Intent intent_filetrans = new Intent(MainActivity.this, Files_Trans_Activity.class);
                        startActivity(intent_filetrans);

                    } else {
                        Toast.makeText(MainActivity.this, "这不是一个IP，内容为：" + msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

    };


    /***************************************************************************************************************/
    private Handler handler4 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    if (!a) {
                        Toast.makeText(MainActivity.this, "局域网内没有搜索到可用设备，正在热点模式下搜索设备", Toast.LENGTH_LONG).show();
                        tcpout = true;
                        //更新并显示WIFI列表，此时还需要判断WIFI是否打开，可以直接写在UpdateWifiList()里面
                        // UpdateWifiList(0);
                    }
                    a = false;
                    break;
                case 8:
                    fab_ScanToJoin.setEnabled(true);
                    fab_CreateConnection.setEnabled(true);
                    break;
                case 9:
                    UpdateWifiList(1);
                    break;
                case 10:
                    update_wifi_flag=false;
                    break;
                default:
                    tcpout = true;
                    offline_trans_log.append("查找到可用设备，其IP为：" + msg.obj + "\n");

                    System.out.println("00000000000000000000000000000000000000000000000000000000000000000000000" + msg.obj);

                    if (isIp((String) (msg.obj))) {
                        Toast.makeText(MainActivity.this, "这是一个IP，地址为：" + (msg.obj), Toast.LENGTH_LONG).show();

                        IP_DuiFangde = (String) (msg.obj);
                        //跳转到文件发送界面
                        Log.i("TAG", "2222222222222222222222222222222");
                        Intent intent_filetrans = new Intent(MainActivity.this, Connect_PC.class);
                        startActivity(intent_filetrans);

                    } else {
                        Toast.makeText(MainActivity.this, "这不是一个IP，内容为：" + msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }

    };
    /*********************************************************************************************************************8/

/*******************************************************************app数据存储的核心代码*************************************************************************/

    /***
     * 添加用户信息的数据表的方法，这个方法应该在onCreate中调用，即app一点开就调用
     **/
    public void add_User_Info_Android() {
        ContentValues cv = new ContentValues();
        cv.put(lingdongdb.Device_ID, getDeviceID());
        cv.put(lingdongdb.Android_Version, getAndroidVersion());
        cv.put(lingdongdb.Device_Brand, android.os.Build.BRAND);
        cv.put(lingdongdb.Device_Model, android.os.Build.MODEL);
        cv.put(lingdongdb.Device_Memory, getTotalMemory());
        cv.put(lingdongdb.Device_CPU, getCpuName());
        cv.put(lingdongdb.Device_Screen_Resolution, getScreenResolution());
        dbWriter.insert(lingdongdb.TABLE_User_Info_Android, null, cv);
    }

    /***
     * 添加用户使用app时间的数据表的方法，这个方法应该在onCreate中调用，即app一点开就调用，退出app时也调用
     **/
    public void add_User_Using_Time_Android() {
        ContentValues cv = new ContentValues();
        cv.put(lingdongdb.Device_ID, getDeviceID());
        cv.put(lingdongdb.Start_APP_Time, getCurrentTime());
        cv.put(lingdongdb.Exit_APP_Time, getCurrentTime());
        cv.put(lingdongdb.Holding_APP_Time, "0");
        dbWriter.insert(lingdongdb.TABLE_User_Using_Time_Android, null, cv);
    }

    /**
     * 更新用户退出app的以及停留在app的时间，这个方法应该在应用退出时调用
     **/
    public void update_User_Using_Time_Android() throws ParseException {
        ContentValues cv = new ContentValues();
        cv.put(lingdongdb.Exit_APP_Time, getCurrentTime());
        cv.put(lingdongdb.Holding_APP_Time, get_User_Holding_APP_Time());
        String whereClause = "_id = ?";//修改条件
        String[] whereArgs = {"1"};//修改条件的参数
        dbWriter.update(lingdongdb.TABLE_User_Using_Time_Android, cv, whereClause, whereArgs);
    }

    /**
     * 获取用户停留在APP的时间长度，单位设定为秒，应该先读取数据库中第一条数据，即_id=1的数据行中用户启动app时的时间，与当前系统时间相减取差值即可
     */
    public int get_User_Holding_APP_Time() throws ParseException {
        //查询获得游标
        Cursor cursor = dbWriter.query(lingdongdb.TABLE_User_Using_Time_Android, null, null, null, null, null, null);
        //将游标移动到第一行，游标就是指针
        cursor.moveToFirst();
        //获取第一行数据的_id
        int _id = cursor.getInt(0);
        //获取start_app_time的值
        String exit_app_time = cursor.getString(2);
        SimpleDateFormat sDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sDate.parse(exit_app_time);
        Date curDate = new Date(System.currentTimeMillis());
        long start_app = date.getTime();
        long exit_app = curDate.getTime();
        long holding_app_time = (exit_app - start_app) / (1000);
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%00000%%%%%%%%%%%%%%%" + _id + start_app);
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%00000%%%%%%%%%%%%%%%" + _id + holding_app_time);
        return (int) holding_app_time;
    }


    /****
     * 添加用户对app不同功能模块使用频率统计的数据表的方法，这个方法应该在onCreate中调用，即app一点开就调用，每当用户点击一个不同的功能模块，数据表中相应模块字段的值就加一
     *****/
    public void add_User_Using_Modules_Times_Android() {
        ContentValues cv = new ContentValues();
        cv.put(lingdongdb.Device_ID, getDeviceID());
        cv.put(lingdongdb.Offline_Files_Upload, 0);
        cv.put(lingdongdb.Offline_Files_Download, 0);
        cv.put(lingdongdb.Bluetooth_Trans, 0);
        cv.put(lingdongdb.Share_APP, 0);
        cv.put(lingdongdb.Files_Manage, 0);
        cv.put(lingdongdb.User_Feedback, 0);
        cv.put(lingdongdb.Software_Version, 0);
        cv.put(lingdongdb.Software_Describe, 0);
        cv.put(lingdongdb.About_Us, 0);
        cv.put(lingdongdb.User_Android_Version, 0);
        cv.put(lingdongdb.Connect_PC, 0);
        cv.put(lingdongdb.Create_Connection, 0);
        cv.put(lingdongdb.Scan_To_Join, 0);
        dbWriter.insert(lingdongdb.TABLE_User_Using_Modules_Times_Android, null, cv);
    }

    /****
     * 更新用户点击app不同功能模块时，统计使用频率的数据表，这个方法应该在点击相应模块时时调用
     ****/
    public static void update_User_Using_Modules_Times_Android(String modules) {

        //查询获得游标
        Cursor cursor = dbWriter.query(lingdongdb.TABLE_User_Using_Modules_Times_Android, null, null, null, null, null, null);
        //将游标移动到第一行，游标就是指针
        cursor.moveToFirst();
        //通过参数来确定要更新的是哪一个字段的数据
        int a = cursor.getColumnIndexOrThrow(modules);
        System.out.println("0000000000000000000000000000000000000啊啊啊啊" + a);
        //获取指定列，存储点击次数的数字
        int num = cursor.getInt(a);
        num++;//每点击一次就加一
        //将加一后的num存入数据库
        String sql = "update " + LingDongDB.TABLE_User_Using_Modules_Times_Android + " set " + modules + " = " + num + " where _id = 1";
        System.out.println("0000000000000000000000000000000000000" + sql);
        dbWriter.execSQL(sql);
    }

    /***
     * 添加用户在安卓与安卓，安卓与电脑之间传输的文件的文件信息的数据表的内容
     **/
    public static void add_User_Using_Files_Trans_Android() {
        ContentValues cv = new ContentValues();
        cv.put(MainActivity.lingdongdb.Device_ID, MainActivity.Device_ID);
        cv.put(MainActivity.lingdongdb.Files_Name, Files_Trans_Activity.Trans_File_Name);
        cv.put(MainActivity.lingdongdb.Files_Type, Files_Trans_Activity.Trans_File_Type);
        cv.put(MainActivity.lingdongdb.Files_Size, Files_Trans_Activity.Trans_File_Size);
        cv.put(MainActivity.lingdongdb.Trans_Time, MainActivity.getCurrentTime());
        MainActivity.dbWriter.insert(MainActivity.lingdongdb.TABLE_User_Using_Files_Trans_Android, null, cv);
    }

    /**
     * 获取系统当前的时间
     */
    public static String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date();
        String str = format.format(curDate);
        return str;
    }

    /**
     * 获取设备唯一的标志码
     */
    public String getDeviceID() {
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        return imei;
    }

    public String getAndroidVersion() {
        String AndroidVersion = android.os.Build.VERSION.RELEASE;
        return AndroidVersion;
    }

    public String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取安卓手机RAM
     */
    public String getTotalMemory() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(getBaseContext(), initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }

    /**
     * 获取屏幕分辨率
     **/
    public String getScreenResolution() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        String strOpt = dm.widthPixels + " * " + dm.heightPixels;
        return strOpt;
    }

    /**
     * 这里写的是将数据表 User_Info_Android 读取出来转换为Json格式的方法,返回的参数为 JSONObject
     */
    public JSONObject get_User_Info_Android_Data_to_JSON() throws JSONException {
        //查询获得游标
        Cursor cursor = dbWriter.query(lingdongdb.TABLE_User_Info_Android, null, null, null, null, null, null);
        //将游标移动到第一行，游标就是指针
        cursor.moveToFirst();
        //创建JSON对象，并向其中添加数据
        JSONObject user_info_android = new JSONObject();
        user_info_android.put("_id", cursor.getInt(0));
        user_info_android.put("device_id", cursor.getString(1));
        user_info_android.put("android_version", cursor.getString(2));
        user_info_android.put("device_brand", cursor.getString(3));
        user_info_android.put("device_model", cursor.getString(4));
        user_info_android.put("device_memory", cursor.getString(5));
        user_info_android.put("device_CPU", cursor.getString(6));
        user_info_android.put("device_screen_resolution", cursor.getString(7));

        System.out.println(user_info_android.toString());
        return user_info_android;
    }

    /**
     * 这里写的是将数据表 User_Using_Time_Android 读取出来转换为Json格式的方法,返回的参数为 JSONObject
     */
    public JSONObject get_User_Using_Time_Android() throws JSONException {
        //查询获得游标
        Cursor cursor = dbWriter.query(lingdongdb.TABLE_User_Using_Time_Android, null, null, null, null, null, null);
        //将游标移动到第一行，游标就是指针
        cursor.moveToFirst();
        //创建JSON对象，并向其中添加数据
        JSONObject user_info_android = new JSONObject();
        user_info_android.put("_id", cursor.getInt(0));
        user_info_android.put("device_id", cursor.getString(1));
        user_info_android.put("start_app_time", cursor.getString(2));
        user_info_android.put("exit_app_time", cursor.getString(3));
        user_info_android.put("holding_app_time", cursor.getString(4));

        System.out.println(user_info_android.toString());
        return user_info_android;
    }

    /**
     * 这里写的是将数据表 User_Using_Modules_Times_Android 读取出来转换为Json格式的方法,返回的参数为 JSONObject
     */
    public JSONObject get_User_Using_Modules_Times_Android() throws JSONException {
        //查询获得游标
        Cursor cursor = dbWriter.query(lingdongdb.TABLE_User_Using_Modules_Times_Android, null, null, null, null, null, null);
        //将游标移动到第一行，游标就是指针
        cursor.moveToFirst();
        //创建JSON对象，并向其中添加数据
        JSONObject user_info_android = new JSONObject();
        user_info_android.put("_id", cursor.getInt(0));
        user_info_android.put("device_id", cursor.getString(1));
        //以下是app基本的十三个模块
        user_info_android.put("offline_files_upload", cursor.getInt(2));
        user_info_android.put("offline_files_download", cursor.getInt(3));
        user_info_android.put("bluetooth_trans", cursor.getInt(4));
        user_info_android.put("share_app", cursor.getInt(5));
        user_info_android.put("files_manage", cursor.getInt(6));
        user_info_android.put("user_feedback", cursor.getInt(7));
        user_info_android.put("software_version", cursor.getInt(8));
        user_info_android.put("software_describe", cursor.getInt(9));
        user_info_android.put("about_us", cursor.getInt(10));
        user_info_android.put("user_android_version", cursor.getInt(11));
        user_info_android.put("connect_PC", cursor.getInt(12));
        user_info_android.put("create_connection", cursor.getInt(13));
        user_info_android.put("scan_to_join", cursor.getInt(14));

        System.out.println(user_info_android.toString());
        return user_info_android;
    }

    /**这里写的是将数据表 User_Using_Files_Trans_Android 读取出来转换为Json格式的方法,返回的参数为 JSONObject ,这里面可能会有多条数据，因而与上面会有不同*/
    /**
     * 唉~~~  算了 想用一个通用方法搞定的，看来这个这个不行，将其设定为  读取一条数据 上传 再读取下一条 数据 上传 ，而不是一次读取所有，然后一次性上传
     * 这个方法暂时写的有点问题，先搞定其他的表格再说吧
     * 方法已经重写 没有问题了
     **/
    public static void get_User_Using_Files_Trans_Android() throws JSONException {
        //查询获得游标
        Cursor cursor = dbWriter.query(lingdongdb.TABLE_User_Using_Files_Trans_Android, null, null, null, null, null, null);
        //String ddd = "http://192.168.1.147/OfflineTrans/DatabaseScript/User_Using_Files_Trans_Android.php";
        String ddd = "http://115.28.101.196/DatabaseScript/User_Using_Files_Trans_Android.php";
        JSONObject user_info_android = new JSONObject();//创建JSON对象，并向其中添加数据

        //将游标移动到第一行，游标就是指针
        cursor.moveToFirst();
        user_info_android.put("_id", cursor.getInt(0));           //获取第一列的值
        user_info_android.put("device_id", cursor.getString(1));  //获取第二列的值
        user_info_android.put("files_name", cursor.getString(2)); //获取第三列的值
        user_info_android.put("files_type", cursor.getString(3));
        user_info_android.put("files_size", cursor.getString(4));
        user_info_android.put("trans_time", cursor.getString(5));

        System.out.println(user_info_android.toString());
        /**启动数据上传的线程，*/
        new LingDongDB_Upload(ddd, user_info_android).start();
        System.out.println("线程启动成功---------------------------------------------------------------成功");

        //清空User_Using_Files_Trans_Android数据表
        dbWriter.execSQL("DELETE FROM user_using_files_trans_android");
        //自增长ID设置为0
        //dbWriter.execSQL("DELETE FROM sqlite_sequence");

       /* //将游标移动到第一行，游标就是指针
        if ((cursor != null) && cursor.moveToFirst()){
            String ddd = "http://192.168.1.147/OfflineTrans/DatabaseScript/User_Using_Files_Trans_Android.php";

            do {

                user_info_android.put("_id",cursor.getInt(0));           //获取第一列的值
                user_info_android.put("device_id",cursor.getString(1));  //获取第二列的值
                user_info_android.put("files_name",cursor.getString(2)); //获取第三列的值
                user_info_android.put("files_type",cursor.getString(3));
                user_info_android.put("files_size",cursor.getString(4));
                user_info_android.put("trans_time",cursor.getString(5));

                System.out.println(user_info_android.toString());

                new LingDongDB_Upload(ddd,user_info_android).start();

                //new LingDongDB_Files_Trans_Records_Upload(ddd,user_info_android);

                System.out.println("线程启动成功---------------------------------------------------------------成功");

                //sb.append(user_info_android);
                //sb.append(",");

            }while (cursor.moveToNext());

            //sb.deleteCharAt(sb.length()-1);

            //String weiba = "]}";
            //sb.append(weiba);
            //在logcat输出这个值用以检查验证
            //System.out.println(sb.toString());

        }*/

    }
    /**
     * 更新服务端的数据库，即上传用户产生的数据
     */
    public void update_LingDongDB() {

        try {
            //更新用户退出与停留在app的时间
            update_User_Using_Time_Android();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            //String aaa = "http://192.168.1.147/OfflineTrans/DatabaseScript/User_Info_Android.php";//这个事本地测试用的地址
            String aaa = "http://115.28.101.196/DatabaseScript/User_Info_Android.php";
            new LingDongDB_Upload(aaa, get_User_Info_Android_Data_to_JSON()).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*try {
            String ddd = "http://192.168.1.147/OfflineTrans/DatabaseScript/User_Using_Files_Trans_Android.php";
            //要首先判断这个数据表是不是为空，即当没有进行文件传输的时候，这个表应该是空的，如果这个时候仍然执行，那么应用就会闪退
            Cursor cursor = dbWriter.query(lingdongdb.TABLE_User_Using_Files_Trans_Android,null,null,null,null,null,null);
            cursor.getCount();
            if (cursor.getCount() > 0){
                get_User_Using_Files_Trans_Android();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        try {
            //String bbb = "http://192.168.1.147/OfflineTrans/DatabaseScript/User_Using_Time_Android.php";
            String bbb = "http://115.28.101.196/DatabaseScript/User_Using_Time_Android.php";
            new LingDongDB_Upload(bbb, get_User_Using_Time_Android()).start();
            //get_User_Using_Time_Android();

        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            //String ccc = "http://192.168.1.147/OfflineTrans/DatabaseScript/User_Using_Modules_Times_Android.php";
            String ccc = "http://115.28.101.196/DatabaseScript/User_Using_Modules_Times_Android.php";
            new LingDongDB_Upload(ccc, get_User_Using_Modules_Times_Android()).start();
            //get_User_Using_Modules_Times_Android();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    /***********************************************************************
     * app数据存储的核心代码
     **************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /****************************app的背景 逐帧动画**************************************/
        /*ImageView rocketImage = (ImageView) findViewById(R.id.iv);
        rocketImage.setBackgroundResource(R.drawable.my_anim);

        AnimationDrawable rocketAnimation = (AnimationDrawable) rocketImage.getBackground();

        rocketAnimation.start();*/

        /************************************************数据库相关操作***************************************/
        lingdongdb = new LingDongDB(this);
        dbWriter = lingdongdb.getWritableDatabase();

        //应该判断以下数据表是否为空，如果为空就插入一条，如果不为空就不执行操作，即不插入
        Cursor cursor_time = dbWriter.query(lingdongdb.TABLE_User_Using_Time_Android, null, null, null, null, null, null);
        if (cursor_time.getCount() < 1) {
            //这里插入的是用户信息的数据，例如硬件信息等等
            add_User_Info_Android();
        } else { /**什么也不执行*/}

        //应该先判断数据表是否为空，如果为空就插入一条，不为空就对数据表执行更新操作，更新表中的Exit时间以及holding时间
        Cursor cursor_info = dbWriter.query(lingdongdb.TABLE_User_Using_Time_Android, null, null, null, null, null, null);
        if (cursor_info.getCount() < 1) {
            //插入的是用户app使用时间相关统计的数据
            add_User_Using_Time_Android();
        } else { /**什么也不执行*/}

        //应该判断以下数据表是否为空，如果为空就插入一条，如果不为空就不执行操作，即不插入
        Cursor cursor_modules_times = dbWriter.query(lingdongdb.TABLE_User_Using_Modules_Times_Android, null, null, null, null, null, null);
        if (cursor_modules_times.getCount() < 1) {
            //这里插入的是用户对不同模块使用频率的统计数据，默认都为0，没点开一个应该将相应的字段的值加一
            add_User_Using_Modules_Times_Android();
        } else { /**什么也不执行*/}

        Device_ID = getDeviceID();


        /********************************************************数据库相关操作*********************************/

        //android 6.0更改系统设置的权限
        //getPermission();

        /*****************************************************/

        wifiManager = (WifiManager) super.getSystemService(Context.WIFI_SERVICE);
        /*****************************************************/


        /*******************************************/
        //设备之间连接的两个fab的定义以及初始化
        fab_CreateConnection = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_CreateConnection);
        fab_ScanToJoin = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_ScanToJoin);

        multiple_actions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        rl_root = (DrawerLayout) findViewById(R.id.drawer_layout);
        //初始化wifiAdmin
        wifiAdmin = new WifiAdmin(MainActivity.this);
        /**获取设备IP*/
        address = getLocalIPAddress();
        ip = address;
        /**点击开启UDP发送线程*/
        fab_ScanToJoin.setOnClickListener(listener);
        /**点击跳转到开启接受UDP请求界面*/
        fab_CreateConnection.setOnClickListener(listener);


        /*******************************************/
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, arraylist);//初始化adapter


        /******************************************/
        //显示离线文件传输的日志提醒的Textview，offline_trans_log,这个Textview同时也作为fab连接设备时候的文本提醒
        offline_trans_log = (TextView) findViewById(R.id.offline_trans_log);
        /*****************************************/

        //控件初始化，设置监听事件
        btnSend_offlinefiles = (Button) findViewById(R.id.btnSend_offlinefiles);
        btnSend_offlinefiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**用于获取网络状态的代码*/
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                /**先判断网络状态，如果有可用网络，就发送，没有就提示网络不可用*/
                if (networkInfo == null || !networkInfo.isAvailable()) {
                    //当前没有可用网络
                    Toast.makeText(MainActivity.this, "当前网络不可用，无法发送文件", Toast.LENGTH_SHORT).show();
                } else {
                    //当前有可用网络
                    Intent intent = new Intent(getApplicationContext(), Offline_Files_Choose_Activity.class);
                    intent.putExtra("Type", "intenet");
                    startActivityForResult(intent, 0);
                }
            }
        });

        //控件初始化，设置监听事件
        btnDown_offlinefiles = (Button) findViewById(R.id.btnDown_offlinefiles);
        //给下载按钮添加一个监听器
        btnDown_offlinefiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**************************************************************************************************************************/


                /**************************************************************************************************************************/

                /**用于获取网络状态的代码*/
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                /**先判断网络状态，如果有可用网络，就发送，没有就提示网络不可用*/
                if (networkInfo == null || !networkInfo.isAvailable()) {
                    //当前没有可用网络
                    Toast.makeText(MainActivity.this, "当前网络不可用，无法下载文件", Toast.LENGTH_SHORT).show();
                } else {
                    //当前有可用网络
                    //弹出对话框，进行文件提取码的输入，然后下载文件
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("请输入漂流文件提取码：");
                    //通过LayoutInflater来加载一个xml的布局文件作为一个View对象
                    final View Dialogview = LayoutInflater.from(MainActivity.this).inflate(R.layout.offline_files_dialog, null);
                    //设置我们自己定义的布局文件作为弹出框的Content
                    builder.setView(Dialogview);
                    //设置点击下载后的事件
                    builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            update_User_Using_Modules_Times_Android(LingDongDB.Offline_Files_Download);

                            EditText WenJianTiQuMa_Edit = (EditText) Dialogview.findViewById(R.id.WenJianTiQuMa_Edit);
                            String url = "http://115.28.101.196/AndroidDownloadAction.php";
                            //String DownLoadNumber = "52124";
                            String DownLoadNumber = WenJianTiQuMa_Edit.getText().toString();
                            new HttpThread_DownLoad(url, DownLoadNumber).start();//启动文件下载的线程
                        }
                    });

                    //设置点击取消后的事件
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            }
        });


        /**当点开程序的时候，在SDcard目录下面新建一个名为LingDong的文件夹用以存放程序接收到的文件*/
        createMkdir(LingDongRootFolder);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**谷歌自带Fab的设置，将其注释掉是因为我们使用的是第三方的库*/
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }





//                    LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.linearlayout);
//                    Resources resources = getBaseContext().getResources();
//                    Drawable btnDrawable = resources.getDrawable(R.drawable.offline_files_main_background_change1);
//                    relativeLayout.setBackgroundDrawable(btnDrawable);


    //type 0新建wifi列表
    //type 1动态更新wifi列表
    void UpdateWifiList(int type) {
        wifiAdmin.startScan();
        wifiAdmin.lookUpScan();
        arraylist.clear();

        for (ScanResult e : wifiAdmin.getWifiList()) {

            Log.i("TAG", "4444444444444444444555555555555555555");
            //  arraylist.add(e.SSID);
//            !arraylist.contains(e.SSID)
            if (e.SSID.equals("LingDong"))//如果热点名有LingDong且不为空且不重复
            {
                //关闭wifi列表更新
                update_wifi_flag = false;
//                Log.i("TAG","SSID:"+e.SSID );

                //这一段输入密码，现阶段设置为默认123456789
                CreatConnection("LingDong", "123456789", 3);//这里输入密码
                //更新这个IP地址
                IP_DuiFangde = "192.168.43.1";
                //设置点击后跳转到文件发送与接收界面，还要有一个判断，判断点击的是不是LingDong热点，这里暂时就不判断了，后期会更改为只显示LingDong这个热点
                Log.i("TAG", "333333333333333444444444444444");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while(GetIpAddress().equals("192.168.43.1")) {
                                Thread.sleep(500);
                            }
                            Intent intent_filetrans = new Intent(MainActivity.this, Files_Trans_Activity.class);
                            startActivity(intent_filetrans);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();// 空线程延时

                break;
            }
        }

    }
//        if(type == 0)
//        {
//            listView.setAdapter(adapter);
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
//            {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
//                {
//                    //关闭wifi列表更新
//                    update_wifi_flag = false;
//                    //这一段输入密码，现阶段设置为默认123456789
//                    CreatConnection(arraylist.get(position), "123456789", 3);//这里输入密码
//                    //当点击特定的WIFI后，即连接上对方所开的热点后，应该跳转到文件发送与接收的界面，同时还应该获取对方的IP，也就是Android开热点时，手机的一个固定的IP 192.168.43.1
//                    //更新这个IP地址
//                    IP_DuiFangde = "192.168.43.1";
//                    //设置点击后跳转到文件发送与接收界面，还要有一个判断，判断点击的是不是LingDong热点，这里暂时就不判断了，后期会更改为只显示LingDong这个热点
//                    Intent intent_filetrans=new Intent(MainActivity.this, Files_Trans_Activity.class);
//                    startActivity(intent_filetrans);
//                }
//            });
//        }
//        else
//            adapter.notifyDataSetChanged();
//    }

    void CreatConnection(final String name, final String key, final int type) {
        new Thread(new Runnable()//匿名内部类的调用方式
        {
            @Override
            public void run() {
                wifiAdmin.openWifi();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(name, key, type));
                wifiFlag = false;//关闭扫描wifi热点的子线程
            }
        }).start();// 建立链接线程

    }

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
     * 接受返回的TCP消息
     */
    private class TcpReceive implements Runnable {
        public void run() {
            while (true) {
                tcpout = false;
                Socket socket = null;
                ServerSocket ss = null;
                BufferedReader in = null;
                try {
                    Log.i("TcpReceive", "ServerSocket +++++++");
                    ss = new ServerSocket(8080);
                    socket = ss.accept();
                    Log.i("TcpReceive", "connect +++++++");
                    if (socket != null) {
                        run = true;
                        a = true;
                        in = new BufferedReader(new InputStreamReader(
                                socket.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        sb.append(socket.getInetAddress().getHostAddress());
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            sb.append(line);
                        }
                        Log.i("TcpReceive", "connect :" + sb.toString());
                        final String ipString = sb.toString().trim();// "192.168.0.104:8731";
                        Message msg = new Message();
                        msg.obj = ipString;

                        tcpout = true;
                        //这里是IP地址
                        handler2.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null)
                            in.close();
                        if (socket != null)
                            socket.close();
                        if (ss != null)
                            ss.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (tcpout) {
                    break;
                }
            }
        }
    }


    /**
     * 接受返回的TCP消息
     */
    private class TcpReceive2 implements Runnable {
        public void run() {
            while (true) {
                tcpout = false;
                Socket socket = null;
                ServerSocket ss = null;
                BufferedReader in = null;
                try {
                    Log.i("TcpReceive", "ServerSocket +++++++");
                    ss = new ServerSocket(8080);
                    socket = ss.accept();
                    Log.i("TcpReceive", "connect +++++++");
                    if (socket != null) {
                        run = true;
                        a = true;
                        in = new BufferedReader(new InputStreamReader(
                                socket.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        sb.append(socket.getInetAddress().getHostAddress());
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            sb.append(line);
                        }
                        Log.i("TcpReceive", "connect :" + sb.toString());
                        final String ipString = sb.toString().trim();// "192.168.0.104:8731";
                        Message msg = new Message();
                        msg.obj = ipString;

                        tcpout = true;
                        //这里是IP地址
                        handler4.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null)
                            in.close();
                        if (socket != null)
                            socket.close();
                        if (ss != null)
                            ss.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (tcpout) {
                    break;
                }
            }
        }
    }


    /**************************************************************
     * 两个按钮的监听事件
     ***************************************************************/
    private View.OnClickListener listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (v == fab_ScanToJoin) {
                tcpout = false;
                update_wifi_flag=true;
                update_User_Using_Modules_Times_Android(LingDongDB.Scan_To_Join);

                //显示雷达扫描界面
                showPopupWindow();

                //打开线程之前先判断热点是否是开的，如果热点是开的，就关掉热点，然后再开启wifi，如果热点本身是关的，就直接开启WIFI
                /***************以下的判断方法是有错误的，应该重写***********/
                if (WifiApAdmin.isWifiApEnabled(wifiManager)) {
                    WifiApAdmin.closeWifiAp(wifiManager);
                    wifiManager.setWifiEnabled(true);

                    Thread thread = new Thread(new TcpReceive());
                    thread.start();
                    offline_trans_log.setText("正在发送UDP请求，若有连接将在此显示，若五秒钟后没有显示，可以点击再次搜索。。。" + "\n");
                    BroadCastUdp bcu = new BroadCastUdp(address);
                    bcu.start();
                    fab_ScanToJoin.setEnabled(false);
                    fab_CreateConnection.setEnabled(false);
                } else {

                    wifiManager.setWifiEnabled(true);

                    Thread thread = new Thread(new TcpReceive());
                    thread.start();
                    offline_trans_log.setText("正在发送UDP请求，若有连接将在此显示，若五秒钟后没有显示，可以点击再次搜索。。。" + "\n");
                    BroadCastUdp bcu = new BroadCastUdp(address);
                    bcu.start();
                    fab_ScanToJoin.setEnabled(false);
                    fab_CreateConnection.setEnabled(false);
                }
            } else {

                if (WifiApAdmin.isWifiApEnabled(wifiManager)) {
                    WifiApAdmin.closeWifiAp(wifiManager);
                    wifiManager.setWifiEnabled(true);
                }

                update_User_Using_Modules_Times_Android(LingDongDB.Create_Connection);
                //显示雷达扫描界面
                showPopupWindow();
                //点击创建链接的按钮之后隐藏Fab.
                fab_ScanToJoin.setEnabled(false);
                fab_CreateConnection.setEnabled(false);



                //设置"正在接受UDP请求，请求连接的设备将在此显示。。。"的提示
                offline_trans_log.setText("正在接收UDP请求，请求连接的设备将在此显示。。。");
                //设置按钮不可点击
                fab_CreateConnection.setEnabled(false);
                /** 开启UDP接受线程*/
                UdpReceive udpreceive = new UdpReceive();
                udpreceive.start();
            }
        }
    };


    /**
     * 获取本机ip方法
     */
    private String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
        return null;
    }

    /**
     * 监听有没有wifi接入到wifi热点的线程
     */
    public class startNew extends Thread {
        public void run() {
            while (!(getConnectDeviceIP().length() > 6)) {
                //上面getConnectDeviceIP().length() > 6 是用来判断getConnectDeviceIP()这个字符串是否获取了IP地址，不一定非要是6，其余合适的值都行
                IP_DuiFangde = getConnectDeviceIP();


            }

            //当获取到IP后跳出上面的循环，将IP值赋值给变量
            Log.i("tag", "3333333333333333333333333333333333333333");
            IP_DuiFangde = getConnectDeviceIP().substring(3, getConnectDeviceIP().length() - 1);

            //跳转到文件发送界面
            if (isIp(IP_DuiFangde)) {

                //如果是一个IP就跳转到文件发送界面
                Log.i("TAG", "444444444444444444444444444444");
                Intent intent_filetrans2 = new Intent(MainActivity.this, Files_Trans_Activity.class);
                startActivity(intent_filetrans2);
            } else {
                //如果不是一个IP就弹出消息提示一下
                // Toast.makeText(getApplicationContext(), "这不是一个IP，其值为："+IP_DuiFangde, Toast.LENGTH_LONG).show();
            }
            //恢复按钮为可点击
            //设置按钮可点击}

        }

    }

    /**
     * UDP广播线程
     */
    public class BroadCastUdp extends Thread {
        private String dataString;
        private DatagramSocket udpSocket;
        public volatile boolean exit = false;

        public BroadCastUdp(String dataString) {
            this.dataString = dataString;
        }


        public void run() {

            show = false;
            /**计算时间标志*/
            long st = System.currentTimeMillis();
            while (!exit) {
                DatagramPacket dataPacket = null;
                try {

                    if(udpSocket==null){
                        udpSocket = new DatagramSocket(null);
                        udpSocket.setReuseAddress(true);
                        udpSocket.bind(new InetSocketAddress(DEFAULT_PORT));
                    }
                   // udpSocket = new DatagramSocket(DEFAULT_PORT);
                    dataPacket = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
                    byte[] data = dataString.getBytes();
                    dataPacket.setData(data);
                    dataPacket.setLength(data.length);
                    dataPacket.setPort(DEFAULT_PORT);
                    InetAddress broadcastAddr;
                    broadcastAddr = InetAddress.getByName("255.255.255.255");
                    dataPacket.setAddress(broadcastAddr);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
                try {
                    udpSocket.send(dataPacket);
                    sleep(10);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
                udpSocket.close();
                /**计算时间标志*/

                long et = System.currentTimeMillis();
                /**8秒后次线程自动销毁*/
                if ((et - st) > 8000) {
                    show = true;
                    break;
                }
                /**tcp返回值后停止发送udp*/
                Log.i("tag", "show");
                if (run) {
                    run = false;
                    break;
                }
            }
            Log.i("tag", "show");
            if (show) {
                //tcpout = true;
                //Message message = new Message();
                show = false;
                //不再进行UDP发送与接收后，扫描并显示WIFI列表
                handler2.sendEmptyMessage(7);
                // handler.sendMessage(message);
                new Thread(new Runnable()//同时开启一个动态更新wifi列表的线程，直到标志位update_wifi_flag被赋值false
                {
                    @Override
                    public void run() {
                        long st = System.currentTimeMillis();

                        while (update_wifi_flag) {
                            long et = System.currentTimeMillis();
                            Log.i("TAG", "ssssssssssssssssssssssssssssssss"+st);
                            Log.i("TAG", "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"+et);
                            /**10秒后次线程自动销毁*/
                            if ((et - st) > 15000) {
                                handler2.sendEmptyMessage(10);

                            }
                            handler2.sendEmptyMessage(9);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
            handler2.sendEmptyMessage(8);

        }
    }


    /****************************************************************************************************/
    public class BroadCastUdp1 extends Thread {
        private String dataString;
        private DatagramSocket udpSocket;
        public volatile boolean exit = false;

        public BroadCastUdp1(String dataString) {
            this.dataString = dataString;
        }


        public void run() {

            show = false;
            /**计算时间标志*/
            long st = System.currentTimeMillis();
            while (!exit) {
                DatagramPacket dataPacket = null;
                try {

                    if(udpSocket==null){
                        udpSocket = new DatagramSocket(null);
                        udpSocket.setReuseAddress(true);
                        udpSocket.bind(new InetSocketAddress(DEFAULT_PORT));
                    }
                    // udpSocket = new DatagramSocket(DEFAULT_PORT);
                    dataPacket = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
                    byte[] data = dataString.getBytes();
                    dataPacket.setData(data);
                    dataPacket.setLength(data.length);
                    dataPacket.setPort(DEFAULT_PORT);
                    InetAddress broadcastAddr;
                    broadcastAddr = InetAddress.getByName("255.255.255.255");
                    dataPacket.setAddress(broadcastAddr);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
                try {
                    udpSocket.send(dataPacket);
                    sleep(10);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
                udpSocket.close();
                /**计算时间标志*/

                long et = System.currentTimeMillis();
                /**8秒后次线程自动销毁*/
                if ((et - st) > 8000) {
                    show = true;
                    break;
                }
                /**tcp返回值后停止发送udp*/
                Log.i("tag", "show");
                if (run) {
                    run = false;
                    break;
                }
            }
            Log.i("tag", "show");
            if (show) {
                //tcpout = true;
                //Message message = new Message();
                show = false;
                //不再进行UDP发送与接收后，扫描并显示WIFI列表
                handler4.sendEmptyMessage(7);
                // handler.sendMessage(message);
                new Thread(new Runnable()//同时开启一个动态更新wifi列表的线程，直到标志位update_wifi_flag被赋值false
                {
                    @Override
                    public void run() {
                        long st = System.currentTimeMillis();

                        while (update_wifi_flag) {
                            long et = System.currentTimeMillis();
                            Log.i("TAG", "ssssssssssssssssssssssssssssssss"+st);
                            Log.i("TAG", "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"+et);
                            /**10秒后次线程自动销毁*/
                            if ((et - st) > 15000) {
                                handler4.sendEmptyMessage(10);

                            }
                            handler4.sendEmptyMessage(9);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
            handler4.sendEmptyMessage(8);

        }
    }
    /**************************************************************************************************************/
    /**
     * UDP接受线程类
     */
    public class UdpReceive extends Thread {
        public void run() {
            udpout = false;   //判断是否退出UDP接收线程的标志位
            byte[] data = new byte[256];
            try {
                udpSocket = new DatagramSocket(43708);
                udpPacket = new DatagramPacket(data, data.length);
            } catch (SocketException e1) {
                e1.printStackTrace();
            }


            /**8秒后发送消息更新UI*/
            handler1.sendEmptyMessageDelayed(3, 7500);
            handler1.sendEmptyMessageDelayed(2, 8000);//8秒后执行case2 也就是自动开启WIFI热点
            while (true) {
                Log.i("tag", "8888888888888888888888888888888888888888888888888888");


                try {
                    Log.i("tag", "8888888888888888888888888888888888888888888888888888");
                    udpSocket.receive(udpPacket);
                } catch (IOException e) {
                    Log.i("tag", "8888888888888888888888888888888888888888888888888888");
                    e.printStackTrace();
                }

                if (null != udpPacket.getAddress()) {
                    Log.i("tag", "9999999999999999999999999999999999999999999999999");
                    final String quest_ip = udpPacket.getAddress().toString();
                    Message msg = new Message();
                    msg.obj = quest_ip;
                    //quest_ip前面会有一个/符号，例如/192.168.0.1，这里对他进行截取，截取后就为真正的IP地址 如 192.168.0.1
                    msg.obj = quest_ip.substring(1);
                    handler1.sendMessage(msg);//msg中包含了IP地址
                    try {
                        final String ip = udpPacket.getAddress().toString().substring(1);
                        //恢复按钮为可点击
                        //设置按钮可点击
                        //fab_CreateConnection.setEnabled(true);.........................................这里不能更新UI

                        handler1.sendEmptyMessage(1);
                        socket = new Socket(ip, 8080);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (null != socket) {
                                Log.i("tag", "socket close");
                                socket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                /**搜索到设备后停止返回tcp并停止监听*/
                if (null != udpPacket.getAddress()) {
                    Log.i("tag", "6666666666666666666666666");
                    udpout = true;
                    udpSocket.close();
                    break;//收到UDP请求后，跳出这个循环
                }
                if (!UdpReceiveOut){
                    Log.i("tag", "77777777777777777777777777777777777777");
                    udpSocket.close();
                    UdpReceiveOut = true;
                    break;
                }

            }

        }
    }




    /**
     * 判断一个字符串是否是标准的IPv4地址
     */
    public static boolean isIp(String IP) {
        boolean b = false;
        //去掉IP字符串前后所有的空格
        while (IP.startsWith(" ")) {
            IP = IP.substring(1, IP.length()).trim();
        }
        while (IP.endsWith(" ")) {
            IP = IP.substring(0, IP.length() - 1).trim();
        }

        //IP = this.deleteSpace(IP);
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }
        return b;
    }

    /**
     * 去除字符串前后的空格
     */
    public String deleteSpace(String IP) {//去掉IP字符串前后所有的空格
        while (IP.startsWith(" ")) {
            IP = IP.substring(1, IP.length()).trim();
        }
        while (IP.endsWith(" ")) {
            IP = IP.substring(0, IP.length() - 1).trim();
        }
        return IP;
    }


    /**
     * 重写onActivityResult()方法，获取选取要上传文件的文件路径
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //选择了文件发送
        if (resultCode == RESULT_OK) {
            String type = data.getStringExtra("Type");
            if (type.equals("intenet")) {
                //将选择的文件的名字以及路径存储下来
                final String fileName = data.getStringExtra("FileName");
                final String path = data.getStringExtra("FilePath");
                System.out.println("0000000000000000000000000000000000000000000000000000000000000000" + path);
                System.out.println("0000000000000000000000000000000000000000000000000000000000000000" + fileName);
                //String uploadUrl = "http://192.168.1.147/OfflineTrans/AndroidUploadAction.php";
                String uploadUrl = "http://115.28.101.196/AndroidUploadAction.php";
                new HttpThread_UpLoad(uploadUrl, path).start();//启动文件上传的线程

                /**在数据库中写入点击了离线上传的数据**/
                update_User_Using_Modules_Times_Android(lingdongdb.Offline_Files_Upload);
            } else if (type.equals("bluetooth")) {
                String path = data.getStringExtra("FilePath");
                File file = new File(path);

                Uri uri = Uri.fromFile(file);

                //打开系统蓝牙模块并发送文件
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("*/*");
                sharingIntent.setPackage("com.android.bluetooth");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(sharingIntent, "Share"));

                Log.d("MainActivity", uri.getPath());//log打印返回的文件路径
            }
        }
    }


    /**
     * 创建文件夹的方法createMkdir()
     */
    public static void createMkdir(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * 点击两次退出程序
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();

//                {
//                    //退出时上传用户使用过程中产生的数据，先判断有无网络，如果有网络就上传，无网络就丢弃本次数据，每次上传成功后，清空所有数据表
//                    //退出时 上传用户产生的数据，这里没有判断网络状况，直接上传的，如果此时网络无法连接，则放弃此条数据，不作为大数据样本统计到数据分析中
//                    update_LingDongDB();
//                    //清空所有数据表
//                    dbWriter.execSQL("DELETE FROM user_info_android");
//                    dbWriter.execSQL("DELETE FROM user_using_time_android");
//                    dbWriter.execSQL("DELETE FROM user_using_modules_times_android");
//                    dbWriter.execSQL("DELETE FROM user_using_files_trans_android");
//                    //自增长ID设置为0
//                    dbWriter.execSQL("DELETE FROM sqlite_sequence");
//                }

            } else {

                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bluetooth) {
            update_User_Using_Modules_Times_Android(LingDongDB.Bluetooth_Trans);
            /**用系统的蓝牙模块来发送文件*/
            Intent intent = new Intent(getApplicationContext(), Offline_Files_Choose_Activity.class);
            intent.putExtra("Type", "bluetooth");
            startActivityForResult(intent, 0);

        } else if (id == R.id.nav_share) {
            update_User_Using_Modules_Times_Android(LingDongDB.Share_APP);

            //弹出对话框，进行文件提取码的输入，然后下载文件
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("扫码下载APP：");
            //通过LayoutInflater来加载一个xml的布局文件作为一个View对象
            final View Dialogview = LayoutInflater.from(MainActivity.this).inflate(R.layout.share_app_dialog, null);
            //设置我们自己定义的布局文件作为弹出框的Content
            builder.setView(Dialogview);

            //设置点击确定后的事件，什么也不执行，关闭dialog
            builder.setNegativeButton("确定", null);
            builder.show();


        } else if (id == R.id.nav_connectPC) {
            update_User_Using_Modules_Times_Android(LingDongDB.Connect_PC);
            /**连接到电脑，与电脑进行文件互传*/

//            Toast.makeText(MainActivity.this, "这不是一个标准IP，内容为：", Toast.LENGTH_LONG).show();
//            Log.i("TAG", "55555555555555555555555555555");
//            Intent intent = new Intent(MainActivity.this, Files_Trans_Activity.class);
//            startActivity(intent);


                //显示雷达扫描界面
                showPopupWindow();

                //打开线程之前先判断热点是否是开的，如果热点是开的，就关掉热点，然后再开启wifi，如果热点本身是关的，就直接开启WIFI
                /***************以下的判断方法是有错误的，应该重写***********/
//                if (WifiApAdmin.isWifiApEnabled(wifiManager)) {
//                    WifiApAdmin.closeWifiAp(wifiManager);
//                    wifiManager.setWifiEnabled(true);

                    Thread thread = new Thread(new TcpReceive2());
                    thread.start();
                    offline_trans_log.setText("正在发送UDP请求，若有连接将在此显示，若五秒钟后没有显示，可以点击再次搜索。。。" + "\n");
                    BroadCastUdp1 bcu = new BroadCastUdp1(address);
                    bcu.start();
                    fab_ScanToJoin.setEnabled(false);
                    fab_CreateConnection.setEnabled(false);
//                } else {
//
//                    wifiManager.setWifiEnabled(true);
//
//                    Thread thread = new Thread(new TcpReceive());
//                    thread.start();
//                    offline_trans_log.setText("正在发送UDP请求，若有连接将在此显示，若五秒钟后没有显示，可以点击再次搜索。。。" + "\n");
//                    BroadCastUdp bcu = new BroadCastUdp(address);
//                    bcu.start();
//                    fab_ScanToJoin.setEnabled(false);
//                    fab_CreateConnection.setEnabled(false);
//                }


        } else if (id == R.id.nav_filesmanage) {
            update_User_Using_Modules_Times_Android(LingDongDB.Files_Manage);
            /**菜单中文件管理选项，跳转到文件管理的Activity进行文件管理的操作*/
            Intent intent = new Intent(MainActivity.this, Files_Manage_Activity.class);
            startActivity(intent);

        } else if (id == R.id.nav_feedback) {
            /**菜单中文件管理选项，跳转到nav_feedback操作*/
            Intent intent = new Intent(MainActivity.this, FeedBack.class);
            startActivity(intent);

        } /*else if (id == R.id.nav_textcutpaste) {
            *//**菜单中文件管理选项，跳转到文本剪贴操作*//*
            Intent intent = new Intent(MainActivity.this, ClipBoardActivity.class);
            startActivity(intent);

        }*/ else if (id == R.id.nav_softversion) {
            update_User_Using_Modules_Times_Android(LingDongDB.Software_Version);
            /**菜单中“版本”选项的弹出显示版本信息的对话框*/
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("软件版本")
                    .setMessage("版本号：" + BuildConfig.VERSION_CODE + "\n版本名：" + BuildConfig.VERSION_NAME)
                    .setPositiveButton("确定", null)
                    .show();
            return true;

        } else if (id == R.id.nav_softdescribe) {
            update_User_Using_Modules_Times_Android(LingDongDB.Software_Describe);
            /**菜单中“软件描述”选项的弹出对话框*/
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("软件描述")
                    .setMessage("此APP还有与之配套的电脑端程序和网页端服务。\n网址：http://115.28.101.196/")
                    .setPositiveButton("确定", null)
                    .show();
            return true;

        } else if (id == R.id.nav_aboutus) {
            update_User_Using_Modules_Times_Android(LingDongDB.About_Us);
            /**菜单中“关于我们”选项的弹出对话框*/
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("关于我们")
                    .setMessage("本程序由\n周博文(zhoubowen.sky@gmail.com)\n郑志琦(664837069@qq.com)\n董致礼(yhinu@qq.com)\n三人开发和维护!\n指导老师：黄辰")
                    .setPositiveButton("确定", null)
                    .show();
            return true;

        } else if (id == R.id.nav_androidversion) {
            update_User_Using_Modules_Times_Android(LingDongDB.User_Android_Version);
            /**菜单中“安卓版本”选项的弹出对话框*/
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("本机的安卓版本")
                    .setMessage("Android SDK:" + Build.VERSION.SDK + "\nAndroid 版本号:" + Build.VERSION.RELEASE)
                    .setPositiveButton("确定", null)
                    .show();
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void finish() {
        if (WifiApAdmin.isWifiApEnabled(wifiManager)) {
            WifiApAdmin.closeWifiAp(wifiManager);

        }

            //退出时上传用户使用过程中产生的数据，先判断有无网络，如果有网络就上传，无网络就丢弃本次数据，每次上传成功后，清空所有数据表
            //退出时 上传用户产生的数据，这里没有判断网络状况，直接上传的，如果此时网络无法连接，则放弃此条数据，不作为大数据样本统计到数据分析中
            update_LingDongDB();
            //清空所有数据表
            dbWriter.execSQL("DELETE FROM user_info_android");
            dbWriter.execSQL("DELETE FROM user_using_time_android");
            dbWriter.execSQL("DELETE FROM user_using_modules_times_android");
            dbWriter.execSQL("DELETE FROM user_using_files_trans_android");
            //自增长ID设置为0
            dbWriter.execSQL("DELETE FROM sqlite_sequence");

        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid()); /**杀死这个应用的全部进程*/

    }

    /**雷达扫面界面的显示方法*/
    private void showPopupWindow() {
        View popView = View.inflate(getApplicationContext(), R.layout.layout_pop, null);
        iv_scanning = (ImageView) popView.findViewById(R.id.iv_scanning);
        initAnimation();
        PopupWindow popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xcc000000));

        popupWindow.showAtLocation(rl_root, Gravity.CENTER, 0, 0);
    }

    /**雷达扫面界面的实现方法*/
    private void initAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1500);
        rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        iv_scanning.startAnimation(rotateAnimation);

    }
}
