package vazkii.quark.experimental.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import vazkii.quark.base.sounds.QuarkSounds;

public class EntityFrog extends EntityLiving {

	int spawnCd;
	
	public EntityFrog(World worldIn) {
		super(worldIn);
		spawnCd = -1;
	}
	
	@Override
	public void onEntityUpdate() {
		if(spawnCd > 0) {
			spawnCd--;
			if(spawnCd == 0 && !world.isRemote) {
				float mult = 0.8F;
				EntityFrog newFrog = new EntityFrog(world);
				newFrog.setPosition(posX, posY, posZ);
				newFrog.motionX = (Math.random() - 0.5) * mult;
				newFrog.motionY = (Math.random() - 0.5) * mult;
				newFrog.motionZ = (Math.random() - 0.5) * mult;
				world.spawnEntity(newFrog);
				newFrog.spawnCd = 2;
			}
		}
		
		super.onEntityUpdate();
	}
	
	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
		spawnCd = 50;
		if(!world.isRemote)
			world.playSound(null, posX, posY, posZ, QuarkSounds.WEDNESDAY, SoundCategory.NEUTRAL, 1F, 1F);
		
		return true;
	}

}
