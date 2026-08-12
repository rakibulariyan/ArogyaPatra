package com.aidwellness.arogyapatra.domain.usecase

data class TemplateValidationResult(
    val isValid: Boolean,
    val processedText: String,
    val unresolvedPlaceholders: List<String>
)

class ValidateTemplateUseCase {
    fun execute(
        templateText: String,
        values: Map<String, String>
    ): TemplateValidationResult {
        var processedText = templateText
        val regex = Regex("\\{\\{([a-zA-Z0-9_]+)\\}\\}")
        val matches = regex.findAll(templateText).map { it.groupValues[1] }.distinct().toList()

        for (key in matches) {
            val value = values[key]
            if (value != null) {
                processedText = processedText.replace("{{$key}}", value)
            }
        }

        // Check if any {{...}} remain unresolved
        val remaining = regex.findAll(processedText).map { it.groupValues[1] }.toList()
        return TemplateValidationResult(
            isValid = remaining.isEmpty(),
            processedText = processedText,
            unresolvedPlaceholders = remaining
        )
    }
}
