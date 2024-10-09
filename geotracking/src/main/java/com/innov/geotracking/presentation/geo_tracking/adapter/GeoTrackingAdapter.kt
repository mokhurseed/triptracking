package com.innov.geotracking.presentation.geo_tracking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.innov.geotracking.R
import com.innov.geotracking.databinding.ItemTripsBinding
import com.innov.geotracking.presentation.geo_tracking.model.GeoLocationDataModel

class GeoTrackingAdapter(
    private val itemList: ArrayList<GeoLocationDataModel>,
    private val listener: OnItemClickListener? = null,
    private val onTripItemClick: (GeoLocationDataModel) -> Unit


) : RecyclerView.Adapter<GeoTrackingAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemTripsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemTripsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]
        with(holder.binding)
        {
            holder.itemView.setOnClickListener {
                onTripItemClick(item)
            }
            ivDelete.setOnClickListener {
                listener?.onDeleteAction(position=position)
            }

            tvFromLocation.text = item.fromLocation
            tvTripNumber.text = item.tripName
            if(item.isOngoingTrip)
            {
                root.strokeWidth = 1
                root.strokeColor = root.context.getColor(R.color.colorPrimary)
              tvTripDate.apply {
                  setText(R.string.on_going)
                  background = AppCompatResources.getDrawable(root.context, R.drawable.bg_chip)
                  setTextColor(root.context.getColor(R.color.gray))
                  background.setTint(root.context.getColor(R.color.titan_white))
                  tvDistance.text = "-"
                  tvToLocation.text = "     -         "
              }

            } else {
                root.strokeWidth = 0
                tvTripDate.apply {
                    tvTripDate.text = item.tripDate
                    background = AppCompatResources.getDrawable(root.context, R.drawable.bg_chip)
                    setTextColor(root.context.getColor(R.color.pale_sky))
                    background.setTint(root.context.getColor(R.color.white))
                    tvDistance.text = item.distance
                    tvToLocation.text = item.toLocation
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onDeleteAction(position: Int) {}
    }
}