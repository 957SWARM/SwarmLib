# SwarmLib

<div align="center">
<a href="https://jitpack.io/#957SWARM/SwarmLib"><img src="https://jitpack.io/v/957SWARM/SwarmLib.svg"></a>
<a href="https://javadoc.jitpack.io/com/github/957SWARM/SwarmLib/latest/javadoc/"><img src ="https://img.shields.io/static/v1.svg?label=Javadocs&message=Release&color=l"></a>

<a href="https://github.com/957SWARM/SwarmLib/actions/workflows/main.yml"><img src="https://github.com/957SWARM/SwarmLib/actions/workflows/main.yml/badge.svg?branch=main"></a>
<a href="https://957SWARM.github.io/SwarmLib"><img src="https://github.com/957SWARM/SwarmLib/actions/workflows/docs.yml/badge.svg"></a>

<a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg"></a>

Reusable robot code for FIRST Robotics Competition Team 957.

<img src="https://github.com/957SWARM/SwarmLib/raw/main/logo.jpeg" width="350" height="350" />

<a href=https://www.team957.com>team957.com</a>

</div>

This library is fundamentally based on work originally done under FRC 997!

## Installation
Artifacts are published through JitPack, so installation is easy.
1) Add the JitPack repository:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
2) Add the dependency:
   1) For stable releases:
   ```groovy
    dependencies {
        ...
        implementation 'com.github.957SWARM:SwarmLib:1.3.1'
    }
   ``` 
   2) For development versions:
   ```groovy
    dependencies {
        ...
        implementation 'com.github.957SWARM:SwarmLib:dev-SNAPSHOT'
    }
   ```

## Contributing:
If you're a member of the Team997Coders org, you can simply create a branch inside this repo and make a pull request to `dev` when you're finished. Currently, 1 review is required and checks must pass to merge to `dev`. No direct commits to `main` are allowed.

For people outside of the org, follow the same steps, except with your own fork. We'll accept any useful PRs!

## License:
Licensed under the GNU GPLv3.
