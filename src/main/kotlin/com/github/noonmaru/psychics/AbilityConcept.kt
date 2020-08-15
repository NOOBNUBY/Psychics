/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.noonmaru.psychics

import com.github.noonmaru.psychics.attribute.EsperStatistic
import com.github.noonmaru.psychics.damage.Damage
import com.github.noonmaru.psychics.tooltip.TooltipBuilder
import com.github.noonmaru.psychics.tooltip.addStats
import com.github.noonmaru.tap.config.*
import com.github.noonmaru.tap.template.renderTemplatesAll
import com.google.common.collect.ImmutableList
import net.md_5.bungee.api.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

@Name("common")
open class AbilityConcept {
    lateinit var name: String
        private set

    lateinit var container: AbilityContainer
        private set

    lateinit var psychicConcept: PsychicConcept
        private set

    /**
     * 표시 이름 (I18N)
     */
    @Config(required = false)
    lateinit var displayName: String
        protected set

    /**
     * 능력의 타입
     * * PASSIVE - 자동으로 적용되는 능력
     * * ACTIVE - 직접 사용하는 능력
     * * TOGGLE - 활성/비활성 가능한 능력
     *
     * Ability의 기본값은 PASSIVE이며
     * ActiveAbility의 기본값은 ACTIVE입니다.
     */
    @Config
    var type = AbilityType.PASSIVE
        protected set

    /**
     * 재사용 대기시간
     */
    @Config(required = false)
    @RangeInt(min = 0)
    var cooldownTicks = 0
        protected set

    /**
     * 마나 소모
     */
    @Config(required = false)
    @RangeDouble(min = 0.0)
    var cost = 0.0
        protected set

    /**
     * 시전 시간
     */
    @Config(required = false)
    @RangeInt(min = 0)
    var castingTicks = 0
        protected set

    /**
     * 시전 시간 -> 집중 시간
     * 스킬을 시전 시 외부에서 중단 가능
     */
    @Config(required = false)
    var interruptible = false
        protected set

    /**
     * 지속 시간
     */
    @Config(required = false)
    var durationTicks = 0
        protected set

    /**
     * 사거리
     */
    @Config(required = false)
    @RangeDouble(min = 0.0)
    var range = 0.0
        protected set

    /**
     * 피해량
     */
    @Config(required = false)
    var damage: Damage? = null
        protected set

    /**
     * 치유량
     */
    @Config(required = false)
    var healing: EsperStatistic? = null
        protected set


    @Config("wand", required = false)
    internal var _wand: ItemStack? = null

    /**
     * 능력과 상호작용하는 [ItemStack]
     */
    var wand
        get() = _wand?.clone()
        protected set(value) {
            _wand = value?.clone()
        }

    /**
     * 능력의 설명
     * 템플릿을 사용 가능합니다.
     * * $variable - Config의 값 활용하기
     * * <variable> 능력 내부 템플릿 값 활용하기
     */
    @Config
    var description: List<String> = ImmutableList.of()
        protected set

    internal fun initialize(
        name: String,
        container: AbilityContainer,
        psychicConcept: PsychicConcept,
        config: ConfigurationSection
    ): Boolean {
        this.name = name
        this.container = container
        this.psychicConcept = psychicConcept
        this.displayName = container.description.name
        if (container.abilityClass.isAssignableFrom(ActiveAbility::class.java)) {
            type = AbilityType.ACTIVE
        }

        val ret = computeConfig(config, true)

        this.description = ImmutableList.copyOf(description.renderTemplatesAll(config))

        return ret
    }

    internal fun renderTooltip(stats: (EsperStatistic) -> Double = { 0.0 }): TooltipBuilder {
        val tooltip = TooltipBuilder().apply {
            title = String.format("%s%s%-16s%s%16s", ChatColor.GOLD, ChatColor.BOLD, displayName, ChatColor.RESET, type)
            addStats(ChatColor.AQUA, "재사용 대기시간", cooldownTicks / 20.0, "초")
            addStats(ChatColor.DARK_AQUA, "마나 소모", cost)
            addStats(ChatColor.BLUE, "${if (interruptible) "집중" else "시전"} 시간", castingTicks / 20.0, "초")
            addStats(ChatColor.DARK_GREEN, "지속 시간", durationTicks / 20.0, "초")
            addStats(ChatColor.LIGHT_PURPLE, "사거리", range, "블록")
            addStats(ChatColor.GREEN, "치유량", "<healing>", healing)
            addStats("damage", damage)
            addDescription(description)
            addTemplates(
                "display-name" to displayName,
                "cooldown-time" to cooldownTicks / 20.0,
                "cost" to cost,
                "casting-time" to castingTicks / 20.0,
                "range" to range,
                "duration" to durationTicks / 20.0
            )
            damage?.let { addTemplates("damage" to stats(it.stats)) }
            healing?.let { addTemplates("healing" to stats(it)) }
        }

        runCatching { onRenderTooltip(tooltip, stats) }

        return tooltip
    }

    internal fun createAbilityInstance(): Ability<*> {
        return container.abilityClass.newInstance().apply {
            initConcept(this@AbilityConcept)
        }
    }

    /**
     * 필드 변수 적용 후 호출
     */
    open fun onInitialize() {}

    /**
     * 툴팁 요청 시 호출
     */
    open fun onRenderTooltip(tooltip: TooltipBuilder, stats: (EsperStatistic) -> Double) {}
}