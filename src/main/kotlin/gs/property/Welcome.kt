package gs.property

import gs.environment.Environment
import gs.environment.Worker
import java.net.URL

abstract class Welcome {
    abstract val introUrl: IProperty<URL>
    abstract val introSeen: IProperty<Boolean>
    abstract val guideUrl: IProperty<URL>
    abstract val guideSeen: IProperty<Boolean>
    abstract val optionalShow: IProperty<Boolean>
    abstract val optionalUrl: IProperty<URL>
    abstract val optionalSeen: IProperty<Boolean>
    abstract val ctaUrl: IProperty<URL>
    abstract val ctaSeenCounter: IProperty<Int>
    abstract val updatedUrl: IProperty<URL>
    abstract val advanced: IProperty<Boolean>
    abstract val obsoleteUrl: IProperty<URL>
    abstract val obsolete: IProperty<Boolean>
    abstract val cleanupUrl: IProperty<URL>
    abstract val conflictingBuilds: IProperty<List<String>>
}

class WelcomeImpl (
        w: Worker,
        xx: Environment
) : Welcome() {
    override val introUrl = newProperty(w, { URL("http://localhost") })
    override val introSeen = newPersistedProperty(w, BasicPersistence(xx, "intro_seen"), { false })
    override val guideUrl = newProperty(w, { URL("http://localhost") })
    override val guideSeen = newPersistedProperty(w, BasicPersistence(xx, "guide_seen"), { false })
    override val optionalShow = newProperty(w, { false })
    override val optionalUrl = newProperty(w, { URL("http://localhost") })
    override val optionalSeen = newPersistedProperty(w, BasicPersistence(xx, "optional_seen"), { false })
    override val ctaUrl = newProperty(w, { URL("http://localhost") })
    override val ctaSeenCounter = newPersistedProperty(w, BasicPersistence(xx, "cta_seen"), { 3 })
    override val updatedUrl = newProperty(w, { URL("http://localhost") })
    override val advanced = newPersistedProperty(w, BasicPersistence(xx, "advanced"), { false })
    override val obsoleteUrl = newProperty(w, { URL("http://localhost") })
    override val obsolete = newProperty(w, { false })
    override val cleanupUrl = newProperty(w, { URL("http://localhost") })
    override val conflictingBuilds = newProperty(w, { listOf<String>() })

}
