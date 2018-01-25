package gs.property

import gs.environment.Environment
import gs.environment.Worker
import gs.kar.BuildConfig

abstract class Version {
        abstract val appName: IProperty<String>
        abstract val name: IProperty<String>
        abstract val code: IProperty<Int>
        abstract val nameCore: IProperty<String>
}

class VersionImpl (
        kctx: Worker,
        xx: Environment
) : Version() {

        override val appName = newProperty(kctx, { "gs" })
        override val name = newProperty(kctx, { "0.0" })
        override val code = newPersistedProperty(kctx, BasicPersistence(xx, "version_code"), { 0 })
        override val nameCore = newProperty(kctx, { BuildConfig.VERSION_NAME })
}
