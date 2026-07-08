package com.parshwa.create.radionautics.menu;

import com.parshwa.create.radionautics.radio.RadioRedstoneLink;
import com.parshwa.create.radionautics.registry.RadioMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BrassRadioLinkMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final BlockPos pos;
    private final Snapshot snapshot;

    public BrassRadioLinkMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readBlockPos(), Snapshot.read(buffer));
    }

    public BrassRadioLinkMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, pos, Snapshot.from(inventory, pos));
    }

    public BrassRadioLinkMenu(int containerId, Inventory inventory, BlockPos pos, Snapshot snapshot) {
        super(RadioMenus.BRASS_RADIO_LINK.get(), containerId);
        this.inventory = inventory;
        this.pos = pos;
        this.snapshot = snapshot;
    }

    public BlockPos pos() {
        return pos;
    }

    public RadioRedstoneLink blockEntity() {
        if (inventory.player.level().getBlockEntity(pos) instanceof RadioRedstoneLink link) {
            return link;
        }
        return null;
    }

    public Snapshot snapshot() {
        return snapshot;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    public record Snapshot(String frequency, boolean receiver) {
        public static Snapshot from(Inventory inventory, BlockPos pos) {
            if (inventory.player.level().getBlockEntity(pos) instanceof RadioRedstoneLink link) {
                return new Snapshot(link.frequency(), link.isReceiver());
            }
            return defaults();
        }

        public static Snapshot defaults() {
            return new Snapshot("145.500", false);
        }

        public static Snapshot read(RegistryFriendlyByteBuf buffer) {
            return new Snapshot(
                    buffer.readUtf(128),
                    buffer.readBoolean());
        }

        public void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeUtf(frequency, 128);
            buffer.writeBoolean(receiver);
        }
    }
}
