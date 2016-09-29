package joe.com.diffutilsample;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView listView;
    private MyAdapter adapter;
    private ArrayList<Person> orgPersons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (RecyclerView) findViewById(R.id.listview);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //用下面注视的语句更新，就会发现onBindViewHolder中的list长度是2了。原因就是在界面刷新前，对同一个数据项，快速更新了多次导致的。
//                adapter.notifyItemChanged(5, "first change");
//                adapter.notifyItemChanged(5, "second change");
//                return;
                final ArrayList<Person> newList = new ArrayList<>();
                for (int i = 1; i < 5; i++) {
                    newList.add(new Person(i, String.valueOf(i)));
                }
                for (int i = 7; i < 12; i++) {
                    newList.add(new Person(i, String.valueOf(i + 1)));
                }
                /**
                 * 应该在异步线程中计算差异，然后主线程进行UI更新。
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MyDiffUtilCallBack(orgPersons, newList));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                result.dispatchUpdatesTo(adapter);
                                adapter.setPersons(newList);
                                orgPersons = newList;
                            }
                        });
                    }
                }).start();
            }
        });
        adapter = new MyAdapter();
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);
        orgPersons = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            orgPersons.add(new Person(i, String.valueOf(i)));
        }
        adapter.setPersons(orgPersons);
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private ArrayList<Person> persons;

        public MyAdapter() {
            persons = new ArrayList<>();
        }

        public void setPersons(ArrayList<Person> persons) {
            this.persons = persons;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            if (position >= 0 && position < persons.size()) {
                holder.setId(persons.get(position).getId());
                holder.setName(persons.get(position).getName());
            }
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position, List<Object> payloads) {
            Log.d("MyAdapter", "onBindViewHolder: id=" + persons.get(position).getId() + "  paySize=" + payloads.size());
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else {
                holder.setName((String) payloads.get(payloads.size() - 1));
            }
        }

        @Override
        public int getItemCount() {
            return persons.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView idTv, nameTv;

            public MyViewHolder(View itemView) {
                super(itemView);
                idTv = (TextView) itemView.findViewById(R.id.tv_id);
                nameTv = (TextView) itemView.findViewById(R.id.tv_name);
            }

            public void setId(int id) {
                idTv.setText(String.format(Locale.getDefault(), "id=%d", id));
            }

            public void setName(String name) {
                nameTv.setText("Name=" + name);
            }
        }
    }

    class Person {
        int id;
        String name;

        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Person() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    class MyDiffUtilCallBack extends DiffUtil.Callback {

        private ArrayList<Person> oldList;
        private ArrayList<Person> newList;

        public MyDiffUtilCallBack(ArrayList<Person> oldList, ArrayList<Person> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        /**
         * 根据唯一标志判定是不是同一数据。
         */
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        /**
         * 如果是同一数据，那么数据内容是否相同。
         */
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getName().equals(newList.get(newItemPosition).getName());
        }

        /**
         * 如果不重写该方法，返回null。将会进行recyclerView的全局刷新。如有返回值，将会进行局部刷新。
         */
        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            Person oldPerson = oldList.get(oldItemPosition);
            Person newPerson = newList.get(newItemPosition);
            return newPerson.getName();
        }
    }
}