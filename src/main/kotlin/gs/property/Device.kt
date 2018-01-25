package gs.property

import android.os.PowerManager
import com.github.salomonbrys.kodein.instance
import gs.environment.Environment
import gs.environment.Worker

abstract class Device {
    abstract val appInForeground: IProperty<Boolean>
    abstract val screenOn: IProperty<Boolean>
}

class DeviceImpl (
        kctx: Worker,
        xx: Environment
) : Device() {

    private val pm: PowerManager by xx.instance()

    override val appInForeground = newProperty(kctx, { false })
    override val screenOn = newProperty(kctx, {
        if (android.os.Build.VERSION.SDK_INT >= 20) { pm.isInteractive } else { pm.isScreenOn }
    })

}
