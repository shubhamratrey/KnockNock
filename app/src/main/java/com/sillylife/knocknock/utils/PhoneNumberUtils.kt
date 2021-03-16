package com.sillylife.knocknock.utils

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.SparseArray
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.sillylife.knocknock.MainApplication
import com.sillylife.knocknock.helpers.ContactsHelper
import com.sillylife.knocknock.services.sharedpreference.SharedPreferenceManager
import java.util.*
import java.util.Arrays.asList

object PhoneNumberUtils {
    val context = MainApplication.getInstance()

    private val DEFAULT_COUNTRY_CODE_INT = 91
    private val DEFAULT_COUNTRY_CODE = DEFAULT_COUNTRY_CODE_INT.toString()
    private val DEFAULT_LOCALE = Locale.US
    private val DEFAULT_COUNTRY = CountryInfo(DEFAULT_LOCALE, DEFAULT_COUNTRY_CODE_INT)

    private val MAX_COUNTRY_CODES = 215
    private val MAX_COUNTRIES = 248
    private val MAX_LENGTH_COUNTRY_CODE = 3

    private val COUNTRY_TO_REGION_CODES = createCountryCodeToRegionCodeMap()

    private var COUNTRY_TO_ISO_CODES: Map<String, Int>? = null

    private val osLocale: Locale
        get() = Locale.getDefault()

    fun format(@NonNull phoneNumber: String, @NonNull countryInfo: CountryInfo): String {
        return if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else {
            ("+" + countryInfo.countryCode + phoneNumber.replace("[^\\d.]".toRegex(), ""))
        }
    }

    @Nullable
    fun formatUsingCurrentCountry(@NonNull phoneNumber: String, context: Context): String {
        return format(phoneNumber, getCurrentCountryInfo(context))
    }

    fun getPseudoValidPhoneNumber(phoneNumber: String, countryCode: String?): String? {
        if (TextUtils.isEmpty(phoneNumber)) {
            return null
        }
        return if (countryCode == null) {
            formatUsingCurrentCountry(phoneNumber, context)
        } else {
            "+$countryCode$phoneNumber"
        }
    }

    @NonNull
    fun getCurrentCountryInfo(@NonNull context: Context): CountryInfo {
        var locale = getSimBasedLocale(context)

        if (locale == null) {
            locale = osLocale
        }

        if (locale == null) {
            return DEFAULT_COUNTRY
        }

        val countryCode = getCountryCode(locale.country)

        return countryCode?.let { CountryInfo(locale, it) } ?: DEFAULT_COUNTRY
    }

    fun isValid(@NonNull number: String): Boolean {
        return number.startsWith("+") && getCountryCodeForPhoneNumber(number) != null
    }

    fun isValidIso(@Nullable iso: String): Boolean {
        return getCountryCode(iso) != null
    }

    @Nullable
    fun getPhoneNumberWithCountryCode(@NonNull phoneNumber: String): String {
        return if (phoneNumber.startsWith("+")) {
            phoneNumber
        } else {
            ("+" + DEFAULT_COUNTRY_CODE + phoneNumber.replace("[^\\d.]".toRegex(), ""))
        }
    }

    @Nullable
    fun getCountryCode(countryIso: String?): Int? {
        if (COUNTRY_TO_ISO_CODES == null) {
            initCountryCodeByIsoMap()
        }
        return if (countryIso == null)
            null
        else
            COUNTRY_TO_ISO_CODES!![countryIso.toUpperCase(Locale.getDefault())]
    }

    private fun getCountryIsoForCountryCode(countryCode: String): String {
        val countries = COUNTRY_TO_REGION_CODES.get(Integer.parseInt(countryCode))
        return if (countries != null) {
            countries[0]
        } else DEFAULT_LOCALE.country
    }

    /**
     * Country code extracted using shortest matching prefix like libPhoneNumber. See:
     * https://github.com/googlei18n/libphonenumber/blob/master/java/libphonenumber/src/com
     * /google/i18n/phonenumbers/PhoneNumberUtil.java#L2395
     */
    @Nullable
    private fun getCountryCodeForPhoneNumber(normalizedPhoneNumber: String): String? {
        val phoneWithoutPlusPrefix = normalizedPhoneNumber.replaceFirst("^\\+".toRegex(), "")
        val numberLength = phoneWithoutPlusPrefix.length

        var i = 1
        while (i <= MAX_LENGTH_COUNTRY_CODE && i <= numberLength) {
            val potentialCountryCode = phoneWithoutPlusPrefix.substring(0, i)
            val countryCodeKey = Integer.valueOf(potentialCountryCode)

            if (COUNTRY_TO_REGION_CODES.indexOfKey(countryCodeKey) >= 0) {
                return potentialCountryCode
            }
            i++
        }
        return null
    }

    @NonNull
    private fun getCountryCodeForPhoneNumberOrDefault(normalizedPhoneNumber: String): String {
        val code = getCountryCodeForPhoneNumber(normalizedPhoneNumber)
        return code ?: DEFAULT_COUNTRY_CODE
    }

    private fun stripCountryCode(phoneNumber: String, countryCode: String): String {
        return phoneNumber.replaceFirst("^\\+?$countryCode".toRegex(), "")
    }

    private fun stripPlusSign(phoneNumber: String): String {
        return phoneNumber.replaceFirst("^\\+?".toRegex(), "")
    }

    private fun getSimBasedLocale(@NonNull context: Context): Locale? {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryIso = tm?.simCountryIso
        return if (TextUtils.isEmpty(countryIso)) null else Locale("", countryIso)
    }

    fun createCountryCodeToRegionCodeMap(): SparseArray<List<String>> {
        val map = SparseArray<List<String>>(MAX_COUNTRY_CODES)

        map.put(1, asList(
                "US", "AG", "AI", "AS", "BB", "BM", "BS", "CA", "DM", "DO", "GD", "GU", "JM", "KN",
                "KY", "LC", "MP", "MS", "PR", "SX", "TC", "TT", "VC", "VG", "VI"))
        map.put(7, asList("RU", "KZ"))
        map.put(20, listOf("EG"))
        map.put(27, listOf("ZA"))
        map.put(30, listOf("GR"))
        map.put(31, listOf("NL"))
        map.put(32, listOf("BE"))
        map.put(33, listOf("FR"))
        map.put(34, listOf("ES"))
        map.put(36, listOf("HU"))
        map.put(39, listOf("IT"))
        map.put(40, listOf("RO"))
        map.put(41, listOf("CH"))
        map.put(43, listOf("AT"))
        map.put(44, asList("GB", "GG", "IM", "JE"))
        map.put(45, listOf("DK"))
        map.put(46, listOf("SE"))
        map.put(47, asList("NO", "SJ"))
        map.put(48, listOf("PL"))
        map.put(49, listOf("DE"))
        map.put(51, listOf("PE"))
        map.put(52, listOf("MX"))
        map.put(53, listOf("CU"))
        map.put(54, listOf("AR"))
        map.put(55, listOf("BR"))
        map.put(56, listOf("CL"))
        map.put(57, listOf("CO"))
        map.put(58, listOf("VE"))
        map.put(60, listOf("MY"))
        map.put(61, asList("AU", "CC", "CX"))
        map.put(62, listOf("ID"))
        map.put(63, listOf("PH"))
        map.put(64, listOf("NZ"))
        map.put(65, listOf("SG"))
        map.put(66, listOf("TH"))
        map.put(81, listOf("JP"))
        map.put(82, listOf("KR"))
        map.put(84, listOf("VN"))
        map.put(86, listOf("CN"))
        map.put(90, listOf("TR"))
        map.put(91, listOf("IN"))
        map.put(92, listOf("PK"))
        map.put(93, listOf("AF"))
        map.put(94, listOf("LK"))
        map.put(95, listOf("MM"))
        map.put(98, listOf("IR"))
        map.put(211, listOf("SS"))
        map.put(212, asList("MA", "EH"))
        map.put(213, listOf("DZ"))
        map.put(216, listOf("TN"))
        map.put(218, listOf("LY"))
        map.put(220, listOf("GM"))
        map.put(221, listOf("SN"))
        map.put(222, listOf("MR"))
        map.put(223, listOf("ML"))
        map.put(224, listOf("GN"))
        map.put(225, listOf("CI"))
        map.put(226, listOf("BF"))
        map.put(227, listOf("NE"))
        map.put(228, listOf("TG"))
        map.put(229, listOf("BJ"))
        map.put(230, listOf("MU"))
        map.put(231, listOf("LR"))
        map.put(232, listOf("SL"))
        map.put(233, listOf("GH"))
        map.put(234, listOf("NG"))
        map.put(235, listOf("TD"))
        map.put(236, listOf("CF"))
        map.put(237, listOf("CM"))
        map.put(238, listOf("CV"))
        map.put(239, listOf("ST"))
        map.put(240, listOf("GQ"))
        map.put(241, listOf("GA"))
        map.put(242, listOf("CG"))
        map.put(243, listOf("CD"))
        map.put(244, listOf("AO"))
        map.put(245, listOf("GW"))
        map.put(246, listOf("IO"))
        map.put(247, listOf("AC"))
        map.put(248, listOf("SC"))
        map.put(249, listOf("SD"))
        map.put(250, listOf("RW"))
        map.put(251, listOf("ET"))
        map.put(252, listOf("SO"))
        map.put(253, listOf("DJ"))
        map.put(254, listOf("KE"))
        map.put(255, listOf("TZ"))
        map.put(256, listOf("UG"))
        map.put(257, listOf("BI"))
        map.put(258, listOf("MZ"))
        map.put(260, listOf("ZM"))
        map.put(261, listOf("MG"))
        map.put(262, asList("RE", "YT"))
        map.put(263, listOf("ZW"))
        map.put(264, listOf("NA"))
        map.put(265, listOf("MW"))
        map.put(266, listOf("LS"))
        map.put(267, listOf("BW"))
        map.put(268, listOf("SZ"))
        map.put(269, listOf("KM"))
        map.put(290, asList("SH", "TA"))
        map.put(291, listOf("ER"))
        map.put(297, listOf("AW"))
        map.put(298, listOf("FO"))
        map.put(299, listOf("GL"))
        map.put(350, listOf("GI"))
        map.put(351, listOf("PT"))
        map.put(352, listOf("LU"))
        map.put(353, listOf("IE"))
        map.put(354, listOf("IS"))
        map.put(355, listOf("AL"))
        map.put(356, listOf("MT"))
        map.put(357, listOf("CY"))
        map.put(358, asList("FI", "AX"))
        map.put(359, listOf("BG"))
        map.put(370, listOf("LT"))
        map.put(371, listOf("LV"))
        map.put(372, listOf("EE"))
        map.put(373, listOf("MD"))
        map.put(374, listOf("AM"))
        map.put(375, listOf("BY"))
        map.put(376, listOf("AD"))
        map.put(377, listOf("MC"))
        map.put(378, listOf("SM"))
        map.put(379, listOf("VA"))
        map.put(380, listOf("UA"))
        map.put(381, listOf("RS"))
        map.put(382, listOf("ME"))
        map.put(385, listOf("HR"))
        map.put(386, listOf("SI"))
        map.put(387, listOf("BA"))
        map.put(389, listOf("MK"))
        map.put(420, listOf("CZ"))
        map.put(421, listOf("SK"))
        map.put(423, listOf("LI"))
        map.put(500, listOf("FK"))
        map.put(501, listOf("BZ"))
        map.put(502, listOf("GT"))
        map.put(503, listOf("SV"))
        map.put(504, listOf("HN"))
        map.put(505, listOf("NI"))
        map.put(506, listOf("CR"))
        map.put(507, listOf("PA"))
        map.put(508, listOf("PM"))
        map.put(509, listOf("HT"))
        map.put(590, asList("GP", "BL", "MF"))
        map.put(591, listOf("BO"))
        map.put(592, listOf("GY"))
        map.put(593, listOf("EC"))
        map.put(594, listOf("GF"))
        map.put(595, listOf("PY"))
        map.put(596, listOf("MQ"))
        map.put(597, listOf("SR"))
        map.put(598, listOf("UY"))
        map.put(599, asList("CW", "BQ"))
        map.put(670, listOf("TL"))
        map.put(672, listOf("NF"))
        map.put(673, listOf("BN"))
        map.put(674, listOf("NR"))
        map.put(675, listOf("PG"))
        map.put(676, listOf("TO"))
        map.put(677, listOf("SB"))
        map.put(678, listOf("VU"))
        map.put(679, listOf("FJ"))
        map.put(680, listOf("PW"))
        map.put(681, listOf("WF"))
        map.put(682, listOf("CK"))
        map.put(683, listOf("NU"))
        map.put(685, listOf("WS"))
        map.put(686, listOf("KI"))
        map.put(687, listOf("NC"))
        map.put(688, listOf("TV"))
        map.put(689, listOf("PF"))
        map.put(690, listOf("TK"))
        map.put(691, listOf("FM"))
        map.put(692, listOf("MH"))
        map.put(800, listOf("001"))
        map.put(808, listOf("001"))
        map.put(850, listOf("KP"))
        map.put(852, listOf("HK"))
        map.put(853, listOf("MO"))
        map.put(855, listOf("KH"))
        map.put(856, listOf("LA"))
        map.put(870, listOf("001"))
        map.put(878, listOf("001"))
        map.put(880, listOf("BD"))
        map.put(881, listOf("001"))
        map.put(882, listOf("001"))
        map.put(883, listOf("001"))
        map.put(886, listOf("TW"))
        map.put(888, listOf("001"))
        map.put(960, listOf("MV"))
        map.put(961, listOf("LB"))
        map.put(962, listOf("JO"))
        map.put(963, listOf("SY"))
        map.put(964, listOf("IQ"))
        map.put(965, listOf("KW"))
        map.put(966, listOf("SA"))
        map.put(967, listOf("YE"))
        map.put(968, listOf("OM"))
        map.put(970, listOf("PS"))
        map.put(971, listOf("AE"))
        map.put(972, listOf("IL"))
        map.put(973, listOf("BH"))
        map.put(974, listOf("QA"))
        map.put(975, listOf("BT"))
        map.put(976, listOf("MN"))
        map.put(977, listOf("NP"))
        map.put(979, listOf("001"))
        map.put(992, listOf("TJ"))
        map.put(993, listOf("TM"))
        map.put(994, listOf("AZ"))
        map.put(995, listOf("GE"))
        map.put(996, listOf("KG"))
        map.put(998, listOf("UZ"))

        return map
    }

    private fun initCountryCodeByIsoMap() {
        val map = HashMap<String, Int>(MAX_COUNTRIES)

        for (i in 0 until COUNTRY_TO_REGION_CODES.size()) {
            val code = COUNTRY_TO_REGION_CODES.keyAt(i)
            val regions = COUNTRY_TO_REGION_CODES.get(code)

            for (region in regions) {
                if (region == "001") {
                    continue
                }
                if (map.containsKey(region)) {
                    throw IllegalStateException("Duplicate regions for country code: $code")
                }

                map[region] = code
            }
        }

        // This map used to be hardcoded so this is the diff from the generated version.
        map.remove("TA")
        map["HM"] = 672
        map["GS"] = 500
        map["XK"] = 381

        COUNTRY_TO_ISO_CODES = Collections.unmodifiableMap(map)
    }

    fun getCountryList(context: Context): ArrayList<String> {
        val codes = createCountryCodeToRegionCodeMap()
        val finalItems = ArrayList<String>()
        for (i in 0 until codes.size()) {
            val key = codes.keyAt(i)
            val innerList = codes.get(key)
            for (j in innerList.indices) {
                val langLocale = Locale(SharedPreferenceManager.getAppLanguage())
                val langContext = ContextWrapper.wrap(context, langLocale)
                val lang = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    langContext.resources.configuration?.locales?.get(0)
                } else {
                    langContext.resources.configuration?.locale
                }
                val locale = Locale(lang?.toString() ?: "", innerList[j])
                val finalString: String = "" + key + " - " + locale.displayName
                finalItems.add(finalString)
            }
        }
        return finalItems
    }
}

