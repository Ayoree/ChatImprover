ChatImprover
===========

[![GNU Affero General Public License](https://img.shields.io/github/license/Ayoree/ChatImprover?&logo=github)](LICENSE)
[![GitHub Workflows](https://github.com/Ayoree/ChatImprove-dexland/actions/workflows/build.yml/badge.svg)](https://github.com/Ayoree/ChatImprover/actions)
[![CodeFactor](https://www.codefactor.io/repository/github/Ayoree/ChatImprover/badge)](https://www.codefactor.io/repository/github/ayoree/chatimprover)

ChatImprover is a Minecraft mod, that allows you to create custom addons for editing received chat messages in a most flexible way.

API
------
### [API source](https://github.com/Ayoree/ChatImprover/tree/main/src/main/java/org/ayoree/chatimprover/api)
#### [Sample usage and addon template](https://github.com/Ayoree/ChatImprove-template)

### Dependency Information
Gradle
```gradle
repositories {
	maven {
        url = uri("https://maven.pkg.github.com/Ayoree/ChatImprover")
        credentials {
            username = 'Ayoree'
            password = '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'
        }
   }
}
```
```gradle
dependencies {
	modImplementation "org.ayoree:chatimprover:${chat_improver_version}"
}
```

Contributing
------
ChatImprover is an open source project, and gladly accepts community contributions.
