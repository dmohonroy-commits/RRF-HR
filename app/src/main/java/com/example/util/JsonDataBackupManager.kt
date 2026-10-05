package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.AddressItemEntity
import com.example.data.AgreementEntity
import com.example.data.AppDatabase
import com.example.data.DraftStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * JsonDataBackupManager
 * Handles full JSON backup export, import, inspection, and restoration
 * for complete data portability across devices and installations.
 */
object JsonDataBackupManager {

    private const val APP_IDENTIFIER = "RRF HR Management"
    private const val CURRENT_SCHEMA_VERSION = 1

    data class ExportResult(
        val file: File,
        val fileName: String,
        val jsonString: String,
        val agreementsCount: Int,
        val addressCount: Int,
        val hasDrafts: Boolean,
        val timestamp: String
    )

    data class BackupSummary(
        val isValid: Boolean,
        val appName: String? = null,
        val schemaVersion: Int = 1,
        val exportDate: String? = null,
        val exportTimestamp: Long = 0L,
        val agreementsCount: Int = 0,
        val addressItemsCount: Int = 0,
        val hasDrafts: Boolean = false,
        val agreements: List<AgreementEntity> = emptyList(),
        val addressItems: List<AddressItemEntity> = emptyList(),
        val draftsJson: JSONObject? = null,
        val preferencesJson: JSONObject? = null,
        val errorMessage: String? = null
    )

    data class RestoreResult(
        val success: Boolean,
        val agreementsRestored: Int,
        val addressesRestored: Int,
        val draftsRestored: Boolean,
        val message: String
    )

    /**
     * Exports all local database rows (Agreements, AddressItems), all Form Drafts from
     * SharedPreferences, and app preferences into a clean, formatted JSON file.
     */
    suspend fun exportAllDataToJson(context: Context, database: AppDatabase): ExportResult = withContext(Dispatchers.IO) {
        val agreements = database.agreementDao().getAllAgreementsList()
        val addressItems = database.addressDao().getAllAddressItems()
        val draftsJson = DraftStorageManager.exportAllDraftsToJson(context)

        val rootJson = JSONObject()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val fileDateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        // Metadata
        val metaJson = JSONObject().apply {
            put("appName", APP_IDENTIFIER)
            put("schemaVersion", CURRENT_SCHEMA_VERSION)
            put("exportDate", dateStr)
            put("exportTimestamp", System.currentTimeMillis())
            put("totalAgreements", agreements.size)
            put("totalAddressItems", addressItems.size)
            put("includesDrafts", draftsJson.length() > 0)
        }
        rootJson.put("metadata", metaJson)

        // Agreements Array
        val agreementsArray = JSONArray()
        for (item in agreements) {
            val aJson = JSONObject().apply {
                put("id", item.id)
                put("serialNo", item.serialNo)
                put("submissionDate", item.submissionDate)
                put("submissionTimestamp", item.submissionTimestamp)
                put("employeeName", item.employeeName)
                put("employeeFatherName", item.employeeFatherName)
                put("designation", item.designation)
                put("employeeDistrict", item.employeeDistrict)
                put("employeeUpazila", item.employeeUpazila)
                put("employeePostOffice", item.employeePostOffice)
                put("employeeVillage", item.employeeVillage)
                put("guarantorName", item.guarantorName)
                put("guarantorFatherName", item.guarantorFatherName)
                put("guarantorMotherName", item.guarantorMotherName)
                put("guarantorRelationship", item.guarantorRelationship)
                put("guarantorRelationshipCustom", item.guarantorRelationshipCustom)
                put("guarantorNid", item.guarantorNid)
                put("sameAddress", item.sameAddress)
                put("guarantorDistrict", item.guarantorDistrict)
                put("guarantorUpazila", item.guarantorUpazila)
                put("guarantorPostOffice", item.guarantorPostOffice)
                put("guarantorVillage", item.guarantorVillage)
                put("isPrinted", item.isPrinted)
                put("printDate", item.printDate ?: "")
                put("isSubmittedByThisDevice", item.isSubmittedByThisDevice)
                put("editCount", item.editCount)
                put("lastEditDate", item.lastEditDate ?: "")
                put("orgLogoPath", item.orgLogoPath ?: "")
                put("assignedBranch", item.assignedBranch)
                put("dispatchDate", item.dispatchDate ?: "")
                put("verificationStatus", item.verificationStatus)
                put("verificationDate", item.verificationDate ?: "")
                put("branchNotes", item.branchNotes)
            }
            agreementsArray.put(aJson)
        }
        rootJson.put("agreements", agreementsArray)

        // Address Items Array
        val addressArray = JSONArray()
        for (addr in addressItems) {
            val addrJson = JSONObject().apply {
                put("type", addr.type)
                put("district", addr.district)
                put("upazila", addr.upazila)
                put("name", addr.name)
            }
            addressArray.put(addrJson)
        }
        rootJson.put("addressItems", addressArray)

        // Drafts
        rootJson.put("drafts", draftsJson)

        // Preferences (e.g. Org Logo and settings)
        val prefsJson = JSONObject()
        try {
            val rrfPrefs = context.getSharedPreferences("rrf_prefs", Context.MODE_PRIVATE)
            val orgLogoPath = rrfPrefs.getString("org_logo_path", null)
            if (orgLogoPath != null) {
                prefsJson.put("org_logo_path", orgLogoPath)
            }
            val formPrefs = context.getSharedPreferences("rrf_form_prefs", Context.MODE_PRIVATE)
            val orgLogoUri = formPrefs.getString("org_logo_uri", null)
            if (orgLogoUri != null) {
                prefsJson.put("org_logo_uri", orgLogoUri)
            }
        } catch (_: Exception) {}
        rootJson.put("preferences", prefsJson)

        val prettyJsonString = rootJson.toString(2)

        // Save to file
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val fileName = "RRF_HR_Backup_$fileDateSuffix.json"
        val backupFile = File(backupDir, fileName)
        FileOutputStream(backupFile).use { fos ->
            fos.write(prettyJsonString.toByteArray(Charsets.UTF_8))
        }

        // Also save a copy to Downloads folder for effortless user access
        try {
            ShareHelper.saveFileToDownloads(context, backupFile, fileName)
        } catch (_: Exception) {}

        ExportResult(
            file = backupFile,
            fileName = fileName,
            jsonString = prettyJsonString,
            agreementsCount = agreements.size,
            addressCount = addressItems.size,
            hasDrafts = draftsJson.length() > 0,
            timestamp = dateStr
        )
    }

    /**
     * Reads and parses a backup JSON file from an Android content URI.
     */
    suspend fun parseBackupUri(context: Context, uri: Uri): BackupSummary = withContext(Dispatchers.IO) {
        try {
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line).append("\n")
                    }
                }
            } ?: return@withContext BackupSummary(isValid = false, errorMessage = "ফাইলটি খোলা সম্ভব হয়নি।")

            parseBackupString(stringBuilder.toString())
        } catch (e: Exception) {
            BackupSummary(isValid = false, errorMessage = "ফাইল পড়তে সমস্যা হয়েছে: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Parses the JSON backup string and validates its structure.
     */
    fun parseBackupString(jsonString: String): BackupSummary {
        return try {
            val root = JSONObject(jsonString)

            val meta = root.optJSONObject("metadata")
            val appName = meta?.optString("appName", "RRF HR Management")
            val schemaVersion = meta?.optInt("schemaVersion", 1) ?: 1
            val exportDate = meta?.optString("exportDate", "অজানা")
            val exportTimestamp = meta?.optLong("exportTimestamp", 0L) ?: 0L

            val agreementsList = mutableListOf<AgreementEntity>()
            val agreementsArray = root.optJSONArray("agreements")
            if (agreementsArray != null) {
                for (i in 0 until agreementsArray.length()) {
                    val obj = agreementsArray.getJSONObject(i)
                    agreementsList.add(
                        AgreementEntity(
                            id = obj.optLong("id", 0L),
                            serialNo = obj.optString("serialNo", ""),
                            submissionDate = obj.optString("submissionDate", ""),
                            submissionTimestamp = obj.optLong("submissionTimestamp", System.currentTimeMillis()),
                            employeeName = obj.optString("employeeName", ""),
                            employeeFatherName = obj.optString("employeeFatherName", ""),
                            designation = obj.optString("designation", ""),
                            employeeDistrict = obj.optString("employeeDistrict", ""),
                            employeeUpazila = obj.optString("employeeUpazila", ""),
                            employeePostOffice = obj.optString("employeePostOffice", ""),
                            employeeVillage = obj.optString("employeeVillage", ""),
                            guarantorName = obj.optString("guarantorName", ""),
                            guarantorFatherName = obj.optString("guarantorFatherName", ""),
                            guarantorMotherName = obj.optString("guarantorMotherName", ""),
                            guarantorRelationship = obj.optString("guarantorRelationship", ""),
                            guarantorRelationshipCustom = obj.optString("guarantorRelationshipCustom", ""),
                            guarantorNid = obj.optString("guarantorNid", ""),
                            sameAddress = obj.optBoolean("sameAddress", false),
                            guarantorDistrict = obj.optString("guarantorDistrict", ""),
                            guarantorUpazila = obj.optString("guarantorUpazila", ""),
                            guarantorPostOffice = obj.optString("guarantorPostOffice", ""),
                            guarantorVillage = obj.optString("guarantorVillage", ""),
                            isPrinted = obj.optBoolean("isPrinted", false),
                            printDate = obj.optString("printDate").takeIf { it.isNotBlank() },
                            isSubmittedByThisDevice = obj.optBoolean("isSubmittedByThisDevice", true),
                            editCount = obj.optInt("editCount", 0),
                            lastEditDate = obj.optString("lastEditDate").takeIf { it.isNotBlank() },
                            orgLogoPath = obj.optString("orgLogoPath").takeIf { it.isNotBlank() },
                            assignedBranch = obj.optString("assignedBranch", ""),
                            dispatchDate = obj.optString("dispatchDate").takeIf { it.isNotBlank() },
                            verificationStatus = obj.optString("verificationStatus", "অপেক্ষমান"),
                            verificationDate = obj.optString("verificationDate").takeIf { it.isNotBlank() },
                            branchNotes = obj.optString("branchNotes", "")
                        )
                    )
                }
            }

            val addressList = mutableListOf<AddressItemEntity>()
            val addressArray = root.optJSONArray("addressItems")
            if (addressArray != null) {
                for (i in 0 until addressArray.length()) {
                    val obj = addressArray.getJSONObject(i)
                    val type = obj.optString("type", "")
                    val district = obj.optString("district", "")
                    val upazila = obj.optString("upazila", "")
                    val name = obj.optString("name", "")
                    if (type.isNotBlank() && district.isNotBlank() && upazila.isNotBlank() && name.isNotBlank()) {
                        addressList.add(
                            AddressItemEntity(
                                type = type,
                                district = district,
                                upazila = upazila,
                                name = name
                            )
                        )
                    }
                }
            }

            val draftsJson = root.optJSONObject("drafts")
            val preferencesJson = root.optJSONObject("preferences")

            val hasValidContent = agreementsList.isNotEmpty() || addressList.isNotEmpty() || (draftsJson != null && draftsJson.length() > 0)

            if (!hasValidContent && meta == null) {
                return BackupSummary(isValid = false, errorMessage = "নির্বাচিত ফাইলটি সঠিক RRF HR ব্যাকআপ JSON ফরম্যাটের নয়।")
            }

            BackupSummary(
                isValid = true,
                appName = appName,
                schemaVersion = schemaVersion,
                exportDate = exportDate,
                exportTimestamp = exportTimestamp,
                agreementsCount = agreementsList.size,
                addressItemsCount = addressList.size,
                hasDrafts = draftsJson != null && draftsJson.length() > 0,
                agreements = agreementsList,
                addressItems = addressList,
                draftsJson = draftsJson,
                preferencesJson = preferencesJson
            )
        } catch (e: Exception) {
            BackupSummary(isValid = false, errorMessage = "JSON ফাইল পার্স করতে ব্যর্থ হয়েছে: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Restores data into local SQLite Room database, SharedPreferences drafts, and preferences.
     * @param replaceExisting If true, purges existing records and replaces with backup data.
     *                        If false (merge), preserves existing records and inserts new ones.
     */
    suspend fun restoreData(
        context: Context,
        database: AppDatabase,
        summary: BackupSummary,
        replaceExisting: Boolean,
        restoreDrafts: Boolean
    ): RestoreResult = withContext(Dispatchers.IO) {
        if (!summary.isValid) {
            return@withContext RestoreResult(
                success = false,
                agreementsRestored = 0,
                addressesRestored = 0,
                draftsRestored = false,
                message = summary.errorMessage ?: "অকার্যকর ব্যাকআপ ফাইল।"
            )
        }

        try {
            val agreementDao = database.agreementDao()
            val addressDao = database.addressDao()

            if (replaceExisting) {
                agreementDao.deleteAllAgreements()
                if (summary.addressItems.isNotEmpty()) {
                    addressDao.deleteAllAddressItems()
                }
            }

            // Restore Agreements
            var agreementsCount = 0
            if (summary.agreements.isNotEmpty()) {
                if (replaceExisting) {
                    agreementDao.insertAgreements(summary.agreements)
                    agreementsCount = summary.agreements.size
                } else {
                    // Merge mode: check existing to avoid primary key duplicates while retaining data
                    val existing = agreementDao.getAllAgreementsList()
                    val existingSerials = existing.map { it.serialNo }.toSet()

                    for (agreement in summary.agreements) {
                        if (existingSerials.contains(agreement.serialNo)) {
                            // Update existing agreement with backup version
                            val match = existing.firstOrNull { it.serialNo == agreement.serialNo }
                            if (match != null) {
                                agreementDao.updateAgreement(agreement.copy(id = match.id))
                            } else {
                                agreementDao.insertAgreement(agreement.copy(id = 0))
                            }
                        } else {
                            // New entry: insert with new auto-generated ID to prevent conflict
                            agreementDao.insertAgreement(agreement.copy(id = 0))
                        }
                        agreementsCount++
                    }
                }
            }

            // Restore Learned Addresses
            var addressesCount = 0
            if (summary.addressItems.isNotEmpty()) {
                addressDao.insertAddressItems(summary.addressItems)
                addressesCount = summary.addressItems.size
            }

            // Restore Form Drafts
            var draftsRestored = false
            if (restoreDrafts && summary.draftsJson != null && summary.draftsJson.length() > 0) {
                DraftStorageManager.restoreDraftsFromJson(context, summary.draftsJson)
                draftsRestored = true
            }

            // Restore Preferences (e.g. Org Logo)
            if (summary.preferencesJson != null) {
                val orgLogoPath = summary.preferencesJson.optString("org_logo_path", "")
                if (orgLogoPath.isNotBlank()) {
                    val rrfPrefs = context.getSharedPreferences("rrf_prefs", Context.MODE_PRIVATE)
                    rrfPrefs.edit().putString("org_logo_path", orgLogoPath).apply()
                }
                val orgLogoUri = summary.preferencesJson.optString("org_logo_uri", "")
                if (orgLogoUri.isNotBlank()) {
                    val formPrefs = context.getSharedPreferences("rrf_form_prefs", Context.MODE_PRIVATE)
                    formPrefs.edit().putString("org_logo_uri", orgLogoUri).apply()
                }
            }

            val modeText = if (replaceExisting) "সম্পূর্ণ প্রতিস্থাপন" else "মার্জ (সংযোজন)"
            RestoreResult(
                success = true,
                agreementsRestored = agreementsCount,
                addressesRestored = addressesCount,
                draftsRestored = draftsRestored,
                message = "সফলভাবে $modeText পদ্ধতিতে $agreementsCount টি এগ্রিমেন্ট ও $addressesCount টি ঠিকানা রিস্টোর সম্পন্ন হয়েছে।"
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                agreementsRestored = 0,
                addressesRestored = 0,
                draftsRestored = false,
                message = "ডাটা রিস্টোর করার সময় ত্রুটি ঘটেছে: ${e.localizedMessage ?: e.message}"
            )
        }
    }
}
