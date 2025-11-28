package com.example.mirutadigital.ui.util


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MapIcons {

    private var inactiveStopBitmap: BitmapDescriptor? = null
    private var activeStopBitmap: BitmapDescriptor? = null

    fun getInactiveStopIcon(context: Context): BitmapDescriptor {
        return inactiveStopBitmap ?: createStopBitmap(context, isActive = false).also {
            inactiveStopBitmap = it
        }
    }

    fun getActiveStopIcon(context: Context): BitmapDescriptor {
        return activeStopBitmap ?: createStopBitmap(context, isActive = true).also {
            activeStopBitmap = it
        }
    }

    fun getBusIcon(context: Context, color: Color): BitmapDescriptor {
        return createBusBitmap(context, color)
    }

    private fun createStopBitmap(context: Context, isActive: Boolean): BitmapDescriptor {
        val baseSize = 36

        val bitmap = createBitmap(baseSize, baseSize)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // fondo blanco
        paint.color = android.graphics.Color.WHITE
        val outerRadius = baseSize / 2f - 3f // Medidas de 'isSmall = true'
        canvas.drawCircle(baseSize / 2f, baseSize / 2f, outerRadius, paint)

        // circulo rojo para activo y el cian para inactivo
        paint.color = if (isActive) android.graphics.Color.RED else 0xFF00BCD4.toInt()
        val innerRadius = baseSize / 2f - 6f
        canvas.drawCircle(baseSize / 2f, baseSize / 2f, innerRadius, paint)

        // borde
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(baseSize / 2f, baseSize / 2f, innerRadius, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun createBusBitmap(context: Context, busColor: Color): BitmapDescriptor {
        val baseSize = 50
        val bitmap = createBitmap(baseSize, baseSize)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = busColor.toArgb()
        canvas.drawCircle(baseSize / 2f, baseSize / 2f, baseSize / 2f - 2f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(baseSize / 2f, baseSize / 2f, baseSize / 2f - 2f, paint)

        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.WHITE
        val rectScale = 0.5f // Escala de 'isSmall = true'
        canvas.drawRect(25f * rectScale, 35f * rectScale, 75f * rectScale, 65f * rectScale, paint) // cuerpo
        canvas.drawRect(30f * rectScale, 40f * rectScale, 45f * rectScale, 50f * rectScale, paint) // ventana 1
        canvas.drawRect(55f * rectScale, 40f * rectScale, 70f * rectScale, 50f * rectScale, paint) // ventana 2

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}