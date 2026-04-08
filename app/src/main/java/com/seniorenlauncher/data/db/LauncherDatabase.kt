package com.seniorenlauncher.data.db
import androidx.room.*
import com.seniorenlauncher.data.model.*
import kotlinx.coroutines.flow.Flow

@Database(entities = [QuickContact::class, Medication::class, MedicationLog::class, Note::class, CalendarEvent::class, EmergencyInfo::class, AlarmEntry::class, RadioStation::class, WeatherLocation::class, BlockedNumber::class], version = 9, exportSchema = false)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun medicationDao(): MedicationDao
    abstract fun noteDao(): NoteDao
    abstract fun calendarDao(): CalendarDao
    abstract fun emergencyDao(): EmergencyDao
    abstract fun alarmDao(): AlarmDao
    abstract fun radioDao(): RadioDao
    abstract fun weatherDao(): WeatherDao
    abstract fun blockedDao(): BlockedDao
}

@Dao interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY sortOrder") fun getAll(): Flow<List<QuickContact>>
    @Query("SELECT * FROM contacts ORDER BY sortOrder") suspend fun getAllSync(): List<QuickContact>
    @Query("SELECT * FROM contacts WHERE isSosContact = 1") fun getSosContacts(): Flow<List<QuickContact>>
    @Query("SELECT * FROM contacts WHERE isSosContact = 1") suspend fun getSosContactsSync(): List<QuickContact>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(c: QuickContact)
    @Delete suspend fun delete(c: QuickContact)
}

@Dao interface BlockedDao {
    @Query("SELECT * FROM blocked_numbers") fun getAll(): Flow<List<BlockedNumber>>
    @Query("SELECT * FROM blocked_numbers") suspend fun getAllSync(): List<BlockedNumber>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(b: BlockedNumber)
    @Delete suspend fun delete(b: BlockedNumber)
}

@Dao interface MedicationDao {
    @Query("SELECT * FROM medications WHERE active = 1") fun getActive(): Flow<List<Medication>>
    @Query("SELECT * FROM medications WHERE active = 1 AND isPending = 1") fun getPending(): Flow<List<Medication>>
    @Query("SELECT * FROM medications") suspend fun getAllSync(): List<Medication>
    @Query("SELECT * FROM medications WHERE id = :id") suspend fun getById(id: Long): Medication?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(m: Medication): Long
    @Update suspend fun update(m: Medication)
    @Delete suspend fun delete(m: Medication)

    // Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertLog(log: MedicationLog)
    @Query("SELECT * FROM medication_logs WHERE medicationId = :medId ORDER BY date DESC, time DESC LIMIT 50")
    fun getLogsForMedication(medId: Long): Flow<List<MedicationLog>>
    @Query("SELECT * FROM medication_logs ORDER BY date DESC, time DESC LIMIT 100")
    fun getAllLogs(): Flow<List<MedicationLog>>
}
@Dao interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC") fun getAll(): Flow<List<Note>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(n: Note)
    @Delete suspend fun delete(n: Note)
}
@Dao interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE dateTime >= :from AND dateTime < :to ORDER BY dateTime")
    fun getEventsInRange(from: Long, to: Long): Flow<List<CalendarEvent>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(e: CalendarEvent)
}
@Dao interface EmergencyDao {
    @Query("SELECT * FROM emergency_info WHERE id = 1") fun get(): Flow<EmergencyInfo?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(info: EmergencyInfo)
}
@Dao interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour, minute") fun getAll(): Flow<List<AlarmEntry>>
    @Query("SELECT * FROM alarms") suspend fun getAllSync(): List<AlarmEntry>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(a: AlarmEntry): Long
    @Update suspend fun update(a: AlarmEntry)
    @Delete suspend fun delete(a: AlarmEntry)
}
@Dao interface RadioDao {
    @Query("SELECT * FROM radio_stations ORDER BY category, name") fun getAll(): Flow<List<RadioStation>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(s: RadioStation)
    @Delete suspend fun delete(s: RadioStation)
}
@Dao interface WeatherDao {
    @Query("SELECT * FROM weather_locations") fun getAll(): Flow<List<WeatherLocation>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(l: WeatherLocation)
    @Delete suspend fun delete(l: WeatherLocation)
}
