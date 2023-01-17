package com.progwml6.ironshulkerbox.common.network;

import com.progwml6.ironshulkerbox.common.block.entity.ICrystalShulkerBox;
import com.progwml6.ironshulkerbox.common.network.helper.IThreadsafePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class InventoryTopStacksSyncPacket implements IThreadsafePacket {

  private final BlockPos pos;
  private final NonNullList<ItemStack> topStacks;

  public InventoryTopStacksSyncPacket(NonNullList<ItemStack> topStacks, BlockPos pos) {
    this.topStacks = topStacks;
    this.pos = pos;
  }

  public InventoryTopStacksSyncPacket(FriendlyByteBuf buffer) {
    int size = buffer.readInt();
    NonNullList<ItemStack> topStacks = NonNullList.<ItemStack>withSize(size, ItemStack.EMPTY);

    for (int item = 0; item < size; item++) {
      ItemStack itemStack = buffer.readItem();

      topStacks.set(item, itemStack);
    }

    this.topStacks = topStacks;

    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(FriendlyByteBuf packetBuffer) {
    packetBuffer.writeInt(this.topStacks.size());

    for (ItemStack stack : this.topStacks) {
      packetBuffer.writeItem(stack);
    }

    packetBuffer.writeBlockPos(this.pos);
  }

  @Override
  public void handleThreadsafe(NetworkEvent.Context context) {
    HandleClient.handle(this);
  }

  /**
   * Safely runs client side only code in a method only called on client
   */
  private static class HandleClient {

    private static void handle(InventoryTopStacksSyncPacket packet) {
      Level world = Minecraft.getInstance().level;

      if (world != null) {
        BlockEntity te = world.getBlockEntity(packet.pos);

        if (te != null) {
          if (te instanceof ICrystalShulkerBox) {
            ((ICrystalShulkerBox) te).receiveMessageFromServer(packet.topStacks);

            Minecraft.getInstance().levelRenderer.blockChanged(null, packet.pos, null, null, 0);
          }
        }
      }
    }
  }
}
