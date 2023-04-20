package com.mobile.pipvideosample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobile.pipvideosample.databinding.RowPairedDevicesBinding

class PairedDevicesAdapter(private val connectDevice: (DeviceInfo) -> Unit) :
    RecyclerView.Adapter<PairedDevicesAdapter.Holder>() {

    private val deviceList = arrayListOf<DeviceInfo>()

    fun updateDeviceList(devices: List<DeviceInfo>) {
        deviceList.clear()
        deviceList.addAll(devices)
        notifyDataSetChanged()
    }

    fun addDevice(device: DeviceInfo) {
        if(!deviceList.contains(device)) {
            deviceList.add(device)
            notifyItemChanged(deviceList.size-1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_paired_devices, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val device = deviceList[holder.layoutPosition]
        holder.itemBinding.tvPairedDeviceName.text = device.name
        holder.itemBinding.btnConnect.text = if (device.isConnected) "Connected" else "Connect"
        holder.itemBinding.btnConnect.setOnClickListener {
            if (!device.isConnected) {
                connectDevice.invoke(device)
            }
        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemBinding = RowPairedDevicesBinding.bind(itemView)

    }
}

data class DeviceInfo(val name: String, val address: String, val isConnected: Boolean)