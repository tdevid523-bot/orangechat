package me.rerere.rikkahub.ui.pages.setting

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Camera01
import me.rerere.hugeicons.stroke.Location01
import me.rerere.hugeicons.stroke.Notification02
import me.rerere.hugeicons.stroke.SmartPhone01
import me.rerere.hugeicons.stroke.Watch01
import me.rerere.rikkahub.data.ai.tools.SystemTools
import me.rerere.rikkahub.data.datastore.SystemToolsSetting
import me.rerere.rikkahub.data.gadgetbridge.GadgetbridgeReader
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.components.ui.permission.PermissionAccessBackgroundLocation
import me.rerere.rikkahub.ui.components.ui.permission.PermissionAccessCoarseLocation
import me.rerere.rikkahub.ui.components.ui.permission.PermissionAccessFineLocation
import me.rerere.rikkahub.ui.components.ui.permission.PermissionCamera
import me.rerere.rikkahub.ui.components.ui.permission.PermissionManager
import me.rerere.rikkahub.ui.components.ui.permission.PermissionInfo
import me.rerere.rikkahub.ui.components.ui.permission.PermissionPostNotifications
import me.rerere.rikkahub.ui.components.ui.permission.rememberPermissionState
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

/**
 * 系统工具设置页面
 * 管理位置、通知、App使用、OCR等系统工具的权限和配置
 */
@Composable
fun SettingSystemToolsPage(vm: SettingVM = koinViewModel()) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    var systemToolsSetting by remember(settings) {
        mutableStateOf(settings.systemToolsSetting)
    }
    // 确保systemToolsSetting始终与最新的settings同步
    LaunchedEffect(settings) {
        systemToolsSetting = settings.systemToolsSetting
    }

    fun updateSystemToolsSetting(setting: SystemToolsSetting) {
        systemToolsSetting = setting
        vm.updateSettings(settings.copy(systemToolsSetting = setting))
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 权限状态
    val locationPermissions = buildSet {
        add(PermissionAccessFineLocation)
        add(PermissionAccessCoarseLocation)
        add(PermissionAccessBackgroundLocation)
    }
    val locationPermissionState = rememberPermissionState(permissions = locationPermissions)

    val notificationPermissionState = rememberPermissionState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setOf(PermissionPostNotifications)
        } else emptySet<PermissionInfo>()
    )

    val cameraPermissionState = rememberPermissionState(permissions = setOf(PermissionCamera))

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text("系统工具")
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


            // 位置服务
            item {
                CardGroup(
                    title = { Text("位置服务") },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    item(
                        leadingContent = {
                            Icon(
                                imageVector = HugeIcons.Location01,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("启用位置工具") },
                        supportingContent = {
                            Text("允许AI获取您的当前位置，并使用高德API转换为地址")
                        },
                        trailingContent = {
                            Switch(
                                checked = systemToolsSetting.locationAccess,
                                onCheckedChange = { enabled ->
                                    if (enabled && !locationPermissionState.allPermissionsGranted) {
                                        locationPermissionState.requestPermissions()
                                    }
                                    updateSystemToolsSetting(
                                        systemToolsSetting.copy(locationAccess = enabled)
                                    )
                                }
                            )
                        }
                    )
                    if (systemToolsSetting.locationAccess && !locationPermissionState.allPermissionsGranted) {
                        item(
                            headlineContent = { Text("⚠ 位置权限未授予") },
                            supportingContent = { Text("点击授权按钮授予位置权限") },
                            trailingContent = {
                                FilledTonalButton(onClick = {
                                    locationPermissionState.requestPermissions()
                                }) {
                                    Text("授权")
                                }
                            }
                        )
                    }
                    if (systemToolsSetting.locationAccess) {
                        item(
                            headlineContent = { Text("高德API Key") },
                            supportingContent = {
                                TextField(
                                    value = systemToolsSetting.amapApiKey,
                                    onValueChange = { key ->
                                        updateSystemToolsSetting(
                                            systemToolsSetting.copy(amapApiKey = key)
                                        )
                                    },
                                    placeholder = { Text("请输入高德Web服务API Key") },
                                    modifier = Modifier.fillMaxSize(),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small,
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // 通知服务
            item {
                CardGroup(
                    title = { Text("通知服务") },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    item(
                        leadingContent = {
                            Icon(
                                imageVector = HugeIcons.Notification02,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("启用通知工具") },
                        supportingContent = {
                            Text("允许AI读取今日通知，了解您的消息动态")
                        },
                        trailingContent = {
                            Switch(
                                checked = systemToolsSetting.notificationAccess,
                                onCheckedChange = { enabled ->
                                    updateSystemToolsSetting(
                                        systemToolsSetting.copy(notificationAccess = enabled)
                                    )
                                }
                            )
                        }
                    )
                    if (systemToolsSetting.notificationAccess) {
                        item(
                            headlineContent = { Text("通知访问权限") },
                            supportingContent = {
                                val cn = android.content.ComponentName(context, me.rerere.rikkahub.data.service.RikkaNotificationListenerService::class.java)
                                val enabled = try {
                                    Settings.Secure.getString(
                                        context.contentResolver,
                                        "enabled_notification_listeners"
                                    )?.contains(cn.flattenToString()) == true
                                } catch (_: Exception) { false }
                                if (enabled) {
                                    Text("✓ 已授予通知访问权限")
                                } else {
                                    Text("⚠ 需要在系统设置中授予通知访问权限")
                                }
                            },
                            trailingContent = {
                                val cn = android.content.ComponentName(context, me.rerere.rikkahub.data.service.RikkaNotificationListenerService::class.java)
                                val enabled = try {
                                    Settings.Secure.getString(
                                        context.contentResolver,
                                        "enabled_notification_listeners"
                                    )?.contains(cn.flattenToString()) == true
                                } catch (_: Exception) { false }
                                if (!enabled) {
                                    FilledTonalButton(onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }) {
                                        Text("去设置")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // App使用统计
            item {
                CardGroup(
                    title = { Text("应用使用统计") },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    item(
                        leadingContent = {
                            Icon(
                                imageVector = HugeIcons.SmartPhone01,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("启用应用使用工具") },
                        supportingContent = {
                            Text("允许AI查看您的应用使用情况和轨迹，需要使用情况访问权限")
                        },
                        trailingContent = {
                            Switch(
                                checked = systemToolsSetting.appUsageAccess,
                                onCheckedChange = { enabled ->
                                    updateSystemToolsSetting(
                                        systemToolsSetting.copy(appUsageAccess = enabled)
                                    )
                                }
                            )
                        }
                    )
                    if (systemToolsSetting.appUsageAccess) {
                        item(
                            headlineContent = { Text("使用情况访问权限") },
                            supportingContent = {
                                if (SystemTools.hasAppUsagePermission(context)) {
                                    Text("✓ 已授予使用情况访问权限")
                                } else {
                                    Text("⚠ 需要在系统设置中授予使用情况访问权限")
                                }
                            },
                            trailingContent = {
                                if (!SystemTools.hasAppUsagePermission(context)) {
                                    FilledTonalButton(onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }) {
                                        Text("去设置")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // 探索周边服务
            item {
                CardGroup(
                    title = { Text("探索周边") },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    item(
                        leadingContent = {
                            Icon(
                                imageVector = HugeIcons.Location01,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("启用周边探索") },
                        supportingContent = {
                            Text("允许AI使用高德API搜索周边POI，如餐厅、商店、景点等")
                        },
                        trailingContent = {
                            Switch(
                                checked = systemToolsSetting.locationExploreEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && !locationPermissionState.allPermissionsGranted) {
                                        locationPermissionState.requestPermissions()
                                    }
                                    updateSystemToolsSetting(
                                        systemToolsSetting.copy(locationExploreEnabled = enabled)
                                    )
                                }
                            )
                        }
                    )
                    if (systemToolsSetting.locationExploreEnabled) {
                        item(
                            headlineContent = { Text("搜索半径 (米)") },
                            supportingContent = {
                                OutlinedTextField(
                                    value = systemToolsSetting.locationExploreRadius.toString(),
                                    onValueChange = { value ->
                                        val radius = value.toIntOrNull()
                                        if (radius != null && radius > 0) {
                                            updateSystemToolsSetting(
                                                systemToolsSetting.copy(locationExploreRadius = radius)
                                            )
                                        }
                                    },
                                    placeholder = { Text("1000") },
                                    singleLine = true,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            },
                        )
                    }
                }
            }

            // Supabase 数据同步
            item {
                CardGroup(
                    title = { Text("Supabase 数据同步") },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    item(
                        leadingContent = {
                            Icon(
                                imageVector = HugeIcons.SmartPhone01,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("启用 Supabase 同步") },
                        supportingContent = {
                            Text("开启后立即同步一次，之后每15分钟自动同步")
                        },
                        trailingContent = {
                            Switch(
                                checked = systemToolsSetting.supabaseEnabled,
                                onCheckedChange = { enabled ->
                                    val newSetting = systemToolsSetting.copy(supabaseEnabled = enabled)
                                    updateSystemToolsSetting(newSetting)
                                    if (enabled) {
                                        me.rerere.rikkahub.data.service.SupabaseSyncService.triggerNow(context)
                                    } else {
                                        me.rerere.rikkahub.data.service.SupabaseSyncService.cancel(context)
                                    }
                                }
                            )
                        }
                    )
                    var supabaseNextTime by remember { mutableStateOf(me.rerere.rikkahub.data.service.SupabaseSyncService.getNextTriggerTime(context)) }
                    var supabaseSyncing by remember { mutableStateOf(me.rerere.rikkahub.data.service.SupabaseSyncService.isSyncing(context)) }
                    LaunchedEffect(systemToolsSetting) {
                        supabaseNextTime = me.rerere.rikkahub.data.service.SupabaseSyncService.getNextTriggerTime(context)
                        supabaseSyncing = me.rerere.rikkahub.data.service.SupabaseSyncService.isSyncing(context)
                        while (true) {
                            kotlinx.coroutines.delay(2_000L)
                            supabaseNextTime = me.rerere.rikkahub.data.service.SupabaseSyncService.getNextTriggerTime(context)
                            supabaseSyncing = me.rerere.rikkahub.data.service.SupabaseSyncService.isSyncing(context)
                        }
                    }
                    item(
                        headlineContent = { Text("同步状态") },
                        supportingContent = {
                            if (!systemToolsSetting.supabaseEnabled) {
                                Text("未启用")
                            } else if (supabaseSyncing) {
                                Text("🔄 同步中...")
                            } else {
                                val currentTime = System.currentTimeMillis()
                                val triggerTime = supabaseNextTime
                                if (triggerTime != null && triggerTime > currentTime) {
                                    val remaining = triggerTime - currentTime
                                    val remainMinutes = remaining / 60_000
                                    val remainSeconds = (remaining % 60_000) / 1000
                                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                    Text("🕐 下次: ${sdf.format(java.util.Date(triggerTime))}（剩余 ${remainMinutes}分${remainSeconds}秒）")
                                } else {
                                    Text("✅ 已开启，等待调度...")
                                }
                            }
                        }
                    )
                        item(
                            headlineContent = { Text("Supabase URL") },
                            supportingContent = {
                                TextField(
                                    value = systemToolsSetting.supabaseUrl,
                                    onValueChange = { url ->
                                        updateSystemToolsSetting(
                                            systemToolsSetting.copy(supabaseUrl = url)
                                        )
                                    },
                                    placeholder = { Text("https://xxxx.supabase.co") },
                                    modifier = Modifier.fillMaxSize(),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small,
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    )
                                )
                            }
                        )
                        item(
                            headlineContent = { Text("Supabase API Key") },
                            supportingContent = {
                                TextField(
                                    value = systemToolsSetting.supabaseApiKey,
                                    onValueChange = { key ->
                                        updateSystemToolsSetting(
                                            systemToolsSetting.copy(supabaseApiKey = key)
                                        )
                                    },
                                    placeholder = { Text("eyJhbGciOiJIUzI1NiIs...") },
                                    modifier = Modifier.fillMaxSize(),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small,
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    )
                                )
                            }
                        )
                        item(
                            headlineContent = { Text("数据表名") },
                            supportingContent = {
                                TextField(
                                    value = systemToolsSetting.supabaseTableName,
                                    onValueChange = { name ->
                                        updateSystemToolsSetting(
                                            systemToolsSetting.copy(supabaseTableName = name)
                                        )
                                    },
                                    placeholder = { Text("device_data") },
                                    modifier = Modifier.fillMaxSize(),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.small,
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    )
                                )
                            }
                        )
                        item(
                            headlineContent = { Text("说明") },
                            supportingContent = {
                                Text("需要先在 Supabase 创建数据表，表结构需包含以下字段：\n" +
                                    "timestamp (text), foreground_app (text), \n" +
                                    "location_latitude (float), location_longitude (float), \n" +
                                    "location_address (text), location_city (text), \n" +
                                    "location_district (text), location_street (text), \n" +
                                    "app_usage (text/jsonb), notifications (text/jsonb)")
                            }
                        )
                }
            }

            // 相机/拍照服务
            item {
                CardGroup(
                    title = { Text("相机/拍照服务") },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    item(
                        leadingContent = {
                            Icon(
                                imageVector = HugeIcons.Camera01,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("启用拍照工具") },
                        supportingContent = {
                            Text("允许AI在后台拍照并识别图像内容（物体、场景、文字等），照片会发送给AI进行视觉分析")
                        },
                        trailingContent = {
                            Switch(
                                checked = systemToolsSetting.cameraAccess,
                                onCheckedChange = { enabled ->
                                    if (enabled && !cameraPermissionState.allPermissionsGranted) {
                                        cameraPermissionState.requestPermissions()
                                    }
                                    updateSystemToolsSetting(
                                        systemToolsSetting.copy(cameraAccess = enabled)
                                    )
                                }
                            )
                        }
                    )
                    if (systemToolsSetting.cameraAccess && !cameraPermissionState.allPermissionsGranted) {
                        item(
                            headlineContent = { Text("⚠ 相机权限未授予") },
                            supportingContent = { Text("点击授权按钮授予相机权限") },
                            trailingContent = {
                                FilledTonalButton(onClick = {
                                    cameraPermissionState.requestPermissions()
                                }) {
                                    Text("授权")
                                }
                            }
                        )
                    }
                }
            }

            // Gadgetbridge 健康数据
            item {
                CardGroup(
                    title = { Text("Gadgetbridge 健康数据") },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    item(
                        leadingContent = {
                            Icon(
                                imageVector = HugeIcons.Watch01,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("启用 Gadgetbridge 工具") },
                        supportingContent = {
                            Text("允许AI读取手表的健康数据（步数、心率、睡眠等），需要存储权限和 Gadgetbridge 开启自动导出")
                        },
                        trailingContent = {
                            Switch(
                                checked = systemToolsSetting.gadgetbridgeEnabled,
                                onCheckedChange = { enabled ->
                                    updateSystemToolsSetting(
                                        systemToolsSetting.copy(gadgetbridgeEnabled = enabled)
                                    )
                                }
                            )
                        }
                    )
                    if (systemToolsSetting.gadgetbridgeEnabled) {
                        // Check storage permission FIRST - without permission, File.exists() returns false
                        val hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            android.os.Environment.isExternalStorageManager()
                        } else {
                            android.content.pm.PackageManager.PERMISSION_GRANTED == androidx.core.content.ContextCompat.checkSelfPermission(
                                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        }
                        if (!hasStoragePermission) {
                            item(
                                headlineContent = { Text("⚠ 存储权限未授予") },
                                supportingContent = { Text("需要存储权限才能读取 Gadgetbridge 导出的数据库文件") },
                                trailingContent = {
                                    FilledTonalButton(onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            try {
                                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                                intent.data = android.net.Uri.parse("package:${context.packageName}")
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                try {
                                                    context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    }) {
                                        Text("授权")
                                    }
                                }
                            )
                        }
                        // Only check DB file if we have storage permission
                        val dbExists = if (hasStoragePermission) GadgetbridgeReader.dbFileExists() else false
                        if (hasStoragePermission && !dbExists) {
                            item(
                                headlineContent = { Text("⚠ 数据库文件未找到") },
                                supportingContent = { Text("请在 Gadgetbridge 设置中开启\"自动导出\"功能，数据库路径应为 /sdcard/Download/手环/Gadgetbridge.db") }
                            )
                        }
                        // Check Gadgetbridge installation
                        val gbInstalled = try {
                            context.packageManager.getPackageInfo("nodomain.freeyourgadget.gadgetbridge", 0) != null
                        } catch (_: Exception) { false }
                        if (!gbInstalled) {
                            item(
                                headlineContent = { Text("⚠ Gadgetbridge 未安装") },
                                supportingContent = { Text("请先安装 Gadgetbridge 应用并配对您的穿戴设备") }
                            )
                        } else if (dbExists && hasStoragePermission) {
                            item(
                                headlineContent = { Text("✓ 数据读取正常") },
                                supportingContent = { Text("已找到数据库文件并拥有存储权限，AI可以读取健康数据") }
                            )
                        }
                    }
                }
            }

        }

        // 权限管理器
        PermissionManager(permissionState = locationPermissionState)
        PermissionManager(permissionState = notificationPermissionState)
        PermissionManager(permissionState = cameraPermissionState)
    }
}