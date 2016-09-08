/**
 * 文件属性
 */
package filesmanage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lingdong20.R;

import org.w3c.dom.Text;

/**
 * Created by Administrator on 2016/8/17.
 */
public class Files_Manage_Property extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files_manage_property);

        Intent intent = getIntent();
        final String name = intent.getStringExtra("Name");
        final String size = intent.getStringExtra("Size");
        final String content = intent.getStringExtra("Content");
        final String lastmodified = intent.getStringExtra("Lastmodified");
        final String path = intent.getStringExtra("Path");

        TextView txtName = (TextView)findViewById(R.id.file_name);
        TextView txtSize = (TextView)findViewById(R.id.file_size);
        TextView txtContent = (TextView)findViewById(R.id.file_content);
        TextView txtContent0 = (TextView)findViewById(R.id.file_content0);
        TextView txtLastmodified = (TextView)findViewById(R.id.file_lastmodified);
        TextView txtPath = (TextView)findViewById(R.id.file_path);

        txtName.setText(name);
        txtSize.setText(size);
        if(content == null)
        {
            txtContent.setVisibility(View.GONE);
            txtContent0.setVisibility(View.GONE);
        }
        else txtContent.setText(content);
        txtLastmodified.setText(lastmodified);
        txtPath.setText(path);
    }
}
