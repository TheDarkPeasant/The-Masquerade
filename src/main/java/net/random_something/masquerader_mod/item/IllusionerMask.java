package net.random_something.masquerader_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.common.Mod;
import net.random_something.masquerader_mod.Config;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.client.model.MaskModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = MasqueraderMod.MOD_ID, value = Dist.CLIENT)
public class IllusionerMask extends ArmorItem {
    public IllusionerMask(ArmorMaterial p_40386_, Type p_266831_, Properties p_40388_) {
        super(p_40386_, p_266831_, p_40388_);
    }

    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> list, TooltipFlag p_41424_) {
        list.add(Component.translatable("tooltip.masquerader_mod.illusioner_mask").withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(p_41421_, p_41422_, list, p_41424_);
    }

    @Override
    public boolean canBeHurtBy(DamageSource p_41387_) {
        return false;
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (Config.alternateTextures.get())
            return MasqueraderMod.MOD_ID + ":textures/models/armor/alternate/illusioner_mask.png";

        return MasqueraderMod.MOD_ID + ":textures/models/armor/illusioner_mask.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> properties) {
                return new MaskModel<>(MaskModel.createArmorLayer().bakeRoot());
            }
        });
    }
}