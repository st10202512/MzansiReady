package com.mzansiready.app.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FeedPost(
    var id: String = "",
    var userId: String = "",
    var authorName: String = "",
    var message: String = "",
    @ServerTimestamp
    var timestamp: Date? = null
)

