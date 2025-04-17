/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.template

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


fun main(args: Array<String>) {
    runApplication<AgentApplication>(*args)
}

@SpringBootApplication
class AgentApplication {

}
