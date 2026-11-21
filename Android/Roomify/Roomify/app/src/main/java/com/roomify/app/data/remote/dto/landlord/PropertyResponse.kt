package com.roomify.app.data.remote.dto.landlord

import java.math.BigDecimal

data class PropertyResponse(
    val propertyId: Long,
    val propertyTypeId: Int,
    val propertyTypeName: String,
    val propertyName: String,
    val description: String?,
    val provinceId: Int,
    val provinceName: String,
    val wardId: Int,
    val wardName: String,
    val addressLine: String,
    val latitude: BigDecimal?,
    val longitude: BigDecimal?,
    val status: String,
    val roomCount: Int,
    val createdAt: String,
    val updatedAt: String
)