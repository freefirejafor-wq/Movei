package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaNavy
import com.example.ui.theme.CinemaNavyCard
import com.example.ui.theme.CinemaNavySurface

const val MOVIE_NEST_HOME_URL = "https://movienestbd.best/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieWebViewScreen() {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(MOVIE_NEST_HOME_URL) }
    var pageTitle by remember { mutableStateOf("MovieNestBD") }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableIntStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(true) }

    // Fullscreen HTML5 video holder
    var customVideoView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // Network connectivity listener
    DisposableEffect(context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
            }

            override fun onLost(network: Network) {
                isConnected = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(request, networkCallback)

        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        onDispose {
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback)
            } catch (_: Exception) {}
        }
    }

    // Android Back button handling:
    // 1. If fullscreen video is showing, exit fullscreen first.
    // 2. If webview can navigate backwards, go back in history.
    BackHandler(enabled = customVideoView != null || canGoBack) {
        if (customVideoView != null) {
            customViewCallback?.onCustomViewHidden()
            customVideoView = null
            customViewCallback = null
        } else if (webViewRef?.canGoBack() == true) {
            hasError = false
            webViewRef?.goBack()
        }
    }

    Scaffold(
        topBar = {
            if (customVideoView == null) {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CinemaGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = "MovieNestBD Logo",
                                        tint = CinemaGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "MovieNestBD",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = CinemaGold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Secure Connection",
                                            tint = CinemaGold.copy(alpha = 0.7f),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "movienestbd.best",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            // Back button inside top bar
                            IconButton(
                                onClick = {
                                    if (webViewRef?.canGoBack() == true) {
                                        hasError = false
                                        webViewRef?.goBack()
                                    }
                                },
                                enabled = canGoBack,
                                modifier = Modifier.testTag("nav_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.nav_back),
                                    tint = if (canGoBack) CinemaGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                )
                            }

                            // Forward button inside top bar
                            IconButton(
                                onClick = {
                                    if (webViewRef?.canGoForward() == true) {
                                        hasError = false
                                        webViewRef?.goForward()
                                    }
                                },
                                enabled = canGoForward,
                                modifier = Modifier.testTag("nav_forward_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = stringResource(R.string.nav_forward),
                                    tint = if (canGoForward) CinemaGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                )
                            }

                            // Refresh / Reload button
                            IconButton(
                                onClick = {
                                    hasError = false
                                    isLoading = true
                                    webViewRef?.reload()
                                },
                                modifier = Modifier.testTag("nav_refresh_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.refresh_page),
                                    tint = CinemaGold
                                )
                            }

                            // Home button
                            IconButton(
                                onClick = {
                                    hasError = false
                                    isLoading = true
                                    webViewRef?.loadUrl(MOVIE_NEST_HOME_URL)
                                },
                                modifier = Modifier.testTag("nav_home_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = stringResource(R.string.back_to_home),
                                    tint = CinemaGold
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CinemaNavySurface,
                            titleContentColor = CinemaGold,
                            actionIconContentColor = CinemaGold
                        )
                    )

                    // Linear loading indicator below top app bar
                    if (isLoading) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .testTag("page_progress_bar"),
                            color = CinemaGold,
                            trackColor = CinemaNavy
                        )
                    }
                }
            }
        },
        containerColor = CinemaNavy
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fullscreen video player layer (if HTML5 video triggers onShowCustomView)
            if (customVideoView != null) {
                AndroidView(
                    factory = {
                        FrameLayout(it).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            addView(customVideoView)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CinemaNavy)
                        .testTag("fullscreen_video_container")
                )
            } else {
                // Native WebView
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("movie_web_view"),
                    factory = { ctx ->
                        createConfiguredWebView(
                            context = ctx,
                            onProgressUpdate = { newProgress ->
                                progress = newProgress
                                if (newProgress >= 100) {
                                    isLoading = false
                                }
                            },
                            onPageStarted = { url ->
                                currentUrl = url
                                isLoading = true
                            },
                            onPageFinished = { url ->
                                currentUrl = url
                                isLoading = false
                            },
                            onNavigationStateChanged = { backAvailable, forwardAvailable ->
                                canGoBack = backAvailable
                                canGoForward = forwardAvailable
                            },
                            onTitleChanged = { title ->
                                if (!title.isNullOrBlank()) {
                                    pageTitle = title
                                }
                            },
                            onErrorReceived = { errDesc ->
                                hasError = true
                                isLoading = false
                                errorMessage = errDesc
                            },
                            onShowCustomView = { view, callback ->
                                customVideoView = view
                                customViewCallback = callback
                            },
                            onHideCustomView = {
                                customVideoView = null
                                customViewCallback = null
                            }
                        ).also { webView ->
                            webViewRef = webView
                            webView.loadUrl(MOVIE_NEST_HOME_URL)
                        }
                    },
                    update = {
                        webViewRef = it
                    }
                )

                // Sleek loading overlay on initial start
                if (isLoading && progress < 40 && !hasError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CinemaNavy.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CinemaNavyCard),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = CinemaGold,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("initial_loading_spinner"),
                                    strokeWidth = 3.5.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.loading_label),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CinemaGold,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                // Error & Retry view when site cannot load
                if (hasError) {
                    ErrorRetryView(
                        isOffline = !isConnected,
                        errorDetails = errorMessage,
                        onRetry = {
                            hasError = false
                            isLoading = true
                            progress = 10
                            if (webViewRef?.url.isNullOrEmpty() || webViewRef?.url == "about:blank") {
                                webViewRef?.loadUrl(MOVIE_NEST_HOME_URL)
                            } else {
                                webViewRef?.reload()
                            }
                        },
                        onGoHome = {
                            hasError = false
                            isLoading = true
                            progress = 10
                            webViewRef?.loadUrl(MOVIE_NEST_HOME_URL)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorRetryView(
    isOffline: Boolean,
    errorDetails: String?,
    onRetry: () -> Unit,
    onGoHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaNavy)
            .padding(24.dp)
            .testTag("error_retry_container"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CinemaNavySurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(CinemaGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOffline) Icons.Default.WifiOff else Icons.Default.WarningAmber,
                        contentDescription = "Error Icon",
                        tint = CinemaGold,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isOffline) "No Internet Connection" else stringResource(R.string.error_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isOffline) {
                        "Your device appears to be offline. Connect to Wi-Fi or mobile data to access MovieNestBD."
                    } else {
                        stringResource(R.string.error_message)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )

                if (!errorDetails.isNullOrBlank() && !isOffline) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Detail: $errorDetails",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Retry Action
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("retry_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = CinemaNavy
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.retry_button),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Go Home Action
                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("go_home_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CinemaGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.back_to_home),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun createConfiguredWebView(
    context: Context,
    onProgressUpdate: (Int) -> Unit,
    onPageStarted: (String) -> Unit,
    onPageFinished: (String) -> Unit,
    onNavigationStateChanged: (Boolean, Boolean) -> Unit,
    onTitleChanged: (String?) -> Unit,
    onErrorReceived: (String?) -> Unit,
    onShowCustomView: (View, WebChromeClient.CustomViewCallback) -> Unit,
    onHideCustomView: () -> Unit
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Enable cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(this, true)

        // WebSettings configuration
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            // Windows handling
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = true

            // Built-in zoom controls support
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // App cache / local storage
            allowFileAccess = false
            allowContentAccess = false
        }

        // WebChromeClient for loading progress, titles, and fullscreen HTML5 video
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onProgressUpdate(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                onTitleChanged(title)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view != null && callback != null) {
                    onShowCustomView(view, callback)
                }
            }

            override fun onHideCustomView() {
                onHideCustomView()
            }
        }

        // WebViewClient to keep website links opening inside the app and handle errors
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                val uri = request.url

                // Keep http and https URLs inside the WebView
                if (uri.scheme == "http" || uri.scheme == "https") {
                    view?.loadUrl(url)
                    return true
                }

                // For external apps and non-web schemes (e.g. mailto:, tel:, intent:, market:, tg:)
                try {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {
                    return true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { onPageStarted(it) }
                view?.let {
                    onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { onPageFinished(it) }
                view?.let {
                    onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    val description = error?.description?.toString() ?: "Connection failed"
                    onErrorReceived(description)
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame == true) {
                    val statusCode = errorResponse?.statusCode ?: 0
                    if (statusCode in 500..599) {
                        onErrorReceived("Server error ($statusCode)")
                    }
                }
            }
        }
    }
}
