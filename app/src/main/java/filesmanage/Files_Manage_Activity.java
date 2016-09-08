/**
 * 文件管理器
 */
package filesmanage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lingdong20.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Files_Manage_Activity extends AppCompatActivity {
    private ArrayList<FileClass> fileList = new ArrayList<FileClass>();
    private FileAdapter adapter;
    private ListView listView;//显示文件列表
    private TextView pathText;//显示当前路径
    private Button button;//粘贴按键
    private String oldpath = null;//旧文件路径（文件剪切，复制参数之一）
    private boolean iscopy = true;//是否复制
    private final String[][] MIME_MapTable = {
            //{后缀名， MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            //{".xml", "text/xml"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files_manage_main);

        pathText = (TextView) findViewById(R.id.pathtext);
        //设定显示路径的textview的背景色
        //pathText.setBackgroundColor(Color.LTGRAY);

        button = (Button)findViewById(R.id.menu);
        adapter = new FileAdapter(Files_Manage_Activity.this, R.layout.files_manage_listview_item, fileList);
        listView = (ListView) findViewById(R.id.filelist);
        listView.setAdapter(adapter);
        UpdateFile(Environment.getExternalStorageDirectory().getPath());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()//点击监听事件
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                FileClass fileClass = fileList.get(position);
                if (fileClass.getImageId() == R.drawable.file)//如果点击的是文件，直接打开文件
                {
                    openFile(fileClass.getPath());
                } else//如果点击的是文件夹，打开文件夹
                {
                    UpdateFile(fileClass.getPath());
                    //listView.setAdapter(new FileAdapter(Files_Manage_Activity.this, R.layout.files_manage_listview_item, fileList));
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()//长按监听事件
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final FileClass fileClass = fileList.get(position);
                final File file = new File(fileClass.getPath());
                //点击文件时，弹出增删该操作选项对话框
                //以下为弹出框的内容
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0://重命名
                                renameFile(fileClass.getPath());
                                //adapter.notifyDataSetChanged();
                                break;
                            case 1://复制
                                oldpath = fileClass.getPath();
                                iscopy = true;
                                //设置粘贴按钮可见
                                button.setVisibility(View.VISIBLE);
                                break;
                            case 2://剪切
                                oldpath = fileClass.getPath();
                                iscopy = false;
                                //设置粘贴按钮可见
                                button.setVisibility(View.VISIBLE);
                                break;
                            case 3://删除
                                deleteFile(new File(fileClass.getPath()));
                                UpdateFile((new File(fileClass.getPath())).getParentFile().getPath());
                                //adapter.notifyDataSetChanged();
                                break;
                            case 4://属性
                                Intent intent = new Intent(Files_Manage_Activity.this, Files_Manage_Property.class);
                                intent.putExtra("Name", file.getName());
                                intent.putExtra("Size", file.length()+"B");
                                if(file.isDirectory())intent.putExtra("Content", scanFile(file.getPath()));
                                intent.putExtra("Lastmodified", new Date(file.lastModified()).toString());
                                intent.putExtra("Path", file.getPath());
                                startActivity(intent);
                        }
                    }
                };
                String[] menu = {"重命名", "复制", "剪切", "删除", "属性"};
                new AlertDialog.Builder(Files_Manage_Activity.this)
                        .setTitle("请选择要进行的操作")
                        .setItems(menu, listener)
                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            //将取消
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                return true;
            }
        });
        button.setOnClickListener(new View.OnClickListener()//粘贴
        {
            @Override
            public void onClick(View view)
            {
                if (oldpath != null) {
                    File oldfile = new File(oldpath);
                    if (oldfile.exists())//如果文件还存在
                    {
                        if (oldfile.isFile())
                            copyFile(oldpath, pathText.getText().toString() + "/" + oldfile.getName());
                        else
                            copyFolder(oldpath, pathText.getText().toString() + "/" + oldfile.getName());
                        if (!iscopy)//如果是剪切
                        {
                            deleteFile(oldfile);//删除原文件
                            iscopy = true;
                        }
                        UpdateFile(pathText.getText().toString());
                    }
                }

                //设置粘贴按钮不可见
                button.setVisibility(View.INVISIBLE);
            }
        });
    }

    //打开文件
    private void openFile(String filepath) {
        File file = new File(filepath);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
        //跳转
        startActivity(intent);
    }

    //获取文件mimetype
    private String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
    /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    //扫描某路径下所有文件及文件夹
    private void UpdateFile(String path) {
        pathText.setText(path);
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
                Toast.makeText(Files_Manage_Activity.this, "文件夹为空", Toast.LENGTH_SHORT).show();
            for (File file : files) {
                if (file.isDirectory())//判断是文件夹，则进行文件名判断
                {
                    fileList.add(new FileClass(file.getName(), file.getPath(), R.drawable.folder));
                } else if (file.isFile())//判断是文件，则进行文件名判断
                {
                    fileList.add(new FileClass(file.getName(), file.getPath(), R.drawable.file));
                }
            }
        } else Toast.makeText(Files_Manage_Activity.this, "无权限访问！", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    //重命名文件
    public void renameFile(String filepath) {
        final File file = new File(filepath);
        LayoutInflater factory = LayoutInflater.from(Files_Manage_Activity.this);
        View view = factory.inflate(R.layout.files_manage_dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        editText.setText(file.getName());
        AlertDialog renameDialog = new AlertDialog.Builder(Files_Manage_Activity.this).create();
        renameDialog.setView(view);
        renameDialog.setButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                String modifyName = editText.getText().toString();
                final String fpath = file.getParentFile().getPath();
                final File newFile = new File(fpath + "/" + modifyName);
                if (newFile.exists()) {
                    //排除没有修改情况
                    if (!modifyName.equals(file.getName())) {
                        new AlertDialog.Builder(Files_Manage_Activity.this)
                                .setTitle("注意!")
                                .setMessage("文件名已存在，是否覆盖？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (file.renameTo(newFile)) {
                                            UpdateFile(fpath);
                                            Toast.makeText(Files_Manage_Activity.this, "重命名成功！", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(Files_Manage_Activity.this, "重命名失败！", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                } else {
                    if (file.renameTo(newFile)) {
                        UpdateFile(fpath);
                        Toast.makeText(Files_Manage_Activity.this, "重命名成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Files_Manage_Activity.this, "重命名失败！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        renameDialog.setButton2("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        renameDialog.show();
    }

    //删除文件或文件夹
    public void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
            }
            for (int i = 0; i < childFiles.length; i++) {
                deleteFile(childFiles[i]);
            }
            file.delete();
        }
    }

    //剪切复制文件
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "复制单个文件操作出错");
            e.printStackTrace();
        }
    }

    //复制文件夹
    public void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
    }

    //新建文件夹
    public void makedirs(final String filepath)//参数为当前路径
    {
        File file = new File(filepath);
        LayoutInflater factory = LayoutInflater.from(Files_Manage_Activity.this);
        View view = factory.inflate(R.layout.files_manage_dialog, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        editText.setText("新建文件夹");//初始显示内容
        AlertDialog newfileDialog = new AlertDialog.Builder(Files_Manage_Activity.this).create();
        newfileDialog.setView(view);
        newfileDialog.setButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                String modifyName = editText.getText().toString();//获取EditText的内容
                File newFile = new File(filepath + "/" + modifyName);//当前目录+EditText既新的文件路径
                if (!newFile.exists())//如果同名文件不存在
                {
                    newFile.mkdir();
                    UpdateFile(pathText.getText().toString());
                    //adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(Files_Manage_Activity.this, "已存在同名文件！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        newfileDialog.setButton2("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        newfileDialog.show();
        UpdateFile(pathText.getText().toString());
        //adapter.notifyDataSetChanged();
    }

    //扫描子文件
    private String scanFile(String path)
    {
        int numCFolder = 0;//子文件夹数量
        int numCFile = 0;//子文件数量
        File file = new File(path);
        File[] childFiles = file.listFiles();
        for (int i = 0; i < childFiles.length; i++)
        {
            if(childFiles[i].isFile())numCFile++;
            else numCFolder++;
        }
        String result = numCFile+"个文件，"+numCFolder+"个文件夹";
        return result;
    }

    //定义菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Menu.FIRST, 0, "新建");
        menu.add(0, Menu.FIRST + 1, 0, "粘贴");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST://新建
                makedirs(pathText.getText().toString());
                return true;
            case Menu.FIRST + 1://粘贴
            {
                if (oldpath != null) {
                    File oldfile = new File(oldpath);
                    if (oldfile.exists())//如果文件还存在
                    {
                        if (oldfile.isFile())
                            copyFile(oldpath, pathText.getText().toString() + "/" + oldfile.getName());
                        else
                            copyFolder(oldpath, pathText.getText().toString() + "/" + oldfile.getName());
                        if (!iscopy)//如果是剪切
                        {
                            deleteFile(oldfile);//删除原文件
                            iscopy = true;
                        }
                        UpdateFile(pathText.getText().toString());
                    }
                }
                return true;
            }
            default:
                return false;
        }
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