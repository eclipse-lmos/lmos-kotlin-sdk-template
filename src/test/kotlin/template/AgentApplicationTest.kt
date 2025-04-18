/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.template

import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.sdk.agents.WotConversationalAgent
import org.eclipse.lmos.sdk.agents.lastMessage
import org.eclipse.lmos.sdk.agents.toAgentRequest
import org.eclipse.thingweb.Wot
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest(
    classes = [AgentApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class AgentApplicationTest {

    private val logger = LoggerFactory.getLogger(AgentApplicationTest::class.java)

    private var port: Int = 8181

    @Autowired
    private lateinit var wot: Wot

    @Test
    fun testChat() = runBlocking {
        val agent = WotConversationalAgent.create(wot, "http://localhost:$port/chatagent")

        val answer: String = agent.chat("What is an Agent?".toAgentRequest()).lastMessage()
        logger.info(answer)
    }
}