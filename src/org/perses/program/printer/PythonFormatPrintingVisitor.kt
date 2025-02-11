/*
 * Copyright (C) 2018-2022 University of Waterloo.
 *
 * This file is part of Perses.
 *
 * Perses is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * Perses is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Perses; see the file LICENSE.  If not see <http://www.gnu.org/licenses/>.
 */
package org.perses.program.printer

import org.perses.grammar.python3.Python3Lexer
import org.perses.program.AbstractTokenizedProgramPrinter.AbstractTokenPlacementListener
import org.perses.program.AbstractTokenizedProgramPrinter.AbstractTokenPositionProvider
import org.perses.program.PersesTokenFactory.PersesToken
import org.perses.program.TokenizedProgram
import org.perses.util.FastStringBuilder

class PythonFormatPrintingVisitor(
  program: TokenizedProgram,
  keepBlankLines: Boolean,
  val numSpacesPerIndent: Int,
  tokenPositionProvider: AbstractTokenPositionProvider,
  tokenPlacementListener: AbstractTokenPlacementListener?
) : AbstractOrigFormatPrintingVisitor(
  program, keepBlankLines, tokenPositionProvider, tokenPlacementListener
) {

  init {
    require(numSpacesPerIndent > 0) { numSpacesPerIndent }
  }

  private var indentLevel = 0

  override fun printNonEmptyLine(line: List<PersesToken>, builder: FastStringBuilder) {
    assert(line.isNotEmpty())
    printIndent(builder)
    printNonEmptyLine(
      startPositionInLine = getCharPositionInLine(line.first()),
      line = line,
      builder = builder
    )
  }

  private fun printIndent(builder: FastStringBuilder) {
    assert(indentLevel >= 0)
    if (indentLevel == 0) {
      return
    }
    assert(builder.lastChar() == '\n') { builder }
    val spaceCount = indentLevel * numSpacesPerIndent
    for (i in 1..spaceCount) {
      builder.append(' ')
    }
  }

  override fun isControlToken(token: PersesToken): Boolean {
    return when (token.type) {
      Python3Lexer.NEWLINE, Python3Lexer.INDENT, Python3Lexer.DEDENT -> true
      else -> false
    }
  }

  override fun visitControlToken(token: PersesToken) {
    when (token.type) {
      Python3Lexer.INDENT -> ++indentLevel
      Python3Lexer.DEDENT -> --indentLevel
    }
    check(indentLevel >= 0) { indentLevel }
  }

  override fun onVisitEnd() {
    check(indentLevel == 0) { indentLevel }
  }
}
