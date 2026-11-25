package com.whotfknows7.ytcomments

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.whotfknows7.ytcomments.ui.CommentsApp
import com.whotfknows7.ytcomments.ui.CommentsViewModel
import com.whotfknows7.ytcomments.ui.YTCommentsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CommentsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            YTCommentsTheme {
                CommentsApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    viewModel.handleIncomingSharedText(sharedText)
                }
            }
            Intent.ACTION_VIEW -> {
                val uriText = intent.dataString
                viewModel.handleIncomingSharedText(uriText)
            }
        }
    }
}
