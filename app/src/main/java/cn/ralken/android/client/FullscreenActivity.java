package cn.ralken.android.client;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.ralken.android.client.udp.LANConnector;

public class FullscreenActivity extends Activity {

	LANConnector lanConnector;

	ListView listView;
	LogAdapter adapter;
	List<String> logs = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);

		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (lanConnector != null && lanConnector.isAlive()) {
					return;
				}
				lanConnector = new LANConnector(FullscreenActivity.this);
				lanConnector.start();
			}
		});

		listView = (ListView) findViewById(R.id.listView1);
		adapter = new LogAdapter();
		listView.setAdapter(adapter);
	}

	public void postLog(String log){
		Message msg = new Message();
		msg.obj = log;
		handler.sendMessage(msg);
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			logs.add((String) msg.obj);
			adapter.notifyDataSetChanged();
		};
	};
	
	class LogAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return logs.size();
		}

		@Override
		public Object getItem(int position) {
			return logs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.item_log_list, null);
				holder = new Holder();
				holder.logItemText = (TextView) convertView.findViewById(R.id.logItemText);
				convertView.setTag(holder);
			}

			holder = (Holder) convertView.getTag();

			holder.logItemText.setText(logs.get(position));
			return convertView;
		}

		Holder holder = null;

		class Holder {
			TextView logItemText;
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(lanConnector != null && lanConnector.isInterrupted()){
			lanConnector.setEnable(false);
			lanConnector.interrupt();
			lanConnector = null;
		}
	}

}
