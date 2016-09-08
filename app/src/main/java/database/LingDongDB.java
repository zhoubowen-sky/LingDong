/**
 * 数据库
 */
package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 周博文 on 2016/8/6.
 */
public class LingDongDB extends SQLiteOpenHelper {

    /**
     * 以下是数据库相关的字段，以及表名等常量
     **/
    //数据库名字
    public static final String DATABASE_NAME = "LingDong_DataBase";


    //uesr_info_android 安卓用户信息 数据表相关的常量字段定义如下
    public static final String TABLE_User_Info_Android = "user_info_android";  //表名
    public static final String ID_User_Info_Android = "_id";                   //ID字段  必须以下划线打头
    public static final String Device_ID = "device_id";                        //设备ID字段
    public static final String Android_Version = "android_version";            //安卓的版本字段
    public static final String Device_Brand = "device_brand";                  //手机品牌字段
    public static final String Device_Model = "device_model";                  //手机型号字段
    public static final String Device_Memory = "device_memory";                //手机内存字段
    public static final String Device_CPU = "device_CPU";                      //手机CPU型号字段
    public static final String Device_Screen_Resolution = "device_screen_resolution";   //手机屏幕分辨率字段


    //安卓用户使用app时间段 数据表 user_using_time_android 相关的常量字段定义如下
    public static final String TABLE_User_Using_Time_Android = "user_using_time_android";
    public static final String ID_User_Using_Time_Android = "_id";
    //Device_ID就不重复定义了
    public static final String Start_APP_Time = "start_app_time";              //启动app的时间
    public static final String Exit_APP_Time = "exit_app_time";                //退出app的时间
    public static final String Holding_APP_Time = "holding_app_time";          //停留在app的时长


    //安卓用户使用模块功能频率 数据表 user_using_modules_times_android 相关的常量字段定义如下
    public static final String TABLE_User_Using_Modules_Times_Android = "user_using_modules_times_android";
    public static final String ID_User_Using_Modules_Times_Android = "_id";
    //Device_ID就不重复定义了
    //以下是app基本的十三个模块
    public static final String Offline_Files_Upload = "offline_files_upload";
    public static final String Offline_Files_Download = "offline_files_download";
    public static final String Bluetooth_Trans = "bluetooth_trans";
    public static final String Share_APP = "share_app";
    public static final String Files_Manage = "files_manage";
    public static final String User_Feedback = "user_feedback";
    public static final String Software_Version = "software_version";
    public static final String Software_Describe = "software_describe";
    public static final String About_Us = "about_us";
    public static final String User_Android_Version = "user_android_version";
    public static final String Connect_PC = "connect_PC";
    public static final String Create_Connection = "create_connection";
    public static final String Scan_To_Join = "scan_to_join";


    //安卓用户文件相互传递 数据表 user_using_files_trans_android 相关的常量字段定义如下
    public static final String TABLE_User_Using_Files_Trans_Android = "user_using_files_trans_android";
    public static final String ID_User_Using_Files_Trans_Android = "_id";
    //Device_ID就不重复定义了
    //以下是传输的文件的信息
    public static final String Files_Name = "files_name";
    public static final String Files_Type = "files_type";
    public static final String Files_Size = "files_size";
    public static final String Trans_Time = "trans_time";


    //下面这个事构造函数
    public LingDongDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //创建数据库 user_info_android 数据表
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_User_Info_Android + "(" +
                ID_User_Info_Android + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Device_ID + " TEXT NOT NULL," +
                Android_Version + " TEXT NOT NULL," +
                Device_Brand + " TEXT NOT NULL," +
                Device_Model + " TEXT NOT NULL," +
                Device_Memory + " TEXT NOT NULL," +
                Device_CPU + " TEXT NOT NULL," +
                Device_Screen_Resolution + " TEXT NOT NULL)");


        //创建数据库 user_using_time_android 数据表
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_User_Using_Time_Android + "(" +
                ID_User_Using_Time_Android + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Device_ID + " TEXT NOT NULL," +
                Start_APP_Time + " TEXT NOT NULL," +
                Exit_APP_Time + " TEXT NOT NULL," +
                Holding_APP_Time + " INT NOT NULL)");


        //创建用户用了哪些功能的数据表 user_using_modules_times_android 用以统计用户对不同功能使用的频率
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_User_Using_Modules_Times_Android + "(" +
                ID_User_Using_Modules_Times_Android + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Device_ID + " TEXT NOT NULL," +
                Offline_Files_Upload + " INT NOT NULL," +
                Offline_Files_Download + " INT NOT NULL," +
                Bluetooth_Trans + " INT NOT NULL," +
                Share_APP + " INT NOT NULL," +
                Files_Manage + " INT NOT NULL," +
                User_Feedback + " INT NOT NULL," +
                Software_Version + " INT NOT NULL," +
                Software_Describe + " INT NOT NULL," +
                About_Us + " INT NOT NULL," +
                User_Android_Version + " INT NOT NULL," +
                Connect_PC + " INT NOT NULL," +
                Create_Connection + " INT NOT NULL," +
                Scan_To_Join + " INT NOT NULL)");


        //创建数据库 user_using_files_trans_android 数据表
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_User_Using_Files_Trans_Android + "(" +
                ID_User_Using_Files_Trans_Android + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Device_ID + " TEXT NOT NULL," +
                Files_Name + " TEXT NOT NULL," +
                Files_Type + " TEXT NOT NULL," +
                Files_Size + " INT NOT NULL," +
                Trans_Time + " TEXT NOT NULL)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //更新数据库,数据库版本更新时使用的方法

    }
}
