/**
 * 文件选择
 */

package offlinefiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.lingdong20.R;

public class Offline_Files_Choose_Activity extends Activity {
    private FileAdapter adapter;
    private ArrayList<FileClass> fileList = new ArrayList<FileClass>();
    private ListView fileView;//显示文件列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files_manage_main);

        Intent intent = getIntent();
        final String type = intent.getStringExtra("Type");

        TextView title = (TextView) findViewById(R.id.pathtext);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.GRAY);
        title.setText("选择要发送的文件");
        title.setTextSize(20);

        //Toast.makeText(getApplicationContext(), "选择要发送的文件", Toast.LENGTH_SHORT).show();
        adapter = new FileAdapter(Offline_Files_Choose_Activity.this, R.layout.files_manage_listview_item, fileList);
        fileView = (ListView) findViewById(R.id.filelist);
        fileView.setAdapter(adapter);
        UpdateFile(Environment.getExternalStorageDirectory().getPath());
        fileView.setOnItemClickListener(new AdapterView.OnItemClickListener()//点击监听事件
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                FileClass fileClass = fileList.get(position);
                if (fileClass.getImageId() == R.drawable.file)//如果点击的是文件，直接打开文件
                {
                    //数据使用Intent返回
                    Intent intent = new Intent();
                    //把返回的数据存入Intent
                    intent.putExtra("Type", type);
                    intent.putExtra("FileName", fileClass.getName());
                    intent.putExtra("FilePath", fileClass.getPath());
                    //设置返回数据
                    Offline_Files_Choose_Activity.this.setResult(RESULT_OK, intent);
                    //关闭Activity
                    Offline_Files_Choose_Activity.this.finish();
                } else//如果点击的是文件夹，打开文件夹
                {
                    UpdateFile(fileClass.getPath());
                }
            }
        });

    }

    //扫描某路径下所有文件及文件夹
    private void UpdateFile(String path) {
        fileList.clear();//清空fileList
        File selectfile = new File(path);
        if (path.equals(Environment.getExternalStorageDirectory().getPath()) == false)
            fileList.add(new FileClass("返回上一层", selectfile.getParentFile().getPath(), R.drawable.back));
        if (selectfile.exists() && selectfile.canRead()) {
            List<File> files = Arrays.asList(new File(path).listFiles());
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile())
                        return -1;
                    if (o1.isFile() && o2.isDirectory())
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });
            if (files.isEmpty())
                Toast.makeText(Offline_Files_Choose_Activity.this, "文件夹为空", Toast.LENGTH_SHORT).show();
            for (File file : files) {
                if (file.isDirectory())//判断是文件夹，则进行文件名判断
                {
                    fileList.add(new FileClass(file.getName(), file.getPath(), R.drawable.folder));
                } else if (file.isFile())//判断是文件，则进行文件名判断
                {
                    fileList.add(new FileClass(file.getName(), file.getPath(), R.drawable.file));
                }
            }
        } else
            Toast.makeText(Offline_Files_Choose_Activity.this, "无权限访问！", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }
}


//文件列表中各项的类
class FileClass {
    private String name;//文件名
    private String type;//后缀名
    private String path;//文件路径
    private int imageId;//图像Id

    public FileClass(String name, String path, int imageId) {
        this.name = name;
        this.imageId = imageId;
        this.path = path;
        this.type = getExtensionName(name);
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    //获取文件后缀名
    public static String getExtensionName(String filename) {

        String type = null;
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        String end = filename.substring(filename.lastIndexOf(".") + 1);
        if (end.length() > 5) {
            return null;
        } else
            return end;
    }
}

//对ListView的优化以及外观设置
class FileAdapter extends ArrayAdapter<FileClass> {
    private int resourceId;

    public FileAdapter(Context context, int textViewResourceId, List<FileClass> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileClass fileClass = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        ImageView fileImage = (ImageView) view.findViewById(R.id.file_image);
        TextView fileName = (TextView) view.findViewById(R.id.file_name);

        fileImage.setImageResource(fileClass.getImageId());
        fileName.setText(fileClass.getName());
        if (fileClass.getImageId() == R.drawable.file && fileClass.getType() != null)//如果是文件，就显示fileType
        {
            TextView fileType = (TextView) view.findViewById(R.id.file_type);
            fileType.setText(fileClass.getType());
            fileType.setBackgroundColor(Color.rgb(30, 42, 95));
        }
        return view;
    }
}