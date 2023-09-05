/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.Block
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.BlockPos
import net.ccbluex.liquidbounce.features.module.modules.color.ColorMixer
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "BlockOverlay", spacedName = "Block Overlay", description = "Allows you to change the design of the block overlay.", category = ModuleCategory.RENDER)
class BlockOverlay : Module() {
	private val colorRedValue = IntegerValue("Red", 68, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 117, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 135, 0, 255)
    val rainbowValue = ListValue("Color-Mode", arrayOf("CRainbow", "SkyRainbow", "LiquidSlowly", "Fade", "Mixer", "Lantern", "Custom"), "SkyRainbow")
    val infoValue = BoolValue("Info", false)

    val currentBlock: BlockPos?
        get() {
            val blockPos = mc.objectMouseOver?.blockPos ?: return null

            if (canBeClicked(blockPos) && mc.theWorld.worldBorder.contains(blockPos))
                return blockPos

            return null
        }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val blockPos = currentBlock ?: return
        val block = mc.theWorld.getBlockState(blockPos).block ?: return
        val partialTicks = event.partialTicks
        val rainbowMode = rainbowValue.get()
        val color = when(rainbowValue.get().toLowerCase()) {
           "crainbow" -> RenderUtils.getRainbowColor(2, 0.9f, 1.0f, 0)
           "skyainbow" -> RenderUtils.skyRainbow(0, 0.9f, 1.0f)
           "fade" -> ColorUtils.fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get()), 0, 100)
           "mixer" -> ColorMixer.getMixedColor(0, 2)
           "lantern" -> ColorUtils.lantern(0, 100)
           else -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
        }
        
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        RenderUtils.glColor(color)
        GL11.glLineWidth(2F)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)

        block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)

        val x = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks
        val y = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks
        val z = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks

        val axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-x, -y, -z)

        RenderUtils.drawSelectionBoundingBox(axisAlignedBB)
        RenderUtils.drawFilledBox(axisAlignedBB)
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (infoValue.get()) {
            val blockPos = currentBlock ?: return
            val block = getBlock(blockPos) ?: return

            val info = "${block.localizedName} §7ID: ${Block.getIdFromBlock(block)}"
            val scaledResolution = ScaledResolution(mc)

            /*RenderUtils.drawBorderedRect(
                    scaledResolution.scaledWidth / 2 - 2F,
                    scaledResolution.scaledHeight / 2 + 5F,
                    scaledResolution.scaledWidth / 2 + Fonts.font40.getStringWidth(info) + 2F,
                    scaledResolution.scaledHeight / 2 + 16F,
                    3F, Color.BLACK.rgb, Color.BLACK.rgb
            )*/
            GlStateManager.resetColor()
            Fonts.fontSFUI40.drawCenteredString(info, scaledResolution.scaledWidth / 2F, scaledResolution.scaledHeight / 2F + 6F, Color.WHITE.rgb)
        }
    }
}