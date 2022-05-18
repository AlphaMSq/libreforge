package com.willfp.libreforge.effects.effects

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.integrations.placeholder.PlaceholderManager
import com.willfp.libreforge.ConfigViolation
import com.willfp.libreforge.chains.EffectChain
import com.willfp.libreforge.chains.EffectChains
import com.willfp.libreforge.effects.CompileData
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.effects.NamedArgument
import com.willfp.libreforge.triggers.InvocationData
import com.willfp.libreforge.triggers.Triggers

class EffectRunChainInline : Effect(
    "run_chain_inline",
    applicableTriggers = Triggers.values()
) {
    override fun handle(invocation: InvocationData, config: Config) {
        val chain = (invocation.compileData as? EffectChainCompileData)?.data ?: return
        val namedArgs = mutableListOf<NamedArgument>()
        val args = config.getSubsection("chain_args")

        for (key in args.getKeys(false)) {
            namedArgs.add(
                NamedArgument(
                    key,
                    PlaceholderManager.translatePlaceholders(args.getString(key), invocation.player)
                )
            )
        }

        chain(invocation, namedArgs)
    }

    override fun validateConfig(config: Config): List<ConfigViolation> {
        val violations = mutableListOf<ConfigViolation>()

        if (!config.has("chain")) violations.add(
            ConfigViolation(
                "chain",
                "You must create a chain!"
            )
        )

        return violations
    }

    override fun makeCompileData(config: Config, context: String): CompileData? {
        val chain = EffectChains.compile(
            config.getSubsection("args").getSubsection("chain"),
            "$context Inline Chain",
            anonymous = true
        ) ?: return null
        return EffectChainCompileData(chain)
    }

    private class EffectChainCompileData(
        override val data: EffectChain
    ) : CompileData
}
