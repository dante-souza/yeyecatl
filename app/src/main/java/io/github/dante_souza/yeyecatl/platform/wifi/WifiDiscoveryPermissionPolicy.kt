package io.github.dante_souza.yeyecatl.platform.wifi

enum class WifiDiscoveryPermission {
    FineLocation
}

enum class PermissionRequirement {
    Required,
    NotRequired
}

enum class PermissionGrantState {
    Available,
    NotGranted,
    RequiresSettingsAction,
    NotRequired
}

data class WifiDiscoveryPermissionStatus(
    val permission: WifiDiscoveryPermission?,
    val requirement: PermissionRequirement,
    val grantState: PermissionGrantState
) {
    val allowsDiscovery: Boolean
        get() = grantState == PermissionGrantState.Available ||
            grantState == PermissionGrantState.NotRequired

    val canRequestFromApp: Boolean
        get() = grantState == PermissionGrantState.NotGranted
}

object WifiDiscoveryPermissionPolicy {
    fun requirementFor(apiLevel: Int): PermissionRequirement =
        if (apiLevel >= 23) {
            PermissionRequirement.Required
        } else {
            PermissionRequirement.NotRequired
        }

    fun evaluate(
        apiLevel: Int,
        isGranted: Boolean,
        permissionRequestAttempted: Boolean,
        shouldShowRationale: Boolean
    ): WifiDiscoveryPermissionStatus {
        if (requirementFor(apiLevel) == PermissionRequirement.NotRequired) {
            return WifiDiscoveryPermissionStatus(
                permission = null,
                requirement = PermissionRequirement.NotRequired,
                grantState = PermissionGrantState.NotRequired
            )
        }

        val grantState = when {
            isGranted -> PermissionGrantState.Available
            permissionRequestAttempted && !shouldShowRationale -> PermissionGrantState.RequiresSettingsAction
            else -> PermissionGrantState.NotGranted
        }

        return WifiDiscoveryPermissionStatus(
            permission = WifiDiscoveryPermission.FineLocation,
            requirement = PermissionRequirement.Required,
            grantState = grantState
        )
    }
}
