package io.axiom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.axiom.ui.theme.AxiomMist
import io.axiom.ui.theme.AxiomTextSecondary
import io.axiom.ui.theme.AxiomVoid

/**
 * Horizontal scrollable row of common programming symbols, shown above the
 * keyboard whenever the code editor is in focus.
 *
 * Layout:
 *  [SearchIcon | divider | ← scrollable symbols →]
 *
 * The search icon on the left switches back to [CommandBar] (caller handles the
 * mode change via [onSearchClick]).  Each symbol button calls [onSymbolClick]
 * with the string to insert at the cursor.
 */
@Composable
fun SymbolBar(
    onSymbolClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    accentColor:   Color    = AxiomMist,
    modifier:      Modifier = Modifier
) {
    val shape = RoundedCornerShape(30.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(
                elevation    = 6.dp,
                shape        = shape,
                spotColor    = accentColor.copy(alpha = 0.45f),
                ambientColor = accentColor.copy(alpha = 0.2f)
            )
            .clip(shape)
            .background(AxiomVoid)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.25f),
                        AxiomMist.copy(alpha = 0.08f),
                        accentColor.copy(alpha = 0.2f)
                    )
                ),
                shape = shape
            )
            .height(52.dp)
    ) {
        // ── Fixed left: search / command-bar icon ──────────────────────────────
        IconButton(
            onClick  = onSearchClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector        = Icons.Rounded.Search,
                contentDescription = "Switch to search",
                tint               = accentColor.copy(alpha = 0.8f),
                modifier           = Modifier.size(17.dp)
            )
        }

        // Divider
        Box(
            Modifier
                .width(1.dp)
                .height(16.dp)
                .background(AxiomMist.copy(alpha = 0.3f))
        )

        // ── Scrollable symbol buttons ──────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier       = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            items(SYMBOLS, key = { it.first }) { (label, insert) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 36.dp)
                        .padding(horizontal = 2.dp)
                        .clickable { onSymbolClick(insert) }
                ) {
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color      = AxiomTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize   = 14.sp
                        )
                    )
                }
            }
        }
    }
}

// ── Symbol definitions ─────────────────────────────────────────────────────────
// Pair(displayLabel, insertedString)

private val SYMBOLS = listOf(
    "⇥"  to "  ",      // tab → 2 spaces (matches hardcoded tabWidth)
    ";"  to ";",
    ":"  to ":",
    "."  to ".",
    ","  to ",",
    "{"  to "{",
    "}"  to "}",
    "("  to "(",
    ")"  to ")",
    "["  to "[",
    "]"  to "]",
    "<"  to "<",
    ">"  to ">",
    "="  to "=",
    "!"  to "!",
    "+"  to "+",
    "-"  to "-",
    "*"  to "*",
    "/"  to "/",
    "\"" to "\"",
    "'"  to "'",
    "`"  to "`",
    "&"  to "&",
    "|"  to "|",
    "?"  to "?",
    "@"  to "@",
    "_"  to "_",
    "\\" to "\\",
    "^"  to "^",
    "~"  to "~",
    "%"  to "%",
    "->" to "->",
    "=>" to "=>"
)
