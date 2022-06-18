package ru.vtb.mssa.digi.integration.migr.service

interface AflService {
    fun getPartyUIdByUncId(uncId: String): Long
}
