/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.covedesign.devs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author sftejoka07
 */

public abstract class ClickableListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List mDataObjects;
	private int mViewId;

	public static class ViewHolder {
		public Object data;
	}

	public static abstract class OnClickListener implements
			View.OnClickListener {

		private ViewHolder mViewHolder;

		public OnClickListener(ViewHolder holder) {
			mViewHolder = holder;
		}

		public void onClick(View v) {
			onClick(v, mViewHolder);
		}
		public abstract void onClick(View v, ViewHolder viewHolder);
	};

	public static abstract class OnLongClickListener implements
			View.OnLongClickListener {
		private ViewHolder mViewHolder;

		public OnLongClickListener(ViewHolder holder) {
			mViewHolder = holder;
		}

		public boolean onLongClick(View v) {
			onLongClick(v, mViewHolder);
			return true;
		}
		public abstract void onLongClick(View v, ViewHolder viewHolder);

	};
	public ClickableListAdapter(Context context, int viewid, List objects) {

		mInflater = LayoutInflater.from(context);
		mDataObjects = objects;
		mViewId = viewid;

		if (objects == null) {
			mDataObjects = new ArrayList<Object>();
		}
	}
	public int getCount() {
		return mDataObjects.size();
	}
	public Object getItem(int position) {
		return mDataObjects.get(position);
	}
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if (view == null) {

			view = mInflater.inflate(mViewId, null);
			holder = createHolder(view);
			view.setTag(holder);

		} else {
			holder = (ViewHolder) view.getTag();
		}
		holder.data = getItem(position);
		bindHolder(holder);

		return view;
	}
	protected abstract ViewHolder createHolder(View v);
	protected abstract void bindHolder(ViewHolder h);
}