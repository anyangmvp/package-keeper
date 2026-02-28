package anyang.mypackages.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import androidx.core.content.ContextCompat
import anyang.mypackages.data.PackageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsReader(private val context: Context) {
    private val parser = SmsParser()

    suspend fun readSmsMessages(): List<PackageEntity> = withContext(Dispatchers.IO) {
        if (!checkPermission()) return@withContext emptyList()

        val packages = mutableListOf<PackageEntity>()

        val uri: Uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)

                if (parser.isPackageSms(body)) {
                    val packageItem = parser.parseSms(body, date)
                    packageItem?.let { pkg -> packages.add(pkg) }
                }
            }
        }

        packages
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
