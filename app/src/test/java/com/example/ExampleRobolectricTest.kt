package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Mero Bazar", appName)
  }

  @Test
  fun `launch MainActivity`() {
    org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
  }

  @Test
  fun `verify admin account login`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.repository.MeroBazarRepository(context)
    val res = repo.signInWithEmailOrPhone("aazakobazar@gmail.com", "A11222333")
    org.junit.Assert.assertTrue(res.isSuccess)
    assertEquals("a", repo.currentUser.value?.role)
    assertEquals("aazakobazar@gmail.com", repo.currentUser.value?.email)
  }

  @Test
  fun `verify merchant registration starts as unverified requiring admin kyc`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.repository.MeroBazarRepository(context)
    val res = repo.signUpWithEmail(
      name = "नयाँ व्यापारी",
      email = "newmerchant@merobazar.np",
      pass = "Pass123456",
      role = "m",
      phone = "9841999888",
      autoSignIn = true
    )
    org.junit.Assert.assertTrue(res.isSuccess)
    val profile = repo.currentUser.value
    assertEquals("m", profile?.role)
    assertEquals(false, profile?.shop?.ok) // Must be unverified until Admin approves!
  }

  @Test
  fun `verify password reset flow`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.repository.MeroBazarRepository(context)
    val res = repo.resetPassword("customer@merobazar.np")
    org.junit.Assert.assertTrue(res.isSuccess)
  }
}
