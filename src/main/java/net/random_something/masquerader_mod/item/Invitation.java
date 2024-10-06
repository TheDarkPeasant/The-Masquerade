package net.random_something.masquerader_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.random_something.masquerader_mod.MasqueraderMod;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Invitation extends Item {
    public Invitation(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> list, TooltipFlag p_41424_) {
        list.add(Component.translatable("tooltip.masquerader_mod.invitation").withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(p_41421_, p_41422_, list, p_41424_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            TagKey<Structure> ballroomStructureTag = TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(MasqueraderMod.MOD_ID, "on_ballroom_explorer_maps"));

            BlockPos ballroomPos = serverLevel.findNearestMapStructure(
                    ballroomStructureTag,
                    player.blockPosition(),
                    100,
                    true
            );

            if (ballroomPos == null) return InteractionResultHolder.fail(player.getItemInHand(hand));

            ItemStack book = createBook();

            ItemStack map = MapItem.create(level, ballroomPos.getX(), ballroomPos.getZ(), (byte) 2, true, true);
            MapItem.renderBiomePreviewMap(serverLevel, map);
            MapItemSavedData.addTargetDecoration(map, ballroomPos, "+", MapDecoration.Type.MANSION);

            player.getItemInHand(hand).shrink(1);

            if (!player.getInventory().add(book)) {
                player.drop(book, false);
            }
            if (!player.getInventory().add(map)) {
                player.drop(map, false);
            }
        }

        if (level.isClientSide()) {
            player.playSound(SoundEvents.BOOK_PAGE_TURN);
        }

        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    private ItemStack createBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.getOrCreateTag().putString("title", "You're Invited!");
        book.getOrCreateTag().putString("author", "The Masquerader");

        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("As most of you who will receive this message know, I am hosting a grand masquerade ball as a social event for a select few. I am pleased to write that you are invited and highly encouraged to come."))));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("The ball will take place in my summer home, a very remote venue owned by me. The journey is long and arduous, so don't tarry now!\n\n     -The Masquerader"))));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("P.S. As renowned as all of you are, I am aware that you are still prone to misplacing things. Do NOT misplace this invitation; You will need it for admission."))));
        book.addTagElement("pages", pages);

        return book;
    }
}