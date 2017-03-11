Neutrino
====

Neutrino is an addon to Configurate, adding the following features:

* Upgraded `ObjectMapper` (`NeutrinoObjectMapper`) that adds the following:
  * `Default` annotation, for specifying the default setting as the serialised string when a `null` object is assigned, 
  allowing for the prevention of loading of complex objects until it's sure the default is needed.
  * `DoNoGenerate` annotation, prevents a configuration setting from being generated unless it is not the default.
  * `ProcessSetting` annotation, for specifying extra transformations that need to be performed on the serialised string
  before or after object mapping. Some `SettingProcessor`s to set items in a list as lower case, or to remove the first `/`
  character from a setting have been included.
  * The ability to set a comment processor that can transform the `comment` string in the `@Setting` annotation into 
  something different, useful for localisation purposes.

Extra `TypeSerializers`
* `PatternTypeSerialiser` for retrieving a regex
* `SetTypeSerialiser` for retrieving a set

Whilst this was built for Nucleus, this can be used in any project that uses Configurate.