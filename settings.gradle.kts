rootProject.name = "job-portal-system"

include(
    ":common",
    ":common:common-application",
    ":common:common-data-access",
    ":common:common-domain",
    ":user-service",
    ":user-service:user-application",
    ":user-service:user-container",
    ":user-service:user-data-access",
    ":user-service:user-domain",
    ":user-service:user-domain:user-application-service",
    ":user-service:user-domain:user-domain-core",
)
