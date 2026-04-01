package com.seniorenlauncher.ui.screens
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.seniorenlauncher.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class SettingsViewModel(app:Application):AndroidViewModel(app){
    private val _s=MutableStateFlow(AppSettings());val settings:StateFlow<AppSettings> =_s.asStateFlow()
    private val _t=MutableStateFlow(AppTheme.CLASSIC);val currentTheme:StateFlow<AppTheme> =_t.asStateFlow()
    fun updateTheme(t:AppTheme){_t.value=t;_s.value=_s.value.copy(theme=t)}
    fun updateLayout(l:LayoutType){_s.value=_s.value.copy(layout=l)}
    fun updateFontSize(v:Int){_s.value=_s.value.copy(fontSize=v)}
    fun toggleNight(){_s.value=_s.value.copy(nightModeAuto=!_s.value.nightModeAuto)}
    fun toggleFall(){_s.value=_s.value.copy(fallDetectionEnabled=!_s.value.fallDetectionEnabled)}
    fun toggleBattery(){_s.value=_s.value.copy(batteryAlertEnabled=!_s.value.batteryAlertEnabled)}
    fun toggleApp(id:String){val x=_s.value.visibleApps.toMutableSet();if(x.contains(id))x.remove(id)else x.add(id);_s.value=_s.value.copy(visibleApps=x)}
}
