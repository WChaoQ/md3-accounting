package com.wchaoq.md3accounting

import com.wchaoq.md3accounting.util.fmtAmount
import com.wchaoq.md3accounting.util.parseAmountToCents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test
    fun compactAmountUsesExactCents() {
        assertEquals("0.29", fmtAmount(29L))
        assertEquals("1", fmtAmount(100L))
        assertEquals("12.3", fmtAmount(1_230L))
        assertEquals("1.2万", fmtAmount(1_234_567L))
    }

    @Test
    fun parsesDecimalAmountsWithoutFloatingPoint() {
        assertEquals(50L, parseAmountToCents(".5"))
        assertEquals(1_250L, parseAmountToCents("12.5"))
        assertEquals(1_205L, parseAmountToCents("12.05"))
        assertEquals(1_200L, parseAmountToCents("12"))
    }

    @Test
    fun rejectsInvalidOrUnsafeAmounts() {
        assertNull(parseAmountToCents(""))
        assertNull(parseAmountToCents("."))
        assertNull(parseAmountToCents("0"))
        assertNull(parseAmountToCents("1.234"))
        assertNull(parseAmountToCents("1.2.3"))
        assertNull(parseAmountToCents("1000000000"))
        assertNull(parseAmountToCents("-1"))
    }
}
