package cn.edu.gdqy.notebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemLongClickListener {
    private MySimpleAdapter adapter;
    private EditText searchBox;
    private View mainMenu1;     //新建，设置按钮菜单
    private View mainMenu2;     //删除，全选按钮菜单
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    private Set<String> checkBoxs = new LinkedHashSet<String>();    //存放相应的被选中的itemId
    private boolean editMode = false;   //复选框是否处于编辑模式（是否可见）
    private final SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private final SimpleDateFormat showFormat = new SimpleDateFormat("MM/dd");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainMenu1 = findViewById(R.id.mainMenu1);
        mainMenu2 =findViewById(R.id.mainMenu2);
        FileUtils.checkSizeFileIsExist(getApplicationContext());

        String[] from = {"itemId",  "dateShow", "dataShow"};
        int[] to = {R.id.itemId,  R.id.dateShow, R.id.dataShow};
        adapter = new MySimpleAdapter(this, list, R.layout.list_items, from, to);

        ListView listView = (ListView) findViewById(R.id.showList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView itemId = view.findViewById(R.id.itemId);
                //点击时将id后传到展示内容的页面，以便通过id后查找数据库得到数据内容
                Intent intent = new Intent(MainActivity.this, ShowPageActivity.class);
                intent.putExtra("itemId", itemId.getText().toString());
                MainActivity.this.startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(this);

        ImageButton createBtn = (ImageButton) findViewById(R.id.createBtn);
        createBtn.setOnClickListener(this);
        ImageButton menuBtn = (ImageButton) findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(this);
        ImageButton deleteBtn = (ImageButton) findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(this);
        CheckBox selectAllBox = (CheckBox) findViewById(R.id.selectAllBox);
        selectAllBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    adapter.setSelectAll(true);
                } else {
                    adapter.setSelectAll(false);
                }
                adapter.notifyDataSetChanged();
            }
        });
        adapter.setSelectAllBox(selectAllBox);

        //搜索框
        searchBox = (EditText) findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateData();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            editMode = false;
            inShowMode();
            adapter.notifyDataSetChanged();
            return;
        }
        if (!searchBox.getText().toString().trim().equals("")) {
            searchBox.setText("");
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        updateData();
        super.onResume();
    }

    private void updateData() {
        getData();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.createBtn) {
            Intent intent = new Intent(this, ShowPageActivity.class);
            this.startActivity(intent);
        } else if (id == R.id.menuBtn) {
            showMenuDialog();
        } else if (id == R.id.deleteBtn) {
            deleteSelectedRecords();
        }
    }

    private void deleteSelectedRecords() {
        final Set<String> ids = adapter.getSelectIds();
        if (ids.isEmpty()) {
            Toast.makeText(MainActivity.this, "未选中任何记录",Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                batchDeleteData(ids);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                inShowMode();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setTitle("提示");
        builder.setMessage("确定删除选中记录？");
        AlertDialog dialog = builder.create();
        dialog.show();
        //改变颜色需要在show()方法调用之后再进行，否则会报错
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
    }

    private void batchDeleteData(Set<String> ids) {
        StringBuilder buf = new StringBuilder();
        buf.append("id in(?");
        for (int i=1; i<ids.size(); i++) {
            buf.append(",?");
        }
        buf.append(")");
        String where = buf.toString();
        String[] whereArgs = new String[ids.size()];
        ids.toArray(whereArgs);

        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            database.delete("notebook", where, whereArgs);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        inShowMode();
        updateData();
        adapter.notifyDataSetChanged();
    }

    private void showMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] items = {"字体大小", "关于"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent intent;
                switch (i) {
                    case 0: //字体大小
                        intent = new Intent(MainActivity.this, FontSizeChangeActivity.class);
                        startActivity(intent);
                        break;
                    case  1: //关于
                        intent = new Intent(MainActivity.this, AboutAppActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
        builder.create();
        builder.show();
    }

    //模拟从数据库取得数据
    public void getData() {
        list.clear();
        SQLiteOpenHelper sqLiteOpenHelper = new MyDatabaseHelper(this);
        SQLiteDatabase database = sqLiteOpenHelper.getReadableDatabase();
        String sql = "select * from " + MyDatabaseHelper.TABLE_NOTEBOOK;
        Cursor cursor = null;
        try {
            String keyWord = searchBox.getText().toString().trim();
            if (searchBox != null && !keyWord.equals("")) {
               sql = sql + " where content like '%" + keyWord + "%'";
            }
            sql = sql + " order by id desc";
            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();
            Map<String, Object> note;
            while (!cursor.isAfterLast()) {
                note = new HashMap<String, Object>();
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String dateShow = "";
                try {
                    dateShow = showFormat.format(sourceFormat.parse(date));
                    if (dateShow.charAt(0) == '0') {
                        dateShow = dateShow.substring(1);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                String content = cursor.getString(cursor.getColumnIndex("content"));
                note.put("itemId", id + "");
                note.put("dateShow", dateShow);
                note.put("dataShow", content);
                list.add(note);
                cursor.moveToNext();
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    //返回值为false时单击事件也会被触发
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        inEditMode();
        return true;
    }

    private void inEditMode() {
        editMode = true;
        adapter.setCheckBoxShow(true);
        adapter.notifyDataSetChanged();
        //设置搜索框不可编辑，隐藏键盘
        searchBox.setEnabled(false);
        searchBox.setFocusable(false);
        searchBox.setEnabled(true);
        mainMenu1.setVisibility(View.GONE);
        mainMenu2.setVisibility(View.VISIBLE);
    }

    private void inShowMode() {
        adapter.setCheckBoxShow(false);
        mainMenu2.setVisibility(View.GONE);
        mainMenu1.setVisibility(View.VISIBLE);
        searchBox.setFocusable(true);
        searchBox.setFocusableInTouchMode(true);
    }
}
