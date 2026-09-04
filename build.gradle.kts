plugins {
    base
}

group = property("group")!!
version = property("version")!!

subprojects {
    group = rootProject.group
    version = rootProject.version

}