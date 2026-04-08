package com.seniorenlauncher.service

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import com.seniorenlauncher.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SeniorInCallService : InCallService() {

    companion object {
        private const val TAG = "SeniorInCallService"
        
        private val _currentCall = MutableStateFlow<Call?>(null)
        val currentCall: StateFlow<Call?> = _currentCall
        
        private val _callState = MutableStateFlow<Int>(Call.STATE_DISCONNECTED)
        val callState: StateFlow<Int> = _callState
        
        private val _audioState = MutableStateFlow<CallAudioState?>(null)
        val audioState: StateFlow<CallAudioState?> = _audioState
        
        private var instance: SeniorInCallService? = null

        private var forceSpeakerOnNextCall = false

        fun setForceSpeaker(enabled: Boolean) {
            forceSpeakerOnNextCall = enabled
            Log.d(TAG, "Force speaker set to: $enabled")
        }

        fun acceptCall() {
            val call = _currentCall.value
            if (call != null && call.state == Call.STATE_RINGING) {
                call.answer(0)
            }
        }

        fun endCall() {
            val call = _currentCall.value
            if (call != null) {
                if (call.state == Call.STATE_RINGING) {
                    call.reject(false, null)
                } else {
                    call.disconnect()
                }
            }
        }
        
        fun toggleSpeaker() {
            val currentAudio = _audioState.value ?: return
            val newRoute = if (currentAudio.route == CallAudioState.ROUTE_SPEAKER) {
                CallAudioState.ROUTE_WIRED_OR_EARPIECE
            } else {
                CallAudioState.ROUTE_SPEAKER
            }
            instance?.setAudioRoute(newRoute)
        }
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call?, state: Int) {
            Log.d(TAG, "Call state changed: $state")
            _callState.value = state
            
            if (state == Call.STATE_ACTIVE && forceSpeakerOnNextCall) {
                Log.d(TAG, "Call active, forcing speaker route")
                setAudioRoute(CallAudioState.ROUTE_SPEAKER)
                forceSpeakerOnNextCall = false // Reset na gebruik
            }

            _currentCall.value = null
            _currentCall.value = call
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(TAG, "Call added: ${call.details.handle}")
        call.registerCallback(callCallback)
        _currentCall.value = call
        _callState.value = call.state
        instance = this
        _audioState.value = callAudioState
        
        // Android 15/16: Start UI from background
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("NAVIGATE_TO", "incoming_call")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start call UI", e)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d(TAG, "Call removed")
        call.unregisterCallback(callCallback)
        _currentCall.value = null
        _callState.value = Call.STATE_DISCONNECTED
        instance = null
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        _audioState.value = audioState
    }
}
