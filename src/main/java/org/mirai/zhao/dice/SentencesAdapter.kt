package org.mirai.zhao.dice

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*


class SentencesAdapter(private val ctx: Context, private val dataList: ArrayList<SentencesThem>) : BaseAdapter() {
    //private val li: LayoutInflater = LayoutInflater.from(ctx)
    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): SentencesThem {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = View.inflate(ctx, R.layout.sentences_them, null)
            ViewHolder(view)
        }
        view = view as View
        val holder = view.tag as ViewHolder // get convertView's holder
        holder.sentenceName.text = getItem(position).tag_view
        return view
    }

    internal inner class ViewHolder(convertView: View) {
        var sentenceName: TextView = convertView.findViewById(R.id.sentence_name)
        init {
            convertView.tag = this //set a viewholder
        }
    }

}