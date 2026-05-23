package me.rerere.rikkahub.ui.pages.setting

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.io.File
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.datastore.ChatFontFamily
import me.rerere.rikkahub.data.datastore.DisplaySetting
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.richtext.MarkdownBlock
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.components.ui.permission.PermissionManager
import me.rerere.rikkahub.ui.components.ui.permission.PermissionNotification
import me.rerere.rikkahub.ui.components.ui.permission.rememberPermissionState
import me.rerere.rikkahub.ui.hooks.rememberAmoledDarkMode
import me.rerere.rikkahub.ui.hooks.rememberSharedPreferenceBoolean
import me.rerere.rikkahub.ui.pages.setting.components.PresetThemeButtonGroup
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingDisplayPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var displaySetting by remember(settings) { mutableStateOf(settings.displaySetting) }
    var amoledDarkMode by rememberAmoledDarkMode()

    fun updateDisplaySetting(setting: DisplaySetting) {
        displaySetting = setting
        vm.updateSettings(settings.copy(displaySetting = setting))
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val permissionState = rememberPermissionState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) setOf(
            PermissionNotification
        ) else emptySet(),
    )
    PermissionManager(permissionState = permissionState)

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(R.string.setting_display_page_title))
                },
                navigationIcon = {
                    BackButton()
                },
                scrollBehavior = scrollBehavior,
                colors = CustomColors.topBarColors
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = CustomColors.topBarColors.containerColor
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.setting_page_theme_setting),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
                    )
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 4.dp
                                )
                            ),
                        headlineContent = { Text(stringResource(R.string.setting_page_dynamic_color)) },
                        supportingContent = { Text(stringResource(R.string.setting_page_dynamic_color_desc)) },
                        trailingContent = {
                            Switch(
                                checked = settings.dynamicColor,
                                onCheckedChange = { vm.updateSettings(settings.copy(dynamicColor = it)) },
                            )
                        },
                        colors = CustomColors.listItemColors,
                    )
                    if (!settings.dynamicColor) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceBright)
                        ) {
                            PresetThemeButtonGroup(
                                themeId = settings.themeId,
                                modifier = Modifier.fillMaxWidth(),
                                onChangeTheme = { vm.updateSettings(settings.copy(themeId = it)) }
                            )
                        }
                    }
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                )
                            ),
                        headlineContent = { Text(stringResource(R.string.setting_display_page_amoled_dark_mode_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_amoled_dark_mode_desc)) },
                        trailingContent = {
                            Switch(
                                checked = amoledDarkMode,
                                onCheckedChange = { amoledDarkMode = it }
                            )
                        },
                        colors = CustomColors.listItemColors,
                    )
                }
            }

            item {
                var createNewConversationOnStart by rememberSharedPreferenceBoolean(
                    "create_new_conversation_on_start",
                    true
                )
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text(stringResource(R.string.setting_page_general_settings)) },
                ) {
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_create_new_conversation_on_start_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_create_new_conversation_on_start_desc)) },
                        trailingContent = {
                            Switch(
                                checked = createNewConversationOnStart,
                                onCheckedChange = { createNewConversationOnStart = it }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_show_updates_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_show_updates_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showUpdates,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showUpdates = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_notification_message_generated)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_notification_message_generated_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.enableNotificationOnMessageGeneration,
                                onCheckedChange = {
                                    if (it && !permissionState.allPermissionsGranted) {
                                        permissionState.requestPermissions()
                                    }
                                    updateDisplaySetting(displaySetting.copy(enableNotificationOnMessageGeneration = it))
                                }
                            )
                        },
                    )
                    if (displaySetting.enableNotificationOnMessageGeneration) {
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_live_update_notification)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_live_update_notification_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.enableLiveUpdateNotification,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(enableLiveUpdateNotification = it))
                                    }
                                )
                            },
                        )
                    }
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    CardGroup(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        title = { Text(stringResource(R.string.setting_page_message_display_settings)) },
                    ) {
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_show_user_avatar_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_show_user_avatar_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.showUserAvatar,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(showUserAvatar = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_show_assistant_bubble_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_show_assistant_bubble_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.showAssistantBubble,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(showAssistantBubble = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_chat_list_model_icon_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_chat_list_model_icon_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.showModelIcon,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(showModelIcon = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_show_model_name_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_show_model_name_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.showModelName,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(showModelName = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_show_date_below_name_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_show_date_below_name_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.showDateBelowName,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(showDateBelowName = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_show_token_usage_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_show_token_usage_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.showTokenUsage,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(showTokenUsage = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_show_thinking_content_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_show_thinking_content_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.showThinkingContent,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(showThinkingContent = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_auto_collapse_thinking_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_auto_collapse_thinking_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.autoCloseThinking,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(autoCloseThinking = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_enable_latex_rendering_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_enable_latex_rendering_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.enableLatexRendering,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(enableLatexRendering = it))
                                    }
                                )
                            },
                        )
                        val chatFontFamilyOptions = listOf(
                            ChatFontFamily.DEFAULT to stringResource(R.string.setting_display_page_chat_font_family_default),
                            ChatFontFamily.SERIF to stringResource(R.string.setting_display_page_chat_font_family_serif),
                            ChatFontFamily.MONOSPACE to stringResource(R.string.setting_display_page_chat_font_family_monospace),
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_chat_font_family_title)) },
                            supportingContent = {
                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .fillMaxWidth()
                                ) {
                                    chatFontFamilyOptions.forEachIndexed { index, (family, label) ->
                                        SegmentedButton(
                                            selected = displaySetting.chatFontFamily == family,
                                            onClick = { updateDisplaySetting(displaySetting.copy(chatFontFamily = family)) },
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index,
                                                chatFontFamilyOptions.size
                                            ),
                                        ) {
                                            Text(
                                                text = label,
                                                fontFamily = when (family) {
                                                    ChatFontFamily.DEFAULT -> FontFamily.Default
                                                    ChatFontFamily.SERIF -> FontFamily.Serif
                                                    ChatFontFamily.MONOSPACE -> FontFamily.Monospace
                                                    ChatFontFamily.CUSTOM -> FontFamily.Default
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_font_size_title)) },
                            supportingContent = {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Slider(
                                            value = displaySetting.fontSizeRatio,
                                            onValueChange = {
                                                updateDisplaySetting(displaySetting.copy(fontSizeRatio = it))
                                            },
                                            valueRange = 0.5f..2f,
                                            steps = 11,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(text = "${(displaySetting.fontSizeRatio * 100).toInt()}%")
                                    }
                                    MarkdownBlock(
                                        content = stringResource(R.string.setting_display_page_font_size_preview),
                                        style = LocalTextStyle.current.copy(
                                            fontSize = LocalTextStyle.current.fontSize * displaySetting.fontSizeRatio,
                                            lineHeight = LocalTextStyle.current.lineHeight * displaySetting.fontSizeRatio,
                                            fontFamily = when (displaySetting.chatFontFamily) {
                                                ChatFontFamily.DEFAULT -> FontFamily.Default
                                                ChatFontFamily.SERIF -> FontFamily.Serif
                                                ChatFontFamily.MONOSPACE -> FontFamily.Monospace
                                                ChatFontFamily.CUSTOM -> FontFamily.Default
                                            }
                                        )
                                    )
                                }
                            }
                        )
                        item(
                            headlineContent = { Text("聊天气泡透明度") },
                            supportingContent = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Slider(
                                        value = displaySetting.chatBubbleTransparency,
                                        onValueChange = {
                                            updateDisplaySetting(displaySetting.copy(chatBubbleTransparency = it))
                                        },
                                        valueRange = 0f..100f,
                                        steps = 19,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(text = "${displaySetting.chatBubbleTransparency.toInt()}%")
                                }
                            }
                        )
                        item(
                            headlineContent = { Text("思维链透明度") },
                            supportingContent = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Slider(
                                        value = displaySetting.thinkingChainTransparency,
                                        onValueChange = {
                                            updateDisplaySetting(displaySetting.copy(thinkingChainTransparency = it))
                                        },
                                        valueRange = 0f..100f,
                                        steps = 19,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(text = "${displaySetting.thinkingChainTransparency.toInt()}%")
                                }
                            }
                        )
                    }
                }
            }

            // Custom Font, Input Background, Avatar Frame section
            item {
                val context = LocalContext.current
                val fontDir = remember { File(context.filesDir, "custom_fonts").apply { mkdirs() } }
                val bgDir = remember { File(context.filesDir, "input_backgrounds").apply { mkdirs() } }
                // Font picker launcher
                val fontPickerLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let {
                        val fileName = it.pathSegments?.last()?.substringAfterLast("/") ?: "custom_font"
                        val destFile = File(fontDir, "custom_font_${System.currentTimeMillis()}.ttf")
                        context.contentResolver.openInputStream(it)?.use { input ->
                            destFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        updateDisplaySetting(displaySetting.copy(
                            chatFontFamily = ChatFontFamily.CUSTOM,
                            customFontPath = destFile.absolutePath
                        ))
                    }
                }

                // Input background picker launcher
                val bgPickerLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let {
                        val destFile = File(bgDir, "input_bg_${System.currentTimeMillis()}.png")
                        context.contentResolver.openInputStream(it)?.use { input ->
                            destFile.outputStream().use { output -> input.copyTo(output) }
                        }
                        updateDisplaySetting(displaySetting.copy(inputBackgroundPath = destFile.absolutePath))
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Custom Font Import
                    CardGroup(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        title = { Text("自定义字体") },
                    ) {
                        item(
                            headlineContent = { Text("导入自定义字体") },
                            supportingContent = {
                                Text(
                                    if (displaySetting.customFontPath.isNotBlank() && File(displaySetting.customFontPath).exists())
                                        "当前字体: ${File(displaySetting.customFontPath).name}"
                                    else "支持 .ttf / .otf 字体文件"
                                )
                            },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (displaySetting.customFontPath.isNotBlank()) {
                                        TextButton(onClick = {
                                            File(displaySetting.customFontPath).delete()
                                            updateDisplaySetting(displaySetting.copy(
                                                customFontPath = "",
                                                chatFontFamily = ChatFontFamily.DEFAULT
                                            ))
                                        }) { Text("清除") }
                                    }
                                    TextButton(onClick = {
                                        fontPickerLauncher.launch(arrayOf("*/*"))
                                    }) { Text("选择字体") }
                                }
                            },
                        )
                        if (displaySetting.customFontPath.isNotBlank() && File(displaySetting.customFontPath).exists()) {
                            item(
                                headlineContent = { Text("字体预览") },
                                supportingContent = {
                                    val customFont = remember(displaySetting.customFontPath) {
                                        runCatching { FontFamily(Font(File(displaySetting.customFontPath))) }
                                            .getOrDefault(FontFamily.Default)
                                    }
                                    Text(
                                        text = "The quick brown fox jumps over the lazy dog. 你好世界！1234567890",
                                        fontFamily = customFont,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            )
                        }
                    }

                    // Input Background
                    CardGroup(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        title = { Text("输入框背景") },
                    ) {
                        item(
                            headlineContent = { Text("自定义输入框背景图") },
                            supportingContent = {
                                Text(
                                    if (displaySetting.inputBackgroundPath.isNotBlank() && File(displaySetting.inputBackgroundPath).exists())
                                        "当前背景: ${File(displaySetting.inputBackgroundPath).name}"
                                    else "选择一张图片作为输入框区域背景"
                                )
                            },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (displaySetting.inputBackgroundPath.isNotBlank()) {
                                        TextButton(onClick = {
                                            File(displaySetting.inputBackgroundPath).delete()
                                            updateDisplaySetting(displaySetting.copy(inputBackgroundPath = ""))
                                        }) { Text("清除") }
                                    }
                                    TextButton(onClick = {
                                        bgPickerLauncher.launch(arrayOf("image/*"))
                                    }) { Text("选择图片") }
                                }
                            },
                        )
                    }

                    // Avatar Frame (QQ-style decoration)
                    val frameDir = remember { File(context.filesDir, "avatar_frames").apply { mkdirs() } }
                    val userFramePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                        uri?.let {
                            val destFile = File(frameDir, "user_frame_${System.currentTimeMillis()}.png")
                            context.contentResolver.openInputStream(it)?.use { input ->
                                destFile.outputStream().use { output -> input.copyTo(output) }
                            }
                            updateDisplaySetting(displaySetting.copy(userAvatarFramePath = destFile.absolutePath))
                        }
                    }
                    val aiFramePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                        uri?.let {
                            val destFile = File(frameDir, "ai_frame_${System.currentTimeMillis()}.png")
                            context.contentResolver.openInputStream(it)?.use { input ->
                                destFile.outputStream().use { output -> input.copyTo(output) }
                            }
                            updateDisplaySetting(displaySetting.copy(aiAvatarFramePath = destFile.absolutePath))
                        }
                    }

                    CardGroup(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        title = { Text("头像挂件") },
                    ) {
                        // ===== 用户头像挂件 =====
                        item(
                            headlineContent = { Text("用户头像挂件") },
                            supportingContent = {
                                if (displaySetting.userAvatarFramePath.isBlank() || !File(displaySetting.userAvatarFramePath).exists()) {
                                    Text("选择一张图片作为头像装饰框")
                                }
                            },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (displaySetting.userAvatarFramePath.isNotBlank()) {
                                        TextButton(onClick = {
                                            File(displaySetting.userAvatarFramePath).delete()
                                            updateDisplaySetting(displaySetting.copy(userAvatarFramePath = ""))
                                        }) { Text("清除") }
                                    }
                                    TextButton(onClick = { userFramePicker.launch(arrayOf("image/*")) }) { Text("选择") }
                                }
                            },
                        )
                        if (displaySetting.userAvatarFramePath.isNotBlank() && File(displaySetting.userAvatarFramePath).exists()) {
                            val userFrameBitmap = remember(displaySetting.userAvatarFramePath) {
                                android.graphics.BitmapFactory.decodeFile(displaySetting.userAvatarFramePath)
                            }
                            if (userFrameBitmap != null) {
                                // 实时预览：圆形头像 + 挂件叠加
                                item(
                                    headlineContent = { Text("预览") },
                                    supportingContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // 参考圆形头像
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                            // 挂件叠加层
                                            Box(
                                                modifier = Modifier
                                                    .offset(
                                                        x = displaySetting.userAvatarFrameOffsetX.dp,
                                                        y = displaySetting.userAvatarFrameOffsetY.dp
                                                    )
                                                    .size((80 * displaySetting.userAvatarFrameScale).dp)
                                            ) {
                                                Image(
                                                    bitmap = userFrameBitmap.asImageBitmap(),
                                                    contentDescription = "用户头像挂件",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit,
                                                )
                                            }
                                        }
                                    },
                                )
                                // 偏移 X
                                item(
                                    headlineContent = { Text("偏移 X") },
                                    supportingContent = {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Slider(
                                                value = displaySetting.userAvatarFrameOffsetX,
                                                onValueChange = { updateDisplaySetting(displaySetting.copy(userAvatarFrameOffsetX = it)) },
                                                valueRange = -100f..100f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${displaySetting.userAvatarFrameOffsetX.toInt()}")
                                        }
                                    },
                                )
                                // 偏移 Y
                                item(
                                    headlineContent = { Text("偏移 Y") },
                                    supportingContent = {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Slider(
                                                value = displaySetting.userAvatarFrameOffsetY,
                                                onValueChange = { updateDisplaySetting(displaySetting.copy(userAvatarFrameOffsetY = it)) },
                                                valueRange = -100f..100f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${displaySetting.userAvatarFrameOffsetY.toInt()}")
                                        }
                                    },
                                )
                                // 缩放
                                item(
                                    headlineContent = { Text("缩放") },
                                    supportingContent = {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Slider(
                                                value = displaySetting.userAvatarFrameScale,
                                                onValueChange = { updateDisplaySetting(displaySetting.copy(userAvatarFrameScale = it)) },
                                                valueRange = 0.5f..2f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${(displaySetting.userAvatarFrameScale * 100).toInt()}%")
                                        }
                                    },
                                )
                            }
                        }

                        // ===== AI头像挂件 =====
                        item(
                            headlineContent = { Text("AI头像挂件") },
                            supportingContent = {
                                if (displaySetting.aiAvatarFramePath.isBlank() || !File(displaySetting.aiAvatarFramePath).exists()) {
                                    Text("选择一张图片作为AI头像装饰框")
                                }
                            },
                            trailingContent = {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (displaySetting.aiAvatarFramePath.isNotBlank()) {
                                        TextButton(onClick = {
                                            File(displaySetting.aiAvatarFramePath).delete()
                                            updateDisplaySetting(displaySetting.copy(aiAvatarFramePath = ""))
                                        }) { Text("清除") }
                                    }
                                    TextButton(onClick = { aiFramePicker.launch(arrayOf("image/*")) }) { Text("选择") }
                                }
                            },
                        )
                        if (displaySetting.aiAvatarFramePath.isNotBlank() && File(displaySetting.aiAvatarFramePath).exists()) {
                            val aiFrameBitmap = remember(displaySetting.aiAvatarFramePath) {
                                android.graphics.BitmapFactory.decodeFile(displaySetting.aiAvatarFramePath)
                            }
                            if (aiFrameBitmap != null) {
                                // 实时预览：圆形头像 + 挂件叠加
                                item(
                                    headlineContent = { Text("预览") },
                                    supportingContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // 参考圆形头像
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                            // 挂件叠加层
                                            Box(
                                                modifier = Modifier
                                                    .offset(
                                                        x = displaySetting.aiAvatarFrameOffsetX.dp,
                                                        y = displaySetting.aiAvatarFrameOffsetY.dp
                                                    )
                                                    .size((80 * displaySetting.aiAvatarFrameScale).dp)
                                            ) {
                                                Image(
                                                    bitmap = aiFrameBitmap.asImageBitmap(),
                                                    contentDescription = "AI头像挂件",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit,
                                                )
                                            }
                                        }
                                    },
                                )
                                // 偏移 X
                                item(
                                    headlineContent = { Text("偏移 X") },
                                    supportingContent = {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Slider(
                                                value = displaySetting.aiAvatarFrameOffsetX,
                                                onValueChange = { updateDisplaySetting(displaySetting.copy(aiAvatarFrameOffsetX = it)) },
                                                valueRange = -100f..100f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${displaySetting.aiAvatarFrameOffsetX.toInt()}")
                                        }
                                    },
                                )
                                // 偏移 Y
                                item(
                                    headlineContent = { Text("偏移 Y") },
                                    supportingContent = {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Slider(
                                                value = displaySetting.aiAvatarFrameOffsetY,
                                                onValueChange = { updateDisplaySetting(displaySetting.copy(aiAvatarFrameOffsetY = it)) },
                                                valueRange = -100f..100f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${displaySetting.aiAvatarFrameOffsetY.toInt()}")
                                        }
                                    },
                                )
                                // 缩放
                                item(
                                    headlineContent = { Text("缩放") },
                                    supportingContent = {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Slider(
                                                value = displaySetting.aiAvatarFrameScale,
                                                onValueChange = { updateDisplaySetting(displaySetting.copy(aiAvatarFrameScale = it)) },
                                                valueRange = 0.5f..2f,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("${(displaySetting.aiAvatarFrameScale * 100).toInt()}%")
                                        }
                                    },
                                )
                            }
                        }
                    }

                }
            }

            item {
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text(stringResource(R.string.setting_page_code_display_settings)) },
                ) {
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_code_block_auto_wrap_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_code_block_auto_wrap_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.codeBlockAutoWrap,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(codeBlockAutoWrap = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_code_block_auto_collapse_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_code_block_auto_collapse_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.codeBlockAutoCollapse,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(codeBlockAutoCollapse = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_show_line_numbers_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_show_line_numbers_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.showLineNumbers,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(showLineNumbers = it))
                                }
                            )
                        },
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    CardGroup(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        title = { Text(stringResource(R.string.setting_page_interaction_notification_settings)) },
                    ) {
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_send_on_enter_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_send_on_enter_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.sendOnEnter,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(sendOnEnter = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_show_message_jumper_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_show_message_jumper_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.showMessageJumper,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(showMessageJumper = it))
                                    }
                                )
                            },
                        )
                        if (displaySetting.showMessageJumper) {
                            item(
                                headlineContent = { Text(stringResource(R.string.setting_display_page_message_jumper_position_title)) },
                                supportingContent = { Text(stringResource(R.string.setting_display_page_message_jumper_position_desc)) },
                                trailingContent = {
                                    Switch(
                                        checked = displaySetting.messageJumperOnLeft,
                                        onCheckedChange = {
                                            updateDisplaySetting(displaySetting.copy(messageJumperOnLeft = it))
                                        }
                                    )
                                },
                            )
                        }
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_enable_auto_scroll_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_enable_auto_scroll_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.enableAutoScroll,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(enableAutoScroll = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_use_app_icon_style_loading_indicator_title)) },
                            supportingContent = {
                                Text(stringResource(R.string.setting_display_page_use_app_icon_style_loading_indicator_desc))
                            },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.useAppIconStyleLoadingIndicator,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(useAppIconStyleLoadingIndicator = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_enable_blur_effect_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_enable_blur_effect_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.enableBlurEffect,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(enableBlurEffect = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_enable_message_generation_haptic_effect_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_enable_message_generation_haptic_effect_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.enableMessageGenerationHapticEffect,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(enableMessageGenerationHapticEffect = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_skip_crop_image_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_skip_crop_image_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.skipCropImage,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(skipCropImage = it))
                                    }
                                )
                            },
                        )
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_paste_long_text_as_file_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_paste_long_text_as_file_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.pasteLongTextAsFile,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(pasteLongTextAsFile = it))
                                    }
                                )
                            },
                        )
                        if (displaySetting.pasteLongTextAsFile) {
                            item(
                                headlineContent = { Text(stringResource(R.string.setting_display_page_paste_long_text_threshold_title)) },
                                supportingContent = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Slider(
                                            value = displaySetting.pasteLongTextThreshold.toFloat(),
                                            onValueChange = {
                                                updateDisplaySetting(displaySetting.copy(pasteLongTextThreshold = it.toInt()))
                                            },
                                            valueRange = 100f..10000f,
                                            steps = 98,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(text = "${displaySetting.pasteLongTextThreshold}")
                                    }
                                },
                            )
                        }
                        item(
                            headlineContent = { Text(stringResource(R.string.setting_display_page_volume_key_scroll_title)) },
                            supportingContent = { Text(stringResource(R.string.setting_display_page_volume_key_scroll_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = displaySetting.enableVolumeKeyScroll,
                                    onCheckedChange = {
                                        updateDisplaySetting(displaySetting.copy(enableVolumeKeyScroll = it))
                                    }
                                )
                            },
                        )
                        if (displaySetting.enableVolumeKeyScroll) {
                            item(
                                headlineContent = { Text(stringResource(R.string.setting_display_page_volume_key_scroll_ratio)) },
                                supportingContent = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Slider(
                                            value = displaySetting.volumeKeyScrollRatio,
                                            onValueChange = {
                                                updateDisplaySetting(displaySetting.copy(volumeKeyScrollRatio = it))
                                            },
                                            valueRange = 0.25f..1.0f,
                                            steps = 2,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(text = "${(displaySetting.volumeKeyScrollRatio * 100).toInt()}%")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                CardGroup(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    title = { Text(stringResource(R.string.setting_page_tts_settings)) },
                ) {
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_tts_only_read_quoted_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_tts_only_read_quoted_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.ttsOnlyReadQuoted,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(ttsOnlyReadQuoted = it))
                                }
                            )
                        },
                    )
                    item(
                        headlineContent = { Text(stringResource(R.string.setting_display_page_auto_play_tts_title)) },
                        supportingContent = { Text(stringResource(R.string.setting_display_page_auto_play_tts_desc)) },
                        trailingContent = {
                            Switch(
                                checked = displaySetting.autoPlayTTSAfterGeneration,
                                onCheckedChange = {
                                    updateDisplaySetting(displaySetting.copy(autoPlayTTSAfterGeneration = it))
                                }
                            )
                        },
                    )
                }
            }
        }
    }
}
