package com.example.orthodoxapp.util

import java.util.Calendar

object EthiopianDateUtils {
    /**
     * Approximate mapping of Gregorian timestamp to Ethiopian month index (0-12).
     * Meskerem (0) starts around Sept 11.
     */
    fun getEthiopianMonth(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        
        val month = cal.get(Calendar.MONTH) // 0-indexed (Jan=0, Sep=8)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        
        return when (month) {
            Calendar.SEPTEMBER -> if (day >= 11) 0 else 11
            Calendar.OCTOBER -> if (day >= 11) 1 else 0
            Calendar.NOVEMBER -> if (day >= 10) 2 else 1
            Calendar.DECEMBER -> if (day >= 10) 3 else 2
            Calendar.JANUARY -> if (day >= 9) 4 else 3
            Calendar.FEBRUARY -> if (day >= 8) 5 else 4
            Calendar.MARCH -> if (day >= 10) 6 else 5
            Calendar.APRIL -> if (day >= 9) 7 else 6
            Calendar.MAY -> if (day >= 9) 8 else 7
            Calendar.JUNE -> if (day >= 8) 9 else 8
            Calendar.JULY -> if (day >= 8) 10 else 9
            Calendar.AUGUST -> if (day >= 7) 11 else 10
            else -> 0
        }
    }
}
