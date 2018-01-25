package gs.property

import android.content.Context
import com.github.salomonbrys.kodein.instance
import gs.environment.*

abstract class User {
    abstract val identity: IProperty<Identity>
}

class UserImpl (
    private val kctx: Worker,
    private val xx: Environment
) : User() {

    private val ctx: Context by xx.instance()

    override val identity = newPersistedProperty(kctx, AIdentityPersistence(ctx), { identityFrom("") })

}
