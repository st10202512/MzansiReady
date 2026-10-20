package com.mzansiready.app.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mzansiready.app.data.model.FeedPost
import com.mzansiready.app.data.repo.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PostState(val isLoading: Boolean = false, val error: String? = null)

class CommunityFeedViewModel : ViewModel() {
    private val repository = FeedRepository()

    private val _feedPosts = MutableLiveData<List<FeedPost>>()
    val feedPosts: LiveData<List<FeedPost>> = _feedPosts

    private val _postState = MutableStateFlow(PostState())
    val postState: StateFlow<PostState> = _postState

    init {
        observeFeed()
    }

    private fun observeFeed() {
        repository.getFeedUpdates { posts ->
            _feedPosts.postValue(posts)
        }
    }

    fun postMessage(message: String) {
        viewModelScope.launch {
            _postState.value = PostState(isLoading = true)
            repository.postMessage(message)
                .onSuccess { _postState.value = PostState() }
                .onFailure { _postState.value = PostState(error = it.message) }
        }
    }
}