package com.innov.geotracking.presentation.geo_tracking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.innov.geotracking.R
import com.innov.geotracking.databinding.ItemVehicleTypeBinding
import com.innov.geotracking.presentation.geo_tracking.model.VehicleTypes

class VehicleOptionsAdapter (
    private val listener: SelectVehicleListener,
    private val vehicles: List<VehicleTypes>
) : RecyclerView.Adapter<VehicleOptionsAdapter.VehicleViewHolder>() {

    var selectedPosition = 0 // Track selected item

    inner class VehicleViewHolder(val binding: ItemVehicleTypeBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previousPosition = selectedPosition
                    selectedPosition = position

                    notifyItemChanged(previousPosition) // Refresh previous selected item
                    notifyItemChanged(selectedPosition) // Refresh current selected item

                    listener.onSelectVehicle(selectedPosition) // Notify listener of the selection
                }
            }
        }

        fun bind(vehicle: VehicleTypes, isSelected: Boolean) {
            binding.ivVehicle.setImageResource(vehicle.imageResId)
            binding.tvVehicleType.text = vehicle.type


            if (isSelected) {
                binding.layoutMain.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.titan_white))
                binding.layoutMain.strokeColor = ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
                binding.tvVehicleType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimary))
                binding.ivVehicle.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.colorPrimary))
            } else {
                binding.layoutMain.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.white))
                binding.layoutMain.strokeColor = ContextCompat.getColor(binding.root.context, R.color.alto)
                binding.tvVehicleType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.mine_shaft))
                binding.ivVehicle.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.mine_shaft))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = vehicles.size

    interface SelectVehicleListener {
        fun onSelectVehicle(position: Int)
    }
}