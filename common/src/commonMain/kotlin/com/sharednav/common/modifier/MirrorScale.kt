package com.sharednav.common.modifier

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.sharednav.common.helper.EnableHelper
import com.xah.shader.skia.RuntimeShader
import com.xah.shader.skia.RuntimeShaderEffect

fun Modifier.scaleMirror(
    scale: Float,
    enabled : Boolean,
): Modifier =
    if(scale == 1f) {
        this
    } else if(!enabled || !EnableHelper.canShader) {
        this.drawWithContent {
            withTransform({
                scale(
                    scaleX = scale,
                    scaleY = scale,
                    pivot = Offset(size.width / 2f, size.height / 2f)
                )
            }) {
                this@drawWithContent.drawContent()
            }
        }
    } else {
        composed {
            // 绘制面
            var rect by remember { mutableStateOf<Rect?>(null) }
            this
                .graphicsLayer {
                    rect?.let { r ->
                        val runtimeShader = RuntimeShader(SHADER_CODE.trimIndent())
                        runtimeShader.setFloatUniform("size", r.width, r.height)
                        runtimeShader.setFloatUniform("scale", scale)

                        renderEffect = RuntimeShaderEffect(runtimeShader, "content")
                    }
                }
                .onGloballyPositioned { layoutCoordinates ->
                    val pos = layoutCoordinates.positionInWindow()
                    val size = layoutCoordinates.size
                    rect = Rect(
                        pos.x,
                        pos.y,
                        pos.x + size.width,
                        pos.y + size.height
                    )
                }
        }
    }

// 改进版 by Claude，无缝隙
private const val SHADER_CODE = """
    uniform shader content;
    uniform float2 size;
    uniform float scale;

    // 无缝镜像折叠：将任意坐标映射到 [0, maxVal] 的三角波
    float mirrorFold(float v, float maxVal) {
        float period = 2.0 * maxVal;
        // 先把负数折到正数范围
        v = abs(v);
        // 取模得到 [0, period) 内的值
        v = mod(v, period);
        // 超过 maxVal 的部分再折回来
        if (v > maxVal) v = period - v;
        return v;
    }

    half4 main(float2 fragCoord) {
        float2 center = size * 0.5;
        float2 offset = fragCoord - center;

        // 缩放
        float2 sampleCoord = center + offset / scale;

        // 镜面折叠（支持多次反射）
        sampleCoord.x = mirrorFold(sampleCoord.x, size.x);
        sampleCoord.y = mirrorFold(sampleCoord.y, size.y);

        // 收缩半像素，防止浮点误差导致边缘双线性采样混入透明像素
        sampleCoord = clamp(sampleCoord, float2(0.5), size - float2(0.5));

        return content.eval(sampleCoord);
    }
"""

/*
@Language("agsl")
private const val SHADER_CODE = """
    uniform shader content;
    uniform float2 size;   // 原始画面宽高
    uniform float scale;   // 缩放比例，

    half4 main(float2 fragCoord) {
        float2 center = size * 0.5;
        float2 offset = fragCoord - center;

        // 缩放
        float2 scaled = offset / scale;
        float2 sampleCoord = center + scaled;

        // 镜面反射逻辑
        if(sampleCoord.x < 0.0) sampleCoord.x = -sampleCoord.x;
        if(sampleCoord.x > size.x) sampleCoord.x = 2.0*size.x - sampleCoord.x;

        if(sampleCoord.y < 0.0) sampleCoord.y = -sampleCoord.y;
        if(sampleCoord.y > size.y) sampleCoord.y = 2.0*size.y - sampleCoord.y;

        return content.eval(sampleCoord);
    }
"""
 */
