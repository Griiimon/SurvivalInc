package enginecrafter77.survivalinc;

import java.awt.Color;

import enginecrafter77.survivalinc.client.RenderHUD;
import enginecrafter77.survivalinc.client.SimpleStatBar;
import enginecrafter77.survivalinc.client.SimpleStatContainer;
import enginecrafter77.survivalinc.config.ModConfig;
import enginecrafter77.survivalinc.ghost.GhostEnergyBar;
import enginecrafter77.survivalinc.ghost.RenderGhost;
import enginecrafter77.survivalinc.item.ItemCanteen;
import enginecrafter77.survivalinc.stats.impl.DefaultStats;
import enginecrafter77.survivalinc.stats.impl.HeatModifier;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		MinecraftForge.EVENT_BUS.register(RenderHUD.instance);
		
		if(ModConfig.GHOST.enabled) MinecraftForge.EVENT_BUS.register(new RenderGhost());
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
		
		SimpleStatContainer stats = new SimpleStatContainer();
		if(ModConfig.HEAT.enabled) stats.add(new SimpleStatBar(HeatModifier.instance, new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/heat.png"), new Color(0xE80000)));
		if(ModConfig.HYDRATION.enabled) stats.add(new SimpleStatBar(DefaultStats.HYDRATION, new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/hydration.png"), new Color(ItemCanteen.waterBarColor)));
		if(ModConfig.SANITY.enabled) stats.add(new SimpleStatBar(DefaultStats.SANITY, new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/sanity.png"), new Color(0xF6AF25)));
		if(ModConfig.WETNESS.enabled) stats.add(new SimpleStatBar(DefaultStats.WETNESS, new ResourceLocation(SurvivalInc.MOD_ID, "textures/gui/wetness.png"), new Color(0x0047D5)));		
		if(ModConfig.GHOST.enabled) RenderHUD.instance.add(new GhostEnergyBar());
		if(!stats.isEmpty()) RenderHUD.instance.add(stats);
	}
}