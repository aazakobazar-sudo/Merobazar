package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object NepalCalendarHelper {

    private val nepaliMonths = listOf(
        "बैशाख (Baisakh)",
        "जेठ (Jestha)",
        "असार (Ashadh)",
        "साउन (Shrawan)",
        "भदौ (Bhadra)",
        "असोज (Ashwin)",
        "कात्तिक (Kartik)",
        "मङ्सिर (Mangsir)",
        "पुस (Poush)",
        "माघ (Magh)",
        "फागुन (Falgun)",
        "चैत (Chaitra)"
    )

    private val nepaliDays = listOf(
        "आइतबार (Sun)",
        "सोमबार (Mon)",
        "मङ्गलबार (Tue)",
        "बुधबार (Wed)",
        "बिहीबार (Thu)",
        "शुक्रबार (Fri)",
        "शनिबार (Sat)"
    )

    /**
     * Approximate BS conversion for display alongside Gregorian dates.
     * BS is approximately 56 years, 8 months, 15-17 days ahead of AD.
     */
    fun getCurrentNepaliDateString(): String {
        val cal = Calendar.getInstance()
        val yearAd = cal.get(Calendar.YEAR)
        val monthAd = cal.get(Calendar.MONTH) // 0-based
        val dayAd = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based Sunday

        // Approximate BS year & month
        val yearBs = yearAd + 57
        val monthBs = (monthAd + 8) % 12
        val dayBs = ((dayAd + 15) % 30) + 1

        val monthName = nepaliMonths.getOrElse(monthBs) { "कात्तिक (Kartik)" }
        val dayName = nepaliDays.getOrElse(dayOfWeek) { "शुक्रबार (Fri)" }

        val adFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "$dayName, $yearBs $monthName $dayBs [A.D. $adFormatted]"
    }

    /**
     * Launches the device Calendar application with a pre-filled event
     * using the standard CalendarContract.Events intent.
     */
    fun addEventToCalendar(
        context: Context,
        title: String,
        description: String,
        location: String = "Nepal",
        startTimeMillis: Long = System.currentTimeMillis() + 86400000L, // Tomorrow default
        endTimeMillis: Long = System.currentTimeMillis() + 90000000L
    ): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
                putExtra(CalendarContract.Events.ALL_DAY, false)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reminder for Monthly VAT / Tax filing to Nepal IRD (by 25th of month)
     */
    fun addMonthlyTaxFilingReminder(context: Context): Boolean {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 25)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.MONTH, 1)
            }
        }
        val start = cal.timeInMillis
        val end = start + 3600000L

        return addEventToCalendar(
            context = context,
            title = "मेरो बजार: IRD मासिक कर/VAT विवरण बुझाउने म्याद",
            description = "नेपाल सरकार आन्तरिक राजस्व विभाग (IRD) मा मासिक कर दाखिला (VAT/PAN return) गर्ने म्याद आज २५ गते हो। prize.ird.gov.np मार्फत पनि विवरण रुजु गर्नुहोस्।",
            location = "IRD Office / Online Portal",
            startTimeMillis = start,
            endTimeMillis = end
        )
    }

    /**
     * Reminder for an Order Delivery
     */
    fun addOrderDeliveryReminder(context: Context, orderId: String, shopName: String, estimatedDays: Int = 2): Boolean {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, estimatedDays)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
        }
        val start = cal.timeInMillis
        val end = start + 3600000L

        return addEventToCalendar(
            context = context,
            title = "📦 मेरो बजार डेलिभरी: अर्डर #$orderId ($shopName)",
            description = "तपाईंको अर्डर डेलिभरी हुने अनुमानित मिति। कृपया सम्पर्क नम्बर चालु राख्नुहोला।",
            location = "घर / डेलिभरी ठेगाना",
            startTimeMillis = start,
            endTimeMillis = end
        )
    }

    /**
     * Reminder for Khata Payment Due
     */
    fun addKhataDueReminder(context: Context, customerName: String, amountRs: Long, dueDateDays: Int = 7): Boolean {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, dueDateDays)
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 0)
        }
        val start = cal.timeInMillis
        val end = start + 3600000L

        return addEventToCalendar(
            context = context,
            title = "💰 खाता भुक्तानी म्याद: $customerName (रु $amountRs)",
            description = "मेरो बजार खाता लेजर: $customerName बाट रु $amountRs असुल गर्ने सम्झाउनी मिति।",
            location = "पसल / खाता काउन्टर",
            startTimeMillis = start,
            endTimeMillis = end
        )
    }
}
