package io.github.corbant.resonancelauncher.data.tmdb

object StreamingProviderMapping {
    private val PACKAGE_TO_PROVIDER_ID = mapOf(
        "com.netflix.ninja" to 8,                     // Netflix (Android TV)
        "com.amazon.amazonvideo.livingroom" to 9,      // Prime Video
        "com.disney.disneyplus" to 337,               // Disney+
        "com.wbd.stream" to 1899,                     // Max (formerly HBO Max)
        "com.apple.atve.androidtv.appletv" to 350,    // Apple TV+
        "com.hulu.livingroomplus" to 15,              // Hulu
        "com.google.android.youtube.tv" to 192,       // YouTube (Rent/Buy/Free)
        "com.peacocktv.peacockandroid" to 386,        // Peacock
        "com.paramountplus.tv" to 531,                // Paramount+
        "com.plexapp.android" to 538                  // Plex Free Movies & TV
    )

    private val PROVIDER_ID_TO_PACKAGE =
        PACKAGE_TO_PROVIDER_ID.entries.associate { (pkg, id) -> id to pkg }

    fun getPackageForProviderId(providerId: Int): String? = PROVIDER_ID_TO_PACKAGE[providerId]
    fun getProviderIdsForInstalledPackages(packageNames: Collection<String>): List<Int> {
        return packageNames.mapNotNull { PACKAGE_TO_PROVIDER_ID[it] }
    }
}