package de.thomaskuenneth.viewfainder

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val WEIGHT_IMAGE = 0.3F

@Composable
fun Results(
    viewModel: MainViewModel,
    uiState: UiState,
    roleStatus: RoleStatus,
    shouldShowMessage: Boolean,
    requestRole: () -> Unit,
    hideMessage: () -> Unit,
    scope: CoroutineScope,
    reset: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var gap by remember { mutableStateOf(0.dp) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.weight(WEIGHT_IMAGE)) {
            CapturedImage(viewModel)
        }
        Box(
            modifier = Modifier
                .weight(1F - WEIGHT_IMAGE)
                .safeContentPadding()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (uiState is UiState.Success) {
                    MarkdownText(
                        markdown = uiState.description,
                        style = MaterialTheme.typography.bodyLarge.merge(MaterialTheme.colorScheme.onBackground)
                    )
                    uiState.actions.forEach { action ->
                        when (action.first) {
                            Action.VCARD -> {
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                putExtra(Intent.EXTRA_TEXT, action.second)
                                                type = "text/vcard"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, null)
                                            context.startActivity(shareIntent)
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .align(Alignment.CenterHorizontally)
                                ) {
                                    Text(text = stringResource(id = R.string.add_to_contacts))
                                }
                            }
                        }
                    }
                } else if (uiState is UiState.Error) {
                    Text(
                        text = uiState.errorMessage,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.errorContainer)
                            .align(Alignment.CenterHorizontally)
                            .padding(all = 16.dp),
                        style = MaterialTheme.typography.bodyLarge.merge(MaterialTheme.colorScheme.onErrorContainer)
                    )
                }
                RoleChecker(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    scope = scope,
                    roleStatus = roleStatus,
                    shouldShowMessage = shouldShowMessage,
                    requestRole = requestRole,
                    hideMessage = hideMessage
                )
                Spacer(modifier = Modifier.height(gap))
            }
            Button(onClick = { reset() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .onGloballyPositioned {
                        gap = with(density) { it.size.height.toDp() }
                    }) {
                Text(text = stringResource(id = R.string.done))
            }
        }
    }
}
