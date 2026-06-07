package me.myogoo.beyondorbit.core.blockentity;

import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class ItemReceiverBlockEntity extends BlockEntity {
    private static final String ITEMS_TAG = "items";
    public static final int SLOT_COUNT = 9;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ItemReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(BeyondOrbitContent.ITEM_RECEIVER_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler items() {
        return items;
    }

    public int storedItemCount() {
        int total = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            total += items.getStackInSlot(i).getCount();
        }
        return total;
    }

    public int occupiedSlots() {
        int occupied = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                occupied++;
            }
        }
        return occupied;
    }

    public int insertFromPlayer(Player player, ItemStack heldStack) {
        if (heldStack.isEmpty()) {
            return 0;
        }
        ItemStack toInsert = heldStack.copy();
        for (int slot = 0; slot < items.getSlots() && !toInsert.isEmpty(); slot++) {
            toInsert = items.insertItem(slot, toInsert, false);
        }
        int inserted = heldStack.getCount() - toInsert.getCount();
        if (inserted <= 0) {
            return 0;
        }
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(inserted);
        }
        setChanged();
        return inserted;
    }

    public int collectToPlayer(Player player) {
        int moved = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack toMove = stack.copy();
            if (player.addItem(toMove)) {
                moved += stack.getCount();
                items.setStackInSlot(i, ItemStack.EMPTY);
            } else if (toMove.getCount() < stack.getCount()) {
                int accepted = stack.getCount() - toMove.getCount();
                moved += accepted;
                stack.shrink(accepted);
                items.setStackInSlot(i, stack);
            }
        }
        if (moved > 0) {
            setChanged();
        }
        return moved;
    }

    public List<ItemStack> copyStoredItems() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
    }

    public Component statusMessage() {
        return Component.translatable(
                "message.beyondorbit.item_receiver.status",
                storedItemCount(),
                occupiedSlots(),
                SLOT_COUNT
        );
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound(ITEMS_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(ITEMS_TAG, items.serializeNBT(registries));
    }
}
