package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class BuildTreeTemplate(private val display: String) {
    MainBuildOnly("single build") {
        override fun applyTo(builder: BuildTreeBuilder) {
        }
    },
    BuildSrc("build with buildSrc") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.addBuildSrc()
        }
    },
    BuildLogicChildBuild("build logic in child build") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.addBuildLogicBuild()
        }
    },
    BuildLogicChildBuildAndBuildSrc("build logic in buildSrc and child build") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.addBuildSrc()
            builder.addBuildLogicBuild()
        }
    };

    override fun toString() = display

    abstract fun applyTo(builder: BuildTreeBuilder)
}