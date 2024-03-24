package io.documentnode.epub4j.domain

enum class ManifestItemProperties(name: String) : ManifestProperties {
    COVER_IMAGE("cover-image"),
    MATHML("mathml"),
    NAV("nav"),
    REMOTE_RESOURCES("remote-resources"),
    SCRIPTED("scripted"),
    SVG("svg"),
    SWITCH("switch");
}
