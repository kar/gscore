package gs.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.github.salomonbrys.kodein.instance
import gs.environment.Environment
import gs.environment.Journal
import gs.kar.R
import gs.property.Version
import gs.property.Welcome

class WelcomeDialogManager (
        private val xx: Environment,
        private val currentAppVersion: Int,
        private val afterWelcome: () -> Unit
) {

    private val ctx: Context by xx.instance()
    private val welcome: Welcome by xx.instance()
    private val version: Version by xx.instance()
    private val j: Journal by xx.instance()

    private var displaying = false

    fun run(step: Int = 0) {
        when {
            displaying -> Unit
            welcome.introSeen(false) && step in listOf(0, 1) -> {
                dialogIntro.listener = { accept ->
                    displaying = false
                    if (accept == 1) {
                        welcome.introSeen %= true
                        afterWelcome()
                    }
                    run(step = 2)
                }
                dialogIntro.show()
                displaying = true
            }
            welcome.guideSeen(false) && step in listOf(0, 2) -> {
                dialogGuide.listener = { button ->
                    displaying = false
                    if (button != null) welcome.guideSeen %= true
                    if (button == 2) welcome.advanced %= true
                    run(step = 3)
                }
                dialogGuide.show()
                displaying = true
            }
            version.previousCode() < currentAppVersion && step in listOf(0, 3)-> {
                dialogUpdate.listener = { accept ->
                    displaying = false
                    if (accept == 1) version.previousCode %= currentAppVersion
                    welcome.optionalSeen %= false
                    welcome.ctaSeenCounter %= 0
                    run(step = 4)
                }
                dialogUpdate.show()
                displaying = true
            }
            welcome.optionalShow() && welcome.optionalSeen(false) && step in listOf(0, 3, 4) -> {
                dialogOptional.listener = { button ->
                    displaying = false
                    if (button == 2) {
                        optionalActor?.openInBrowser()
                    } else if (button != null) welcome.optionalSeen %= true
                    run(step = 5)
                }
                dialogOptional.show()
                displaying = true
            }
            step == 0 && welcome.ctaSeenCounter() > 0 -> welcome.ctaSeenCounter %= welcome.ctaSeenCounter() - 1
            step == 0 && welcome.ctaSeenCounter() == 0 -> {
                dialogCta.listener = { button ->
                    displaying = false
                    if (button != null) welcome.ctaSeenCounter %= 5
                }
                dialogCta.show()
                displaying = true
            }
            welcome.obsolete() -> dialogObsolete.show()
            getInstalledBuilds().size > 1 -> {
                dialogCleanup.listener = { accept ->
                    displaying = false
                    if (accept == 1) {
                        val builds = getInstalledBuilds()
                        for (b in builds.subList(1, builds.size).reversed()) {
                            uninstallPackage(b)
                            Toast.makeText(ctx, R.string.welcome_cleanup_done, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                dialogCleanup.show()
                displaying = true
            }
        }
    }

    private fun getInstalledBuilds(): List<String> {
        return welcome.conflictingBuilds().map {
            if (isPackageInstalled(it)) it else null
        }.filterNotNull()
    }

    private fun isPackageInstalled(appId: String): Boolean {
        val intent = ctx.packageManager.getLaunchIntentForPackage(appId) as Intent? ?: return false
        val activities = ctx.packageManager.queryIntentActivities(intent, 0)
        return activities.size > 0
    }

    private fun uninstallPackage(appId: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:" + appId)
            ctx.startActivity(intent)
        } catch (e: Exception) {
            j.log(e)
        }
    }

    private val dialogIntro by lazy {
        val dialog = SimpleDialog(ctx, R.layout.webview)
        WebViewActor(dialog.view, welcome.introUrl, reloadOnError = true)
        dialog
    }

    private val dialogGuide by lazy {
        val dialog = SimpleDialog(ctx, R.layout.webview, additionalButton = R.string.welcome_advanced)
        WebViewActor(dialog.view, welcome.guideUrl, reloadOnError = true)
        dialog
    }

    private val dialogOptional by lazy {
        val dialog = SimpleDialog(ctx, R.layout.webview, additionalButton = R.string.welcome_optional)
        dialog.view.minimumHeight = ctx.resources.toPx(480)
        optionalActor = WebViewActor(dialog.view, welcome.optionalUrl, forceEmbedded = true,
                reloadOnError = true, javascript = true)
        dialog
    }

    private var optionalActor: WebViewActor? = null

    private val dialogCta by lazy {
        val dialog = SimpleDialog(ctx, R.layout.webview)
        WebViewActor(dialog.view, welcome.ctaUrl, reloadOnError = true)
        dialog
    }

    private val dialogUpdate by lazy {
        val dialog = SimpleDialog(ctx, R.layout.webview)
        WebViewActor(dialog.view, welcome.updatedUrl, reloadOnError = true)
        dialog
    }

    private val dialogObsolete by lazy {
        val dialog = SimpleDialog(ctx, R.layout.webview)
        WebViewActor(dialog.view, welcome.obsoleteUrl, reloadOnError = true)
        dialog
    }

    private val dialogCleanup by lazy {
        val dialog = SimpleDialog(ctx, R.layout.webview)
        WebViewActor(dialog.view, welcome.cleanupUrl, reloadOnError = true)
        dialog
    }

}
