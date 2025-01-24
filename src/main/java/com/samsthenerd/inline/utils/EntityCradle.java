package com.samsthenerd.inline.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.samsthenerd.inline.utils.cradles.EntTypeCradle;
import com.samsthenerd.inline.utils.cradles.NbtCradle;
import com.samsthenerd.inline.utils.cradles.PlayerCradle;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A fancy entity supplier with serialization.
 * <p>
 * EntityCradle is generally server safe, although calls to {@link EntityCradle#getEntity(World)}
 * aren't guaranteed to be.
 * 
 * @see EntTypeCradle
 * @see NbtCradle
 * @see PlayerCradle 
 */
public abstract class EntityCradle {
    public abstract CradleType<?> getType();

    /**
     * Supplies an entity wrapped by the cradle. 
     * This isn't guaranteed to be server-safe.
     * <p>
     * Implementations should try to cache their entity if possible.
     * @param world
     * @return an entity based on this cradle
     */
    @Nullable
    public abstract Entity getEntity(World world);

    /**
     * Gets an id representing this entity in some way. This is used for texture caching on glows (and maybe other stuff in the future).
     * @return string that can be used in an identifier/resloc.
     */
    public String getId(){ return null; }

    public static interface CradleType<C extends EntityCradle>{

        public Identifier getId();

        public Codec<C> getCodec();
    }

    private static final Map<Identifier, CradleType<? extends EntityCradle>> CRADLES = new HashMap<>();

    public static <T extends CradleType> T addCradleType(T cradleType){
        CRADLES.put(cradleType.getId(), cradleType);
        return cradleType;
    }

    private static final Codec<CradleType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(
        (id) -> {
            if(CRADLES.containsKey(id)){
                return DataResult.success(CRADLES.get(id));
            } else {
                return DataResult.error(() -> "No entity cradle type: " + id.toString());
            }
        }, (CradleType<?> type) -> {
            return type.getId();
        });

    public static final Codec<EntityCradle> CRADLE_CODEC = TYPE_CODEC.dispatch("type",
        EntityCradle::getType,
        cradTy -> cradTy.getCodec().fieldOf("entity")
    );
}
