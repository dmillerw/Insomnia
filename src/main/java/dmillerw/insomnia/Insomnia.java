package dmillerw.insomnia;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

import java.util.List;

/**
 * @author dmillerw
 */
@Mod(modid = "Insomnia", name = "Insomnia", version = "%MOD_VERSION%")
public class Insomnia {

    @Mod.Instance("Insomnia")
    public static Insomnia instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(Insomnia.instance);
    }

    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event) {
        EntityPlayer entityPlayer = event.entityPlayer;
        EntityPlayer.EnumStatus enumStatus = EntityPlayer.EnumStatus.OK;

        if (!entityPlayer.worldObj.isRemote) {
            if (entityPlayer.isPlayerSleeping() || !entityPlayer.isEntityAlive()) {
                enumStatus = EntityPlayer.EnumStatus.OTHER_PROBLEM;
            }

            if (!entityPlayer.worldObj.provider.isSurfaceWorld()) {
                enumStatus = EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE;
            }

            if (entityPlayer.worldObj.isDaytime()) {
                enumStatus = EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW;
            }

            if (Math.abs(entityPlayer.posX - (double) event.x) > 3.0D || Math.abs(entityPlayer.posY - (double) event.y) > 2.0D || Math.abs(entityPlayer.posZ - (double) event.z) > 3.0D) {
                enumStatus = EntityPlayer.EnumStatus.TOO_FAR_AWAY;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List list = entityPlayer.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox((double) event.x - d0, (double) event.y - d1, (double) event.z - d0, (double) event.x + d0, (double) event.y + d1, (double) event.z + d0));

            if (!list.isEmpty()) {
                enumStatus = EntityPlayer.EnumStatus.NOT_SAFE;
            }
        }

        if (enumStatus == EntityPlayer.EnumStatus.OK) {
            ChunkCoordinates chunkCoordinates = new ChunkCoordinates(event.x, event.y, event.z);
            ChunkCoordinates bedCoordinates = entityPlayer.worldObj.getBlock(event.x, event.y, event.z).getBedSpawnPosition(entityPlayer.worldObj, chunkCoordinates.posX, chunkCoordinates.posY, chunkCoordinates.posZ, entityPlayer);
            if (bedCoordinates == null) {
                bedCoordinates = new ChunkCoordinates(chunkCoordinates.posX, chunkCoordinates.posY + 1, chunkCoordinates.posZ);
            }

            ChunkCoordinates verifiedCoordinates = EntityPlayer.verifyRespawnCoordinates(entityPlayer.worldObj, bedCoordinates, false);

            if (verifiedCoordinates == null) {
                verifiedCoordinates = new ChunkCoordinates(chunkCoordinates.posX, chunkCoordinates.posY, chunkCoordinates.posZ);
            }

            entityPlayer.setSpawnChunk(verifiedCoordinates, false);

            // To hide any possible chat message
            event.result = EntityPlayer.EnumStatus.OTHER_PROBLEM;

            // Then send our own
            entityPlayer.addChatComponentMessage(new ChatComponentTranslation(tile.bed.insomnia));
        } else {
            event.result = enumStatus;
        }
    }
}
