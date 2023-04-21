package com.picker.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.picker.example.R
import com.picker.example.databinding.ItemImageBinding
import com.picker.mylibrary.MediaModel
import com.picker.mylibrary.Which

class ImageAdapter(private val list: ArrayList<MediaModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImageBinding.inflate(inflater, parent, false)
        return ViewDataHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewDataHolder).bindView(list[position])
    }

    inner class ViewDataHolder(
        private val binding: ItemImageBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindView(mediaModel: MediaModel) {
            if (mediaModel.which == Which.FILE) {
                Glide.with(binding.img).load(R.drawable.file).into(binding.img)
            } else {
                Glide.with(binding.img).load(mediaModel.uri).into(binding.img)
            }
            binding.video.isVisible = mediaModel.which == Which.VIDEO
        }
    }


}
