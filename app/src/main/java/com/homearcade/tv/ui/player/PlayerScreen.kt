package com.homearcade.tv.ui.player

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.homearcade.tv.ui.components.TvButton
import com.homearcade.tv.ui.components.TvLoadingSpinner
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.BgCard
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.homearcade.tv.ui.theme.TextDim
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onExit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isPageLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val isConfirmingExitState = rememberUpdatedState(uiState.isConfirmingExit)
    val exitButtonFocusRequester = remember { FocusRequester() }

    HideSystemUI()

    LaunchedEffect(uiState.isConfirmingExit) {
        if (uiState.isConfirmingExit) {
            webViewRef?.isFocusable = false
            webViewRef?.isFocusableInTouchMode = false
            webViewRef?.clearFocus()
            exitButtonFocusRequester.requestFocus()
        } else {
            webViewRef?.isFocusable = true
            webViewRef?.isFocusableInTouchMode = true
            webViewRef?.requestFocus()
        }
    }

    BackHandler {
        if (uiState.isConfirmingExit) {
            viewModel.dismissExitDialog()
        } else {
            viewModel.showExitDialog()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val streamUrl = uiState.streamUrl
        if (!streamUrl.isNullOrEmpty()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Hardware acceleration for WebRTC 60 FPS video decode
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            builtInZoomControls = false
                            displayZoomControls = false
                            allowFileAccess = false
                            allowContentAccess = false
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onPermissionRequest(request: PermissionRequest?) {
                                request?.grant(request.resources)
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isPageLoading = false
                            }
                        }

                        setOnKeyListener { _, keyCode, event ->
                            if (isConfirmingExitState.value) {
                                return@setOnKeyListener false
                            }
                            if (event.action == KeyEvent.ACTION_DOWN) {
                                when (keyCode) {
                                    KeyEvent.KEYCODE_DPAD_UP -> {
                                        dispatchKeyEventToJs("ArrowUp")
                                        true
                                    }
                                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                                        dispatchKeyEventToJs("ArrowDown")
                                        true
                                    }
                                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                                        dispatchKeyEventToJs("ArrowLeft")
                                        true
                                    }
                                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                        dispatchKeyEventToJs("ArrowRight")
                                        true
                                    }
                                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                        dispatchKeyEventToJs("Enter")
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }

                        loadUrl(streamUrl)
                        webViewRef = this
                    }
                },
                update = { wv ->
                    webViewRef = wv
                    if (uiState.isConfirmingExit) {
                        wv.isFocusable = false
                        wv.isFocusableInTouchMode = false
                        wv.clearFocus()
                    } else {
                        wv.isFocusable = true
                        wv.isFocusableInTouchMode = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Loading spinner while page initialises
        if (isPageLoading || uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TvLoadingSpinner(size = 54.dp)
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Connessione al flusso di gioco...",
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Caricamento Native Libretro WebRTC Engine",
                        color = TextDim,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Exit confirmation modal dialog
        if (uiState.isConfirmingExit) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(480.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgCard)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown &&
                                (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK ||
                                 event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BUTTON_B)
                            ) {
                                viewModel.dismissExitDialog()
                                true
                            } else {
                                false
                            }
                        }
                        .padding(32.dp)
                ) {
                    Text(
                        text = "Vuoi uscire dalla partita?",
                        color = TextMain,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "I tuoi progressi e i salvataggi automatici sono stati sincronizzati con l'add-on HomeArcade.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TvButton(
                            text = "Continua a Giocare",
                            icon = Icons.Default.PlayArrow,
                            isPrimary = true,
                            modifier = Modifier.focusRequester(exitButtonFocusRequester),
                            onClick = { viewModel.dismissExitDialog() }
                        )

                        TvButton(
                            text = "Esci al Catalogo",
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            onClick = {
                                viewModel.dismissExitDialog()
                                webViewRef?.destroy()
                                onExit()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun WebView.dispatchKeyEventToJs(keyName: String) {
    val js = """
        (function() {
            var eventDown = new KeyboardEvent('keydown', { key: '$keyName', code: '$keyName', bubbles: true });
            document.dispatchEvent(eventDown);
            setTimeout(function() {
                var eventUp = new KeyboardEvent('keyup', { key: '$keyName', code: '$keyName', bubbles: true });
                document.dispatchEvent(eventUp);
            }, 50);
        })();
    """.trimIndent()
    evaluateJavascript(js, null)
}

@Composable
private fun HideSystemUI() {
    val context = LocalContext.current
    val activity = context as? Activity
    DisposableEffect(Unit) {
        val window = activity?.window
        val decorView = window?.decorView
        val originalVisibility = decorView?.systemUiVisibility ?: 0

        decorView?.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )

        onDispose {
            decorView?.systemUiVisibility = originalVisibility
        }
    }
}
