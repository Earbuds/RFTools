package mcjty.rftools;

import mcjty.rftools.apideps.ComputerCraftHelper;
import mcjty.rftools.blocks.blockprotector.BlockProtectors;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.blocks.logic.RedstoneChannels;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.security.SecurityChannels;
import mcjty.rftools.blocks.shield.ShieldSetup;
import mcjty.rftools.blocks.storage.RemoteStorageIdRegistry;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.commands.CommandRftCfg;
import mcjty.rftools.commands.CommandRftDb;
import mcjty.rftools.commands.CommandRftDim;
import mcjty.rftools.commands.CommandRftTp;
import mcjty.rftools.dimension.DimensionStorage;
import mcjty.rftools.dimension.ModDimensions;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.dimlets.DimletDropsEvent;
import mcjty.rftools.items.dimlets.DimletMapping;
import mcjty.rftools.network.DimensionSyncChannelHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;

@Mod(modid = RFTools.MODID, name="RFTools", dependencies = "required-after:Forge@["+RFTools.MIN_FORGE_VER+",);required-after:CoFHCore@["+RFTools.MIN_COFHCORE_VER+",)", version = RFTools.VERSION)
public class RFTools {
    public static final String MODID = "rftools";
    public static final String VERSION = "3.10";
    public static final String MIN_FORGE_VER = "10.13.2.1291";
    public static final String MIN_COFHCORE_VER = "1.7.10R3.0.0B9";

    @SidedProxy(clientSide="mcjty.rftools.ClientProxy", serverSide="mcjty.rftools.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance("rftools")
    public static RFTools instance;

    // Are some mods loaded?.
    public boolean enderio = false;
    public boolean mfr = false;
    public boolean jabba = false;
    public boolean mekanism = false;
    public boolean draconicevolution = false;

    public Logger logger;

    public static EnumMap<Side, FMLEmbeddedChannel> channels;

    /** This is used to keep track of GUIs that we make*/
    private static int modGuiIndex = 0;

    public static boolean debugMode = false;

    public ClientInfo clientInfo = new ClientInfo();

    public static CreativeTabs tabRfTools = new CreativeTabs("RfTools") {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return ModItems.rfToolsManualItem;
        }
    };
    public static CreativeTabs tabRfToolsDimlets = new CreativeTabs("RfToolsDimlets") {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return DimletSetup.unknownDimlet;
        }
    };

    public static final String SHIFT_MESSAGE = "<Press Shift>";

    /** Set our custom inventory Gui index to the next available Gui index */
    public static final int GUI_LIST_BLOCKS = modGuiIndex++;
    public static final int GUI_RF_MONITOR = modGuiIndex++;
    public static final int GUI_LIQUID_MONITOR = modGuiIndex++;
    public static final int GUI_CRAFTER = modGuiIndex++;
    public static final int GUI_STORAGE_SCANNER = modGuiIndex++;
    public static final int GUI_RELAY = modGuiIndex++;
    public static final int GUI_MATTER_TRANSMITTER = modGuiIndex++;
    public static final int GUI_MATTER_RECEIVER = modGuiIndex++;
    public static final int GUI_DIALING_DEVICE = modGuiIndex++;
    public static final int GUI_TELEPORTPROBE = modGuiIndex++;
    public static final int GUI_MANUAL_MAIN = modGuiIndex++;
    public static final int GUI_MANUAL_DIMENSION = modGuiIndex++;
    public static final int GUI_ENDERGENIC = modGuiIndex++;
    public static final int GUI_SEQUENCER = modGuiIndex++;
    public static final int GUI_COUNTER = modGuiIndex++;
    public static final int GUI_PEARL_INJECTOR = modGuiIndex++;
    public static final int GUI_TIMER = modGuiIndex++;
    public static final int GUI_ENDERMONITOR = modGuiIndex++;
    public static final int GUI_SHIELD = modGuiIndex++;
    public static final int GUI_DEVELOPERS_DELIGHT = modGuiIndex++;
    public static final int GUI_DIMLET_RESEARCHER = modGuiIndex++;
    public static final int GUI_DIMENSION_ENSCRIBER = modGuiIndex++;
    public static final int GUI_DIMENSION_BUILDER = modGuiIndex++;
    public static final int GUI_DIMLET_SCRAMBLER = modGuiIndex++;
    public static final int GUI_MACHINE_INFUSER = modGuiIndex++;
    public static final int GUI_DIMENSION_EDITOR = modGuiIndex++;
    public static final int GUI_ITEMFILTER = modGuiIndex++;
    public static final int GUI_SCREEN = modGuiIndex++;
    public static final int GUI_SCREENCONTROLLER = modGuiIndex++;
    public static final int GUI_DIMLET_WORKBENCH = modGuiIndex++;
    public static final int GUI_ENVIRONMENTAL_CONTROLLER = modGuiIndex++;
    public static final int GUI_SPAWNER = modGuiIndex++;
    public static final int GUI_MATTER_BEAMER = modGuiIndex++;
    public static final int GUI_DIMLET_FILTER = modGuiIndex++;
    public static final int GUI_SPACE_PROJECTOR = modGuiIndex++;
    public static final int GUI_BLOCK_PROTECTOR = modGuiIndex++;
    public static final int GUI_MODULAR_STORAGE = modGuiIndex++;
    public static final int GUI_REMOTE_STORAGE_ITEM = modGuiIndex++;
    public static final int GUI_MODULAR_STORAGE_ITEM = modGuiIndex++;
    public static final int GUI_REMOTE_STORAGE = modGuiIndex++;
    public static final int GUI_STORAGE_FILTER = modGuiIndex++;
    public static final int GUI_BUILDER = modGuiIndex++;
    public static final int GUI_CHAMBER_DETAILS = modGuiIndex++;
    public static final int GUI_SECURITY_MANAGER = modGuiIndex++;


    public static void logError(String msg) {
        instance.logger.log(Level.ERROR, msg);
    }

    public static long prevTicks = -1;
    public static void log(World world, TileEntity te, String message) {
        if (GeneralConfiguration.doLogging) {
            long ticks = world.getTotalWorldTime();
            if (ticks != prevTicks) {
                prevTicks = ticks;
                instance.logger.log(Level.INFO, "=== Time " + ticks + " ===");
            }
            String id = te.xCoord + "," + te.yCoord + "," + te.zCoord + ": ";
            instance.logger.log(Level.INFO, id + message);
        }
    }

    public static void log(String message) {
        instance.logger.log(Level.INFO, message);
    }

    public static void logDebug(String message) {
        if (debugMode) {
            instance.logger.log(Level.INFO, message);
        }
    }

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = LogManager.getLogger();
        MinecraftForge.EVENT_BUS.register(new DimletDropsEvent());
        this.proxy.preInit(e);
        FMLInterModComms.sendMessage("Waila", "register", "mcjty.rftools.apideps.WailaCompatibility.load");
        FMLInterModComms.sendMessage("JAKJ_RedstoneInMotion", "blacklistSoft", Block.blockRegistry.getNameForObject(ShieldSetup.invisibleShieldBlock));
        FMLInterModComms.sendMessage("JAKJ_RedstoneInMotion", "blacklistSoft", Block.blockRegistry.getNameForObject(ShieldSetup.solidShieldBlock));
        FMLInterModComms.sendMessage("JAKJ_RedstoneInMotion", "blacklistSoft", Block.blockRegistry.getNameForObject(ScreenSetup.screenHitBlock));
    }
    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @EventHandler
    public void init(FMLInitializationEvent e) {
        this.proxy.init(e);

        channels = NetworkRegistry.INSTANCE.newChannel("RFToolsChannel", DimensionSyncChannelHandler.instance);

        Achievements.init();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRftDim());
        event.registerServerCommand(new CommandRftTp());
        event.registerServerCommand(new CommandRftDb());
        event.registerServerCommand(new CommandRftCfg());
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        log("RFTools: server is starting");
        ModDimensions.initDimensions();
    }

    @EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        log("RFTools: server is stopping. Shutting down gracefully");
        RfToolsDimensionManager.cleanupDimensionInformation();
        TeleportDestinations.clearInstance();
        RfToolsDimensionManager.clearInstance();
        DimensionStorage.clearInstance();
        DimletMapping.clearInstance();
        RedstoneChannels.clearInstance();
        SecurityChannels.clearInstance();
        BlockProtectors.clearInstance();
        RemoteStorageIdRegistry.clearInstance();
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        this.proxy.postInit(e);

        enderio = Loader.isModLoaded("EnderIO");
        if (enderio) {
            RFTools.log("RFTools Detected EnderIO: enabling support");
        }
        mfr = Loader.isModLoaded("MineFactoryReloaded");
        if (mfr) {
            RFTools.log("RFTools Detected MineFactory Reloaded: enabling support");
        }
        jabba = Loader.isModLoaded("JABBA");
        if (jabba) {
            RFTools.log("RFTools Detected JABBA: enabling support");
        }
        mekanism = Loader.isModLoaded("Mekanism");
        if (mekanism) {
            RFTools.log("RFTools Detected Mekanism: enabling support");
        }
        draconicevolution = Loader.isModLoaded("DraconicEvolution");
        if (draconicevolution) {
            RFTools.log("RFTools Detected Draconic Evolution: enabling support");
        }

        if (Loader.isModLoaded("ComputerCraft")) {
            RFTools.log("RFTools Detected ComputerCraft: enabling support");
            ComputerCraftHelper.register();
        }
    }

    public static void message(EntityPlayer player, String message) {
        player.addChatComponentMessage(new ChatComponentText(message));
    }

    public static void warn(EntityPlayer player, String message) {
        player.addChatComponentMessage(new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
    }
}
