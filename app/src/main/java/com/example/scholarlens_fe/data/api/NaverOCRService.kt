package com.example.scholarlens_fe.data.api

import retrofit2.http.*
import okhttp3.RequestBody

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
    @POST("/custom/v1/36234/55ca75b3a0b95c5fe75af4fbbb6e2f0b5dcb5f7e04509a60e3b7ba5efc9fa1e6/document/cv")
    @Headers("Content-Type: application/json")
    suspend fun extractCVData(
        @Header("X-OCR-SECRET") secretKey: String,
        @Body request: ClovaOCRRequest
    ): retrofit2.Response<ClovaOCRResponse>
}