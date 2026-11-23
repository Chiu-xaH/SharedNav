package com.xah.navigation.model.dest

/**
 * 为便于管理，不允许使用多级Path进行传参，只允许以query的形式传入基本参数
 *
 * Uri格式：
 *
 * scheme://host
 *
 * scheme://host?query1=xxx
 *
 * scheme://host?query1=xxx&query2=xxx
 *
 * scheme://host?query1=xxx&query2=xxx&query3=xxx
 *
 * ...
 *
 * 虽然格式要求很激进，但这样可以降低开发和管理成本
 */
class DeepLink<D : Destination>(
    val host: String,
    /**
     * 深链接解析为Dest的函数，有query参数直接解析query参数并组装成Destination，没有query参数则直接返回Destination即可
     */
    val parse: (DeepLinkUri) -> D?
)

/*
abstract class DeepLink<D : Destination>(
    val host: String
) {
    /**
     * Uri 映射为 Destination
     */
    abstract fun parse(uri: Uri): D?
}
 */