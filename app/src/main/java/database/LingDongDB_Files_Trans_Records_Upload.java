package database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 周博文 on 2016/8/9.
 */
public class LingDongDB_Files_Trans_Records_Upload extends Thread {

    public String url;
    public String string_records;
    public static String echoFromPHP_Files_Trans_Records_Upload;

    /**
     * 用来记录服务器返回的数据，其实可以不需要
     */

    //构造方法
    public LingDongDB_Files_Trans_Records_Upload(String url, String string_records) {
        this.url = url;
        this.string_records = string_records;
    }


    //向服务器上传josn数据
    private void upload_json() {

        try {

            URL httpurl = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) httpurl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setReadTimeout(5000);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            //向服务器写入字符串数据流
            outputStream.write(string_records.getBytes());

            /**下面是处理服务器返回的信息的相关代码*/
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String str;

            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            echoFromPHP_Files_Trans_Records_Upload = sb.toString();
            //在logcat输出这个值用以检查验证
            System.out.println("PHP服务器返回的字符串信息为:" + echoFromPHP_Files_Trans_Records_Upload);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {

        try {

            upload_json();

        } catch (Exception e) {
            e.printStackTrace();
        }

        super.run();
    }


}
