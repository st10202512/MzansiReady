package com.mzansiready.app.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mzansiready.app.data.model.FeedPost
import com.mzansiready.app.databinding.ItemFeedPostBinding

class FeedAdapter : ListAdapter<FeedPost, FeedAdapter.FeedViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemFeedPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FeedViewHolder(private val binding: ItemFeedPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: FeedPost) {
            binding.tvAuthor.text = post.authorName
            binding.tvMessage.text = post.message
            binding.tvTimestamp.text = post.timestamp?.toString() ?: "Just now"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FeedPost>() {
        override fun areItemsTheSame(oldItem: FeedPost, newItem: FeedPost) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FeedPost, newItem: FeedPost) = oldItem == newItem
    }
}