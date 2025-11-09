package com.example.scholarlens_fe.data.mapper

import com.example.scholarlens_fe.domain.model.Scholarship
import com.example.scholarlens_fe.domain.model.ScholarshipHit
import com.example.scholarlens_fe.domain.model.ScholarshipSource

/**
 * Mapper to convert API response to domain models
 */
object ScholarshipMapper {

    /**
     * Convert ScholarshipHit from API to Scholarship domain model
     */
    fun toScholarship(hit: ScholarshipHit): Scholarship {
        val source = hit.source
        return Scholarship(
            id = hit.id,
            scholarshipName = source.Scholarship_Name ?: "",
            country = source.Country,
            startDate = source.Start_Date,
            endDate = source.End_Date ?: source.Deadline,
            fundingLevel = source.Funding_Level,
            scholarshipType = source.Scholarship_Type,
            description = source.Description,
            eligibility = source.Eligibility,
            applicationProcess = source.Application_Process,
            website = source.Website,
            applicationMode = source.Application_Mode,
            eligibleFields = parseEligibleFields(source.Eligible_Fields),
            amount = parseAmount(source.Amount),
            deadline = source.Deadline,
            duration = source.Duration,
            languageRequirements = parseLanguageRequirements(source.Language_Requirements),
            gpaRequirement = parseGpaRequirement(source.GPA_Requirement),
            ageLimit = parseAgeLimit(source.Age_Limit),
            workExperienceRequired = parseBoolean(source.Work_Experience_Required),
            publicationsRequired = source.Publications_Required,
            contactEmail = source.Contact_Email,
            contactPhone = source.Contact_Phone,
            notes = source.Notes,
            score = hit.score
        )
    }

    /**
     * Parse Eligible_Fields which can be String or List<String>
     */
    private fun parseEligibleFields(fields: Any?): List<String>? {
        return when (fields) {
            null -> null
            is String -> listOf(fields)
            is List<*> -> fields.filterIsInstance<String>()
            else -> null
        }
    }

    /**
     * Parse Amount which can be String or Number
     */
    private fun parseAmount(amount: Any?): String? {
        return when (amount) {
            null -> null
            is String -> amount
            is Number -> amount.toString()
            else -> amount.toString()
        }
    }

    /**
     * Parse Language_Requirements which can be String or List<String>
     */
    private fun parseLanguageRequirements(requirements: Any?): String? {
        return when (requirements) {
            null -> null
            is String -> requirements
            is List<*> -> requirements.joinToString(", ")
            else -> requirements.toString()
        }
    }

    /**
     * Parse GPA_Requirement which can be String or Number
     */
    private fun parseGpaRequirement(gpa: Any?): String? {
        return when (gpa) {
            null -> null
            is String -> gpa
            is Number -> gpa.toString()
            else -> gpa.toString()
        }
    }

    /**
     * Parse Age_Limit which can be String or Number
     */
    private fun parseAgeLimit(age: Any?): String? {
        return when (age) {
            null -> null
            is String -> age
            is Number -> age.toString()
            else -> age.toString()
        }
    }

    /**
     * Parse boolean from Any type
     */
    private fun parseBoolean(value: Any?): Boolean? {
        return when (value) {
            null -> null
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull()
            else -> null
        }
    }
}

