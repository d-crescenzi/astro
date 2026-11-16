package com.astro.app.data.sensors

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import com.astro.app.core.LOG
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed interface HrEvent {
    data class Reading(val bpm: Double) : HrEvent
    data class Unavailable(val offBody: Boolean) : HrEvent
}

// Live heart rate (~1 Hz) from Wear OS Health Services, plus availability changes.
class HeartRateSource(context: Context) {

    private val measureClient = HealthServices.getClient(context).measureClient

    fun events(): Flow<HrEvent> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                LOG.d("HR availability: $availability")
                if (availability != DataTypeAvailability.AVAILABLE && availability != DataTypeAvailability.ACQUIRING) {
                    trySendBlocking(HrEvent.Unavailable(offBody = availability == DataTypeAvailability.UNAVAILABLE_DEVICE_OFF_BODY))
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                data.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value
                    ?.takeIf { it > 0.0 }
                    ?.let { trySendBlocking(HrEvent.Reading(it)) }
            }
        }
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)
        awaitClose {
            measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, callback)
        }
    }
}
