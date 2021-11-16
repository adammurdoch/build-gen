package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.files.DslLanguage

class Parameters(
    val treeTemplate: BuildTreeTemplate,
    val implementation: Implementation,
    val availableOptions: List<OptionalParameter<*>>,
    val dsl: DslLanguage,
    val enabledOptions: Set<TemplateOption> = emptySet()
) {
    companion object {
        fun fromMap(args: Map<String, String>): Parameters {
            val params1 = RootParameters().options.filter { it.productionStructure.name == args.getValue("production-structure") }
            require(params1.size == 1)
            val params2 = params1.first().options.filter { it.buildLogic.name == args.getValue("build-logic") }
            require(params2.size == 1)
            val params3 = params2.first().options.filter { it.implementation.name == args.getValue("implementation") }
            require(params3.size == 1)
            var params = params3.first()
            params = params.withDslLanguage(DslLanguage.valueOf(args.getValue("dsl")))
            for (option in params.availableOptions) {
                if (option is BooleanParameter && args.get(option.templateOption.id) == "true") {
                    params = params.withOption(option.templateOption, true)
                }
            }
            return params
        }
    }

    override fun toString(): String {
        return implementation.toString()
    }

    fun asMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["production-structure"] = treeTemplate.productionTreeStructure.name
        map["build-logic"] = treeTemplate.buildLogic.name
        map["implementation"] = implementation.name
        map["dsl"] = dsl.name
        for (enabledOption in enabledOptions) {
            map[enabledOption.id] = "true"
        }
        return map
    }

    fun withOption(option: TemplateOption, enabled: Boolean): Parameters {
        val newOptions = if (enabled) enabledOptions + option else enabledOptions - option
        return Parameters(treeTemplate, implementation, availableOptions, dsl, newOptions)
    }

    fun enabled(option: TemplateOption) = enabledOptions.contains(option)

    fun withDslLanguage(dsl: DslLanguage): Parameters {
        return Parameters(treeTemplate, implementation, availableOptions, dsl, enabledOptions)
    }
}