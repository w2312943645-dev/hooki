package com.example.globalvpnhook

import android.util.Log
import org.lsposed.lspdroid.LSPosed
import org.lsposed.lspdroid.callbacks.OnHookLoadPackage
import org.lsposed.lspdroid.hooks.Hook
import java.lang.reflect.Field

class HookMain : OnHookLoadPackage {
    companion object {
        private const val TAG = "GlobalVpnHook"
        private const val TARGET_PKG = "com.akspeed.jiasuqi.gameboost"
        private const val VPN_PROFILE_CLASS = "com.yunshi.openlibrary.openvpn.VpnProfile"
        private const val OPENVPN_CLIENT_CLASS = "com.yunshi.openlibrary.openvpn.OpenVpnClient"
    }

    override fun onHookLoadPackage(pkg: String, classLoader: ClassLoader) {
        if (pkg != TARGET_PKG) return
        Log.i(TAG, "Target app loaded, installing hooks...")

        try {
            val vpnProfileClass = classLoader.loadClass(VPN_PROFILE_CLASS)
            val openVpnClientClass = classLoader.loadClass(OPENVPN_CLIENT_CLASS)

            // 读取 assets 白名单（可选）
            val whitelist = mutableSetOf<String>()
            try {
                classLoader.getResourceAsStream("assets/lsp_whitelist.txt")?.use { input ->
                    input.bufferedReader().forEachLine { line ->
                        line.trim().takeIf { it.isNotEmpty() && !it.startsWith("#") }?.let { whitelist.add(it) }
                    }
                }
            } catch (_: Exception) { /* ignore */ }
            // 必保留系统包，防止 VPN 起来后界面卡死
            whitelist += setOf("com.android.systemui", "com.android.networkstack.inprocess")

            // ---------- Hook 1: startOpenVpn 前强制改字段 ----------
            Hook.onMethod(openVpnClientClass, "startOpenVpn")
                .before { param ->
                    // 拿到单例里的 VpnProfile
                    val profileField = openVpnClientClass.getDeclaredField("profile")
                    profileField.isAccessible = true
                    val profile = profileField.get(openVpnClientClass.getDeclaredField("INSTANCE").get(null))
                    if (profile == null) return@before

                    // 1) 黑名单模式：列表里的包不走 VPN，其余全走
                    val disallowedField = vpnProfileClass.getDeclaredField("mAllowedAppsVpnAreDisallowed")
                    disallowedField.isAccessible = true
                    disallowedField.setBoolean(profile, true)

                    // 2) 只保留白名单包
                    val allowedAppsField = vpnProfileClass.getDeclaredField("mAllowedAppsVpn")
                    allowedAppsField.isAccessible = true
                    val set = allowedAppsField.get(profile) as java.util.HashSet<String>
                    set.clear()
                    set.addAll(whitelist)

                    Log.i(TAG, "startOpenVpn: forced blacklist mode, allowedApps = ${set.size}")
                }
                .install()

            // ---------- Hook 2: 拦截后续写入 mAllowedAppsVpnAreDisallowed = false ----------
            Hook.onField(vpnProfileClass, "mAllowedAppsVpnAreDisallowed")
                .write { fieldHook ->
                    if (fieldHook.newValue == false) {
                        fieldHook.newValue = true
                        Log.d(TAG, "Intercepted mAllowedAppsVpnAreDisallowed=false -> forced true")
                    }
                }
                .install()

            // ---------- Hook 3: 拦截业务代码往 mAllowedAppsVpn 塞包名 ----------
            Hook.onField(vpnProfileClass, "mAllowedAppsVpn")
                .write { fieldHook ->
                    val set = fieldHook.newValue as? java.util.HashSet<String>
                    if (set != null && set.isNotEmpty()) {
                        val filtered = java.util.HashSet<String>()
                        filtered.addAll(whitelist)
                        fieldHook.newValue = filtered
                        Log.d(TAG, "Intercepted mAllowedAppsVpn set, filtered to whitelist only")
                    }
                }
                .install()

            Log.i(TAG, "All hooks installed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Hook install failed", e)
        }
    }
}