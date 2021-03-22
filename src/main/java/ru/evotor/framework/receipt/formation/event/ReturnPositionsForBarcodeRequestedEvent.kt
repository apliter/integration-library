package ru.evotor.framework.receipt.formation.event

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import ru.evotor.framework.ParcelableUtils
import ru.evotor.framework.common.event.IntegrationEvent
import ru.evotor.framework.receipt.Position

/**
 * Событие, которое приходит после сканирования штрихкода товара.
 *
 * Штрихкод может быть любого формата (EAN-13, QR-код, DataMatrix или другой) и, кроме цифрового значения, содержать различные данные, например, вес товара.
 *
 * Обрабатывая данные, содержащиеся в событии, приложения могут добавлять позиции в чек и / или создавать новые товары.
 *
 * @property barcode строка данных, полученных от сканера штрихкодов.
 * @property extractedData агрегированные данные, которые удалось получить из отсканированного штрихкода
 * @property creatingNewProduct указывает на необходимость создать новый товар. Сразу после сканирования штрихкода всегда содержит false.
 * @see <a href="https://developer.evotor.ru/docs/doc_java_return_positions_for_barcode_requested.html">Обработка события сканирования штрихкода</a>
 */
data class ReturnPositionsForBarcodeRequestedEvent(
        val barcode: String,
        val extractedData: DataExtracted? = null,
        val creatingNewProduct: Boolean
) : IntegrationEvent() {

    override fun toBundle() = Bundle().apply {
        putString(KEY_BARCODE_EXTRA, barcode)
        putParcelable(KEY_EXTRACTED_DATA_EXTRA, extractedData)
        putBoolean(KEY_CREATE_PRODUCT_EXTRA, creatingNewProduct)
    }

    companion object {
        const val KEY_BARCODE_EXTRA = "key_barcode_extra"
        const val KEY_EXTRACTED_DATA_EXTRA = "key_extracted_data_extra"
        const val KEY_CREATE_PRODUCT_EXTRA = "key_create_product_extra"

        @JvmStatic
        fun from(bundle: Bundle?) = bundle?.let {
            ReturnPositionsForBarcodeRequestedEvent(
                    barcode = it.getString(KEY_BARCODE_EXTRA)
                            ?: return null,
                    extractedData = it.getParcelable<DataExtracted>(KEY_EXTRACTED_DATA_EXTRA),
                    creatingNewProduct = it.getBoolean(KEY_CREATE_PRODUCT_EXTRA))
        }
    }

    /**
     * Результат обработки события сканирования штрихкода.
     *
     * @property positionsList список списков позиций, все позиции из одного списка будут добавлены в чек вместе. Используется на версиях ЭвоторПос выше 6.35.0
     * @property positions список позиций, которые будут отображены пользователю для выбора, будет добавлена только одна  (использовать для обратной совместимости)
     * @property iCanCreateNewProduct указывает, будет приложение создавать товар на основе отсканированного штрихкода или нет.
     */
    data class Result(
            val iCanCreateNewProduct: Boolean,
            @Deprecated("""Использовать для обратной совместимости.
                    На старых версиях кассы (с приложением ЭвоторПос ниже 6.35) будут браться значения отсюда,
                    на новых версиях кассы (с приложением ЭвоторПос выше 6.35) будут браться из @property positionsList
                    Будет удалено после обновления рынка.""")
            val positions: List<Position>,
            val positionsList: List<List<Position>>
    ) : IntegrationEvent.Result() {

        @Deprecated("Используйте основной конструктор")
        constructor(
                positions: List<Position>,
                iCanCreateNewProduct: Boolean
        ) : this(iCanCreateNewProduct, positions, emptyList())

        override fun toBundle() = Bundle().apply {
            classLoader = Position::class.java.classLoader
            putInt(KEY_EXTRA_POSITIONS_COUNT, positions.size)
            if (!positions.isNullOrEmpty()) {
                for (i in positions.indices) {
                    putParcelable(KEY_EXTRA_POSITIONS + i, positions[i])
                }
            }
            if (!positionsList.isNullOrEmpty()) {
                putInt(KEY_EXTRA_POSITIONS_LIST_COUNT, positionsList.size)
                for (i in positionsList.indices) {
                    putInt(KEY_EXTRA_SUB_POSITIONS_COUNT + i, positionsList[i].size)
                    for (j in positionsList[i].indices) {
                        putParcelable("${KEY_EXTRA_SUB_POSITIONS_LIST}_${i}_${j}", positionsList[i][j]) // KEY_EXTRA_SUB_POSITIONS_LIST0
                    }
                }
            }

            putBoolean(KEY_EXTRA_CAN_CREATE, iCanCreateNewProduct)
        }

        companion object {
            private const val KEY_EXTRA_POSITIONS = "extra_position_"
            private const val KEY_EXTRA_POSITIONS_COUNT = "extra_positions_count"
            const val KEY_EXTRA_POSITIONS_LIST_COUNT = "extra_positions_list_count"
            const val KEY_EXTRA_SUB_POSITIONS_LIST = "extra_sub_positions_list_"
            const val KEY_EXTRA_SUB_POSITIONS_COUNT = "extra_sub_positions_count"
            const val KEY_EXTRA_CAN_CREATE = "extra_can_create_product"

            @JvmStatic
            fun from(bundle: Bundle?) = bundle?.let {
                it.classLoader = Position::class.java.classLoader
                val count = it.getInt(KEY_EXTRA_POSITIONS_COUNT)
                val positions = mutableListOf<Position>()

                for (i in 0 until count) {
                    positions.add(it.getParcelable(KEY_EXTRA_POSITIONS + i) ?: return null)
                }

                val iCanCreateNewProduct = it.getBoolean(KEY_EXTRA_CAN_CREATE)

                val listCount = it.getInt(KEY_EXTRA_POSITIONS_LIST_COUNT)
                val positionsList = mutableListOf<List<Position>>()

                if (listCount != 0) {
                    for (i in 0 until listCount) {
                        val subListCount = it.getInt(KEY_EXTRA_SUB_POSITIONS_COUNT + i)
                        val subList = mutableListOf<Position>()
                        for (j in 0 until subListCount) {
                            subList.add(
                                    it.getParcelable("${KEY_EXTRA_SUB_POSITIONS_LIST}_${i}_${j}")
                                            ?: return null
                            )
                        }
                        positionsList.add(subList)
                    }
                }

                Result(iCanCreateNewProduct, positions, positionsList)
            }
        }
    }

    data class DataExtracted(
            val ean: String?
    ) : Parcelable {

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            ParcelableUtils.writeExpand(dest, VERSION) { parcel ->
                parcel.writeString(ean)
            }
        }

        companion object {

            /**
             * Текущая версия объекта
             */
            private const val VERSION = 1

            @JvmField
            val CREATOR = object : Parcelable.Creator<DataExtracted> {
                override fun createFromParcel(parcel: Parcel): DataExtracted = create(parcel)
                override fun newArray(size: Int): Array<DataExtracted?> = arrayOfNulls(size)
            }

            private fun create(dest: Parcel): DataExtracted {
                var dataExtracted: DataExtracted? = null

                ParcelableUtils.readExpand(dest, VERSION) { parcel, version ->

                    var ean: String? = null

                    if (version >= 1) {
                        ean = parcel.readString()
                    }

                    checkNotNull(ean)
                    dataExtracted = DataExtracted(
                            ean = ean
                    )
                }
                checkNotNull(dataExtracted)
                return dataExtracted as DataExtracted
            }
        }
    }
}
