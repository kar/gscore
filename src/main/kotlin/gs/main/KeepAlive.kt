package gs.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.github.salomonbrys.kodein.instance
import gs.environment.inject

/**
 *
 */
class AKeepAliveAgent {
    private val serviceConnection = object: ServiceConnection {
        @Synchronized override fun onServiceConnected(name: ComponentName, binder: IBinder) {}
        @Synchronized override fun onServiceDisconnected(name: ComponentName?) {}
    }

    fun bind(ctx: Context) {
        val intent = Intent(ctx, AKeepAliveService::class.java)
        intent.setAction(AKeepAliveService.Statics.BINDER_ACTION)
        ctx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbind(ctx: Context) {
        try { ctx.unbindService(serviceConnection) } catch (e: Exception) {}
    }
}

class AKeepAliveService : android.app.Service() {

    companion object Statics {
        val BINDER_ACTION = "AKeepAliveService"
    }

    class KeepAliveBinder : android.os.Binder()

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        val j: gs.environment.Journal = inject().instance()

        j.log("KeepAliveService start command")
        return android.app.Service.START_STICKY;
    }

    private var binder: gs.main.AKeepAliveService.KeepAliveBinder? = null
    override fun onBind(intent: android.content.Intent?): android.os.IBinder? {
        if (gs.main.AKeepAliveService.Statics.BINDER_ACTION.equals(intent?.action)) {
            binder = gs.main.AKeepAliveService.KeepAliveBinder()
            return binder
        }
        return null
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        binder = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        sendBroadcast(android.content.Intent("org.blokada.keepAlive"))
    }

}
