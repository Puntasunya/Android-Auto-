package com.example.carmirror

import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import androidx.car.app.AppManager
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.validation.HostValidator

class MirrorCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen = MirrorScreen(carContext)
    }
}

class MirrorScreen(ctx: CarContext) : Screen(ctx) {
    private var vd: VirtualDisplay? = null

    init {
        ctx.getCarService(AppManager::class.java).setSurfaceCallback(object : SurfaceCallback {
            override fun onSurfaceAvailable(c: SurfaceContainer) {
                val surface = c.surface ?: return
                val w = c.width
                val h = c.height
                val dpi = c.dpi
                val existing = vd
                if (existing != null) {
                    existing.resize(w, h, dpi)
                    existing.surface = surface
                } else {
                    val p = MirrorHolder.projection ?: return
                    vd = p.createVirtualDisplay(
                        "CarMirror", w, h, dpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        surface, null, null
                    )
                }
            }

            override fun onSurfaceDestroyed(c: SurfaceContainer) {
                vd?.surface = null
            }
        })
    }

    override fun onGetTemplate(): Template =
        NavigationTemplate.Builder()
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(Action.Builder().setTitle("ออก")
                        .setOnClickListener { carContext.finishCarApp() }.build())
                    .build()
            )
            .build()
}