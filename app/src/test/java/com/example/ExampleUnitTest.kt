package com.example

import com.example.data.service.DownloadService
import com.example.data.service.LinkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `validateLink returns invalid for blank input`() {
        val result = DownloadService.validateLink("   ")
        assertEquals(LinkType.INVALID, result.type)
        assertEquals("লিংক খালি রাখা যাবে না", result.message)
    }

    @Test
    fun `validateLink returns invalid for malformed or userInfo urls`() {
        val withSpaces = DownloadService.validateLink("not a valid url")
        assertEquals(LinkType.INVALID, withSpaces.type)
        assertEquals("সঠিক URL দিন", withSpaces.message)

        val withUserInfo = DownloadService.validateLink("https://user:pass@example.com/video.mp4")
        assertEquals(LinkType.INVALID, withUserInfo.type)
        assertEquals("সঠিক URL দিন", withUserInfo.message)
    }

    @Test
    fun `validateLink adds https and detects direct video`() {
        val result = DownloadService.validateLink("cdn.example.com/clip.mp4?token=abc")
        assertEquals(LinkType.DIRECT_VIDEO, result.type)
        assertEquals("ভিডিও লিংক শনাক্ত হয়েছে", result.message)
        assertEquals("https://cdn.example.com/clip.mp4?token=abc", result.url)
    }

    @Test
    fun `validateLink detects direct audio formats`() {
        val result = DownloadService.validateLink("https://cdn.example.com/track.mp3")
        assertEquals(LinkType.DIRECT_AUDIO, result.type)
        assertEquals("অডিও লিংক শনাক্ত হয়েছে", result.message)
    }

    @Test
    fun `validateLink detects social media hosts and subdomains`() {
        val yt = DownloadService.validateLink("https://m.youtube.com/watch?v=dQw4w9WgXcQ")
        assertEquals(LinkType.SOCIAL_MEDIA, yt.type)
        assertEquals("সোশ্যাল মিডিয়া লিংক শনাক্ত হয়েছে", yt.message)

        val fb = DownloadService.validateLink("fb.watch/abc123")
        assertEquals(LinkType.SOCIAL_MEDIA, fb.type)
        assertEquals("https://fb.watch/abc123", fb.url)
    }

    @Test
    fun `validateLink rejects non media html page`() {
        val result = DownloadService.validateLink("https://example.com/article.html")
        assertEquals(LinkType.INVALID, result.type)
        assertEquals("এটি সরাসরি মিডিয়া লিংক নয়", result.message)
    }

    @Test
    fun `isStatusValid checks 200 to 399`() {
        assertTrue(DownloadService.isStatusValid(200))
        assertTrue(DownloadService.isStatusValid(302))
        assertFalse(DownloadService.isStatusValid(403))
        assertFalse(DownloadService.isStatusValid(404))
    }

    @Test
    fun `isMediaContentType accepts video audio and octet stream only`() {
        assertTrue(DownloadService.isMediaContentType("video/mp4"))
        assertTrue(DownloadService.isMediaContentType("Audio/MPEG"))
        assertTrue(DownloadService.isMediaContentType("application/octet-stream; charset=binary"))
        assertFalse(DownloadService.isMediaContentType("text/html"))
        assertFalse(DownloadService.isMediaContentType(null))
    }

    @Test
    fun `sanitizeFileName cleans special characters and uses timestamp fallback`() {
        assertEquals(
            "big_buck_bunny_1_.mp4",
            DownloadService.sanitizeFileName("https://cdn.example.com/big%20buck(bunny@1).mp4")
        )
        assertEquals(
            "big_buck_bunny_1_.mp4",
            DownloadService.sanitizeFileName("https://cdn.example.com/big buck(bunny@1).mp4")
        )
        assertEquals(
            "AR_media_12345",
            DownloadService.sanitizeFileName("https://cdn.example.com/", nowMs = 12345L)
        )
    }

    @Test
    fun `isYouTubeUrl detects youtube and youtu_be variants only`() {
        assertTrue(DownloadService.isYouTubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue(DownloadService.isYouTubeUrl("m.youtube.com/shorts/abc123"))
        assertTrue(DownloadService.isYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"))
        assertFalse(DownloadService.isYouTubeUrl("https://www.facebook.com/watch?v=123"))
        assertFalse(DownloadService.isYouTubeUrl("https://cdn.example.com/video.mp4"))
    }
}
