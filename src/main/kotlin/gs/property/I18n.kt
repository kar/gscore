package gs.property

import android.content.Context
import android.content.res.Resources
import com.github.salomonbrys.kodein.instance
import gs.environment.Environment
import gs.environment.Worker
import gs.main.getPreferredLocales

abstract class I18n {
    abstract val locale: IProperty<String>
    abstract val localised: (key: Any) -> String
    abstract fun getString(resId: Int): String
    abstract fun getQuantityString(resId: Int, quantity: Int, vararg arguments: Any): String
    abstract fun contentUrl(): String
}

typealias LanguageTag = String
typealias Key = String
typealias Localised = String

class I18nImpl (
        private val kctx: Worker,
        private val xx: Environment
) : I18n() {

    private val ctx: Context by xx.instance()
    private val repo: Repo by xx.instance()
    private val res: Resources by lazy { ctx.resources }

    init {
        repo.content.doWhenSet().then {
            locale.refresh(force = true)
        }
    }

    override fun contentUrl(): String {
        return "%s/%s".format(repo.content().contentPath ?: "http://localhost", locale())
    }

    override val locale = newPersistedProperty(kctx, BasicPersistence(xx, "locale"), { "en" },
            refresh = {
                val preferred = getPreferredLocales()
                val available = repo.content().locales

                /**
                 * Since pulling in proper locale lookup would take a lot of code dependencies, for now
                 * I coded up something dead simple. If no exact match is found amoung available locales,
                 * try matching just the language tag. This isn't a nice approach, but since we will support
                 * only the main languages for a long time to come, it should do the job.
                 */
                val exact = preferred.firstOrNull { available.contains(it) }
                val tag = if (exact == null) {
                    val langs = preferred.map { it.language }.distinct()
                    val lang = langs.firstOrNull { available.map { it.language }.contains(it) }
                    lang ?: "en"
                } else exact.toString()
                tag
            })

    private val localisedMap: MutableMap<LanguageTag, Map<Key, Localised>> = mutableMapOf()

    override val localised = { key: Any ->
        // Map resId to actual string key defined in xml files since we use them dynamically
        val realKey = if (key is Int) res.getResourceName(key) else key.toString()

        // Get all cached translations for current locale
        val strings = localisedMap.getOrPut(locale(), { mutableMapOf<Key, Localised>() })

        // If cache miss, just return the string key itself
        strings.getOrElse(realKey, { realKey })
    }

    override fun getString(resId: Int): String {
        return localised(resId)
    }

    override fun getQuantityString(resId: Int, quantity: Int, vararg arguments: Any): String {
        // Intentionally no support for quantity strings for now
        return localised(resId).format(arguments)
    }

}
