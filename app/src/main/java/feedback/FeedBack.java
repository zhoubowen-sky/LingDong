/**
 * 用户反馈Activity
 */
package feedback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lingdong20.MainActivity;
import com.lingdong20.R;

import database.LingDongDB;
import offlinefiles.Offline_Files_Choose_Activity;


/**
 * Created by 周博文 on 2016/7/31.
 */
public class FeedBack extends Activity {

    /**
     * 反馈提交的按钮
     */
    private Button feedback_submit_bt;
    //private TextView feedback_name_tv,feedback_email_tv,feedback_suggestion_tv;
    /**
     * 反馈信息的三个EditText控件
     */
    private EditText feedback_name_et, feedback_email_et, feedback_suggestion_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_main);

        /**对控件进行定义*/
        feedback_submit_bt = (Button) findViewById(R.id.feedback_submit_bt);
        feedback_name_et = (EditText) findViewById(R.id.feedback_name_et);
        feedback_email_et = (EditText) findViewById(R.id.feedback_email_et);
        feedback_suggestion_et = (EditText) findViewById(R.id.feedback_suggestion_et);

        feedback_submit_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = feedback_name_et.getText().toString().trim();
                String useremail = feedback_email_et.getText().toString().trim();
                String usersuggestion = feedback_suggestion_et.getText().toString();
                String url = "http://115.28.101.196/feedback.php";

                /**用于获取网络状态的代码*/
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                /**先判断网络状态，如果有可用网络，就发送，没有就提示网络不可用*/
                if (networkInfo == null || !networkInfo.isAvailable()) {
                    //当前没有可用网络
                    Toast.makeText(FeedBack.this, "当前网络不可用，无法发送反馈", Toast.LENGTH_SHORT).show();
                } else {
                    //当前有可用网络,判断usersuggestion字符串是否为空
                    if (TextUtils.isEmpty(usersuggestion)) {
                        Toast.makeText(FeedBack.this, "建议栏不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        //向数据库中写入数据
                        MainActivity.update_User_Using_Modules_Times_Android(LingDongDB.User_Feedback);
                        new FeedbackThread(url, username, useremail, usersuggestion).start();
                        Toast.makeText(FeedBack.this, "提交成功，感谢你的反馈", Toast.LENGTH_SHORT).show();
                        finish();//结束当前Activity自动跳转到上一个Activity
                    }
                }


            }
        });


    }


}
