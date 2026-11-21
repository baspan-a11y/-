package com.example.data.model

enum class NewsCategory(val labelEl: String) {
    ALL("Όλα"),
    FOOTBALL("Ποδόσφαιρο"),
    BASKETBALL("Μπάσκετ"),
    AMATEUR("Ερασιτέχνης"),
    TRANSFERS("Μεταγραφές");

    companion object {
        fun fromKey(key: String?): NewsCategory {
            return entries.firstOrNull { it.name == key } ?: FOOTBALL
        }
    }
}
