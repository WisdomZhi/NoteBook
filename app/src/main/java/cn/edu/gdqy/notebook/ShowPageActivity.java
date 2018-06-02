package cn.edu.gdqy.notebook;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class ShowPageActivity extends AppCompatActivity implements View.OnClickListener {
    private Date currentDate;                  // 保存当前的日期
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");  //定义日期格式
    private TextView contentDateShow;
    private int changeCount = 0;             //判断是否文本框是否有改变 => 大于1时只发生了改动（由于初始的时候也会被监听到）

    private boolean editMode = false;       //当前是否是编辑模式
    private EditText contentShow;
    private View showModeMenu;              //展示模式下的菜单
    private View editModeMenu;              //编辑模式下的菜单
    private boolean isNewRecord = false;          //是否为新建的记录

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_page);
        showModeMenu = findViewById(R.id.showModeMenu);
        editModeMenu = findViewById(R.id.editModeMenu);
        contentDateShow = (TextView) findViewById(R.id.contentDateShow);
        contentShow = (EditText) findViewById(R.id.contentShow);
        Properties properties = FileUtils.readSizeFile(this);
        float fontsize = Float.parseFloat(properties.getProperty("fontsize"));
        contentShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontsize);
        contentShow.setOnClickListener(this);
        contentShow.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                //非编辑模式时返回true，表示已处理请求，可以避免在该模式下输入东西
                return !editMode;
            }
        });
        //监听EditView 是否有发生改变
        contentShow.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                changeCount ++;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        String itemId = getIntent().getStringExtra("itemId");
        if (itemId == null) {
            currentDate = new Date();
            contentDateShow.setText(dateFormat.format(currentDate));
            changeCount ++;     //为了下面的判断内容是否修改这里先加1，以便方便下面条件的判断
            isNewRecord = true;
            inEditMode();
        } else {
            //根据itemId查找数据库，并将数据展示在页面中
            //currentDate ：查找到的日期
            //将查询到的数据设置到editText中
            inShowMode();
            fillData(itemId);
        }

        ImageButton shareBtn = (ImageButton) findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(this);
        ImageButton delBtn = (ImageButton) findViewById(R.id.delBtn);
        delBtn.setOnClickListener(this);
        ImageButton cancelBtn = (ImageButton) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(this);
        ImageButton saveBtn = (ImageButton) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);
    }

    private void fillData(String itemId) {
        SQLiteOpenHelper sqLiteOpenHelper = new MyDatabaseHelper(this);
        SQLiteDatabase database = sqLiteOpenHelper.getReadableDatabase();
        String sql = "select * from " + MyDatabaseHelper.TABLE_NOTEBOOK + " where id=?";
        String[] selectArgs = {itemId};
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, selectArgs);
            if (cursor.moveToFirst()) {
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                contentDateShow.setText(date);
                contentShow.setText(content);
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.contentShow) {
            inEditMode();
        }else if (id == R.id.cancelBtn) {
            // 检查编辑文本框内是否有输入文字（去除空格后）（如果原来有内容则判断是否修改）
            // 如果有：提示是否放弃本次操作    放弃或保存
            // 放弃：直接退出当前activity，回到主界面
            // 保存：将数据库存入数据库后，退出当前activity，回到主界面
            exitConfirm(contentShow.getText().toString());
        } else if (id == R.id.saveBtn) {
            // 将数据库存入数据库后，退出当前activity，回到主界面
            String content = contentShow.getText().toString();
            if (!content.trim().equals("")) {
                saveData(content);
                if (isNewRecord) {
                    this.finish();
                } else {
                    changeCount = 1;
                    inShowMode();
                }
            } else {
                this.finish();
            }
        } else if (id == R.id.shareBtn) {
            shareContent();
        } else if (id == R.id.delBtn) {
            deleteRecord();
        }
    }

    private void deleteRecord() {
        //弹出删除确认框：确认或者取消操作
        //确认：从数据库删除该条数据
        final String itemId = getIntent().getStringExtra("itemId");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteOpenHelper sqLiteOpenHelper = new MyDatabaseHelper(ShowPageActivity.this);
                SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
                try {
                    database.beginTransaction();
                    String where = "id=?";
                    String[] whereArgs = {itemId};
                    database.delete(MyDatabaseHelper.TABLE_NOTEBOOK, where, whereArgs);
                    database.setTransactionSuccessful();
                } finally {
                     database.endTransaction();
                     ShowPageActivity.this.finish();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ShowPageActivity.this.finish();
            }
        });
        builder.setTitle("提示");
        builder.setMessage("是否删除该条日记");
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
    }

    private void shareContent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, contentShow.getText().toString().trim());
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "分享"));
    }

    //判断软键盘是否以弹出
    private boolean isKeyboardShow() {
        //获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        //DecorView即为activity的顶级view
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        //考虑到虚拟导航栏的情况（虚拟导航栏情况下：screenHeight = rect.bottom + 虚拟导航栏高度）
        //选取screenHeight*2/3进行判断
        return screenHeight*2/3 > rect.bottom;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //拦截返回键
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            //判断触摸UP事件才会进行返回事件处理
            if (event.getAction() == KeyEvent.ACTION_UP) {
                onBackPressed();
            }
            //只要是返回事件，直接返回true，表示消费掉
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (isKeyboardShow()) {
            return;
        }
        if (editMode) {
            exitConfirm(contentShow.getText().toString());
        } else {
            this.finish();
        }
    }

    private void inEditMode() {
        editMode = true;
        contentShow.setCursorVisible(true);
        editModeMenu.setVisibility(View.VISIBLE);
        showModeMenu.setVisibility(View.GONE);
    }

    private void inShowMode() {
        editMode = false;
        contentShow.setCursorVisible(false);
        editModeMenu.setVisibility(View.GONE);
        showModeMenu.setVisibility(View.VISIBLE);
    }

    //退出时确认是否要保存
    private void exitConfirm(final String content) {
        if (changeCount <= 1 || content.trim().equals("")) {
            this.finish();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveData(content);
                ShowPageActivity.this.finish();
            }
        });

        builder.setNegativeButton("放弃", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //关闭对话框
                dialogInterface.dismiss();
                ShowPageActivity.this.finish();
            }
        });

        builder.setMessage("是否保存当前修改？");
        builder.setTitle("提示");
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
    }

    private void saveData(String content) {
        String itemId = getIntent().getStringExtra("itemId");
        String date = contentDateShow.getText().toString();
        SQLiteOpenHelper databaseHelper = new MyDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", date);
        cv.put("content", content);
        if (itemId == null) {
            try {
                database.beginTransaction();
                database.insert(MyDatabaseHelper.TABLE_NOTEBOOK, null, cv);
                database.setTransactionSuccessful();
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            } finally {
                database.endTransaction();
            }
        } else {
            try {
                database.beginTransaction();
                String where = "id=?";
                String[] whereArgs = {itemId};
                database.update(MyDatabaseHelper.TABLE_NOTEBOOK, cv, where, whereArgs);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    }
}
