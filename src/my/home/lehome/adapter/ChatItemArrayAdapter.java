package my.home.lehome.adapter;

import java.text.SimpleDateFormat;
import java.util.List;

import my.home.lehome.R;
import my.home.lehome.util.Constants;
import android.R.integer;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.greenrobot.lehome.ChatItem;

public class ChatItemArrayAdapter extends ArrayAdapter<ChatItem> {

	private ResendButtonClickListener mResendButtonClickListener;
	private TextView chatTextView;

	@Override
	public void add(ChatItem object) {
		super.add(object);
	}
	
	public void setData(List<ChatItem> items) {
	    clear();
	    setNotifyOnChange(false);
	    if (items != null) {
	        for (ChatItem item : items)
	            add(item);
	    }
	    setNotifyOnChange(true);
	    notifyDataSetChanged();
	}

	public ChatItemArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.chat_item, parent, false);
		}

		RelativeLayout wrapper = (RelativeLayout) row.findViewById(R.id.wrapper);
		ChatItem chatItem = getItem(position);
		chatTextView = (TextView) row.findViewById(R.id.chat_item);
		chatTextView.setText(chatItem.getContent());
		chatTextView.setBackgroundResource(!chatItem.getIsMe() ? R.drawable.chatfrom_bg : R.drawable.chatto_bg);
		
		ImageButton resend = (ImageButton) row.findViewById(R.id.resend_imagebutton);
		if (chatItem.getSucceed() || !chatItem.getIsMe()) {
			resend.setVisibility(View.GONE);
		}else {
			resend.setVisibility(View.VISIBLE);
			if (mResendButtonClickListener != null) {
				mResendButtonClickListener.onResendButtonClicked(position);
			}
		}
		
		TextView dateTextView = (TextView) row.findViewById(R.id.date_textview);
		String dateString = getTimeWithFormat(position);
		if (dateString != null) {
			dateTextView.setText(dateString);
			dateTextView.setVisibility(View.VISIBLE);
		}else {
			dateTextView.setVisibility(View.INVISIBLE);
		}
		
		wrapper.setGravity(!chatItem.getIsMe() ? Gravity.LEFT : Gravity.RIGHT);

		return row;
	}
	
	private String getTimeWithFormat(int position) {
		ChatItem chatItem = getItem(position);
		String formatString = "MM月dd日 hh时mm分";
		SimpleDateFormat df=new SimpleDateFormat(formatString);
		if (position == 0) {
			return df.format(chatItem.getDate());
		}else {
			ChatItem preItem = getItem(position - 1);
			if (chatItem.getDate().getTime() - preItem.getDate().getTime() > Constants.SHOW_DATE_TEXTVIEW_GAP) {
				return df.format(chatItem.getDate());
			}else {
				return null;
			}
		}
	}
	
	/*
	 * delegate
	 */
	
	public ResendButtonClickListener getResendButtonClickListener() {
		return mResendButtonClickListener;
	}

	public void setResendButtonClickListener(
			ResendButtonClickListener mResendButtonClickListener) {
		this.mResendButtonClickListener = mResendButtonClickListener;
	}

	public interface ResendButtonClickListener{
		public void onResendButtonClicked(int pos);
	}
}