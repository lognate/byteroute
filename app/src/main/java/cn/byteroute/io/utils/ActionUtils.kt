package cn.byteroute.io.utils

import android.content.ContentValues
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.util.Log
import android.util.SparseArray
import androidx.annotation.RequiresApi
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScan.*
import java.util.*

object ActionUtils {
    private val addressMap: SparseArray<Int?> = SparseArray()
    private val phoneMap = SparseArray<Int>()
    private val emailMap = SparseArray<Int>()

    init {
        addressMap.put(
            AddressInfo.OTHER_USE_TYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER
        )
        addressMap.put(
            AddressInfo.OFFICE_TYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK
        )
        addressMap.put(
            AddressInfo.RESIDENTIAL_USE_TYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
        )

        phoneMap.put(
            TelPhoneNumber.OTHER_USE_TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
        )
        phoneMap.put(
            TelPhoneNumber.OFFICE_USE_TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK
        )
        phoneMap.put(
            TelPhoneNumber.RESIDENTIAL_USE_TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME
        )
        phoneMap.put(
            TelPhoneNumber.FAX_USE_TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX
        )
        phoneMap.put(
            TelPhoneNumber.CELLPHONE_NUMBER_USE_TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
        )

        emailMap.put(EmailContent.OTHER_USE_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_OTHER)
        emailMap.put(EmailContent.OFFICE_USE_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
        emailMap.put(
            EmailContent.RESIDENTIAL_USE_TYPE,
            ContactsContract.CommonDataKinds.Email.TYPE_HOME
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getCalendarEventIntent(calendarEvent: HmsScan.EventInfo): Intent? {
        val intent = Intent(Intent.ACTION_INSERT)
        try {
            intent.setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, getTime(calendarEvent.beginTime))
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, getTime(calendarEvent.closeTime))
                .putExtra(CalendarContract.Events.TITLE, calendarEvent.getTheme())
                .putExtra(CalendarContract.Events.DESCRIPTION, calendarEvent.getAbstractInfo())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, calendarEvent.getPlaceInfo())
                .putExtra(CalendarContract.Events.ORGANIZER, calendarEvent.getSponsor())
                .putExtra(CalendarContract.Events.STATUS, calendarEvent.getCondition())
        } catch (e: NullPointerException) {
            Log.w("getCalendarEventIntent", e)
        }
        return intent
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getTime(calendarDateTime: EventTime): Long {
        val calendar = Calendar.getInstance()
        calendar[calendarDateTime.getYear(), calendarDateTime.getMonth() - 1, calendarDateTime.getDay(), calendarDateTime.getHours(), calendarDateTime.getMinutes()] =
            calendarDateTime.getSeconds()
        return calendar.time.time
    }


    fun getContactInfoIntent(contactInfo: ContactDetail): Intent? {
        val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
        try {
            intent.putExtra(
                ContactsContract.Intents.Insert.NAME,
                contactInfo.getPeopleName().getFullName()
            )
            intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, contactInfo.getTitle())
            intent.putExtra(ContactsContract.Intents.Insert.COMPANY, contactInfo.getCompany())
            val data = ArrayList<ContentValues>()
            data.addAll(getAddresses(contactInfo)!!)
            data.addAll(getPhones(contactInfo)!!)
            data.addAll(getEmails(contactInfo)!!)
            data.addAll(getUrls(contactInfo)!!)
            intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data)
        } catch (e: java.lang.NullPointerException) {
            Log.w("getCalendarEventIntent", e)
        }
        return intent
    }

    private fun getAddresses(contactInfo: ContactDetail): ArrayList<ContentValues>? {
        val data = ArrayList<ContentValues>()
        if (contactInfo.getAddressesInfos() != null) {
            for (address in contactInfo.getAddressesInfos()) {
                if (address.getAddressDetails() != null) {
                    val contentValues = ContentValues()
                    contentValues.put(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                    )
                    val addressBuilder = StringBuilder()
                    for (addressLine in address.getAddressDetails()) {
                        addressBuilder.append(addressLine)
                    }
                    contentValues.put(
                        ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                        addressBuilder.toString()
                    )
                    val type = addressMap[address.getAddressType()]
                    contentValues.put(
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                        type ?: ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER
                    )
                    data.add(contentValues)
                }
            }
        }
        return data
    }

    private fun getPhones(contactInfo: ContactDetail): ArrayList<ContentValues>? {
        val data = ArrayList<ContentValues>()
        if (contactInfo.getTelPhoneNumbers() != null) {
            for (phone in contactInfo.getTelPhoneNumbers()) {
                val contentValues = ContentValues()
                contentValues.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                contentValues.put(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    phone.getTelPhoneNumber()
                )
                val type = phoneMap[phone.getUseType()]
                contentValues.put(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    type ?: ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
                )
                data.add(contentValues)
            }
        }
        return data
    }

    private fun getEmails(contactInfo: ContactDetail): ArrayList<ContentValues>? {
        val data = ArrayList<ContentValues>()
        if (contactInfo.emailContents != null) {
            for (email in contactInfo.emailContents) {
                val contentValues = ContentValues()
                contentValues.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
                contentValues.put(
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                    email.getAddressInfo()
                )
                val type = emailMap[email.getAddressType()]
                contentValues.put(
                    ContactsContract.CommonDataKinds.Email.TYPE,
                    type ?: ContactsContract.CommonDataKinds.Email.TYPE_OTHER
                )
                data.add(contentValues)
            }
        }
        return data
    }

    private fun getUrls(contactInfo: ContactDetail): ArrayList<ContentValues>? {
        val data = ArrayList<ContentValues>()
        if (contactInfo.getContactLinks() != null) {
            for (url in contactInfo.getContactLinks()) {
                val contentValues = ContentValues()
                contentValues.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                )
                contentValues.put(ContactsContract.CommonDataKinds.Website.URL, url)
                data.add(contentValues)
            }
        }
        return data
    }

    fun getDialIntent(telPhoneNumber: TelPhoneNumber): Intent? {
        val uri = Uri.parse("tel:" + telPhoneNumber.getTelPhoneNumber())
        return Intent(Intent.ACTION_DIAL, uri)
    }

    fun getEmailInfo(emailContent: EmailContent): Intent? {
        val uri = Uri.parse("mailto:" + emailContent.addressInfo)
        val tos = arrayOf(emailContent.addressInfo)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra(Intent.EXTRA_EMAIL, tos)
        intent.putExtra(Intent.EXTRA_SUBJECT, emailContent.subjectInfo)
        intent.putExtra(Intent.EXTRA_TEXT, emailContent.bodyInfo)
        return intent
    }

    fun getLoactionInfo(locationCoordinate: LocationCoordinate): Intent? {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("androidamap://viewMap?lat=" + locationCoordinate.getLatitude() + "&lon=" + locationCoordinate.getLongitude())
        )
    }

    fun getSMSInfo(smsContent: SmsContent): Intent? {
        val uri = Uri.parse("smsto:" + smsContent.getDestPhoneNumber())
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", smsContent.getMsgContent())
        return intent
    }

}