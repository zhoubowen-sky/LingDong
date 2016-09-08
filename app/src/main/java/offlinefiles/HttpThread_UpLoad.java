/**
 * 离线文件上传线程
 */
package offlinefiles;

import com.lingdong20.MainActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 周博文 on 2016/7/24.
 */
public class HttpThread_UpLoad extends Thread {

    public String uploadUrl;
    public String path;
    public static String result;

    //创建HttpThread_Upload的构造方法
    public HttpThread_UpLoad(String uploadUrl, String path) {
        this.uploadUrl = uploadUrl;
        this.path = path;
    }

    /**
     * 上传离线文件的方法myuploadFile()
     */

    public void myuploadFile() {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        try {
            /**提示正在上传。。。*/
            MainActivity.handler.sendEmptyMessage(5);//发送消息到handler，通知操作的结果

            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
            //httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
                    + path.substring(path.lastIndexOf("/") + 1)
                    + "\""
                    + end);
            dos.writeBytes(end);
            FileInputStream fis = new FileInputStream(path);
            System.out.println("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" + path);
            byte[] buffer = new byte[8192]; // 缓冲区大小为8k
            int count = 0;
            // 读取文件
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            result = br.readLine();

            System.out.println("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" + result);
            //Toast.makeText(getClass(), result, Toast.LENGTH_LONG).show();
            //这里应该有提示，提示内容为字符串result
            //MainActivity.handler.sendEmptyMessage(0);//发送消息到handler，通知上传进度
            dos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainActivity.handler.sendEmptyMessage(3);//发送消息到handler，通知操作的结果
    }


    @Override
    public void run() {
        myuploadFile();
        super.run();
    }
}
