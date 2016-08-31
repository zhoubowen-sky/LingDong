/**
 * 这是离线文件部分，下载文件的请求线程
 */
package offlinefiles;

import android.os.Environment;

import com.lingdong20.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 周博文 on 2016/7/24.
 */
public class HttpThread_DownLoad extends Thread {

    String url;

    public static String DownLoadNumber;

    public static String echoFromPHP;

    //创建HttpThread_DownLoad的构造方法
    public HttpThread_DownLoad(String url, String DownLoadNumber) {
        this.url = url;
        this.DownLoadNumber = DownLoadNumber;
    }

    /**
     * 这是用Get方式上传参数，可以当作参考
     */
   /* private void doGet(){
        url = url+"?DownLoadNumber="+DownLoadNumber;
        try {
            URL httpUrl = new URL(url);
            try {
                HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(5000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String str;
                StringBuffer sb = new StringBuffer();
                while ((str=reader.readLine())!=null){
                    sb.append(str);
                }
                System.out.print("resualt:"+sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * POST方式向服务器发送参数，优点是可以隐藏你要发送的参数，同时可以发送大数据，get方式只能发送几K的数据且发送的数据是不能隐藏的
     */

    private void doPost() {
        try {

            URL httpUrl = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            OutputStream out = conn.getOutputStream();
            String content = "DownLoadNumber=" + DownLoadNumber;
            out.write(content.getBytes());


            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String str;

            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            echoFromPHP = sb.toString();
            //在logcat输出这个值用以检查验证
            System.out.println("PHP服务器返回的字符串信息为:" + echoFromPHP);

            /**************下面是文件下载的方法**************/

            if (echoFromPHP.equals("This_downloadnumber_is_null")) {  /**没有输入文件提取码！*/
                MainActivity.handler.sendEmptyMessage(4);//发送消息到handler，通知操作的结果

            } else if (echoFromPHP.equals("This_downloadnumber_is_not_valid")) {  /**此文件提取码无效！*/
                MainActivity.handler.sendEmptyMessage(7);//发送消息到handler，通知操作的结果

            } else if (true) {
                //Toast.makeText(getApplicationContext(), filemd5name+"文件正在后台下载。。。", Toast.LENGTH_LONG).show();
                //更改下载按钮字样以提示用户正在下载
                //mFileDownLoad.setText("正在下载，请不要退出。。。");
                //截取字符串ResultFromPHP，只保存文件名
                String filemd5name = echoFromPHP.substring(34);//本地测试的话，这个值是不同的，截取字符串的一段

                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/LingDong/");
                //如果目标文件已经存在，则删除。产生覆盖旧文件的效果
                if (!file.exists()) {
                    file.mkdir();//delete();
                }
                try {
                    /**提示正在下载。。。*/
                    MainActivity.handler.sendEmptyMessage(6);//发送消息到handler，通知操作的结果
                    // 构造URL
                    //URL url = new URL("http://192.168.1.147/"+ResultFromPHP);
                    URL url = new URL(/*"http://115.28.101.196/FilesUpload/"+*/echoFromPHP);
                    //URL url = new URL("http://115.28.101.196/FilesUpload/63561648acbaf320b9d7b923e37ab8e1.jpg");
                    //Toast.makeText(getApplicationContext(), url.toString(), Toast.LENGTH_LONG).show();
                    // 打开连接
                    URLConnection con = url.openConnection();
                    //获得文件的长度
                    int contentLength = con.getContentLength();
                    System.out.println("长度 :" + contentLength);
                    // 输入流
                    InputStream is = con.getInputStream();
                    // 1K的数据缓冲
                    byte[] bs = new byte[1024];
                    // 读取到的数据长度
                    int len;
                    // 输出的文件流
                    /**********这样写会默认存储到app内部的data文件夹里面，会写入失败，应该指定写入的位置*********/
                    //下面这句话表示文件存储位置为Environment.getExternalStorageDirectory().getPath()+"/LingDong"+".jpg"
                    //即文件路径以及文件名为  如下所示   sdcard/lingDong.jpg
                    OutputStream os = new FileOutputStream(file + "/" + filemd5name);
                    // 开始读取
                    while ((len = is.read(bs)) != -1) {
                        os.write(bs, 0, len);
                    }
                    // 完毕，关闭所有链接
                    os.close();
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MainActivity.handler.sendEmptyMessage(2);//发送消息到handler，通知操作的结果
            }
            /*********************************************/

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        //doGet();
        doPost();

        super.run();
    }
}
