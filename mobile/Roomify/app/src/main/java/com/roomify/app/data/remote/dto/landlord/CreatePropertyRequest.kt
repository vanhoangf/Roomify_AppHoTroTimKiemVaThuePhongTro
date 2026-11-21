package com.roomify.app.data.remote.dto.landlord

import java.math.BigDecimal

data class CreatePropertyRequest(
    val propertyTypeId: Int,
    val propertyName: String,
    val description: String? = null,
    val provinceId: Int,
    val wardId: Int,
    val addressLine: String,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null
)