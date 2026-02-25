/**
 * Author: Walfre López Prado
 * Email: loppra@plataformasinformaticas.com
 * Creation date: 2025-12-12
 */

package org.ole.planet.myplanet.lite

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import org.ole.planet.myplanet.lite.dashboard.DashboardAvatarLoader
import org.ole.planet.myplanet.lite.databinding.ItemTeamMemberBinding

class DashboardTeamMembersAdapter(
    internal var avatarLoader: DashboardAvatarLoader
) :
    ListAdapter<TeamMember, DashboardTeamMembersAdapter.TeamMemberViewHolder>(
        TeamMemberDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberViewHolder {
        val binding =
            ItemTeamMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeamMemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamMemberViewHolder, position: Int) {
        val member = getItem(position)
        holder.bind(member)
    }

    inner class TeamMemberViewHolder(private val binding: ItemTeamMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(teamMember: TeamMember) {
            val member = teamMember.membership
            val userProfile = teamMember.userProfile
            val username = member.userId?.substringAfter("org.couchdb.user:")
            val fullName = if (userProfile != null) {
                "${userProfile.firstName} ${userProfile.lastName}"
            } else {
                username
            }

            binding.teamMemberName.text =
                fullName ?: itemView.context.getString(R.string.dashboard_team_members_unknown)

            val roleLabel = if (member.isLeader == true) {
                R.string.dashboard_team_members_leader_role
            } else {
                R.string.dashboard_team_members_member_role
            }
            binding.teamMemberRole.text = itemView.context.getString(roleLabel)

            val usernameLabel = username?.let {
                itemView.context.getString(R.string.dashboard_team_member_profile_username_format, it)
            } ?: itemView.context.getString(R.string.dashboard_team_members_unknown_username)
            binding.teamMemberUsername.text = usernameLabel

            val hasAvatar = userProfile?.attachments?.image != null
            val shouldAttemptAvatarLoad = hasAvatar || !username.isNullOrBlank()
            binding.teamMemberAvatar.setImageResource(R.drawable.ic_person_placeholder_24)
            avatarLoader.bind(binding.teamMemberAvatar, username, shouldAttemptAvatarLoad)
        }
    }

    private class TeamMemberDiffCallback : DiffUtil.ItemCallback<TeamMember>() {
        override fun areItemsTheSame(
            oldItem: TeamMember,
            newItem: TeamMember
        ): Boolean {
            return oldItem.membership.id == newItem.membership.id
        }

        override fun areContentsTheSame(
            oldItem: TeamMember,
            newItem: TeamMember
        ): Boolean {
            return oldItem == newItem
        }
    }
}
