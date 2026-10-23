package com.wchaoq.md3accounting.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun compactAmountPreservesExactCents() {
        assertEquals("0.29", fmtAmount(29L))
        assertEquals("0.25", fmtAmount(25L))
        assertEquals("0.2", fmtAmount(20L))
        assertEquals("1", fmtAmount(100L))
    }

    @Test
    fun compactAmountUsesShortTenThousandsNotation() {
        assertEquals("1万", fmtAmount(1_000_000L))
        assertEquals("1.5万", fmtAmount(1_500_000L))
    }

    @Test
    fun parsesDecimalAmountsWithoutFloatingPoint() {
        assertEquals(50L, parseAmountToCents(".5"))
        assertEquals(1_250L, parseAmountToCents("12.5"))
        assertEquals(1_234L, parseAmountToCents("12.34"))
    }

    @Test
    fun rejectsInvalidOrOutOfRangeAmounts() {
        assertNull(parseAmountToCents(""))
        assertNull(parseAmountToCents("0"))
        assertNull(parseAmountToCents("1.234"))
        assertNull(parseAmountToCents("1.2.3"))
        assertNull(parseAmountToCents("1000000000"))
    }
}
