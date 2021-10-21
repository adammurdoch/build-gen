package net.rubygrapefruit.gen.specs

sealed class AppImplementationSpec

class CustomAppImplementationSpec: AppImplementationSpec()

class JavaAppImplementationSpec: AppImplementationSpec()
