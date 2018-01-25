package gs.presentation

import android.app.Activity
import com.github.salomonbrys.kodein.instance
import gs.environment.ActivityProvider
import gs.environment.inject
import gs.kar.R

class SimpleDialog(
        private val ctx: android.content.Context,
        private val viewId: Int,
        private val additionalButton: Int? = null
) {
    private val activity by lazy { ctx.inject().instance<ActivityProvider<Activity>>() }
    private val themedContext by lazy { android.view.ContextThemeWrapper(ctx, R.style.GsTheme_Dialog) }
    val view = android.view.LayoutInflater.from(themedContext).inflate(viewId, null, false)
    private val dialog: android.app.AlertDialog

    init {
        val d = android.app.AlertDialog.Builder(activity.get())
        d.setView(view)
        d.setPositiveButton(R.string.welcome_continue, { dia, int -> })
        if (additionalButton != null) d.setNeutralButton(ctx.getString(additionalButton), { dia, int -> })
        dialog = d.create()
    }

    fun show() {
        dialog.show()
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            listener(1)
            listener = {}
            dialog.dismiss()
        }

        dialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            listener(2)
            listener = {}
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            listener(null)
            listener = {}
        }

        dialog.window.clearFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )
    }

    var listener = { button: Int? -> }

}

