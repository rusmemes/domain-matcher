package domainmatcher

import org.junit.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DomainMatcherTest {

    @Test
    fun test0() {
        val matcher = DomainMatcher.create(
            listOf(
                "A-b-Cd.cale-ndar-.go-ogle.cOm"
            )
        )
        assertTrue(matcher matched "a-b-cd.cale-ndar-.go-ogle.com")
        assertTrue(matcher matched "-a-b-cd.cale-ndar-.go-ogle.com")
        assertFalse(matcher matched "-b-cd.cale-ndar-.go-ogle.com")
        assertFalse(matcher matched "b-cd.cale-ndar-.go-ogle.com")
        assertFalse(matcher matched "-cd.cale-ndar-.go-ogle.com")
        assertFalse(matcher matched "cd.cale-ndar-.go-ogle.com")
        assertFalse(matcher matched "-d.cale-ndar-.go-ogle.com")
        assertFalse(matcher matched "d.cale-ndar-.go-ogle.com")
        assertFalse(matcher matched "cale-ndar.go-ogle.com")
    }

    @Test
    fun test1() {
        val matcher = DomainMatcher.create(
            listOf(
                "AbCd.calendar.google.cOm"
            )
        )
        assertTrue(matcher matched "abcd.calendar.google.com")
        assertTrue(matcher matched "1abcd.calendar.google.com")
        assertFalse(matcher matched "bcd.calendar.google.com")
        assertFalse(matcher matched "cd.calendar.google.com")
        assertFalse(matcher matched "d.calendar.google.com")
        assertFalse(matcher matched "calendar.google.com")
    }

    @Test
    fun test2() {
        val matcher = DomainMatcher.create(
            listOf(
                "CAlEnDaR.calendar.google.cOm",
                "alEnDaR.calendar.google.cOm",
            )
        )
        assertTrue(matcher matched "CAlEnDaR.calendar.google.cOm")
        assertTrue(matcher matched "alEnDaR.calendar.google.cOm")
        assertTrue(matcher matched "superalEnDaR.calendar.google.cOm")
        assertTrue(matcher matched "superCalEnDaR.calendar.google.cOm")
    }

    @Test
    fun test3() {
        val matcher = DomainMatcher.create(
            listOf(
                "car.google.cOm"
            )
        )
        assertTrue(matcher matched "car.google.Com")
        assertTrue(matcher matched "supercar.google.coM")
        assertFalse(matcher matched "r.google.cOm")
        assertFalse(matcher matched "google.cOm")
    }

    @Test
    fun test4() {
        val patterns = listOf(
            "http://AbCd.calendar.google.cOm/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "https://CAlEnDaR.calendar.google.cOm/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "alEnDaR.calendar.google.cOm/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "https://www.car.google.cOm/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "ru/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}"
        )

        val matcher = DomainMatcher.create(patterns)

        assertFalse(matcher matched "yahoo.com")
        assertFalse(matcher matched "calendar.google.com")

        listOf(
            "http://efgh.abcd.calendar.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "https://car.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "auto.car.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "www.car.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "https://www.car.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "http://www.car.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "car.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "abcd.calendar.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "one.supercalendar.calendar.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "one.alendar.calendar.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "alendar.calendar.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "balendar.calendar.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "aalendar.calendar.google.com/${UUID.randomUUID()}?${UUID.randomUUID()}=${UUID.randomUUID()}",
            "mail.ru",
        ).forEach {
            assertTrue(matcher matched it, "failed to match $it")
        }
    }

    @Test
    fun testCorrectUrl1() {
        val matcher = DomainMatcher.create(
            listOf(
                "blabla.com",
            )
        )

        assertTrue(matcher matched "https://blabla.com")
        assertFalse(matcher matched "https://bla-bla.com")
    }

    @Test
    fun testCorrectUrl2() {
        val matcher = DomainMatcher.create(
            listOf(
                "bla-bla.com",
            )
        )

        assertFalse(matcher matched "https://blabla.com")
        assertTrue(matcher matched "https://bla-bla.com")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl1() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched ".blabla"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl2() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched "//blabla.com"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl3() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched "http//blabla.com"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl4() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched "http:/blabla.com"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl5() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched "https//blabla.com"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl6() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched "https:/blabla.com"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl7() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched "http-s://blabla.com"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl8() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched "-https://blabla.com"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl9() {
        val matcher = DomainMatcher.create(emptyList())

        matcher matched "https://\$blabla.com"
    }

    @Test(expected = IllegalArgumentException::class)
    fun testWrongUrl10() {
        DomainMatcher.create(listOf("\$blabla.com"))
    }
}
