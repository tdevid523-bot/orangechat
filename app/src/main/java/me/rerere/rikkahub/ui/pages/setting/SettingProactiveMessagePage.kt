package me.rerere.rikkahub.ui.pages.setting

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.data.service.ProactiveMessageService
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingProactiveMessagePage(vm: SettingVM = koinInject()) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("主动消息") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                CardGroup {
                    // Enable switch
                    item(
                        headlineContent = { Text("启用主动消息") },
                        supportingContent = { Text("开启后AI立即主动发一条消息，之后按设定间隔循环") },
                        trailingContent = {
                            Switch(
                                checked = settings.proactiveMessageSetting.enabled,
                                onCheckedChange = { enabled ->
                                    val newSetting = settings.proactiveMessageSetting.copy(enabled = enabled)
                                    vm.updateSettings(settings.copy(proactiveMessageSetting = newSetting))
                                    if (enabled) {
                                        // 开启时立即触发第一次，然后安排后续
                                        ProactiveMessageService.triggerNow(context, newSetting)
                                    } else {
                                        ProactiveMessageService.cancel(context)
                                    }
                                }
                            )
                        }
                    )
                    // Next trigger time
                    if (settings.proactiveMessageSetting.enabled) {
                        var nextTime by remember { mutableStateOf(ProactiveMessageService.getNextTriggerTime(context)) }
                        LaunchedEffect(settings.proactiveMessageSetting) {
                            nextTime = ProactiveMessageService.getNextTriggerTime(context)
                            while (true) {
                                kotlinx.coroutines.delay(10_000L)
                                nextTime = ProactiveMessageService.getNextTriggerTime(context)
                            }
                        }
                        item(
                            headlineContent = { Text("下次触发时间") },
                            supportingContent = {
                                val currentTime = System.currentTimeMillis()
                                val triggerTime = nextTime
                                if (triggerTime != null && triggerTime > currentTime) {
                                    val remaining = triggerTime - currentTime
                                    val remainMinutes = remaining / 60_000
                                    val remainSeconds = (remaining % 60_000) / 1000
                                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                    Text("🕐 ${sdf.format(java.util.Date(triggerTime))}（剩余 ${remainMinutes}分${remainSeconds}秒）")
                                } else {
                                    Text("等待调度中...")
                                }
                            }
                        )
                    }
                }
            }

            item {
                CardGroup {
                    // Min interval
                    item(
                        headlineContent = { Text("最小间隔 (分钟)") },
                        supportingContent = {
                            OutlinedTextField(
                                value = settings.proactiveMessageSetting.minIntervalMinutes.toString(),
                                onValueChange = { value ->
                                    val minutes = value.toIntOrNull()
                                    if (minutes != null && minutes > 0) {
                                        vm.updateSettings(
                                            settings.copy(
                                                proactiveMessageSetting = settings.proactiveMessageSetting.copy(
                                                    minIntervalMinutes = minutes
                                                )
                                            )
                                        )
                                    }
                                },
                                placeholder = { Text("30") },
                                singleLine = true,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        },
                    )

                    // Max interval
                    item(
                        headlineContent = { Text("最大间隔 (分钟)") },
                        supportingContent = {
                            OutlinedTextField(
                                value = settings.proactiveMessageSetting.maxIntervalMinutes.toString(),
                                onValueChange = { value ->
                                    val minutes = value.toIntOrNull()
                                    if (minutes != null && minutes >= settings.proactiveMessageSetting.minIntervalMinutes) {
                                        vm.updateSettings(
                                            settings.copy(
                                                proactiveMessageSetting = settings.proactiveMessageSetting.copy(
                                                    maxIntervalMinutes = minutes
                                                )
                                            )
                                        )
                                    }
                                },
                                placeholder = { Text("90") },
                                singleLine = true,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        },
                    )
                }
            }

            item {
                CardGroup {
                    item(
                        headlineContent = { Text("说明") },
                        supportingContent = { Text("启用后，AI 会在设定的最小和最大间隔之间随机一个时间点主动给你发消息。" +
                            "你回复后计时器重置，重新开始随机计时；不回复则继续循环发消息。" +
                            "AI 可以自己思考选择要不要回复，如果觉得没什么好说的可以跳过。") },
                    )
                }
            }
        }
    }
}