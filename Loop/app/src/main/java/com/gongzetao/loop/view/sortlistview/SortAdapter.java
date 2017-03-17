package com.gongzetao.loop.view.sortlistview;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gongzetao.loop.R;
import com.gongzetao.loop.application.MyApplication;
import com.gongzetao.loop.bean.User;
import com.gongzetao.loop.view.CircleImageView;
import com.lidroid.xutils.BitmapUtils;


public class SortAdapter extends BaseAdapter implements SectionIndexer{
	private List<SortModel> list = null;
	private Context mContext;

	public void setList(List<SortModel> list) {
		this.list = list;
	}

	public SortAdapter(Context mContext, List<SortModel> list) {
		this.mContext = mContext;
		this.list = list;
	}
	
	/**
	 * @param list
	 */
	public void updateListView(List<SortModel> list){
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		final SortModel mContent = list.get(position);
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mContext).inflate(R.layout.my_friends_item, null);
			viewHolder.tvTitle = (TextView) view.findViewById(R.id.title);
			viewHolder.tvLetter = (TextView) view.findViewById(R.id.catalog);
			viewHolder.frie_item_iv_photo = (CircleImageView)view.findViewById(R.id.frie_item_iv_photo);
			viewHolder.friends_line = (ImageView)view.findViewById(R.id.friends_line);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		
		int section = getSectionForPosition(position);
		
		if(position == getPositionForSection(section)){
			viewHolder.tvLetter.setVisibility(View.VISIBLE);
			viewHolder.friends_line.setVisibility(View.VISIBLE);
			viewHolder.tvLetter.setText(mContent.getSortLetters());
		}else {
			viewHolder.tvLetter.setVisibility(View.GONE);
			viewHolder.friends_line.setVisibility(View.GONE);
		}
	
		viewHolder.tvTitle.setText((String) this.list.get(position).getUser().get(User.str_accountName));
		String userPhoto = (String) this.list.get(position).getUser().get(User.str_photo);
		if(!TextUtils.isEmpty(userPhoto)) {
			Glide.with(mContext).load(userPhoto).into(viewHolder.frie_item_iv_photo);
		}
//		bitmapUtils.configDefaultLoadFailedImage(R.drawable.persion_default_photo);

		return view;
	}
	


	final static class ViewHolder {
		TextView tvLetter;
		TextView tvTitle;
		CircleImageView frie_item_iv_photo;
		ImageView friends_line;
	}


	/**
	 */
	public int getSectionForPosition(int position) {
		return list.get(position).getSortLetters().charAt(0);
	}

	/**
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 *
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String  sortStr = str.trim().substring(0, 1).toUpperCase();
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}