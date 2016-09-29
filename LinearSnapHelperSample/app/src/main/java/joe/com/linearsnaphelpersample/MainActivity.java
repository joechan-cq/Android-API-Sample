package joe.com.linearsnaphelpersample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (RecyclerView) findViewById(R.id.listview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(linearLayoutManager);
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        MyAdapter adapter = new MyAdapter();
        listView.setAdapter(adapter);
        snapHelper.attachToRecyclerView(listView);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private String[] data = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position >= 0 && position < data.length) {
                holder.setValue(data[position]);
            }
        }

        @Override
        public int getItemCount() {
            return data.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView txt;

            public ViewHolder(View itemView) {
                super(itemView);
                txt = (TextView) itemView.findViewById(R.id.tv);
            }

            public void setValue(String s) {
                txt.setText(s);
            }
        }
    }
}
