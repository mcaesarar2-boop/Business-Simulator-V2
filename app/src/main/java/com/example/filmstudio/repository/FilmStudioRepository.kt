package com.example.filmstudio.repository

import com.example.data.PlayerState

/**
 * Clean interface for film studio state mutations and business operations.
 */
interface FilmStudioRepository {

    /**
     * Attempts to initiate production for a new movie project.
     * Returns Pair(success, updatedState).
     */
    fun produceMovie(
        currentState: PlayerState,
        instanceId: String,
        title: String,
        budget: Long,
        promoBudget: Long,
        genres: List<String>,
        isGlobal: Boolean,
        schedMonth: Int?,
        schedYear: Int?,
        filmFormat: String,
        productionFocus: String,
        scheduledReleaseDate: String?,
        targetDurationMonths: Int
    ): Pair<Boolean, PlayerState>

    /**
     * Cancels an ongoing film project and applies refund to the studio's cash.
     */
    fun cancelMovieProject(
        currentState: PlayerState,
        instanceId: String,
        projectTitle: String,
        refundAmount: Long
    ): PlayerState

    /**
     * Polishes a film in QC phase, increasing internal score at the cost of budget and extra time.
     */
    fun polishMovieProject(
        currentState: PlayerState,
        instanceId: String,
        projectTitle: String,
        budgetCost: Long,
        extraMonths: Int
    ): Pair<Boolean, PlayerState>

    /**
     * Sets the scheduled release date for a film that completed production or QC.
     */
    fun scheduleMovieRelease(
        currentState: PlayerState,
        instanceId: String,
        projectTitle: String,
        schedStr: String
    ): PlayerState

    /**
     * Activates a streaming license contract for an archived IP.
     */
    fun startStreamingLicense(
        currentState: PlayerState,
        instanceId: String,
        title: String,
        licenseeName: String,
        fee: Long,
        duration: Int
    ): PlayerState

    /**
     * Sells the IP of a finished film, crediting the studio (or creator if co-produced).
     */
    fun sellMovieIp(
        currentState: PlayerState,
        instanceId: String,
        title: String,
        sellPrice: Long
    ): PlayerState
}
