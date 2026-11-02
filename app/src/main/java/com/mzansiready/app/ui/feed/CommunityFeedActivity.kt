package com.mzansiready.app.ui.feed

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mzansiready.app.MzansiApp
import com.mzansiready.app.databinding.ActivityCommunityFeedBinding
import kotlinx.coroutines.launch

class CommunityFeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityFeedBinding
    private val viewModel: CommunityFeedViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CommunityFeedViewModel() as T
            }
        }
    }
    private lateinit var adapter: FeedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ FIX: apply saved theme BEFORE super.onCreate
        applySavedTheme()

        super.onCreate(savedInstanceState)

        binding = ActivityCommunityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = FeedAdapter()
        binding.rvFeed.layoutManager = LinearLayoutManager(this)
        binding.rvFeed.adapter = adapter

        binding.btnPost.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.postMessage(message)
                binding.etMessage.text?.clear()
            }
        }

        lifecycleScope.launch {
            viewModel.postState.collect { state ->
                if (state.isLoading) {
                    binding.progress.visibility = View.VISIBLE
                } else {
                    binding.progress.visibility = View.GONE
                }
                state.error?.let {
                    Toast.makeText(this@CommunityFeedActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.feedPosts.observe(this) { posts ->
            adapter.submitList(posts)
        }
    }

    // ✅ FIX: apply saved theme app-wide
    private fun applySavedTheme() {
        val savedTheme = MzansiApp.from(this).session.theme()
        AppCompatDelegate.setDefaultNightMode(
            if (savedTheme == "dark") AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
