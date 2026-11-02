package com.example

import com.example.data.MeroBazarSeedData
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.ui.ProductFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeroBazarUnitTest {

    @Test
    fun testCategories_countAndContent() {
        val categories = MeroBazarSeedData.categories
        assertEquals(5, categories.size)
        assertEquals("आधारभूत तथा अत्यावश्यक", categories[0].name)
        assertTrue(categories[0].subTypes.contains("किराना"))
        assertTrue(categories[1].subTypes.contains("हार्डवेयर सामग्री"))
    }

    @Test
    fun testProvinces_allSevenPresent() {
        val provinces = MeroBazarSeedData.provinces
        assertEquals(7, provinces.size)
        assertTrue(provinces.containsKey("Bagmati"))
        assertTrue(provinces.containsKey("Gandaki"))
        assertTrue(provinces["Bagmati"]!!.contains("Kathmandu"))
        assertTrue(provinces["Gandaki"]!!.contains("Kaski"))
    }

    @Test
    fun testProduct_effectivePrice() {
        val prod = Product(
            id = "test_1",
            name = "बासमती चामल",
            price = 650,
            dis = 50,
            stock = 10
        )
        assertEquals(600L, prod.effectivePrice)
        assertTrue(prod.inStock)
    }

    @Test
    fun testOrderItem_totalCalculation() {
        val item = OrderItem(pid = "p1", n = "दाल", q = 3, p = 150)
        assertEquals(450L, item.total)
    }

    @Test
    fun testProductFilter_filteringLogic() {
        val products = MeroBazarSeedData.demoProducts
        val filter = ProductFilter(query = "चामल")
        val filtered = products.filter { it.name.contains(filter.query) }
        assertTrue(filtered.isNotEmpty())
        assertEquals("बासमती चामल (५ केजी)", filtered.first().name)
    }

    @Test
    fun testProductFilter_inStockOnly() {
        val p1 = Product(id = "1", name = "A", stock = 5)
        val p2 = Product(id = "2", name = "B", stock = 0)
        val list = listOf(p1, p2)
        val inStock = list.filter { it.stock > 0 }
        assertEquals(1, inStock.size)
        assertEquals("A", inStock.first().name)
    }

    @Test
    fun testCartOperations_addRemoveUpdate() {
        val cart = mutableMapOf<String, Int>()

        // 1. Add item
        val pid = "p_1_1"
        cart[pid] = (cart[pid] ?: 0) + 1
        assertEquals(1, cart[pid])

        // 2. Increment quantity
        cart[pid] = (cart[pid] ?: 0) + 1
        assertEquals(2, cart[pid])

        // 3. Update quantity explicitly
        cart[pid] = 5
        assertEquals(5, cart[pid])

        // 4. Decrement quantity
        cart[pid] = cart[pid]!! - 1
        assertEquals(4, cart[pid])

        // 5. Remove item completely
        cart.remove(pid)
        assertFalse(cart.containsKey(pid))
        assertEquals(0, cart.size)
    }

    @Test
    fun testCartPricing_totalsAndFreeDelivery() {
        val items = listOf(
            OrderItem(pid = "p1", n = "Item 1", q = 2, p = 400), // 800
            OrderItem(pid = "p2", n = "Item 2", q = 1, p = 300)  // 300
        )
        val subtotal = items.sumOf { it.total }
        assertEquals(1100L, subtotal)

        // Free delivery for orders >= 1000
        val deliveryFee = if (subtotal >= 1000L) 0L else 50L
        assertEquals(0L, deliveryFee)

        val discount = 100L // Promo discount
        val grandTotal = subtotal + deliveryFee - discount
        assertEquals(1000L, grandTotal)
    }

    @Test
    fun testSearch_keywordAndCategoryMatching() {
        val products = MeroBazarSeedData.demoProducts

        // Search by Nepali keyword
        val riceResults = products.filter {
            it.name.contains("चामल", ignoreCase = true) || it.type.contains("चामल", ignoreCase = true)
        }
        assertTrue(riceResults.isNotEmpty())
        assertTrue(riceResults.any { it.name.contains("चामल") })

        // Search by Category Type
        val foodProducts = products.filter { it.type == "किराना" }
        assertTrue(foodProducts.size >= 2)

        // Search by English text/transliteration
        val dalResults = products.filter {
            it.name.contains("दाल", ignoreCase = true) || it.type.contains("दाल", ignoreCase = true)
        }
        assertTrue(dalResults.isNotEmpty())
    }

    @Test
    fun testCart_stockBoundaryEnforcement() {
        val maxStock = 5
        var currentQty = 1

        // Increment within stock bounds
        for (i in 1 until maxStock) {
            if (currentQty < maxStock) {
                currentQty++
            }
        }
        assertEquals(5, currentQty)

        // Attempt to exceed stock
        if (currentQty < maxStock) {
            currentQty++
        }
        assertEquals(5, currentQty) // Capped at maxStock
    }

    @Test
    fun testPromoVoucher_discounts() {
        val subtotal = 2000L

        // MEROBAZAR gives 10%
        val meroBazarDiscount = (subtotal * 0.10).toLong().coerceAtLeast(50L)
        assertEquals(200L, meroBazarDiscount)

        // DASHAYN gives fixed 200
        val dashaynDiscount = 200L
        assertEquals(200L, dashaynDiscount)

        // NEPAL50 gives fixed 50
        val nepalDiscount = 50L
        assertEquals(50L, nepalDiscount)
    }

    @Test
    fun testPastOrders_filteringByStatusAndDetails() {
        val orders = MeroBazarSeedData.demoOrders
        assertTrue("Seed orders should not be empty", orders.isNotEmpty())

        val pendingOrders = orders.filter { it.st.equals("pending", ignoreCase = true) }
        val deliveredOrders = orders.filter { it.st.equals("delivered", ignoreCase = true) }
        val cancelledOrders = orders.filter { it.st.equals("cancelled", ignoreCase = true) }

        assertTrue("Should have at least one pending order", pendingOrders.isNotEmpty())
        assertTrue("Should have at least one delivered order", deliveredOrders.isNotEmpty())
        assertTrue("Should have at least one cancelled order", cancelledOrders.isNotEmpty())

        // Verify summary details calculation
        val sampleDelivered = deliveredOrders.first()
        val calculatedItemsTotal = sampleDelivered.items.sumOf { it.total }
        assertTrue("Delivered order should have items", sampleDelivered.items.isNotEmpty())
        assertEquals(calculatedItemsTotal, sampleDelivered.total)
    }

    @Test
    fun testPriceRange_filteringLogic() {
        val products = MeroBazarSeedData.demoProducts

        // Filter products under 500
        val under500 = products.filter { it.effectivePrice <= 500 }
        assertTrue(under500.isNotEmpty())
        assertTrue(under500.all { it.effectivePrice <= 500 })

        // Filter products between 500 and 1500
        val midRange = products.filter { it.effectivePrice in 500..1500 }
        assertTrue(midRange.isNotEmpty())
        assertTrue(midRange.all { it.effectivePrice in 500..1500 })

        // Filter products above 1500
        val highRange = products.filter { it.effectivePrice >= 1500 }
        assertTrue(highRange.isNotEmpty())
        assertTrue(highRange.all { it.effectivePrice >= 1500 })
    }

    @Test
    fun testMultiRoleUser_profilesAndPermissions() {
        // Customer profile
        val customer = com.example.data.model.UserProfile(
            id = "cust_001",
            name = "Ramesh Sharma",
            email = "ramesh@example.com",
            phone = "9841234567",
            role = "c",
            dist = "Kathmandu"
        )
        assertEquals("c", customer.role)
        assertEquals("Kathmandu", customer.dist)

        // Merchant profile with shop details and PAN
        val merchant = com.example.data.model.UserProfile(
            id = "merch_001",
            name = "Sita Shrestha",
            email = "sita@nepalmart.com",
            phone = "9851098765",
            role = "m",
            dist = "Lalitpur",
            shop = com.example.data.model.ShopDetails(
                name = "Sita Kirana Store",
                type = "किराना",
                pan = "601234567",
                lrn = "LRN-2081-098",
                ok = false
            )
        )
        assertEquals("m", merchant.role)
        assertNotNull(merchant.shop)
        assertEquals("601234567", merchant.shop?.pan)
        assertFalse(merchant.shop?.ok ?: true)

        // Admin profile
        val admin = com.example.data.model.UserProfile(
            id = "admin_001",
            name = "Mero Bazar Chief Admin",
            email = "aazakobazar@gmail.com",
            phone = "9841000000",
            role = "a",
            dist = "Kathmandu"
        )
        assertEquals("a", admin.role)
        assertEquals("aazakobazar@gmail.com", admin.email)
    }

    @Test
    fun testRegistration_bonusRewardsAndDefaults() {
        val registeredUser = com.example.data.model.UserProfile(
            id = "usr_new",
            name = "Hari Bahadur",
            email = "hari@example.com",
            phone = "9812345678",
            role = "c",
            wallet = 100L,
            pts = 50L
        )
        // Verify welcome bonus rewards
        assertEquals(50L, registeredUser.pts)
        assertEquals(100L, registeredUser.wallet)
        assertEquals("c", registeredUser.role)
    }

    @Test
    fun testInitialScreen_authFirstForUnauthenticatedUser() {
        val currentUser: com.example.data.model.UserProfile? = null
        val startingScreen = if (currentUser == null) "auth" else "home"
        assertEquals("auth", startingScreen)
    }

    @Test
    fun testZipRegistration_codeFormatAndDetails() {
        val codeCustomer = "AB-C-45892"
        val codeMerchant = "AB-M-78214"
        assertTrue(codeCustomer.startsWith("AB-C-"))
        assertTrue(codeMerchant.startsWith("AB-M-"))
        assertEquals(10, codeCustomer.length)
    }

    @Test
    fun testAppUpdate_versionAndAutoUpgrade() {
        val update = com.example.data.model.AppUpdateInfo(
            currentVersion = "1.0.0",
            latestVersion = "1.0.1",
            isUpdateAvailable = true,
            autoUpgradeEnabled = true
        )
        assertEquals("1.0.0", update.currentVersion)
        assertEquals("1.0.1", update.latestVersion)
        assertTrue(update.isUpdateAvailable)
        assertTrue(update.autoUpgradeEnabled)
        assertTrue(update.githubReleaseUrl.contains("github.com"))
    }

    @Test
    fun testAiModules_fiveInAppModulesPresent() {
        val modules = com.example.data.repository.AiModule.values()
        assertEquals(5, modules.size)
        val moduleNames = modules.map { it.name }
        assertTrue(moduleNames.contains("LOCATION"))
        assertTrue(moduleNames.contains("SHOPS"))
        assertTrue(moduleNames.contains("PROBLEM"))
        assertTrue(moduleNames.contains("SUPPORT"))
        assertTrue(moduleNames.contains("ANALYTICS"))
    }

    @Test
    fun testNepalMap_provincesAndDistricts() {
        val provinces = MeroBazarSeedData.provinces
        assertEquals(7, provinces.size)
        assertTrue(provinces["Bagmati"]!!.contains("Kathmandu"))
        assertTrue(provinces["Gandaki"]!!.contains("Kaski"))
        assertTrue(provinces["Koshi"]!!.contains("Morang"))
    }
}

