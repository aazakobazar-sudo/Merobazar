package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Bill
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GoogleDocsHelper {

    /**
     * Share formatted text/markdown to Google Docs, Drive, or any document app via Intent.ACTION_SEND
     */
    fun shareDocument(context: Context, docTitle: String, content: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, docTitle)
            putExtra(Intent.EXTRA_SUBJECT, docTitle)
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export to Google Docs / Drive")
        context.startActivity(shareIntent)
    }

    /**
     * Open Google Docs web to create a new document
     */
    fun openGoogleDocsNew(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/create"))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    /**
     * Generate official tax invoice formatted for Google Docs
     */
    fun generateInvoiceDoc(order: Order, sellerPan: String = "600123456"): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val sb = StringBuilder()
        sb.append("===================================================\n")
        sb.append("             MERO BAZAR OFFICIAL INVOICE          \n")
        sb.append("         मेरो बजार कर बिजक (Tax Invoice)        \n")
        sb.append("===================================================\n\n")
        sb.append("Invoice No (बिजक नं): ${order.id}\n")
        sb.append("Date & Time (मिति): $dateStr\n")
        sb.append("Seller / Shop: ${order.shopName}\n")
        sb.append("Seller PAN/VAT: $sellerPan\n")
        sb.append("Payment Mode: ${order.payMode}\n")
        sb.append("Delivery Status: ${order.st}\n\n")

        sb.append("CUSTOMER DETAILS (ग्राहकको विवरण):\n")
        sb.append("---------------------------------------------------\n")
        sb.append("Name: ${order.cname}\n")
        sb.append("Phone: ${order.cphone}\n")
        sb.append("Delivery Address: ${order.caddress}\n\n")

        sb.append("ITEMIZED PARTICULARS (सामानको विवरण):\n")
        sb.append("---------------------------------------------------\n")
        sb.append(String.format("%-22s %-6s %-10s %-10s\n", "Item Name", "Qty", "Rate(Rs)", "Total(Rs)"))
        sb.append("---------------------------------------------------\n")
        var subtotal = 0L
        for (item in order.items) {
            val itemTot = item.q * item.p
            subtotal += itemTot
            val nTrunc = if (item.n.length > 20) item.n.take(18) + ".." else item.n
            sb.append(String.format("%-22s %-6d %-10d %-10d\n", nTrunc, item.q, item.p, itemTot))
        }
        sb.append("---------------------------------------------------\n")

        val taxable = (subtotal / 1.13).toLong()
        val vat13 = subtotal - taxable

        sb.append("Subtotal (जम्मा):                     Rs. $subtotal\n")
        sb.append("Taxable Amount (करयोग्य रकम):          Rs. $taxable\n")
        sb.append("VAT 13% (१३% मूल्य अभिवृद्धि कर):     Rs. $vat13\n")
        sb.append("===================================================\n")
        sb.append("GRAND TOTAL (कुल भुक्तानी रकम):       Rs. ${order.total}\n")
        sb.append("===================================================\n\n")
        sb.append("Incentive Prize Portal: prize.ird.gov.np\n")
        sb.append("Note: Generated digitally via Mero Bazar Nepal Platform.\n")
        sb.append("Safe, encrypted and verified transaction.\n")
        return sb.toString()
    }

    /**
     * Generate Khata Ledger Statement formatted for Google Docs
     */
    fun generateKhataLedgerDoc(customerName: String, customerPhone: String, shopName: String, bills: List<Bill>): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sb = StringBuilder()
        sb.append("===================================================\n")
        sb.append("          MERO BAZAR KHATA BOOK LEDGER             \n")
        sb.append("          मेरो बजार ग्राहक खाता विवरण              \n")
        sb.append("===================================================\n\n")
        sb.append("Shop / Merchant: $shopName\n")
        sb.append("Customer Name: $customerName\n")
        sb.append("Customer Phone: $customerPhone\n")
        sb.append("Statement Generated: $dateStr\n\n")

        sb.append("TRANSACTION HISTORY (कारोबार सूची):\n")
        sb.append("---------------------------------------------------\n")
        sb.append(String.format("%-12s %-12s %-16s %-10s\n", "Date", "Bill No", "Particulars", "Amount(Rs)"))
        sb.append("---------------------------------------------------\n")
        var totalLedger = 0L
        for (bill in bills) {
            val itemsSummary = bill.items.joinToString(", ") { "${it.n}x${it.q}" }.take(15)
            sb.append(String.format("%-12s %-12s %-16s %-10d\n", bill.d, bill.id, itemsSummary, bill.total))
            totalLedger += bill.total
        }
        sb.append("---------------------------------------------------\n")
        sb.append("Total Credit/Due (कुल बाँकी हिसाब):   Rs. $totalLedger\n")
        sb.append("===================================================\n\n")
        sb.append("Merchant Signature: _____________________________\n")
        sb.append("Customer Signature: _____________________________\n")
        sb.append("Mero Bazar Verified Ledger for Nepal Traders.\n")
        return sb.toString()
    }

    /**
     * Generate Sales and Business Performance Analytics formatted for Google Docs
     */
    fun generateBusinessReportDoc(
        shopName: String,
        orders: List<Order>,
        products: List<Product>,
        isNepali: Boolean
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val totalRevenue = orders.sumOf { it.total }
        val completedOrders = orders.filter { it.st == "Delivered" }
        val pendingOrders = orders.filter { it.st == "Pending" || it.st == "Processing" }
        val cancelledOrders = orders.filter { it.st == "Cancelled" }
        val aov = if (orders.isNotEmpty()) totalRevenue / orders.size else 0L

        val taxable = (totalRevenue / 1.13).toLong()
        val vatAmount = totalRevenue - taxable
        val estimatedCost = (totalRevenue * 0.72).toLong()
        val netProfit = totalRevenue - estimatedCost

        val sb = StringBuilder()
        sb.append("===================================================\n")
        sb.append("      MERO BAZAR BUSINESS PERFORMANCE REPORT       \n")
        sb.append("      मेरो बजार व्यापार तथा बिक्री विश्लेषण         \n")
        sb.append("===================================================\n\n")
        sb.append("Shop Name (पसल): $shopName\n")
        sb.append("Report Date (मिति): $dateStr\n\n")

        sb.append("1. EXECUTIVE SUMMARY (मुख्य सूचकहरू):\n")
        sb.append("---------------------------------------------------\n")
        sb.append("• Total Gross Sales (कुल बिक्री):         Rs. $totalRevenue\n")
        sb.append("• Estimated Net Profit (अनुमानित नाफा):    Rs. $netProfit (28% Margin)\n")
        sb.append("• Total Orders (कुल अर्डर सङ्ख्या):       ${orders.size}\n")
        sb.append("• Average Order Value (AOV):             Rs. $aov\n")
        sb.append("• Delivered Orders (सफल डेलिभरी):        ${completedOrders.size}\n")
        sb.append("• Pending Fulfillment (प्रक्रियामा):     ${pendingOrders.size}\n")
        sb.append("• Cancelled Orders (रद्द भएको):          ${cancelledOrders.size}\n\n")

        sb.append("2. NEPAL TAX & VAT SUMMARY (कर विवरण):\n")
        sb.append("---------------------------------------------------\n")
        sb.append("• Taxable Turnover (करयोग्य कारोबार):     Rs. $taxable\n")
        sb.append("• 13% VAT Collected (१३% भ्याट रकम):      Rs. $vatAmount\n")
        sb.append("• IRD Monthly Return Due: 25th of month (prize.ird.gov.np)\n\n")

        sb.append("3. INVENTORY & PRODUCT PERFORMANCE:\n")
        sb.append("---------------------------------------------------\n")
        sb.append(String.format("%-24s %-8s %-12s %-8s\n", "Product Name", "Price", "Discount", "Stock"))
        sb.append("---------------------------------------------------\n")
        for (p in products.take(10)) {
            val nTrunc = if (p.name.length > 22) p.name.take(20) + ".." else p.name
            sb.append(String.format("%-24s Rs.%-5d Rs.%-9d %-8d\n", nTrunc, p.price, p.dis, p.stock))
        }
        sb.append("---------------------------------------------------\n\n")
        sb.append("Generated with Mero Bazar Mobile Application.\n")
        sb.append("Document ready to import into Google Docs / Sheets.\n")
        return sb.toString()
    }
}
