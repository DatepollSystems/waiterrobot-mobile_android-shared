package org.datepollsystems.waiterrobot.android.ui.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.android.ui.core.Preview
import org.datepollsystems.waiterrobot.android.util.bestContrastColor
import org.datepollsystems.waiterrobot.android.util.desaturateOnDarkMode
import org.datepollsystems.waiterrobot.android.util.getContentColor
import org.datepollsystems.waiterrobot.android.util.toColor
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.Allergen
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.Product
import org.datepollsystems.waiterrobot.shared.utils.euro

@Composable
fun Product(
    product: Product,
    groupColor: Color?,
    onSelect: () -> Unit,
) {
    val backgroundColor = (product.color.toColor() ?: groupColor)?.desaturateOnDarkMode()
    OutlinedButton(
        onClick = onSelect,
        enabled = !product.soldOut,
        shape = RoundedCornerShape(10),
        contentPadding = PaddingValues(5.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = backgroundColor?.getContentColor()
                ?: MaterialTheme.colorScheme.onSurface,
            containerColor = backgroundColor ?: Color.Unspecified,
            disabledContainerColor = backgroundColor?.copy(alpha = 0.5f) ?: Color.Unspecified,
        ),
        border = if (backgroundColor == null) ButtonDefaults.outlinedButtonBorder(!product.soldOut) else null,
        // elevation = ButtonDefaults.elevation() // TODO yes/no?
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                textDecoration = if (product.soldOut) TextDecoration.LineThrough else null
            )
            if (product.allergens.isNotEmpty()) {
                Text(
                    text = product.allergens.joinToString(", ") { it.shortName },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = backgroundColor?.bestContrastColor(Color.LightGray, Color.DarkGray)
                        ?: Color.LightGray
                )
            }
            Text(
                text = product.price.toString(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun ProductPreview() = Preview {
    Product(
        product = Product(
            id = 1,
            name = "Beer",
            price = 4.euro,
            soldOut = false,
            color = null,
            allergens = listOf(Allergen(1, "Egg", "E")),
            position = 1
        ),
        groupColor = null,
        onSelect = {}
    )
}
