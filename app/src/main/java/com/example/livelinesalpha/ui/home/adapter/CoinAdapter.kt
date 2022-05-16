package com.example.livelinesalpha.ui.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.livelinesalpha.R
import com.example.livelinesalpha.ui.home.model.CoinDetails

class CoinAdapter(private val context: Context, private val coinsList: List<CoinDetails>):
    RecyclerView.Adapter<CoinAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.coin_item,parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CoinAdapter.ViewHolder, position: Int) {
        val item = coinsList[position]
        holder.nameTextView.text = item.name
        holder.priceInUSD.text =  "$" + String.format("%.3f", item.priceUsd)
        holder.changePercent24hr.text = String.format("%.3f", item.changePercent24Hr)
        val textColor: Int = if(item.changePercent24Hr < 0) {
            ContextCompat.getColor(context, R.color.design_default_color_error)
        } else {
            ContextCompat.getColor(context, R.color.design_default_color_secondary_variant)
        }
        holder.changePercent24hr.setTextColor(textColor)
    }

    override fun getItemCount(): Int {
        return coinsList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nameTextView: TextView = view.findViewById(R.id.name)
        var priceInUSD: TextView = view.findViewById(R.id.priceInUSD)
        var changePercent24hr: TextView = view.findViewById(R.id.changePercent24hr)
    }
}