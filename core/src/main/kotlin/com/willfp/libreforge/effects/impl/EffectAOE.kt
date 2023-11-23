package com.willfp.libreforge.effects.impl

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.libreforge.ViolationContext
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.effects.impl.aoe.AOECompileData
import com.willfp.libreforge.effects.impl.aoe.AOEShapes
import com.willfp.libreforge.get
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.toFloat3
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.entity.LivingEntity

object EffectAOE : Effect<AOECompileData>("aoe") {
    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require("effects", "You must specify the effects!")
        require("shape", "You must specify a valid shape!", Config::getString) {
            AOEShapes[it] != null
        }
        inherit { AOEShapes[it.getString("shape")] }
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: AOECompileData): Boolean {
        val location = data.dispatcher.get<LivingEntity>()?.eyeLocation
            ?: data.dispatcher.location
            ?: return false

        val shape = compileData.shape ?: return false

        for (entity in shape.getEntities(
            location.toFloat3(),
            location.direction.toFloat3(),
            location.world,
            data
        ).filterNot { it.uniqueId == data.dispatcher.uuid }) {
            compileData.chain
                ?.trigger(
                    data.copy(
                        victim = entity,
                        location = entity.location
                    ).dispatch(data.dispatcher)
                )
        }

        return true
    }

    override fun makeCompileData(config: Config, context: ViolationContext): AOECompileData {
        return AOECompileData(
            AOEShapes.compile(config, context),
            Effects.compileRichChain(
                config,
                context.with("aoe effects")
            )
        )
    }
}
