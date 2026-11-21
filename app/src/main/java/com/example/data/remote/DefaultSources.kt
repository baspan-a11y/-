package com.example.data.remote

import com.example.data.model.NewsCategory
import com.example.data.model.NewsSourceEntity

object DefaultSources {
    val entries: List<NewsSourceEntity> = listOf(
        NewsSourceEntity(
            id = "google_news_pao_all",
            name = "Ελληνικά ΜΜΕ • Παναθηναϊκός",
            feedUrl = "https://news.google.com/rss/search?q=%CE%A0%CE%B1%CE%BD%CE%B1%CE%B8%CE%B7%CE%BD%CE%B1%CF%8A%CE%BA%CF%8C%CF%82&hl=el&gl=GR&ceid=GR:el",
            websiteUrl = "https://news.google.com",
            description = "Συγκεντρωτική ροή από Sport24, Gazzetta, SDNA, NovaSports και μεγάλα αθλητικά portal.",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            accentColorHex = 0xFF006837
        ),
        NewsSourceEntity(
            id = "google_news_pao_football",
            name = "Ποδόσφαιρο • Παναθηναϊκός",
            feedUrl = "https://news.google.com/rss/search?q=%CE%A0%CE%B1%CE%BD%CE%B1%CE%B8%CE%B7%CE%BD%CE%B1%CF%8A%CE%BA%CF%8C%CF%82+%CF%80%CE%BF%CE%B4%CF%8C%CF%83%CF%86%CE%B1%CE%B9%CF%81%CE%BF&hl=el&gl=GR&ceid=GR:el",
            websiteUrl = "https://news.google.com",
            description = "Όλη η ποδοσφαιρική επικαιρότητα για Super League, Κύπελλο, Ευρώπη και μεταγραφές.",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            accentColorHex = 0xFF15803D
        ),
        NewsSourceEntity(
            id = "google_news_pao_basket",
            name = "Μπάσκετ • Παναθηναϊκός AKTOR",
            feedUrl = "https://news.google.com/rss/search?q=%CE%A0%CE%B1%CE%BD%CE%B1%CE%B8%CE%B7%CE%BD%CE%B1%CF%8A%CE%BA%CF%8C%CF%82+%CE%BC%CF%80%CE%AC%CF%83%CE%BA%CE%B5%CF%84+Euroleague&hl=el&gl=GR&ceid=GR:el",
            websiteUrl = "https://news.google.com",
            description = "Ειδήσεις Euroleague, Stoiximan GBL, ΟΑΚΑ και δηλώσεις για το μπάσκετ του ΠΑΟ.",
            defaultCategory = NewsCategory.BASKETBALL.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            accentColorHex = 0xFFC59B27
        ),
        NewsSourceEntity(
            id = "google_news_pao_ao",
            name = "Ερασιτέχνης • Παναθηναϊκός Α.Ο.",
            feedUrl = "https://news.google.com/rss/search?q=%CE%A0%CE%B1%CE%BD%CE%B1%CE%B8%CE%B7%CE%BD%CE%B1%CF%8A%CE%BA%CF%8C%CF%82+%CE%91%CE%9F+%CE%B2%CF%8C%CE%BB%CE%B5%CF%8A+%CF%80%CF%8C%CE%BB%CE%BF&hl=el&gl=GR&ceid=GR:el",
            websiteUrl = "https://news.google.com",
            description = "Νέα για βόλεϊ, πόλο, μπάσκετ γυναικών, στίβο και όλα τα τμήματα του Α.Ο.",
            defaultCategory = NewsCategory.AMATEUR.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            accentColorHex = 0xFF0D9488
        ),
        NewsSourceEntity(
            id = "trifilara",
            name = "Trifilara.gr",
            feedUrl = "https://trifilara.gr/feed/",
            websiteUrl = "https://trifilara.gr",
            description = "Αποκλειστικά νέα, παρασκήνιο και ρεπορτάζ για όλα τα τμήματα του Παναθηναϊκού.",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            accentColorHex = 0xFF059669
        ),
        NewsSourceEntity(
            id = "paopantou",
            name = "Paopantou.gr",
            feedUrl = "https://paopantou.gr/feed/",
            websiteUrl = "https://paopantou.gr",
            description = "Ανεξάρτητο πράσινο ρεπορτάζ, αναλύσεις αγώνων και μεταγραφικά νέα.",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            accentColorHex = 0xFF047857
        ),
        NewsSourceEntity(
            id = "newspao",
            name = "Newspao.gr",
            feedUrl = "https://newspao.gr/feed/",
            websiteUrl = "https://newspao.gr",
            description = "Συνεχής ροή ενημέρωσης για τον Παναθηναϊκό σε ποδόσφαιρο, μπάσκετ και Α.Ο.",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            accentColorHex = 0xFF16A34A
        ),
        NewsSourceEntity(
            id = "panathinaikos24",
            name = "Panathinaikos24.gr",
            feedUrl = "https://www.panathinaikos24.gr/feed/",
            websiteUrl = "https://www.panathinaikos24.gr",
            description = "24ωρη πράσινη ενημέρωση, δηλώσεις, συνεντεύξεις και αγωνιστικά ρεπορτάζ.",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = false,
            isEnabled = true,
            accentColorHex = 0xFF10B981
        ),
        NewsSourceEntity(
            id = "sdna_pao",
            name = "SDNA.gr (Φίλτρο ΠΑΟ)",
            feedUrl = "https://www.sdna.gr/rss.xml",
            websiteUrl = "https://www.sdna.gr/omades/panathinaikos",
            description = "Αυτόματο φιλτράρισμα από τη ροή του SDNA μόνο για θέματα Παναθηναϊκού.",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = true,
            isEnabled = true,
            accentColorHex = 0xFF2563EB
        ),
        NewsSourceEntity(
            id = "gazzetta_pao",
            name = "Gazzetta.gr (Φίλτρο ΠΑΟ)",
            feedUrl = "https://www.gazzetta.gr/rss.xml",
            websiteUrl = "https://www.gazzetta.gr/omades/panathinaikos",
            description = "Αυτόματο φιλτράρισμα από τη ροή του Gazzetta μόνο για ειδήσεις Παναθηναϊκού.",
            defaultCategory = NewsCategory.FOOTBALL.name,
            requiresPaoKeywordFilter = true,
            isEnabled = true,
            accentColorHex = 0xFFD97706
        )
    )
}
