package enginecrafter77.survivalinc.client;


import enginecrafter77.survivalinc.SurvivalInc;
import enginecrafter77.survivalinc.block.ContainerCooler;
import enginecrafter77.survivalinc.block.ContainerHeater;
import enginecrafter77.survivalinc.block.TileEntityCooler;
import enginecrafter77.survivalinc.block.TileEntityHeater;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class GuiCooler extends GuiContainer {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 152;

    private static final ResourceLocation background = new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/container/heater.png");

    public GuiCooler(TileEntityCooler tileEntity, ContainerCooler container) {
        super(container);

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}