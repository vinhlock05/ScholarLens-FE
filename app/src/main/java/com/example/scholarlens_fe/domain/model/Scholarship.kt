package com.example.scholarlens_fe.domain.model

/**
 * Domain model representing a Scholarship
 * Maps to backend API response structure
 */
data class Scholarship(
    val id: String,
    val scholarshipName: String, // Scholarship_Name
    val country: String? = null, // Country
    val startDate: String? = null, // Start_Date
    val endDate: String? = null, // End_Date
    val fundingLevel: String? = null, // Funding_Level
    val scholarshipType: String? = null, // Scholarship_Type
    val description: String? = null, // Description
    val eligibility: String? = null, // Eligibility
    val applicationProcess: String? = null, // Application_Process
    val website: String? = null, // Website
    val applicationMode: String? = null, // Application_Mode
    val eligibleFields: List<String>? = null, // Eligible_Fields (Field of Study)
    val amount: String? = null, // Amount
    val deadline: String? = null, // Deadline
    val duration: String? = null, // Duration
    val languageRequirements: String? = null, // Language_Requirements
    val gpaRequirement: String? = null, // GPA_Requirement
    val ageLimit: String? = null, // Age_Limit
    val workExperienceRequired: Boolean? = null, // Work_Experience_Required
    val publicationsRequired: Boolean? = null, // Publications_Required
    val contactEmail: String? = null, // Contact_Email
    val contactPhone: String? = null, // Contact_Phone
    val notes: String? = null, // Notes
    val score: Float? = null // Relevance score from search
)

/**
 * Filter criteria for scholarship search
 */
data class ScholarshipFilter(
    val keyword: String = "",
    val country: String? = null,
    val fundingLevel: String? = null,
    val scholarshipType: String? = null,
    val eligibleFields: List<String>? = null,
    val applicationMode: String? = null
)

/**
 * Search response from backend API
 */
data class ScholarshipSearchResponse(
    val total: Int,
    val items: List<ScholarshipHit>
)

/**
 * Scholarship hit from search API
 */
data class ScholarshipHit(
    val id: String,
    val score: Float,
    val source: ScholarshipSource
)

/**
 * Scholarship source data from API
 */
data class ScholarshipSource(
    val Scholarship_Name: String? = null,
    val Country: String? = null,
    val Start_Date: String? = null,
    val End_Date: String? = null,
    val Funding_Level: String? = null,
    val Scholarship_Type: String? = null,
    val Description: String? = null,
    val Eligibility: String? = null,
    val Application_Process: String? = null,
    val Website: String? = null,
    val Application_Mode: String? = null,
    val Eligible_Fields: Any? = null, // Can be String or List<String>
    val Amount: Any? = null, // Can be String or Number
    val Deadline: String? = null,
    val Duration: String? = null,
    val Language_Requirements: Any? = null,
    val GPA_Requirement: Any? = null,
    val Age_Limit: Any? = null,
    val Work_Experience_Required: Any? = null,
    val Publications_Required: Boolean? = null,
    val Contact_Email: String? = null,
    val Contact_Phone: String? = null,
    val Notes: String? = null
)

/**
 * Filter item for advanced filtering
 */
data class FilterItem(
    val field: String,
    val values: List<Any>,
    val operator: String = "OR" // "AND" or "OR"
)

