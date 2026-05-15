package com.seniorenlauncher.data.api

import retrofit2.http.GET

interface GitHubService {
    @GET("repos/cma58/senioren-launcher/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}
