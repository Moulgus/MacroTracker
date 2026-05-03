package com.moulgus.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moulgus.macrotracker.data.local.entity.ProductUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductUnitDao {

    @Query("SELECT * FROM product_units WHERE productID = :productID ORDER BY unitName ASC")
    fun observeUnitsForProduct(productID: Long): Flow<List<ProductUnitEntity>>

    @Query("SELECT * FROM product_units WHERE productID = :productID ORDER BY unitName ASC")
    suspend fun getUnitsForProduct(productID: Long): List<ProductUnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductUnit(unit: ProductUnitEntity): Long

    @Update
    suspend fun updateProductUnit(unit: ProductUnitEntity)

    @Delete
    suspend fun deleteProductUnit(unit: ProductUnitEntity)

    @Query("DELETE FROM product_units WHERE productID = :productID")
    suspend fun deleteUnitsForProduct(productID: Long)
}