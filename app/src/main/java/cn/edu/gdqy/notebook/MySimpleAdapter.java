package cn.edu.gdqy.notebook;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class MySimpleAdapter extends SimpleAdapter {
    private Context context;
    private Set<String> checkBoxs;
    private boolean isCheckBoxShow = false;     //是否显示复选框
    private boolean isSelectAll = false;        //是否全选复选框
    private CheckBox selectAllBox;                //全选按钮
    private List<Map<String,Object>> data;

    public MySimpleAdapter(Context context, List<Map<String, Object>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.data = data;
        checkBoxs = new HashSet<String>();
    }

    public void setCheckBoxShow(boolean isCheckBoxShow) {
        this.isCheckBoxShow = isCheckBoxShow;
        checkBoxs.clear();
    }

    public void setSelectAll(boolean isSelectAll) {
        this.isSelectAll = isSelectAll;
        if (isSelectAll) {
            Map<String,Object> map;
            for (int i=0; i<data.size(); i++) {
                map = data.get(i);
                String itemId = (String) map.get("itemId");
                checkBoxs.add(itemId);
            }
        } else if (!isSelectAll && checkBoxs.size() == data.size()) {
            checkBoxs.clear();
        }
    }

    public void setSelectAllBox(CheckBox selectAllBox) {
        this.selectAllBox = selectAllBox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView dataShow = view.findViewById(R.id.dataShow);
        Properties properties = FileUtils.readSizeFile(context);
        float fontsize = Float.parseFloat(properties.getProperty("fontsize"));
        dataShow.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontsize);
        CheckBox flagBox = view.findViewById(R.id.flagBox);
        flagBox.setVisibility(View.GONE);
        if (isCheckBoxShow) {
            flagBox.setVisibility(View.VISIBLE);
            TextView itemId = view.findViewById(R.id.itemId);
            String id = itemId.getText().toString();
            flagBox.setOnCheckedChangeListener(new CheckBoxChangeListener(id));
            if (checkBoxs.contains(id)) {
                flagBox.setChecked(true);
            } else {
                flagBox.setChecked(false);
            }
            if (isSelectAll) {
                flagBox.setChecked(true);
            }
        } else {
            flagBox.setChecked(false);
        }
        return view;
    }

    class CheckBoxChangeListener implements CompoundButton.OnCheckedChangeListener {
        String id;
        public CheckBoxChangeListener(String id) {
            this.id = id;
        }
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                checkBoxs.add(id);
            } else {
                if (checkBoxs.contains(id)) {
                    checkBoxs.remove(id);
                }
            }
            if (selectAllBox != null) {
                if (checkBoxs.size() == data.size()) {
                    selectAllBox.setChecked(true);
                }
                if(!isChecked) {    //有一个没选中则全选按钮取消选中
                    selectAllBox.setChecked(false);
                }
            }
        }
    }

    public Set<String> getSelectIds() {
        return checkBoxs;
    }
}
