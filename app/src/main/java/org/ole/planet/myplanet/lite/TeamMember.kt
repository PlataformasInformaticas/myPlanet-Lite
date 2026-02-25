/**
 * Author: Walfre López Prado
 * Email: loppra@plataformasinformaticas.com
 * Creation date: 2025-11-27
 */

package org.ole.planet.myplanet.lite

import org.ole.planet.myplanet.lite.dashboard.DashboardTeamsRepository

data class TeamMember(
    val membership: DashboardTeamsRepository.MembershipDocument,
    val userProfile: DashboardTeamsRepository.UserDocument?
)
