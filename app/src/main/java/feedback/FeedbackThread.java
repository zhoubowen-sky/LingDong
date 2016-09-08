/**
 * 用户反馈的上传线程
 */
package feedback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by 周博文 on 2016/7/31.
 */
public class FeedbackThread extends Thread {

    String url;
    public static String username;
    public static String useremail;
    public static String usersuggestion;

    public static String echoFromPHP;

    public FeedbackThread(String url, String username, String useremail, String usersuggestion) {
        this.url = url;
        this.username = username;
        this.useremail = useremail;
        this.usersuggestion = usersuggestion;
    }

    private void submitSuggestion() {
        try {
            URL httpUrl = new URL(url);

            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            OutputStream out = conn.getOutputStream();
            String content = "username=" + username + "&useremail=" + useremail + "&usersuggestion=" + usersuggestion;
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


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        submitSuggestion();

        super.run();
    }

}
