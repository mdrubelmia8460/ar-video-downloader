package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.service.DownloadService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AR All Video Downloader", appName)
  }

  @Test
  fun `downloadDirect rejects social media url`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val ex = assertThrows(IllegalArgumentException::class.java) {
      runBlocking {
        DownloadService.downloadDirect(
          context = context,
          url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        )
      }
    }
    assertEquals("শুধু সরাসরি অনুমোদিত মিডিয়া URL ডাউনলোড করা যাবে", ex.message)
  }
}
