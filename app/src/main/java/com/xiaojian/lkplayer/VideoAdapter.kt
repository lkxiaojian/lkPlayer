package com.xiaojian.lkplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView


/**
 *create_time : 21-5-31 下午4:19
 *author: lk
 *description： VideoAdapter
 */
class VideoAdapter(context: Context, list: ArrayList<VideoInfo>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {
    private val mContext = context
    private val mData = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v: View =
            LayoutInflater.from(mContext).inflate(R.layout.video_item, parent, false)
        return ViewHolder(v)


    }

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoInfo = mData[position]
        holder.atvName.text=videoInfo.displayName
        holder.clTop.setOnClickListener {
            val intent = Intent(mContext, VideoPlayActivity::class.java)
            intent.putExtra("url",videoInfo.data)
            mContext.startActivity(intent)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val atvName = view.findViewById<AppCompatTextView>(R.id.atv_name)
        val image = view.findViewById<AppCompatImageView>(R.id.iv)
        val clTop = view.findViewById<ConstraintLayout>(R.id.cl_top)

    }
}