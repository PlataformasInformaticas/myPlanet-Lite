/**
 * Author: Walfre López Prado
 * Email: loppra@plataformasinformaticas.com
 * Creation date: 2025-11-27
 */

package org.ole.planet.myplanet.lite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import org.ole.planet.myplanet.lite.dashboard.DashboardTeamsRepository

import kotlinx.coroutines.launch

class DashboardTeamMembersViewModel(
    private val teamsRepository: DashboardTeamsRepository,
    private val baseUrl: String,
    private val credentials: org.ole.planet.myplanet.lite.profile.StoredCredentials?,
    private val sessionCookie: String?
) : ViewModel() {

    private val _teamMembers = MutableLiveData<List<TeamMember>>()
    val teamMembers: LiveData<List<TeamMember>> = _teamMembers

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchTeamMembers(teamId: String) {
        viewModelScope.launch {
            val result = teamsRepository.fetchTeamMembers(baseUrl, credentials, sessionCookie, teamId)
            result.onSuccess { members ->
                val teamMembers = members.map { TeamMember(it, null) }
                _teamMembers.postValue(teamMembers)

                val userIds = members.mapNotNull { it.userId }
                val profilesResult = teamsRepository.fetchUserProfiles(baseUrl, credentials, sessionCookie, userIds)
                profilesResult.onSuccess { profiles ->
                    val updatedTeamMembers = members.map { member ->
                        val profile = profiles.find { it._id == member.userId }
                        TeamMember(member, profile)
                    }
                    _teamMembers.postValue(updatedTeamMembers)
                }.onFailure {
                    _error.postValue("Failed to fetch user profiles")
                }
            }.onFailure {
                _error.postValue("Failed to fetch team members")
            }
        }
    }

    fun filter(query: String) {
        val listToFilter = _teamMembers.value
        if (listToFilter != null) {
            val filteredList = listToFilter.filter { teamMember ->
                val username = teamMember.membership.userId?.substringAfter("org.couchdb.user:") ?: ""
                val userProfile = teamMember.userProfile
                val fullName = "${userProfile?.firstName} ${userProfile?.lastName}"
                username.contains(query, ignoreCase = true) || fullName.contains(query, ignoreCase = true)
            }
            _teamMembers.postValue(filteredList)
        }
    }
}

class DashboardTeamMembersViewModelFactory(
    private val teamsRepository: DashboardTeamsRepository,
    private val baseUrl: String,
    private val credentials: org.ole.planet.myplanet.lite.profile.StoredCredentials?,
    private val sessionCookie: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardTeamMembersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardTeamMembersViewModel(teamsRepository, baseUrl, credentials, sessionCookie) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
