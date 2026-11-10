# Rules propagated to consumers of this library.

# Data classes are (de)serialized by Moshi and referenced from data binding expressions.
-keep public class cc.chenhe.weargallery.common.bean.* { *; }
-keep class cc.chenhe.weargallery.common.jsonadapter.** { *; }
