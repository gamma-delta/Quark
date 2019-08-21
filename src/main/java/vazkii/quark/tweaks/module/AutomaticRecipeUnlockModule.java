package vazkii.quark.tweaks.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.CraftingScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.RecipeToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import vazkii.quark.base.handler.ReflectionKeys;
import vazkii.quark.base.module.Config;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;

@LoadModule(category = ModuleCategory.TWEAKS, hasSubscriptions = true)
public class AutomaticRecipeUnlockModule extends Module {

	@Config(description = "A list of recipe names that should NOT be added in by default")
	public static List<String> ignoredRecipes = Lists.newArrayList();

	@Config public static boolean forceLimitedCrafting = false;	
	@Config public static boolean disableRecipeBook = false;

	@SubscribeEvent 
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		PlayerEntity player = event.getPlayer();
		if(player instanceof ServerPlayerEntity) {
			ServerPlayerEntity spe = (ServerPlayerEntity) player;
			List<IRecipe<?>> recipes = new ArrayList<>(spe.getServer().getRecipeManager().getRecipes());
			recipes.removeIf((recipe) -> ignoredRecipes.contains(Objects.toString(recipe.getId())) || recipe.getRecipeOutput().isEmpty());
			player.unlockRecipes(recipes);

			if(forceLimitedCrafting)
				player.world.getGameRules().get(GameRules.DO_LIMITED_CRAFTING).set(true, spe.getServer());
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onInitGui(InitGuiEvent.Post event) {
		Screen gui = event.getGui();
		if(disableRecipeBook && (gui instanceof InventoryScreen || gui instanceof CraftingScreen)) {
			Minecraft.getInstance().player.getRecipeBook().setGuiOpen(false);
			
			List<Widget> widgets = event.getWidgetList();
			for(Widget w : widgets)
		 		if(w instanceof ImageButton) {
		 			event.removeWidget(w);
		 			return;
		 		}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void clientTick(ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player != null && mc.player.ticksExisted < 20) {
			ToastGui toasts = mc.getToastGui();
			Queue<IToast> toastQueue = ObfuscationReflectionHelper.getPrivateValue(ToastGui.class, toasts, ReflectionKeys.ToastGui.TOASTS_QUEUE);
			for(IToast toast : toastQueue)
				if(toast instanceof RecipeToast) {
					RecipeToast recipeToast = (RecipeToast) toast;
					List<ItemStack> stacks = ObfuscationReflectionHelper.getPrivateValue(RecipeToast.class, recipeToast, ReflectionKeys.RecipeToast.RECIPES);
					if(stacks.size() > 100) {
						toastQueue.remove(toast);
						return;
					}
				}
		}
	}
	
}