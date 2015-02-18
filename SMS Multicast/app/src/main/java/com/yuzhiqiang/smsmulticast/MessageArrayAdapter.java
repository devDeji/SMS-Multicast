package com.yuzhiqiang.smsmulticast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MessageArrayAdapter extends ArrayAdapter<Message> {
	private final Context context;
	private final List<Message> list;

	public MessageArrayAdapter(Context context, List<Message> list) {
		super(context, R.layout.message_list_item, list);
		this.context = context;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.message_list_item, parent, false);
		Message message = list.get(position);

		ImageView imageView = (ImageView) itemView.findViewById(R.id.icon);
		TextView textView1 = (TextView) itemView.findViewById(android.R.id.text1);
		TextView textView2 = (TextView) itemView.findViewById(android.R.id.text2);
		TextView textViewStatus = (TextView) itemView.findViewById(R.id.textViewStatus);

		switch (message.status) {
			case Message.NEW:
				imageView.setImageResource(android.R.drawable.ic_menu_send);
				textViewStatus.setText("New");
				break;
			case Message.PENDING:
				imageView.setImageResource(android.R.drawable.ic_media_pause);
				textViewStatus.setText("Pending");
				break;
			case Message.SENDING:
				imageView.setImageResource(android.R.drawable.ic_media_play);
				textViewStatus.setText("Sending");
				break;
			case Message.SENT:
				imageView.setImageResource(android.R.drawable.ic_dialog_info);
				textViewStatus.setText("Sent");
				break;
			case Message.FAILED:
				imageView.setImageResource(android.R.drawable.ic_delete);
				textViewStatus.setText("Failed");
				break;
		}

		textView1.setText(message.destination);
		textView2.setText(message.content);

		return itemView;
	}
}
