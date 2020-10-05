package com.example.dyno.MyPage.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.dyno.R

class MedicineAdapter : RecyclerView.Adapter<MedicineAdapter.VersionViewHolder> {
    var versionModels: List<String>? = null
    var isHomeList: Boolean
    var context: Context? = null

    fun setDiseaseList(context: Context) {
        val listArray =
            context.resources.getStringArray(R.array.disease_name)
        val subTitleArray =
            context.resources.getStringArray(R.array.disease_date)

        nameList!!.clear()
        dateList.clear()
        for (i in listArray.indices) {
            nameList!!.add(listArray[i])
            dateList.add(subTitleArray[i])
        }
    }
    constructor(context: Context) {
        isHomeList = true
        this.context = context
        setDiseaseList(context)
    }

    constructor(versionModels: List<String>?) {
        isHomeList = false
        this.versionModels = versionModels
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): VersionViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recyclerlist_item, viewGroup, false)
        return VersionViewHolder(view)
    }

    override fun onBindViewHolder(versionViewHolder: VersionViewHolder, i: Int) {
        if (isHomeList) {
            versionViewHolder.title.text = nameList!![i]
            versionViewHolder.subTitle.text = dateList[i]
        } else {
            versionViewHolder.title.text = versionModels!![i]
        }
    }

    override fun getItemCount(): Int {
        return if (isHomeList)
            if (nameList == null)
                0
            else
                nameList!!.size
        else if (versionModels == null)
            0
        else
            versionModels!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class VersionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var cardItemLayout: CardView
        var title: TextView
        var subTitle: TextView
        override fun onClick(v: View?) {
            Toast.makeText(context,"gdgd2", Toast.LENGTH_SHORT).show()
        }

        init {
            cardItemLayout = itemView.findViewById(R.id.cardlist_item)
            title = itemView.findViewById(R.id.listitem_name)
            subTitle = itemView.findViewById(R.id.listitem_subname)
            if (isHomeList) {
                itemView.setOnClickListener(this)
            } else {
                subTitle.visibility = View.GONE
            }
        }
    }

    companion object {
        var nameList: MutableList<String>? =
            ArrayList()
        var dateList: MutableList<String> =
            ArrayList()
    }
}