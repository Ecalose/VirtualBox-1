package com.virtual.box.core.hook.service

import android.app.IServiceConnection
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Debug
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.virtual.box.base.util.compat.BuildCompat
import com.virtual.box.base.util.log.L
import com.virtual.box.base.util.log.Logger
import com.virtual.box.core.VirtualBox
import com.virtual.box.core.helper.ProviderHelper
import com.virtual.box.core.hook.BaseHookHandle
import com.virtual.box.core.hook.core.MethodHandle
import com.virtual.box.core.manager.AppActivityManager
import com.virtual.box.core.manager.AppActivityThread
import com.virtual.box.core.manager.VmAppPackageManager
import com.virtual.box.core.proxy.ProxyManifest
import com.virtual.box.reflect.android.app.HActivityManager
import com.virtual.box.reflect.android.app.HActivityManagerNative
import com.virtual.box.reflect.android.util.HSingleton


/**
 *
 * @author zhangzhipeng
 * @date   2022/4/25
 **/
@Suppress("UNUSED")
class ActivityManagerHookHandle : BaseHookHandle() {

    private val logger: Logger = Logger.getLogger(L.HOOK_TAG, "ActivityManagerHookHandle")

    override fun getOriginObject(): Any? {
        val iActivityManager: Any? = if (BuildCompat.isAtLeastOreo) {
            HActivityManager.IActivityManagerSingleton.get()
        } else {
            HActivityManagerNative.gDefault.get()
        }
        return HSingleton.get.call(iActivityManager)
    }

    override fun hookInject(target: Any, proxy: Any) {
        val iActivityManager: Any? = if (BuildCompat.isAtLeastOreo) {
            HActivityManager.IActivityManagerSingleton.get()
        } else {
            HActivityManagerNative.gDefault.get()
        }
        HSingleton.mInstance.set(iActivityManager, proxy)
    }

    override fun isHooked(): Boolean {
        return getOriginObject() == proxyInvocation
    }

    fun openContentUri(methodHandle: MethodHandle, uriString: String): ParcelFileDescriptor? {
        return methodHandle.invokeOriginMethod() as? ParcelFileDescriptor
    }

    fun registerUidObserver(methodHandle: MethodHandle, observer: Any?, watch: Int, cutPoint: Int, callingPackage: String) {
        methodHandle.invokeOriginMethod(
            arrayOf(
                observer, watch, cutPoint, hostPkg
            )
        )
    }

    fun isUidActive(methodHandle: MethodHandle, uid: Int, callingPackage: String): Boolean {
        return methodHandle.invokeOriginMethod(
            arrayOf(
                uid, hostPkg
            )
        ) as Boolean
    }

    fun getUidProcessState(methodHandle: MethodHandle, uid: Int, callingPackage: String): Int {
        return methodHandle.invokeOriginMethod(arrayOf(uid, hostPkg)) as Int
    }

    @Deprecated("maxTargetSdk=29", ReplaceWith("android.content.Context#startActivity(android.content.Intent) as Int"))
    fun startActivity(
        methodHandle: MethodHandle,
        caller: Any?, callingPackage: String?, intent: Intent?, resolvedType: String?,
        resultTo: IBinder?, resultWho: String?,
        requestCode: Int, flags: Int, profilerInfo: Any?, options: Bundle?
    ): Int {
        logger.e("startActivity#Deprecated > maxTargetSdk = 29")
        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, hostPkg, intent, resolvedType, resultTo, resultWho,
                requestCode, flags, profilerInfo, options
            )
        ) as Int
    }

    fun startActivityWithFeature(
        methodHandle: MethodHandle, caller: Any?, callingPackage: String,
        callingFeatureId: String?, intent: Intent?, resolvedType: String?,
        resultTo: IBinder?, resultWho: String?, requestCode: Int, flags: Int,
        profilerInfo: Any?, options: Bundle?
    ): Int {
        logger.e("startActivityWithFeature")
        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, hostPkg, callingFeatureId, intent, resolvedType, resultTo,
                resultWho, requestCode, flags, profilerInfo, options
            )
        ) as Int
    }

    fun finishActivity(methodHandle: MethodHandle, token: IBinder, code: Int, data: Intent, finishTask: Int): Boolean {
        // TODO 关闭窗口时
        return methodHandle.invokeOriginMethod() as Boolean
    }

    /**
     * @param receiver IIntentReceiver
     */
    @Deprecated(
        "maxTargetSdk=29",
        ReplaceWith("android.content.Context#registerReceiver(android.content.BroadcastReceiver, android.content.IntentFilter)")
    )
    fun registerReceiver(
        methodHandle: MethodHandle,
        caller: Any?, callerPackage: String,
        receiver: Any?, filter: IntentFilter?,
        requiredPermission: String?, userId: Int, flags: Int
    ): Intent? {
        logger.i("registerReceiver#callerPackage = %s", callerPackage)
        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, hostPkg, receiver, filter, requiredPermission, userId, flags
            )
        ) as Intent?
    }

    /**
     * @param receiver IIntentReceiver
     */
    fun registerReceiverWithFeature(
        methodHandle: MethodHandle,
        caller: Any, callerPackage: String,
        callingFeatureId: String?, receiverId: String?, receiver: Any?,
        filter: IntentFilter?, requiredPermission: String?, userId: Int, flags: Int
    ): Intent? {
        logger.i("registerReceiverWithFeature#callerPackage = %s", callerPackage)
        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, hostPkg, callingFeatureId, receiverId, receiver, filter,
                requiredPermission, userId, flags
            )
        ) as Intent?
    }

    fun registerReceiverWithFeature(
        methodHandle: MethodHandle,
        caller: Any, callerPackage: String,
        callingFeatureId: String?, receiver: Any?,
        filter: IntentFilter?, requiredPermission: String?, userId: Int, flags: Int
    ): Intent? {
        logger.i("registerReceiverWithFeature#callerPackage = %s", callerPackage)
        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, hostPkg, callingFeatureId, receiver, filter,
                requiredPermission, userId, flags
            )
        ) as Intent?
    }

    /**
     * @param caller IApplicationThread
     * @param resultTo IIntentReceiver
     */
    @Deprecated(
        "maxTargetSdk=29",
        ReplaceWith("android.content.Context#sendBroadcast(android.content.Intent)")
    )
    fun broadcastIntent(
        methodHandle: MethodHandle,
        caller: Any?, intent: Intent?,
        resolvedType: String, resultTo: Any?, resultCode: Int,
        resultData: String?, map: Bundle?, requiredPermissions: Array<String>?,
        appOp: Int, options: Bundle?, serialized: Boolean, sticky: Boolean, userId: Int
    ): Int {
        // TODO
        return methodHandle.invokeOriginMethod() as Int
    }

    /**
     * @param caller IApplicationThread
     * @param resultTo IIntentReceiver
     */
    fun broadcastIntentWithFeature(
        methodHandle: MethodHandle,
        caller: Any, callingFeatureId: String?,
        intent: Intent?, resolvedType: String?, resultTo: Any?, resultCode: Int,
        resultData: String?, map: Bundle?, requiredPermissions: Array<String>?, excludePermissions: Array<String>?,
        appOp: Int, options: Bundle?, serialized: Boolean, sticky: Boolean, userId: Int
    ): Int {
        // TODO
        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, callingFeatureId, intent, resolvedType, resultTo, resultCode,
                resultData, map, requiredPermissions, excludePermissions,
                appOp, options, serialized, sticky, userId
            )
        ) as Int
    }

    fun unbroadcastIntent(
        methodHandle: MethodHandle,
        caller: Any?, intent: Intent?, userId: Int
    ) {
        // TODO intent
        methodHandle.invokeOriginMethod()
    }

    fun moveTaskToFront(
        methodHandle: MethodHandle, caller: Any?, callingPackage: String?, task: Int,
        flags: Int, options: Bundle?
    ) {
        methodHandle.invokeOriginMethod(
            arrayOf(
                caller, hostPkg, task, flags, options
            )
        )
    }

    /**
     * @return ContentProviderHolder
     */
    fun getContentProvider(
        methodHandle: MethodHandle, caller: Any?, callingPackage: String?,
        name: String?, userId: Int, stable: Boolean
    ): Any? {
        if (callingPackage == VirtualBox.get().hostPkg) {
            logger.i("getContentProvider#host获取ContentProvider")
            return methodHandle.invokeOriginMethod()
        }
        if (ProxyManifest.isProxy(name)) {
            logger.i("getContentProvider#是代理Provider，调用源方法")
            return methodHandle.invokeOriginMethod()
        }
        if (name == "settings" || name == "media" || name == "telephony") {
            logger.i(
                "getContentProvider#获取的是系统Provider，替换为 %s 并调用源方法, args = %s", hostPkg, arrayOf(
                    caller, callingPackage, name, userId, stable
                ).contentToString()
            )
            val result = methodHandle.invokeOriginMethod(
                arrayOf(
                    caller, hostPkg, name, userId, stable
                )
            )
            ProviderHelper.replaceNewProvider(result)
            return result
        } else {
            logger.i("getContentProvider#获取非系统Provider pks = %s, auth = %s", callingPackage, name)
            val providerInfo = VmAppPackageManager.resolveContentProvider(
                name, PackageManager.GET_PROVIDERS,
                AppActivityThread.currentProcessVmUserId
            )
            if (providerInfo == null) {
                logger.e("getContentProvider#VmPMS获取ProviderInfo == null")
                return null
            }

            logger.i("getContentProvider#解析到Provider = %s", providerInfo)
            val packageName = providerInfo.packageName
            val processName = providerInfo.processName
            val initNewProcess = AppActivityManager.initNewProcess(packageName, processName, AppActivityThread.currentProcessVmUserId)
                ?: return methodHandle.invokeOriginMethod()
            logger.i("getContentProvider#初始化Provider进程成功 appConfig = %s", initNewProcess)
            var stubAuth = name
            var iContentProvider: IBinder? = null
            if (initNewProcess.mainProcessVmPid != AppActivityThread.currentProcessVmPid) {
                logger.i("getContentProvider#插件号进程不一致，是获取的非当前进程的Provider")
            }
            iContentProvider = AppActivityThread.acquireContentProviderClient(providerInfo)
            stubAuth = ProxyManifest.getProxyAuthorities(initNewProcess.vmProcessRecord!!.vmPid)

            if (iContentProvider == null) {
                logger.e("getContentProvider#获取Provider为空")
                return methodHandle.invokeOriginMethod(
                    arrayOf(
                        caller, hostPkg, name, userId, stable
                    )
                )
            }
            logger.e("getContentProvider#调用ams获取 ProviderHolder")
            val result = methodHandle.invokeOriginMethod(
                arrayOf(
                    caller, hostPkg, stubAuth, userId, stable
                )
            ) ?: return null

            ProviderHelper.replaceProviderAndInfo(result, providerInfo, AppActivityThread.mVmPackageName)

            return result
        }
    }

    fun getRunningServiceControlPanel(methodHandle: MethodHandle, service: ComponentName?): PendingIntent? {
        // TODO replace proxyService
        return methodHandle.invokeOriginMethod() as? PendingIntent
    }

    fun startService(
        methodHandle: MethodHandle, caller: Any?, service: Intent?,
        resolvedType: String?, requireForeground: Boolean, callingPackage: String?,
        callingFeatureId: String?, userId: Int
    ): ComponentName? {
        Debug.waitForDebugger()
        if (hostPkg != callingPackage) {
            // TODO BUserHandle.myUserId()
            val resolveService = VmAppPackageManager.resolveService(service, resolvedType, 0, AppActivityThread.currentProcessVmUserId)
            if (resolveService?.serviceInfo != null) {
                logger.i("startService#解析到虚拟程序的服务信息，使用自定义ams处理")
                val componentName = AppActivityManager.startService(service, resolvedType, requireForeground, userId)
                if (componentName != null) {
                    return componentName
                }
            }
        }

        logger.i("startService#服务进程启动服务返回ComponentName == null, 调用源方法处理")
        return methodHandle.invokeOriginMethod() as? ComponentName
    }

    fun startService(
        methodHandle: MethodHandle, caller: Any, service: Intent?,
        resolvedType: String?, requireForeground: Boolean, callingPackage: String?,
        userId: Int
    ): ComponentName? {
        return startService(methodHandle, caller, service, resolvedType, requireForeground, callingPackage, "", userId)
    }

    fun stopService(
        methodHandle: MethodHandle, caller: Any?, service: Intent?,
        resolvedType: String?, userId: Int
    ): Int {
        val calPkg = service?.getPackage() ?: service?.component?.packageName
        if (hostPkg != calPkg) {
            // TODO BUserHandle.myUserId()
            val resolveService = VmAppPackageManager.resolveService(service, resolvedType, 0, AppActivityThread.currentProcessVmUserId)
            if (resolveService?.serviceInfo != null) {
                logger.i("stopService#解析到虚拟程序的服务信息，使用自定义ams处理")
                val res = AppActivityManager.stopService(service, resolvedType, userId)
                if (res >= 0) {
                    return res
                }
            }
        }

        return methodHandle.invokeOriginMethod() as Int
    }

    fun bindService(
        methodHandle: MethodHandle, caller: Any?, token: IBinder?, service: Intent?,
        resolvedType: String?, connection: IServiceConnection?, flags: Int,
        callingPackage: String?, userId: Int
    ): Int {
        if (hostPkg != callingPackage) {
            val resolveService = VmAppPackageManager.resolveService(service, resolvedType, 0, AppActivityThread.currentProcessVmUserId)
            if (resolveService?.serviceInfo != null) {
                logger.i("bindService#解析到虚拟程序的服务信息，使用自定义ams处理")
                val res = AppActivityManager.bindService(service, token, resolvedType, connection, userId)
                if (res >= 0) {
                    return res
                }
            }
        }

        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, token, service, resolvedType, connection, flags,
                hostPkg, userId
            )
        ) as Int
    }

    fun bindIsolatedService(
        methodHandle: MethodHandle, caller: Any?, token: IBinder?, service: Intent?,
        resolvedType: String?, connection: IServiceConnection?, flags: Int,
        instanceName: String?, callingPackage: String?, userId: Int
    ): Int {
        if (hostPkg != callingPackage) {
            val resolveService = VmAppPackageManager.resolveService(service, resolvedType, 0, AppActivityThread.currentProcessVmUserId)
            if (resolveService?.serviceInfo != null) {
                logger.i("bindService#解析到虚拟程序的服务信息，使用自定义ams处理")
                val res = AppActivityManager.bindService(service, token, resolvedType, connection, userId)
                if (res >= 0) {
                    return res
                }
            }
        }
        logger.i("bindIsolatedService#callingPackage = %s, service = %s", callingPackage, service)
        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, token, service, resolvedType, connection, flags,
                instanceName, hostPkg, userId
            )
        ) as Int
    }

//    @Deprecated("maxTargetSdk 30")
//    fun setDebugApp(methodHandle: MethodHandle, packageName: String?,
//                    waitForDebugger: Boolean, persistent: Boolean) {
//        methodHandle.invokeOriginMethod()
//    }

//    fun setAgentApp(methodHandle: MethodHandle, packageName: String?, agent: String?) {
//        methodHandle.invokeOriginMethod()
//    }

    /**
     * @param watcher IInstrumentationWatcher
     * @param connection IUiAutomationConnection
     */
    fun startInstrumentation(
        methodHandle: MethodHandle, className: ComponentName?, profileFile: String?,
        flags: Int, arguments: Bundle?, watcher: Any?,
        connection: Any?, userId: Int,
        abiOverride: String?
    ): Boolean {
        // TODO className
        return methodHandle.invokeOriginMethod() as Boolean
    }

    fun grantUriPermission(
        methodHandle: MethodHandle, caller: Any?, targetPkg: String?, uri: Uri?,
        mode: Int, userId: Int
    ) {
        methodHandle.invokeOriginMethod()
    }

    fun revokeUriPermission(
        methodHandle: MethodHandle, caller: Any?, targetPkg: String?, uri: Uri?,
        mode: Int, userId: Int
    ) {
        methodHandle.invokeOriginMethod()
    }

    fun getIntentSender(
        methodHandle: MethodHandle, type: Int, packageName: String?, token: IBinder?,
        resultWho: String?, requestCode: Int, intents: Array<Intent>?, resolvedTypes: Array<String>?,
        flags: Int, options: Bundle?, userId: Int
    ): Any? {
        return methodHandle.invokeOriginMethod()
    }

    /**
     * @return IIntentSender
     */
    fun getIntentSenderWithFeature(
        methodHandle: MethodHandle, type: Int, packageName: String?, featureId: String?,
        token: IBinder?, resultWho: String?, requestCode: Int, intents: Array<Intent>?,
        resolvedTypes: Array<String>?, flags: Int, options: Bundle?, userId: Int
    ): Any? {
        return methodHandle.invokeOriginMethod()
    }

    /**
     * @param sender IIntentSender
     * @param workSource WorkSource
     */
    fun noteWakeupAlarm(
        methodHandle: MethodHandle, sender: Any?, workSource: Any?, sourceUid: Int,
        sourcePkg: String?, tag: String?
    ) {
        methodHandle.invokeOriginMethod(
            arrayOf(
                sender, workSource, sourceUid, hostPkg, tag
            )
        )
    }

    fun setServiceForeground(
        methodHandle: MethodHandle, className: ComponentName?, token: IBinder?,
        id: Int, notification: Notification?, flags: Int, foregroundServiceType: Int
    ) {
        // TODO className
        methodHandle.invokeOriginMethod()
    }

    fun getForegroundServiceType(methodHandle: MethodHandle, className: ComponentName?, token: IBinder?): Int {
        return methodHandle.invokeOriginMethod() as Int
    }

    /**
     * @param observer IPackageDataObserver
     */
    fun clearApplicationUserData(
        methodHandle: MethodHandle, packageName: String?, keepState: Boolean,
        observer: Any?, userId: Int
    ): Boolean {
        // TODO
        return methodHandle.invokeOriginMethod() as Boolean
    }

    fun forceStopPackage(methodHandle: MethodHandle, packageName: String?, userId: Int) {

        methodHandle.invokeOriginMethod()
    }

    fun killPids(methodHandle: MethodHandle, pids: IntArray, reason: String?, secure: Boolean): Boolean {
        return methodHandle.invokeOriginMethod() as Boolean
    }

    fun peekService(
        methodHandle: MethodHandle, service: Intent?, resolvedType: String?,
        callingPackage: String?
    ): IBinder? {
        if (hostPkg != callingPackage) {
            return AppActivityManager.peekService(service, resolvedType, AppActivityThread.currentProcessVmUserId)
        }
        return methodHandle.invokeOriginMethod(
            arrayOf(
                service, resolvedType, hostPkg
            )
        ) as? IBinder
    }

    @Deprecated("maxTargetSdk = 30")
    fun profileControl(
        methodHandle: MethodHandle, process: String?, userId: Int, start: Boolean,
        profilerInfo: Any?, profileType: Int
    ): Boolean {
        return methodHandle.invokeOriginMethod() as Boolean
    }

    fun bindBackupAgent(
        methodHandle: MethodHandle, packageName: String?, backupRestoreMode: Int, targetUserId: Int,
        operationType: Int
    ): Boolean {
        return methodHandle.invokeOriginMethod() as Boolean
    }

    fun backupAgentCreated(methodHandle: MethodHandle, packageName: String?, agent: IBinder?, userId: Int) {
        methodHandle.invokeOriginMethod()
    }

    fun unbindBackupAgent(methodHandle: MethodHandle, appInfo: ApplicationInfo?) {
        methodHandle.invokeOriginMethod()
    }

    fun handleIncomingUser(
        methodHandle: MethodHandle, callingPid: Int, callingUid: Int, userId: Int, allowAll: Boolean,
        requireFull: Boolean, name: String?, callerPackage: String?
    ): Int {
        return methodHandle.invokeOriginMethod() as Int
    }

    fun addPackageDependency(methodHandle: MethodHandle, packageName: String?) {
        methodHandle.invokeOriginMethod()
    }

    fun killApplication(methodHandle: MethodHandle, pkg: String?, appId: Int, userId: Int, reason: String?) {
        methodHandle.invokeOriginMethod()
    }

    fun killApplicationProcess(methodHandle: MethodHandle, processName: String?, uid: Int) {
        methodHandle.invokeOriginMethod()
    }

    fun killBackgroundProcesses(methodHandle: MethodHandle, packageName: String?, userId: Int) {
        methodHandle.invokeOriginMethod()
    }

    fun updatePersistentConfigurationWithAttribution(
        methodHandle: MethodHandle, values: Configuration?,
        callingPackageName: String?, callingAttributionTag: String?
    ) {
        methodHandle.invokeOriginMethod()
    }


    @Deprecated("maxTargetSdk = 29")
    fun startActivityAsUser(
        methodHandle: MethodHandle, caller: Any?, callingPackage: String?,
        intent: Intent?, resolvedType: String?, resultTo: IBinder?, resultWho: String?,
        requestCode: Int, flags: Int, profilerInfo: Any?,
        options: Bundle?, userId: Int
    ): Int {
        return methodHandle.invokeOriginMethod(
            arrayOf(
                caller, hostPkg, intent, resolvedType, resultTo, resultWho,
                requestCode, flags, profilerInfo, options, userId
            )
        ) as Int
    }

    fun startActivityAsUserWithFeature(
        methodHandle: MethodHandle, caller: Any?, callingPackage: String?,
        callingFeatureId: String?, intent: Intent?, resolvedType: String?,
        resultTo: IBinder?, resultWho: String?, requestCode: Int, flags: Int,
        profilerInfo: Any?, options: Bundle?, userId: Int
    ): Int {
        return methodHandle.invokeOriginMethod() as Int
    }

    fun getPackageProcessState(methodHandle: MethodHandle, packageName: String?, callingPackage: String?): Int {
        return methodHandle.invokeOriginMethod(
            arrayOf(
                hostPkg, hostPkg
            )
        ) as Int
    }

    fun updateDeviceOwner(methodHandle: MethodHandle, packageName: String?) {
        methodHandle.invokeOriginMethod(
            arrayOf(
                hostPkg
            )
        )
    }

    fun killPackageDependents(methodHandle: MethodHandle, packageName: String?, userId: Int) {
        methodHandle.invokeOriginMethod()
    }

    fun makePackageIdle(methodHandle: MethodHandle, packageName: String?, userId: Int) {
        methodHandle.invokeOriginMethod()
    }

    fun isVrModePackageEnabled(methodHandle: MethodHandle, packageName: ComponentName?): Boolean {
        return false
    }

    fun notifyLockedProfile(methodHandle: MethodHandle, userId: Int) {
        methodHandle.invokeOriginMethod()
    }

    fun startConfirmDeviceCredentialIntent(methodHandle: MethodHandle, intent: Intent?, options: Bundle?) {
        methodHandle.invokeOriginMethod()
    }

    /**
     * @param target IIntentSender
     * @param finishedReceiver IIntentReceiver
     */
    fun sendIntentSender(
        methodHandle: MethodHandle, target: Any?, whitelistToken: IBinder?, code: Int,
        intent: Intent?, resolvedType: String?, finishedReceiver: Any?,
        requiredPermission: String?, options: Bundle?
    ): Int {
        return methodHandle.invokeOriginMethod() as Int
    }

    fun isBackgroundRestricted(methodHandle: MethodHandle, packageName: String?): Boolean {
        return methodHandle.invokeOriginMethod() as Boolean
    }

    /**
     * @return ParceledListSlice<ApplicationExitInfo?>
     */
    fun getHistoricalProcessExitReasons(
        methodHandle: MethodHandle,
        packageName: String?,
        pid: Int, maxNum: Int, userId: Int
    ): Any? {
        return methodHandle.invokeOriginMethod()
    }

    /**
     * @param locusId LocusId
     */
    fun setActivityLocusContext(
        methodHandle: MethodHandle, activity: ComponentName?, locusId: Any?,
        appToken: IBinder?
    ) {
        methodHandle.invokeOriginMethod()
    }
}