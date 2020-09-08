package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.specs.JvmClassName
import java.nio.file.Path

class SourceFileGenerator(private val textFileGenerator: TextFileGenerator) {
    fun java(srcDir: Path, className: JvmClassName): JavaSourceFileBuilder {
        return JavaSourceFileBuilderImpl(srcDir, className)
    }

    private inner class JavaSourceFileBuilderImpl(val srcDir: Path, val className: JvmClassName) : JavaSourceFileBuilder {
        private val imports = mutableListOf<String>()
        private val implements = mutableListOf<String>()
        private val methods = mutableListOf<String>()

        override fun imports(name: String) {
            imports.add(name)
        }

        override fun implements(name: String) {
            implements.add(name)
        }

        override fun method(text: String) {
            methods.add(text)
        }

        override fun complete() {
            textFileGenerator.file(srcDir.resolve(className.name.replace(".", "/") + ".java")) {
                if (className.packageName.isNotEmpty()) {
                    println()
                    print("package ")
                    print(className.packageName)
                    println(";")
                }
                if (imports.isNotEmpty()) {
                    println()
                    for (import in imports) {
                        print("import ")
                        print(import)
                        println(";")
                    }
                }
                println()
                print("public class ")
                print(className.simpleName)
                if (implements.isNotEmpty()) {
                    print(" implements ")
                    print(implements.joinToString(", "))
                }
                println(" {")
                for (method in methods) {
                    println(method)
                }
                println("}")
            }
        }
    }
}