package com.example.scholarlens_fe.data.api

import retrofit2.http.*
import retrofit2.Response

data class ClovaOCRRequest(
    val version: String = "V2",
    val requestId: String,
    val timestamp: Long,
    val images: List<ClovaImage>
)

data class ClovaImage(
    val format: String = "pdf",
    val name: String = "cv",
    val data: String? = null,
    val url: String? = null
)

data class ClovaOCRResponse(
    val version: String,
    val requestId: String,
    val timestamp: Long,
    val images: List<ClovaImageResult>
)

data class ClovaImageResult(
    val uid: String,
    val name: String,
    val inferResult: String,
    val message: String,
    val validationResult: ClovaValidationResult?,
    val fields: List<ClovaField>
)

data class ClovaValidationResult(
    val result: String
)

data class ClovaField(
    val name: String,
    val valueType: String,
    val inferText: String,
    val inferConfidence: Double,
    val boundingPoly: ClovaBoundingPoly
)

data class ClovaBoundingPoly(
    val vertices: List<ClovaVertex>
)

data class ClovaVertex(
    val x: Double,
    val y: Double
)

interface ClovaOCRService {
    @POST("/external/v1/47847/578f0eec9e573e8211a0d8cbab98dd375403a31096af9b8b3f3deb35e2ec99ea")
    @Headers("Content-Type: application/json")
    suspend fun extractCVData(
        @Header("X-OCR-SECRET") secretKey: String,
        @Body request: ClovaOCRRequest
    ): Response<ClovaOCRResponse>
}