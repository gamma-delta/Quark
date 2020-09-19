package vazkii.quark.base.world.config;

import net.minecraft.world.biome.Biome;
import vazkii.quark.base.module.IConfigType;

public interface IBiomeConfig extends IConfigType {

	public boolean canSpawn(Biome b);
	
}
