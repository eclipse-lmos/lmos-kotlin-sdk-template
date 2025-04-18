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
import org.junit.jupiter.api.Disabled
import org.slf4j.LoggerFactory
import kotlin.test.Test

class LocalAgentApplicationTest {

    private val logger = LoggerFactory.getLogger(LocalAgentApplicationTest::class.java)

    private var port: Int = 8181

    @Test()
    fun testChat() = runBlocking {
        val agent = WotConversationalAgent.create("http://localhost:$port/chatagent")

        val answer: String = agent.chat("What is an Agent?".toAgentRequest()).lastMessage()
        logger.info(answer)
    }
}