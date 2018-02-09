package lzc.com.drawboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by lzc on 2017/12/31.
 */

public class FunctionAdapter extends BaseAdapter {
    private LayoutInflater inflater;//这个一定要懂它的用法及作用
    private List<Map<String,String>> mapList;

    public FunctionAdapter(Context context, List<Map<String,String>> mapList){
        this.inflater = LayoutInflater.from(context);
        this.mapList = mapList;
    }

    @Override
    public int getCount() {
        return this.mapList.size();
    }

    @Override
    public Object getItem(int i) {
        return this.mapList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //有很多例子中都用到这个holder,理解下??
        ViewHolder holder = null;
        //思考这里为何要判断convertView是否为空  ？？
        if(view == null){
            holder = new ViewHolder();

            //把vlist layout转换成View【LayoutInflater的作用】
            view = inflater.inflate(R.layout.function_item, null);
            //通过上面layout得到的view来获取里面的具体控件
            holder.tvState = (TextView) view.findViewById(R.id.item_state);
            holder.tvFunction = (TextView) view.findViewById(R.id.item_func);
            holder. tvRemark= (TextView) view.findViewById(R.id.item_remark);

            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        holder.tvState.setText((String) mapList.get(i).get("state"));
        holder.tvFunction.setText((String) mapList.get(i).get("function"));
        holder.tvRemark.setText((String) mapList.get(i).get("remark"));
        if("功能开发中".equals((String) mapList.get(i).get("state"))){
            holder.tvState.setTextColor(Color.parseColor("#e87f00"));
        }else if("功能已加入".equals((String) mapList.get(i).get("state"))){
            holder.tvState.setTextColor(Color.parseColor("#00c113"));
        }else if("反馈问题回复".equals((String) mapList.get(i).get("state"))){
            holder.tvState.setTextColor(Color.parseColor("#3300ff"));
        }else {
            holder.tvState.setTextColor(Color.RED);
        }



        return view;
    }

    public class ViewHolder{
        private TextView tvState;
        private TextView tvFunction;
        private TextView tvRemark;
    }
}
