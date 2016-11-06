package liuliu.dadlyplant.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Administrator on 2016/11/6.
 */
public class LoadMoreAdapter<T> extends BaseAdapter {
    private List<T> list;
    private int layout_id;
    private LayoutInflater mInflater;

    public LoadMoreAdapter(List<T> model, int layout_id, Context context) {
        this.list = model;
        this.layout_id = layout_id;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView =mInflater.inflate(layout_id,null);
        }
        return null;
    }
}
