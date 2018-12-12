package ru.evotor.framework.inventory.product.provider

import android.net.Uri
import ru.evotor.framework.inventory.provider.InventoryContract

internal object ProductContract {
    private const val PATH = "products"

    val URI: Uri = Uri.withAppendedPath(InventoryContract.URI, PATH)

    const val COLUMN_CLASS_ID = "CLASS_ID"
    const val COLUMN_UUID = "UUID"
    const val COLUMN_GROUP_UUID = "GROUP_UUID"
    const val COLUMN_NAME = "NAME"
    const val COLUMN_CODE = "CODE"
    const val COLUMN_VENDOR_CODE = "VENDOR_CODE"
    const val COLUMN_BARCODES = "BARCODES"
    const val COLUMN_PURCHASE_PRICE = "PURCHASE_PRICE"
    const val COLUMN_SELLING_PRICE = "SELLING_PRICE"
    const val COLUMN_VAT_RATE = "VAT_RATE"
    const val COLUMN_QUANTITY = "QUANTITY"
    const val COLUMN_DESCRIPTION = "DESCRIPTION"
    const val COLUMN_ALLOWED_TO_SELL = "ALLOWED_TO_SELL"
}