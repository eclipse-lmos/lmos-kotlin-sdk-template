/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.template

import org.eclipse.lmos.arc.agents.dsl.AllTools
import org.eclipse.lmos.arc.spring.Agents
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AgentConfiguration {

    @Bean
    fun chatArcAgent(agent: Agents) = agent {
        name = "ChatAgent"
        prompt {
            """
            You are a professional assistant.  
        """.trimIndent()
        }
        model = { "GPT-4o" }
        tools = AllTools
    }
    @Bean
    fun agentEventListener(applicationEventPublisher: ApplicationEventPublisher) =
        ArcEventListener(applicationEventPublisher)
}
