pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        google()
        mavenCentral()
        gradlePluginPortal()

    }

  }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        google()
        mavenCentral()

        maven {
            url = uri("https://jitpack.io")
            credentials { username = "glpat-yVhPk2uKyM1sj5xxaHov"  }
        }
    }


}

rootProject.name = "InnovAllResources"
include(":app")
include(":geotracking")

