package com.example.data

enum class ExamSection(
  val key: String,
  val displayName: String,
  val papersSubtitle: String,
  val description: String,
) {
  FOUNDATION(
    key = "Foundation",
    displayName = "Foundation",
    papersSubtitle = "Papers 1 – 4",
    description = "Business Laws, Accounting, Maths & Stats, Economics & Management",
  ),
  INTER(
    key = "Inter",
    displayName = "Inter",
    papersSubtitle = "Papers 5 – 12",
    description = "Corporate Laws, Accounting, Taxation, Costing, OM & SM, FM & Analytics",
  ),
  FINAL(
    key = "Final",
    displayName = "Final",
    papersSubtitle = "Papers 13 – 20C",
    description = "Strategic Management, Valuation, Tax, Audit, CFR, Risk & Entrepreneurship",
  );

  companion object {
    fun fromKey(key: String): ExamSection =
      entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: FOUNDATION
  }
}
