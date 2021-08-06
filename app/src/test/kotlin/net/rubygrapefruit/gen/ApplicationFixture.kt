package net.rubygrapefruit.gen

class ApplicationFixture(
    val buildTree: BuildTreeFixture
) {
    fun assertHasBuildSrc() {
        buildTree.assertHasBuildSrc()
    }

    fun assertNoBuildSrc() {
        buildTree.assertNoBuildSrc()
    }

    fun assertHasPluginsBuild() {
        buildTree.assertHasBuild("plugins")
    }
}