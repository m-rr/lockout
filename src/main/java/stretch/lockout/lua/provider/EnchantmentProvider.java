package stretch.lockout.lua.provider;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;

public class EnchantmentProvider implements LuaRegistryEnumWrapper<Enchantment> {
    private EnchantmentProvider() {}
    private static final EnchantmentProvider INSTANCE = new EnchantmentProvider();

    @Override
    public Enchantment[] values() {
        ArrayList<Enchantment> enchantments = new ArrayList<>();
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).iterator().forEachRemaining(enchantments::add);

        return enchantments.toArray(new Enchantment[0]);
    }

    public static EnchantmentProvider getINSTANCE() {
        return INSTANCE;
    }
}
