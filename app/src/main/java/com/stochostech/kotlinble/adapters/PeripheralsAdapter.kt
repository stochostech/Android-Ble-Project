package com.stochostech.kotlinble.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stochostech.kotlinble.R
import com.stochostech.kotlinble.databinding.PeripheralsItemBinding
import com.stochostech.kotlinble.models.Peripherals

class PeripheralsAdapter (var peripherals: ArrayList<Peripherals>) :
    RecyclerView.Adapter<PeripheralsAdapter.PeripheralsViewHolder>() {

    inner class PeripheralsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: PeripheralsItemBinding

        init {
            binding = PeripheralsItemBinding.bind(itemView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeripheralsViewHolder {
        return PeripheralsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.peripherals_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PeripheralsViewHolder, position: Int) {
        val peripheral = peripherals[position]

        holder.binding.apply {
            nameTv.text = "Name : ${peripheral.peripheralName}"
            macAddressTV.text = "Device Address : ${peripheral.peripheralMacAddress.toString()}"
            serviceUuidTV.text = "Service Uuid : ${peripheral.peripheralServiceUuid}"

            rl1.setOnClickListener {
                onItemClickListener?.let { it(peripheral) }
            }

        }

    }

    private var onItemClickListener: ((Peripherals) -> Unit)? = null

    fun setPeripheralClickListener(listener: (Peripherals) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return peripherals.size
    }
}