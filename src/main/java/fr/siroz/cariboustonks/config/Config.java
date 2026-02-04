package fr.siroz.cariboustonks.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import fr.siroz.cariboustonks.config.configs.ChatConfig;
import fr.siroz.cariboustonks.config.configs.CombatConfig;
import fr.siroz.cariboustonks.config.configs.FarmingConfig;
import fr.siroz.cariboustonks.config.configs.FishingConfig;
import fr.siroz.cariboustonks.config.configs.ForagingConfig;
import fr.siroz.cariboustonks.config.configs.GeneralConfig;
import fr.siroz.cariboustonks.config.configs.HuntingConfig;
import fr.siroz.cariboustonks.config.configs.InstanceConfig;
import fr.siroz.cariboustonks.config.configs.MiscConfig;
import fr.siroz.cariboustonks.config.configs.SlayerConfig;
import fr.siroz.cariboustonks.config.configs.UIAndVisualsConfig;
import fr.siroz.cariboustonks.config.configs.VanillaConfig;

public class Config {

    @SerialEntry
    public int version = ConfigManager.CONFIG_VERSION;

    @SerialEntry
    public GeneralConfig general = new GeneralConfig();

	@SerialEntry
	public UIAndVisualsConfig uiAndVisuals = new UIAndVisualsConfig();

    @SerialEntry
    public ChatConfig chat = new ChatConfig();

    @SerialEntry
    public CombatConfig combat = new CombatConfig();

	@SerialEntry
	public InstanceConfig instance = new InstanceConfig();

	@SerialEntry
	public SlayerConfig slayer = new SlayerConfig();

    @SerialEntry
    public ForagingConfig foraging = new ForagingConfig();

	@SerialEntry
	public HuntingConfig hunting = new HuntingConfig();

    @SerialEntry
    public FarmingConfig farming = new FarmingConfig();

    @SerialEntry
    public FishingConfig fishing = new FishingConfig();

    @SerialEntry
    public MiscConfig misc = new MiscConfig();

	@SerialEntry
	public VanillaConfig vanilla = new VanillaConfig();
}
